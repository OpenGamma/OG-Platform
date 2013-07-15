/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.curveconstruction;

import java.io.PrintStream;

import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;

/**
 * Example for cash.
 */
public class CashExample {

  // CSOFF
  public static final Currency ccy = Currency.EUR;
  public static final double t = 1.0;
  public static final double notional = 10000.0;
  public static final double r = 0.03;

  public static final String yieldCurveName = "Euro Yield Curve Fixed 2%";
  public static final double y = 0.02;
  // CSON

  public static void cashDemo(final PrintStream out) {
    final Cash loan = new Cash(ccy, 0.0, t, notional, r, t, yieldCurveName);

    out.println(loan.getInterestAmount());
  }

  public static YieldCurveBundle getBundle() {
    final YieldCurveBundle bundle = new YieldCurveBundle();
    final ConstantDoublesCurve curve = new ConstantDoublesCurve(y);
    final YieldCurve yieldCurve = YieldCurve.from(curve);
    bundle.setCurve(yieldCurveName, yieldCurve);
    return bundle;
  }

  public static void parRateDemo(final PrintStream out) {
    final Cash loan = new Cash(ccy, 0.0, t, notional, r, t, yieldCurveName);
    final YieldCurveBundle bundle = getBundle();

    final ParRateCalculator parRateCalculator = ParRateCalculator.getInstance();
    final double parRate = loan.accept(parRateCalculator, bundle);
    out.println(parRate);
  }

  public static void presentValueDemo(final PrintStream out) {
    final Cash loan = new Cash(ccy, 0.0, t, notional, r, t, yieldCurveName);
    final YieldCurveBundle bundle = getBundle();

    final PresentValueCalculator presentValueCalculator = PresentValueCalculator.getInstance();
    final double presentValue = loan.accept(presentValueCalculator, bundle);
    out.println(presentValue);
  }

}
