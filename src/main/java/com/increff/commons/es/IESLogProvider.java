package com.increff.commons.es;

/**
 * This interface defines a contract for logging JSON strings of ESRequests.
 * Implementors of this interface are expected to provide a concrete implementation of the log method.
 */
public interface IESLogProvider {
    /**
     * The implementor will log JSON string of the ES Request
     * @param json The JSON string to be logged.
     */
    public void log(String json);
}
