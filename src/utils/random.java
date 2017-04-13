package utils;

/**
 * Created by johnchronis on 4/13/17.
 */
public class random {

    public static int randomInRange(int max, int min){

            int range = (max - min) + 1;
            return (int)(Math.random() * range) + min;

    }
}
