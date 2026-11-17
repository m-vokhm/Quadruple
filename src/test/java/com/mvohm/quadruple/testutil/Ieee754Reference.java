package com.mvohm.quadruple.testutil;

import static com.mvohm.quadruple.Quadruple.*;

import java.math.BigInteger;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.Ieee754Bits;

/**
 * Reference conversion of IEEE-754 quadruple-precision values to Quadruple,
 * implemented independently of the library: with {@link BigInteger},
 * straight from the definition of the format, without any bit-shifting tricks.
 */
public class Ieee754Reference {

  private static final int  IEEE_EXPONENT_BIAS    = 16383;
  private static final int  IEEE_SIGNIFICAND_BITS = 112;
  private static final int  IEEE_EXPONENT_MAX     = 0x7FFF;   // NaN / Infinity
  private static final long LOWER_48_BITS         = 0x0000_FFFF_FFFF_FFFFL;

  private static final BigInteger MASK_64 = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

  /**
   * Returns a new Quadruple whose fields hold exactly the value of the given IEEE-754 quadruple:
   * <ul>
   *   <li>NaN (with any sign and payload) gives the canonical Quadruple NaN
   *       ({@code +, EXPONENT_OF_INFINITY, 0x8000_0000_0000_0000, 0});</li>
   *   <li>infinities and zeros keep their signs;</li>
   *   <li>for finite non-zero values: the value equals {@code s * 2^(e - 16383 - 112)}, where
   *       {@code s = 2^112 + f} and {@code e} is the exponent field for normal values,
   *       {@code s = f} and {@code e = 1} for subnormal ones ({@code f} is the 112-bit significand field).
   *       If {@code k} is the position of the most significant bit of {@code s}, the Quadruple
   *       has the unbiased exponent {@code k + e - 16383 - 112}, and its 128-bit fractional part of
   *       the mantissa is {@code (s - 2^k)} shifted left by {@code 128 - k}.</li>
   * </ul>
   */
  public static Quadruple expectedQuadruple(Ieee754Bits bits) {
    final boolean negative = bits.hi() < 0;
    final int ieeeExponent = (int)(bits.hi() >>> 48) & 0x7FFF;
    final BigInteger f = unsigned(bits.hi() & LOWER_48_BITS).shiftLeft(64).or(unsigned(bits.lo()));

    if (ieeeExponent == IEEE_EXPONENT_MAX) {
      return f.signum() != 0?
          new Quadruple(false,    EXPONENT_OF_INFINITY, 0x8000_0000_0000_0000L, 0) :   // NaN
          new Quadruple(negative, EXPONENT_OF_INFINITY, 0, 0);                         // Infinity
    }
    if (ieeeExponent == 0 && f.signum() == 0)
      return new Quadruple(negative, 0, 0, 0);                                        // Zero

    final BigInteger s = ieeeExponent == 0?
        f :                                                       // subnormal
        f.setBit(IEEE_SIGNIFICAND_BITS);                          // normal: implicit unity
    final int lsbExponent = Math.max(ieeeExponent, 1) - IEEE_EXPONENT_BIAS - IEEE_SIGNIFICAND_BITS;

    final int k = s.bitLength() - 1;                              // position of the MSB
    final BigInteger fraction128 = s.clearBit(k).shiftLeft(128 - k);
    final long mantHi = fraction128.shiftRight(64).longValue();
    final long mantLo = fraction128.and(MASK_64).longValue();
    return new Quadruple(negative, k + lsbExponent + EXPONENT_BIAS, mantHi, mantLo);
  }

  private static BigInteger unsigned(long value) {
    return new BigInteger(Long.toUnsignedString(value));
  }

}
