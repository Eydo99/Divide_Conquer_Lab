package part2;

import java.io.*;
import java.util.Arrays;

import java.util.Locale;

public class Main {

    public static void main(String[] args) throws FileNotFoundException
    {

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
            String line;

            //loop until EOF
            while ((line = reader.readLine()) != null)
            {
                //empty line in the middle is skipped
                if(line.trim().isEmpty()) continue;

                //get the no of points for the test case
                int no_of_points=Integer.parseInt(line.trim());

                //put these points in an array one by one
                double[][] points=new double[no_of_points][];
                for(int i=0;i<no_of_points;i++)
                {
                    line=reader.readLine().trim();
                    String[] point=line.split("\\s+");
                    points[i]=new double[3];
                    points[i][0]=Double.parseDouble(point[0]);
                    points[i][1]=Double.parseDouble(point[1]);
                }


                //get the minPerimeter of triangle formed by these points
                double minPerimeter=MinTrianglePerimeter.minTrianglePerimeter(points);

                //write the result of each test case to the output file
                String s=String.format(Locale.US,"%.3f",minPerimeter);
                System.out.println(s);
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
