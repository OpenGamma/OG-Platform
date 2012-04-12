package com.opengamma.analytics.example.curveconstruction;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.CouponFloating;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import java.io.PrintStream;
import java.util.Arrays;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

public class AnnuityExample {
    public static double[] FUNDING_CURVE_TIMES = new double[] {1, 2, 5, 10, 20, 31};
    public static double[] LIBOR_CURVE_TIMES = new double[] {0.5, 1, 2, 5, 10, 20, 31};
    public static double[] FUNDING_YIELDS = new double[] {0.021, 0.036, 0.06, 0.054, 0.049, 0.044};
    public static double[] LIBOR_YIELDS = new double[] {0.01, 0.02, 0.035, 0.06, 0.055, 0.05, 0.045};

    public static Currency CCY = Currency.EUR;
    public static double T = 1.0;
    public static double NOTIONAL = 10000.0;
    public static double R = 0.03;

    public static String FUNDING_CURVE_NAME = "Funding Curve";
    public static String LIBOR_CURVE_NAME = "Libor Curve";

    public static int MATURITY = 5;

    public static YieldCurveBundle getBundle() {
        YieldCurveBundle bundle = new YieldCurveBundle();

        Interpolator1D extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);

        InterpolatedDoublesCurve fCurve = InterpolatedDoublesCurve.from(FUNDING_CURVE_TIMES, FUNDING_YIELDS, extrapolator);
        YieldCurve fundingCurve = new YieldCurve(fCurve);
        bundle.setCurve(FUNDING_CURVE_NAME, fundingCurve);

        InterpolatedDoublesCurve lcurve = InterpolatedDoublesCurve.from(LIBOR_CURVE_TIMES, LIBOR_YIELDS, extrapolator);
        YieldCurve liborCurve = new YieldCurve(lcurve);
        bundle.setCurve(LIBOR_CURVE_NAME, liborCurve);

        return bundle;
    }

    public static double[] fixedPaymentTimes(int maturity) {
        double[] fixedPaymentTimes = new double[maturity+1];
        for (int i = 0; i <= maturity; i++) {
            fixedPaymentTimes[i] = i;
        }
        return fixedPaymentTimes;
    }

    public static void fixedPaymentTimesDemo(PrintStream out) {
        double [] paymentTimes = fixedPaymentTimes(MATURITY);
        out.println(Arrays.toString(paymentTimes));
    }

    public static void annuityFixedDemo(PrintStream out) {
        double [] paymentTimes = fixedPaymentTimes(MATURITY);
        AnnuityCouponFixed annuity = new AnnuityCouponFixed(CCY, paymentTimes, R, LIBOR_CURVE_NAME, false);

        out.println(Arrays.deepToString(annuity.getPayments()));

        YieldCurveBundle bundle = getBundle();

        PresentValueCalculator presentValueCalculator = PresentValueCalculator.getInstance();
        double presentValue = presentValueCalculator.visit(annuity, bundle);
        out.format("Present Value %f%n", presentValue);
    }

    public static double[] floatingPaymentTimes(int maturity) {
        double[] floatingPaymentTimes = new double[2*maturity+1];
        for (int i = 0; i <= 2*maturity; i++) {
            floatingPaymentTimes[i] = i*0.5;
        }
        return floatingPaymentTimes;
    }

    public static void floatingPaymentTimesDemo(PrintStream out) {
        double [] paymentTimes = floatingPaymentTimes(MATURITY);
        out.println(Arrays.toString(paymentTimes));
    }
}
