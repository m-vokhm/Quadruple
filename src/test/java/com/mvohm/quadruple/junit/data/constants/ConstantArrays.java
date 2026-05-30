package com.mvohm.quadruple.junit.data.constants;

import com.mvohm.quadruple.Quadruple;

public class ConstantArrays {

  public static final double[] BASIC_d2Q_CONVERSION_DATA = {
    0d,
    -0d,
    Double.NaN,
    Double.NEGATIVE_INFINITY,
    Double.POSITIVE_INFINITY,

    Double.MAX_VALUE,
    Double.MAX_VALUE - Double.MAX_VALUE * 1e-16,
    1.2345e25,
    5.4351e-25,
    Double.MIN_NORMAL,
    Double.MIN_NORMAL + Double.MIN_NORMAL * 1e-16,
    Double.MIN_NORMAL - Double.MIN_NORMAL / 4,
    Double.MIN_NORMAL / 256,
    Double.MIN_VALUE * 2,
    Double.MIN_VALUE * 10,
    Double.MIN_VALUE,

    -Double.MAX_VALUE,
    -(Double.MAX_VALUE - Double.MAX_VALUE * 1e-16),
    -1.2345e25,
    -5.4351e-25,
    -Double.MIN_NORMAL,
    -(Double.MIN_NORMAL + Double.MIN_NORMAL * 1e-16),
    -(Double.MIN_NORMAL - Double.MIN_NORMAL / 4),
    -Double.MIN_NORMAL / 256,
    -Double.MIN_VALUE * 2,
    -Double.MIN_VALUE * 10,
    -Double.MIN_VALUE,
  };

  public static final long[] BASIC_l2Q_CONVERSION_DATA = {
    Long.MAX_VALUE,
    Long.MAX_VALUE / 2,
    Integer.MAX_VALUE * 2L,
    Integer.MAX_VALUE,
    Integer.MAX_VALUE / 2,
    1234567890,
    12345,
    0,
    -12345,
    -1234567890,
    Integer.MIN_VALUE / 2 + 1,
    Integer.MIN_VALUE + 1,
    Integer.MIN_VALUE * 2L + 1,
    Long.MIN_VALUE / 2 + 1,
    Long.MIN_VALUE + 1,
    Long.MIN_VALUE,
  };

}
