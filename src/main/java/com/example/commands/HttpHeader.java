package com.example.commands;

import java.util.AbstractMap;

@SuppressWarnings("serial")
public class HttpHeader extends AbstractMap.SimpleEntry<String, String> {
    public HttpHeader(String key, String value) {
        super(key, value);
    }
}
