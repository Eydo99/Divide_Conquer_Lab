package Part1.Algorithms;

import java.util.Arrays;

public class NaiveSelect implements OrderStatistics{

    @Override
    public int select(int[] arr,int i)
    {
        //sort the array and return element at index i-1
        Arrays.sort(arr);
        return arr[i-1];
    }
}
