// @export "imports"
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR6M;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.CouponFloating;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.schedule.NoHolidayCalendar;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;
import java.io.PrintStream;
import java.util.Arrays;

import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

public class AnnuityExample {
    static double[] FUNDING_CURVE_TIMES = new double[] {1, 2, 5, 10, 20, 31};
    static double[] LIBOR_CURVE_TIMES = new double[] {0.5, 1, 2, 5, 10, 20, 31};
    static double[] FUNDING_YIELDS = new double[] {0.021, 0.036, 0.06, 0.054, 0.049, 0.044};
    static double[] LIBOR_YIELDS = new double[] {0.01, 0.02, 0.035, 0.06, 0.055, 0.05, 0.045};

    // @export "yieldCurveBundle"
    public static YieldCurveBundle getBundle() {
        YieldCurveBundle bundle = new YieldCurveBundle();

        // TODO why these interpolators/extrapolators?
        Interpolator1D extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);

        InterpolatedDoublesCurve fCurve = InterpolatedDoublesCurve.from(FUNDING_CURVE_TIMES, FUNDING_YIELDS, extrapolator);
        YieldCurve fundingCurve = new YieldCurve(fCurve);
        bundle.setCurve(fundingCurveName, fundingCurve);

        InterpolatedDoublesCurve lcurve = InterpolatedDoublesCurve.from(LIBOR_CURVE_TIMES, LIBOR_YIELDS, extrapolator);
        YieldCurve liborCurve = new YieldCurve(lcurve);
        bundle.setCurve(liborCurveName, liborCurve);

        return bundle;
    }

    // @export "constants"
    static Currency ccy = Currency.EUR;
    static double t = 1.0;
    static double notional = 10000.0;
    static double r = 0.03;

    static String fundingCurveName = "Funding Curve";
    static String liborCurveName = "Libor Curve";

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
        AnnuityCouponFixed annuity = new AnnuityCouponFixed(ccy, paymentTimes, r, liborCurveName, false);

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

        AnnuityCouponIbor annuity = new AnnuityCouponIbor(ccy, paymentTimes, euribor, fundingCurveName, liborCurveName, true);

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
        OpenGammaExampleClass.main(ogargs);
    }
}
