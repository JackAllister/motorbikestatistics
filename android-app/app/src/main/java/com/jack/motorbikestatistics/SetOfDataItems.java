/**
 * @file SetOfDataItems.java
 * @brief Extension of ArrayList allows for searching via name.
 *
 * This class is created to allow RealtimeFragment to search items by name.
 * Simple searches through all items for a matching name.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import java.util.ArrayList;

/**
 * @brief ArrayList extension to allow searching via item name.
 */
public class SetOfDataItems extends ArrayList<DataItem> {

    /**
     * @brief Constructor, just calls inherited constructor method.
     */
    public SetOfDataItems() {
        super();
    }

    /**
     * @brief Function to allow searching of ArrayList<DataItem> via name.
     *
     * Loops through all items in array until one item with matching name
     * is found. This is then returned by the function.
     *
     * @param name - Name to match.
     * @return DataItem - The item with matching name.
     */
    public DataItem getItemByName(String name) {
        DataItem result = null;

        for (DataItem item: this) {
            if (item.getName().equals(name)) {
                result = item;
                break;
            }
        }

        return result;
    }
}
