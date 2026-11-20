package com.mvohm.quadruple.testutil;

import java.util.OptionalDouble;
import java.util.OptionalLong;

import com.mvohm.quadruple.Quadruple;

/**
 * A form of the call of a binary arithmetic operation of Quadruple (see {@link BinaryOperation}):
 * an instance or a static method, with the second operand of type Quadruple, double, or long.
 * <p>
 * The test data are always pairs of Quadruples; for the forms with a double or long argument,
 * the second operand is converted to the type of the argument, and the form is applicable only if
 * it can be converted exactly (see {@link #accepts(Quadruple)}). The conversion uses the model ({@link ExactValue}),
 * not the tested conversions of Quadruple.
 */
public enum CallForm {

  INSTANCE        ("a.op(Quadruple b)",                 true,  Type.QUADRUPLE),
  STATIC          ("Quadruple.op(Quadruple a, Quadruple b)", false, Type.QUADRUPLE),
  INSTANCE_DOUBLE ("a.op(double b)",                    true,  Type.DOUBLE),
  STATIC_DOUBLE   ("Quadruple.op(Quadruple a, double b)", false, Type.DOUBLE),
  INSTANCE_LONG   ("a.op(long b)",                      true,  Type.LONG),
  STATIC_LONG     ("Quadruple.op(Quadruple a, long b)", false, Type.LONG);

  enum Type { QUADRUPLE, DOUBLE, LONG }

  private final String description;
  private final boolean instance;
  private final Type argumentType;

  CallForm(String description, boolean instance, Type argumentType) {
    this.description = description;
    this.instance = instance;
    this.argumentType = argumentType;
  }

  /** @return true for instance methods, which assign the result to {@code this} and return it */
  public boolean isInstance()   { return instance; }
  Type argumentType()           { return argumentType; }

  /**
   * @return true if the value can be passed as the second operand of this form: always for Quadruple;
   * for double, if it is exactly representable as double (including NaN, infinities and both zeros);
   * for long, if it is an integer within the range of long, except -0 (long has no negative zero)
   */
  public boolean accepts(Quadruple b) {
    return switch (argumentType) {
      case QUADRUPLE -> true;
      case DOUBLE    -> toDouble(b).isPresent();
      case LONG      -> toLong(b).isPresent();
    };
  }

  /** @return the exact value of {@code q} as double, or empty if it is not exactly representable */
  static OptionalDouble toDouble(Quadruple q) {
    if (q.isNaN())      return OptionalDouble.of(Double.NaN);
    if (q.isInfinite()) return OptionalDouble.of(q.isNegative()? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
    final ExactValue.Rounding<Double> r = ExactValue.of(q).round(BinaryFormat.DOUBLE);
    return r.exact()? OptionalDouble.of(r.rounded()) : OptionalDouble.empty();
  }

  /** @return the exact value of {@code q} as long, or empty if it is not an integer within the range of long, or -0 */
  static OptionalLong toLong(Quadruple q) {
    if (q.isNaN() || q.isInfinite() || q.isZero() && q.isNegative())
      return OptionalLong.empty();
    final ExactValue exact = ExactValue.of(q);
    final long l = exact.toLongTruncated();
    return ExactValue.of(l).compareTo(exact) == 0? OptionalLong.of(l) : OptionalLong.empty();
  }

  @Override
  public String toString() {
    return description;
  }

}
