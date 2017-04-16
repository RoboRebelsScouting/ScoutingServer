package com.walpolerobotics.scouting.scoutingserver.util;

import java.util.ArrayList;
import java.util.Comparator;

public class SortableArrayList<T> extends ArrayList<T> {

    /**
     * Adds an object to the ArrayList with respect to a specific pattern
     * <p>
     * <b>This method assumes that the ArrayList has already been sorted by the Comparator given</b>
     *
     * @param object     The object that will be added to the ArrayList
     * @param comparator The comparator representing the pattern to insert the object
     * @return The position at which the object was inserted
     */
    public int add(T object, Comparator<T> comparator) {
        int i = 0;
        for (T a : this) {
            int relation = comparator.compare(object, a);

            if (relation == -1 || relation == 0) {
                // The object we are adding is less than or equal to the current object, we should
                // add it in it's place
                add(i, object);
                return i;
            }

            i++;
        }
        // The object was never less than or equal to any of the already existing objects in the
        // ArrayList; it is greater than all the objects in the ArrayList
        add(object);
        return size() - 1;
    }
}