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
