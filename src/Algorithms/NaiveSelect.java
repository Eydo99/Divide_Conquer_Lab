package Algorithms;

import java.util.Arrays;

public class NaiveSelect implements OrderStatistics{

    @Override
    public int select(int[] arr,int i)
    {
        Arrays.sort(arr);
        return arr[i-1];
    }
}
