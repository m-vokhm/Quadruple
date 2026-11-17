package com.mvohm.quadruple.testdata;

import com.mvohm.quadruple.testutil.QuadAssert;

/**
 * Low-level parts of a Quadruple value, as they are stored in its fields
 * (the exponent is biased, as it is stored in the {@code exponent} field).
 * <p>
 * Used as test data for methods that take the parts directly,
 * like {@code assign(boolean, int, long, long)}.
 */
public record QuadParts(boolean negative, int exponent, long mantHi, long mantLo) {

  @Override
  public String toString() {
    return QuadAssert.formatFields(negative, exponent, mantHi, mantLo);
  }

}
