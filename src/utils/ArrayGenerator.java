package utils;

import java.util.Random;

public class ArrayGenerator {
    public static int[] generate(int n,int bound){
        Random rand=new Random();
        int[] arr=new int[n];
        for(int i=0;i<n;i++){
            arr[i]=rand.nextInt(-bound,bound+1);
        }
        return arr;
    }
}
