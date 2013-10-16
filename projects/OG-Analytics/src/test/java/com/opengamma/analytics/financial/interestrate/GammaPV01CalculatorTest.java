/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
public class GammaPV01CalculatorTest {
  private final static YieldAndDiscountCurve FUNDING_CURVE = YieldCurve.from(FunctionalDoublesCurve.from(new MyFunction(-0.04, 0.006, 0.1, 0.05)));
  private final static YieldAndDiscountCurve LIBOR_CURVE = YieldCurve.from(FunctionalDoublesCurve.from(new MyFunction(-0.04, 0.005, 0.11, 0.055)));
  private final static GammaPV01Calculator GAMMA_PV01 = GammaPV01Calculator.getInstance();
  private final static PV01Calculator PV01 = PV01Calculator.getInstance();
  private final static PresentValueCalculator PV = PresentValueCalculator.getInstance();
  private final static double EPS = 1e-4;

  private static final String FUNDING_CURVE_NAME = "funding curve";
  private static final String LIBOR_CURVE_NAME = "libor";
  private static YieldCurveBundle CURVES;
  private static final Currency CUR = Currency.EUR;

  static {
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    CURVES.setCurve(LIBOR_CURVE_NAME, LIBOR_CURVE);
  }

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FUNDING_CURVE_NAME);
    final double df = curve.getDiscountFactor(t);
    final double r = 1 / t * (1 / df - 1);
    final Cash cash = new Cash(CUR, 0, t, 1, r, t, FUNDING_CURVE_NAME);
    final Double gammaPV01 = cash.accept(GAMMA_PV01, CURVES);
    assertEquals(0., gammaPV01, EPS); //linear instrument
  }

  @Test
  public void testFRA() {
    final double paymentTime = 0.5;
    final double paymentYearFraction = 30. / 360;
    final double fixingTime = paymentTime - 2. / 365;
    final double fixingPeriodStart = paymentTime;
    final double fixingPeriodEnd = 7. / 12;
    final double fixingYearFraction = 31. / 365;
    final double rate = 0.15;
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
        true);
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStart, fixingPeriodEnd, fixingYearFraction,
        rate, LIBOR_CURVE_NAME);
    final Double gammaPV01 = fra.accept(GAMMA_PV01, CURVES);
    assertEquals(0., gammaPV01, EPS); //linear instrument
  }

  private static class MyFunction extends Function1D<Double, Double> {
    private final double _a;
    private final double _b;
    private final double _c;
    private final double _d;

    public MyFunction(final double a, final double b, final double c, final double d) {
      Validate.isTrue(a + d > 0, "a+d>0");
      Validate.isTrue(d > 0, "d>0");
      Validate.isTrue(c > 0, "c>0");
      _a = a;
      _b = b;
      _c = c;
      _d = d;
    }

    @Override
    public Double evaluate(final Double t) {
      return (_a + _b * t) * Math.exp(-_c * t) + _d;
    }
  }
}
