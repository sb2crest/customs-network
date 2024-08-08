package com.customs.network.fdapn.utils;
@FunctionalInterface
public interface TriPredicate <T,U,V>{
    boolean test(T t, U u, V v);
}
