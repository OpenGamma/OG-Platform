/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwap;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.index.generator.EUR1YEURIBOR6M;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborG2ppApproximationMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod;
import com.opengamma.analytics.financial.model.interestrate.G2ppTestsDataSet;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of physical delivery swaption in G2++ model.
 */
public class SwaptionPhysicalFixedIborG2ppMethodTest {
  // Swaption 5Yx5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final boolean IS_LONG = true;
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final FixedCouponSwap<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_G2PP_APPROXIMATION = new SwaptionPhysicalFixedIborG2ppApproximationMethod();
  private static final SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod METHOD_G2PP_NI = new SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod();
  private static final SwapFixedDiscountingMethod METHOD_SWAP = SwapFixedDiscountingMethod.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = G2ppTestsDataSet.createG2ppParameters1();
  private static final G2ppPiecewiseConstantDataBundle BUNDLE_G2PP = new G2ppPiecewiseConstantDataBundle(PARAMETERS_G2PP, CURVES);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  private static final double BP1 = 0.0001;

  @Test(enabled = false)
  /**
   * Test the present value vs a external system. "enabled = false" for the standard testing: the external system is using a TimeCalculator with ACT/365.
   */
  public void presentValueExternal() {
    G2ppPiecewiseConstantParameters parametersCst = G2ppTestsDataSet.createG2ppCstParameters();
    final YieldAndDiscountCurve curve5 = new YieldCurve(ConstantDoublesCurve.from(0.05));
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(FUNDING_CURVE_NAME, curve5);
    curves.setCurve(FORWARD_CURVE_NAME, curve5);
    G2ppPiecewiseConstantDataBundle bundleCst = new G2ppPiecewiseConstantDataBundle(parametersCst, curves);
    CurrencyAmount pv = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, bundleCst);
    double pvExternal = 6885626.28245924; // ! TimeCalculator with ACT/365
    assertEquals("Swaption physical - G2++ - present value - external system", pvExternal, pv.getAmount(), 1E-2);
  }

  @Test(enabled = true)
  /**
   * Test the present value vs a hard-coded value.
   */
  public void presentValue() {
    CurrencyAmount pv = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    double pvExpected = 4893110.87;
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvExpected, pv.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    CurrencyAmount pvPayerLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    CurrencyAmount pvPayerShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvPayerLong.getAmount(), -pvPayerShort.getAmount(), 1E-2);
    CurrencyAmount pvReceiverLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_G2PP);
    CurrencyAmount pvReceiverShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvReceiverLong.getAmount(), -pvReceiverShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParity() {
    CurrencyAmount pvReceiverLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_G2PP);
    CurrencyAmount pvPayerShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_G2PP);
    double pvSwap = PVC.visit(SWAP_RECEIVER, CURVES);
    assertEquals("Swaption physical - G2++ - present value - payer/receiver/swap parity", pvReceiverLong.getAmount() + pvPayerShort.getAmount(), pvSwap, 1E-2);
  }

  @Test
  /**
   * Test the present value by approximation vs by numerical integration.
   */
  public void approximationNumericalIntegration() {
    CurrencyAmount pvApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    CurrencyAmount pvNI = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - approximation vs Numerical integration", pvApproximation.getAmount(), pvNI.getAmount(), 2.0E+3);
  }

  @Test(enabled = false)
  /**
   * Test the present value by approximation vs by numerical integration for a grid of expiry/tenor.
   */
  public void approximationNumericalIntegrationGrid() {
    G2ppPiecewiseConstantParameters parametersG2pp = G2ppTestsDataSet.createG2ppParameters2();
    G2ppPiecewiseConstantDataBundle bundleG2pp = new G2ppPiecewiseConstantDataBundle(parametersG2pp, CURVES);
    GeneratorSwap generator = new EUR1YEURIBOR6M(CALENDAR);
    Period[] expiry = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(5), Period.ofYears(10), Period.ofYears(25)};
    int nbExpiry = expiry.length;
    Period[] tenor = new Period[] {Period.ofYears(2), Period.ofYears(5), Period.ofYears(10), Period.ofYears(25)};
    int nbTenor = tenor.length;
    double[] fixedRate = new double[] {0.02, 0.03, 0.04, 0.05, 0.06};
    int nbStrike = fixedRate.length;
    SwaptionPhysicalFixedIbor[][][] swaption = new SwaptionPhysicalFixedIbor[nbExpiry][nbTenor][nbStrike];
    ZonedDateTime[] expiryDate = new ZonedDateTime[nbExpiry];
    ZonedDateTime[] settleDate = new ZonedDateTime[nbExpiry];
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, expiry[loopexp], generator.getIborIndex());
      settleDate[loopexp] = ScheduleCalculator.getAdjustedDate(expiryDate[loopexp], generator.getSpotLag(), CALENDAR);
      for (int loopten = 0; loopten < nbTenor; loopten++) {
        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
          SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(settleDate[loopexp], tenor[loopten], generator, 10000.0, fixedRate[loopstrike], true);
          SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(expiryDate[loopexp], swapDefinition, true);
          swaption[loopexp][loopten][loopstrike] = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
        }
      }
    }
    double[][][] pvApprox = new double[nbExpiry][nbTenor][nbStrike];
    double[][][] volApprox = new double[nbExpiry][nbTenor][nbStrike];
    double[][][] pvNI = new double[nbExpiry][nbTenor][nbStrike];
    double[][][] volNI = new double[nbExpiry][nbTenor][nbStrike];
    double[][][] pvDiff = new double[nbExpiry][nbTenor][nbStrike];
    double[][][] volDiff = new double[nbExpiry][nbTenor][nbStrike];
    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      for (int loopten = 0; loopten < nbTenor; loopten++) {
        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
          pvApprox[loopexp][loopten][loopstrike] = METHOD_G2PP_APPROXIMATION.presentValue(swaption[loopexp][loopten][loopstrike], bundleG2pp).getAmount();
          pvNI[loopexp][loopten][loopstrike] = METHOD_G2PP_NI.presentValue(swaption[loopexp][loopten][loopstrike], bundleG2pp).getAmount();
          pvDiff[loopexp][loopten][loopstrike] = pvApprox[loopexp][loopten][loopstrike] - pvNI[loopexp][loopten][loopstrike];
          double pvbp = METHOD_SWAP.presentValueBasisPoint(swaption[loopexp][loopten][loopstrike].getUnderlyingSwap(), CURVES);
          double forward = PRC.visit(swaption[loopexp][loopten][loopstrike].getUnderlyingSwap(), CURVES);
          BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
          volApprox[loopexp][loopten][loopstrike] = implied.getImpliedVolatility(data, swaption[loopexp][loopten][loopstrike], pvApprox[loopexp][loopten][loopstrike]);
          volNI[loopexp][loopten][loopstrike] = implied.getImpliedVolatility(data, swaption[loopexp][loopten][loopstrike], pvNI[loopexp][loopten][loopstrike]);
          volDiff[loopexp][loopten][loopstrike] = (volApprox[loopexp][loopten][loopstrike] - volNI[loopexp][loopten][loopstrike]) / BP1; // In bp
        }
      }
    }
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      for (int loopten = 0; loopten < nbTenor; loopten++) {
        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
          assertEquals("Swaption physical - G2++ - present value - approximation vs Numerical integration", volNI[loopexp][loopten][loopstrike], volApprox[loopexp][loopten][loopstrike], 10 * BP1);
        }
      }
    }
  }

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

    System.out.println("G2++ approximation - present value: " + pvPayerLongApproximation);
    System.out.println("G2++ numerical integration - present value: " + pvPayerLongNI);
  }

}
