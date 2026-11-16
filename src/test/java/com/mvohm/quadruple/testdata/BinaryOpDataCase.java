package com.mvohm.quadruple.testdata;

public record BinaryOpDataCase<T1, T2, R>(
    T1 left,
    T2 right,
    R expected
) {}