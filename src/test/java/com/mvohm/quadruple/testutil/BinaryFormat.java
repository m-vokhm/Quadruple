package com.mvohm.quadruple.testutil;

import static com.mvohm.quadruple.Quadruple.*;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.function.BiPredicate;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.Ieee754Bits;

/**
 * A binary floating-point format, to which the model ({@link ExactValue}) can round its values:
 * the precision, the exponent range, the rule for ties, and the way to build a value of the target type {@code T}.
 * <p>
 * All formats have the same structure: normal values {@code 1.f * 2^e} with {@code precision} significant bits
 * (including the implicit unity) and {@code minExponent <= e <= maxExponent}, subnormal values
 * with the LSB weight equal to that of normal values with {@code e == minExponent}, and signed infinities and zeros.
 */
public final class BinaryFormat<T> {

  public enum Ties { AWAY_FROM_ZERO, TO_EVEN }

  /** Builds a finite value of the format: zero, subnormal or normal */
  private interface Encoder<T> {
    /**
     * @param subnormal  true for zero and subnormal values
     * @param exponent   the unbiased exponent of a normal value (undefined for subnormal ones)
     * @param fraction   for normal values, the fractional part of the mantissa (without the implicit unity),
     *                   as an integer of {@code precision - 1} bits; for subnormal values, the significand
     *                   in units of the LSB of subnormal values
     */
    T encode(boolean negative, boolean subnormal, long exponent, BigInteger fraction);
  }

  private interface InfinityEncoder<T> {
    T encode(boolean negative);
  }

  private static final long QUAD_BIAS = EXPONENT_BIAS;
  private static final BigInteger MASK_64 = ONE.shiftLeft(64).subtract(ONE);

  /** Quadruple: 128 bits of the fraction, half-up */
  public static final BinaryFormat<Quadruple> QUADRUPLE = new BinaryFormat<>("Quadruple",
      129, 1 - QUAD_BIAS, EXPONENT_OF_MAX_VALUE - QUAD_BIAS, Ties.AWAY_FROM_ZERO,
      (negative, subnormal, exponent, fraction) ->
          new Quadruple(negative, subnormal? 0 : (int)(exponent + QUAD_BIAS),
                        fraction.shiftRight(64).longValue(), fraction.and(MASK_64).longValue()),
      negative -> new Quadruple(negative, EXPONENT_OF_INFINITY, 0, 0),
      QuadAssert::sameFields);

  /** IEEE-754 quadruple precision (binary128), half-up (as {@code Quadruple.toIeee754Longs()} is documented) */
  public static final BinaryFormat<Ieee754Bits> IEEE754_QUAD = new BinaryFormat<>("IEEE-754 quad",
      113, -16382, 16383, Ties.AWAY_FROM_ZERO,
      (negative, subnormal, exponent, fraction) ->
          ieee754Bits(negative, subnormal? 0 : exponent + 16383, fraction),
      negative -> ieee754Bits(negative, 0x7FFF, ZERO),
      Ieee754Bits::equals);

  /** double, half-even */
  public static final BinaryFormat<Double> DOUBLE = new BinaryFormat<>("double",
      53, -1022, 1023, Ties.TO_EVEN,
      (negative, subnormal, exponent, fraction) -> Double.longBitsToDouble(
          (negative? Long.MIN_VALUE : 0) | (subnormal? 0 : (exponent + 1023) << 52) | fraction.longValue()),
      negative -> negative? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY,
      (a, b) -> Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b));

  /** float, half-even */
  public static final BinaryFormat<Float> FLOAT = new BinaryFormat<>("float",
      24, -126, 127, Ties.TO_EVEN,
      (negative, subnormal, exponent, fraction) -> Float.intBitsToFloat(
          (negative? Integer.MIN_VALUE : 0) | (subnormal? 0 : (int)(exponent + 127) << 23) | fraction.intValue()),
      negative -> negative? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY,
      (a, b) -> Float.floatToRawIntBits(a) == Float.floatToRawIntBits(b));

  private final String name;
  private final int precision;
  private final long minExponent;
  private final long maxExponent;
  private final Ties ties;
  private final Encoder<T> encoder;
  private final InfinityEncoder<T> infinityEncoder;
  private final BiPredicate<T, T> same;

  private BinaryFormat(String name, int precision, long minExponent, long maxExponent, Ties ties,
                       Encoder<T> encoder, InfinityEncoder<T> infinityEncoder, BiPredicate<T, T> same) {
    this.name = name;
    this.precision = precision;
    this.minExponent = minExponent;
    this.maxExponent = maxExponent;
    this.ties = ties;
    this.encoder = encoder;
    this.infinityEncoder = infinityEncoder;
    this.same = same;
  }

  /** @return the number of significant bits of normal values, including the implicit unity */
  public int precision()    { return precision; }
  /** @return the unbiased exponent of the minimum normal value */
  public long minExponent() { return minExponent; }
  /** @return the unbiased exponent of the maximum value */
  public long maxExponent() { return maxExponent; }
  public Ties ties()        { return ties; }

  /** @return the binary exponent of the weight of the LSB of subnormal values */
  public long minLsbExponent() {
    return minExponent - (precision - 1);
  }

  /** @return true if the two values are identical (bit-to-bit, so that 0.0 and -0.0 are different) */
  public boolean same(T a, T b) {
    return same.test(a, b);
  }

  public T infinity(boolean negative) {
    return infinityEncoder.encode(negative);
  }

  /**
   * Builds the value {@code ±significand * 2^lsbExponent}, which must be exactly representable
   * in this format or exceed its range (then the result is infinity).
   */
  T encode(boolean negative, BigInteger significand, long lsbExponent) {
    if (significand.signum() == 0)
      return encoder.encode(negative, true, 0, ZERO);
    final int k = significand.bitLength() - 1;                     // position of the MSB
    final long exponent = k + lsbExponent;                          // unbiased exponent of the value
    if (exponent > maxExponent)
      return infinity(negative);
    if (exponent < minExponent)                                     // subnormal, in units of the subnormal LSB
      return encoder.encode(negative, true, 0, shift(significand, lsbExponent - minLsbExponent()));
    return encoder.encode(negative, false, exponent,
                          shift(significand.clearBit(k), precision - 1 - k));
  }

  /** shifts left by n bits if n >= 0, otherwise right (the lost bits are zeros, since the value is representable) */
  private static BigInteger shift(BigInteger value, long n) {
    return n >= 0? value.shiftLeft(Math.toIntExact(n)) : value.shiftRight(Math.toIntExact(-n));
  }

  private static Ieee754Bits ieee754Bits(boolean negative, long biasedExponent, BigInteger fraction) {
    final long hi = (negative? Long.MIN_VALUE : 0) | (biasedExponent << 48) | fraction.shiftRight(64).longValue();
    return new Ieee754Bits(hi, fraction.and(MASK_64).longValue());
  }

  @Override
  public String toString() {
    return name;
  }

}
