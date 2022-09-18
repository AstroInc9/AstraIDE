package com.intellij.openapi.util;

@FunctionalInterface
public interface NullableComputable<T> extends Computable<T> {
    @Override
    T compute();
}
