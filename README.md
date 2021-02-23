# Quadruple
## A 128-bit floating-point arithmetic for Java

**Quadruple** is a Java class for quadruple-precision floating-point arithmetic
(actually, a little more precise than the standard IEEE-754 quadruple).

An instance of the class has a value that is internally represented as a sign,
128 bits of the fractional part of the mantissa, and 32 bits of the exponent.
Values range from approximately `6.673e-646457032` to `1.761e+646456993`
and include `NaN`, `-Infinty`, and `Infinity`. 
A set of constructors with different types of arguments is provided 
to create instances with corresponding values. 

A value of an instance may be converted to other numeric types 
(namely, `int`, `long`, `double`, and `BigDecimal`) and 
to a string representation. Conversions to primitive numeric types 
semantically are similar to standard narrowing conversions. 

Implements four arithmetic operations and square root calculation.
The relative error of any of the operations does not exceed half of the least significant
bit of the mantissa, i.e. `2^-129`, which approximately corresponds to `1.47e-39`.

Instances are mutable, instance operations change the values, for example, `q1.add(q2)`
replaces the old value of `q1` with the sum of the old value of `q1` and the value of `q2`.
Static methods that take two instances and create a new instance with
the value of the operation result, such as `q3 = Quadruple.add(q1, q2)`,
are also implemented.

A set of of `assign(v)` instance methods replace the old value of the instance
by a new one. The new value can be passed in as an argument of type `Quadruple`,
either as a value of another numeric type, or as a `String` representing a decimal number
in standard notation. Some special notations, such as "Quadruple.NaN", are also admittable.

The class is not thread safe. Different threads should not simultaneously perform
operations even with different instances of the class.
