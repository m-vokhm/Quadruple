package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.Quadruple.EXPONENT_BIAS;
import static java.math.BigInteger.ONE;

import java.math.BigInteger;
import java.util.Random;

import org.junit.jupiter.api.Named;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testutil.ExactValue;
import com.mvohm.quadruple.testutil.QuadAssert;

/**
 * Small helpers shared by the data classes: building values, naming them, copying and negating named values.
 */
public final class DataHelpers {

  private static final BigInteger MASK_64 = ONE.shiftLeft(64).subtract(ONE);

  private DataHelpers() {}

  /** @return the exact value {@code 2^exponent} */
  public static ExactValue pow2(long exponent) {
    return ExactValue.of(false, ONE, exponent);
  }

  /** @return the Quadruple exactly equal to the given value; throws if it's not representable */
  public static Quadruple toQuadruple(ExactValue value) {
    final ExactValue.Rounding<Quadruple> r = value.round();
    if (!r.exact())
      throw new IllegalStateException("Test data error: the value is not representable as Quadruple: " + value);
    return r.rounded();
  }

  /** @return the value named by its fields */
  public static Named<Quadruple> named(Quadruple q) {
    return Named.of(QuadAssert.fieldsOf(q), q);
  }

  /** @return a new instance with the same name and value (the tests may modify the instances) */
  public static Named<Quadruple> copy(Named<Quadruple> v) {
    return Named.of(v.getName(), new Quadruple(v.getPayload()));
  }

  /** @return the value with the opposite sign, named {@code -(name)} */
  public static Named<Quadruple> negated(Named<Quadruple> v) {
    return Named.of("-(" + v.getName() + ")", negated(v.getPayload()));
  }

  /** @return a new instance with the opposite sign (for any value, including NaN and zeros) */
  public static Quadruple negated(Quadruple q) {
    return new Quadruple(!q.isNegative(), q.exponent(), q.mantHi(), q.mantLo());
  }

  /** @return a new instance with the positive sign (for any value, including NaN and zeros) */
  public static Quadruple abs(Quadruple q) {
    return new Quadruple(false, q.exponent(), q.mantHi(), q.mantLo());
  }

  /** @return the value of the 64 bits as an unsigned integer */
  public static BigInteger unsigned(long value) {
    return BigInteger.valueOf(value).and(MASK_64);
  }

  /** @return the unbiased exponent (the biased one taken as unsigned, minus the bias) */
  public static long unbiasedExponent(Quadruple q) {
    return (q.exponent() & 0xFFFF_FFFFL) - EXPONENT_BIAS;
  }

  /** A random normal value with the given unbiased exponent and a random sign; ~1/4 of them with {@code mantLo == 0} */
  public static Quadruple randomNormal(Random random, long exponent) {
    final long mantLo = random.nextInt(4) == 0? 0 : random.nextLong();     // some shorter mantissas
    return new Quadruple(random.nextBoolean(), (int)(EXPONENT_BIAS + exponent), random.nextLong(), mantLo);
  }

}
