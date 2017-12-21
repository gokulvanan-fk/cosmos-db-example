package com.example;

import lombok.Getter;

import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class ObjectCache<T> {
    private ConcurrentLinkedQueue<T> cache = new ConcurrentLinkedQueue<>();
}
