package com.mvohm.quadruple.junit.data;

public record UnaryOpDataCase<T, R>(
    T operand,
    R expected
) {}
