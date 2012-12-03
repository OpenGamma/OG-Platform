/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetHullWhite;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.montecarlo.provider.HullWhiteMonteCarloMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of physical delivery swaption in Hull-White one factor model.
 */
public class SwaptionPhysicalFixedIborHullWhiteMethodTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex IBOR_INDEX = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  // Swaption 5Yx5Y
  private static final Currency CUR = IBOR_INDEX.getCurrency();
  private static final Calendar CALENDAR = IBOR_INDEX.getCalendar();
  private static final int SPOT_LAG = IBOR_INDEX.getSpotLag();
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final boolean IS_LONG = true;
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SPOT_LAG, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0175;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);

  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_PAYER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_LONG_RECEIVER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_PAYER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_SHORT_RECEIVER_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, !IS_LONG);
  //to derivatives
  public static final String NOT_USED = "Not used";
  public static final String[] NOT_USED_A = {NOT_USED, NOT_USED, NOT_USED};

  //  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);
  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_LONG_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_LONG_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_SHORT_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_SHORT_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);

  // Calculator
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_HW = SwaptionPhysicalFixedIborHullWhiteMethod.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  private static final PresentValueCurveSensitivityHullWhiteCalculator PVCSHWC = PresentValueCurveSensitivityHullWhiteCalculator.getInstance();
  private static final ParameterSensitivityHullWhiteCalculator PS_HW_C = new ParameterSensitivityHullWhiteCalculator(PVCSHWC);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator PS_HW_FDC = new ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(PVHWC, SHIFT);

  private static final SwaptionPhysicalFixedIborHullWhiteNumericalIntegrationMethod METHOD_HW_INTEGRATION = SwaptionPhysicalFixedIborHullWhiteNumericalIntegrationMethod.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteApproximationMethod METHOD_HW_APPROXIMATION = SwaptionPhysicalFixedIborHullWhiteApproximationMethod.getInstance();
  private static final int NB_PATH = 12500;
  private static final HullWhiteMonteCarloMethod METHOD_HW_MONTECARLO = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), NB_PATH);

  private static final HullWhiteOneFactorPiecewiseConstantParameters HW_PARAMETERS = TestsDataSetHullWhite.createHullWhiteParameters();
  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, HW_PARAMETERS, CUR);

  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+0; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  /**
   * Test the present value.
   */
  public void presentValueExplicit() {
    final MultipleCurrencyAmount pv = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final double timeToExpiry = SWAPTION_LONG_PAYER.getTimeToExpiry();
    final AnnuityPaymentFixed cfe = CFEC.visitSwap(SWAPTION_LONG_PAYER.getUnderlyingSwap(), MULTICURVES);
    final int numberOfPayments = cfe.getNumberOfPayments();
    final double alpha[] = new double[numberOfPayments];
    final double disccf[] = new double[numberOfPayments];
    for (int loopcf = 0; loopcf < numberOfPayments; loopcf++) {
      alpha[loopcf] = MODEL.alpha(HW_PARAMETERS, 0.0, timeToExpiry, timeToExpiry, cfe.getNthPayment(loopcf).getPaymentTime());
      disccf[loopcf] = MULTICURVES.getDiscountFactor(CUR, cfe.getNthPayment(loopcf).getPaymentTime()) * cfe.getNthPayment(loopcf).getAmount();
    }
    final double kappa = MODEL.kappa(disccf, alpha);
    double pvExpected = 0.0;
    for (int loopcf = 0; loopcf < numberOfPayments; loopcf++) {
      pvExpected += disccf[loopcf] * NORMAL.getCDF(-kappa - alpha[loopcf]);
    }
    assertEquals("Swaption physical - Hull-White - present value", pvExpected, pv.getAmount(CUR), 1E-2);
    final MultipleCurrencyAmount pv2 = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, cfe, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - present value", pv, pv2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParityExplicit() {
    final MultipleCurrencyAmount pvLong = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvShort = METHOD_HW.presentValue(SWAPTION_SHORT_PAYER, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - present value - long/short parity", pvLong.getAmount(CUR), -pvShort.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParityExplicit() {
    final MultipleCurrencyAmount pvReceiverLong = METHOD_HW.presentValue(SWAPTION_LONG_RECEIVER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvPayerShort = METHOD_HW.presentValue(SWAPTION_SHORT_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvSwap = SWAP_RECEIVER.accept(PVDC, MULTICURVES);
    assertEquals("Swaption physical - Hull-White - present value - payer/receiver/swap parity", pvReceiverLong.getAmount(CUR) + pvPayerShort.getAmount(CUR), pvSwap.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the method against the present value calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = SWAPTION_LONG_PAYER.accept(PVHWC, HW_MULTICURVES);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: present value : method and calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Compare explicit formula with numerical integration.
   */
  public void presentValueNumericalIntegration() {
    final MultipleCurrencyAmount pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvPayerLongExplicit.getAmount(CUR), pvPayerLongIntegration.getAmount(CUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvPayerShortExplicit = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvPayerShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvPayerShortExplicit.getAmount(CUR), pvPayerShortIntegration.getAmount(CUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvReceiverLongExplicit = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvReceiverLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvReceiverLongExplicit.getAmount(CUR), pvReceiverLongIntegration.getAmount(CUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvReceiverShortExplicit = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvReceiverShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvReceiverShortExplicit.getAmount(CUR), pvReceiverShortIntegration.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Compare explicit formula with approximated formula.
   */
  public void presentValueApproximation() {
    final BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    final double forward = SWAPTION_LONG_PAYER.getUnderlyingSwap().accept(ParRateDiscountingCalculator.getInstance(), MULTICURVES);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAPTION_LONG_PAYER.getUnderlyingSwap(), MULTICURVES);
    final MultipleCurrencyAmount pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvPayerLongApproximation = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
    final double volExplicit = implied.getImpliedVolatility(data, SWAPTION_LONG_PAYER, pvPayerLongExplicit.getAmount(CUR));
    final double volApprox = implied.getImpliedVolatility(data, SWAPTION_LONG_PAYER, pvPayerLongApproximation.getAmount(CUR));
    assertEquals("Swaption physical - Hull-White - present value - explicit/approximation", pvPayerLongExplicit.getAmount(CUR), pvPayerLongApproximation.getAmount(CUR), 5.0E+2);
    assertEquals("Swaption physical - Hull-White - present value - explicit/approximation", volExplicit, volApprox, 2.5E-4); // 0.025%
    final MultipleCurrencyAmount pvReceiverLongExplicit = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyAmount pvReceiverLongApproximation = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvReceiverLongExplicit.getAmount(CUR), pvReceiverLongApproximation.getAmount(CUR), 5.0E+2);
  }

  @Test
  /**
   * Approximation analysis.
   */
  public void presentValueApproximationAnalysis() {
    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    int nbStrike = 20;
    double[] pvExplicit = new double[nbStrike + 1];
    double[] pvApproximation = new double[nbStrike + 1];
    double[] strike = new double[nbStrike + 1];
    double[] volExplicit = new double[nbStrike + 1];
    double[] volApprox = new double[nbStrike + 1];
    double strikeRange = 0.010;
    SwapFixedCoupon<Coupon> swap = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);
    double forward = swap.accept(PRDC, MULTICURVES);
    double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, MULTICURVES);
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strike[loopstrike] = forward - strikeRange + 3 * strikeRange * loopstrike / nbStrike; // From forward-strikeRange to forward+2*strikeRange
      SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, strike[loopstrike], FIXED_IS_PAYER);
      SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinition, IS_LONG);
      SwaptionPhysicalFixedIbor swaption = swaptionDefinition.toDerivative(REFERENCE_DATE, NOT_USED_A);
      pvExplicit[loopstrike] = METHOD_HW.presentValue(swaption, HW_MULTICURVES).getAmount(CUR);
      pvApproximation[loopstrike] = METHOD_HW_APPROXIMATION.presentValue(swaption, HW_MULTICURVES).getAmount(CUR);
      BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
      volExplicit[loopstrike] = implied.getImpliedVolatility(data, swaption, pvExplicit[loopstrike]);
      volApprox[loopstrike] = implied.getImpliedVolatility(data, swaption, pvApproximation[loopstrike]);
      assertEquals("Swaption physical - Hull-White - implied volatility - explicit/approximation", volExplicit[loopstrike], volApprox[loopstrike], 1.0E-3); // 0.10%
    }
  }

  @Test(enabled = true)
  /**
   * Compare explicit formula with Monte-Carlo and long/short and payer/receiver parities.
   */
  public void presentValueMonteCarlo() {
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    MultipleCurrencyAmount pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    MultipleCurrencyAmount pvPayerLongMC = methodMC.presentValue(SWAPTION_LONG_PAYER, CUR, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - Monte Carlo", pvPayerLongExplicit.getAmount(CUR), pvPayerLongMC.getAmount(CUR), 1.0E+4);
    double pvMCPreviousRun = 5788344.797;
    assertEquals("Swaption physical - Hull-White - Monte Carlo", pvMCPreviousRun, pvPayerLongMC.getAmount(CUR), TOLERANCE_PV);
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    MultipleCurrencyAmount pvPayerShortMC = methodMC.presentValue(SWAPTION_SHORT_PAYER, CUR, HW_MULTICURVES);
    assertEquals("Swaption physical - Hull-White - Monte Carlo", -pvPayerLongMC.getAmount(CUR), pvPayerShortMC.getAmount(CUR), TOLERANCE_PV);
    MultipleCurrencyAmount pvReceiverLongMC = methodMC.presentValue(SWAPTION_LONG_RECEIVER, CUR, HW_MULTICURVES);
    MultipleCurrencyAmount pvSwap = SWAP_RECEIVER.accept(PVDC, MULTICURVES);
    assertEquals("Swaption physical - Hull-White - Monte Carlo - payer/receiver/swap parity", pvReceiverLongMC.getAmount(CUR) + pvPayerShortMC.getAmount(CUR), pvSwap.getAmount(CUR), 1.0E+5);
  }

  @Test
  /**
   * Tests the Hull-White parameters sensitivity for the explicit formula.
   */
  public void presentValueHullWhiteSensitivityExplicit() {
    final double[] hwSensitivity = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final int nbVolatility = HW_PARAMETERS.getVolatility().length;
    final double shiftVol = 1.0E-6;
    final double[] volatilityBumped = new double[nbVolatility];
    System.arraycopy(HW_PARAMETERS.getVolatility(), 0, volatilityBumped, 0, nbVolatility);
    final double[] volatilityTime = new double[nbVolatility - 1];
    System.arraycopy(HW_PARAMETERS.getVolatilityTime(), 1, volatilityTime, 0, nbVolatility - 1);
    final double[] pvBumpedPlus = new double[nbVolatility];
    final double[] pvBumpedMinus = new double[nbVolatility];
    final HullWhiteOneFactorPiecewiseConstantParameters parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(HW_PARAMETERS.getMeanReversion(), volatilityBumped, volatilityTime);
    final HullWhiteOneFactorProviderDiscount bundleBumped = new HullWhiteOneFactorProviderDiscount(MULTICURVES, parametersBumped, CUR);
    for (int loopvol = 0; loopvol < nbVolatility; loopvol++) {
      volatilityBumped[loopvol] += shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedPlus[loopvol] = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, bundleBumped).getAmount(CUR);
      volatilityBumped[loopvol] -= 2 * shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedMinus[loopvol] = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, bundleBumped).getAmount(CUR);
      assertEquals(
          "Swaption - Hull-White sensitivity adjoint: derivative " + loopvol + " - difference:" + ((pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol) - hwSensitivity[loopvol]),
          (pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol), hwSensitivity[loopvol], TOLERANCE_PV_DELTA);
      volatilityBumped[loopvol] = HW_PARAMETERS.getVolatility()[loopvol];
    }
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void presentValueHullWhiteSensitivitylongShortParityExplicit() {
    final double[] pvhwsLong = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final double[] pvhwsShort = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_SHORT_PAYER, HW_MULTICURVES);
    for (int loophw = 0; loophw < pvhwsLong.length; loophw++) {
      assertEquals("Swaption physical - Hull-White - presentValueHullWhiteSensitivity - long/short parity", pvhwsLong[loophw], -pvhwsShort[loophw], TOLERANCE_PV_DELTA);
    }
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void presentValueHullWhiteSensitivitypayerReceiverParityExplicit() {
    final double[] pvhwsReceiverLong = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_LONG_RECEIVER, HW_MULTICURVES);
    final double[] pvhwsPayerShort = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_SHORT_PAYER, HW_MULTICURVES);
    for (int loophw = 0; loophw < pvhwsReceiverLong.length; loophw++) {
      assertEquals("Swaption physical - Hull-White - present value - payer/receiver/swap parity", 0, pvhwsReceiverLong[loophw] + pvhwsPayerShort[loophw], TOLERANCE_PV_DELTA);
    }
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_HW_C.calculateSensitivity(SWAPTION_SHORT_RECEIVER, HW_MULTICURVES, HW_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_HW_FDC.calculateSensitivity(SWAPTION_SHORT_RECEIVER, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void presentValueCurveSensitivityLongShortParityExplicit() {
    final MultipleCurrencyMulticurveSensitivity pvhwsLong = METHOD_HW.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvhwsShort = METHOD_HW.presentValueCurveSensitivity(SWAPTION_SHORT_PAYER, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("Swaption physical - Hull-White - presentValueCurveSensitivity - long/short parity", pvhwsLong, pvhwsShort.multipliedBy(-1.0), TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void presentValueCurveSensitivityPayerReceiverParityExplicit() {
    final MultipleCurrencyMulticurveSensitivity pvhwsReceiverLong = METHOD_HW.presentValueCurveSensitivity(SWAPTION_LONG_RECEIVER, HW_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvhwsPayerShort = METHOD_HW.presentValueCurveSensitivity(SWAPTION_SHORT_PAYER, HW_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvSwap = SWAP_RECEIVER.accept(PVCSDC, MULTICURVES);
    AssertSensivityObjects.assertEquals("Swaption physical - Hull-White - presentValueCurveSensitivity - payer/receiver/swap parity", pvSwap.cleaned(TOLERANCE_PV_DELTA),
        pvhwsReceiverLong.plus(pvhwsPayerShort).cleaned(TOLERANCE_PV_DELTA), TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the curve sensitivity in Monte Carlo approach.
   */
  public void presentValueCurveSensitivityMonteCarlo() {
    double toleranceDelta = 1.0E+6; // 100 USD by bp
    MultipleCurrencyMulticurveSensitivity pvcsExplicit = METHOD_HW.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES).cleaned(TOLERANCE_PV_DELTA);
    HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    MultipleCurrencyMulticurveSensitivity pvcsMC = methodMC.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, CUR, HW_MULTICURVES).cleaned(TOLERANCE_PV_DELTA);
    AssertSensivityObjects.assertEquals("Swaption physical - Hull-White - presentValueCurveSensitivity - payer/receiver/swap parity", pvcsExplicit, pvcsMC, toleranceDelta);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;
    MultipleCurrencyAmount pvPayerLongExplicit = MultipleCurrencyAmount.of(CUR, 0.0);
    MultipleCurrencyAmount pvPayerLongIntegration = MultipleCurrencyAmount.of(CUR, 0.0);
    MultipleCurrencyAmount pvPayerLongApproximation = MultipleCurrencyAmount.of(CUR, 0.0);
    @SuppressWarnings("unused")
    MultipleCurrencyAmount pvPayerLongMC = MultipleCurrencyAmount.of(CUR, 0.0);
    double[] pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    MultipleCurrencyMulticurveSensitivity pvcs = METHOD_HW.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: HW price: 19-Nov-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 380 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " HW sensitivity swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: HW sensitivity (3): 19-Nov-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 430 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs = METHOD_HW.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve sensitivity swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: curve sensitivity (40): 19-Nov-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 855 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
      pvcs = METHOD_HW.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
      pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " price/delta/vega swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: present value/delta/vega: 19-Nov-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1730 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: HW numerical integration: 19-Nov-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1700 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongApproximation = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White approximation method: " + (endTime - startTime) + " ms");
    // Performance note: HW approximation: 19-Nov-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 250 ms for 10000 swaptions.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongMC = METHOD_HW_MONTECARLO.presentValue(SWAPTION_LONG_PAYER, CUR, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White Monte Carlo method (" + NB_PATH + " paths): " + (endTime - startTime) + " ms");
    // Performance note: HW approximation: 18-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 9200 ms for 1000 swaptions (12500 paths).

    final double difference = pvPayerLongExplicit.getAmount(CUR) - pvPayerLongIntegration.getAmount(CUR);
    final double difference2 = pvPayerLongExplicit.getAmount(CUR) - pvPayerLongApproximation.getAmount(CUR);
    //      double difference3 = pvPayerLongExplicit.getAmount(CUR) - pvPayerLongMC.getAmount(CUR);
    System.out.println("Difference explicit-integration: " + difference);
    System.out.println("Difference explicit-approximation: " + difference2);
    //      System.out.println("Difference explicit-Monte Carlo: " + difference3);
    System.out.println("Curve sensitivity: " + pvcs.toString());
    System.out.println("HW sensitivity: " + Arrays.toString(pvhws));
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceCurveSensitivity() {
    long startTime, endTime;
    final int nbTest = 25;
    MultipleCurrencyAmount pvMC = MultipleCurrencyAmount.of(CUR, 0.0);
    MultipleCurrencyMulticurveSensitivity pvcsExplicit = METHOD_HW.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, HW_MULTICURVES);
    MultipleCurrencyMulticurveSensitivity pvcsMC = pvcsExplicit;
    HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC = METHOD_HW_MONTECARLO.presentValue(SWAPTION_LONG_PAYER, CUR, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White Monte Carlo method (" + NB_PATH + " paths): " + (endTime - startTime) + " ms / price:" + pvMC.toString());
    // Performance note: HW approximation: 03-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 250 ms for 25 swaptions (12500 paths).
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcsMC = methodMC.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, CUR, HW_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve sensitivity swaption Hull-White MC method: (" + NB_PATH + " paths) " + (endTime - startTime) + " ms / risk:" + pvcsMC.toString());
    // Performance note: curve sensitivity (40): 03-Dec-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 600 ms for 25 swaptions (12500 paths).

  }

}
