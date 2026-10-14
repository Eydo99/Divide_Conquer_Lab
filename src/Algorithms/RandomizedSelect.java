package Algorithms;

import utils.Util;

import java.util.Random;

public class RandomizedSelect implements OrderStatistics {

    Random rand = new Random();

    @Override
    public int select(int[] arr,int i) {
        return rand_select(arr,0,arr.length-1,i);
    }

    private int rand_select(int[] arr,int low,int high,int i) {
        //base case:if arr size is 1 return that element
        if (low == high)
            return arr[low];

        //partition around a random pivot and return the lt and gt boundaries
        int[] boundaries = randomized_partition(arr,low,high);

        int lt=boundaries[0];
        int gt=boundaries[1];

        //recurse
        if (i>(lt-low) && i<=(gt-low+1))
            return arr[lt];
        else if (i<=(lt-low))
            return rand_select(arr,low,lt-1,i);
        else
            return rand_select(arr,gt+1,high,i-(gt-low+1));
    }



    private int[] randomized_partition(int[] arr, int low, int high) {

        //choose an index between low and high inclusive
        int i=rand.nextInt(low,high+1);

        //swap the selected pivot index with the firs element
        Util.swap(arr,i,low);

        //return the boundaries of that randomized pivot
        return Util.three_way_partition(arr,low,high);
    }


}
