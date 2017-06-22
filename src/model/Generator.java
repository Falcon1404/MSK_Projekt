package model;


import java.util.Random;

public class Generator
{
    Random rand = new Random();

    public double normal(double a, double b)
    {
        double x1;
        if (b <= 0.0)
        {
            System.err.println("SimGenerator.normal: b must be >0");
            return -1;
        }
        x1 = rand.nextGaussian();
        return (a + x1 * b);
    }
}
