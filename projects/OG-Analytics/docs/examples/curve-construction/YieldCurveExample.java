// @export "imports"
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import java.io.PrintStream;

public class YieldCurveExample {
    // @export constantYieldCurveDemo
    static double y = 0.02;
    public static void constantYieldCurveDemo(PrintStream out) {
        Curve curve = new ConstantDoublesCurve(y);
        YieldCurve yieldCurve = new YieldCurve(curve);

        out.println(yieldCurve.getInterestRate(1.0));
        out.println(yieldCurve.getInterestRate(2.0));
        out.println(yieldCurve.getInterestRate(10.0));

        out.println(yieldCurve.getDiscountFactor(1.0));
        out.println(yieldCurve.getDiscountFactor(2.0));
        out.println(yieldCurve.getDiscountFactor(10.0));
    }

    // @export yieldCurveBundleDemo
    public static void yieldCurveBundleDemo(PrintStream out) {
        Curve curve = new ConstantDoublesCurve(y);
        YieldCurve yieldCurve = new YieldCurve(curve);

        YieldCurveBundle bundle = new YieldCurveBundle();
        bundle.setCurve("Constant 2% Yield Curve", yieldCurve);

        out.println(bundle.getAllNames());

        assert bundle.containsName("Constant 2% Yield Curve");
        assert bundle.getCurve("Constant 2% Yield Curve").equals(yieldCurve);
    }

    // @end
    public static void main(String[] args) throws Exception {
        String[] ogargs = {
            "YieldCurveExample",
            "../dexy--yield-curve-output.json",
            "../dexy--yield-curve-fields.json"
        };
        OpenGammaExample.main(ogargs);
    }
}
