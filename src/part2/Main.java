package part2;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class Main {

    public static void main(String[] args) throws FileNotFoundException
    {
        //hardcoded input file and output file for now
        String filePath = "C:\\Users\\eyada\\OneDrive\\Desktop\\Divide and Conquer Lab\\Divide And Conquer Lab\\src\\part2\\test";
        String answer="answer.txt";


        try(BufferedReader reader = new BufferedReader(new FileReader(filePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(answer));
            )
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

                //Presorting
                //sort the points by X-Coordinate and then append the global index of X's for y sorting
                Arrays.sort(points,Comparator.comparingDouble(point->point[0]));
                for(int i=0;i<points.length;i++)
                    points[i][2]=i;

                //arr_x is done
                double[][] arr_x=Arrays.copyOf(points,points.length);

                //then sort the points by y-coordinate
                Arrays.sort(points,Comparator.comparingDouble(point->point[1]));

                //arr_y is done
                double[][] arr_y=Arrays.copyOf(points,points.length);

                //get the minPerimeter of triangle formed by these points
                double minPerimeter=MinTrianglePerimeter.getMinTrianglePerimeter(arr_x, arr_y);

                //write the result of each test case to the output file
                String s=String.format(Locale.US,"%.3f",minPerimeter);
                writer.write(s + "\n");

            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
