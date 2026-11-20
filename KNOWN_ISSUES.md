# Known issues

Confirmed bugs found while writing the new tests. Each entry has an ID (`BUG-xx`)
that is referenced from `@Disabled("BUG-xx ...")` annotations of the tests that reproduce it.

Status: **open** — confirmed, not fixed; **fixed** — fixed, tests re-enabled.

---

## BUG-01 — `assign(double)` with ±`Double.MIN_VALUE` keeps stale mantissa

**Status:** open
**Found:** 2026-11-16
**Where:** `Quadruple.makeQuadOfSubnormDoubleAsLong(long)`, called from `assign(double)`

**Reproduction:**
```java
Quadruple q = new Quadruple(1.75);
q.assign(Double.MIN_VALUE);
q.doubleValue();   // 9.9E-324, expected 4.9E-324
```

**Cause:** for `±Double.MIN_VALUE` the number of leading zeros is 63, and the branch
`if (numOfZeros < 63) mantHi = ...` leaves `mantHi` unassigned, so it keeps the value
of the previous contents of the instance. Constructors are not affected,
since a new instance has `mantHi == 0`.

**Fix (proposed):** assign `mantHi = 0` in the `numOfZeros == 63` case.

**Tests:** `AssignTest.shouldAssignDoubleMinValueToDirtyInstance` (disabled).

---

## BUG-02 — javadoc errors in low-level constructors and `assign` methods (documentation only)

**Status:** open
**Found:** 2026-11-17
**Where:** javadoc of `Quadruple`; the code behaves consistently, only the descriptions are wrong.

1. `Quadruple(boolean negative, int exponent, long mantHi, long mantLo)` and
   `Quadruple(int exponent, long mantHi, long mantLo)`: `@param exponent` says
   "the binary exponent (unbiased)", but the value is stored as is, i.e. it is the **biased** exponent
   (like in `assign(boolean, int, long, long)`).
2. `assign(boolean, int, long, long)` and `assign(int, long, long)`: the description says
   "its value equal to `EXPONENT_OF_ONE` (`0xFFFF_FFFEL`) corresponds to 1.0",
   but `EXPONENT_OF_ONE == 0x7FFF_FFFF` (`0xFFFF_FFFE` is `EXPONENT_OF_MAX_VALUE`).
   The `@param exponent` of the same methods gives the correct value `0x7FFF_FFFF`.
3. `assign(long[])`: `value[2]` is described as "The most significant 64 bits of the fractional part
   of the mantissa", should be "the least significant" (as in `toLongWords()`).

**Fix (proposed):** correct the javadoc.

**Tests:** not applicable (the behavior is tested by `CreationTest.shouldCreateFromParts`,
`AssignTest.shouldAssignPartsToDirtyInstance`, `AssignTest.shouldAssignLongArrayToDirtyInstance`).

---

## BUG-03 — `doubleValue()` rounds ties half-up in the subnormal range of `double`

**Status:** open
**Found:** 2026-11-19
**Where:** `Quadruple.doubleValue()`, the branch for subnormal results (`expD < -(EXP_0D - 1)`)

**Reproduction:**
```java
Quadruple min = new Quadruple(Double.MIN_VALUE);
Quadruple.divide(min, 2.0).doubleValue();       // 4.9E-324 (MIN_VALUE), expected 0.0
Quadruple.multiply(min, 2.5).doubleValue();     // 1.5E-323 (3 * MIN_VALUE), expected 9.9E-324 (2 * MIN_VALUE)
```

**Cause:** for normal results `doubleValue()` rounds half-even, as documented
("like `BigDecimal.doubleValue()`"), but the subnormal branch adds a half of the LSB and shifts,
which rounds exact ties up (away from zero). Only exact ties are affected.

**Fix (proposed):** round half-even in the subnormal branch too (round a tie up only if the result is odd,
taking into account all the shifted-out bits, including `mantLo`).

**Tests:** `ConversionTest.shouldConvertSubnormalDoubleTiesToEven` (disabled; 1006 ties, 498 of them fail —
the others are rounded up to even anyway).
The ties are excluded from the data of `ConversionTest.shouldConvertToDouble`.

---

## BUG-04 — `floatValue()` may be rounded incorrectly (double rounding)

**Status:** open
**Found:** 2026-11-19
**Where:** `Quadruple.floatValue()`, implemented as `(float)doubleValue()`

**Reproduction:**
```java
Quadruple q = new Quadruple(1.0).add(Math.scalb(1.0, -24)).add(Math.scalb(1.0, -60));  // 1 + 2^-24 + 2^-60
q.floatValue();    // 1.0, expected 1.0000001 (1 + 2^-23): the value is above the midpoint 1 + 2^-24
```

**Cause:** the value is rounded twice: first to double, then to float. If the value is slightly off the midpoint
between two adjacent floats (closer to it than a half ulp of double, i.e. 2^-30 ulp of float),
the first rounding gives exactly the midpoint, and the second one (half-even) may go the wrong way.

**Fix (proposed):** round the mantissa directly to 24 bits (half-even, taking into account all the bits),
the same way as `doubleValue()` does it for 53 bits; or round to double with "round to odd" (sticky) before
the conversion to float.

**Tests:** `ConversionTest.shouldConvertToFloatWithoutDoubleRounding` (disabled; 5780 cases, all fail).
These values are excluded from the data of `ConversionTest.shouldConvertToFloat`.

---

## BUG-05 — `toIeee754Longs()`/`toIeee754Bytes()` give 0 for values in `[MIN_VALUE/2, MIN_VALUE)` of IEEE-754

**Status:** open
**Found:** 2026-11-19
**Where:** `Quadruple.toIeee754Longs()` (and `toIeee754Bytes()`, which uses it):
`unbiasedExponent < IEEE754_MIN_EXPONENT` (-16494) returns zero

**Reproduction:**
```java
// 2^-16495 = a half of the minimum IEEE-754 subnormal value (a tie), and 1.5 * 2^-16495
Quadruple tie   = new Quadruple(false, Quadruple.EXPONENT_BIAS - 16495, 0, 0);
Quadruple above = new Quadruple(false, Quadruple.EXPONENT_BIAS - 16495, 0x8000_0000_0000_0000L, 0);
tie.toIeee754Longs();     // {0, 0}, expected {0, 1} (the minimum subnormal; half-up)
above.toIeee754Longs();   // {0, 0}, expected {0, 1} (it is closer to the minimum subnormal than to 0)
```

**Cause:** the values with the exponent -16495 (i.e. in the range `[MIN_VALUE/2, MIN_VALUE)`) are converted to zero
without rounding. The javadoc describes this behavior ("the values whose magnitude is less than ...
(minimum positive value of IEEE-754 Quadruple) are converted to 0"), but it contradicts the beginning
of the same javadoc ("the IEEE-754 quadruple precision number nearest to the value", "the rounding mode is half-up").

**Fix (proposed):** return zero only for exponents less than -16495, let the subnormal branch round the values
with the exponent -16495 (check that it handles the shift by 113 bits); fix the javadoc.

**Tests:** `ConversionTest.shouldRoundValuesBelowIeee754MinValueToMinValue` (disabled; 8 cases, all fail).
These values are excluded from the data of `ConversionTest.shouldConvertToIeee754Longs/Bytes`.

---

## BUG-06 — `equals()` returns `false` for two different instances of NaN

**Status:** open
**Found:** 2026-11-19
**Where:** `Quadruple.equals(Object)`: `if (isNaN() && other.isNaN()) return false;`

**Reproduction:**
```java
Quadruple.nan().equals(Quadruple.nan());   // false, expected true (like Double.valueOf(Double.NaN).equals(Double.NaN))
```

**Cause:** the code treats all NaNs as different (only an instance is equal to itself), while
`compareTo()` considers all NaNs equal, `hashCode()` returns the same value for all NaNs
("Since NaNs are considered equal"), and `Double.equals()` considers all NaNs equal
(it compares `doubleToLongBits()`, which collapses all NaNs to the canonical one).
As a result, `equals()` is inconsistent with `compareTo()`, and e.g. a `HashSet` may contain any number of NaNs.

**Fix (proposed):** return `true` if both are NaN (regardless of the sign and the mantissa bits).

**Tests:** `ComparisonTest.shouldConsiderAllNaNsEqual` (disabled; 25 pairs of NaNs, including NaNs
with different bit patterns, all fail). These pairs are excluded from the data of `ComparisonTest.shouldTestEquality`.

---

## BUG-07 — `min()` and `assignMin()` ignore a NaN operand

**Status:** open
**Found:** 2026-11-19
**Where:** `Quadruple.min(Quadruple, Quadruple)`, `Quadruple.assignMin(Quadruple)`

**Reproduction:**
```java
Quadruple.min(Quadruple.nan(), Quadruple.one());    // 1.0, expected NaN (like Math.min(Double.NaN, 1.0))
Quadruple.min(Quadruple.one(), Quadruple.nan());    // 1.0, expected NaN
Quadruple.one().assignMin(Quadruple.nan());         // 1.0, expected NaN
Quadruple.nan().assignMin(Quadruple.one());         // 1.0, expected NaN
```

**Cause:** the methods rely on `compareTo()`, which considers NaN greater than any other value
(like `Double.compare()`), so the other operand is always the lesser one. `Math.min()` returns NaN
if any of the operands is NaN. `max()` and `assignMax()` give NaN anyway, since NaN is the greatest.

**Fix (proposed):** return (assign) NaN if any of the operands is NaN; mention it in the javadoc.

**Tests:** `ComparisonTest.shouldGiveNaNAsMinIfAnyOperandIsNaN` (disabled; 450 pairs with exactly one NaN,
for both `min()` and `assignMin()`, all fail). These pairs are excluded from the data of
`ComparisonTest.shouldFindMin` and `ComparisonTest.shouldAssignMin`.

---

## BUG-08 — `signum()` should behave like `Math.signum()` (API change for the next release)

**Status:** open (planned API change)
**Found:** 2026-11-19
**Where:** `Quadruple.signum()`

**Reproduction:**
```java
Quadruple.nan().signum();                        // 1, expected NaN
new Quadruple(true, 0, 0, 0).signum();           // -1 (as documented), expected -0.0
```

**Cause:** `signum()` returns `int`, so it can't return NaN for NaN and -0.0 for -0, like `Math.signum(double)`.
Currently `signum(NaN) == 1` (not documented), `signum(-0) == -1` (documented).

**Fix (planned):** change the return type to `double`, return NaN for NaN, -0.0 for -0, 0.0 for +0,
-1.0 and 1.0 for other values (including infinities); update the javadoc. This is an incompatible API change,
it goes to the next release.

**Tests:** `ComparisonTest.shouldReturnSignumOfNaNAndNegativeZeroLikeMathSignum` (disabled; -0 and 4 NaNs,
all fail). The test code compiles with both `int` and `double` return types.
These values are excluded from the data of `ComparisonTest.shouldReturnSignum`.

---

## BUG-09 — javadoc of comparison methods does not describe special values (documentation only)

**Status:** open
**Found:** 2026-11-19
**Where:** javadoc of `Quadruple`; the code behaves like `Double` (except BUG-06, BUG-07, BUG-08).

1. `compareTo(Quadruple)`, `compareTo(long)`, `compareTo(double)`, `compare()`: should state that the order is
   that of `Double.compare()`: NaN is greater than any other value (including +Infinity), all NaNs are equal
   to each other, -0 is less than +0.
2. `compareMagnitudeTo()`, `compareMagnitudes()`: should state that NaN is greater in magnitude than any
   other value, all NaNs are equal, Infinity is greater than any finite value, -0 and +0 are equal in magnitude.
   `compareMagnitudeTo()` promises -1, 0, 1, while `compareMagnitudes()` promises
   "a negative integer, zero, or a positive integer" — make them consistent (the code returns -1, 0, 1).
3. `equals()`: should state that, like `Double.equals()`, all NaNs are equal to each other (after BUG-06 is fixed),
   and -0 is not equal to +0.
4. `max()`, `min()`, `assignMax()`, `assignMin()`: should state that the result is NaN if any of the operands is NaN
   (after BUG-07 is fixed), and that -0 is considered less than +0. In `max()` and `min()`, the description of `q2`
   says "first operand to compare".

**Fix (proposed):** correct the javadoc.

**Tests:** not applicable (the behavior is tested by `ComparisonTest`).

---

## BUG-10 — `sqrt(-0)` returns NaN instead of -0

**Status:** open
**Found:** 2026-11-20
**Where:** `Quadruple.sqrt()`: `if (negative) return assignNaN();` precedes the check for zero

**Reproduction:**
```java
new Quadruple(true, 0, 0, 0).sqrt();     // NaN, expected -0 (like Math.sqrt(-0.0) == -0.0, IEEE-754)
Quadruple.sqrt(new Quadruple(-0.0));     // NaN, expected -0
```

**Cause:** the sign is checked before the special values, so -0 is treated as a negative number.

**Fix (proposed):** check `isZero()` (and NaN) before the sign: `sqrt(±0) = ±0`, `sqrt(NaN) = NaN`,
`sqrt(x < 0) = NaN`; mention it in the javadoc.

**Tests:** `SpecialValuesTest.shouldComputeSqrtOfNegativeZeroLikeMathSqrt` (disabled; the only case, fails).
-0 is excluded from the data of `SpecialValuesTest.shouldComputeSqrtOfSpecialValuesLikeMathSqrt`.

---

## BUG-11 — subtraction may round "a half of the ulp plus a little" as a tie

**Status:** open
**Found:** 2026-11-20
**Where:** `Quadruple.subtractDifferentExp()` and `Quadruple.subtractSubnormalFromNormal()`
(called from `subtract()`, and from `add()` for operands of different signs)

**Reproduction:**
```java
// b = 2^-100 + 2^-129 + 2^-200: its bits below the LSB of 1.5 (2^-128) are a half of the ulp plus 2^-200
Quadruple b = new Quadruple(false, Quadruple.EXPONENT_OF_ONE - 100, 0x0000_0008_0000_0000L, 0x0000_0000_1000_0000L);
Quadruple.subtract(new Quadruple(1.5), b);
// mantLo = 0xFFFF_FFFF_F000_0000, expected 0xFFFF_FFFF_EFFF_FFFF:
// the exact difference is below the midpoint, it must be rounded down (in magnitude)
```

**Cause:** the mantissa of the subtrahend is shifted right by the difference of the exponents, and only
the highest 64 of the shifted-out bits are kept (`shiftedOutBits`). The borrow (rounding down) is decided by
`shiftedOutBits > HIGH_BIT`, i.e. "more than a half of the ulp". When the shift exceeds 64, the lower shifted-out bits
are lost, so that a half of the ulp plus a value less than 2^-64 ulp is treated as an exact half (a tie),
and the result is rounded up instead of down. In addition it is not a problem, since rounding half-up
needs only the highest shifted-out bit. If the result goes down to the lower binade, the result is correct.

**Fix (proposed):** take into account a sticky bit: whether any of the bits shifted out beyond the 64 kept bits is 1
(e.g. `shiftedOutBits == HIGH_BIT && lowerBitsNonZero` means "more than a half").

**Tests:** `AddSubTest.shouldRoundSubtractionNearTiesCorrectly` (disabled; 62 cases, all fail).
The criterion (shift from 65 to 128 bits, the shifted-out bits are `100..00` in the first 64 bits and not all zeros
in the rest, the result in the binade of the greater operand) is in `AddSubData.isSubtractionNearTie()`;
these cases are excluded from the data of `AddSubTest.shouldBeCorrectlyRounded`.

---

## BUG-12 — adding a subnormal value to a value with the exponent >= `0x8000_0001` gives a wrong result

**Status:** open
**Found:** 2026-11-20
**Where:** `Quadruple.addNormalAndSubnormal()`: `final int shift = exponent - 1;`

**Reproduction:**
```java
Quadruple.add(new Quadruple(4), Quadruple.minValue());   // 4 + 2^-128 ulp (mantLo = 1), expected 4
```
(the exponent of 4 is `0x8000_0001`). In other cases the result may differ from the correct one in any bits.

**Cause:** for exponents `0x8000_0001` and greater, `shift` is negative as `int`, so the check
`shift + lz > 128` ("the subnormal is too small to affect the result") does not work, and the mantissa
of the subnormal value is shifted by a negative distance (that Java takes modulo 64).
`subtractSubnormalFromNormal()` checks `(shift & 0xFFFF_FF00) != 0` and is not affected.

**Fix (proposed):** compare the shift as unsigned (`Integer.compareUnsigned`) or check `(shift & 0xFFFF_FF00) != 0`,
like in `subtractSubnormalFromNormal()`.

**Tests:** `AddSubTest.shouldAddSubnormalsToHugeValues` (disabled; 182 cases, 158 of them fail;
the others are correct by coincidence: `MIN_VALUE` added to values with the exponent `0xFFFF_FFFE`,
where the shift by -2 gives zero). These cases are excluded from the data of `AddSubTest.shouldBeCorrectlyRounded`.

---

## BUG-13 — subtracting a subnormal value from a value with the exponent 130 may give a wrong result

**Status:** open
**Found:** 2026-11-20
**Where:** `Quadruple.subtractSubnormalFromNormal()`: the check `shift + lz > 129` lets `shift == 129`
pass when the highest bit of the subnormal mantissa is set (`lz == 0`), and `shiftMantissaRight(129)`
is not designed for shifts greater than 128.

**Reproduction:**
```java
Quadruple a = new Quadruple(false, 130, 0, 0);                         // 2^-2147483517
Quadruple b = new Quadruple(false, 0, 0x8000_0000_0000_0000L, 0);      // a half of MIN_NORMAL, = a * 2^-130
Quadruple.subtract(a, b);
// exp = 0x81, mantHi = 0xFFFF_FFFF_FFFF_FFFF, mantLo = 0x8000_0000_0000_0000 (not even a neighbor of the exact value);
// expected a (exp = 0x82, mantissa = 0): a - a * 2^-130 is the tie between a and the next lower value
// (a - a * 2^-129), rounded half-up
```

**Cause:** see above; the subtraction of normal values handles the shifts of 129 and 130 bits by special methods
(`subtract_1e_129()`, `subtract_1e_130()`), the subtraction of a subnormal value has no such special cases.

**Fix (proposed):** handle `shift == 129` separately (like `subtract_1e_129()`), or extend `shiftMantissaRight()`.

**Tests:** `AddSubTest.shouldSubtractSubnormalsWithShift129` (disabled; 48 cases, all fail).
These cases are excluded from the data of `AddSubTest.shouldBeCorrectlyRounded`.

---

## BUG-14 — subnormal products are rounded twice

**Status:** open
**Found:** 2026-11-20
**Where:** `Quadruple.multUnsigned()`: `roundBuffer()`, then `normalizeSubnormal()` / `makeSubnormal()`

**Reproduction:**
```java
Quadruple a = new Quadruple(false, 0x4000_00E9, 0xCBEC_F7B0_0000_0000L, 0);
Quadruple b = new Quadruple(false, 0x3FFF_FEF6, 0x0464_3416_BA2D_68B1L, 0xD5A3_9A42_6800_0000L);
Quadruple.multiply(a, b);
// exp = 0, mantHi = 0x0000_0000_E9E8_58DA, mantLo = 0x3BF4_64EA_969C_6BE6,
// expected mantLo = 0x3BF4_64EA_969C_6BE5: the exact product is below the midpoint by 5.8e-11 ulp
```

**Cause:** the product is first rounded half-up to 128 fractional bits, as for a normal result (`roundBuffer()`),
and then, if the result is subnormal, it is shifted right and rounded half-up again (`makeSubnormal()`).
If the first rounding turns a tail like `0111..11|1..` into `1000..00`, the second rounding sees a tie and rounds up,
though the exact value is below the midpoint. To compensate the first rounding, `normalizeSubnormal()` clears
the LSB of the rounded value if it was rounded up, which in turn sometimes makes the result rounded down,
though the exact value is above the midpoint (2 of the cases). Normal results are not affected.

**Fix (proposed):** for subnormal results, round only once: shift the full (unrounded) product right
and round it at the position of the subnormal LSB, taking into account all the shifted-out bits.

**Tests:** `MultiplyTest.shouldRoundSubnormalProductsOnce` (disabled; 370 cases, all fail).
The criterion (`MultiplyData.isSubnormalDoubleRoundingCase()`) emulates the rounding of the library
and selects exactly the failing cases; they are excluded from the data of `MultiplyTest.shouldBeCorrectlyRounded`.

## BUG-15 — square roots slightly above the midpoint may be rounded down

**Status:** open
**Found:** 2026-11-20
**Where:** `Quadruple.sqrt()`: `sqrtMant()` (and `multBySqrt2()` for odd exponents), rounding by the highest bit of `thirdWord`

**Reproduction:**
```java
Quadruple q = new Quadruple(false, 0x8000_095C, 0x4254_054A_B753_15E2L, 0x6C31_4590_EFB6_D3BFL);
Quadruple.sqrt(q);
// exp = 0x8000_04AD, mantHi = 0x963D_C9AB_69F5_9CDD, mantLo = 0x37B1_B4B0_EF34_DE3B,
// expected mantLo = 0x37B1_B4B0_EF34_DE3C: the exact root is above the midpoint by 3.3e-28 ulp
```

**Cause:** the root is computed with 64 extra bits (`thirdWord`) and rounded half-up by the highest of them,
but only about 31 of the extra bits are correct, and the computed root is slightly less than the exact one.
So if the exact root is above the midpoint by less than ~2^-31.5 ulp, the result may be rounded down.
The error is always in this direction; roots farther from the midpoint are always rounded correctly.
`Math.sqrt()` is correctly rounded (IEEE-754), and so is `Quadruple.divide()`.

**Fix (proposed):** if the computed root is close to the midpoint (the extra bits are within ~2^-30 ulp of `0x8000...`),
decide the rounding exactly: compare the square of the midpoint with the argument (one 256-bit multiplication).

**Tests:** `SqrtTest.shouldRoundRootsNearMidpointCorrectly` (disabled; 4234 cases closer to the midpoint
than 2^-31 ulp, 1635 of them fail). There's no exact criterion of failing (it depends on the internal error
of `sqrtMant()`), so all the values closer to the midpoint than 2^-31 ulp (`SqrtData.isNearMidpoint()`)
are excluded from the data of `SqrtTest.shouldBeCorrectlyRounded`, which requires correct rounding (ε = 0).
