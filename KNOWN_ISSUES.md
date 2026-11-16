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

**Tests:** to be added in `AssignTest` (not yet written).
