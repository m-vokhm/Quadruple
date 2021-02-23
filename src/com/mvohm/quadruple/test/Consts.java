package com.mvohm.quadruple.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import com.mvohm.quadruple.Quadruple;

import static java.math.RoundingMode.*;

/**
 * Constants used by various other classes in the {@code com.mvohm.quadruple.test} package
 *
 * @author M.Vokhmentev
 *
 */
public class Consts {

  // started JavaDoc 21.02.19 10:43:13
  /** The bits of the mantissa of a {@code double}'s bits represented as a {@code long} */
  public static final long DOUBLE_MANT_MASK   = 0x000f_ffff_ffff_ffffL;

  /** The first bit */
  /** The bits of the exponent of a {@code double}'s bits represented as a {@code long} */
  public static final long DOUBLE_EXP_MASK    = 0x7ff0_0000_0000_0000L;
  /** The sign bit of the exponent of a {@code double}'s bits represented as a {@code long} */
  public static final long DOUBLE_SIGN_MASK   = 0x8000_0000_0000_0000L;

  /** The exponent of a {@code double} values falling within the range of 1.0d ... 1.999d (unbiased exponent = 0) */
  public static final int EXP_0D              = 0x0000_03FF;

  /** = log<sub>2</sub>(10) = 3.3219280948873626 */
  public static final double LOG2_10          = Math.log(10) / Math.log(2);
  /** = log<sub>2</sub>(e) = 1.4426950408889634 */
  public static final double LOG2_E           = 1/Math.log(2.0);

  /** A mask for the least significant 32 bits of a {@code long}, 0x0000_0000_FFFF_FFFFL; */
  public static final long LOWER_32_BITS      = 0x0000_0000_FFFF_FFFFL;
  /** A mask for the most significant 32 bits of a {@code long}, 0xFFFF_FFFF_0000_0000L */
  public static final long HIGHER_32_BITS     = 0xFFFF_FFFF_0000_0000L;
  /** A mask for the most significant 32 bits of a {@code long}, 0xFFFF_FFFF_0000_0000L */
  public static final long UPPER_32_BITS      = HIGHER_32_BITS;
  /** A mask for the most significant bit of a {@code long}, 0x8000_0000_0000_0000L */
  public static final long HIGH_BIT           = 0x8000_0000_0000_0000L;

  /**
   * A mapping between valid string representations of {@code Quadruple} constants and their respective values
   */
  @SuppressWarnings("serial")
  public static final Map<String, Quadruple> QUADRUPLE_CONSTS = new HashMap<String, Quadruple>() {{
    put("quadruple.min_value",          Quadruple.minValue());
    put("min_value",                    Quadruple.minValue());
    put("quadruple.max_value",          Quadruple.maxValue());
    put("max_value",                    Quadruple.maxValue());
    put("quadruple.min_normal",         Quadruple.minNormal());
    put("min_normal",                   Quadruple.minNormal());
    put("quadruple.nan",                Quadruple.nan());
    put("nan",                          Quadruple.nan());
    put("quadruple.negative_infinity",  Quadruple.negativeInfinity());
    put("negative_infinity",            Quadruple.negativeInfinity());
    put("-infinity",                    Quadruple.negativeInfinity());
    put("quadruple.positive_infinity",  Quadruple.positiveInfinity());
    put("positive_infinity",            Quadruple.positiveInfinity());
    put("infinity",                     Quadruple.positiveInfinity());
    put("+infinity",                    Quadruple.positiveInfinity());
  }};


  /** = new MathContext(40, HALF_EVEN) */
  public static final MathContext MC_40_HALF_EVEN   = new MathContext(40, HALF_EVEN);
  /** = new MathContext(55, HALF_EVEN) */
  public static final MathContext MC_55_HALF_EVEN   = new MathContext(55, HALF_EVEN);
  /** = new MathContext(80, HALF_EVEN) */
  public static final MathContext MC_80_HALF_EVEN   = new MathContext(80, HALF_EVEN);
  /** = new MathContext(100, HALF_EVEN) */
  public static final MathContext MC_100_HALF_EVEN  = new MathContext(100, HALF_EVEN);
  /** = new MathContext(120, HALF_EVEN) */
  public static final MathContext MC_120_HALF_EVEN  = new MathContext(120, HALF_EVEN);


  /** = new MathContext(140, HALF_UP) */
  public static final MathContext MC_140_HALF_UP    = new MathContext(140, HALF_UP);
  /** = new MathContext(140, CEILING) */
  public static final MathContext MC_140_CEILING    = new MathContext(140, CEILING);
  /** = new MathContext(140, FLOOR) */
  public static final MathContext MC_140_FLOOR      = new MathContext(140, FLOOR);

  /** The value of the {@code Quadruple}'s exponent field for value = {@code 1.0e0} */
  public static final int EXP_0Q              = 0x7FFF_FFFF;
  /** The value of the {@code Quadruple}'s exponent field for Infinity */
  public static final int EXP_INF             = 0xFFFF_FFFF;
  /** The value of the {@code Quadruple}'s exponent field for {@code Quadruple.MAX_VALUE}*/
  public static final int EXP_MAX             = 0xFFFF_FFFE;

  /** Maximum unbiased value of the decimal exponent, corresponds to biased binary exponent value EXP_MAX */
  public static final int MAX_EXP10           = 646456993;
  /** Minimum unbiased value of the decimal exponent, corresponds to Quadruple.MIN_VALUE */
  public static final int MIN_EXP10           = -646457032;      // corresponds

  /** {@code BigDecimal} value of 1.0 */
  public static final BigDecimal BD_ONE       = BigDecimal.ONE;
  /** {@code BigDecimal} value of 2.0 */
  public static final BigDecimal BD_2         = new BigDecimal("2");
  /** {@code BigDecimal} value of 2.0 */
  public static final BigDecimal BD_TWO       = new BigDecimal("2");

  /** {@code BigDecimal} value of 2^64 = 18446744073709551616 */
  public static final BigDecimal POW_2_64     = new BigDecimal("18446744073709551616"); // 2^64
  /** {@code BigDecimal} value of 2^63 = 9223372036854775808 */
  public static final BigDecimal POW_2_63     = new BigDecimal( "9223372036854775808"); // 2^63

  /** {@code BigDecimal} value of 2^64 = 18446744073709551616 */
  public static final BigDecimal BD_2$64      = POW_2_64;
  /** {@code BigDecimal} value of 2^-32 = 0.00000000023283064365386962890625*/
  public static final BigDecimal BD_2$_32     = new BigDecimal("0.00000000023283064365386962890625");
  /** {@code BigDecimal} value of 2^-64  = 0.0000000000000000000542101086242752217003726400434970855712890625*/
  public static final BigDecimal BD_2$_64     = new BigDecimal("0.0000000000000000000542101086242752217003726400434970855712890625");
  /** {@code BigDecimal} value of 2^-128 */
  public static final BigDecimal BD_2$_128    = BD_2$_64.multiply(BD_2$_64);
  /** {@code BigDecimal} value of 2^-129 */
  public static final BigDecimal BD_2$_129    = BD_2$_128.divide(BD_2);

  /** {@code BigDecimal} value of 2^100_000_000 */
  public static final BigDecimal TWO_RAISED_TO_1E8 = // 2^100_000_000 ==
      BD_TWO.pow(100_000_000, new MathContext(140, HALF_EVEN));

  /** {@code BigDecimal} value of 0.5 */
  public static final BigDecimal HALF_OF_ONE = new BigDecimal("0.5");

  /** {@code BigDecimal} value of Quadruple.MIN_VALUE, 2^(-2^31 - 126) =
      6.67282948260747430814835377499134611597699952289599614231191219193080765017673213896448630962593280732542984344224002873E-646457032 */
   public static final BigDecimal MIN_VALUE =
      new BigDecimal(
     "6.67282948260747430814835377499134611597699952289599614231191219193080765017673213896448630962593280732542984344224002873E-646457032");
  //  6.6728294826074743081483537749913461159769995228959961423119121919308076501767321389644863096259328073254298434422400287333891324734150384986969256569475770315096987333801855863367196321905178120651375e-646457032
  //   1___5___10____5___20____5___30____5___40____5___50____5___60____5___70____5___80____5___90____5__100____5___10____5___20____5___30____5___40____5___50____5___60____5___70____5___80____5___90____5__200____5___10____5___20____5___30____5___40____5___50____5___60____5___70____5___80____5___90____5__200


  /** {@code BigDecimal} value of Quadruple.MIN_NORMAL,  2^(-2^31 + 2) =
      2.270646210401492537526567265179587581247477299091065574630920136143484160472225689321414103921299574064476052240377955565e-646456993 */
  public static final BigDecimal MIN_NORMAL    = new BigDecimal(
     "2.27064621040149253752656726517958758124747729909106557463092013614348416047222568932141410392129957406447605224037795556e-646456993");
  //  2.2706462104014925375265672651795875812474772990910655746309201361434841604722256893214141039212995740644760522403779555631474179611977248047528763819531062771213622792665125540340375601723011976709225e-646456993   //   1___5___10____5___20____5___30____5___40____5___50____5___60____5___70____5___80____5___90____5__100____5___10____5___20____5___30____5___40____5___50____5___60____5___70____5___80____5___90____5__200____5___10____5___20____5___30____5___40____5___50____5___60____5___70____5___80____5___90____5__200

  /** {@code BigDecimal} value of Quadruple.MAX_VALUE,  (2 - 2^-128) * 2^(2^31 - 1) =
      1.7616130516839633532074931497918402856645231004498981593813968270782618644566862951132189864850806619896480140030422219564500583744987354528641318295076178190794408045746437523397282566199245466796392e+646456993
   */
  static final BigDecimal MAX_VALUE = new BigDecimal(
     "1.76161305168396335320749314979184028566452310044989815938139682707826186445668629511321898648508066198964801400304222196e+646456993");

} // public class Consts

