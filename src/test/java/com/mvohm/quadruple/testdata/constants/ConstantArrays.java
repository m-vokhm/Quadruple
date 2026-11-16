package com.mvohm.quadruple.testdata.constants;

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
    Math.nextDown(Double.MIN_NORMAL),           // largest subnormal, all mantissa bits set
    Double.MIN_NORMAL / 2,                      // subnormal, single bit (MSB of mantissa)
    1.0,
    2.0,
    0.5,
    Math.nextUp(1.0),                           // lowest mantissa bit only
    Math.nextDown(2.0),                         // all mantissa bits set
    Math.scalb(1.0, 1023),                      // highest power of 2
    Math.scalb(1.0, -1022),                     // == MIN_NORMAL, lowest normal power of 2
    Math.PI,

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
    -Math.nextDown(Double.MIN_NORMAL),
    -Double.MIN_NORMAL / 2,
    -1.0,
    -2.0,
    -0.5,
    -Math.nextUp(1.0),
    -Math.nextDown(2.0),
    -Math.scalb(1.0, 1023),
    -Math.scalb(1.0, -1022),
    -Math.PI,
  };

  public static final long[] BASIC_l2Q_CONVERSION_DATA = {
    Long.MAX_VALUE,
    Long.MAX_VALUE / 2,
    Integer.MAX_VALUE * 2L,
    Integer.MAX_VALUE,
    Integer.MAX_VALUE / 2,
    1234567890,
    12345,
    1L << 62,                                   // powers of 2: mantissa == 0
    1L << 32,
    4,
    3,                                          // all bits set after the implicit unity
    2,
    1,                                          // the only bit is shifted out entirely
    0,
    -1,
    -2,
    -3,
    -4,
    -(1L << 32),
    -(1L << 62),
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
