package com.mvohm.quadruple.testutil;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;

import com.mvohm.quadruple.Quadruple;

/**
 * A binary arithmetic operation of Quadruple, callable in all its forms ({@link CallForm}),
 * with its counterparts for double (the reference for special values) and for the model ({@link ExactValue}).
 * <p>
 * {@link #apply(CallForm, Quadruple, Quadruple)} calls the operation in the given form and checks the contract
 * of the form: an instance method must return {@code this} (holding the result) and must not change the argument;
 * a static method must return a new instance and must not change the operands.
 * So the tests need to check only the value of the result, the same way for all forms.
 */
public enum BinaryOperation {

  ADD("add",
      (a, b) -> a.add(b),      (a, b) -> Quadruple.add(a, b),
      (a, b) -> a.add(b),      (a, b) -> Quadruple.add(a, b),
      (a, b) -> a.add(b),      (a, b) -> Quadruple.add(a, b),
      (a, b) -> a + b,         ExactValue::add),
  SUBTRACT("subtract",
      (a, b) -> a.subtract(b), (a, b) -> Quadruple.subtract(a, b),
      (a, b) -> a.subtract(b), (a, b) -> Quadruple.subtract(a, b),
      (a, b) -> a.subtract(b), (a, b) -> Quadruple.subtract(a, b),
      (a, b) -> a - b,         ExactValue::subtract),
  MULTIPLY("multiply",
      (a, b) -> a.multiply(b), (a, b) -> Quadruple.multiply(a, b),
      (a, b) -> a.multiply(b), (a, b) -> Quadruple.multiply(a, b),
      (a, b) -> a.multiply(b), (a, b) -> Quadruple.multiply(a, b),
      (a, b) -> a * b,         ExactValue::multiply),
  DIVIDE("divide",
      (a, b) -> a.divide(b),   (a, b) -> Quadruple.divide(a, b),
      (a, b) -> a.divide(b),   (a, b) -> Quadruple.divide(a, b),
      (a, b) -> a.divide(b),   (a, b) -> Quadruple.divide(a, b),
      (a, b) -> a / b,         ExactValue::divide);

  @FunctionalInterface private interface WithQuadruple { Quadruple apply(Quadruple a, Quadruple b); }
  @FunctionalInterface private interface WithDouble    { Quadruple apply(Quadruple a, double b); }
  @FunctionalInterface private interface WithLong      { Quadruple apply(Quadruple a, long b); }

  private final String name;
  private final WithQuadruple instance, statik;
  private final WithDouble instanceDouble, staticDouble;
  private final WithLong instanceLong, staticLong;
  private final DoubleBinaryOperator onDoubles;
  private final BinaryOperator<ExactValue> onExactValues;

  BinaryOperation(String name, WithQuadruple instance, WithQuadruple statik,
                  WithDouble instanceDouble, WithDouble staticDouble,
                  WithLong instanceLong, WithLong staticLong,
                  DoubleBinaryOperator onDoubles, BinaryOperator<ExactValue> onExactValues) {
    this.name = name;
    this.instance = instance;
    this.statik = statik;
    this.instanceDouble = instanceDouble;
    this.staticDouble = staticDouble;
    this.instanceLong = instanceLong;
    this.staticLong = staticLong;
    this.onDoubles = onDoubles;
    this.onExactValues = onExactValues;
  }

  /**
   * Calls the operation {@code a op b} in the given form and checks the contract of the form
   * (see the class description). The operands are not modified (an instance method is called on a copy of {@code a}).
   * @param b the second operand; must be accepted by the form ({@link CallForm#accepts(Quadruple)})
   * @return the result
   */
  public Quadruple apply(CallForm form, Quadruple a, Quadruple b) {
    if (!form.accepts(b))
      throw new IllegalArgumentException(form + " does not accept " + fieldsOf(b));
    final Quadruple a0 = new Quadruple(a), b0 = new Quadruple(b);
    final String context = describe(form, a, b);

    final Quadruple target = form.isInstance()? new Quadruple(a) : a;
    final Quadruple result = switch (form) {
      case INSTANCE        -> instance.apply(target, b);
      case STATIC          -> statik.apply(target, b);
      case INSTANCE_DOUBLE -> instanceDouble.apply(target, CallForm.toDouble(b).getAsDouble());
      case STATIC_DOUBLE   -> staticDouble.apply(target, CallForm.toDouble(b).getAsDouble());
      case INSTANCE_LONG   -> instanceLong.apply(target, CallForm.toLong(b).getAsLong());
      case STATIC_LONG     -> staticLong.apply(target, CallForm.toLong(b).getAsLong());
    };

    if (form.isInstance())
      assertThat(result).as(context + " must return this").isSameAs(target);
    else
      assertThat(result).as(context + " must return a new instance").isNotSameAs(a).isNotSameAs(b);
    assertSameFields(a, a0, "the first operand after " + context);
    assertSameFields(b, b0, "the second operand after " + context);
    return result;
  }

  /** @return the result of the operation on doubles */
  public double applyToDoubles(double a, double b) {
    return onDoubles.applyAsDouble(a, b);
  }

  /** @return the result of the operation in the model */
  public ExactValue applyToExactValues(ExactValue a, ExactValue b) {
    return onExactValues.apply(a, b);
  }

  /** @return a description of the call, like "add: a.op(Quadruple b), a = (...), b = (...)" */
  public String describe(CallForm form, Quadruple a, Quadruple b) {
    return name + ": " + form + ", a = (" + fieldsOf(a) + "), b = (" + fieldsOf(b) + ")";
  }

  @Override
  public String toString() {
    return name;
  }

}
