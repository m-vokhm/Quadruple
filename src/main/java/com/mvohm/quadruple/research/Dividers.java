package com.mvohm.quadruple.research;

import java.util.Arrays;

/**
 * A sandbox for development of a new division algorithm.
 * Why BigDecimal division works faster than Quadruple?
 * @author M.Vokhmentev
 *
 */


public class Dividers {

  private static final long LOWER_32_BITS     = 0x0000_0000_FFFF_FFFFL;
  private static final long HIGHER_32_BITS    = 0xFFFF_FFFF_0000_0000L;

  private static final long[] BUFFER_10x32_B  = new long[10];

  /**
  * Divides an unpacked value held in the 10 longs of the {@code dividend)
  * by the value held in the 5 longs of the {@code divisor}
  * and fills 5 longs of the {@code quotient} with the quotient value.
  * All values are unpacked 129-bit values, containing integer parts
  * (implicit unities, always 1) in LSB of buff[0] (buff[1] for dividend)
  * and 128 bits of the fractional part in lower halves of buff[1] -- buff[4] (buff[2] -- buff[5] for dividend).
  * It uses the principle of the standard long division algorithm, with the difference
  * that instead of one decimal digit of the quotient at each step, the next 32 bits are calculated.
  * <br>Uses static arrays
  * <b><i>BUFFER_10x32_B</i></b>
  * @param dividend an unpacked value of the mantissa of the dividend (10 x 32 bits: 0, 1, dd1, dd2, dd3, dd4, 0, 0, 0, 0)
  * @param divisor an unpacked value of the mantissa of the divisor (5 x 32 bits: 1, dr1, dr2, dr3, dr4)
  * @param quotient a buffer that gets filled with the mantissa of the quotient
  * @return the next bit of the quotient (half the LSB), to be used for rounding the result
  * <br>Covered
  */
  public static long divideArrays_old(long[] dividend, long[] divisor, long[] quotient) {
     final long[] remainder = dividend;            // will contain remainder after each iteration
     Arrays.fill(quotient, 0);

     final long divisorHigh = (divisor[0] << 32) | divisor[1];   // The most significant word of the divisor
     int offset = 0;                               // the index of the quotient word being computed
     quotient[offset++] = 1;                       // the integer part aka the implicit unity of the quotient is always 1
     subtractDivisor(divisor, remainder);          // Subtract divisor multiplied by 1 from the remainder

     // Compute the quotient by portions by 32 bits per iterations
     if (!isEmpty(remainder))
       do {
         final long remainderHigh = (remainder[offset + 1] << 32) | remainder[offset + 2]; // The most significant 64 bits of the remainder
         long quotientWord = (remainder[offset] == 0)?
             Long.divideUnsigned(remainderHigh, divisorHigh):
             divide65bits(remainder[offset], remainderHigh, divisorHigh);

         if (quotientWord != 0) {    // Multiply divisor by quotientWord and subtract the product from the remainder, adjust quotientWord
           multipyAndSubtract(divisor, quotientWord, offset, remainder);
           if (remainder[0] < 0) {                         // The quotiendWord occurred to be too great
             quotientWord--;                               // decrease it
             addDivisorBack(divisor, remainder, offset);   // Add divisor * 1 back
           }
         }

         quotient[offset++] = quotientWord;          // The next word of the quotient
       } while (offset <= 4 && !isEmpty(remainder));        // while the 5 half-words of the quotient are not filled and the remainder !=0

   return findNextBitOfQuotient(remainder, divisor);
  } // private static long divideArrays(long[] dividend, long[] divisor, long[] quotient) {

  /**
   * Alternative divider, try to adapt the algorithm from BigDecimal
   * @param dividend
   * @param divisor
   * @param quotient
   * @return
   */
  public static long divideArrays_alt(long[] dividend, long[] divisor, long[] quotient) {
    // Temporarily use the old one
    return divideArrays_old(dividend, divisor, quotient);
  }


  /**
   * Subtracts the divisor from the dividend to obtain the remainder for the first iteration
   * @param divisor unpacked mantissa of the divisor, 1 + 4 x 32 bits, implicit integer 1 in divisor[0]
   * @param remainder unpacked mantissa of the dividend, 2 + 8 x 32 bits, implicit integer 1 in divisor[1]
   */
  private static void subtractDivisor(long[] divisor, long[] remainder) {
    for (int i = 5; i >= 1; i--) {
      remainder[i] -= divisor[i - 1];
      if ((remainder[i] & HIGHER_32_BITS) != 0) {
        remainder[i - 1]--;
        remainder[i] &= LOWER_32_BITS;
      }
    }
  } // private static void subtractDivisor(long[] divisor, long[] remainder) {

  /**
   * Checks if the buffer is empty (contains nothing but zeros)
   * @param buffer the buffer to check
   * @return {@code true} if the buffer is empty, {@code false} otherwise
   */
  private static boolean isEmpty(long[] buffer) {
    for (int i = 0; i < buffer.length; i++)
      if (buffer[i] != 0)
        return false;
    return true;
  } // private static boolean isEmpty(long[] buffer) {

  /**
   * Divides a dividend, consisting of more than 64 bits (and less than 81 bits),
   * by the given divisor, that may contain up to 33 bits.
   * @param dividendHi the most significant 64 bits of the dividend
   * @param dividendLo the least significant 64 bits of the dividend
   * @param divisor the divisor
   * @return the quotient
   */
  private static long divide65bits(long dividendHi, long dividendLo, long divisor) {
    final long shiftedDividend = dividendHi << 48 | dividendLo >>> 16;  // 16 bits of dividendHi and 48 bits of dividendLo
    final long quotientHi = shiftedDividend / divisor;                  // The most significant 32 bits of the quotient
    final long remainder = ((shiftedDividend % divisor) << 16) | (dividendLo & 0xFFFF);
    final long quotientLo = remainder / divisor;                        // The least significant 16 bits of the quotient
    return quotientHi << 16 | quotientLo;
  } // private static long divide65bits(long dividendHi, long dividendLo, long divisor) {

  /**
   * Multiplies the divisor by a newly found word of quotient,
   * taking into account the position of the word in the quotient ({@code offset} is the index
   * of the given word in the array that contains the quotient being calculated),
   * and subtracts the product from the remainder, to prepare the remainder for the next iteration
   * <br>Uses static arrays
   * <b><i>BUFFER_10x32_B</i></b>
   * @param divisor unpacked mantissa of the divisor, 1 + 4 x 32 bits, implicit integer 1 in divisor[0]
   * @param quotientWord a newly found word (32 bits) of the quotient being computed
   * @param offset the position (index) of the given {@code quotientWord} in the future quotient,
   *    defines the position of the product, that is subtracted from the remainder, relative to the latter
   * @param remainder the remainder to subtract the product from
   */
  private static void multipyAndSubtract(long[] divisor, long quotientWord, int offset, long[] remainder) {
    offset++;
    final long[] partialProduct = BUFFER_10x32_B;
    multDivisorBy(divisor, quotientWord, partialProduct, offset); // multiply divisor by qW with the given offset
    subtractProduct(partialProduct, remainder, offset);           // and subtract the product from the remainder
  } // private static void multipyAndSubtract(long[] divisor, long quotientWord, int offset, long[] remainder) {

  /**
   * Adds the divisor, shifted by offset words, back to remainder, to correct the remainder in case when
   * preliminarily estimated word of quotient occurred to be too great
   * @param divisor unpacked mantissa of the divisor, 1 + 4 x 32 bits, implicit integer 1 in divisor[0]
   * @param remainder unpacked mantissa of the dividend, 2 + 8 x 32 bits, implicit integer 1 in divisor[1]
   * @param offset
   */
  private static void addDivisorBack(long[] divisor, long[] remainder, int offset) {
    offset++;
    for (int i = 4 + offset; i >= 1; i--) {
      remainder[i] += i < offset? 0 : divisor[i - offset];
      if ((remainder[i] & HIGHER_32_BITS) != 0) {
        remainder[i - 1]++;
        remainder[i] &= LOWER_32_BITS;
      }
    }
  } // private static void addDivisorBack(long[] divisor, long[] remainder, int offset) {

  /**
   * After the basic division, finds the next bit of the quotient
   * (corresponding to 2^-129, i.e. half the least significant bit of the mantissa), to round up the quotient if this bit isn't 0.
   * Compares the remainder with the divisor and if the remainder >= 0.5 * divisor, returns 1, otherwise returns 0.
   * @param remainder the remainder (dividend - quotient * divisor ), unpacked (10 longs)
   * @param divisor  the divisor, unpacked (5 longs)
   * @return 1 if the remainder is equal to or greater than half the divisor
   */
  private static long findNextBitOfQuotient(long[] remainder, long[] divisor) {
    for (int i = 0; i < divisor.length - 1; i++) {
      final long rw = (remainder[i + 5] * 2 & LOWER_32_BITS) // a current word of the remainder multiplied by 2
                    + (remainder[i + 6] >> 31);              // MSB of the next word as the LSB
      if (rw > divisor[i]) return 1;
      if (rw < divisor[i]) return 0;
    }
    if ((remainder[9] * 2 & LOWER_32_BITS) < divisor[4]) return 0;
    return 1;
  } // private static long findNextBitOfQuotient(long[] remainder, long[] divisor) {

  /**
   * Multiplies the divisor by the 32 bits of the quotientWord with the given offset,
   * so that <br>{@code product[i + offset] = divisor[i] * quotientWord},}<br>
   * as if the quotientWord were the only non-zero item in a buffer containing 5 x 32 bits of a factor,
   * and were located in buff[offset]. The result is an 'unpacked' value, i. e. contains the value
   * in the 32 least significant bits of each long.
   * @param divisor unpacked mantissa of the divisor, 1 + 4 x 32 bits, implicit integer 1 in divisor[0]
   * @param quotientWord the 32 bits to multiply the divisor by
   * @param product a buffer to hold the result, 10 longs
   * @param offset the offset to add to the index when filling the resulting product
   * <br>Covered
   */
  private static void multDivisorBy(long[] divisor, long quotientWord, long[] product, int offset) {
    Arrays.fill(product, 0);            // Partial product
    if (quotientWord == 1)              // Just copy
      for (int i = 0; i < 5; i++)       // product has the most significant bit in halfword 1, product[0] is not used
        product[i + offset] = divisor[i];
    else {
      for (int i = 0; i < 5; i++)
        product[i + offset] = divisor[i] * quotientWord;   // product[offset..offset+4]
      for (int i = 4 + offset; i >= offset; i--) {         // propagate carry
        product[i - 1] += product[i] >>> 32;
        product[i] &= LOWER_32_BITS;
      }
    }
  } // private static void multDivisorBy(long[] divisor, long quotientWord, long[] product, int offset) {

  /**
   * Subtracts half-words of product[1] through product[4 + offset]
   * from the corresponding half-words of the remainder.
   * Propagates borrow from the least significant word of the result (remainder[4 + offset]) up to remainder[0].
   * Keeps the higher half-words of the remainder empty.
   * @param product the product to subtract from the remainder, as an unpacked 257-bit value
   * @param remainder the remainder to subtract product from, as an unpacked 257-bit value
   * @param offset the offset of the implied most significant word of 129-bit remainder
   * <br>Covered
   */
  private static void subtractProduct(long[] product, long[] remainder, int offset) {
    for (int i = 4 + offset; i >= 1; i--) {
      remainder[i] -= product[i];
      if ((remainder[i] & HIGHER_32_BITS) != 0) {
        remainder[i - 1]--;
        remainder[i] &= LOWER_32_BITS;
      }
    }
  } // private static void subtractProduct(long[] product, long[] remainder, int offset) {


}
