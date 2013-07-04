/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.curveconstruction;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

import java.io.PrintStream;
import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;

/**
 * Example annuity.
 */
public class AnnuityExample {

  // CSOFF
  public static final double[] FUNDING_CURVE_TIMES = new double[] {1, 2, 5, 10, 20, 31 };
  public static final double[] LIBOR_CURVE_TIMES = new double[] {0.5, 1, 2, 5, 10, 20, 31 };
  public static final double[] FUNDING_YIELDS = new double[] {0.021, 0.036, 0.06, 0.054, 0.049, 0.044 };
  public static final double[] LIBOR_YIELDS = new double[] {0.01, 0.02, 0.035, 0.06, 0.055, 0.05, 0.045 };

  public static final Currency CCY = Currency.EUR;
  public static final double T = 1.0;
  public static final double NOTIONAL = 10000.0;
  public static final double R = 0.03;

  public static final String FUNDING_CURVE_NAME = "Funding Curve";
  public static final String LIBOR_CURVE_NAME = "Libor Curve";

  public static final int MATURITY = 5;
  // CSON

  public static YieldCurveBundle getBundle() {
    final YieldCurveBundle bundle = new YieldCurveBundle();

    final Interpolator1D extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);

    final InterpolatedDoublesCurve fCurve = InterpolatedDoublesCurve.from(FUNDING_CURVE_TIMES, FUNDING_YIELDS, extrapolator);
    final YieldCurve fundingCurve = YieldCurve.from(fCurve);
    bundle.setCurve(FUNDING_CURVE_NAME, fundingCurve);

    final InterpolatedDoublesCurve lcurve = InterpolatedDoublesCurve.from(LIBOR_CURVE_TIMES, LIBOR_YIELDS, extrapolator);
    final YieldCurve liborCurve = YieldCurve.from(lcurve);
    bundle.setCurve(LIBOR_CURVE_NAME, liborCurve);

    return bundle;
  }

  public static double[] fixedPaymentTimes(final int maturity) {
    final double[] fixedPaymentTimes = new double[maturity + 1];
    for (int i = 0; i <= maturity; i++) {
      fixedPaymentTimes[i] = i;
    }
    return fixedPaymentTimes;
  }

  public static void fixedPaymentTimesDemo(final PrintStream out) {
    final double[] paymentTimes = fixedPaymentTimes(MATURITY);
    out.println(Arrays.toString(paymentTimes));
  }

  public static void annuityFixedDemo(final PrintStream out) {
    final double[] paymentTimes = fixedPaymentTimes(MATURITY);
    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(CCY, paymentTimes, R, LIBOR_CURVE_NAME, false);

    out.println(Arrays.deepToString(annuity.getPayments()));

    final YieldCurveBundle bundle = getBundle();

    final PresentValueCalculator presentValueCalculator = PresentValueCalculator.getInstance();
    final double presentValue = annuity.accept(presentValueCalculator, bundle);
    out.format("Present Value %f%n", presentValue);
  }

  public static double[] floatingPaymentTimes(final int maturity) {
    final double[] floatingPaymentTimes = new double[2 * maturity + 1];
    for (int i = 0; i <= 2 * maturity; i++) {
      floatingPaymentTimes[i] = i * 0.5;
    }
    return floatingPaymentTimes;
  }

  public static void floatingPaymentTimesDemo(final PrintStream out) {
    final double[] paymentTimes = floatingPaymentTimes(MATURITY);
    out.println(Arrays.toString(paymentTimes));
  }

}
