package com.jack.motorbikestatistics;

/**
 * Created by Jack on 05-Mar-17.
 */

public class DataItem<T> {

    private static String NOT_SET = "";

    private String name;
    private T value = null;
    private T average = null;
    private T minimum = null;
    private T maximum = null;

    public DataItem(String name)
    {
        this.name = name;
    }

    public DataItem(String name, T value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

}
