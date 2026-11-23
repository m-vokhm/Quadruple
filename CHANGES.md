# Changes

The numbers `BUG-xx` refer to [KNOWN_ISSUES.md](KNOWN_ISSUES.md), which describes each bug in detail:
how to reproduce it, its cause, the fix, and the tests that cover it.

## 1.2.1 (2026-11)

A bug-fix release: no changes in the API or in the syntax of strings.
All arithmetic operations and the square root are now correctly rounded,
and the conversions to `double`, `float` and IEEE-754 quadruple are correctly rounded
in the whole range, including subnormal results.

### Arithmetic

- Subtraction (and addition of values of different signs) could round a result that was a half of the ulp
  plus a little away from a representable value as a tie, if the exponents of the operands differed
  by more than 64 (BUG-11).
- Subtraction whose result was just below a power of 2 could be rounded up to the power of 2 (BUG-27).
- Adding a subnormal value to a value with a biased exponent of `0x8000_0001` or greater gave a wrong result (BUG-12).
- Subtracting a subnormal value with the highest bit of the mantissa set from a value with the exponent 130
  gave a wrong result (BUG-13).
- Subnormal products were rounded twice and could differ from the correctly rounded value by one ulp (BUG-14).
- `sqrt()`: roots slightly above the midpoint between two adjacent values (by less than ~2^-31 ulp)
  could be rounded down; now the rounding near the midpoint is decided exactly (BUG-15).
- `sqrt(-0)` returned NaN instead of -0, unlike `Math.sqrt()` (BUG-10).

### Conversions

- `doubleValue()` rounded exact ties half-up instead of half-even in the subnormal range of `double` (BUG-03).
- `floatValue()` rounded the value twice, via `double`, and could give a wrong result for values
  very close to the midpoint between two adjacent floats; now it rounds directly to `float` (BUG-04).
- `toIeee754Longs()` and `toIeee754Bytes()` gave 0 for values not less than a half of the minimum
  IEEE-754 subnormal value; now they give the minimum subnormal value (BUG-05).
- `assign(double)` with `±Double.MIN_VALUE` kept a part of the previous mantissa of the instance (BUG-01).
- `Quadruple(BigDecimal)` and `assign(BigDecimal)` threw `ArithmeticException` for values not less than
  `10^(2^31)`; now they give Infinity (BUG-25).

### Parsing of strings

- An exponent of more than 18 digits combined with a fractional or a multi-digit mantissa (e.g. `"0.01e-99999999999999999999"`)
  overflowed, so that huge values gave 0 and tiny ones gave Infinity (BUG-16).
- Values from `2^(2^31 + 1)` (~3.52e646456993) up to `1e646456994` gave wrong finite values instead of Infinity (BUG-20).

### Comparisons and other operations

- `equals()` returned `false` for two different instances of NaN; now all NaNs are equal,
  like with `Double.equals()`, which is consistent with `compareTo()` and `hashCode()` (BUG-06).
- `min()` and `assignMin()` ignored a NaN operand; now the result is NaN, like with `Math.min()` (BUG-07).
- `nextRandom()` filled the free low bits of the mantissa wrongly if its 64 high bits were zero
  (probability ~2^-64) (BUG-24).

### Documentation

- The class description has a new section "Accuracy", referred to from the arithmetic operations
  and `sqrt()` (BUG-26).
- Fixed and completed: the description of the rounding of `toString()` and `bigDecimalValue()` (BUG-17),
  the order of comparisons and the handling of special values by the comparison methods, `equals()`,
  `min()`, `max()` (BUG-09), the separator `_` in strings (BUG-21), the biased exponent of the constructors
  and `assign()` methods from parts, and the words of the array in `assign(long[])` (BUG-02, BUG-23),
  the conditional thread safety of the class.

### Tests

- The old test utility (`QuadTest`, `QuadJUnitTest` and the rest of the `com.mvohm.quadruple.test` package)
  is replaced by a new set of JUnit 5 tests, which check the results against exact values computed with
  `BigInteger` and require correct rounding. The tests jar is no longer built.

## 1.2.0 (2025-06-19)

- The class is conditionally thread-safe: different instances may be used concurrently in different threads.

## 1.1.3 (2025-06-12)

- `sqrt(0)` threw an exception.

## 1.1.2 (2025-06-11)

- Division by `MIN_VALUE` might give a wrong result.

## 1.1.1 (2025-06-09)

- Addition and subtraction could give a wrong result if the difference of the exponents of the operands
  exceeded 646456993.
- The benchmarks are moved to a [separate project](https://github.com/m-vokhm/Quadruple_benchmarks).
