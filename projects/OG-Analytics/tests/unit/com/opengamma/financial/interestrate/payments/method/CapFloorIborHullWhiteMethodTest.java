/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.montecarlo.HullWhiteMonteCarloMethod;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests on the Hull-White one factor method to price Cap/Floor on Ibor. 
 */
public class CapFloorIborHullWhiteMethodTest {
  // Cap/floor description
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final CapFloorIborDefinition CAP_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  private static final CapFloorIborDefinition CAP_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final CapFloorIborHullWhiteMethod METHOD_HW = new CapFloorIborHullWhiteMethod();
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final int NB_PATH = 12500;

  //  private static final HullWhiteMonteCarloMethod METHOD_HW_MONTECARLO = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), NB_PATH);

  @Test
  public void presentValueStandard() {
    double tp = CAP_LONG.getPaymentTime();
    double t0 = CAP_LONG.getFixingPeriodStartTime();
    double t1 = CAP_LONG.getFixingPeriodEndTime();
    double theta = CAP_LONG.getFixingTime();
    double deltaF = CAP_LONG.getFixingYearFraction();
    double deltaP = CAP_LONG.getPaymentYearFraction();
    double alpha0 = MODEL.alpha(0.0, theta, tp, t0, PARAMETERS_HW);
    double alpha1 = MODEL.alpha(0.0, theta, tp, t1, PARAMETERS_HW);
    double ptp = CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(tp);
    double pt0 = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(t0);
    double pt1 = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(t1);
    double kappa = Math.log((1.0 + deltaF * STRIKE) * pt1 / pt0);
    kappa += -(alpha1 * alpha1 - alpha0 * alpha0) / 2.0;
    kappa /= alpha1 - alpha0;
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 1);
    double priceExpected = pt0 / pt1 * normal.getCDF(-kappa - alpha0) - (1.0 + deltaF * STRIKE) * normal.getCDF(-kappa - alpha1);
    priceExpected *= deltaP / deltaF * ptp;
    priceExpected *= NOTIONAL;
    CurrencyAmount priceMethod = METHOD_HW.presentValue(CAP_LONG, BUNDLE_HW);
    assertEquals("Cap/floor: Hull-White pricing", priceExpected, priceMethod.getAmount(), 1E-2);
    assertEquals("Cap/floor: Hull-White pricing", CUR, priceMethod.getCurrency());
  }

  //TODO: present value in arrears

  @Test
  public void presentValueLongShort() {
    CurrencyAmount priceLong = METHOD_HW.presentValue(CAP_LONG, BUNDLE_HW);
    CurrencyAmount priceShort = METHOD_HW.presentValue(CAP_SHORT, BUNDLE_HW);
    assertEquals("Cap/floor: Hull-White pricing", priceLong.getAmount(), -priceShort.getAmount(), 1E-2);

  }

  @Test(enabled = true)
  /**
   * Compare explicit formula with Monte-Carlo and long/short and payer/receiver parities.
   */
  public void monteCarlo() {
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), 10 * NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvExplicit = METHOD_HW.presentValue(CAP_LONG, BUNDLE_HW);
    CurrencyAmount pvMC = methodMC.presentValue(CAP_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_HW);
    assertEquals("Cap/floor - Hull-White - Monte Carlo", pvExplicit.getAmount(), pvMC.getAmount(), 4.0E+2);
    double pvMCPreviousRun = 150060.593;
    assertEquals("Swaption physical - Hull-White - Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), 10 * NB_PATH);
    CurrencyAmount pvShortMC = methodMC.presentValue(CAP_SHORT, CUR, FUNDING_CURVE_NAME, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - Monte Carlo", -pvMC.getAmount(), pvShortMC.getAmount(), 1.0E-2);
  }

  @Test(enabled = false)
  /**
   * Compare explicit formula with Monte-Carlo and long/short and payer/receiver parities.
   */
  public void convergence() {
    long startTime, endTime;
    CurrencyAmount pvExplicit = METHOD_HW.presentValue(CAP_LONG, BUNDLE_HW);
    HullWhiteMonteCarloMethod methodMC;
    int nbPath = 1000000;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    int nbTest = 10;
    startTime = System.currentTimeMillis();
    double[] pv = new double[nbTest];
    double[] pvDiff = new double[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = methodMC.presentValue(CAP_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_HW).getAmount();
      pvDiff[looptest] = pv[looptest] - pvExplicit.getAmount();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv cap/floor Hull-White MC method (" + nbPath + " paths): " + (endTime - startTime) + " ms");
  }

}
