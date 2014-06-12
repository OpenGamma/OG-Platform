/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetLiborMarketModelDisplacedDiffusion;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.montecarlo.LiborMarketModelMonteCarloMethod;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of physical delivery swaption in LMM displaced diffusion.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapFloorIborLMMDDMethodTest {
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  // Swaption 5Yx5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final int SWAP_TENOR_YEAR = 4;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(3);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.ACT_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR, CALENDAR);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.0375;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, STRIKE, FIXED_IS_PAYER, CALENDAR);
  //to derivatives
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[CURVES.size()]);

  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], CURVES_NAME[1]});
  private static final int NB_CPN_IBOR = SWAP_PAYER.getSecondLeg().getNumberOfPayments();
  private static final boolean IS_CAP = true;

  private static final CouponIbor COUPON_IBOR_LAST = (CouponIbor) SWAP_PAYER.getSecondLeg().getNthPayment(NB_CPN_IBOR - 1);
  private static final CapFloorIbor CAP_LAST = new CapFloorIbor(CUR, COUPON_IBOR_LAST.getPaymentTime(), COUPON_IBOR_LAST.getFundingCurveName(), COUPON_IBOR_LAST.getPaymentYearFraction(), NOTIONAL,
      COUPON_IBOR_LAST.getFixingTime(), IBOR_INDEX, COUPON_IBOR_LAST.getFixingPeriodStartTime(), COUPON_IBOR_LAST.getFixingPeriodEndTime(), COUPON_IBOR_LAST.getFixingAccrualFactor(),
      COUPON_IBOR_LAST.getForwardCurveName(), STRIKE, IS_CAP);
  private static final CapFloorIbor FLOOR_LAST = new CapFloorIbor(CUR, COUPON_IBOR_LAST.getPaymentTime(), COUPON_IBOR_LAST.getFundingCurveName(), COUPON_IBOR_LAST.getPaymentYearFraction(), NOTIONAL,
      COUPON_IBOR_LAST.getFixingTime(), IBOR_INDEX, COUPON_IBOR_LAST.getFixingPeriodStartTime(), COUPON_IBOR_LAST.getFixingPeriodEndTime(), COUPON_IBOR_LAST.getFixingAccrualFactor(),
      COUPON_IBOR_LAST.getForwardCurveName(), STRIKE, !IS_CAP);
  private static final CapFloorIbor CAP_LAST_SHORT = new CapFloorIbor(CUR, COUPON_IBOR_LAST.getPaymentTime(), COUPON_IBOR_LAST.getFundingCurveName(), COUPON_IBOR_LAST.getPaymentYearFraction(),
      -NOTIONAL, COUPON_IBOR_LAST.getFixingTime(), IBOR_INDEX, COUPON_IBOR_LAST.getFixingPeriodStartTime(), COUPON_IBOR_LAST.getFixingPeriodEndTime(), COUPON_IBOR_LAST.getFixingAccrualFactor(),
      COUPON_IBOR_LAST.getForwardCurveName(), STRIKE, IS_CAP);

  private static final CouponIbor COUPON_IBOR_6 = (CouponIbor) SWAP_PAYER.getSecondLeg().getNthPayment(6);
  private static final CapFloorIbor CAP_6 = new CapFloorIbor(CUR, COUPON_IBOR_6.getPaymentTime(), COUPON_IBOR_6.getFundingCurveName(), COUPON_IBOR_6.getPaymentYearFraction(), NOTIONAL,
      COUPON_IBOR_6.getFixingTime(), IBOR_INDEX, COUPON_IBOR_6.getFixingPeriodStartTime(), COUPON_IBOR_6.getFixingPeriodEndTime(), COUPON_IBOR_6.getFixingAccrualFactor(),
      COUPON_IBOR_6.getForwardCurveName(), STRIKE, IS_CAP);
  // Parameters and methods
  private static final int NB_PATH = 12500;
  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE,
      SWAP_PAYER_DEFINITION.getIborLeg());
  private static final LiborMarketModelDisplacedDiffusionDataBundle BUNDLE_LMM = new LiborMarketModelDisplacedDiffusionDataBundle(PARAMETERS_LMM, CURVES);
  private static final CapFloorIborLMMDDMethod METHOD_LMM_CAP = new CapFloorIborLMMDDMethod();

  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  @Test
  /**
   * Test the present value explicit formula in the one curve framework.
   */
  public void presentValueExplicitOneCurve() {
    final YieldCurveBundle curves1 = new YieldCurveBundle();
    curves1.setCurve(CURVES_NAME[0], CURVES.getCurve(CURVES_NAME[0]));
    curves1.setCurve(CURVES_NAME[1], CURVES.getCurve(CURVES_NAME[0]));
    final LiborMarketModelDisplacedDiffusionDataBundle bundleLmm1 = new LiborMarketModelDisplacedDiffusionDataBundle(PARAMETERS_LMM, curves1);
    final CurrencyAmount pvLastExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, bundleLmm1);
    //    YieldAndDiscountCurve forwardCurve = CURVES.getCurve(CURVES_NAME[0]);
    final YieldAndDiscountCurve discountingCurve = CURVES.getCurve(CURVES_NAME[0]);
    final int index = PARAMETERS_LMM.getTimeIndex(CAP_LAST.getFixingPeriodStartTime());
    double volatility = 0;
    for (int loopfact = 0; loopfact < PARAMETERS_LMM.getNbFactor(); loopfact++) {
      volatility += PARAMETERS_LMM.getVolatility()[index][loopfact] * PARAMETERS_LMM.getVolatility()[index][loopfact];
    }
    volatility = Math.sqrt(volatility);
    final double timeDependentFactor = Math.sqrt((Math.exp(2 * PARAMETERS_LMM.getMeanReversion() * CAP_LAST.getFixingTime()) - 1.0) / (2.0 * PARAMETERS_LMM.getMeanReversion()));
    volatility *= timeDependentFactor;
    final double displacement = PARAMETERS_LMM.getDisplacement()[index];
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE + displacement, 1.0, CAP_LAST.isCap()); // Time is in timeDependentFactor
    final double forward = CAP_LAST.accept(PRC, curves1);
    final double df = discountingCurve.getDiscountFactor(CAP_LAST.getPaymentTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward + displacement, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double pvLastExpected = func.evaluate(dataBlack) * NOTIONAL * CAP_LAST.getPaymentYearFraction();
    assertEquals("Cap/floor: LMM pricing by explicit formula - 1 curve", pvLastExpected, pvLastExplicit.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Test the present value explicit formula in the multi-curves framework.
   */
  public void presentValueExplicitMultiCurves() {
    final CurrencyAmount pvLastExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, BUNDLE_LMM);
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(CURVES_NAME[1]);
    final YieldAndDiscountCurve discountingCurve = CURVES.getCurve(CURVES_NAME[0]);
    final int index = PARAMETERS_LMM.getTimeIndex(CAP_LAST.getFixingPeriodStartTime());
    double volatility = 0;
    for (int loopfact = 0; loopfact < PARAMETERS_LMM.getNbFactor(); loopfact++) {
      volatility += PARAMETERS_LMM.getVolatility()[index][loopfact] * PARAMETERS_LMM.getVolatility()[index][loopfact];
    }
    volatility = Math.sqrt(volatility);
    final double timeDependentFactor = Math.sqrt((Math.exp(2 * PARAMETERS_LMM.getMeanReversion() * CAP_LAST.getFixingTime()) - 1.0) / (2.0 * PARAMETERS_LMM.getMeanReversion()));
    volatility *= timeDependentFactor;
    final double displacement = PARAMETERS_LMM.getDisplacement()[index];
    final double beta = forwardCurve.getDiscountFactor(CAP_LAST.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(CAP_LAST.getFixingPeriodEndTime())
        * discountingCurve.getDiscountFactor(CAP_LAST.getFixingPeriodEndTime()) / discountingCurve.getDiscountFactor(CAP_LAST.getFixingPeriodStartTime());
    final double strikeAdjusted = (STRIKE - (beta - 1) / CAP_LAST.getFixingAccrualFactor()) / beta;
    // Strike adjusted from Forward on forward curve and Forward on discount curve.
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeAdjusted + displacement, 1.0, CAP_LAST.isCap());
    final double forwardDsc = (discountingCurve.getDiscountFactor(CAP_LAST.getFixingPeriodStartTime()) / discountingCurve.getDiscountFactor(CAP_LAST.getFixingPeriodEndTime()) - 1.0)
        / CAP_LAST.getFixingAccrualFactor();
    final double df = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(CAP_LAST.getPaymentTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardDsc + displacement, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double pvLastExpected = beta * func.evaluate(dataBlack) * NOTIONAL * CAP_LAST.getPaymentYearFraction();
    assertEquals("Cap/floor: LMM pricing by explicit formula - Multi-curves", pvLastExpected, pvLastExplicit.getAmount(), 1.0E-2);
  }

  @Test(enabled = true)
  /**
   * Test the present value.
   */
  public void presentValueMCOneCurve() {
    final YieldCurveBundle curves1 = new YieldCurveBundle();
    curves1.setCurve(CURVES_NAME[0], CURVES.getCurve(CURVES_NAME[1]));
    curves1.setCurve(CURVES_NAME[1], CURVES.getCurve(CURVES_NAME[1]));
    final LiborMarketModelDisplacedDiffusionDataBundle bundleLmm1 = new LiborMarketModelDisplacedDiffusionDataBundle(PARAMETERS_LMM, curves1);
    final YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[1]);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pvLastMC = methodLmmMc.presentValue(CAP_LAST, CUR, dsc, bundleLmm1);
    final double pvLastPreviousRun = 187362.915; // 12500 paths - 1Y jump
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pvLastPreviousRun, pvLastMC.getAmount(), 1E-2);
    final CurrencyAmount pvLastExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, bundleLmm1);
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pvLastExplicit.getAmount(), pvLastMC.getAmount(), 2.0E+2);
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pv6MC = methodLmmMc.presentValue(CAP_6, CUR, dsc, bundleLmm1);
    final double pv6PreviousRun = 154023.582; // 12500 paths - 1Y jump
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pv6PreviousRun, pv6MC.getAmount(), 1E-2);
    final CurrencyAmount pv6Explicit = METHOD_LMM_CAP.presentValue(CAP_6, bundleLmm1);
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pv6Explicit.getAmount(), pv6MC.getAmount(), 1.25E+3);
  }

  @Test(enabled = true)
  /**
   * Test the present value.
   */
  public void presentValueMCMultiCurves() {
    final YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pvLastMC = methodLmmMc.presentValue(CAP_LAST, CUR, dsc, BUNDLE_LMM);
    final double pvLastPreviousRun = 190791.921; // 12500 paths - 1Y jump
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pvLastPreviousRun, pvLastMC.getAmount(), 1E-2);
    final CurrencyAmount pvLastExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, BUNDLE_LMM);
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pvLastExplicit.getAmount(), pvLastMC.getAmount(), 2.0E+2);
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pv6MC = methodLmmMc.presentValue(CAP_6, CUR, dsc, BUNDLE_LMM);
    final double pv6PreviousRun = 159886.927; // 12500 paths - 1Y jump
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pv6PreviousRun, pv6MC.getAmount(), 1E-2);
    final CurrencyAmount pv6Explicit = METHOD_LMM_CAP.presentValue(CAP_6, BUNDLE_LMM);
    assertEquals("Cap/floor: LMM pricing by Monte Carlo", pv6Explicit.getAmount(), pv6MC.getAmount(), 1.25E+3);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    final CurrencyAmount pvLongExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, BUNDLE_LMM);
    final CurrencyAmount pvShortExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST_SHORT, BUNDLE_LMM);
    assertEquals("Cap/floor - LMM - present value - long/short parity", pvLongExplicit.getAmount(), -pvShortExplicit.getAmount(), 1E-2);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pvLongMC = methodLmmMc.presentValue(CAP_LAST, CUR, dsc, BUNDLE_LMM);
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pvShortMC = methodLmmMc.presentValue(CAP_LAST_SHORT, CUR, dsc, BUNDLE_LMM);
    assertEquals("Cap/floor - LMM - present value MC- long/short parity", pvLongMC.getAmount(), -pvShortMC.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests payer/receiver/fixed parity.
   */
  public void capFloorParity() {
    final CurrencyAmount pvCapExplicit = METHOD_LMM_CAP.presentValue(CAP_LAST, BUNDLE_LMM);
    final CurrencyAmount pvFloorExplicit = METHOD_LMM_CAP.presentValue(FLOOR_LAST, BUNDLE_LMM);
    final double pvFixedExplicit = -SWAP_PAYER.getFirstLeg().getNthPayment(NB_CPN_IBOR - 1).accept(PVC, CURVES);
    final double pvIborExplicit = SWAP_PAYER.getSecondLeg().getNthPayment(NB_CPN_IBOR - 1).accept(PVC, CURVES);
    assertEquals("Cap/floor - LMM - present value Explcit- cap/floor/strike/Ibor parity", pvCapExplicit.getAmount() - pvFloorExplicit.getAmount() + pvFixedExplicit, pvIborExplicit, 1E-2);
    final YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pvCapMC = methodLmmMc.presentValue(CAP_LAST, CUR, dsc, BUNDLE_LMM);
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pvFloorMC = methodLmmMc.presentValue(FLOOR_LAST, CUR, dsc, BUNDLE_LMM);
    assertEquals("Cap/floor - LMM - present value - cap/floor/strike/Ibor parity", pvCapMC.getAmount() - pvFloorMC.getAmount() + pvFixedExplicit, pvIborExplicit, 1.1E+3);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10;

    final YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final double[] pvMC = new double[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC[looptest] = methodLmmMc.presentValue(CAP_LAST, CUR, dsc, BUNDLE_LMM).getAmount();
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " cap/floor LMM Monte Carlo method: " + (endTime - startTime) + " ms");
    // Performance note: LMM Monte Carlo: 15-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 980 ms for 10 cap (12,500 paths).
  }

}
