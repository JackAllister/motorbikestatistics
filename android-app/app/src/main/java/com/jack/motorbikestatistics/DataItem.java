/**
 * @file DataItem.java
 * @brief UI class for holding information regarding a specific statistic.
 *
 * Implementation of generic class to allow multiple data types android
 * added functionality such as averaging, minimum and maximum.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

/**
 * @brief Class used for holding and displaying a piece of data within the
 * statistic ListView UI.
 */
public class DataItem<T> {

    /** @brief The name of the statistic. */
    private String name;
    /** @brief Whether averaging, min & max values should be calculated. */
    private boolean enableAvgMinMax;

    /** @brief Current reading value */
    private T current = null;

    /** @brief Average reading value */
    private Double average = null;
    /** @brief Sum of all readings, used for averaging */
    private Double averageSum = 0.0;
    /** @brief Number of readings, used for averaging */
    private int averageCount = 0;

    /** @brief Minimum reading value */
    private T minimum = null;
    /** @brief Maximum reading value */
    private T maximum = null;

    /**
     * @brief Constructor for creation of a DataItem.
     *
     * Sets up the name of the data item as well as Whether
     * averaging, minimum and maximum readings will be used
     *
     * @param name - Name of the data item.
     * @param avgMinMax - Whether additive functionality shall be available.
     */
    public DataItem(String name, boolean avgMinMax) {
        this.name = name;
        this.enableAvgMinMax = avgMinMax;
    }

    /**
     * @brief Constructor for creation of a DataItem.
     *
     * Similar to other constructor however allows setting of an
     * initial value.
     *
     * @param name - Name of the data item.
     * @param avgMinMax - Whether additive functionality shall be available.
     * @param value - Initial reading value.
     */
    public DataItem(String name, boolean avgMinMax, T value) {
        this.name = name;
        this.enableAvgMinMax = avgMinMax;
        this.current = value;

        if ((avgMinMax) && (current instanceof Number)) {

            Number val = (Number)value;
            this.average = val.doubleValue();
            this.averageSum = val.doubleValue();
            this.averageCount++;

            this.minimum = value;
            this.maximum = value;
        }
    }

    /**
     * @brief Getter for name of data item.
     * @return String - DataItem name.
     */
    public String getName() {
        return name;
    }

    /**
     * @brief Getter for whether additional functionality enabled.
     * @return boolean - Averaging, Minimum & Maximum enabled.
     */
    public boolean getEnabledAvgMinMax() {
        return enableAvgMinMax;
    }

    /**
     * @brief Getter for current reading value.
     * @return T - Current reading value.
     */
    public T getCurrent() {
        return current;
    }

    /**
     * @brief Getter for average of readings.
     * @return Double - Average of all readings.
     */
    public Double getAverage() {
        return average;
    }

    /**
     * @brief Getter for minimum of readings.
     * @return T - Minimum value.
     */
    public T getMinimum() {
        return minimum;
    }

    /**
     * @brief Getter for maximum of readings.
     * @return T - Maximum value.
     */
    public T getMaximum() {
        return maximum;
    }

    /**
     * @brief Setter for current reading value.
     *
     * If additive functionality enabled and the reading is of types
     * number then we go ahead and update our min, max & average values
     * as well will the passed in new reading.
     *
     * @param value - New reading.
     */
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

    /**
     * @brief Function to allow addition of numbers with variable types.
     * @param a - First operand.
     * @param b - Second operand.
     * @return Double - Sum.
     */
    private Double add(Number a, Number b) {
        return new Double(a.doubleValue() + b.doubleValue());
    }

    /**
     * @brief Function to allow division of numbers with variable types.
     * @param numerator - Numerator of divisior.
     * @param denominator - Denominator of divisor.
     * @return Double - Result of division.
     */
    private Double divide(Number numerator, Number denominator) {
        return new Double(numerator.doubleValue() / denominator.doubleValue());
    }

    /**
     * @brief Function to chcek whether A is greater than B.
     * @param a - First operand.
     * @param b - Second operand.
     * @return boolean - Whether A is greater than B.
     */
    private boolean greaterThan(Number a, Number b) {
        return a.doubleValue() > b.doubleValue();
    }

    /**
     * @brief Function to chcek whether A is less than B.
     * @param a - First operand.
     * @param b - Second operand.
     * @return boolean - Whether A is less than B.
     */
    private boolean lessThan(Number a, Number b) {
        return a.doubleValue() < b.doubleValue();
    }

}
