package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.testdata.DataHelpers.scaled;
import static com.mvohm.quadruple.Quadruple.*;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TEN;
import static java.math.BigInteger.ZERO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Data for {@code StringParsingTest}: strings to parse with the exact values they represent, or the values
 * they must give, and invalid strings.
 * <p>
 * The exact value of a numeric string is not found by parsing it (that would be a second parser to test):
 * the data are generated from the components of the value (sign, decimal digits, decimal exponent), which give
 * the exact value ({@link ExactValue#ofDecimal}), and the string is composed from them in a random form:
 * with or without the sign {@code +}, with the decimal point at a random position (or without it), with leading
 * and trailing zeros, with an exponent in a random form ({@code e/E}, sign, leading zeros), or without it.
 * The hand-written strings are given with their values explicitly.
 * <p>
 * Numeric data:
 * <ul>
 *   <li>hand-written strings of various forms (syntax), extreme exponents;</li>
 *   <li>random values with 1 to 80 digits (and some of 100 to 1000), with exponents: moderate, over the whole range,
 *       near the upper bound of the range, in the subnormal range;</li>
 *   <li>near-midpoint values: the midpoint between two adjacent Quadruple values (over the whole range,
 *       including the thresholds of overflow and underflow and the bound of the subnormal range)
 *       truncated to 40 to 75 digits, and the same plus a unit of the last digit,
 *       so that the distance from the midpoint varies from ~0.3 to ~10^-36 ulp;</li>
 *   <li>exact midpoints with moderate exponents, written with all their digits (often more than 59,
 *       the number of digits taken into account by the parser), and the same minus a unit of the last digit;</li>
 *   <li>{@code Double.toString()} of random doubles.</li>
 * </ul>
 * All of them with both signs.
 */
public class StringParsingData {

  private static final int RANDOM_COUNT       = scaled(3000);
  private static final int LONG_COUNT         = scaled(50);
  private static final int NEAR_MIDPOINT_COUNT = scaled(2000);
  private static final int TIE_COUNT          = scaled(300);
  private static final int DOUBLE_COUNT       = scaled(500);

  /** The decimal exponents of MAX_VALUE (1.76e646456993) and MIN_VALUE (6.67e-646457032) */
  private static final long MAX_EXP10 = 646_456_993L, MIN_EXP10 = -646_457_032L;
  /** The decimal exponent of MIN_NORMAL (2.27e-646456993) */
  private static final long MIN_NORMAL_EXP10 = -646_456_993L;
  /** The binary exponent of the LSB of subnormal values */
  private static final long MIN_LSB_EXPONENT = 1L - EXPONENT_BIAS - 128;

  /* ************************************************************************
   * Numeric strings: {String, ExactValue}
   */

  /** Numeric strings with their exact values (see the class description) */
  public static Stream<Arguments> numbers() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    final List<Arguments> list = new ArrayList<>(handWritten());
    for (int i = 0; i < RANDOM_COUNT; i++) list.add(randomNumber(random, 1 + random.nextInt(80)));
    for (int i = 0; i < LONG_COUNT; i++)   list.add(randomNumber(random, 100 + random.nextInt(901)));
    nearMidpoints(random).forEach(list::add);
    for (int i = 0; i < TIE_COUNT; i++)    exactTies(random).forEach(list::add);
    new RandomGenerator().doubles(DOUBLE_COUNT).forEach(d -> list.add(fromBigDecimal(Double.toString(d))));
    return list.stream();
  }

  private static List<Arguments> handWritten() {
    final List<Arguments> list = new ArrayList<>();
    // Zeros
    for (final String s : new String[] {"0", "+0", "00", "0.0", "0.", ".0", "0e0", "0.000e+10", "000.000E-10",
                                        "0e999999999999999999999", "0e-999999999999999999999"})
      list.add(number(s, false, ZERO, 0));
    for (final String s : new String[] {"-0", "-0.0", "-0.", "-.0", "-0e5", "-000.000E-10", "-0e999999999999999999999"})
      list.add(number(s, true, ZERO, 0));
    // Forms of 1.5
    for (final String s : new String[] {"1.5", "+1.5", "1.50", "001.500", "15e-1", "0.15e1", "150e-2", "1.5e0",
                                        "1.5E0", "1.5e+0", "1.5e-0", "1.5e00000", ".15e+001", "15000000000e-10",
                                        " 1.5", "1.5 ", "\t1.5\n", "\r\n 1.5 \t", "0.000_015e5", "1_5e-1"})
      list.add(number(s, false, BigInteger.valueOf(15), -1));
    list.add(number("-1.5e+0001", true, BigInteger.valueOf(15), 0));
    // Point at the edges
    list.add(number("1.", false, ONE, 0));
    list.add(number("-1.", true, ONE, 0));
    list.add(number(".5", false, BigInteger.valueOf(5), -1));
    list.add(number("-.5", true, BigInteger.valueOf(5), -1));
    list.add(number("+.5", false, BigInteger.valueOf(5), -1));
    list.add(number("5.e-1", false, BigInteger.valueOf(5), -1));
    // The separator '_'
    list.add(number("1_000", false, BigInteger.valueOf(1000), 0));
    list.add(number("-1_000_000.000_001", true, new BigInteger("1000000000001"), -6));
    list.add(number("0.000_001", false, ONE, -6));
    // Values not representable exactly
    list.add(number("0.1", false, ONE, -1));
    list.add(number("-0.3", true, BigInteger.valueOf(3), -1));
    list.add(number("3.14159265358979323846264338327950288419716939937510582097494459", false,
                    new BigInteger("314159265358979323846264338327950288419716939937510582097494459"), -62));
    // Extreme exponents
    list.add(number("1e646456993",   false, ONE, MAX_EXP10));                        // < MAX_VALUE
    list.add(number("1.8e646456993", false, BigInteger.valueOf(18), MAX_EXP10 - 1)); // > MAX_VALUE: Infinity
    list.add(number("-3.5e646456993", true, BigInteger.valueOf(35), MAX_EXP10 - 1)); // < 2^(2^31 + 1): Infinity
    list.add(number("3.6e646456993",  false, BigInteger.valueOf(36), MAX_EXP10 - 1)); // >= 2^(2^31 + 1) (BUG-20)
    list.add(number("-9.99e646456993", true, BigInteger.valueOf(999), MAX_EXP10 - 2));
    list.add(number("-1e646456994",  true,  ONE, MAX_EXP10 + 1));
    list.add(number("1e999999999",   false, ONE, 999_999_999));
    list.add(number("1e99999999999999999999",   false, ONE, Long.MAX_VALUE / 2));    // > 18 digits, no correction
    list.add(number("-1e-99999999999999999999", true,  ONE, Long.MIN_VALUE / 2));
    list.add(number("0.1e-99999999999999999999", false, ONE, Long.MIN_VALUE / 2));   // the correction -1
    // > 18 digits with the correction for the position of the point (BUG-16)
    list.add(number("10e9223372036854775807",        false, TEN, Long.MAX_VALUE / 2));   // Infinity
    list.add(number("123e999999999999999999999",     false, BigInteger.valueOf(123), Long.MAX_VALUE / 2));
    list.add(number("-10e+99999999999999999999",     true,  TEN, Long.MAX_VALUE / 2));
    list.add(number("1234.5e99999999999999999999",   false, BigInteger.valueOf(12345), Long.MAX_VALUE / 2));
    list.add(number("0.01e-99999999999999999999",    false, ONE, Long.MIN_VALUE / 2));   // 0
    list.add(number("-0.001e-99999999999999999999",  true,  ONE, Long.MIN_VALUE / 2));
    list.add(number("0.0000123e-99999999999999999999", false, BigInteger.valueOf(123), Long.MIN_VALUE / 2));
    list.add(number("1e-646457032",  false, ONE, MIN_EXP10));                        // < MIN_VALUE / 2: 0
    list.add(number("4e-646457032",  false, BigInteger.valueOf(4), MIN_EXP10));      // MIN_VALUE
    list.add(number("3.4e-646457032", false, BigInteger.valueOf(34), MIN_EXP10 - 1)); // > MIN_VALUE / 2: MIN_VALUE
    list.add(number("3.3e-646457032", false, BigInteger.valueOf(33), MIN_EXP10 - 1)); // < MIN_VALUE / 2: 0
    list.add(number("-9e-646457033", true,  BigInteger.valueOf(9), MIN_EXP10 - 1));
    list.add(number("1e-999999999",  false, ONE, -999_999_999));
    list.add(number("2.27e-646456993", false, BigInteger.valueOf(227), MIN_NORMAL_EXP10 - 2)); // ~MIN_NORMAL
    list.add(number("1e-646456993", false, ONE, MIN_NORMAL_EXP10));                  // subnormal
    // From the old tests: rounding of the decimal mantissa with a carry, leading zeros, extreme exponents
    final String nines51 = "9." + "9".repeat(50), nines70 = "9." + "9".repeat(69);
    list.add(number(nines51, false, new BigInteger(nines51.replace(".", "")), -50));   // rounds to 10
    list.add(number(nines70, false, new BigInteger(nines70.replace(".", "")), -69));   // > 59 digits, carry
    list.add(number("9.99999999990000000000000000000000000000000000000001", false,
                    new BigInteger("999999999990000000000000000000000000000000000000001"), -50));
    list.add(number("000", false, ZERO, 0));
    list.add(number("1.23456e2147483648",  false, BigInteger.valueOf(123456), 2147483648L - 5));   // Infinity
    list.add(number("-1.23456e2147483648", true,  BigInteger.valueOf(123456), 2147483648L - 5));
    list.add(number("5.23456e-646457032",  false, BigInteger.valueOf(523456), MIN_EXP10 - 5));     // MIN_VALUE
    return list;
  }

  /** A random value {@code ±digits * 10^exp10} with the given number of digits, written in a random form */
  private static Arguments randomNumber(Random random, int length) {
    final BigInteger digits = randomDigits(random, length);
    final long magnitude;                              // the exponent of the leading digit + 1
    switch (random.nextInt(10)) {
      case 0, 1, 2, 3 -> magnitude = random.nextInt(801) - 400;
      case 4, 5, 6    -> magnitude = MIN_EXP10 - 2 + (long)(random.nextDouble() * (MAX_EXP10 - MIN_EXP10 + 5));
      case 7          -> magnitude = MAX_EXP10 - 2 + random.nextInt(5);
      default         -> magnitude = MIN_EXP10 - 1 + random.nextInt((int)(MIN_NORMAL_EXP10 - MIN_EXP10 + 3));
    }
    return randomForm(random, random.nextBoolean(), digits, magnitude - length);
  }

  /**
   * Values near the midpoints between adjacent Quadruple values: the midpoint above a random value,
   * truncated to 40..75 digits (below the midpoint), and the same plus a unit of the last digit (above it)
   */
  private static Stream<Arguments> nearMidpoints(Random random) {
    final List<Quadruple> values = new ArrayList<>(List.of(
        new Quadruple(false, 0, 0, 0),                                      // the midpoint is MIN_VALUE / 2
        new Quadruple(false, 0, 0, 1),                                      // MIN_VALUE
        new Quadruple(false, 0, -1L, -1L),                                  // max subnormal: below MIN_NORMAL
        new Quadruple(false, 0, 0x8000_0000_0000_0000L, 0),
        new Quadruple(false, 1, 0, 0),                                      // MIN_NORMAL
        new Quadruple(false, (int)EXPONENT_OF_MAX_VALUE, -1L, -1L),         // MAX_VALUE: the threshold of overflow
        new Quadruple(false, (int)EXPONENT_OF_MAX_VALUE, -1L, -2L),
        new Quadruple(false, EXPONENT_OF_ONE, 0, 0),                        // 1
        new Quadruple(false, EXPONENT_OF_ONE - 1, -1L, -1L)));              // 1 - ulp/2: carry to 1
    new RandomGenerator().quadruples(NEAR_MIDPOINT_COUNT / 2).forEach(values::add);
    new RandomGenerator(RandomGenerator.DEFAULT_RANDOM_SEED + 1)
      .quadruples(NEAR_MIDPOINT_COUNT / 2, -1000, 1000).forEach(values::add);
    return values.stream().flatMap(q -> {
      final BigDecimal d = midpointAbove(q).toBigDecimal(40 + random.nextInt(36));
      final BigInteger digits = d.unscaledValue();
      final long exp10 = -d.scale();
      final boolean negative = random.nextBoolean();
      return Stream.of(randomForm(random, negative, digits, exp10), randomForm(random, negative, digits.add(ONE), exp10));
    });
  }

  /**
   * An exact midpoint (with a moderate exponent) written with all its digits,
   * and the same minus a unit of the last digit
   */
  private static Stream<Arguments> exactTies(Random random) {
    final int length = 1 + random.nextInt(129);                  // the significand of q: 1 to 129 bits
    final int exponent = random.nextInt(301) - 150;
    final BigInteger mant = new BigInteger(length, random).setBit(length - 1).shiftLeft(129 - length);
    final Quadruple q = new Quadruple(false, EXPONENT_OF_ONE + exponent, mant.shiftRight(64).longValue(), mant.longValue());
    final BigDecimal d = midpointAbove(q).toBigDecimal().stripTrailingZeros();
    final boolean negative = random.nextBoolean();
    return Stream.of(randomForm(random, negative, d.unscaledValue(), -d.scale()),
                     randomForm(random, negative, d.unscaledValue().subtract(ONE), -d.scale()));
  }

  /** @return the exact midpoint between {@code |q|} and the next Quadruple value by magnitude */
  private static ExactValue midpointAbove(Quadruple q) {
    final long lsb = q.exponent() == 0? MIN_LSB_EXPONENT : (q.exponent() & 0xFFFF_FFFFL) - EXPONENT_BIAS - 128;
    return ExactValue.of(new Quadruple(false, q.exponent(), q.mantHi(), q.mantLo()))
                     .add(ExactValue.of(false, ONE, lsb - 1));
  }

  /* ************************************************************************
   * Composing strings
   */

  /**
   * Writes {@code ±digits * 10^exp10} in a random form: with or without '+', with the point at a random position
   * or without it, with random leading and trailing zeros, with the exponent in a random form, or without
   * the exponent (if it's not too long)
   */
  private static Arguments randomForm(Random random, boolean negative, BigInteger digits, long exp10) {
    final String ds = digits.toString();
    final StringBuilder sb = new StringBuilder();
    sb.append(negative? "-" : random.nextInt(4) == 0? "+" : "");
    final int length = ds.length();
    final long plainPoint = length + exp10;        // the position of the point in ds without an exponent
    if (random.nextInt(4) == 0 && plainPoint >= -30 && plainPoint <= length + 30) {     // without the exponent
      if (plainPoint <= 0)
        sb.append(random.nextBoolean()? "0." : ".").append("0".repeat((int)-plainPoint)).append(ds);
      else if (plainPoint >= length)
        sb.append(ds).append("0".repeat((int)(plainPoint - length))).append(random.nextBoolean()? "." : "");
      else
        sb.append(ds, 0, (int)plainPoint).append('.').append(ds, (int)plainPoint, length);
      return number(sb.toString(), negative, digits, exp10);
    }
    sb.append("0".repeat(random.nextInt(4) == 0? random.nextInt(4) : 0));                 // leading zeros
    final int point = random.nextInt(3) == 0? -1 : random.nextInt(length + 1);           // -1: no point
    if (point < 0) {
      final int zeros = random.nextInt(4) == 0? random.nextInt(4) : 0;                     // trailing zeros
      sb.append(ds).append("0".repeat(zeros));
      appendExponent(random, sb, exp10 - zeros);
    } else {
      sb.append(ds, 0, point).append('.').append(ds, point, length);
      sb.append("0".repeat(random.nextInt(4) == 0? random.nextInt(4) : 0));               // trailing zeros
      appendExponent(random, sb, exp10 + length - point);
    }
    return number(sb.toString(), negative, digits, exp10);
  }

  private static void appendExponent(Random random, StringBuilder sb, long exponent) {
    if (exponent == 0 && random.nextBoolean()) return;
    sb.append(random.nextBoolean()? 'e' : 'E');
    sb.append(exponent < 0? "-" : random.nextBoolean()? "+" : "");
    sb.append("0".repeat(random.nextInt(4) == 0? random.nextInt(4) : 0));
    sb.append(Math.abs(exponent));
  }

  /* ************************************************************************
   * Hand-written cases with expected Quadruple values
   */

  /** Designations of special values, in various cases, with the values they must give: {String, Quadruple} */
  public static Stream<Arguments> namedValues() {
    final List<Arguments> list = new ArrayList<>();
    final Object[][] names = {
        {"Quadruple.MIN_VALUE", minValue()},  {"MIN_VALUE", minValue()},
        {"Quadruple.MAX_VALUE", maxValue()},  {"MAX_VALUE", maxValue()},
        {"Quadruple.MIN_NORMAL", minNormal()}, {"MIN_NORMAL", minNormal()},
        {"Quadruple.NaN", nan()},            {"NaN", nan()},
        {"Quadruple.NEGATIVE_INFINITY", negativeInfinity()}, {"NEGATIVE_INFINITY", negativeInfinity()},
        {"-INFINITY", negativeInfinity()},
        {"Quadruple.POSITIVE_INFINITY", positiveInfinity()}, {"POSITIVE_INFINITY", positiveInfinity()},
        {"INFINITY", positiveInfinity()},    {"+INFINITY", positiveInfinity()},
    };
    for (final Object[] n : names) {
      final String name = (String)n[0];
      for (final String s : new String[] {name, name.toLowerCase(), mixedCase(name), " " + name + "\t"})
        list.add(Arguments.of(s, n[1]));
    }
    list.add(Arguments.of("Infinity", positiveInfinity()));       // as Double.toString() gives them
    list.add(Arguments.of("-Infinity", negativeInfinity()));
    return list.stream();
  }

  /** Invalid strings: must cause NumberFormatException */
  public static Stream<String> invalidStrings() {
    return Stream.of("", " ", "\t", ".", "-", "+", "-.", "+.", "e", "e5", ".e5", "-e5", "1e", "1e+", "1e-", "1e+-1",
                     "+-1", "-+1", "--1", "++1", "1-", "1+", "1.2.3", "1..2", "1e2.5", "1e2e3", "1ee2", "1 2", "1. 5",
                     "- 1", "1 e5", "1,5", "1,000", "abc", "1a", "a1", "0x", "#1", "1%", "∞", "١", "1/2",
                     "NaN1", "Na N", "nan.", "Infinityx", "Inf", "+-Infinity", "--Infinity", "++Infinity",
                     "MAX", "Quadruple.", "Double.MAX_VALUE", "Quadruple.MAX_VALUE.", "Quadruple. MAX_VALUE",
                     // from the old tests
                     "Blazing nonsense", "123e.123", "-123.321ee345", "1.2345e+5.5", "1.2345e-5.5", ".e35");
  }

  /* ************************************************************************
   * Cases of known bugs
   */

  /** BUG-18: a sign before any designation of a special value: {String, Quadruple} */
  public static Stream<Arguments> signedNames() {
    return Stream.of(
        Arguments.of("-NaN", nan()),                         Arguments.of("+NaN", nan()),
        Arguments.of("-Quadruple.NaN", nan()),               Arguments.of("+quadruple.nan", nan()),
        Arguments.of("-MAX_VALUE", negate(maxValue())),      Arguments.of("+MAX_VALUE", maxValue()),
        Arguments.of("-Quadruple.MAX_VALUE", negate(maxValue())),
        Arguments.of("-MIN_VALUE", negate(minValue())),      Arguments.of("+Quadruple.MIN_VALUE", minValue()),
        Arguments.of("-min_normal", negate(minNormal())),    Arguments.of("+MIN_NORMAL", minNormal()),
        Arguments.of("-POSITIVE_INFINITY", negativeInfinity()),
        Arguments.of("+NEGATIVE_INFINITY", negativeInfinity()),
        Arguments.of("-NEGATIVE_INFINITY", positiveInfinity()),
        Arguments.of("+Quadruple.POSITIVE_INFINITY", positiveInfinity()));
  }

  /**
   * BUG-19: the forms accepted by {@code Double.parseDouble()} but not by Quadruple:
   * the suffixes of type (d, D, f, F) and hexadecimal floating-point literals
   */
  public static Stream<Arguments> doubleSyntax() {
    return Stream.of(
        number("1d",       false, ONE, 0),
        number("1D",       false, ONE, 0),
        number("1f",       false, ONE, 0),
        number("-1.5F",    true,  BigInteger.valueOf(15), -1),
        number("2.5e3d",   false, BigInteger.valueOf(25), 2),
        number(" 0.1d ",   false, ONE, -1),
        hex("0x1p3",       false, ONE, 3),
        hex("0X1P3",       false, ONE, 3),
        hex("0x1.8p1",     false, BigInteger.valueOf(0x18), 1 - 4),
        hex("-0x10p0",     true,  BigInteger.valueOf(0x10), 0),
        hex("+0x.8p1",     false, BigInteger.valueOf(8), 1 - 4),
        hex("0x1p-1074",   false, ONE, -1074),
        hex("0x1p-2147483646", false, ONE, -2147483646L),                        // MIN_NORMAL
        hex("0x1.8p1d",    false, BigInteger.valueOf(0x18), 1 - 4),
        hex("0x1.000000000000000000000000000000008p0", false,                    // 1 + ulp/2: a tie, rounds up
            new BigInteger("1000000000000000000000000000000008", 16), -33 * 4),
        hex("0x1.FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFp2147483647", false,             // MAX_VALUE
            new BigInteger("1FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16), 2147483647L - 32 * 4));
  }

  /* ************************************************************************
   * Helpers
   */

  /** A numeric string with its exact value {@code ±digits * 10^exp10} */
  private static Arguments number(String s, boolean negative, BigInteger digits, long exp10) {
    return Arguments.of(named(s), ExactValue.ofDecimal(negative, digits, exp10));
  }

  /** A hexadecimal string with its exact value {@code ±mant * 2^exp2} */
  private static Arguments hex(String s, boolean negative, BigInteger mant, long exp2) {
    return Arguments.of(named(s), ExactValue.of(negative, mant, exp2));
  }

  /** A string in the syntax of BigDecimal (e.g. from {@code Double.toString()}) with the value given by BigDecimal */
  private static Arguments fromBigDecimal(String s) {
    final BigDecimal d = new BigDecimal(s);
    return number(s, s.startsWith("-"), d.unscaledValue().abs(), -d.scale());
  }

  /** Long strings are shortened in the display names */
  private static Named<String> named(String s) {
    final String visible = s.replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
    return Named.of(visible.length() <= 100? "\"" + visible + "\""
                                           : "\"" + visible.substring(0, 60) + "...\" (" + s.length() + " chars)", s);
  }

  /** @return a random integer of exactly {@code length} decimal digits */
  private static BigInteger randomDigits(Random random, int length) {
    final StringBuilder sb = new StringBuilder().append((char)('1' + random.nextInt(9)));
    for (int i = 1; i < length; i++)
      sb.append((char)('0' + random.nextInt(10)));
    return new BigInteger(sb.toString());
  }

  private static String mixedCase(String s) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++)
      sb.append(i % 2 == 0? Character.toLowerCase(s.charAt(i)) : Character.toUpperCase(s.charAt(i)));
    return sb.toString();
  }

  private static Quadruple negate(Quadruple q) {
    return new Quadruple(q).negate();
  }

}
