package edu.buffalo.cse.cse486586.groupmessenger2;

/**
 * Created by prasad-pc on 3/2/17.
 */
import java.util.Collections;
import java.util.ArrayList;
public class RandomIdGenerator {

    private static ArrayList<Integer> numbers = new ArrayList<Integer>();
    public static String randomNumber(){

        //define ArrayList to hold Integer objects

        for(int i = 0; i < 5000; i++)
        {
            numbers.add(i+1);
        }

        Collections.shuffle(numbers);
        Integer num=numbers.get(0);
       // System.out.println(numbers);
        numbers.remove(0);
        return (num.toString());
    }
}
