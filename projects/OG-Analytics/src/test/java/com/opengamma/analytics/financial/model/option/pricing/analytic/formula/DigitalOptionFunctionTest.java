/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.TestsDataSetsForex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class DigitalOptionFunctionTest {

  private static final double SPOT = 105.;
  private static final double[] STRIKES = new double[] {97., 105., 105.1, 114. };
  private static final double TIME = 4.2;
  private static final double[] INTERESTS = new double[] {-0.01, 0.017, 0.05, 0.1 };
  private static final double[] VOLS = new double[] {0.05, 0.1, 0.5 };
  private static final double[] DIVIDENDS = new double[] {0.005, 0.024, 0.05 };

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final String[] CURVES_NAME = TestsDataSetsForex.curveNames();
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILE_TERM = TestsDataSetsForex.smile5points(REFERENCE_DATE);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Tests the present value against an explicit computation. The amount is paid in the domestic currency.
   */
  @Test
  public void presentValueDomestic() {
    final double strike = 1.45;
    final boolean isCall = true;
    final boolean isLong = true;
    final double notional = 100000000;
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofMonths(9), BUSINESS_DAY, CALENDAR);
    final ZonedDateTime expDate = ScheduleCalculator.getAdjustedDate(payDate, -SETTLEMENT_DAYS, CALENDAR);
    final double timeToExpiry = TimeCalculator.getTimeBetween(REFERENCE_DATE, expDate);
    final ForexDefinition forexUnderlyingDefinition = new ForexDefinition(EUR, USD, payDate, notional, strike);
    final ForexOptionDigitalDefinition forexOptionDefinition = new ForexOptionDigitalDefinition(forexUnderlyingDefinition, expDate, isCall, isLong);
    final ForexOptionDigital forexOption = forexOptionDefinition.toDerivative(REFERENCE_DATE);
    final double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(forexOption.getUnderlyingForex().getPaymentTime());
    final double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(forexOption.getUnderlyingForex().getPaymentTime());
    final double rDomestic = CURVES.getCurve(CURVES_NAME[1]).getInterestRate(forexOption.getUnderlyingForex().getPaymentTime());
    final double rForeign = CURVES.getCurve(CURVES_NAME[0]).getInterestRate(forexOption.getUnderlyingForex().getPaymentTime());
    final double forward = SPOT * dfForeign / dfDomestic;
    final double volatility = SMILE_TERM.getVolatility(new Triple<>(timeToExpiry, forward, forward));
    final double sigmaRootT = volatility * Math.sqrt(forexOption.getExpirationTime());
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final int omega = isCall ? 1 : -1;

    final double pvExpected = Math.abs(forexOption.getUnderlyingForex().getPaymentCurrency1().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dM) * (isLong ? 1.0 : -1.0);
    final double price = Math.abs(forexOption.getUnderlyingForex().getPaymentCurrency1().getAmount()) *
        DigitalOptionFunction.price(SPOT, strike, forexOption.getUnderlyingForex().getPaymentTime(), volatility, rDomestic, rDomestic - rForeign, isCall);
    assertEquals(price, pvExpected, pvExpected * 1.e-14);
  }

  /**
   *
   */
  @Test
  public void greeksTest() {
    final boolean[] tfSet = new boolean[] {true, false };
    final double eps = 1.e-6;
    for (final boolean isCall : tfSet) {
      for (final double strike : STRIKES) {
        for (final double interest : INTERESTS) {
          for (final double vol : VOLS) {
            for (final double dividend : DIVIDENDS) {
              final double delta = DigitalOptionFunction.delta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
              final double gamma = DigitalOptionFunction.gamma(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
              final double theta = DigitalOptionFunction.theta(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
              final double vega = DigitalOptionFunction.vega(SPOT, strike, TIME, vol, interest, interest - dividend, isCall);
              final double upSpot = DigitalOptionFunction.price(SPOT + eps, strike, TIME, vol, interest, interest - dividend, isCall);
              final double downSpot = DigitalOptionFunction.price(SPOT - eps, strike, TIME, vol, interest, interest - dividend, isCall);
              final double upSpotDelta = DigitalOptionFunction.delta(SPOT + eps, strike, TIME, vol, interest, interest - dividend, isCall);
              final double downSpotDelta = DigitalOptionFunction.delta(SPOT - eps, strike, TIME, vol, interest, interest - dividend, isCall);
              final double upTime = DigitalOptionFunction.price(SPOT, strike, TIME + eps, vol, interest, interest - dividend, isCall);
              final double downTime = DigitalOptionFunction.price(SPOT, strike, TIME - eps, vol, interest, interest - dividend, isCall);
              final double upVol = DigitalOptionFunction.price(SPOT, strike, TIME, vol + eps, interest, interest - dividend, isCall);
              final double downVol = DigitalOptionFunction.price(SPOT, strike, TIME, vol - eps, interest, interest - dividend, isCall);
              assertEquals(delta, 0.5 * (upSpot - downSpot) / eps, eps);
              assertEquals(gamma, 0.5 * (upSpotDelta - downSpotDelta) / eps, eps);
              assertEquals(theta, -0.5 * (upTime - downTime) / eps, eps);
              assertEquals(vega, 0.5 * (upVol - downVol) / eps, eps);

              final double forward = SPOT * Math.exp(interest - dividend * TIME);
              final double forwardTheta = DigitalOptionFunction.driftlessTheta(forward, strike, TIME, vol, isCall);
              final double sign = isCall ? 1. : -1.;
              final double dUp = Math.log(forward / strike) / vol / Math.sqrt(TIME + eps) - 0.5 * vol * Math.sqrt(TIME + eps);
              final double dDw = Math.log(forward / strike) / vol / Math.sqrt(TIME - eps) - 0.5 * vol * Math.sqrt(TIME - eps);
              final double fwUp = NORMAL.getCDF(sign * dUp);
              final double fwDw = NORMAL.getCDF(sign * dDw);
              assertEquals(forwardTheta, -0.5 * (fwUp - fwDw) / eps, eps);
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotPriceTest() {
    DigitalOptionFunction.price(-SPOT, STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikePriceTest() {
    DigitalOptionFunction.price(SPOT, -STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimePriceTest() {
    DigitalOptionFunction.price(SPOT, STRIKES[0], -TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolPriceTest() {
    DigitalOptionFunction.price(SPOT, STRIKES[0], TIME, -VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotdeltaTest() {
    DigitalOptionFunction.delta(-SPOT, STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikedeltaTest() {
    DigitalOptionFunction.delta(SPOT, -STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimedeltaTest() {
    DigitalOptionFunction.delta(SPOT, STRIKES[0], -TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVoldeltaTest() {
    DigitalOptionFunction.delta(SPOT, STRIKES[0], TIME, -VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotgammaTest() {
    DigitalOptionFunction.gamma(-SPOT, STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikegammaTest() {
    DigitalOptionFunction.gamma(SPOT, -STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimegammaTest() {
    DigitalOptionFunction.gamma(SPOT, STRIKES[0], -TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolgammaTest() {
    DigitalOptionFunction.gamma(SPOT, STRIKES[0], TIME, -VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotthetaTest() {
    DigitalOptionFunction.theta(-SPOT, STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikethetaTest() {
    DigitalOptionFunction.theta(SPOT, -STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimethetaTest() {
    DigitalOptionFunction.theta(SPOT, STRIKES[0], -TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolthetaTest() {
    DigitalOptionFunction.theta(SPOT, STRIKES[0], TIME, -VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotvegaTest() {
    DigitalOptionFunction.vega(-SPOT, STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikevegaTest() {
    DigitalOptionFunction.vega(SPOT, -STRIKES[0], TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimevegaTest() {
    DigitalOptionFunction.vega(SPOT, STRIKES[0], -TIME, VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVolvegaTest() {
    DigitalOptionFunction.vega(SPOT, STRIKES[0], TIME, -VOLS[1], INTERESTS[1], INTERESTS[1] - DIVIDENDS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeSpotdriftlessThetaTest() {
    DigitalOptionFunction.driftlessTheta(-SPOT, STRIKES[0], TIME, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeStrikedriftlessThetaTest() {
    DigitalOptionFunction.driftlessTheta(SPOT, -STRIKES[0], TIME, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeTimedriftlessThetaTest() {
    DigitalOptionFunction.driftlessTheta(SPOT, STRIKES[0], -TIME, VOLS[1], true);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeVoldriftlessThetaTest() {
    DigitalOptionFunction.driftlessTheta(SPOT, STRIKES[0], TIME, -VOLS[1], true);
  }
}
