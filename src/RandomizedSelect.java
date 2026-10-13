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

        //partition around a random pivot and retun that pivot's index
        int r = randomized_partition(arr,low,high);

        //get the rank of the pivot
        int rank=r-low+1;

        //recurse
        if (rank==i)
            return arr[r];
        else if (i<rank)
            return rand_select(arr,low,r-1,i);
        else
            return rand_select(arr,r+1,high,i-rank);
    }



    private int randomized_partition(int[] arr, int low, int high) {

        //choose an index between low and high inclusive
        int i=rand.nextInt(low,high+1);

        //swap the selected pivot index with the firs element
        Util.swap(arr,i,low);

        //return the index of that randomized pivot
        return Util.partition(arr,low,high);
    }


}
