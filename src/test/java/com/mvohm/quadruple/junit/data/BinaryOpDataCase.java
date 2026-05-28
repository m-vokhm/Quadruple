package com.mvohm.quadruple.junit.data;

public record BinaryOpDataCase<T1, T2, R>(
    T1 left,
    T2 right,
    R expected
) {}