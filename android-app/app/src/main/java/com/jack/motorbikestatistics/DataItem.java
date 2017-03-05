package com.jack.motorbikestatistics;

/**
 * Created by Jack on 05-Mar-17.
 */

public class DataItem<T> {

    private static String NOT_SET = "";

    private String name;
    private T value = null;
    private T attribute1 = null;
    private T attribute2 = null;
    private T attribute3 = null;

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

    public T getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(T attribute1) {
        this.attribute1 = attribute1;
    }

    public T getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(T attribute2) {
        this.attribute2 = attribute2;
    }

    public T getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(T attribute3) {
        this.attribute3 = attribute3;
    }
}
