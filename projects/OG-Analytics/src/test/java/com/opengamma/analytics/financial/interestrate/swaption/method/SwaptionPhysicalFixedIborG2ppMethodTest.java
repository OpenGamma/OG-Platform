/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetG2pp;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.montecarlo.G2ppMonteCarloMethod;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
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
 * Tests related to the pricing of physical delivery swaption in G2++ model.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborG2ppMethodTest {
  // Swaption 5Yx5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  //  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR, CALENDAR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  private static final boolean IS_LONG = true;
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_G2PP_APPROXIMATION = new SwaptionPhysicalFixedIborG2ppApproximationMethod();
  private static final SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod METHOD_G2PP_NI = new SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod();
  //  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  //  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = TestsDataSetG2pp.createG2ppParameters1();
  private static final G2ppPiecewiseConstantDataBundle BUNDLE_G2PP = new G2ppPiecewiseConstantDataBundle(PARAMETERS_G2PP, CURVES);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  //  private static final double BP1 = 0.0001;

  @Test(enabled = false)
  /**
   * Test the present value vs a external system. "enabled = false" for the standard testing: the external system is using a TimeCalculator with ACT/365.
   */
  public void presentValueExternal() {
    final G2ppPiecewiseConstantParameters parametersCst = TestsDataSetG2pp.createG2ppCstParameters();
    final YieldAndDiscountCurve curve5 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(FUNDING_CURVE_NAME, curve5);
    curves.setCurve(FORWARD_CURVE_NAME, curve5);
    final G2ppPiecewiseConstantDataBundle bundleCst = new G2ppPiecewiseConstantDataBundle(parametersCst, curves);
    final CurrencyAmount pv = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, bundleCst);
    final double pvExternal = 6885626.28245924; // ! TimeCalculator with ACT/365
    assertEquals("Swaption physical - G2++ - present value - external system", pvExternal, pv.getAmount(), 1E-2);
  }

  @Test(enabled = true)
  /**
   * Test the present value vs a hard-coded value.
   */
  public void presentValue() {
    final CurrencyAmount pv = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    final double pvExpected = 4893110.87;
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvExpected, pv.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final CurrencyAmount pvPayerLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    final CurrencyAmount pvPayerShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvPayerLong.getAmount(), -pvPayerShort.getAmount(), 1E-2);
    final CurrencyAmount pvReceiverLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_G2PP);
    final CurrencyAmount pvReceiverShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvReceiverLong.getAmount(), -pvReceiverShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParity() {
    final CurrencyAmount pvReceiverLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_G2PP);
    final CurrencyAmount pvPayerShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_G2PP);
    final double pvSwap = SWAP_RECEIVER.accept(PVC, CURVES);
    assertEquals("Swaption physical - G2++ - present value - payer/receiver/swap parity", pvReceiverLong.getAmount() + pvPayerShort.getAmount(), pvSwap, 1E-2);
  }

  @Test
  /**
   * Test the present value by approximation vs by numerical integration.
   */
  public void approximationNumericalIntegration() {
    final CurrencyAmount pvApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    final CurrencyAmount pvNI = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - approximation vs Numerical integration", pvApproximation.getAmount(), pvNI.getAmount(), 2.0E+3);
  }

  @Test
  /**
   * Test the present value by approximation vs Monte Carlo.
   */
  public void presentValueMonteCarlo() {
    final int nbPath = 12500;
    final G2ppMonteCarloMethod methodMC = new G2ppMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final CurrencyAmount pvMC = methodMC.presentValue(SWAPTION_PAYER_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_G2PP);
    final CurrencyAmount pvApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - approximation vs Monte Carlo", pvApproximation.getAmount(), pvMC.getAmount(), 2.5E+4);
  }

  @Test(enabled = false)
  /**
   * Test the present value by approximation vs Monte Carlo: convergence.
   */
  public void presentValueMonteCarloConvergence() {
    final CurrencyAmount pvApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    final int[] nbPath = new int[] {12500, 100000, 1000000, 5000000 };
    final CurrencyAmount[] pvMC = new CurrencyAmount[nbPath.length];
    for (int loopmc = 0; loopmc < nbPath.length; loopmc++) {
      final G2ppMonteCarloMethod methodMC = new G2ppMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath[loopmc]);
      pvMC[loopmc] = methodMC.presentValue(SWAPTION_PAYER_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_G2PP);
    }
    assertEquals("Swaption physical - G2++ - present value - approximation vs Monte Carlo", pvApproximation.getAmount(), pvMC[nbPath.length - 1].getAmount(), 1.0E+3);
  }

  //  @Test(enabled = false)
  //  /**
  //   * Test the present value by approximation vs by numerical integration for a grid of expiry/tenor.
  //   */
  //  public void approximationNumericalIntegrationGrid() {
  //    G2ppPiecewiseConstantParameters parametersG2pp = TestsDataSetG2pp.createG2ppParameters2();
  //    G2ppPiecewiseConstantDataBundle bundleG2pp = new G2ppPiecewiseConstantDataBundle(parametersG2pp, CURVES);
  //    GeneratorSwapFixedIbor generator = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);
  //    Period[] expiry = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(5), Period.ofYears(10), Period.ofYears(25)};
  //    int nbExpiry = expiry.length;
  //    Period[] tenor = new Period[] {Period.ofYears(2), Period.ofYears(5), Period.ofYears(10), Period.ofYears(25)};
  //    int nbTenor = tenor.length;
  //    double[] fixedRate = new double[] {0.02, 0.03, 0.04, 0.05, 0.06};
  //    int nbStrike = fixedRate.length;
  //    SwaptionPhysicalFixedIbor[][][] swaption = new SwaptionPhysicalFixedIbor[nbExpiry][nbTenor][nbStrike];
  //    ZonedDateTime[] expiryDate = new ZonedDateTime[nbExpiry];
  //    ZonedDateTime[] settleDate = new ZonedDateTime[nbExpiry];
  //    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
  //      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, expiry[loopexp], generator.getIborIndex());
  //      settleDate[loopexp] = ScheduleCalculator.getAdjustedDate(expiryDate[loopexp], generator.getSpotLag(), CALENDAR);
  //      for (int loopten = 0; loopten < nbTenor; loopten++) {
  //        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
  //          SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(settleDate[loopexp], tenor[loopten], generator, 10000.0, fixedRate[loopstrike], true);
  //          SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(expiryDate[loopexp], swapDefinition, true);
  //          swaption[loopexp][loopten][loopstrike] = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //        }
  //      }
  //    }
  //    double[][][] pvApprox = new double[nbExpiry][nbTenor][nbStrike];
  //    double[][][] volApprox = new double[nbExpiry][nbTenor][nbStrike];
  //    double[][][] pvNI = new double[nbExpiry][nbTenor][nbStrike];
  //    double[][][] volNI = new double[nbExpiry][nbTenor][nbStrike];
  //    double[][][] pvDiff = new double[nbExpiry][nbTenor][nbStrike];
  //    double[][][] volDiff = new double[nbExpiry][nbTenor][nbStrike];
  //    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
  //    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
  //      for (int loopten = 0; loopten < nbTenor; loopten++) {
  //        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
  //          pvApprox[loopexp][loopten][loopstrike] = METHOD_G2PP_APPROXIMATION.presentValue(swaption[loopexp][loopten][loopstrike], bundleG2pp).getAmount();
  //          pvNI[loopexp][loopten][loopstrike] = METHOD_G2PP_NI.presentValue(swaption[loopexp][loopten][loopstrike], bundleG2pp).getAmount();
  //          pvDiff[loopexp][loopten][loopstrike] = pvApprox[loopexp][loopten][loopstrike] - pvNI[loopexp][loopten][loopstrike];
  //          double pvbp = METHOD_SWAP.presentValueBasisPoint(swaption[loopexp][loopten][loopstrike].getUnderlyingSwap(), CURVES);
  //          double forward = PRC.visit(swaption[loopexp][loopten][loopstrike].getUnderlyingSwap(), CURVES);
  //          BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
  //          volApprox[loopexp][loopten][loopstrike] = implied.getImpliedVolatility(data, swaption[loopexp][loopten][loopstrike], pvApprox[loopexp][loopten][loopstrike]);
  //          volNI[loopexp][loopten][loopstrike] = implied.getImpliedVolatility(data, swaption[loopexp][loopten][loopstrike], pvNI[loopexp][loopten][loopstrike]);
  //          volDiff[loopexp][loopten][loopstrike] = (volApprox[loopexp][loopten][loopstrike] - volNI[loopexp][loopten][loopstrike]) / BP1; // In bp
  //        }
  //      }
  //    }
  //    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
  //      for (int loopten = 0; loopten < nbTenor; loopten++) {
  //        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
  //          assertEquals("Swaption physical - G2++ - present value - approximation vs Numerical integration - exp: " + loopexp + ", ten: " + loopten + ", str: " + loopstrike,
  //              volNI[loopexp][loopten][loopstrike], volApprox[loopexp][loopten][loopstrike], 100 * BP1);
  //        }
  //      }
  //    }
  //  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;
    CurrencyAmount pvPayerLongApproximation = CurrencyAmount.of(CUR, 0.0);
    CurrencyAmount pvPayerLongNI = CurrencyAmount.of(CUR, 0.0);

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ approximation method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 24-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 175 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongNI = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 24-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1075 ms for 100 swaptions.
    // TODO: 20-Nov-12: Why is it now very slow (6000ms)?

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ approximation method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 24-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 175 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongNI = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 24-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1075 ms for 100 swaptions.

    System.out.println("G2++ approximation - present value: " + pvPayerLongApproximation);
    System.out.println("G2++ numerical integration - present value: " + pvPayerLongNI);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceMC() {
    long startTime, endTime;
    final int nbTest = 10;

    final int nbPath = 12500;
    final G2ppMonteCarloMethod methodMC = new G2ppMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    @SuppressWarnings("unused")
    CurrencyAmount pvMC;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC = methodMC.presentValue(SWAPTION_PAYER_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_G2PP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ Monte Carlo with " + nbPath + " paths: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 18-Jul-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 140 ms for 10 swaptions (12500 paths).
  }

}
