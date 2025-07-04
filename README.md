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

***The class is conditionally thread-safe.***
While instances are mutable, concurrent use of the same instance is unsafe when performing 
operations that modify its internal state. Read-only operations 
(such as toString() or bigDecimalValue()) can be safely called from multiple threads, 
provided no thread is modifying the instance at the same time.
Using different instances in different threads is always safe.

For more details, see the 
[Quadruple class documentation](https://m-vokhm.github.io/Quadruple/src/main/javadoc/index.html)

#### Motivation
The main goal of the project was to provide the ability to perform calculations 
more accurately than the standard `double` allows, and at the same time 
to do them faster than the standard `BigDecimal` can do. This ability may be important 
for some resource-intensive scientific computing and simulations.  

The results below are from a mid-performance machine with `OpenJDK 21.0.3ф`
when calculating over arrays of random numbers with a size of 65,536 elements.
For measurements, the `SimpleJmhBench.java` utility included in the project was used.
These numbers are not very useful on their own, but give an idea of ​​the performance 
of `Quadruple` versus `BigDecimal`, whose accuracy is limited to 38 decimal digits, 
to make their precision comparable.

    Benchmark                    Mode  Cnt    Score     Error  Units  Q/BD ratio
    ----------------------------------------------------------------------------
    BigDecimal___Addition        avgt   10  300.121  ±  2.243  ns/op 
    QuadStatic___Addition        avgt   10   48.097  ±  0.209  ns/op  6.2
    QuadInstance_Addition        avgt   10   32.364  ±  0.635  ns/op  9.3
    BigDecimal___Subtraction     avgt   10  308.684  ±  1.420  ns/op  
    QuadStatic___Subtraction     avgt   10   52.477  ±  0.559  ns/op  5.9
    QuadInstance_Subtraction     avgt   10   38.364  ±  0.314  ns/op  8.0
    BigDecimal___Multiplication  avgt   10  562.883  ±  5.548  ns/op  
    QuadStatic___Multiplication  avgt   10   95.521  ±  0.766  ns/op  5.9
    QuadInstance_Multiplication  avgt   10   76.426  ±  2.298  ns/op  7.4
    BigDecimal___Division        avgt   10  633.257  ± 10.220  ns/op  
    QuadStatic___Division        avgt   10  248.964  ±  2.217  ns/op  2.5
    QuadInstance_Division        avgt   10  237.597  ±  0.931  ns/op  2.7 
  
The immutable nature of `BigDecimal`, which means the need to create a new object 
with each arithmetic operation, combined with their usage of dynamically 
allocated memory while performing arithmetic operations, causes extremely 
high load on the garbage collector during intensive calculations on large 
amounts of data.

This significantly reduces the overall performance and causes instability 
in the execution time, while the processor load can be 100% most of the time, 
which means that the performance of other tasks running on the computer 
during such calculations decreases.

Unlike `BgDecimal`, `Quadruple` does not use heap memory in arithmetic operations, 
and the mutable nature of `Quadruple` in many cases allows 
most or even all of the computations to be performed without 
creating new object instances, which gives additional benefits 
on large amounts of data.

The following digits were obtained on the same machine when calculating 
on 4,194,304-element arrays

    Benchmark                    Mode  Cnt    Score     Error  Units  Q/BD ratio
    ----------------------------------------------------------------------------
    BigDecimal___Addition        avgt   10  356.115  ± 12.128  ns/op  
    QuadStatic___Addition        avgt   10   54.167  ±  2.190  ns/op   6.6
    QuadInstance_Addition        avgt   10   33.499  ±  0.182  ns/op  10.6
    BigDecimal___Subtraction     avgt   10  390.802  ± 18.310  ns/op      
    QuadStatic___Subtraction     avgt   10   59.101  ±  2.333  ns/op   6.6
    QuadInstance_Subtraction     avgt   10   39.990  ±  0.597  ns/op   9.8
    BigDecimal___Multiplication  avgt   10  641.846  ± 15.363  ns/op      
    QuadStatic___Multiplication  avgt   10  101.757  ±  4.847  ns/op   6.3
    QuadInstance_Multiplication  avgt   10   77.441  ±  0.508  ns/op   8.3
    BigDecimal___Division        avgt   10  714.986  ± 16.402  ns/op      
    QuadStatic___Division        avgt   10  255.274  ±  6.486  ns/op   2.8
    QuadInstance_Division        avgt   10  225.219  ±  1.297  ns/op   3.2

Note the high measurement error values for `BigDecimals`, which are brought about 
by the operation time instability caused by the high load on the garbage collector. 

The charts below show the performance ratio in a graphical form. 
The numbers in the charts represent millions of individual operations per second.     

![Performance at arrays of 64k items](https://github.com/m-vokhm/Quadruple/blob/master/images/Performance_64k.png)

![Performance at arrays of 4M items](https://github.com/m-vokhm/Quadruple/blob/master/images/Performance_4M.png)
     
A simple set of benchmarks can be found in a [separate project](https://github.com/m-vokhm/Quadruple_benchmarks)

#### Usage
A simple example:

       Quadruple radius = new Quadruple(5.5);  // Constructors accept doubles
       System.out.println("Radius of the circle: " + radius); 
       // prints "Radius of the circle: 5.500000000000000000000000000000000000000e+00"
       radius.multiply(radius);                // r^2
       System.out.println("Area of the circle:   " +
                          radius.multiply(Quadruple.pi())); // pi*r^2
       // prints "Area of the circle:   9.503317777109124546349496234420496224688e+01"
    
There exists a [simple matrix library](https://github.com/m-vokhm/QuadMatrix) that utilizes the `Quadruple` type. This library offers three distinct matrix classes, enabling basic operations on square matrices in three modes: a fast but less accurate mode using standard `double` arithmetic, a relatively fast and fairly accurate mode using `Quadruple`, and a highly accurate but slower mode using `BigDecimal`.



#### Testing
A simple stand-alone test utility `QuadTest.java` is included 
in `com.mvohm.quadruple.test` package located in the `test` folder.
You can run it in an IDE, alternatively you can build the project with Maven (or download a release),
and run the tests from a command line as follows:

A simple standalone test utility `QuadTest.java` is included
in the `com.mvohm.quadruple.test` package, located in the `test` folder.
You can run it in an IDE, or you can build the project with Maven (or download a release),
and run the tests from the command line as follows:

    java -cp Quadruple-RV.jar;Quadruple-RV-tests.jar com.mvohm.quadruple.test.QuadTest
    
where `RV` stands for the release version.

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



