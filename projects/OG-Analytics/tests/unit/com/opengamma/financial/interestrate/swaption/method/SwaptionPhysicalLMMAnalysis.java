/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

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
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.LiborMarketModelDisplacedDiffusionTestsDataSet;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.montecarlo.LiborMarketModelMonteCarloMethod;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Analysis related to the pricing of physical delivery swaption in LMM displaced diffusion.
 */
public class SwaptionPhysicalLMMAnalysis {

  // Swaption 5Yx5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(6);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 10;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final boolean IS_LONG = true;
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();

  private static final FixedCouponSwap<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Parameters and methods
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = new SwaptionPhysicalFixedIborLMMDDMethod();
  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE, SWAP_PAYER_DEFINITION);
  private static final LiborMarketModelDisplacedDiffusionDataBundle BUNDLE_LMM = new LiborMarketModelDisplacedDiffusionDataBundle(PARAMETERS_LMM, CURVES);

  @Test(enabled = false)
  /**
   * Test the present value: approximated formula vs Monte Carlo.
   */
  public void convergenceMCApprox() {

    long startTime, endTime;

    double forward = PRC.visit(SWAP_RECEIVER, CURVES);
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(SWAP_RECEIVER, CURVES);
    BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    int nbStrike = 5;
    double[] strike = new double[nbStrike];
    CurrencyAmount[] pvPayerApprox = new CurrencyAmount[nbStrike];
    double[] impliedVolPayerApprox = new double[nbStrike];
    CurrencyAmount[] pvReceiApprox = new CurrencyAmount[nbStrike];
    double[] impliedVolReceiApprox = new double[nbStrike];

    YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    int[] nbPath = new int[] {25000, 500000, 2000000};
    double[] maxJump = new double[] {10.0, 2.0, 1.0};

    CurrencyAmount[][][] pvPayerMC = new CurrencyAmount[nbStrike][nbPath.length][maxJump.length];
    double[][][] impliedVolPayerMC = new double[nbStrike][nbPath.length][maxJump.length];
    CurrencyAmount[][][] pvReceiMC = new CurrencyAmount[nbStrike][nbPath.length][maxJump.length];
    double[][][] impliedVolReceiMC = new double[nbStrike][nbPath.length][maxJump.length];

    for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
      strike[loopstrike] = forward - 0.03 + 0.015 * loopstrike;
      SwapFixedIborDefinition swapPayerDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, strike[loopstrike], FIXED_IS_PAYER);
      SwaptionPhysicalFixedIborDefinition swaptionPayerDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapPayerDefinition, IS_LONG);
      SwaptionPhysicalFixedIbor swaptionPayer = swaptionPayerDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      EuropeanVanillaOption optionPayer = new EuropeanVanillaOption(strike[loopstrike], swaptionPayer.getTimeToExpiry(), FIXED_IS_PAYER);
      SwapFixedIborDefinition swapReceiDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, strike[loopstrike], !FIXED_IS_PAYER);
      SwaptionPhysicalFixedIborDefinition swaptionReceiDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapReceiDefinition, IS_LONG);
      SwaptionPhysicalFixedIbor swaptionRecei = swaptionReceiDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      EuropeanVanillaOption optionRecei = new EuropeanVanillaOption(strike[loopstrike], swaptionPayer.getTimeToExpiry(), !FIXED_IS_PAYER);

      pvPayerApprox[loopstrike] = METHOD_LMM.presentValue(swaptionPayer, BUNDLE_LMM);
      impliedVolPayerApprox[loopstrike] = implied.getImpliedVolatility(data, optionPayer, pvPayerApprox[loopstrike].getAmount());

      pvReceiApprox[loopstrike] = METHOD_LMM.presentValue(swaptionRecei, BUNDLE_LMM);
      impliedVolReceiApprox[loopstrike] = implied.getImpliedVolatility(data, optionRecei, pvReceiApprox[loopstrike].getAmount());

      for (int looppath = 0; looppath < nbPath.length; looppath++) {
        startTime = System.currentTimeMillis();
        for (int loopmax = 0; loopmax < maxJump.length; loopmax++) {
          LiborMarketModelMonteCarloMethod methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath[looppath], maxJump[loopmax]);
          pvPayerMC[loopstrike][looppath][loopmax] = methodLmmMc.presentValue(swaptionPayer, CUR, dsc, BUNDLE_LMM);
          impliedVolPayerMC[loopstrike][looppath][loopmax] = implied.getImpliedVolatility(data, optionPayer, pvPayerMC[loopstrike][looppath][loopmax].getAmount());
          pvReceiMC[loopstrike][looppath][loopmax] = methodLmmMc.presentValue(swaptionRecei, CUR, dsc, BUNDLE_LMM);
          impliedVolReceiMC[loopstrike][looppath][loopmax] = implied.getImpliedVolatility(data, optionRecei, pvReceiMC[loopstrike][looppath][loopmax].getAmount());
        }
        endTime = System.currentTimeMillis();
        System.out.println("Swaption LMM Monte Carlo method (" + nbPath[looppath] + "): " + (endTime - startTime) + " ms");
      }

    }

    double test = 0.0;
    test++;
  }

}
