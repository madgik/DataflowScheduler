/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package utils;

/**
 * @author heraldkllapi
 */
public class Check {

    public static void True(boolean cond, String msg) throws RuntimeException {
        if (!cond) {
            throw new RuntimeException(msg);
        }
    }
}
