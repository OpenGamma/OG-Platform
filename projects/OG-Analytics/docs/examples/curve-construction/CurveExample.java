// @export "imports"
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.NodalDoublesCurve;

import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;

import com.opengamma.math.interpolation.LinearExtrapolator1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;

import java.io.PrintStream;

public class CurveExample {
    // @export "constantDoublesCurveDemo"
    public static void constantDoublesCurveDemo(PrintStream out) {
        Curve curve = new ConstantDoublesCurve(5.0);

        out.println(curve.getYValue(0.0));
        out.println(curve.getYValue(10.0));
        out.println(curve.getYValue(-10.0));
    }

    // @export "nodalDoublesCurveDemo"
    public static void nodalDoublesCurveDemo(PrintStream out) {
        double[] xdata = { 1.0, 2.0, 3.0 };
        double[] ydata = { 2.0, 4.0, 6.0 };
        Curve curve = new NodalDoublesCurve(xdata, ydata, true);

        out.println(curve.getYValue(1.0));
        out.println(curve.getYValue(2.0));
        out.println(curve.getYValue(3.0));

        try {
            out.println("Trying to get y value for an undefined x value...");
            curve.getYValue(1.5);
        } catch (java.lang.IllegalArgumentException  e) {
            out.println("IllegalArgumentException called");
        }
    }

    // @export "interpolatedDoublesCurveDemo"
    public static void interpolatedDoublesCurveDemo(PrintStream out) {
        double[] xdata = { 1.0, 2.0, 3.0 };
        double[] ydata = { 2.0, 4.0, 6.0 };
        LinearInterpolator1D interpolator = new LinearInterpolator1D();
        Curve curve = new InterpolatedDoublesCurve(xdata, ydata, interpolator, true);

        out.println(curve.getYValue(1.0));
        out.println(curve.getYValue(2.0));
        out.println(curve.getYValue(3.0));

        out.println(curve.getYValue(1.5));
        try {
            out.println("Trying to get y value for too large an x...");
            curve.getYValue(4.0);
        } catch (java.lang.IllegalArgumentException  e) {
            out.println("IllegalArgumentException called");
        }
    }

    // @export "interpolatorExtrapolatorDoublesCurveDemo"
    public static void interpolatorExtrapolatorDoublesCurveDemo(PrintStream out) {
        double[] xdata = { 1.0, 2.0, 3.0 };
        double[] ydata = { 2.0, 4.0, 6.0 };

        Interpolator1D interpolator = new LinearInterpolator1D();
        Interpolator1D leftExtrapolator = new LinearExtrapolator1D(interpolator);
        Interpolator1D rightExtrapolator = new LinearExtrapolator1D(interpolator);
        Interpolator1D combined = new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);

        Curve curve = new InterpolatedDoublesCurve(xdata, ydata, combined, true);

        out.println(curve.getYValue(1.0));
        out.println(curve.getYValue(2.0));
        out.println(curve.getYValue(3.0));

        out.println(curve.getYValue(1.5));
        out.println(curve.getYValue(4.0));
    }

    // @end
    public static void main(String[] args) throws Exception {
        String[] ogargs = {"CurveExample", "../dexy--curve-output.json"};
        OpenGammaExample.main(ogargs);
    }
}
