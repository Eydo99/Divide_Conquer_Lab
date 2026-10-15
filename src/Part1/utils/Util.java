package Part1.utils;

public class Util {

    //Method for partitioning while handling duplicates at the same time
    public static int[] three_way_partition(int[] arr, int low, int high) {

        //we choose the first element as the pivot
        int pivot = arr[low];

        int lt=low;       //arr[low,lt-1]<pivot
        int i = low;     //arr[lt,i-1]=pivot
        int gt=high;     //arr[gt+1,high]>pivot
                        //arr[i,gt] unobserved yet

        //loop until the unobserved region is empty
        while(i<=gt){
            if(arr[i]<pivot)
                //swap element at lt with i then increment both the lt to increase the boundary of < pivot and i
                //increase bec new element is classified as equal to pivot so it needs no inspection
                swap(arr,i++,lt++);
            else if (arr[i]>pivot)
                //swap element at gt with i and then decrement gt to inc the boundary of >pivot but don't increment i
                //as new element at i right now is unclassified and needs classification
                swap(arr,i,gt--);
            else
                //just increment i if a[i] = pivot bec between lt and i-1 is equal to pivot so we just inc the boundary
                i++;
        }

        //return boundaries
        return new  int[]{lt,gt};
    }

    public static void swap(int[] arr ,int i,int j)
    {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static int partition(int[] arr, int low, int high) {
        int pivot = arr[low];
        int i =low;
        for(int j=i+1;j<high;j++)
        {
            if(arr[j]<=pivot)
            {
                i++;
                swap(arr,i,j);
            }
        }
        swap(arr,i,low);
        return i;
    }
}
