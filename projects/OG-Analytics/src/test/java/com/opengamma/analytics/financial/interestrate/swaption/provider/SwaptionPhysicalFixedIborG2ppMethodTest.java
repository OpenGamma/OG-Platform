/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetG2pp;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.volatility.NormalImpliedVolatilityFormula;
import com.opengamma.analytics.financial.montecarlo.provider.G2ppMonteCarloMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of physical delivery swaption in G2++ model.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborG2ppMethodTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex IBOR_INDEX = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency CUR = IBOR_INDEX.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final int SPOT_LAG = IBOR_INDEX.getSpotLag();
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap SWAP_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR, CALENDAR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final boolean IS_LONG = true;
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SPOT_LAG, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0225;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);

  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_PAYER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_RECEIVER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_PAYER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_RECEIVER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  //to derivatives
  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_LONG_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_LONG_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_SHORT_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_SHORT_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = TestsDataSetG2pp.createG2ppParameters1();
  private static final G2ppProviderDiscount G2PP_MULTICURVES = new G2ppProviderDiscount(MULTICURVES, PARAMETERS_G2PP, CUR);

  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_G2PP_APPROXIMATION = SwaptionPhysicalFixedIborG2ppApproximationMethod.getInstance();
  private static final SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod METHOD_G2PP_NI = new SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  private static final double BP1 = 0.0001;
  private static final double TOLERANCE_PV = 1.0E-2;

  //  @Test(enabled = false)
  //  /**
  //   * Test the present value vs a external system. "enabled = false" for the standard testing: the external system is using a TimeCalculator with ACT/365.
  //   */
  //  public void presentValueExternal() {
  //    G2ppPiecewiseConstantParameters parametersCst = TestsDataSetG2pp.createG2ppCstParameters();
  //    final YieldAndDiscountCurve curve5 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  //    final YieldCurveBundle curves = new YieldCurveBundle();
  //    curves.setCurve(FUNDING_CURVE_NAME, curve5);
  //    curves.setCurve(FORWARD_CURVE_NAME, curve5);
  //    G2ppPiecewiseConstantDataBundle bundleCst = new G2ppPiecewiseConstantDataBundle(parametersCst, curves);
  //    CurrencyAmount pv = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, bundleCst);
  //    double pvExternal = 6885626.28245924; // ! TimeCalculator with ACT/365
  //    assertEquals("Swaption physical - G2++ - present value - external system", pvExternal, pv.getAmount(), 1E-2);
  //  }

  @Test(enabled = true)
  /**
   * Test the present value vs a hard-coded value.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    final double pvExpected = 4276532.681;
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvExpected, pv.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final MultipleCurrencyAmount pvPayerLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvPayerShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_SHORT_PAYER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvPayerLong.getAmount(CUR), -pvPayerShort.getAmount(CUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvReceiverLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_RECEIVER, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvReceiverShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_SHORT_RECEIVER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvReceiverLong.getAmount(CUR), -pvReceiverShort.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParity() {
    final MultipleCurrencyAmount pvReceiverLong = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_RECEIVER, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvPayerShort = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_SHORT_PAYER, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvSwap = SWAP_RECEIVER.accept(PVDC, MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - payer/receiver/swap parity", pvReceiverLong.getAmount(CUR) + pvPayerShort.getAmount(CUR), pvSwap.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value by approximation vs by numerical integration.
   */
  public void approximationNumericalIntegration() {
    final MultipleCurrencyAmount pvApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvNI = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - approximation vs Numerical integration", pvApproximation.getAmount(CUR), pvNI.getAmount(CUR), 2.0E+3);
  }

  @Test
  /**
   * Test the present value by approximation vs Monte Carlo.
   */
  public void presentValueMonteCarlo() {
    final int nbPath = 12500;
    final G2ppMonteCarloMethod methodMC = new G2ppMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    final MultipleCurrencyAmount pvMC = methodMC.presentValue(SWAPTION_LONG_PAYER, CUR, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - approximation vs Monte Carlo", pvApproximation.getAmount(CUR), pvMC.getAmount(CUR), 2.5E+4);
  }

  @Test(enabled = false)
  /**
   * Test the present value by approximation vs Monte Carlo: convergence.
   */
  public void presentValueMonteCarloConvergence() {
    final MultipleCurrencyAmount pvApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    final int[] nbPath = new int[] {12500, 100000, 1000000, 10000000};
    final MultipleCurrencyAmount[] pvMC = new MultipleCurrencyAmount[nbPath.length];
    final double[] pvDiff = new double[nbPath.length];
    for (int loopmc = 0; loopmc < nbPath.length; loopmc++) {
      final G2ppMonteCarloMethod methodMC = new G2ppMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath[loopmc]);
      pvMC[loopmc] = methodMC.presentValue(SWAPTION_LONG_PAYER, CUR, G2PP_MULTICURVES);
      pvDiff[loopmc] = pvApproximation.getAmount(CUR) - pvMC[loopmc].getAmount(CUR);
    }
    assertEquals("Swaption physical - G2++ - present value - approximation vs Monte Carlo", pvApproximation.getAmount(CUR), pvMC[nbPath.length - 1].getAmount(CUR), 1.0E+3);
  }

  @Test(enabled = true)
  /**
   * Test the present value by approximation vs by numerical integration for a grid of expiry/tenor.
   * To check the precision, increase the NB_INTEGRATION to 50 (slower but more precise).
   */
  public void approximationNumericalIntegrationGrid() {
    final G2ppPiecewiseConstantParameters parametersG2pp = TestsDataSetG2pp.createG2ppParameters3();
    final G2ppProviderDiscount bundleG2pp = new G2ppProviderDiscount(MULTICURVES, parametersG2pp, CUR);
    final GeneratorSwapFixedIbor generator = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);
    final Period[] expiry = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(5), Period.ofYears(10), Period.ofYears(25)};
    final int nbExpiry = expiry.length;
    final Period[] tenor = new Period[] {Period.ofYears(2), Period.ofYears(5), Period.ofYears(10), Period.ofYears(25)};
    final int nbTenor = tenor.length;
    final double[] fixedRate = new double[] {0.01, 0.02, 0.025, 0.03, 0.04};
    final int nbStrike = fixedRate.length;
    final SwaptionPhysicalFixedIbor[][][] swaption = new SwaptionPhysicalFixedIbor[nbExpiry][nbTenor][nbStrike];
    final ZonedDateTime[] expiryDate = new ZonedDateTime[nbExpiry];
    final ZonedDateTime[] settleDate = new ZonedDateTime[nbExpiry];
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      expiryDate[loopexp] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, expiry[loopexp], generator.getIborIndex(), CALENDAR);
      settleDate[loopexp] = ScheduleCalculator.getAdjustedDate(expiryDate[loopexp], generator.getSpotLag(), CALENDAR);
      for (int loopten = 0; loopten < nbTenor; loopten++) {
        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
          final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(settleDate[loopexp], tenor[loopten], generator, 10000.0, fixedRate[loopstrike], true);
          final SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(expiryDate[loopexp], swapDefinition, true, true);
          swaption[loopexp][loopten][loopstrike] = swaptionDefinition.toDerivative(REFERENCE_DATE);
        }
      }
    }
    final double[][][] pvApprox = new double[nbExpiry][nbTenor][nbStrike];
    final double[][][] volApprox = new double[nbExpiry][nbTenor][nbStrike];
    final double[][][] pvNI = new double[nbExpiry][nbTenor][nbStrike];
    final double[][][] volNI = new double[nbExpiry][nbTenor][nbStrike];
    final double[][][] pvDiff = new double[nbExpiry][nbTenor][nbStrike];
    final double[][][] volDiff = new double[nbExpiry][nbTenor][nbStrike];
    final NormalImpliedVolatilityFormula implied = new NormalImpliedVolatilityFormula(); // Normal vol: LogNormal vol not always well defined with G2++ prices
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      for (int loopten = 0; loopten < nbTenor; loopten++) {
        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
          pvApprox[loopexp][loopten][loopstrike] = METHOD_G2PP_APPROXIMATION.presentValue(swaption[loopexp][loopten][loopstrike], bundleG2pp).getAmount(CUR);
          pvNI[loopexp][loopten][loopstrike] = METHOD_G2PP_NI.presentValue(swaption[loopexp][loopten][loopstrike], bundleG2pp).getAmount(CUR);
          pvDiff[loopexp][loopten][loopstrike] = pvApprox[loopexp][loopten][loopstrike] - pvNI[loopexp][loopten][loopstrike];
          final double pvbp = METHOD_SWAP.presentValueBasisPoint(swaption[loopexp][loopten][loopstrike].getUnderlyingSwap(), MULTICURVES);
          final double forward = swaption[loopexp][loopten][loopstrike].getUnderlyingSwap().accept(PRDC, MULTICURVES);
          final NormalFunctionData data = new NormalFunctionData(forward, pvbp, 0.01);
          volApprox[loopexp][loopten][loopstrike] = implied.getImpliedVolatility(data, swaption[loopexp][loopten][loopstrike], pvApprox[loopexp][loopten][loopstrike]);
          volNI[loopexp][loopten][loopstrike] = implied.getImpliedVolatility(data, swaption[loopexp][loopten][loopstrike], pvNI[loopexp][loopten][loopstrike]);
          volDiff[loopexp][loopten][loopstrike] = (volApprox[loopexp][loopten][loopstrike] - volNI[loopexp][loopten][loopstrike]) / BP1; // In bp
        }
      }
    }
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      for (int loopten = 0; loopten < nbTenor; loopten++) {
        for (int loopstrike = 0; loopstrike < nbStrike; loopstrike++) {
          assertEquals("Swaption physical - G2++ - present value - approximation vs Numerical integration - exp: " + loopexp + ", ten: " + loopten + ", str: " + loopstrike,
              volNI[loopexp][loopten][loopstrike], volApprox[loopexp][loopten][loopstrike], 3 * BP1);
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
    MultipleCurrencyAmount pvPayerLongApproximation = MultipleCurrencyAmount.of(CUR, 0.0);
    MultipleCurrencyAmount pvPayerLongNI = MultipleCurrencyAmount.of(CUR, 0.0);

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ approximation method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 20-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 275 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongNI = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 20-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2200 ms for 100 swaptions. (result strongly dependent of the number of integration points).

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongApproximation = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ approximation method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 20-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 275 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongNI = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption G2++ numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 20-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2200 ms for 100 swaptions. (result strongly dependent of the number of integration points).

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
    MultipleCurrencyAmount pvMC;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC = methodMC.presentValue(SWAPTION_LONG_PAYER, CUR, G2PP_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption physical G2++ Monte Carlo with " + nbPath + " paths: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 04-Dec-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 140 ms for 10 swaptions (12500 paths).
  }

}
