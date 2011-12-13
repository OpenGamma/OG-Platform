// @export "imports"
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR6M;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.schedule.NoHolidayCalendar;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;
import java.io.PrintStream;
import java.util.Arrays;

public class AnnuityExample {
    // @export "yieldCurveBundle"
    public static YieldCurveBundle getBundle() {
        YieldCurveBundle bundle = new YieldCurveBundle();

        ConstantDoublesCurve curve = new ConstantDoublesCurve(y);
        YieldCurve yieldCurve = new YieldCurve(curve);
        bundle.setCurve(yieldCurveName, yieldCurve);

        double liborRate = 0.015;
        ConstantDoublesCurve lcurve = new ConstantDoublesCurve(liborRate);
        YieldCurve liborCurve = new YieldCurve(lcurve);
        bundle.setCurve(liborCurveName, liborCurve);

        return bundle;
    }

    // @export "constants"
    static Currency ccy = Currency.EUR;
    static double t = 1.0;
    static double notional = 10000.0;
    static double r = 0.03;

    static String yieldCurveName = "Euro Yield Curve Fixed 2%";
    static double y = 0.02;

    static String liborCurveName = "Euribor";

    static int maturity = 5;

    // @export "fixedPaymentTimes"
    public static double[] fixedPaymentTimes(int maturity) {
        double[] fixedPaymentTimes = new double[maturity+1];
        for (int i = 0; i <= maturity; i++) {
            fixedPaymentTimes[i] = i;
        }
        return fixedPaymentTimes;
    }
    // @export "fixedPaymentTimesDemo"
    public static void fixedPaymentTimesDemo(PrintStream out) {
        double [] paymentTimes = fixedPaymentTimes(maturity);
        out.println(Arrays.toString(paymentTimes));
    }

    // @export "annuityFixedDemo"
    public static void annuityFixedDemo(PrintStream out) {
        double [] paymentTimes = fixedPaymentTimes(maturity);
        AnnuityCouponFixed annuity = new AnnuityCouponFixed(ccy, paymentTimes, r, yieldCurveName, false);

        out.println(Arrays.deepToString(annuity.getPayments()));

        YieldCurveBundle bundle = getBundle();

        PresentValueCalculator presentValueCalculator = PresentValueCalculator.getInstance();
        double presentValue = presentValueCalculator.visit(annuity, bundle);
        out.format("Present Value %f%n", presentValue);
    }

    // @export "floatingPaymentTimes"
    public static double[] floatingPaymentTimes(int maturity) {
        double[] floatingPaymentTimes = new double[2*maturity+1];
        for (int i = 0; i <= 2*maturity; i++) {
            floatingPaymentTimes[i] = i*0.5;
        }
        return floatingPaymentTimes;
    }

    // @export "floatingPaymentTimesDemo"
    public static void floatingPaymentTimesDemo(PrintStream out) {
        double [] paymentTimes = floatingPaymentTimes(maturity);
        out.println(Arrays.toString(paymentTimes));
    }

    // @export "annuityFloatingDemo"
    public static void annuityFloatingDemo(PrintStream out) {
        double[] paymentTimes = floatingPaymentTimes(maturity);

        // set up Euribor
        NoHolidayCalendar calendar = new NoHolidayCalendar();
        IborIndex euribor = new EURIBOR6M(calendar);

        AnnuityCouponIbor annuity = new AnnuityCouponIbor(ccy, paymentTimes, euribor, liborCurveName, liborCurveName, true);
        out.println(Arrays.deepToString(annuity.getPayments()));

        YieldCurveBundle bundle = getBundle();

        PresentValueCalculator presentValueCalculator = PresentValueCalculator.getInstance();
        double presentValue = presentValueCalculator.visit(annuity, bundle);
        out.format("Present Value %f%n", presentValue);
    }

    // @end
    public static void main(String[] args) throws Exception {
        String[] ogargs = {
            "AnnuityExample",
            "../dexy--annuity-output.json",
            "../dexy--annuity-fields.json"
        };
        OpenGammaExample.main(ogargs);
    }
}
