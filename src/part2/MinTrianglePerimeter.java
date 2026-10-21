package part2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MinTrianglePerimeter {

    private MinTrianglePerimeter() {}

    public static double minTrianglePerimeter(double[][] points) {

        //Presorting
        //sort the points by X-Coordinate and then append the global index of X's for y sorting
        Arrays.sort(points, Comparator.comparingDouble(point->point[0]));
        for(int i=0;i<points.length;i++)
            points[i][2]=i;

        //arr_x is done
        double[][] arr_x=Arrays.copyOf(points,points.length);

        //then sort the points by y-coordinate
        Arrays.sort(points,Comparator.comparingDouble(point->point[1]));

        //arr_y is done
        double[][] arr_y=Arrays.copyOf(points,points.length);

        return getMinTrianglePerimeter(arr_x, arr_y);
    }

    private static double getMinTrianglePerimeter(double[][] arr_x,double[][] arr_y)
    {
        //base case
        // 3 points:Brute force the perimeter
        if(arr_x.length==3) return computePerimeter(arr_x);

        //1 or 2 points:return inf so it doesn't count in combine step
        if(arr_x.length<3) return Double.POSITIVE_INFINITY;

        //divide and conquer
        //get the median of x_array (split line)
        int median_index=(int) Math.ceil(arr_x.length/2.0-1);

        //create the 4 subarrays with sizes n/2
        double[][] arr_x_left=new double[median_index+1][];
        double[][] arr_x_right=new double[arr_x.length-(median_index+1)][];
        double[][] arr_y_left=new double[median_index+1][];
        double[][] arr_y_right=new double[arr_x.length-(median_index+1)][];

        //fill the x_left and x_right subarrays with arr_x values
        int l_x=0,r_x=0;
        for(int i=0;i<arr_x.length;i++)
        {
            if(i<=median_index) arr_x_left[l_x++]=arr_x[i];
            else arr_x_right[r_x++]=arr_x[i];
        }


        //fill the y_left and y_right subarrays with arr_y values
        int l_y=0,r_y=0;
        for(int i=0;i<arr_y.length;i++)
        {
            //compare with the global median index
            if(arr_y[i][2]<=arr_x[median_index][2]) arr_y_left[l_y++]=arr_y[i];
            else arr_y_right[r_y++]=arr_y[i];
        }


        //get the best left perimeter and right perimeter
        double left_perimeter=getMinTrianglePerimeter(arr_x_left,arr_y_left);
        double right_perimeter=getMinTrianglePerimeter(arr_x_right,arr_y_right);

        //combine
        //get the min perimeter from left and right
        double best=Math.min(left_perimeter,right_perimeter);

        //get the split line x coordinate
        double midX=arr_x[median_index][0];

        //get the candidate points at such that |X-best/2|<midX
        ArrayList<double[]> candidates=new ArrayList<>();
        for(int i=0;i<arr_y.length;i++)
        {
            if((arr_y[i][0]>(midX-best/2)&& (arr_y[i][0]<midX+best/2)))
              candidates.add(arr_y[i]);
        }


        for (int i=0;i<candidates.size();i++)
        {
            //for each candidate point get the current window which will be at maxx 15 points
            // we take only points whose y<candidate_y+best/2
            ArrayList<double[]> current_window=new ArrayList<>();
            for(int j=i+1;j<candidates.size();j++)
            {
                if(candidates.get(j)[1]<(candidates.get(i)[1])+best/2)
                    current_window.add(candidates.get(j));
                else break;
            }

            //for every window pair brute force the perimeter with the candidate point
            for(int j=0;j<current_window.size()-1;j++)
            {
                for (int k=j+1;k<current_window.size();k++)
                {
                    double[][] current_arr=new double[3][];
                    current_arr[0]=candidates.get(i);
                    current_arr[1]=current_window.get(j);
                    current_arr[2]=current_window.get(k);
                    double current_per=computePerimeter(current_arr);
                    if(current_per<best)
                        best=current_per;
                }
            }
        }

        return best;

    }

    private static double computePerimeter(double[][] arr)
    {
        //get all points
        double x1=arr[0][0];
        double y1=arr[0][1];
        double x2=arr[1][0];
        double y2=arr[1][1];
        double x3=arr[2][0];
        double y3=arr[2][1];

        //compute distances
        double d1=Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
        double d2=Math.sqrt(Math.pow(x2-x3,2)+Math.pow(y2-y3,2));
        double d3=Math.sqrt(Math.pow(x3-x1,2)+Math.pow(y3-y1,2));

        //return the perimeter
        return d1+d2+d3;

    }

}
