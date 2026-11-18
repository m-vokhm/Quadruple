package com.mvohm.quadruple.testutil;

import static com.mvohm.quadruple.Quadruple.*;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.mvohm.quadruple.Quadruple;

/**
 * A model of an ideal Quadruple, used as the reference for testing Quadruple arithmetic.
 * <p>
 * The model works the way Quadruple would work with infinite precision: it computes the result
 * of an operation <b>exactly</b>, and then <b>rounds it once</b>, correctly, to the nearest Quadruple
 * (see {@link #round()}). Besides the ideal result, the rounding tells how close the exact value is
 * to the midpoint between the two adjacent Quadruple values, which allows tests to tolerate
 * the known uncertainty of the library near the midpoint.
 * The model is deliberately straightforward, without any case analysis or optimization,
 * so that it is obviously correct and does not repeat the possible mistakes of the library.
 * <p>
 * Computing exactly is not always possible (1/3 or sqrt(2) have infinite binary expansions)
 * or practical ({@code 1 + 2^-2000000000} has 2 billion bits). But the rounding does not need the whole tail
 * of the value beyond the rounding position, it only needs to know whether the tail is less than, equal to,
 * or greater than a half of the LSB. So the model keeps a few hundred bits of the value, and compresses the rest
 * into a single "sticky" bit, which only says "there is something non-zero here". For the rounding,
 * this compression is lossless (like guard/round/sticky bits in hardware FPUs).
 * <p>
 * Technically, it is an exact binary value {@code ±mant * 2^exp2} ({@code mant} is a non-negative {@link BigInteger}),
 * implemented independently of the library.
 * <p>
 * Addition, subtraction and multiplication of exact values are exact.
 * Division and square root are computed with at least {@value #MIN_PRECISION} significant bits
 * and a "sticky" lowest bit that shows that the remainder is not zero.
 * The same is done for addition, when the smaller summand is so small, that it can only affect the rounding
 * (otherwise exponents like {@code 2^-2147483774} would require gigantic BigIntegers).
 * Such values are marked as inexact. An inexact value lies strictly between two adjacent integer multiples
 * of its lowest unit, and is represented by the odd one between them. Since the rounding position
 * is always at least {@value #MIN_PRECISION} - 129 bits higher, this does not affect the direction
 * of the rounding: the midpoint and the adjacent Quadruple values are even multiples of the lowest unit,
 * so the true value and its representation are always on the same side of them.
 * The distance to the midpoint, computed for inexact values, has an error less than 2^-250 ulp.
 * <p>
 * Operations on inexact values are not supported (they would lose this guarantee),
 * and throw {@link IllegalStateException}.
 * <p>
 * {@link #round()} rounds the value to Quadruple as the library is expected to do it: to the nearest value,
 * ties away from zero (half-up by magnitude), the same way for normal and subnormal results;
 * values greater than or equal to {@code MAX_VALUE + 0.5 ulp} become infinity,
 * values less than {@code MIN_VALUE / 2} become zero (keeping the sign).
 * {@link #round(BinaryFormat)} rounds it the same way to other binary formats (IEEE-754 quad, double, float),
 * with their precision, exponent range and rule for ties; this is the model for conversions of Quadruple
 * to these types. {@link #toLongTruncated()} and {@link #toIntTruncated()} model narrowing conversions to integers.
 * Special values (NaN, infinities) are not represented.
 */
public final class ExactValue {

  /** Min number of significant bits of inexact results */
  static final int MIN_PRECISION = 384;

  private static final long BIAS = EXPONENT_BIAS;
  /** The binary exponent of the weight of the least significant bit of subnormal values (and MIN_NORMAL) */
  private static final long MIN_LSB_EXPONENT = 1 - BIAS - 128;
  private static final BigInteger FIVE = BigInteger.valueOf(5);

  private final boolean negative;
  private final BigInteger mant;
  private final long exp2;
  private final boolean inexact;

  private ExactValue(boolean negative, BigInteger mant, long exp2, boolean inexact) {
    this.negative = negative;
    this.mant = mant;
    this.exp2 = exp2;
    this.inexact = inexact;
  }

  /* ************************************************************************
   * Creation
   */

  /** @return the exact value {@code ±mant * 2^exp2}; {@code mant} must not be negative */
  public static ExactValue of(boolean negative, BigInteger mant, long exp2) {
    if (mant.signum() < 0)
      throw new IllegalArgumentException("mant must not be negative: " + mant);
    return new ExactValue(negative, mant, exp2, false);
  }

  /** @return the exact value of the given finite Quadruple */
  public static ExactValue of(Quadruple q) {
    if (q.isNaN() || q.isInfinite())
      throw new IllegalArgumentException("Can't make an exact value of " + q);
    final long biased = q.exponent() & 0xFFFF_FFFFL;
    final BigInteger fraction = unsigned(q.mantHi()).shiftLeft(64).or(unsigned(q.mantLo()));
    if (biased == 0)                                                      // subnormal or zero
      return new ExactValue(q.isNegative(), fraction, MIN_LSB_EXPONENT, false);
    return new ExactValue(q.isNegative(), fraction.setBit(128), biased - BIAS - 128, false);
  }

  /** @return the exact value of the given finite double */
  public static ExactValue of(double d) {
    if (Double.isNaN(d) || Double.isInfinite(d))
      throw new IllegalArgumentException("Can't make an exact value of " + d);
    final long bits = Double.doubleToRawLongBits(d);
    final int exponent = (int)(bits >>> 52) & 0x7FF;
    final long fraction = bits & 0x000F_FFFF_FFFF_FFFFL;
    final long significand = exponent == 0? fraction : fraction | 0x0010_0000_0000_0000L;
    return new ExactValue(bits < 0, BigInteger.valueOf(significand), Math.max(exponent, 1) - 1023 - 52, false);
  }

  /** @return the exact value of the given long */
  public static ExactValue of(long value) {
    return new ExactValue(value < 0, BigInteger.valueOf(value).abs(), 0, false);
  }

  /* ************************************************************************
   * Properties
   */

  public boolean isNegative() { return negative; }
  public boolean isZero()     { return mant.signum() == 0; }
  /** @return false if the value is only an approximation of the true result (see the class description) */
  public boolean isExact()    { return !inexact; }

  /* ************************************************************************
   * Arithmetic
   */

  public ExactValue negate() {
    return new ExactValue(!negative, mant, exp2, inexact);
  }

  public ExactValue add(ExactValue other) {
    requireExact(this, other);
    if (other.isZero()) return isZero()? new ExactValue(negative && other.negative, ZERO, 0, false) : this;
    if (isZero())       return other;

    ExactValue big = this, small = other;
    if (small.top() > big.top()) { big = other; small = this; }

    final int keep = Math.max(big.mant.bitLength(), MIN_PRECISION);
    if (big.top() - small.top() > keep + 2) {
      // The small one is less than the lowest unit of the representation below, it can only affect rounding.
      // big * 2^(g + 1) ± 1 unit, where |small| < 1 unit
      final int g = keep - big.mant.bitLength();
      final BigInteger m = big.mant.shiftLeft(g + 1);
      return new ExactValue(big.negative,
                            big.negative == small.negative? m.add(ONE) : m.subtract(ONE),
                            big.exp2 - g - 1, true);
    }

    final long e = Math.min(exp2, other.exp2);
    final BigInteger sum = signedMant().shiftLeft(toInt(exp2 - e))
                      .add(other.signedMant().shiftLeft(toInt(other.exp2 - e)));
    return new ExactValue(sum.signum() < 0, sum.abs(), e, false);
  }

  public ExactValue subtract(ExactValue other) {
    return add(other.negate());
  }

  public ExactValue multiply(ExactValue other) {
    requireExact(this, other);
    return new ExactValue(negative != other.negative, mant.multiply(other.mant), exp2 + other.exp2, false);
  }

  /** @throws ArithmeticException if the divisor is zero */
  public ExactValue divide(ExactValue divisor) {
    requireExact(this, divisor);
    if (divisor.isZero()) throw new ArithmeticException("Division by zero");
    final boolean resultNegative = negative != divisor.negative;
    if (isZero()) return new ExactValue(resultNegative, ZERO, 0, false);

    final int n = Math.max(0, MIN_PRECISION + divisor.mant.bitLength() - mant.bitLength());
    final BigInteger[] qr = mant.shiftLeft(n).divideAndRemainder(divisor.mant);  // quotient >= 2^(MIN_PRECISION - 1)
    final long e = exp2 - n - divisor.exp2;
    if (qr[1].signum() == 0)
      return new ExactValue(resultNegative, qr[0], e, false);
    return new ExactValue(resultNegative, qr[0].shiftLeft(1).add(ONE), e - 1, true);  // the sticky bit
  }

  /** @throws ArithmeticException if the value is negative (-0 is allowed) */
  public ExactValue sqrt() {
    requireExact(this);
    if (isZero()) return this;
    if (negative) throw new ArithmeticException("Square root of a negative value");

    int shift = Math.max(0, 2 * MIN_PRECISION - mant.bitLength());
    if (((exp2 - shift) & 1) != 0) shift++;                       // the exponent must be even
    final BigInteger[] rr = mant.shiftLeft(shift).sqrtAndRemainder();
    final long e = (exp2 - shift) / 2;
    if (rr[1].signum() == 0)
      return new ExactValue(false, rr[0], e, false);
    return new ExactValue(false, rr[0].shiftLeft(1).add(ONE), e - 1, true);  // the sticky bit
  }

  /* ************************************************************************
   * Rounding
   */

  /**
   * The result of rounding an exact value to a binary format.
   *
   * @param rounded       the correctly rounded value (to the nearest; ties as the format prescribes)
   * @param truncated     the nearest value of the format not greater than the exact value by magnitude
   * @param awayFromZero  the nearest value of the format greater than the exact value by magnitude
   *                      (for exactly representable values, equal to {@code truncated})
   * @param offsetFromMidpoint the position of the exact value relative to the midpoint
   *                      between {@code truncated} and {@code awayFromZero}, in ulps, by magnitude:
   *                      from -0.5 (equals {@code truncated}) to 0.5 (exclusive; equals {@code awayFromZero}),
   *                      0 for exact ties
   * @param exact         true if the exact value is representable in the format
   * @param format        the format
   */
  public record Rounding<T>(T rounded, T truncated, T awayFromZero,
                            double offsetFromMidpoint, boolean exact, BinaryFormat<T> format) {

    /**
     * @return true if {@code value} is the correctly rounded value, or if the exact value is closer
     * to the midpoint than {@code epsilon} ulp, and {@code value} is either of the two adjacent values.
     */
    public boolean accepts(T value, double epsilon) {
      if (format.same(value, rounded)) return true;
      return !exact
          && Math.abs(offsetFromMidpoint) < epsilon
          && (format.same(value, truncated) || format.same(value, awayFromZero));
    }
  }

  /** @return the result of rounding this value to Quadruple: to the nearest, ties away from zero */
  public Rounding<Quadruple> round() {
    return round(BinaryFormat.QUADRUPLE);
  }

  /**
   * Rounds this value to the given format: to the nearest value, ties as the format prescribes,
   * the same way for normal and subnormal results. Values beyond {@code MAX_VALUE + 0.5 ulp}
   * (and the tie itself, unless it is rounded down to even) become infinity,
   * values less than a half of the minimum subnormal value become zero (keeping the sign).
   */
  public <T> Rounding<T> round(BinaryFormat<T> format) {
    if (isZero()) {
      final T zero = format.encode(negative, ZERO, 0);
      return new Rounding<>(zero, zero, zero, -0.5, true, format);
    }
    final int bitLength = mant.bitLength();
    final long exponent = bitLength - 1 + exp2;                      // value is in [2^exponent, 2^(exponent + 1))
    final long lsbExponent = Math.max(exponent - (format.precision() - 1), format.minLsbExponent());
    final long shift = lsbExponent - exp2;                          // the number of bits to drop

    if (exponent > format.maxExponent()) {                           // >= 2 * MAX_VALUE: far beyond the range
      final T infinity = format.infinity(negative);
      return new Rounding<>(infinity, infinity, infinity, -0.5, false, format);
    }

    if (shift <= 0) {                                                // exactly representable
      final T value = format.encode(negative, mant.shiftLeft(toInt(-shift)), lsbExponent);
      return new Rounding<>(value, value, value, -0.5, true, format);
    }

    if (shift > bitLength + 64) {                                    // less than MIN_VALUE * 2^-63: rounds to zero
      final T zero = format.encode(negative, ZERO, lsbExponent);
      return new Rounding<>(zero, zero, format.encode(negative, ONE, lsbExponent), -0.5, false, format);
    }

    final int s = toInt(shift);
    final BigInteger truncated = mant.shiftRight(s);
    final BigInteger remainder = mant.subtract(truncated.shiftLeft(s));
    final BigInteger half = ONE.shiftLeft(s - 1);
    final int cmp = remainder.compareTo(half);
    final boolean exact = remainder.signum() == 0 && !inexact;
    final T lower = format.encode(negative, truncated, lsbExponent);
    final T upper = exact? lower : format.encode(negative, truncated.add(ONE), lsbExponent);
    final boolean roundUp = cmp > 0
        || cmp == 0 && (format.ties() == BinaryFormat.Ties.AWAY_FROM_ZERO || truncated.testBit(0));
    return new Rounding<>(roundUp? upper : lower, lower, upper,
                          scaled(remainder.subtract(half), s), exact, format);
  }

  /**
   * @return the integer part of this value (truncated toward zero), saturated to the range of {@code long},
   * as the standard narrowing conversion {@code (long)double} does
   */
  public long toLongTruncated() {
    return integerPart().max(BigInteger.valueOf(Long.MIN_VALUE)).min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
  }

  /**
   * @return the integer part of this value (truncated toward zero), saturated to the range of {@code int},
   * as the standard narrowing conversion {@code (int)double} does
   */
  public int toIntTruncated() {
    return integerPart().max(BigInteger.valueOf(Integer.MIN_VALUE)).min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
  }

  /** @return the integer part, or a value beyond the range of long, if the magnitude is not less than 2^64 */
  private BigInteger integerPart() {
    if (isZero() || top() <= 0) return ZERO;
    final BigInteger abs = top() > 64?
        ONE.shiftLeft(64) :
        exp2 >= 0? mant.shiftLeft(toInt(exp2)) : mant.shiftRight(toInt(-exp2));
    return negative? abs.negate() : abs;
  }

  /** @return {@code value * 2^-s} as a double */
  private static double scaled(BigInteger value, int s) {
    final int drop = Math.max(0, value.bitLength() - 62);
    return Math.scalb(value.shiftRight(drop).doubleValue(), drop - s);
  }

  /* ************************************************************************
   * Conversion and helpers
   */

  /**
   * @return the exact value as BigDecimal (for inexact values, the value of their representation).
   * Practical only for moderate exponents.
   */
  public BigDecimal toBigDecimal() {
    final BigDecimal abs = exp2 >= 0?
        new BigDecimal(mant.shiftLeft(toInt(exp2))) :
        new BigDecimal(mant.multiply(FIVE.pow(toInt(-exp2))), toInt(-exp2));  // m * 2^-n == m * 5^n * 10^-n
    return negative? abs.negate() : abs;
  }

  @Override
  public String toString() {
    return (negative? "-" : "+") + "0x" + mant.toString(16).toUpperCase() + " * 2^" + exp2
        + (inexact? " (inexact)" : "");
  }

  /** @return the position of the bit just above the most significant one: the value is less than 2^top */
  private long top() {
    return mant.bitLength() + exp2;
  }

  private BigInteger signedMant() {
    return negative? mant.negate() : mant;
  }

  private static void requireExact(ExactValue... values) {
    for (final ExactValue v : values)
      if (v.inexact)
        throw new IllegalStateException("Operations on inexact values are not supported: " + v);
  }

  private static int toInt(long value) {
    return Math.toIntExact(value);
  }

  private static BigInteger unsigned(long value) {
    return new BigInteger(Long.toUnsignedString(value));
  }

}
