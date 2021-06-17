# Quadruple
## A 128-bit floating-point arithmetic for Java

**Quadruple** is a Java class for quadruple-precision floating-point arithmetic
(actually, a little more precise than the standard IEEE-754 quadruple).

An instance of the class has a value that is internally represented as a sign,
128 bits of the fractional part of the mantissa, and 32 bits of the exponent.
Values range from approximately `6.673e-646457032` to `1.761e+646456993`
and include `NaN`, `-Infinty`, and `Infinity`. 
A set of constructors with different types of arguments, including `String`, 
is provided to create instances with corresponding values. 

A value of an instance may be converted to other numeric types 
(namely, `int`, `long`, `double`, and `BigDecimal`) and 
to a string representation. Conversions to primitive numeric types 
semantically are similar to standard narrowing conversions. 

Implements four arithmetic operations and square root calculation.
The relative error of any of the operations does not exceed half of the least significant
bit of the mantissa, i.e. `2^-129`, which approximately corresponds to `1.47e-39`.

***Instances are mutable***, instance operations change the values, for example, `q1.add(q2)`
replaces the old value of `q1` with the sum of the old value of `q1` and the value of `q2`.
Static methods that take two instances and create a new instance with
the value of the operation result, such as `q3 = Quadruple.add(q1, q2)`,
are also implemented.

A set of `assign(v)` instance methods replace the old value of the instance
with a new one. The new value can be passed in as an argument of type `Quadruple`,
either as a value of another numeric type, or as a `String` representing a decimal number
in standard notation. Some special notations, such as "Quadruple.NaN", are also admittable.

***The class is not thread safe.*** Different threads should not simultaneously perform
operations even with different instances of the class.

For more details, see the 
[Quadruple class documentation](https://m-vokhm.github.io/Quadruple/src/main/javadoc/index.html)

#### Motivation
The main goal of the project was to provide the ability to perform calculations 
more accurately than the standard `double` allows, and at the same time 
to do them faster than the standard `BigDecimal` can do, that may be important 
for some resource-intensive scientific computing and simulations.  

The results below are from an average performance machine with Java 1.8.211
when calculating over arrays of random numbers with a size of 65,536 elements.
For measurements, the SimpleJmhBench.java utility included in the project was used.
These numbers are not very useful on their own, but give an idea of ​​the performance 
of Quadruple versus BigDecimal.

  Benchmark                        Mode  Cnt     Score     Error  Units   Q/BD ratio
  ---------------------------------------------------------------------------------
   a1_BigDecimal___Addition        avgt   10   263.178 ±   1.097  ns/op   
   a2_QuadStatic___Addition        avgt   10    43.435 ±   0.315  ns/op   6.059
   a3_QuadInstance_Addition        avgt   10    34.966 ±   0.222  ns/op   7.527
   b1_BigDecimal___Subtraction     avgt   10   277.652 ±   0.390  ns/op   
   b2_QuadStatic___Subtraction     avgt   10    50.630 ±   0.200  ns/op   5.484
   b3_QuadInstance_Subtraction     avgt   10    43.602 ±   0.281  ns/op   6.368
   c1_BigDecimal___Multiplication  avgt   10   516.112 ±   1.152  ns/op   
   c2_QuadStatic___Multiplication  avgt   10    93.112 ±   0.524  ns/op   5.543
   c3_QuadInstance_Multiplication  avgt   10    87.104 ±   0.109  ns/op   5.925
   d1_BigDecimal___Division        avgt   10   580.177 ±   2.633  ns/op   
   d2_QuadStatic___Division        avgt   10   247.557 ±   0.386  ns/op   2.344
   d3_QuadInstance_Division        avgt   10   240.569 ±   0.534  ns/op   2.412
  
The immutable nature of BigDecimal, which means the need to create a new object 
with each arithmetic operation, combined with their usage of dynamically 
allocated memory while performing arithmetic operations, causes extremely 
high load on the garbage collector during intensive calculations on large 
amounts of data.

This significantly reduces the overall performance and causes instability 
in the execution time, while the processor load can be 100% most of the time, 
which means that the performance of other tasks 
running on the computer during such calculations decreases.

Quadruple does not use heap memory in arithmetic operatons, 
and the mutable nature of Quadruple in many cases allows 
most or even all of the computations to be performed without 
creating new object instances, which gives additional benefits 
on large amounts of data.

The following digits were obtained on the same machine when calculating over 4,194,304 element arrays

   Benchmark                        Mode  Cnt     Score     Error  Units  Q/BD Ratio
   ---------------------------------------------------------------------------------
    a1_BigDecimal___Addition        avgt   10   910.662 ± 261.937  ns/op  
    a2_QuadStatic___Addition        avgt   10    69.834 ±  21.660  ns/op  13.040
    a3_QuadInstance_Addition        avgt   10    37.015 ±   0.083  ns/op  24.603
    b1_BigDecimal___Subtraction     avgt   10   943.394 ± 200.669  ns/op  
    b2_QuadStatic___Subtraction     avgt   10    77.527 ±  22.030  ns/op  12.169
    b3_QuadInstance_Subtraction     avgt   10    45.511 ±   0.081  ns/op  20.729
    c1_BigDecimal___Multiplication  avgt   10  1244.141 ± 327.183  ns/op  
    c2_QuadStatic___Multiplication  avgt   10   122.723 ±  20.559  ns/op  10.138
    c3_QuadInstance_Multiplication  avgt   10    88.910 ±   0.127  ns/op  13.993
    d1_BigDecimal___Division        avgt   10  1287.110 ± 286.895  ns/op  
    d2_QuadStatic___Division        avgt   10   283.074 ±  24.969  ns/op  4.547
    d3_QuadInstance_Division        avgt   10   228.153 ±   0.287  ns/op  5.641

Note the high measurement error values for BigDecimals, which are brought about 
by the operation time instability caused by high load on the garbage collector. 


#### Usage
A simple example:

       Quadruple radius = new Quadruple(5.5);  // Constructors accept double
       System.out.println("Radius of the circle: " + radius); 
       // prints "Radius of the circle: 5.500000000000000000000000000000000000000e+00"
       radius.multiply(radius);                // r^2
       System.out.println("Area of the circle:   " +
                          radius.multiply(Quadruple.pi())); // pi*r^2
       // prints "Area of the circle:   9.503317777109124546349496234420496224688e+01"
    
#### Testing
A simple stand-alone test utility `QuadTest.java` is included 
in `com.mvohm.quadruple.test` package located in the `test` folder.

It uses a statically defined set of test data to provide complete coverage 
of the code under test, as well as automatically generated data sequences 
to test the basic operations performed by `Quadruple.java`, 
and evaluates both the correctness of the operation being tested 
and the relative error of the resulting value. 
Also some statistics are collected, e.g. mean error, maximum error, and MSE.
The test results are printed to `System.out`. 
 
A set of test methods, one for each tested operation, 
intended to be used with `JUnit`, are in the `QuadJUnitTests.java` class. 
They use the same data as the aforementioned standalone utility. 

For more details, see the 
[Quadruple tests documentation](https://m-vokhm.github.io/Quadruple/src/test/javadoc/index.html)


   