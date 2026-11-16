package com.mvohm.quadruple.testdata;

public record UnaryOpDataCase<T, R>(
    T operand,
    R expected
) {}
