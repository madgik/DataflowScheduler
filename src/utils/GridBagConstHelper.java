/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package utils;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * @author Herald Kllapi <br> University of Athens / Department of Informatics
 * and Telecommunications.
 * @since 1.0
 */
public class GridBagConstHelper {

    public static GridBagConstraints create(int x,
        int y,
        double weightX,
        double weightY,
        Insets insets,
        int fill) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        if (weightX >= 0) {
            constraints.weightx = weightX;
        }
        if (weightY >= 0) {
            constraints.weighty = weightY;
        }
        constraints.insets = insets;
        constraints.fill = fill;
        return constraints;
    }
}
