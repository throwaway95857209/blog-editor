package com.example.commands;

public abstract class AbstractCommand<T> {
    public abstract T execute() throws Exception;
}
