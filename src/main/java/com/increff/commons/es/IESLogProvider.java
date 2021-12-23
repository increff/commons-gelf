package com.increff.commons.es;

public interface IESLogProvider {
    //The implementor will log JSON string of the ES Request
    public void log(String json);
}
