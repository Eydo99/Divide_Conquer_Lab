import java.util.Random;

public class RandomizedSelect implements OrderStatistics {

    Random rand = new Random();

    @Override
    public int select(int[] arr,int i) {
        return rand_select(arr,0,arr.length-1,i);
    }

    private int rand_select(int[] arr,int low,int high,int i) {
        if (low == high)
            return arr[low];
        int r = randomized_partition(arr,low,high);
        int rank=r-low+1;
        if (rank==i)
            return arr[r];
        else if (i<rank)
            return rand_select(arr,low,r-1,i);
        else
            return rand_select(arr,r+1,high,i-rank);
    }



    private int randomized_partition(int[] arr, int low, int high) {
        int i=rand.nextInt(low,high+1);
        Util.swap(arr,i,low);
        return Util.partition(arr,low,high);
    }


}
