package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.testutil.QuadAssert.hex;

import java.nio.ByteBuffer;

/**
 * The 128 bits of an IEEE-754 quadruple-precision value, as two big-endian longs:
 * {@code hi} contains the sign bit, the 15-bit exponent, and the 48 most significant bits
 * of the significand, {@code lo} contains its 64 least significant bits.
 */
public record Ieee754Bits(long hi, long lo) {

  /** @return the bits as an array of two longs, {@code {hi, lo}} */
  public long[] toLongs() {
    return new long[] { hi, lo };
  }

  /** @return the bits as an array of 16 bytes in the big-endian order */
  public byte[] toBytes() {
    return ByteBuffer.allocate(16).putLong(hi).putLong(lo).array();
  }

  @Override
  public String toString() {
    return hex(hi, 16) + ", " + hex(lo, 16);
  }

}
