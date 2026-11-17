package com.mvohm.quadruple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.mvohm.quadruple.Quadruple.*;
import static com.mvohm.quadruple.testutil.QuadAssert.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.UnaryOperator;

import com.mvohm.quadruple.testdata.AssignData;
import com.mvohm.quadruple.testdata.Ieee754Bits;
import com.mvohm.quadruple.testutil.Ieee754Reference;
import com.mvohm.quadruple.testdata.QuadParts;

/**
 * Tests for {@code assign(...)} methods.
 * <p>
 * The main property checked for the methods assigning a value of another type:
 * <b>{@code dirty.assign(x)} sets all internal fields exactly as {@code new Quadruple(x)} does,
 * regardless of the previous contents of the instance</b>, and returns the same instance.
 * <p>
 * Correctness of the values themselves is checked by {@link CreationTest}
 * and by the conversion tests; here we only check that the previous value
 * of the instance does not affect the result.
 * <p>
 * Fields are compared directly (not via {@code doubleValue()} etc.), since a stale value
 * in {@code mantLo} would be lost by rounding when converting back to {@code double}.
 * <p>
 * Each test iterates over all dirty instances internally rather than using them as a parameter,
 * to keep the number of test nodes reasonable.
 */
public class AssignTest {

  /* *******************************************************************************
   * assign(double)
   *********************************************************************************/

  /**
   * Verifies that {@code assign(double)} produces the same fields as the constructor,
   * whatever value the instance held before.
   * <p>
   * {@code ±Double.MIN_VALUE} are excluded from the data (BUG-01),
   * they are tested by {@link #shouldAssignDoubleMinValueToDirtyInstance(double)}.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#doubleValuesForAssign")
  @DisplayName("assign(double) on a dirty instance should give the same result as the constructor")
  void shouldAssignDoubleToDirtyInstance(double value) {
    checkAssignDouble(value);
  }

  /**
   * The same as {@link #shouldAssignDoubleToDirtyInstance(double)}, for {@code ±Double.MIN_VALUE}.
   */
  @Disabled("BUG-01: assign(±Double.MIN_VALUE) keeps stale mantHi, see KNOWN_ISSUES.md")
  @ParameterizedTest
  @ValueSource(doubles = { Double.MIN_VALUE, -Double.MIN_VALUE })
  @DisplayName("assign(±Double.MIN_VALUE) on a dirty instance should give the same result as the constructor")
  void shouldAssignDoubleMinValueToDirtyInstance(double value) {
    checkAssignDouble(value);
  }

  private static void checkAssignDouble(double value) {
    checkOnDirtyInstances(new Quadruple(value), q -> q.assign(value), "assign(" + value + ")");
  }

  /* *******************************************************************************
   * assign(long)
   *********************************************************************************/

  /**
   * Verifies that {@code assign(long)} produces the same fields as the constructor,
   * whatever value the instance held before.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#longValuesForAssign")
  @DisplayName("assign(long) on a dirty instance should give the same result as the constructor")
  void shouldAssignLongToDirtyInstance(long value) {
    checkOnDirtyInstances(new Quadruple(value), q -> q.assign(value), "assign(" + value + ")");
  }

  /* *******************************************************************************
   * assign(Quadruple)
   *********************************************************************************/

  /**
   * Verifies that {@code assign(Quadruple)} copies all fields of the source exactly,
   * whatever value the instance held before, and leaves the source unchanged.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#quadrupleValuesForAssign")
  @DisplayName("assign(Quadruple) on a dirty instance should copy all fields of the source")
  void shouldAssignQuadrupleToDirtyInstance(Quadruple source) {
    final Quadruple sourceCopy = new Quadruple(source.isNegative(), source.exponent(), source.mantHi(), source.mantLo());
    final String description = "assign(" + fieldsOf(source) + ")";
    checkOnDirtyInstances(sourceCopy, q -> q.assign(source), description);
    assertSameFields(source, sourceCopy, description + ": the source must not change");
  }

  /**
   * Verifies that assigning an instance to itself ({@code q.assign(q)}) doesn't change it.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#quadrupleValuesForAssign")
  @DisplayName("q.assign(q) should leave q unchanged")
  void shouldAssignQuadrupleToItself(Quadruple q) {
    final Quadruple copy = new Quadruple(q.isNegative(), q.exponent(), q.mantHi(), q.mantLo());
    final Quadruple result = q.assign(q);
    assertThat(result).as("assign(Quadruple) must return this instance").isSameAs(q);
    assertSameFields(q, copy, "q.assign(q)");
  }

  /* *******************************************************************************
   * Low-level parts: assign(boolean, int, long, long), assign(int, long, long),
   * assignWithUnbiasedExponent(...), assign(long[])
   *
   * The expected values are built with the constructor Quadruple(boolean, int, long, long),
   * that just stores its arguments in the fields (see CreationTest).
   *********************************************************************************/

  /**
   * Verifies that {@code assign(boolean, int, long, long)} stores the given parts
   * in the fields as they are, whatever value the instance held before.
   * The {@code exponent} argument is the biased exponent.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#rawPartsForAssign")
  @DisplayName("assign(boolean, int, long, long) should store the parts as they are")
  void shouldAssignPartsToDirtyInstance(QuadParts p) {
    checkOnDirtyInstances(fromParts(p.negative(), p.exponent(), p),
                          q -> q.assign(p.negative(), p.exponent(), p.mantHi(), p.mantLo()),
                          "assign(" + p + ")");
  }

  /**
   * Verifies that {@code assign(int, long, long)} stores the given parts as they are
   * and makes the value positive, whatever value the instance held before.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#rawPartsForAssign")
  @DisplayName("assign(int, long, long) should store the parts as they are, with positive sign")
  void shouldAssignPositivePartsToDirtyInstance(QuadParts p) {
    checkOnDirtyInstances(fromParts(false, p.exponent(), p),
                          q -> q.assign(p.exponent(), p.mantHi(), p.mantLo()),
                          "assign(int, long, long) with " + p);
  }

  /**
   * Verifies that {@code assignWithUnbiasedExponent(boolean, int, long, long)} stores
   * the exponent biased (i.e. {@code exponent + EXPONENT_BIAS}, with the usual int overflow),
   * and the other parts as they are.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#rawPartsForAssign")
  @DisplayName("assignWithUnbiasedExponent(boolean, int, long, long) should store biased exponent")
  void shouldAssignPartsWithUnbiasedExponentToDirtyInstance(QuadParts p) {
    checkOnDirtyInstances(fromParts(p.negative(), p.exponent() + EXPONENT_BIAS, p),
                          q -> q.assignWithUnbiasedExponent(p.negative(), p.exponent(), p.mantHi(), p.mantLo()),
                          "assignWithUnbiasedExponent(" + p + ")");
  }

  /**
   * Verifies that {@code assignWithUnbiasedExponent(int, long, long)} stores
   * the exponent biased, and makes the value positive.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#rawPartsForAssign")
  @DisplayName("assignWithUnbiasedExponent(int, long, long) should store biased exponent, with positive sign")
  void shouldAssignPositivePartsWithUnbiasedExponentToDirtyInstance(QuadParts p) {
    checkOnDirtyInstances(fromParts(false, p.exponent() + EXPONENT_BIAS, p),
                          q -> q.assignWithUnbiasedExponent(p.exponent(), p.mantHi(), p.mantLo()),
                          "assignWithUnbiasedExponent(int, long, long) with " + p);
  }

  /**
   * Verifies that {@code assign(long[])} takes the sign from bit 63 of {@code value[0]},
   * the exponent from its bits 31..0, and the mantissa from {@code value[1]} and {@code value[2]},
   * leaves the array unchanged, and that the array produced by {@code toLongWords()}
   * of the resulting instance is equal to the source array.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#rawPartsForAssign")
  @DisplayName("assign(long[]) should take the parts from the array, toLongWords() should restore it")
  void shouldAssignLongArrayToDirtyInstance(QuadParts p) {
    final long[] words = {
        (p.negative()? 0x8000_0000_0000_0000L : 0) | (p.exponent() & 0xFFFF_FFFFL),
        p.mantHi(),
        p.mantLo() };
    final long[] wordsCopy = words.clone();
    final String description = "assign(long[]) with " + p;
    checkOnDirtyInstances(fromParts(p.negative(), p.exponent(), p), q -> q.assign(words), description);
    assertThat(words).as(description + ": the array must not change").isEqualTo(wordsCopy);
    assertThat(new Quadruple().assign(words).toLongWords())
      .as(description + ": toLongWords() must restore the source array")
      .isEqualTo(words);
  }

  /**
   * Verifies that {@code assign(long[])} ignores the bits 62..32 of {@code value[0]}
   * (they are not used by the format described in the documentation)
   * and the array elements following the first three.
   */
  @Test
  @DisplayName("assign(long[]) should ignore unused bits of value[0] and extra elements")
  void shouldIgnoreUnusedPartsOfLongArray() {
    final long[] words = { 0x7FFF_FFFF_0000_0000L | EXPONENT_OF_ONE, 0x1234L, 0x5678L, 0x9ABCL };
    checkOnDirtyInstances(new Quadruple(false, EXPONENT_OF_ONE, 0x1234L, 0x5678L),
                          q -> q.assign(words),
                          "assign(long[]) with garbage in bits 62..32 and 4 elements");
  }

  /**
   * Verifies that {@code assign(long[])} throws {@code IndexOutOfBoundsException}
   * if the array has less than 3 elements, and leaves the instance unchanged.
   */
  @ParameterizedTest
  @ValueSource(ints = { 0, 1, 2 })
  @DisplayName("assign(long[]) should throw IndexOutOfBoundsException for arrays shorter than 3")
  void shouldThrowOnShortLongArray(int length) {
    checkThrowsOnDirtyInstances(q -> q.assign(new long[length]), IndexOutOfBoundsException.class,
                                "assign(new long[" + length + "])");
  }

  /* *******************************************************************************
   * IEEE-754: assignIeee754(long[]), assignIeee754(byte[])
   *
   * The expected values are computed by Ieee754Reference, independently of the library
   *********************************************************************************/

  /**
   * Verifies that {@code assignIeee754(long[])} converts the IEEE-754 quadruple
   * to the exactly equal Quadruple value (NaN with any payload to canonical NaN),
   * whatever value the instance held before, and leaves the array unchanged.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#ieee754ValuesForAssign")
  @DisplayName("assignIeee754(long[]) should assign the exact value of the IEEE-754 quadruple")
  void shouldAssignIeee754LongsToDirtyInstance(Ieee754Bits bits) {
    final long[] longs = bits.toLongs();
    final String description = "assignIeee754(long[]) with " + bits;
    checkOnDirtyInstances(Ieee754Reference.expectedQuadruple(bits), q -> q.assignIeee754(longs), description);
    assertThat(longs).as(description + ": the array must not change").isEqualTo(bits.toLongs());
  }

  /**
   * Verifies that {@code assignIeee754(byte[])} converts the IEEE-754 quadruple, given as
   * 16 big-endian bytes, to the exactly equal Quadruple value,
   * whatever value the instance held before, and leaves the array unchanged.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#ieee754ValuesForAssign")
  @DisplayName("assignIeee754(byte[]) should assign the exact value of the IEEE-754 quadruple")
  void shouldAssignIeee754BytesToDirtyInstance(Ieee754Bits bits) {
    final byte[] bytes = bits.toBytes();
    final String description = "assignIeee754(byte[]) with " + bits;
    checkOnDirtyInstances(Ieee754Reference.expectedQuadruple(bits), q -> q.assignIeee754(bytes), description);
    assertThat(bytes).as(description + ": the array must not change").isEqualTo(bits.toBytes());
  }

  /**
   * Verifies that {@code toIeee754Longs()} and {@code toIeee754Bytes()} restore the original bits
   * of an IEEE-754 quadruple assigned with {@code assignIeee754()}
   * (Quadruple is more precise than IEEE-754 quadruple, so the conversion back must be exact),
   * except for NaN, which becomes the canonical IEEE-754 NaN {@code 0x7FFF_8000_0000_0000, 0}.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#ieee754ValuesForAssign")
  @DisplayName("toIeee754Longs() and toIeee754Bytes() should restore bits assigned by assignIeee754()")
  void shouldRestoreIeee754Bits(Ieee754Bits bits) {
    final Quadruple q = new Quadruple().assignIeee754(bits.toLongs());
    final Ieee754Bits expected = q.isNaN()? new Ieee754Bits(0x7FFF_8000_0000_0000L, 0) : bits;
    assertThat(q.toIeee754Longs())
      .as("toIeee754Longs() after assignIeee754(%s)", bits)
      .isEqualTo(expected.toLongs());
    assertThat(q.toIeee754Bytes())
      .as("toIeee754Bytes() after assignIeee754(%s)", bits)
      .isEqualTo(expected.toBytes());
  }

  /**
   * Verifies that {@code assignIeee754(long[])} throws {@code IllegalArgumentException}
   * if the array length is not 2, and leaves the instance unchanged.
   */
  @ParameterizedTest
  @ValueSource(ints = { 0, 1, 3 })
  @DisplayName("assignIeee754(long[]) should throw IllegalArgumentException if length != 2")
  void shouldThrowOnWrongIeee754LongsLength(int length) {
    checkThrowsOnDirtyInstances(q -> q.assignIeee754(new long[length]), IllegalArgumentException.class,
                                "assignIeee754(new long[" + length + "])");
  }

  /**
   * Verifies that {@code assignIeee754(byte[])} throws {@code IllegalArgumentException}
   * if the array length is not 16, and leaves the instance unchanged.
   */
  @ParameterizedTest
  @ValueSource(ints = { 0, 8, 15, 17, 32 })
  @DisplayName("assignIeee754(byte[]) should throw IllegalArgumentException if length != 16")
  void shouldThrowOnWrongIeee754BytesLength(int length) {
    checkThrowsOnDirtyInstances(q -> q.assignIeee754(new byte[length]), IllegalArgumentException.class,
                                "assignIeee754(new byte[" + length + "])");
  }

  /* *******************************************************************************
   * Special values: assignNaN(), assignPositiveInfinity(), assignNegativeInfinity()
   *********************************************************************************/

  /**
   * Verifies that {@code assignNaN()} sets the fields to the canonical NaN
   * ({@code +, EXPONENT_OF_INFINITY, mantHi = 0x8000_0000_0000_0000, mantLo = 0}),
   * whatever value the instance held before.
   */
  @Test
  @DisplayName("assignNaN() should set canonical NaN")
  void shouldAssignNaNToDirtyInstance() {
    checkOnDirtyInstances(new Quadruple(false, EXPONENT_OF_INFINITY, 0x8000_0000_0000_0000L, 0),
                          Quadruple::assignNaN, "assignNaN()");
    final Quadruple q = new Quadruple().assignNaN();
    assertThat(q.isNaN()).as("isNaN() after assignNaN()").isTrue();
    assertThat(q.isInfinite()).as("isInfinite() after assignNaN()").isFalse();
  }

  /**
   * Verifies that {@code assignPositiveInfinity()} sets the fields to
   * {@code +, EXPONENT_OF_INFINITY, 0, 0}, whatever value the instance held before.
   */
  @Test
  @DisplayName("assignPositiveInfinity() should set +Infinity")
  void shouldAssignPositiveInfinityToDirtyInstance() {
    checkOnDirtyInstances(new Quadruple(false, EXPONENT_OF_INFINITY, 0, 0),
                          Quadruple::assignPositiveInfinity, "assignPositiveInfinity()");
    final Quadruple q = new Quadruple().assignPositiveInfinity();
    assertThat(q.isInfinite()).as("isInfinite() after assignPositiveInfinity()").isTrue();
    assertThat(q.isNaN()).as("isNaN() after assignPositiveInfinity()").isFalse();
    assertThat(q.isNegative()).as("isNegative() after assignPositiveInfinity()").isFalse();
  }

  /**
   * Verifies that {@code assignNegativeInfinity()} sets the fields to
   * {@code -, EXPONENT_OF_INFINITY, 0, 0}, whatever value the instance held before.
   */
  @Test
  @DisplayName("assignNegativeInfinity() should set -Infinity")
  void shouldAssignNegativeInfinityToDirtyInstance() {
    checkOnDirtyInstances(new Quadruple(true, EXPONENT_OF_INFINITY, 0, 0),
                          Quadruple::assignNegativeInfinity, "assignNegativeInfinity()");
    final Quadruple q = new Quadruple().assignNegativeInfinity();
    assertThat(q.isInfinite()).as("isInfinite() after assignNegativeInfinity()").isTrue();
    assertThat(q.isNaN()).as("isNaN() after assignNegativeInfinity()").isFalse();
    assertThat(q.isNegative()).as("isNegative() after assignNegativeInfinity()").isTrue();
  }

  /* *******************************************************************************
   * Helpers
   *********************************************************************************/

  /**
   * Applies the given assignment to each of the dirty instances and checks that
   * it returns the same instance, and that the fields of the instance are
   * equal to those of {@code expected}.
   *
   * @param expected    the instance with the expected values of the fields
   * @param assignment  the assignment to check, like {@code q -> q.assign(value)}
   * @param description description of the assignment, for failure messages
   */
  private static void checkOnDirtyInstances(Quadruple expected, UnaryOperator<Quadruple> assignment, String description) {
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances()) {
      final Quadruple q = dirty.getPayload();
      final String context = description + " to instance holding " + dirty.getName();
      final Quadruple result = assignment.apply(q);
      assertThat(result).as(context + ": must return this instance").isSameAs(q);
      assertSameFields(q, expected, context);
    }
  }

  /**
   * Applies the given assignment to each of the dirty instances and checks that
   * it throws an exception of the given type and leaves the instance unchanged.
   */
  private static void checkThrowsOnDirtyInstances(UnaryOperator<Quadruple> assignment,
                                                  Class<? extends Throwable> exceptionType, String description) {
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances()) {
      final Quadruple q = dirty.getPayload();
      final Quadruple copy = new Quadruple(q.isNegative(), q.exponent(), q.mantHi(), q.mantLo());
      final String context = description + " to instance holding " + dirty.getName();
      assertThatThrownBy(() -> assignment.apply(q)).as(context).isInstanceOf(exceptionType);
      assertSameFields(q, copy, context + ": the instance must not change");
    }
  }

  /** A new Quadruple with the given sign and exponent, and the mantissa from the given parts */
  private static Quadruple fromParts(boolean negative, int exponent, QuadParts p) {
    return new Quadruple(negative, exponent, p.mantHi(), p.mantLo());
  }

}
