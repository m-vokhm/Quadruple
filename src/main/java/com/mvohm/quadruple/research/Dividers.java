package com.mvohm.quadruple.research;

import java.util.Arrays;
import static com.mvohm.quadruple.research.AuxMethods.*;
import static com.mvohm.quadruple.research.BigInteger.LONG_MASK;

/**
 * A sandbox for development of a new division algorithm.
 * Why BigDecimal division works faster than Quadruple?
 * @author M.Vokhmentev
 *
 */


public class Dividers {

  private static final boolean TALKATIVE = false;

  private static final long LOWER_32_BITS     = 0x0000_0000_FFFF_FFFFL;
  private static final long HIGHER_32_BITS    = 0xFFFF_FFFF_0000_0000L;

  private static final long[] BUFFER_10x32_B  = new long[10];

  private static long qdrAddBackCounter = 0, mbiAddBackCounter = 0;

  public static void clearAddBackCounters() {
    qdrAddBackCounter = mbiAddBackCounter = 0;
  }

  public static long getQdrAddBackCounter() {
    return qdrAddBackCounter;
  }

  public static long getMbiAddBackCounter() {
    return mbiAddBackCounter;
  }



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
  public static long divideArrays_1(long[] dividend, long[] divisor, long[] quotient) {
    return TALKATIVE?
        divideArrays_old_talkative(dividend, divisor, quotient):
        divideArrays_old_silent(dividend, divisor, quotient);
  }

  public static long divideArrays_old_talkative(long[] dividend, long[] divisor, long[] quotient) {
    final long[] remainder = dividend;            // will contain remainder after each iteration
    Arrays.fill(quotient, 0);

    final long divisorHigh = (divisor[0] << 32) | divisor[1];   // The most significant word of the divisor
    int offset = 0;                               // the index of the quotient word being computed
    quotient[offset++] = 1;                       // the integer part aka the implicit unity of the quotient is always 1

    say("\n<<< divideArrays_old");
    say("  r: " + hexStr_u(remainder));
    say("  d:           " + hexStr_u(divisor));
    subtractDivisor(divisor, remainder);          // Subtract divisor multiplied by 1 from the remainder
    say("  r: " + hexStr_u(remainder));
    say(" ---------------");

    // Compute the quotient by portions by 32 bits per iterations
    if (!isEmpty(remainder)) {
      do {
        final long remainderHigh = (remainder[offset + 1] << 32) | remainder[offset + 2]; // The most significant 64 bits of the remainder
        long quotientWord, qRemainder;
        if (remainder[offset] == 0) {
          quotientWord  = Long.divideUnsigned(remainderHigh, divisorHigh);
          qRemainder    = Long.remainderUnsigned(remainderHigh, divisorHigh);
        } else {
          quotientWord  = divide65bits(remainder[offset], remainderHigh, divisorHigh);
          qRemainder    = quotientRemainder;        // set by divide65bits() called above
        }

        say(" qw: " + hexStr(quotientWord));
        say(" qr: " + hexStr(qRemainder));

        // 21.06.10 15:55:38 Снизить вероятность обратного прибавления, проверить, не слишком ли велико q
        // Проверить выполнение неравенства q * v[n-2] > b * r + u[j + n - 2]
        /* */
        say("mul  %s       * %s", hexStr(quotientWord), hexStr(divisor[2]));
        final long lPart = quotientWord * divisor[2]; // [2]
        say("add  %s << 32 + %s", hexStr(qRemainder), hexStr(remainder[offset + 3]));
        final long rPart = (qRemainder << 32) + remainder[offset + 3];
        say("cmp  %s, %s", hexStr(lPart), hexStr(rPart));
        if (Long.compareUnsigned(lPart, rPart) > 0
            && qRemainder < 0x1_0000_0000L
            || quotientWord == 0x1_0000_0000L
        //    || lPart == 0xFFFF_FFFF_0000_0000L
        ) {
          say("I'd decrease the quotient word!");

          // Если оно удовлетворяется, то уменьшить q^ на 1, увеличить r^ на v[n-1]
          quotientWord--;
//          say("-qw: " + hexStr(quotientWord));
//          final long qw1 = quotientWord;
//          final long qr1 = qRemainder + divisor[1];
//
//          // и повторить эту проверку при r^ < b
//          lPart = qw1 * divisor[2];
//          rPart = (qr1 << 32) + remainder[offset + 3];
//          say("%s, %s", hexStr(lPart), hexStr(rPart));
//          if (Long.compareUnsigned(lPart, rPart) > 0)
//            say("I'd decrease the quotient word once more!");
//
        }
        /**/

        if (quotientWord != 0) {    // Multiply divisor by quotientWord and subtract the product from the remainder, adjust quotientWord
          multipyAndSubtract(divisor, quotientWord, offset, remainder);
          say("  r: " + hexStr_u(remainder));
          if (remainder[0] < 0) {                         // The quotiendWord occurred to be too great
            quotientWord--;                               // decrease it
            say("+qw: " + hexStr(quotientWord));
            addDivisorBack(divisor, remainder, offset);   // Add divisor * 1 back
            say("+ r: " + hexStr_u(remainder));
            qdrAddBackCounter++;
          }
        }

        quotient[offset++] = quotientWord;          // The next word of the quotient
      } while (offset <= 4 && !isEmpty(remainder));        // while the 5 half-words of the quotient are not filled and the remainder !=0
    } // if (!isEmpty(remainder)) {

    say(" ---------------");
    say("  q: " + hexStr_u(quotient));
    say(">>> divideArrays_old\n");

    return findNextBitOfQuotient(remainder, divisor);
  } // private static long divideArrays(long[] dividend, long[] divisor, long[] quotient) {

  public static long divideArrays_old_silent(long[] dividend, long[] divisor, long[] quotient) {
    final long[] remainder = dividend;            // will contain remainder after each iteration
    Arrays.fill(quotient, 0);

    final long divisorHigh = (divisor[0] << 32) | divisor[1];   // The most significant word of the divisor
    int offset = 0;                               // the index of the quotient word being computed
    quotient[offset++] = 1;                       // the integer part aka the implicit unity of the quotient is always 1
    subtractDivisor(divisor, remainder);          // Subtract divisor multiplied by 1 from the remainder

    // Compute the quotient by portions by 32 bits per iterations
    if (!isEmpty(remainder)) {
      do {
        final long remainderHigh = (remainder[offset + 1] << 32) | remainder[offset + 2]; // The most significant 64 bits of the remainder

        long quotientWord = (remainder[offset] == 0)?
            Long.divideUnsigned(remainderHigh, divisorHigh):
            divide65bits(remainder[offset], remainderHigh, divisorHigh);

  //****************************************************************
  // The performance gains aren't worth the extra cost
  // of more accurate computation of the quotientWord
  //****************************************************************
  //    long quotientWord, qRemainder;
  //
  //    if (remainder[offset] == 0) {
  //      quotientWord  = Long.divideUnsigned(remainderHigh, divisorHigh);
  //      qRemainder    = Long.remainderUnsigned(remainderHigh, divisorHigh);
  //    } else {
  //      quotientWord  = divide65bits(remainder[offset], remainderHigh, divisorHigh);
  //      qRemainder    = quotientRemainder;        // set by divide65bits() called above
  //    }
  //
  //    if (  quotientWord == 0x1_0000_0000L
  //          || ( qRemainder < 0x1_0000_0000L
  //               && Long.compareUnsigned( quotientWord * divisor[2],
  //                                       (qRemainder << 32) + remainder[offset + 3] ) > 0 )
  //     ) {
  //       quotientWord--;
  //     }
  //****************************************************************

        if (quotientWord != 0) {    // Multiply divisor by quotientWord and subtract the product from the remainder, adjust quotientWord
          multipyAndSubtract(divisor, quotientWord, offset, remainder);
          if (remainder[0] < 0) {                         // The quotiendWord occurred to be too great
            quotientWord--;                               // decrease it
            addDivisorBack(divisor, remainder, offset);   // Add divisor * 1 back
            qdrAddBackCounter++;
          }
        }

        quotient[offset++] = quotientWord;          // The next word of the quotient
      } while (offset <= 4 && !isEmpty(remainder));        // while the 5 half-words of the quotient are not filled and the remainder !=0
    } // (!isEmpty(remainder)) {

    return findNextBitOfQuotient(remainder, divisor);

  } // private static long divideArrays(long[] dividend, long[] divisor, long[] quotient) {

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
    quotientRemainder = remainder % divisor;
    return quotientHi << 16 | quotientLo;
  } // private static long divide65bits(long dividendHi, long dividendLo, long divisor) {

  private static long quotientRemainder;


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
    // 21.06.11 14:07:25
    // Сделаем временный дублёр с новым алгоритмом, чтобы проверить, эквивалентен ли он
    final long[] tmpRemainder = remainder;

    offset++;

//    final long[] partialProduct = BUFFER_10x32_B;
//    multDivisorBy(divisor, quotientWord, partialProduct, offset); // multiply divisor by qW with the given offset
//    subtractProduct(partialProduct, remainder, offset);           // and subtract the product from the remainder

    long prod = 0;
    for (int i = divisor.length - 1; i >= 0; i--) {         // product[offset..offset+4]
      prod = quotientWord * divisor[i];
      final long tmpRem = tmpRemainder[i + offset] - (prod & 0xFFFF_FFFFL);
      tmpRemainder[i + offset - 1] -= (prod >>> 32) - (tmpRem >>> 32);
      tmpRemainder[i + offset] = tmpRem & 0xFFFF_FFFFL;
    }
    if ((int)tmpRemainder[offset] < 0) {
      for (int i = offset - 1; i > 0; i--)
        tmpRemainder[i] = 0xFFFF_FFFFL;
      tmpRemainder[0] = -1L;
    }

//    if (!Arrays.equals(tmpRemainder, remainder))
//      say("Reminder differs: \n  old: %s\n  new: %s", hexStr_u(remainder), hexStr_u(tmpRemainder));
//    else
//      say("Reminders OK!");
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
        product[i + offset] = divisor[i] * quotientWord;
      for (int i = 4 + offset; i >= offset; i--) {         // product[offset..offset+4]
        product[i - 1] += product[i] >>> 32;               // propagate carry
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

/* *********************************************************************************
 * Stealed from MutableBigInteger and adapted to work with arrays instead of MutableBigInteger
 ***********************************************************************************/

  /**
   * Alternative divider, try to adapt the algorithm from BigDecimal
   * @param dividend
   * @param divisor
   * @param quotient
   * @return the next bit of the quotient (0 or 1)
   */
  public static long divideArrays_2(long[] dividend, long[] divisor, long[] quotient) {
//    // Temporarily use the old one
//    return divideArrays_old(dividend, divisor, quotient);
    final int[] dividend1 = longsToInts(dividend, 1);

    final MutableBigInteger mbiDivisor = new MutableBigInteger(longsToInts(divisor));
    Arrays.fill(quotient, 0);
    final MutableBigInteger mbiQuotient = new MutableBigInteger(longsToInts(quotient));

    final MutableBigInteger mbiRemainder = divideMagnitude(dividend1, 0, dividend1.length, mbiDivisor, mbiQuotient, true);
    final int offset = mbiQuotient.value.length - quotient.length;
    quotient = intsToLongs(mbiQuotient.value, quotient, offset);

//    say("quo: %s", hexStr_(mbiQuotient.value));
//    say("div: %s", hexStr_u(divisor));
//    say("rem: %s %s %s %s %s %s",
//        hexStr(mbiRemainder.value[3 + offset]),
//        hexStr(mbiRemainder.value[4 + offset]),
//        hexStr(mbiRemainder.value[5 + offset]),
//        hexStr(mbiRemainder.value[6 + offset]),
//        hexStr(mbiRemainder.value[7 + offset]),
//        hexStr(mbiRemainder.value[8 + offset]),
//        hexStr(mbiRemainder.value[9 + offset])
//        );
//    say("     %s", hexStr_(mbiRemainder.value));
//    say(offset > 0? "###": "---");
    if (greaterThanHalfOfDivisor(mbiRemainder.value, mbiDivisor.value, offset)) {
//    if (mbiRemainder.value[4 + lenDiff] != 0 || mbiRemainder.value[5  + lenDiff] < 0) {
//      say("return 1");
      return 1;
    }
//    say("return 0");
    return 0;
  }


  private static boolean greaterThanHalfOfDivisor(int[] remainder, int[] divisor, int offset) {
    final boolean result = false;
    for (int i = 0; i < 6; i++) {
      final int cmp = Integer.compareUnsigned(
            (remainder[4 + i + offset] << 1) + (remainder[5 + i + offset] >>> 31),  // Doubled remainder
            divisor[i]                                                              // Greater than divisor
          );
      if (cmp > 0)
        return true;
      if (cmp < 0)
        return false;
    }
    return true;
  }


  private static int[] longsToInts(long[] longs) {
    final int[] ints = new int[longs.length];
    for (int i = 0; i < longs.length; i++)
      ints[i] = (int)longs[i];
    return ints;
  }

  private static int[] longsToInts(long[] longs, int offset) {
    final int[] ints = new int[longs.length - offset];
    for (int i = 0; i < longs.length - offset; i++)
      ints[i] = (int)longs[i + offset];
    return ints;
  }

  private static long[] intsToLongs(int[] ints) {
    final long[] longs = new long[ints.length];
    for (int i = 0; i < longs.length; i++)
      longs[i] = ints[i] & 0xFFFF_FFFFL;
    return longs;
  }

  private static long[] intsToLongs(int[] ints, long[] longs) {
    if (ints.length != longs.length)
      throw new IllegalArgumentException(String.format("intsToLongs: the lengths of args differ: %s != %s",
                                                        ints.length, longs.length));
    for (int i = 0; i < longs.length; i++)
      longs[i] = ints[i] & 0xFFFF_FFFFL;
    return longs;
  }

  private static long[] intsToLongs(int[] ints, long[] longs, int offset) {
    if (ints.length != longs.length + offset)
      throw new IllegalArgumentException(String.format("intsToLongs: the lengths of args differ: %s != %s",
                                                        ints.length, longs.length + offset));
    for (int i = 0; i < longs.length; i++)
      longs[i] = ints[i + offset] & 0xFFFF_FFFFL;
    return longs;
  }


  /**
   * Divide this MutableBigInteger by the divisor.
   * The quotient will be placed into the provided quotient object &
   * the remainder object is returned.
   */
  private static MutableBigInteger divideMagnitude(
      int[] value,    // Instead of fields of this
      int offset,
      int intLen,

      MutableBigInteger mbiDivisor,
      MutableBigInteger mbiQuotient,
      boolean needRemainder ) {

//    say("\n<<< divideMagnitude");
    // assert div.intLen > 1
    // D1 normalize the divisor
    final int shift = Integer.numberOfLeadingZeros(mbiDivisor.value[mbiDivisor.offset]);
    // Copy divisor value to protect divisor
    final int dlen = mbiDivisor.intLen;
    int[] divisor;

    MutableBigInteger mbiRemainder; // Remainder starts as dividend with space for a leading zero
    if (shift > 0) {
      divisor = new int[dlen];
      copyAndShift(mbiDivisor.value, mbiDivisor.offset, dlen, divisor, 0, shift);
      if (Integer.numberOfLeadingZeros(value[offset]) >= shift) {
        final int[] remarr = new int[intLen + 1];
        mbiRemainder = new MutableBigInteger(remarr);
        mbiRemainder.intLen = intLen;
        mbiRemainder.offset = 1;
        copyAndShift(value,offset,intLen,remarr,1,shift);
      } else {
        final int[] remarr = new int[intLen + 2];
        mbiRemainder = new MutableBigInteger(remarr);
        mbiRemainder.intLen = intLen+1;
        mbiRemainder.offset = 1;
        int rFrom = offset;
        int c=0;
        final int n2 = 32 - shift;
        for (int i=1; i < intLen+1; i++,rFrom++) {
          final int b = c;
          c = value[rFrom];
          remarr[i] = (b << shift) | (c >>> n2);
        }
        remarr[intLen+1] = c << shift;
      }
    } else {
      divisor = Arrays.copyOfRange(mbiDivisor.value, mbiDivisor.offset, mbiDivisor.offset + mbiDivisor.intLen);
      mbiRemainder = new MutableBigInteger(new int[intLen + 1]);
      System.arraycopy(value, offset, mbiRemainder.value, 1, intLen);
      mbiRemainder.intLen = intLen;
      mbiRemainder.offset = 1;
    }

    final int nlen = mbiRemainder.intLen;

    // Set the quotient size
    final int limit = nlen - dlen + 1;
    if (mbiQuotient.value.length < limit) {
      mbiQuotient.value = new int[limit];
      mbiQuotient.offset = 0;
    }
    mbiQuotient.intLen = limit;
    final int[] q = mbiQuotient.value;

    // Must insert leading 0 in rem if its length did not change
    if (mbiRemainder.intLen == nlen) {
      mbiRemainder.offset = 0;
      mbiRemainder.value[0] = 0;
      mbiRemainder.intLen++;
    }

//    say("  r: " + hexStr_(rem.value));

    final int divisorHighWord = divisor[0];
    final long divisorHighLong = divisorHighWord & LONG_MASK;
    final int dl = divisor[1];

    // D2 Initialize j
    for (int j=0; j < limit-1; j++) {

      // D3 Calculate qhat
      // estimate qhat
      int qhat = 0;
      int qrem = 0;
      boolean skipCorrection = false;
      final int remainderHighWord = mbiRemainder.value[j + mbiRemainder.offset];
      final int nh2 = remainderHighWord + 0x80000000;
      final int remainderNextWord = mbiRemainder.value[j + 1 + mbiRemainder.offset];

      if (remainderHighWord == divisorHighWord) {
        qhat = ~0;
        qrem = remainderHighWord + remainderNextWord;
        skipCorrection = qrem + 0x80000000 < nh2;
      } else {
        final long nChunk = (((long)remainderHighWord) << 32) | (remainderNextWord & LONG_MASK);
        if (nChunk >= 0) {
          qhat = (int) (nChunk / divisorHighLong);
          qrem = (int) (nChunk - (qhat * divisorHighLong));
        } else {
          final long tmp = divWord(nChunk, divisorHighWord);
          qhat = (int) (tmp & LONG_MASK);
          qrem = (int) (tmp >>> 32);
        }
      }

      if (qhat == 0)
        continue;

      if (!skipCorrection) { // Correct qhat
        final long nl = mbiRemainder.value[j + 2 + mbiRemainder.offset] & LONG_MASK;
        long rs = ((qrem & LONG_MASK) << 32) | nl;
        long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

        if (unsignedLongCompare(estProduct, rs)) {
          qhat--;
          qrem = (int)((qrem & LONG_MASK) + divisorHighLong);
          if ((qrem & LONG_MASK) >=  divisorHighLong) {
            estProduct -= (dl & LONG_MASK);
            rs = ((qrem & LONG_MASK) << 32) | nl;
            if (unsignedLongCompare(estProduct, rs))
              qhat--;
          }
        }
      }
//      say(" qw: " + hexStr(qhat));

      // D4 Multiply and subtract
      mbiRemainder.value[j+mbiRemainder.offset] = 0;
      final int borrow = mulsub(mbiRemainder.value, divisor, qhat, dlen, j+mbiRemainder.offset);
//      say("  r: " + hexStr_(rem.value));

      // D5 Test remainder
      if (borrow + 0x80000000 > nh2) {
        // D6 Add back
        divadd(divisor, mbiRemainder.value, j+1+mbiRemainder.offset);
        qhat--;
        mbiAddBackCounter++;
//        say("+qw: " + hexStr(qhat));
//        say("+ r: " + hexStr_(rem.value));
      }

      // Store the quotient digit
      q[j] = qhat;
    } // D7 loop on j

    // D3 Calculate qhat
    // estimate qhat
    int qhat = 0;
    int qrem = 0;
    boolean skipCorrection = false;
    final int nh = mbiRemainder.value[limit - 1 + mbiRemainder.offset];
    final int nh2 = nh + 0x80000000;
    final int nm = mbiRemainder.value[limit + mbiRemainder.offset];

    if (nh == divisorHighWord) {
      qhat = ~0;
      qrem = nh + nm;
      skipCorrection = qrem + 0x80000000 < nh2;
    } else {
      final long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
      if (nChunk >= 0) {
          qhat = (int) (nChunk / divisorHighLong);
          qrem = (int) (nChunk - (qhat * divisorHighLong));
      } else {
          final long tmp = divWord(nChunk, divisorHighWord);
          qhat = (int) (tmp & LONG_MASK);
          qrem = (int) (tmp >>> 32);
      }
    }

    if (qhat != 0) {
      if (!skipCorrection) { // Correct qhat
        final long nl = mbiRemainder.value[limit + 1 + mbiRemainder.offset] & LONG_MASK;
        long rs = ((qrem & LONG_MASK) << 32) | nl;
        long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

        if (unsignedLongCompare(estProduct, rs)) {
          qhat--;
          qrem = (int) ((qrem & LONG_MASK) + divisorHighLong);
          if ((qrem & LONG_MASK) >= divisorHighLong) {
            estProduct -= (dl & LONG_MASK);
            rs = ((qrem & LONG_MASK) << 32) | nl;
            if (unsignedLongCompare(estProduct, rs))
              qhat--;
          }
        }
      }


//      say(" qw: " + hexStr(qhat));
      // D4 Multiply and subtract
      int borrow;
      mbiRemainder.value[limit - 1 + mbiRemainder.offset] = 0;
      if(needRemainder)
        borrow = mulsub(mbiRemainder.value, divisor, qhat, dlen, limit - 1 + mbiRemainder.offset);
      else
        borrow = mulsubBorrow(mbiRemainder.value, divisor, qhat, dlen, limit - 1 + mbiRemainder.offset);

//      say("  r: " + hexStr_(rem.value));
      // D5 Test remainder
      if (borrow + 0x80000000 > nh2) {
        // D6 Add back
        if(needRemainder)
            divadd(divisor, mbiRemainder.value, limit - 1 + 1 + mbiRemainder.offset);
        qhat--;
        mbiAddBackCounter++;
//        say("+qw: " + hexStr(qhat));
//        say("+ r: " + hexStr_(rem.value));
      }

      // Store the quotient digit
      q[(limit - 1)] = qhat;
    }

//    say("------------- ");
//    say("  q: " + hexStr_(mbiQuotient.value));
//    say("------------- normalize: ");
    if (needRemainder) {
        // D8 Unnormalize
        if (shift > 0)
            mbiRemainder.rightShift(shift);
        mbiRemainder.normalize();
    }
    mbiQuotient.normalize();
//    say("  q: " + hexStr_(mbiQuotient.value));
//    say(">>> exiting divideMagnitude");
    return needRemainder ? mbiRemainder : null;
  } // divideMagnitude(MutableBigInteger div, ...

  private static void copyAndShift(int[] src, int srcFrom, int srcLen, int[] dst, int dstFrom, int shift) {
    final int n2 = 32 - shift;
    int c=src[srcFrom];
    for (int i=0; i < srcLen-1; i++) {
      final int b = c;
      c = src[++srcFrom];
      dst[dstFrom+i] = (b << shift) | (c >>> n2);
    }
    dst[dstFrom+srcLen-1] = c << shift;
  }

  /**
   * This method divides a long quantity by an int to estimate
   * qhat for two multi precision numbers. It is used when
   * the signed value of n is less than zero.
   * Returns long value where high 32 bits contain remainder value and
   * low 32 bits contain quotient value.
   */
  private static long divWord(long n, int d) {
    final long dLong = d & LONG_MASK;
    long r;
    long q;
    if (dLong == 1) {
      q = (int)n;
      r = 0;
      return (r << 32) | (q & LONG_MASK);
    }

    q = (n >>> 1) / (dLong >>> 1);     // Approximate the quotient and remainder
    r = n - q*dLong;


    while (r < 0) {     // Correct the approximation
      r += dLong;
      q--;
    }
    while (r >= dLong) {
      r -= dLong;
      q++;
    }

    return (r << 32) | (q & LONG_MASK); // n - q*dlong == r && 0 <= r <dLong, hence we're done.
  }

  /**
   * Compare two longs as if they were unsigned.
   * Returns true iff one is bigger than two.
   */
  private static boolean unsignedLongCompare(long one, long two) {
      return (one+Long.MIN_VALUE) > (two+Long.MIN_VALUE);
  }

  /**
   * This method is used for division. It multiplies an n word input a by one
   * word input x, and subtracts the n word product from q. This is needed
   * when subtracting qhat*divisor from dividend.
   */
  private static int mulsub(int[] q, int[] a, int x, int len, int offset) {
      final long xLong = x & LONG_MASK;
      long carry = 0;
      offset += len;

      for (int j=len-1; j >= 0; j--) {
          final long product = (a[j] & LONG_MASK) * xLong + carry;
          final long difference = q[offset] - product;
          q[offset--] = (int)difference;
          carry = (product >>> 32)
                   + (((difference & LONG_MASK) >
                       (((~(int)product) & LONG_MASK))) ? 1:0);
      }
      return (int)carry;
  }

  /**
   * A primitive used for division. This method adds in one multiple of the
   * divisor a back to the dividend result at a specified offset. It is used
   * when qhat was estimated too large, and must be adjusted.
   */
  private static int divadd(int[] a, int[] result, int offset) {
    long carry = 0;

    for (int j=a.length-1; j >= 0; j--) {
      final long sum = (a[j] & LONG_MASK) +
                 (result[j+offset] & LONG_MASK) + carry;
      result[j+offset] = (int)sum;
      carry = sum >>> 32;
    }
    return (int)carry;
  }

  /**
   * The method is the same as mulsun, except the fact that q array is not
   * updated, the only result of the method is borrow flag.
   */
  private static int mulsubBorrow(int[] q, int[] a, int x, int len, int offset) {
      final long xLong = x & LONG_MASK;
      long carry = 0;
      offset += len;
      for (int j=len-1; j >= 0; j--) {
          final long product = (a[j] & LONG_MASK) * xLong + carry;
          final long difference = q[offset--] - product;
          carry = (product >>> 32)
                   + (((difference & LONG_MASK) >
                       (((~(int)product) & LONG_MASK))) ? 1:0);
      }
      return (int)carry;
  }




}
