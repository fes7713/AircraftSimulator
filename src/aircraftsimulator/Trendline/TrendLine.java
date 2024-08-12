package aircraftsimulator.Trendline;

import org.apache.commons.math3.linear.RealMatrix;

public interface TrendLine {
    public void setValues(double[] y, double[] x); // y ~ f(x)
    public double predict(double x); // get a predicted y for a given x
    public RealMatrix getParameters();
}