package com.jack.motorbikestatistics;

/**
 * Created by Jack on 05-Mar-17.
 */

public class DataItem<T> {

    private static String NOT_SET = "";

    private String name;
    private String value;
    private String attribute1 = NOT_SET;
    private String attribute2 = NOT_SET;
    private String attribute3 = NOT_SET;

    public DataItem(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }

    public String getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(String attribute3) {
        this.attribute3 = attribute3;
    }
}
