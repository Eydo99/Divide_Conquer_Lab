package Part1.Algorithms;

import Part1.utils.Util;

import java.util.Arrays;

import static java.lang.Math.min;

public class DeterministicSelect implements OrderStatistics {

    @Override
    public int select(int[] arr,int i) {
        return select(arr,0,arr.length-1,i);
    }

    private int select(int[] arr,int low,int high ,int i) {

        //get the current array size
        int size=high-low+1;

        //base case and don't forget to add the offset
        if(size<=5) {
            Arrays.sort(arr,low,high+1);
            return arr[low+i-1];
        }

        //create the medians array which will store the median of each 5-grouped element
        int[] medians = new int[(int)Math.ceil(size/5.0)];
        int j=0;

        //loop through the array and sort each 5-grouped elements and get their median
        for (int start = low; start <= high; start+=5) {

            //sort each 5-grouped elements alone and handle the last group
            Arrays.sort(arr,start,min(start + 5,high+1));

            //default:sort and get the median which is the 3rd element with index 2
            if (high>=start+4)
                medians[j]=arr[start + 2];
            //if last group < 5 then get its median_index and add to it the start offset
            else
            {
                int median_index=start+(int)(Math.ceil((high-start+1)/2.0)-1);
                medians[j]=arr[median_index];
            }
            j++;
        }

        //Recursively select the median from the new medians array with its new rank
        int pivot=select(medians,0,medians.length-1,(int) Math.ceil(medians.length/2.0));

        //get the pivot index and swap it with first element in the array
        int pivot_index=0;
        for (int k=low;k<=high;k++)
        {
            if(arr[k]==pivot)
                pivot_index=k;

        }
        Util.swap(arr,low,pivot_index);

        //partition around our pivot
        int[] boundaries= Util.three_way_partition(arr,low,high);

        //get the boundaries
        int lt=boundaries[0];
        int gt=boundaries[1];

        //recurse to get the rank
        //recurse
        if (i>(lt-low) && i<=(gt-low+1))
            return arr[lt];
        else if (i<=(lt-low))
            return select(arr,low,lt-1,i);
        else
            return select(arr,gt+1,high,i-(gt-low+1));
    }
}
