package com.jack.motorbikestatistics;

/**
 * Created by Jack on 05-Mar-17.
 */

public class DataItem<T> {

    private String name;
    private boolean enableAvgMinMax;

    private T current = null;

    private Double average = 0.0;
    private Double averageSum = 0.0;
    private int averageCount = 0;

    private T minimum = null;
    private T maximum = null;

    public DataItem(String name, boolean avgMinMax) {
        this.name = name;
        this.enableAvgMinMax = avgMinMax;
    }

    public DataItem(String name, boolean avgMinMax, T value) {
        this.name = name;
        this.enableAvgMinMax = avgMinMax;
        this.current = value;

        if ((avgMinMax) && (current instanceof Number)) {
            this.average = (Double)value;
            this.averageSum = (Double)value;
            this.averageCount++;

            this.minimum = value;
            this.maximum = value;
        }
    }

    public String getName() {
        return name;
    }

    public boolean getEnabledAvgMinMax() {
        return enableAvgMinMax;
    }

    public T getCurrent() {
        return current;
    }

    public Double getAverage() {
        return average;
    }

    public T getMinimum() {
        return minimum;
    }

    public T getMaximum() {
        return maximum;
    }

    public void setCurrent(T value) {
        this.current = value;

        if ((enableAvgMinMax) && (current instanceof Number)) {

            /* Sets the average */
            averageCount++;
            averageSum = add(averageSum, (Number)value);
            average = divide(averageSum, averageCount);

            /* Sets the new minimum and maximums if true */
            if ((minimum == null) || lessThan((Number)current, (Number)minimum)) {
                minimum = current;
            }
            if ((maximum == null) || greaterThan((Number)current, (Number)maximum)) {
                maximum = current;
            }
        }
    }

    private Double add(Number a, Number b) {
        return new Double(a.doubleValue() + b.doubleValue());
    }

    private Double divide(Number numerator, Number denominator) {
        return new Double(numerator.doubleValue() / denominator.doubleValue());
    }

    private boolean greaterThan(Number a, Number b) {
        return a.doubleValue() > b.doubleValue();
    }

    private boolean lessThan(Number a, Number b) {
        return a.doubleValue() < b.doubleValue();
    }

}
