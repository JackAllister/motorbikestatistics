package com.jack.motorbikestatistics;

/**
 * Created by Jack on 15-Mar-17.
 */

import java.util.ArrayList;

public class SetOfDataItems extends ArrayList<DataItem> {

    public SetOfDataItems() {
        super();
    }

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
