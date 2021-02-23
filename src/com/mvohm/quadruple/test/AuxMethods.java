package com.mvohm.quadruple.test;

import static com.mvohm.quadruple.test.Consts.*;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import com.mvohm.quadruple.Quadruple;

/**
 * A set of convenience methods to be used by other classes of the quadruple.test package.
 * Includes mainly a number of methods to convert values of various types to each other,
 * and to output values to console.
 *
 * @author M.Vokhmentev
 *
 */

public class AuxMethods {

  /**
   * A simplest 'logger' to write to a file certain info while debugging.
   * Had been used for debugging. Currently not used, but may still be useful in the future.
   */
  private static PrintStream logFile = null;



 /****************************************************************************************
  *** Output to the console **************************************************************
  ****************************************************************************************/

  /** == System.out.println(); */
	public static void say() 	{ System.out.println(); }

	/** == System.out.println(Object o); */
	public static void say(Object o) 	{ System.out.println(o); }

	/** == System.out.print(Object o); */
	public static void say_(Object o) 	{ System.out.print(o); }

	/** == System.out.println(String.format(String format, Object... args) */
	public static void say(String format, Object... args) { System.out.println(String.format(format, args)); }

  /** == System.out.print(String.format(String format, Object... args) */
	public static void say_(String format, Object... args) { System.out.print(String.format(format, args)); }

	/** Terminates execution with exit code == 0 */
	public static void exit() { System.exit(0); }

 /****************************************************************************************
  *** Conversions to Hexadecimal string representations **********************************
  ****************************************************************************************/

  /**
   * Returns a hexadecimal string representation of the given int value,
   * with digits grouped by 4, e.g. "60bf_b765".
   * @param iValue the value to convert to hexadecimal string
   * @return the hexadecimal string representation of the given int value
   */
  public static String hexStr(int iValue) {
    return String.format(
        "%04x_%04x",
        iValue >> 16 & 0xFFFF, iValue & 0xFFFF);
  } // public static String hexStr(int iValue) {

	/**
	 * Returns a hexadecimal string representation of the given long value,
	 * with digits grouped by 4, e.g. "60bf_b765_972a_f2a2".
	 * @param lValue the value to convert to hexadecimal string
	 * @return the hexadecimal string representation of the given long value
	 */
  public static String hexStr(long lValue) {
    return String.format(
        "%04x_%04x_%04x_%04x",
        lValue >> 48 & 0xFFFF, lValue >> 32 & 0xFFFF,
        lValue >> 16 & 0xFFFF, lValue & 0xFFFF);
  } // public static String hexStr(long lValue) {

  /**
   * Returns a hexadecimal string representation of the given double value,
   * that separately shows its sign, mantissa, and exponent, e.g. <nobr>"-6_a09e_667f_3bcd e 3ff"</nobr>
   * @param dValue -- the value to convert to hexadecimal string
   * @return the hexadecimal string representation of the given double value
   */
  public static  String hexStr(double dValue) {
    final long l = Double.doubleToLongBits(dValue);
    String expStr = hexStr((l & DOUBLE_EXP_MASK) >> 52);
    expStr = expStr.substring(expStr.length() - 3, expStr.length());
    String mantStr = hexStr(l);
    mantStr = mantStr.substring(mantStr.length() - 16, mantStr.length());
    final String signStr = (l & DOUBLE_SIGN_MASK) == 0? "+" : "-";
    return String.format("%s%s e %s", signStr, mantStr, expStr);
  } // public static  String hexStr(double dValue) {

  /**
   * Returns a hexadecimal string representation of the given {@code Quadruple} value,
   * that separately shows its sign, mantissa, and exponent, e.g.
   * <nobr>"+3c7e_7dc8_2aef_ddf3 ab41_94a5_1831_2fce e 10ac_83b0"</nobr>
   * @param dValue the value to convert to hexadecimal string
   * @return the hexadecimal string representation of the given {@code Quadruple} value
   */
	public static String hexStr(Quadruple qValue) {
		return String.format(	"%s%s %s e %s", (qValue.isNegative()? "-":"+"),
				  				hexStr(qValue.mantHi()), hexStr(qValue.mantLo()),
				  				hexStr(qValue.exponent()));
	} // public static String hexStr(Quadruple qValue) {

	/**
   * Returns a hexadecimal string representation of the given array of {@code long}s,
   * with digits grouped by 4. Places each long in a new line, preceded by its index, e.g.<pre>   0: 79a5_120e_08e4_6ad2
   1: ffff_ffff_742f_c7cb
   2: abcd_ef01_2345_6789</pre>
   * Had been used for debugging. Currently not used, but may still be useful in the future.<br><br>
   * @param buff the buffer of {@code long}s to be represented as hex strings
   * @return a string with hexadecimal representation of the contents of the buffer, as described above
	 */
	public static String hexStr(long[] buffer) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < buffer.length; i++) {
			if (sb.length() != 0) sb.append(System.lineSeparator());
			sb.append(String.format("%2d: %s", i, hexStr(buffer[i])));
		}
		return sb.toString();
	} // public static String hexStr(long[] buffer) {

  /**
   * Returns a hexadecimal string representation of the given array of {@code long}s,
   * treating it as an 'unpacked' value, i.e a buffer where all the elements except the first one
   * contain non-null values in their least significant 32 bits only
   * (this format is used by {@code Quadruple} internally).
   * If the buffer has an odd number of items, the last value will be complemented with 0.
   * For example, a buffer containing
   * <pre> 0: 0000_0001_2222_2222
 1: 3333_3333_4444_4444
 2: abcd_ef01_2345_6789
 3: 1234_5678_fedc_ba98</pre>
 will be shown like <pre>0000_0001 2222_2222_4444_4444 2345_6789_fedc_ba98</pre>
   * Had been used for debugging. Currently not used, but may still be useful in the future.<br><br>
   * @param buff the buffer of {@code long}s to be represented as hex strings
   * @return a string with hexadecimal representation of the contents of the buffer, as described above
   */
  public static String hexStr_u(long[] buffer) {
    final StringBuilder sb = new StringBuilder();
    sb.append(hexStr((int)(buffer[0] >>> 32)) + " ");
    for (int i = 0; i < buffer.length; i += 2) { // Big-endian ++
      final long lowerHalf = buffer.length >= i + 2 ? buffer[i+1] & LOWER_32_BITS: 0;
      sb.append(hexStr(buffer[i] << 32 | lowerHalf) + " ");
    }
    return sb.toString();
  } // public static String hexStr_u(long[] buffer) {

	/**
	 * Returns a hexadecimal string representation of the given array of {@code long}s,
   * with digits grouped by 4. Places all values in a single line, e.g.
   * <pre>  1111_1111_2222_2222 3333_3333_4444_4444 abcd_ef01_2345_6789</pre>
   * Had been used for debugging. Currently not used, but may still be useful in the future.<br><br>
   * @param buffer the buffer of {@code long}s to be represented as a hex string
   * @return a string with hexadecimal representation of the contents of the buffer, as described above
	 */
	public static String hexStr_(long[] buffer) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < buffer.length; i++) { // Big-endian ++
			if (sb.length() != 0) sb.append(" ");
			sb.append(hexStr(buffer[i]));
		}
		return sb.toString();
	} // public static String hexStr_(long[] buffer) {

	/**
	 * Returns a string representing the bits of the argument. 1 is depicted with '#', and 0 with '_', so that
	 * <pre>0xABCD_EF01_2345_6789L</pre> will be shown as
	 * <nobr><pre>#_#_ #_## ##__ ##_# ###_ #### ____ ___# __#_ __## _#__ _#_# _##_ _### #___ #__#.</pre></nobr><br>
   * Had been used for debugging. Currently not used, but may still be useful in the future.<br><br>
	 * @param lValue the value to show
	 * @return a string representing the bits of the argument
	 */
	public static String binStr(long lValue) {
		final StringBuilder sb = new StringBuilder(80);
		long mask = DOUBLE_SIGN_MASK; int count = 0;
		do {
			if ((mask & lValue) != 0) sb.append('#');
			else sb.append('_');
			mask >>>= 1;
			if (++count == 4) { sb.append(' '); count = 0; }
		} while (mask != 0);
		return sb.toString();
	} // public static String binStr(long lValue) {

	/****************************************************************************************
	 *** Converting values from Quadruple to BigDecimal and vice versa **********************
	 ****************************************************************************************/

  /**
   * Calculates the value stored by the instance of {@code Quadruple}, using the values of its fields,
   * rounds it to 140 decimal digits, and returns it as {@code BigDecimal}.
   * @param qValue a {@code Quadruple} instance to extract the value from
   * @return the value of the given {@code Quadruple} as {@code BigDecimal}
   */
  public static BigDecimal bigDecimalValueOf(Quadruple qValue) {
    checkNaNInfinity(qValue);                      // Throws exception if NaN or Infinity
    if (qValue.isZero()) return BigDecimal.ZERO;   // BigDecimal with value of -0 can't exist

    BigDecimal mant = buildBDMantissa(qValue);

    int exp2 = qValue.unbiasedExponent(); // Binary exponent in normal form
    final boolean expNegative = exp2 < 0;
    exp2 = Math.abs(exp2);

    // 20.12.23 12:44:56 Calculates the exact value if the exponent is less than 1023
    // When testing conversion from Quadruple to double we need exact values
    // to perform correct round-off, like BigDecimal does
    final BigDecimal pow2 = exp2 < 1024?
                              BD_TWO.pow(exp2):
                              expNegative?
                                powerOfTwo(exp2, MC_140_FLOOR):         // 2^exponent
                                powerOfTwo(exp2, MC_140_CEILING);

    // mantissa * 2^exponent
    if (exp2 < 1024) {
      if (expNegative)  mant = mant.divide(pow2);
      else              mant = mant.multiply(pow2);
    } else {
      if (expNegative)  mant = mant.divide(pow2, MC_140_CEILING);
      else              mant = mant.multiply(pow2, MC_140_CEILING);
    }

    if (qValue.isNegative()) mant = mant.negate();
    return mant;
  } // private BigDecimal bigDecimalValueOf(Quadruple testedQ) {

  /**
   * Creates a new {@code Quadruple} instance containing the value passed in the {@code value} parameter.
	 * @param value the value to assign to the newly-created {@code Quadruple} instance
	 * @return the newly-created {@code Quadruple} instance
	 */
	public static Quadruple buildQuadruple(BigDecimal value) {
		return buildQuadruple(value, null);
	}

	/**
	 * Constructs a Quadruple containing the value passed in the {@code value} parameter
	 * and puts it in the {@code result}.
	 * If the {@code result} parameter is null, creates a new {@code Quadruple} instance, otherwise
	 * assigns the value of the {@code value} parameter to the given {@code Quadruple} instance.
   * @param value the value to assign to the given or a newly-created {@code Quadruple} instance
	 * @param result an instance of {@code Quadruple} to assign the value to, or null
   * @return the given or the newly-created {@code Quadruple} instance with the given value
	 */
	public static Quadruple buildQuadruple(BigDecimal value, Quadruple result) {
		if (value == null) return null;
		if (result == null) result = new Quadruple();

		if (value.signum() == 0)
			return result.assign(0, 0, 0);

		value = value.stripTrailingZeros(); // Eliminates extra work and thus speeds up in some cases
		final boolean negative = value.signum() < 0;
		if (negative) {
			value = value.negate();        // value = abs(value);
			result.assign(true, 0, 0, 0);  // result = -0;, to keep the sign
		}

		final int exp10 = value.precision() - value.scale() - 1;  // Decimal exponent
		if (exceedsNormalRange(result, exp10)) 			              // May assign 0 or Infinity to result
			return result; 					 														    // Returns true in such a case

		long exp2 = findBinaryExponent(value, exp10);

		final boolean negExp = exp2 < 0; exp2 = Math.abs(exp2);
		BigDecimal mant2 = findMant2(value, exp2, negExp);

		// Correct possible logarithm inaccuracy
		if (mant2.compareTo(BigDecimal.ONE) < 0) {
			mant2 = mant2.multiply(BD_TWO);
			exp2 += negExp? 1 : -1;
		} else if (mant2.compareTo(BD_TWO) >= 0) {
			mant2 = mant2.divide(BD_TWO);
			exp2 += negExp? -1 : 1;			// Seems like 1 here never can occur
		}

		// the binary mantissa as two longs with implied unity
		exp2 = findMantValues(result, mant2, exp2, negExp); // Assigns mantissa values, exponent remains 0
		exp2 = (negExp? -exp2 : exp2) + EXP_0Q;

		if (exp2 <= 0) exp2 = makeSubnormal(result, exp2);
		final int exponent = (int)exp2;

		if (exponent == -1) // Infinity
			return result.assign(negative, exponent, 0, 0);
		else
			return result.assign(negative, exponent, result.mantHi(), result.mantLo());

	}  // private Quadruple buildQuadruple(BigDecimal value, Quadruple result) {

  /****************************************************************************************
   *** Converting values from Quadruple to String and vice versa **************************
   ****************************************************************************************/

	/**
	 * Converts the value of the given {@code Quadruple} instance to a {@code String} representation
	 * formatted as a decimal number in computerized scientific notation with 43 digits after the point
	 * (similar to formatting BigDecimal with format string "%.e43").
	 * For {@code Infinity}, {@code -Infinity}, and {@code NaN} returns their respective string notations.
	 * @param value a {@code Quadruple} instance to format its value
	 * @return a string representation of the given {@code Quadruple} value
	 */
  public static String quadToString43(Quadruple value) {
    return formatQuadruple("%.80e", value);
  } // public static String quadToString(Quadruple value) {

  /**
   * Converts the value of the given {@code Quadruple} instance to a {@code String} representation
   * formatted as a decimal number in computerized scientific notation with 80 digits after the point
   * (similar to formatting BigDecimal with format string "%.e80").
   * For {@code Infinity}, {@code -Infinity}, and {@code NaN} returns their respective string notations.
   * @param value a {@code Quadruple} instance to format its value
   * @return a string representation of the given {@code Quadruple} value
   */
  public static String quadToString80(Quadruple value) {
    return formatQuadruple("%.80e", value);
  } //public static String quadToString80(Quadruple value) {

  /**
   * Converts the value of the given {@code Quadruple} instance to a {@code String} representation
   * formatted in accordance with the given format string.
   * For {@code Infinity}, {@code -Infinity} and {@code NaN} returns their respective string notations.
   * @param format a format string to format the value
   * @param value a {@code Quadruple} instance to format its value
   * @return a string representation of the given {@code Quadruple} value
   */
  public static String formatQuadruple(String format, Quadruple value) {
    if (value.exponent() == Quadruple.EXP_INF)
      if ((value.mantHi() | value.mantLo()) != 0) // NaN
        return "NaN";
      else
        return value.isNegative()?  "-Infinity" : "Infinity" ;
    return String.format(format, bigDecimalValueOf(value));
  } // public static String formatQuadruple(String format, Quadruple value) {

	/**
   * Creates a new {@code Quadruple} instance containing the value passed in the {@code value} parameter.
	 * @param value a string representation of the value to be assigned to the new {@code Quadruple} instance
	 * @return the newly-created instance of {@code Quadruple} with the given value
   * @throws NumberFormatException if {@code value} is not a valid
   *         representation of a Quadruple.
	 */
	public static Quadruple buildQuadruple(String value) {
		return buildQuadruple(value, null);
	} // public static Quadruple buildQuadruple(String value) {

	/**
   * Constructs a Quadruple containing the value passed in the {@code value} parameter
   * and puts it in the {@code result}.
   * If the {@code result} parameter is null, creates a new {@code Quadruple} instance, otherwise
   * assigns the value of the {@code value} parameter to the given {@code Quadruple} instance.
   * @param value a string representation of the value to be assigned to the new {@code Quadruple} instance
   * @param result
   * @return the newly-created instance of {@code Quadruple} with the given value
   * @throws NumberFormatException if {@code value} is not a valid
   *         representation of a Quadruple.
	 */
	public static Quadruple buildQuadruple(String value, Quadruple result) {
		if (result == null) result = new Quadruple();
		value = value.trim().toLowerCase();
		BigDecimal srcBD = null;
		switch (value) {
			case "infinity":
				result = result.assignPositiveInfinity(); break;
			case "-infinity":
				result = result.assignNegativeInfinity();  break;
			case "nan":
				result = result.assignNaN();  break;
			default:
				// Common case (a numeric value)
				// Converts String -> BigDecimal as a source,
				// source BigDecimal -> Quadruple as a result
				srcBD = new BigDecimal(value, MC_140_HALF_UP); 					// Can throw an exception, if format is invalid
				result = buildQuadruple(srcBD, result);				// The Quadruple to test. May be Infinity
				if (result.isZero() && value.startsWith("-")) 	// -0
					result.assign(true, result.exponent(), result.mantLo(), result.mantHi());
		};

		return result;
	} // public static Quadruple buildQuadruple(String value, Quadruple testedQ) {

  /****************************************************************************************
   *** Building BigDecimal values of various values of other types ************************
   ****************************************************************************************/

  /**
   * Converts the value of the given {@code number} to a {@code BigDecimal}.
   * @param number a value, that can be {@code BigDecimal}, {@code Double}, {@code Long} or {@code Integer}
   * @return a {@code BigDecimal} with the value of the given number
   */
  public static BigDecimal bd(Number number) {
    return
      number instanceof BigDecimal? (BigDecimal)number:
      number instanceof Double?     BigDecimal.valueOf((double)number): // Exact value
      number instanceof Long?       BigDecimal.valueOf((long)number):
                                    BigDecimal.valueOf((int)number);
  } // public static BigDecimal bd(Number number) {

  /**
   * Converts the value of the given {@code double} to a {@code BigDecimal}.
   * @param number a value of type {@code double}
   * @return a {@code BigDecimal} with the value of the given argument
   * @see BigDecimal#valueOf(double)
   */
  public static BigDecimal bd(double value) {
    return BigDecimal.valueOf(value);
  } // public static BigDecimal bd(double value) {

  /**
   * Converts the value of the given {@code long} to a {@code BigDecimal}.
   * @param number a value of type {@code long}
   * @return a {@code BigDecimal} with the value of the given argument
   * @see BigDecimal#valueOf(long)
   */
  public static BigDecimal bd(long value) {
    return BigDecimal.valueOf(value);
  } //public static BigDecimal bd(long value) {

  /**
   * Converts the value represented by the given {@code String} to a {@code BigDecimal}.
   * @param number a value expressed as a {@code String}
   * @return a {@code BigDecimal} with the value of the given argument
   * @throws NumberFormatException if {@code value} is not a valid representation
   *          of a {@code BigDecimal}.
   * @see BigDecimal#BigDecimal(String)
   */
  public static BigDecimal bd(String value) {
    return new BigDecimal(value);
  } // public static BigDecimal bd(String value) {

  /**
   * Builds a {@code BigDecimal} whose value is equal to the value
   * that a Quadruple would have if it were built of the given parts,
   * with an accuracy of 140 decimal places.
   * If the value is not convertible to {@code BigDecimal} (NaN or Infinity), {@code NumberFormatException} is thrown.
   * @param sign the sign of the corresponding {@code Quadruple}, {@code true} for negative values
   * @param mantHi the most significant 64 bits of the mantissa of the corresponding {@code Quadruple}
   * @param mantLo the least significant 64 bits of the mantissa of the corresponding {@code Quadruple}
   * @param exponent the exponent of the corresponding {@code Quadruple}
   * @return a {@code BigDecimal} with the value of a {@code Quadruple} built of the given parts
   * @throws NumberFormatException if the value of the corresponding {@code Quadruple} can't be expressed as {@code BigDecimal}
   */
  public static BigDecimal bd(boolean sign, long mantHi, long mantLo, int exponent) {
    final BigInteger biMantHi = new BigInteger(Long.toUnsignedString(mantHi));
    final BigInteger biMantLo = new BigInteger(Long.toUnsignedString(mantLo));

    if (exponent == EXP_INF) {
      throw new NumberFormatException("Can't convert "
                  + (((mantHi | mantLo) != 0)?  "NaN" :
                      sign?                     "NEGATIVE_INFINITY" :
                                                "POSITIVE_INFINITY")
                  + " to BigDecimal"); // " + hexStr(testedQ));
    }

    BigDecimal bd;
    if (exponent == 0) { // Subnormal
      bd = new BigDecimal(biMantHi).multiply(BD_2$_64).add(new BigDecimal(biMantLo).multiply(BD_2$_128));
      bd = bd.multiply(powerOfTwo(-EXP_0Q + 1, MC_140_HALF_UP));
    } else {
      bd = BigDecimal.ONE.add(new BigDecimal(biMantHi).multiply(BD_2$_64).add(new BigDecimal(biMantLo).multiply(BD_2$_128)));
      bd = bd.multiply(powerOfTwo(exponent + 2 + EXP_0Q, MC_140_HALF_UP));
    }
    bd = bd.stripTrailingZeros();
    return sign? bd.negate() : bd;
  } // public static BigDecimal bd(boolean sign, long mantHi, long mantLo, int exponent) {

  /**
   * Builds a non-negative BigDecimal whose value is equal to the value
   * that a Quadruple would have if it were built of the given parts,
   * with an accuracy of 140 decimal places.
   * If the value is not convertible to {@code BigDecimal} (NaN or Infinity), {@code NumberFormatException} is thrown.
   * @param mantHi the most significant 64 bits of the mantissa of the corresponding {@code Quadruple}
   * @param mantLo the least significant 64 bits of the mantissa of the corresponding {@code Quadruple}
   * @param exponent the exponent of the corresponding {@code Quadruple}
   * @return a {@code BigDecimal} with the value of a {@code Quadruple} built of the given parts
   * @throws NumberFormatException if the value of the corresponding {@code Quadruple} can't be expressed as {@code BigDecimal}
   */
  public static BigDecimal bd(long mantHi, long mantLo, int exponent) {
    return bd(false, mantHi, mantLo, exponent).stripTrailingZeros();
  } // public static BigDecimal bd(long mantHi, long mantLo, int exponent) {

  /****************************************************************************************
   *** Arithmetic operations on {@code BigDecimal} values *********************************
   ****************************************************************************************/

  /**
   * Calculates and returns a {@code BigDecimal} sum of the given operands,
   * with accuracy of 120 decimal digits.
   * @param op1 the first summand
   * @param op2 the second  summand
   * @return the resulting sum
   */
  public static BigDecimal add(Number op1, Number op2) {
    return bd(op1).add(bd(op2), MC_120_HALF_EVEN);
  } // public static BigDecimal add(Number op1, Number op2) {

  /**
   * Calculates and returns a {@code BigDecimal} sum of the given operands,
   * with accuracy defined by the {@code mc} parameter
   * @param op1 the first summand
   * @param op2 the second  summand
   * @param mc a {@code MathContest} instance that defines the precision of the calculation
   * @return the resulting quotient
   */
  public static BigDecimal add(Number op1, Number op2, MathContext mc) {
    return bd(op1).add(bd(op2), mc);
  } // public static BigDecimal add(Number op1, Number op2, MathContext mc) {

  /**
   * Calculates and returns a {@code BigDecimal} difference of the given operands,
   * with accuracy of 120 decimal digits.
   * @param op1 the minuend
   * @param op2 the subtrahend
   * @return the resulting difference
   */
  public static BigDecimal sub(Number op1, Number op2) {
    return bd(op1).subtract(bd(op2), MC_120_HALF_EVEN);
  } // public static BigDecimal sub(Number op1, Number op2) {

  /**
   * Calculates and returns a {@code BigDecimal} difference of the given operands,
   * with accuracy defined by the {@code mc} parameter
   * @param op1 the minuend
   * @param op2 the subtrahend
   * @param mc a {@code MathContest} instance that defines the precision of the calculation
   * @return the resulting difference
   */
  public static BigDecimal sub(Number op1, Number op2, MathContext mc) {
    return bd(op1).subtract(bd(op2), mc);
  }

  /**
   * Calculates and returns a {@code BigDecimal} product of the given factors,
   * with accuracy of 120 - n decimal digits, where n is the amount of the factors.
   * @param factors an array of factors to multiply
   * @return the product of the given factors
   */
	public static BigDecimal mult(Number... factors) {
	  return mult(MC_120_HALF_EVEN, factors);
	} // public static BigDecimal mult(Number... factors) {

  /**
   * Calculates and returns a {@code BigDecimal} product of the given factors,
   * with the accuracy of {@code p - n}, where p is the precision
   * determined by the {@code mc} parameter, and n is the number the factors.
   * @param factors an array of factors to be multiplied
   * @return the product of the given factors
   */
	public static BigDecimal mult(MathContext mc, Number... factors) {
		BigDecimal product = bd(factors[0]);
		for (int i = 1; i < factors.length; i++)
			product = product.multiply(bd(factors[i]), mc);
		return product;
	} // public static BigDecimal mult(MathContext mc, Number... factors) {

  /**
   * Calculates and returns a {@code BigDecimal} product of the given factors,
   * with accuracy of 120 decimal digits.
	 * @param op1 the first operand
	 * @param op2 the second operand
   * @return the product of the given factors
	 */
	public static BigDecimal mult(Number op1, Number op2) {
		return bd(op1).multiply(bd(op2), MC_120_HALF_EVEN);
	} // public static BigDecimal mult(Number op1, Number op2) {

  /**
   * Calculates and returns a {@code BigDecimal} product of the given factors,
   * with the accuracy determined by the {@code mc} parameter.
   * @param op1 the first operand
   * @param op2 the second operand
   * @param mc a {@code MathContest} instance that defines the precision of the calculation
   * @return the product of the given factors
   */
	public static BigDecimal mult(Number op1, Number op2, MathContext mc) {
		return bd(op1).multiply(bd(op2), mc);
	} // public static BigDecimal mult(Number op1, Number op2, MathContext mc) {

  /**
   * Calculates and returns a {@code BigDecimal} quotient of the given operands,
   * with accuracy of 120 decimal digits.
   * @param op1 the dividend
   * @param op2 the divisor
   * @return the resulting quotient
   */
	public static BigDecimal div(Number op1, Number op2) {
		return bd(op1).divide(bd(op2), MC_120_HALF_EVEN);
	} // public static BigDecimal div(Number op1, Number op2) {

  /**
   * Calculates and returns a {@code BigDecimal} quotient of the given operands,
   * with accuracy defined by the {@code mc} parameter
   * @param op1 the dividend
   * @param op2 the divisor
   * @param mc a {@code MathContest} instance that defines the precision of the calculation
   * @return the resulting quotient
   */
	public static BigDecimal div(Number op1, Number op2, MathContext mc) {
		return bd(op1).divide(bd(op2), mc);
	} // public static BigDecimal div(Number op1, Number op2, MathContext mc) {

  /****************************************************************************************
   *** String representations of BigDecimal values of arguments of different types ********
   ****************************************************************************************/

  /**
   * Builds a {@code BigDecimal} whose value is equal to the value
   * that a Quadruple would have if it were built of the given parts,
   * with an accuracy of 140 decimal places, and returns its {@code String} representation.
   * For {@code Infinity}, {@code -Infinity}, and {@code NaN} returns their respective string notations.
   * @param sign the sign of the corresponding {@code Quadruple}, {@code true} for negative values
   * @param mantHi the most significant 64 bits of the mantissa of the corresponding {@code Quadruple}
   * @param mantLo the least significant 64 bits of the mantissa of the corresponding {@code Quadruple}
   * @param exponent the exponent of the corresponding {@code Quadruple}
   * @return a decimal {@code String} representation of the value of a {@code Quadruple} built of the given parts
   */
  public static String bdStr(boolean sign, long mantHi, long mantLo, int exponent) {
    if (exponent == EXP_INF)                           // infinity or NaN
      return ((mantHi | mantLo) != 0)? "NaN" : (sign)? "-Infinity" : "Infinity";
    return bd(sign, mantHi, mantLo, exponent).stripTrailingZeros().toString();
  } // public static String bdStr(boolean sign, long mantHi, long mantLo, int exponent) {

  /**
   * Builds a non-negative {@code BigDecimal} whose value is equal to the value
   * that a Quadruple would have if it were built of the given parts,
   * with an accuracy of 140 decimal places, and returns its {@code String} representation.
   * For {@code Infinity} and {@code NaN} returns their respective string notations.
   * @param mantHi the most significant 64 bits of the mantissa of the corresponding {@code Quadruple}
   * @param mantLo the least significant 64 bits of the mantissa of the corresponding {@code Quadruple}
   * @param exponent the exponent of the corresponding {@code Quadruple}
   * @return a decimal {@code String} representation of the value of a {@code Quadruple} built of the given parts
   */
  public static String bdStr(long mantHi, long mantLo, int exponent) {
    return bdStr(false, mantHi, mantLo, exponent);
  } // public static String bdStr(long mantHi, long mantLo, int exponent) {

  /**
   * Returns the string representation of this {@code BigDecimal}, using scientific notation if an exponent is needed.
   * Simply calls {@code BigDecimal.toString(), actually.
   * @param value the value to convert to {@code String}
   * @return a {@code String} representation of the given value
   * @see BigDecimal#toString()
   */
  public static String bdStr(BigDecimal value) { return value.toString(); }

  /**
   * Returns the string representation of this {@code Number}, using scientific notation if an exponent is needed.
   * Simply calls {@code BigDecimal.toString(), actually.
   * @param value the value to convert to {@code String}
   * @return a {@code String} representation of the given value
   * @see BigDecimal#toString()
   */
  public static String bdStr(Number value) {
    return bd(value).toString();
  } // public static String bdStr(Number value) {

  /**
   * Returns the string representation of this {@code Number}, using scientific notation with 50 digits after the point.
   * @param value the value to convert to {@code String}
   * @return a {@code String} representation of the given value
   */
  static String str(Number value) {
    return String.format("%.50e", bd(value));
  }

  /**
   * Returns the string representation of this {@code double} value.
   * <p>
   * The representation is exactly the one returned by the
   * {@code Double.toString} method of one argument.
   *
   * @param value the value to convert to {@code String}
   * @return a {@code String} representation of the given value
   * @see     java.lang.Double#toString(double)
   */
  static String str(double value) {
    return Double.toString(value);
  }

  /**
   * Returns the string representation of this {@code long} value.
   * <p>
   * The representation is exactly the one returned by the
   * {@code Long.toString} method of one argument.
   *
   * @param value the value to convert to {@code String}
   * @return a {@code String} representation of the given value
   * @see     java.lang.Long#toString(double)
   */
  static String str(long value) {
    return Long.toString(value);
  }

 /* ********************************************************************************************
  *** Other auxiliary stuff ********************************************************************
  **********************************************************************************************/

  /**
   * Calculates the square root of the given {@code BigDecimal}
   * value using the Babylonian algorithm, with the precision of {@code 1e-85}.
   * @param arg the argument of the square root
   * @return  the value of the square root
   * @throws ArithmeticException if the argument is negative
   */
  public static BigDecimal sqrt(final BigDecimal arg) {
    if (arg.signum() < 0)
      throw new ArithmeticException("Can't find square root of negative number " + String.format("%.43e", arg));
    final double d = arg.doubleValue();

    // First approximation
    BigDecimal root;
    final int halfOfExp = (arg.precision() - arg.scale() - 1)/2; // E/2
    if (Double.isInfinite(d)) {
      root = new BigDecimal("2e"+halfOfExp);      // 2*10^(E/2)
    } else if (d == 0.0) {
      root = new BigDecimal("6e"+halfOfExp);      // 6*10^(E/2)
    } else
      root = BigDecimal.valueOf(Math.sqrt(d));

    final BigDecimal eps = mult(arg, new BigDecimal("1e-85"));
    BigDecimal delta = sub(mult(root, root, MC_100_HALF_EVEN), arg, MC_100_HALF_EVEN);
    while (delta.abs().compareTo(eps) > 0) {
      root = mult(add(root, div(arg, root, MC_100_HALF_EVEN), MC_100_HALF_EVEN), 0.5, MC_100_HALF_EVEN);
      delta = sub(mult(root, root, MC_100_HALF_EVEN), arg, MC_100_HALF_EVEN);
    }
    return root;
  } // public static BigDecimal sqrt(final BigDecimal arg) {

  /**
   * Calculates {@code 2^exp} with the accuracy of 140 decimal digits.
   *
   * @param exp -- the power to raise 2 to
   * @return the value of two raised to the given power
   */
  public static BigDecimal powerOfTwo(int exp) {
    return powerOfTwo(exp, MC_140_HALF_UP);
  } // public static BigDecimal powerOfTwo(int power) {

  /**
   * Calculates {@code 2^exp} with the accuracy defined by the {@code mc} argument.
   *
   * @param exp the power to raise 2 to
   * @param mc {@code MathContext} that defines the precision
   * @return the value of two raised to the given power
   */
  public static BigDecimal powerOfTwo(long exp, MathContext mc) {
    final boolean exp2Negative = exp < 0;
    exp = Math.abs(exp);
    final int _1e8 = 100_000_000;             // We can't raise a BigDecimal to a power >= 1_000_000_000 directly

    final BigDecimal power = (Long.compareUnsigned(exp, _1e8) <= 0)?
      BD_TWO.pow((int)exp, mc) :
      TWO_RAISED_TO_1E8.pow((int)(exp / _1e8), mc)
                       .multiply(BD_TWO.pow((int)(exp % _1e8), mc), mc);
    return exp2Negative? BigDecimal.ONE.divide(power, mc) : power;
  } // public static BigDecimal powerOfTwo(long exp, MathContext mc) {

  /**
   * Opens a simplest 'logger' (actually, a simple text file with the given name) to write something to, while debugging.
   * Had been used for debugging. Currently not used, but may still be useful in the future.
   */
  public static void openLog(String filename) throws FileNotFoundException {
    logFile = new PrintStream(filename);
  }

  /** logFile.println();  */
  public static void log()            { logFile.println(); }

  /** logFile.println(o);  */
  public static void log(Object o)    { logFile.println(o); }

  /** logFile.print(o);   */
  public static void log_(Object o)   { logFile.print(o); }

  /** logFile.println(String.format(format, args));  */
  public static void log(String format, Object... args)   { logFile.println(String.format(format, args)); }

  /** logFile.print(String.format(format, args)); */
  public static void log_(String format, Object... args)  { logFile.print(String.format(format, args)); }

  /** Closes the 'logger' */
  public static void closeLog() {
    logFile.close();
    logFile = null;
  }

 /* ********************************************************************************************
  *** Private methods **************************************************************************
  **********************************************************************************************/

  /**
   * Checks if the given {@code Quadruple} value can be represented as {@code BifDecimal}.<br>
   * Throws {@code NumberFormatException} in case if the {@code qValue} is {@code NaN, Infinity}, or {@code -Infinity}
   * @param qValue the value to check
   */
  private static void checkNaNInfinity(Quadruple qValue) {
    if (qValue.exponent() == EXP_INF) {
      throw new NumberFormatException("Can't convert "
                  + (((qValue.mantHi() | qValue.mantLo()) != 0)?  "NaN" :
                      qValue.isNegative()?                         "NEGATIVE_INFINITY" :
                                                                    "POSITIVE_INFINITY")
                  + " to BigDecimal"); // " + hexStr(testedQ));
    }
  } // private static void checkNaNInfinity(Quadruple qValue) {

  /**
   * Given a {@code Quadruple value}, calculates the value of the mantissa
   * of the corresponding {@code BigDecimal} value, {@code mant = 1.0 + (mantHi() * 2^-64) + (mantLo() * 2^-128}
   * @param qValue a {@code Quadriple} value being converted to a {@code BigDecimal} value
   * @return the resulting mantissa, ranged from 1.0 to 1.99999999...
   */
  private static BigDecimal buildBDMantissa(Quadruple qValue) {
    BigDecimal mant = new BigDecimal(Long.toUnsignedString(qValue.mantHi()));  // Upper 64 bits
    mant = mant.add(new BigDecimal(Long.toUnsignedString(qValue.mantLo())).divide(POW_2_64));

    // Now it's the fractional part of the mantissa
    if (qValue.exponent() == 0) // It's subnormal
      mant = mant.divide(POW_2_63);                           // No Implied "1.", just div by 2^63
    else
      mant = BigDecimal.ONE.add(mant.divide(POW_2_64));   // + Implied "1.", and now we have the mantissa
    return mant;
  } // private static BigDecimal buildBDMantissa(Quadruple qValue) {

  /**
   * Checks that the decimal exponent of a value does not exceed the range valid for non-null Quadruple.
   * In case it exceeds, assigns to {@code result} 0, -0, Infinity, or -Infinity,
   * depending on the sign of {@code result} and the value of {@code exp10}, and returns {@code true},
   * otherwise returns {@code false}.
   * @param result
   * @param exp10
   * @return
   */
  private static boolean exceedsNormalRange(Quadruple result, int exp10) {
    if (exp10 < MIN_EXP10)                // < MIN_VALUE
      return true;                        // The sign is already assigned to result

    if (exp10 > MAX_EXP10) {              // > MAX_VALUE
      if (result.isNegative())
        result.assignNegativeInfinity();  // -Infinity
      else
        result.assignPositiveInfinity();  // Infinity
      return true;
    }
    return false;                         // The range is OK
  } // private static boolean exceedsNormalRange(Quadruple q, int exp10) {

  /**
   * Given a {@code BigDecimal} value and its decimal exponent, finds the most probable value of its binary exponent.
   * Due to the limited precision of {@code double} arithmetic, the result in very rare cases
   * may differ from the true value by +/-1, so the intermediate result of binary mantissa calculated based on this value,
   * may require an additional correction.
   * @param value a {@code BigDecimal} value
   * @param exp10 the decimal exponent of the {@code value}
   * @return the most probable value of the binary exponent of the {@code value}
   */
  private static long findBinaryExponent(BigDecimal value, int exp10) {
    final BigDecimal mant10 = value.divide(raise10toPower(exp10), MC_55_HALF_EVEN); // Decimal mantissa, normalized (1.0 .. 9.999...). High precision is not needed here
    return (long) Math.floor( exp10 * LOG2_10 + log2(mant10.doubleValue()) );   // Binary exponent
  }

  /**
   * Raises the {@code BigDecimal} value of {@code 10.0} to power {@code exp10}
   * @param exp10 the power to raise 10 to
   * @return a {@code BigDecimal} with the value of 10.0<sup>exp10</sup>
   */
  private static BigDecimal raise10toPower(int exp10) {
    return BigDecimal.ONE.scaleByPowerOfTen(exp10);
  } // private static BigDecimal raise10toPower(int exp10) {

  /**
   * Given a {@code BigDecimal} value and its binary exponent, finds its binary mantissa,
   * <nobr><code>mant2 = v / 2^exp2</code></nobr>.<p>
   * The value of {@code exp2} is always non-negative here, for the values less in magnitude than 1 the {@code negExp} flag is true;
   * in such cases multiplication performed instead of division, <nobr><code>mant2 = v * 2^exp2</code></nobr>.<p>
   * Used by {@code buildQuadruple()}
   * @param value the value whose {@code Quadruple} representation is being searched for
   * @param exp2 the binary exponent of the value
   * @param negExp a flag signifying that the value is less than 1 (i.e. the real binary exponent is negative)
   * @return the value of the binary mantissa {@code mant2}, such that {@code mant2 * 2^exp2 = value}
   */
  private static BigDecimal findMant2(BigDecimal value, long exp2, boolean negExp) {
    final BigDecimal pow2 = powerOfTwo(exp2, MC_120_HALF_EVEN); // power of 2. Without limiting MathContext it may be too long
    return (negExp)?  value.multiply(pow2, MC_120_HALF_EVEN) :  // binary mantissa
                      value.divide(pow2, MC_120_HALF_EVEN);
  } // private static BigDecimal findMant2(BigDecimal value, long exp2, boolean negExp) {

  /**
   * Finds the most significant 64 bits and the least significant 64 bits
   * of the fractional part of the given mantissa, the values of which are to be assigned
   * to the {@code mantHi} and {@code mantLo} fields of the {@code Quadruple} being built.
   * If the value of the remainder of the division {@code (mant - 1.0) / 2^128} is greater than 0.5,
   * rounds the found value up and may correct the exponent in case if rounding results in overflow of the fractional part.
   * Assigns the resulting values to the corresponding fields of the given {@code Quadruple} instance.
   * @param result the {@code Quadruple} instance whose fields are to set
   * @param mant the value of the mantissa, ranged from 1.0 to 1.999...
   * @param exp2 the binary exponent of the {@code Quadruple} that may get corrected
   * @param negExp a flag signifying that the module of the source value was less than 1.0 (i.e. it had negative exponent)
   * @return the value of {@code exp2}, perhaps corrected by +/-1.
   */
  private static long findMantValues(Quadruple result, BigDecimal mant, long exp2, boolean negExp) {
    BigDecimal fractPart = mant.subtract(BigDecimal.ONE).multiply(POW_2_64); // (1.xxxx - 1.0) << 64 -- higher 64 bits
    long mantHi = fractPart.longValue();

    fractPart = fractPart.subtract(new BigDecimal(Long.toUnsignedString(mantHi))).multiply(POW_2_64); // lower 64 bits
    long mantLo = fractPart.longValue();

    fractPart = fractPart.subtract(new BigDecimal(Long.toUnsignedString(mantLo)), MC_40_HALF_EVEN); // Fraction left after multiplying by 2^128

    // For subnormal values, negExp == true and exp2 >= Integer.MAX_VALUE
    if (  (!negExp || exp2 < Integer.MAX_VALUE)             // Not a subnormal
           || fractPart.compareTo(BigDecimal.ONE) >= 0  ) { // Or fractPart >= 1.0 (as a result of rounding of 0.9999999999...
      if (fractPart.compareTo(HALF_OF_ONE) >= 0)
        if (++mantLo == 0 && ++mantHi == 0)                 //  Rounding up
          exp2 += negExp? -1 : 1;                                                     // Overflow, adjust exponent
    }
    result.assign(0, mantHi, mantLo);
    return exp2;
  } // private static long findMantValues(Quadruple result, BigDecimal mant, long exp2, boolean negExp) {

  /**
   * Converts the given {@code Quadruple} value to the corresponding subnormal
   * value by shifting right the bits of its mantissa and setting the bit that
   * corresponds to its integer part (that is meant by the "implicit unity"
   * in the normal form of {@code Quadruple}),
   * in accordance with the value of the binary exponent.
   * The rounding of the shifted-out bits of mantissa may result in overflow of the fractional part
   * of the mantissa thus requiring the {@code exp2} be corrected.
   * @param result the {@code Quadruple} instance to convert
   * @param exp2 the difference between the actual exponent of the number and the exponent
   *    of {@code Quadruple.MIN_NORMAL}, negative for subnormal values
   * @return the value of the {@code exp2} parameter, perhaps corrected.
   */
  private static long makeSubnormal(Quadruple result, long exp2) {
    exp2 = -exp2;                               // just for convenience
    long mantLo = result.mantLo(), mantHi = result.mantHi();
    if (exp2 > 127) {                           // Effectively 0 or MIN_VALUE
      mantLo = mantHi = 0;
      if (exp2 == 128) mantLo++;                // MIN_VALUE
      result.assign(result.isNegative(), result.exponent(), mantHi, mantLo);
      return 0;                                 // >= 129 means 0
    }

    long shiftedOut = mantLo & 1;               // The highest of shifted out bits to evaluate carry
    mantLo = (mantLo >>> 1) | (mantHi << 63);
    mantHi = (mantHi >>> 1) | HIGH_BIT;         // move 1 bit right and set unity that was implied

    if (exp2 >= 64) {                           // the higher word move into the lower
      if (exp2 == 64)
        shiftedOut = mantLo >>> 63;             // former lowest bit of mantHi now is the highest bit of mantLo
      else
        shiftedOut = (mantHi >>> (exp2 - 65)) & 1; // one of the bits of the high word
      mantLo = mantHi >>> exp2 - 64;
      mantHi = 0;
    } else if (exp2 > 0) {                      // Shift both words
      shiftedOut = (mantLo >>> exp2 - 1) & 1;
      mantLo = (mantLo >>> exp2) | (mantHi << 64 - exp2);
      mantHi = mantHi >>> exp2;
    }

    if ((shiftedOut != 0)                       // The highest shifted out bit is 1 -- add it to the lower word
        && (mantLo += shiftedOut) == 0          // carry to the higher word
        && (++mantHi == 0))                     // carry beyond the higher word
      exp2 = 1;                                 // it becomes MIN_NORMAL
    else
      exp2 = 0;                                 // else mark that it's subnormal

    result.assign(result.isNegative(), result.exponent(), mantHi, mantLo);
    return exp2;
  } // private static long makeSubnormal(Quadruple result, long exp2) {

  /**
   * Calculates logarithm of x to base 2.
   * @param x the argument of the logarithm
   * @return the value of log<sub>2</sub>(x)
   */
  private static double log2(double x) {
    if (x == 0) return 0;
    return LOG2_E * Math.log(x);
  } // private static double log2(double x) {

  @SuppressWarnings("unused")
  private void dummyMethodToEnableThePreviousCommentBeFoldedInEclipse() {}

}
