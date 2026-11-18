package com.mvohm.quadruple.testutil;

import static com.mvohm.quadruple.Quadruple.*;
import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static java.math.BigInteger.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;

/**
 * Validation of the reference itself: {@link ExactValue} and its rounding.
 * <ul>
 *   <li>hand-computed cases: ties, near-ties, huge exponent gaps, subnormal results, overflow;</li>
 *   <li>cross-check with an independent BigDecimal computation for random operands with moderate exponents;</li>
 *   <li>round trip Quadruple -> ExactValue -> Quadruple.</li>
 * </ul>
 */
public class ExactValueTest {

  private static final long BIAS = EXPONENT_BIAS;
  private static final MathContext MC = new MathContext(400);

  private static final Quadruple ONE_Q            = q(false, EXPONENT_OF_ONE, 0, 0);
  private static final Quadruple MIN_VALUE_Q      = q(false, 0, 0, 1);
  private static final Quadruple MAX_SUBNORMAL_Q  = q(false, 0, -1L, -1L);
  private static final Quadruple MIN_NORMAL_Q     = q(false, 1, 0, 0);
  private static final Quadruple MAX_VALUE_Q      = q(false, (int)EXPONENT_OF_MAX_VALUE, -1L, -1L);
  private static final Quadruple POS_INFINITY_Q   = q(false, EXPONENT_OF_INFINITY, 0, 0);
  private static final Quadruple NEG_INFINITY_Q   = q(true,  EXPONENT_OF_INFINITY, 0, 0);

  private static final long FIVES = 0x5555_5555_5555_5555L;
  private static final long AS    = 0xAAAA_AAAA_AAAA_AAAAL;

  /* ************************************************************************
   * Hand-computed cases
   */

  @Test
  void exactTieShouldRoundAwayFromZero() {
    final ExactValue x = pow2(0).add(pow2(-129));                 // 1 + 0.5 ulp
    checkRounding(x, q(false, EXPONENT_OF_ONE, 0, 1), ONE_Q, q(false, EXPONENT_OF_ONE, 0, 1), 0.0);
    checkRounding(x.negate(), q(true, EXPONENT_OF_ONE, 0, 1), q(true, EXPONENT_OF_ONE, 0, 0),
                  q(true, EXPONENT_OF_ONE, 0, 1), 0.0);
  }

  @Test
  void valueJustBelowTieShouldRoundDown() {
    final ExactValue x = pow2(0).add(pow2(-129)).subtract(pow2(-300));
    checkRounding(x, ONE_Q, ONE_Q, q(false, EXPONENT_OF_ONE, 0, 1), -Math.scalb(1.0, -171));
  }

  @Test
  void thirdsShouldBeRoundedCorrectly() {
    // 1/3 = 1.0101...b * 2^-2, the tail after 128 bits is 0.0101...b = 1/3 ulp
    checkRounding(ExactValue.of(1).divide(ExactValue.of(3)),
                  q(false, EXPONENT_OF_ONE - 2, FIVES, FIVES), q(false, EXPONENT_OF_ONE - 2, FIVES, FIVES),
                  q(false, EXPONENT_OF_ONE - 2, FIVES, FIVES + 1), 1.0 / 3 - 0.5);
    // -5/3 = -1.1010...b, the tail is 0.1010...b = 2/3 ulp
    checkRounding(ExactValue.of(-5).divide(ExactValue.of(3)),
                  q(true, EXPONENT_OF_ONE, AS, AS + 1), q(true, EXPONENT_OF_ONE, AS, AS),
                  q(true, EXPONENT_OF_ONE, AS, AS + 1), 2.0 / 3 - 0.5);
  }

  @Test
  void exactQuotientAndRootShouldBeExact() {
    final ExactValue quotient = ExactValue.of(3).divide(ExactValue.of(-4));
    assertThat(quotient.isExact()).isTrue();
    checkRounding(quotient, q(true, EXPONENT_OF_ONE - 1, 0x8000_0000_0000_0000L, 0), null, null, null);
    final ExactValue root = ExactValue.of(9.0 / 16).sqrt();
    assertThat(root.isExact()).isTrue();
    checkRounding(root, q(false, EXPONENT_OF_ONE - 1, 0x8000_0000_0000_0000L, 0), null, null, null);
  }

  @Test
  void tinySummandShouldOnlyAffectRounding() {
    final ExactValue tiny = pow2(-1_000_000_000L);
    checkRounding(pow2(0).add(tiny),
                  ONE_Q, ONE_Q, q(false, EXPONENT_OF_ONE, 0, 1), -0.5);
    checkRounding(pow2(0).subtract(tiny),                         // just below 1.0: lower binade
                  ONE_Q, q(false, EXPONENT_OF_ONE - 1, -1L, -1L), ONE_Q, 0.5);
    checkRounding(pow2(0).add(pow2(-129)).add(tiny).negate(),     // just above the tie
                  q(true, EXPONENT_OF_ONE, 0, 1), q(true, EXPONENT_OF_ONE, 0, 0),
                  q(true, EXPONENT_OF_ONE, 0, 1), 0.0);
    assertThat(tiny.add(pow2(0)).isExact()).isFalse();
    assertThat(pow2(0).add(pow2(-129)).add(tiny).round().offsetFromMidpoint()).isPositive();
  }

  @Test
  void subnormalResultsShouldBeRoundedHalfUp() {
    final ExactValue min = ExactValue.of(MIN_VALUE_Q);
    final Quadruple zero = q(false, 0, 0, 0), negZero = q(true, 0, 0, 0);
    checkRounding(min.divide(ExactValue.of(2)),  MIN_VALUE_Q, zero, MIN_VALUE_Q, 0.0);
    checkRounding(min.divide(ExactValue.of(-2)), q(true, 0, 0, 1), negZero, q(true, 0, 0, 1), 0.0);
    checkRounding(min.multiply(ExactValue.of(0.75)), MIN_VALUE_Q, zero, MIN_VALUE_Q, 0.25);
    checkRounding(min.multiply(ExactValue.of(0.25)), zero, zero, MIN_VALUE_Q, -0.25);
    checkRounding(min.multiply(pow2(-10)), zero, zero, MIN_VALUE_Q, -0.5 + Math.scalb(1.0, -10));
    checkRounding(min.multiply(pow2(-100)).negate(), negZero, negZero, q(true, 0, 0, 1), -0.5);
    // 11 units * 0.5 = 5.5 units -> 6 units
    checkRounding(ExactValue.of(q(false, 0, 0, 11)).multiply(ExactValue.of(0.5)),
                  q(false, 0, 0, 6), q(false, 0, 0, 5), q(false, 0, 0, 6), 0.0);
    // max subnormal + half unit -> MIN_NORMAL
    checkRounding(ExactValue.of(MAX_SUBNORMAL_Q).add(min.divide(ExactValue.of(2))),
                  MIN_NORMAL_Q, MAX_SUBNORMAL_Q, MIN_NORMAL_Q, 0.0);
    // MIN_NORMAL * (1 + 2^-129) -> MIN_NORMAL + 1 ulp
    checkRounding(ExactValue.of(MIN_NORMAL_Q).multiply(pow2(0).add(pow2(-129))),
                  q(false, 1, 0, 1), MIN_NORMAL_Q, q(false, 1, 0, 1), 0.0);
    // Exact subnormals and MIN_NORMAL
    checkRounding(ExactValue.of(MAX_SUBNORMAL_Q).add(min), MIN_NORMAL_Q, null, null, null);
    checkRounding(ExactValue.of(MIN_NORMAL_Q).subtract(min), MAX_SUBNORMAL_Q, null, null, null);
  }

  @Test
  void overflowShouldGiveInfinity() {
    final ExactValue max = ExactValue.of(MAX_VALUE_Q);
    final ExactValue halfUlp = pow2(Integer.MAX_VALUE - 129L);
    checkRounding(max.add(halfUlp), POS_INFINITY_Q, MAX_VALUE_Q, POS_INFINITY_Q, 0.0);
    checkRounding(max.add(halfUlp).subtract(pow2(0)).negate(),
                  q(true, (int)EXPONENT_OF_MAX_VALUE, -1L, -1L), q(true, (int)EXPONENT_OF_MAX_VALUE, -1L, -1L),
                  NEG_INFINITY_Q, -0.0);
    assertThat(max.add(halfUlp).subtract(pow2(0)).round().offsetFromMidpoint()).isNegative();
    checkRounding(max.multiply(ExactValue.of(2)), POS_INFINITY_Q, POS_INFINITY_Q, POS_INFINITY_Q, -0.5);
    checkRounding(max.multiply(ExactValue.of(-2)), NEG_INFINITY_Q, NEG_INFINITY_Q, NEG_INFINITY_Q, -0.5);
    checkRounding(max, MAX_VALUE_Q, null, null, null);
  }

  @Test
  void acceptsShouldAllowBothNeighborsOnlyNearMidpoint() {
    final Quadruple one = ONE_Q, onePlusUlp = q(false, EXPONENT_OF_ONE, 0, 1);
    final double eps = Math.scalb(1.0, -32);

    final ExactValue.Rounding tie = pow2(0).add(pow2(-129)).round();
    assertThat(tie.accepts(onePlusUlp, 0)).isTrue();
    assertThat(tie.accepts(one, 0)).isFalse();
    assertThat(tie.accepts(one, eps)).isTrue();

    final ExactValue.Rounding nearTie = pow2(0).add(pow2(-129)).subtract(pow2(-300)).round();
    assertThat(nearTie.accepts(one, 0)).isTrue();
    assertThat(nearTie.accepts(onePlusUlp, 0)).isFalse();
    assertThat(nearTie.accepts(onePlusUlp, eps)).isTrue();
    assertThat(nearTie.accepts(q(false, EXPONENT_OF_ONE, 0, 2), eps)).isFalse();

    final ExactValue.Rounding third = ExactValue.of(1).divide(ExactValue.of(3)).round();
    assertThat(third.accepts(third.awayFromZero(), eps)).isFalse();
    assertThat(third.accepts(third.awayFromZero(), 0.2)).isTrue();

    final ExactValue.Rounding exact = pow2(0).round();
    assertThat(exact.accepts(one, 0)).isTrue();
    assertThat(exact.accepts(onePlusUlp, 1)).isFalse();
    assertThat(exact.accepts(q(true, EXPONENT_OF_ONE, 0, 0), 1)).isFalse();
  }

  @Test
  void unsupportedOperationsShouldThrow() {
    final ExactValue inexact = ExactValue.of(1).divide(ExactValue.of(3));
    assertThatThrownBy(() -> inexact.add(pow2(0))).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> pow2(0).multiply(inexact)).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> inexact.sqrt()).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> pow2(0).divide(ExactValue.of(0))).isInstanceOf(ArithmeticException.class);
    assertThatThrownBy(() -> ExactValue.of(-1).sqrt()).isInstanceOf(ArithmeticException.class);
    assertThatThrownBy(() -> ExactValue.of(POS_INFINITY_Q)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ExactValue.of(Double.NaN)).isInstanceOf(IllegalArgumentException.class);
  }

  /* ************************************************************************
   * Round trip
   */

  static Stream<Quadruple> roundTripValues() {
    return Stream.concat(
        Stream.of(MIN_VALUE_Q, MAX_SUBNORMAL_Q, MIN_NORMAL_Q, MAX_VALUE_Q, ONE_Q,
                  q(false, 0, 0, 0), q(true, 0, 0, 0), q(true, 0, 0x8000_0000_0000_0000L, 1),
                  q(true, (int)EXPONENT_OF_MAX_VALUE, 0, 0)),
        new RandomGenerator().quadruples(2000));
  }

  @ParameterizedTest
  @MethodSource("roundTripValues")
  void quadrupleShouldSurviveRoundTrip(Quadruple q) {
    final ExactValue.Rounding r = ExactValue.of(q).round();
    assertThat(r.exact()).isTrue();
    assertSameFields(r.rounded(), q, "round trip");
  }

  @Test
  void doublesAndLongsShouldMatchQuadrupleConstructors() {
    new RandomGenerator().doubles(2000).forEach(d ->
        assertSameFields(ExactValue.of(d).round().rounded(), new Quadruple(d), "of(" + d + ")"));
    new RandomGenerator().subnormalDoubles(500).forEach(d ->
        assertSameFields(ExactValue.of(d).round().rounded(), new Quadruple(d), "of(" + d + ")"));
    Stream.concat(Stream.of(0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE),
                  new RandomGenerator().longs(2000).boxed()).forEach(v ->
        assertSameFields(ExactValue.of(v).round().rounded(), new Quadruple(v), "of(" + v + ")"));
  }

  /* ************************************************************************
   * Cross-check with BigDecimal
   */

  static Stream<Arguments> operandPairs() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    return Stream.generate(() -> Arguments.of(moderateQuadruple(random), moderateQuadruple(random))).limit(2000);
  }

  @ParameterizedTest
  @MethodSource("operandPairs")
  void operationsShouldMatchBigDecimal(Quadruple a, Quadruple b) {
    final ExactValue ea = ExactValue.of(a), eb = ExactValue.of(b);
    final BigDecimal x = toBigDecimal(a), y = toBigDecimal(b);
    final String operands = "(" + fieldsOf(a) + "), (" + fieldsOf(b) + ")";

    checkSum(ea.add(eb),        x.add(y),      "add " + operands);
    checkSum(ea.subtract(eb),   x.subtract(y), "subtract " + operands);
    checkExact(ea.multiply(eb), x.multiply(y), "multiply " + operands);
    checkRoundingAgainst(ea.divide(eb), x.divide(y, MC), "divide " + operands);
    checkRoundingAgainst((a.isNegative()? ea.negate() : ea).sqrt(), x.abs().sqrt(MC), "sqrt(|a|) " + operands);
    checkRoundingAgainst(ea.multiply(ea).sqrt(), x.abs(), "sqrt(a^2) " + operands);
  }

  /** A random value with exponent within +/-300, with ~1/4 of mantissas shortened (for exact ties) */
  private static Quadruple moderateQuadruple(Random random) {
    long hi = random.nextLong(), lo = random.nextLong();
    if (random.nextInt(4) == 0) {
      final int zeros = random.nextInt(129);                     // clear lowest bits of the 128-bit fraction
      if (zeros >= 64) { lo = 0; hi = zeros == 128? 0 : hi & (-1L << (zeros - 64)); }
      else               lo &= -1L << zeros;
    }
    return q(random.nextBoolean(), EXPONENT_OF_ONE + random.nextInt(601) - 300, hi, lo);
  }

  /**
   * Sums are exact, unless the exponents differ so much, that the smaller summand
   * can only affect the rounding (see {@link ExactValue})
   */
  private static void checkSum(ExactValue sum, BigDecimal expected, String context) {
    if (sum.isExact())
      assertThat(sum.toBigDecimal()).as(context).isEqualByComparingTo(expected);
    checkRoundingAgainst(sum, expected, context);
  }

  private static void checkExact(ExactValue exact, BigDecimal expected, String context) {
    assertThat(exact.isExact()).as(context).isTrue();
    assertThat(exact.toBigDecimal()).as(context).isEqualByComparingTo(expected);
    checkRoundingAgainst(exact, expected, context);
  }

  /**
   * Checks the rounding of {@code exact} against its (approximate) value {@code x}
   * computed independently with BigDecimal
   */
  private static void checkRoundingAgainst(ExactValue exact, BigDecimal x, String context) {
    final ExactValue.Rounding r = exact.round();
    final BigDecimal ax = x.abs();
    final BigDecimal lower = toBigDecimal(r.truncated()).abs();
    if (r.exact()) {
      assertThat(lower).as(context).isEqualByComparingTo(ax);
      assertSameFields(r.rounded(), r.truncated(), context);
      return;
    }
    final BigDecimal upper = toBigDecimal(r.awayFromZero()).abs();
    assertThat(upper.subtract(lower)).as(context + ": the neighbors must be 1 ulp apart")
      .isEqualByComparingTo(ulp(r.truncated()));
    assertThat(ax).as(context).isBetween(lower, upper);

    final double offset = ax.subtract(lower).divide(upper.subtract(lower), MC).subtract(new BigDecimal("0.5")).doubleValue();
    assertThat(r.offsetFromMidpoint()).as(context + ": offset").isCloseTo(offset, within(1e-15));
    assertSameFields(r.rounded(), offset >= 0? r.awayFromZero() : r.truncated(), context);
  }

  /* ************************************************************************
   * Helpers
   */

  private static Quadruple q(boolean negative, int exponent, long mantHi, long mantLo) {
    return new Quadruple(negative, exponent, mantHi, mantLo);
  }

  private static ExactValue pow2(long exponent) {
    return ExactValue.of(false, ONE, exponent);
  }

  /**
   * Checks the result of rounding; null {@code truncated}, {@code awayFromZero} and {@code offset}
   * mean that the value must be exact.
   */
  private static void checkRounding(ExactValue x, Quadruple rounded, Quadruple truncated,
                                    Quadruple awayFromZero, Double offset) {
    final ExactValue.Rounding r = x.round();
    final String context = "round(" + x + ")";
    assertSameFields(r.rounded(), rounded, context + ": rounded");
    if (offset == null) {
      assertThat(r.exact()).as(context + ": exact").isTrue();
      return;
    }
    assertThat(r.exact()).as(context + ": exact").isFalse();
    assertSameFields(r.truncated(), truncated, context + ": truncated");
    assertSameFields(r.awayFromZero(), awayFromZero, context + ": awayFromZero");
    assertThat(r.offsetFromMidpoint()).as(context + ": offset").isCloseTo(offset, within(1e-15));
  }

  /** Independent conversion of a normal Quadruple with a moderate exponent to BigDecimal */
  private static BigDecimal toBigDecimal(Quadruple q) {
    final long biased = q.exponent() & 0xFFFF_FFFFL;
    final BigInteger fraction = new BigInteger(Long.toUnsignedString(q.mantHi()))
                                .shiftLeft(64).add(new BigInteger(Long.toUnsignedString(q.mantLo())));
    final BigDecimal mantissa = new BigDecimal(fraction).divide(new BigDecimal(ONE.shiftLeft(128))).add(BigDecimal.ONE);
    final int e = Math.toIntExact(biased - BIAS);
    final BigDecimal abs = e >= 0?
        mantissa.multiply(new BigDecimal(ONE.shiftLeft(e))) :
        mantissa.divide(new BigDecimal(ONE.shiftLeft(-e)));
    return q.isNegative()? abs.negate() : abs;
  }

  private static BigDecimal ulp(Quadruple q) {
    final int e = Math.toIntExact((q.exponent() & 0xFFFF_FFFFL) - BIAS - 128);
    return e >= 0? new BigDecimal(ONE.shiftLeft(e)) : BigDecimal.ONE.divide(new BigDecimal(ONE.shiftLeft(-e)));
  }

}
