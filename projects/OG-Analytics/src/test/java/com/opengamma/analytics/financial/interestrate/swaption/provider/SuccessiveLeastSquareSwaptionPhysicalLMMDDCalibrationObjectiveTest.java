/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBasketMethod;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetLiborMarketModelDisplacedDiffusion;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.method.CalibrationEngineWithCalculators;
import com.opengamma.analytics.financial.provider.method.SuccessiveLeastSquareLMMDDCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveLeastSquareLMMDDCalibrationObjective;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the calibration engine for LMM DD calibration at best to European swaptions. The calibration is obtained
 * by changing volatility parameters with a common multiplicative factor and displacement with a common additive term.
 */
@Test(groups = TestGroup.UNIT)
public class SuccessiveLeastSquareSwaptionPhysicalLMMDDCalibrationObjectiveTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER, EUR1YEURIBOR6M);

  private static final int[] SWAP_TENOR_YEAR = {1, 2, 3, 4, 5 };
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final int EXPIRY_TENOR = 5;
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(EXPIRY_TENOR), EURIBOR6M, TARGET);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, EURIBOR6M.getSpotLag(), TARGET);

  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final double NOTIONAL = 100000000; //100m
  private static final boolean IS_LONG = true;
  private static final int SWAP_TENOR = 5;

  private static final double[] MONEYNESS = new double[] {-0.0150, -0.0075, -0.0025, 0.0025, 0.0075, 0.0150 };
  // {-0.01, 0.0, 0.01}, {-0.0100, -0.0025, 0.0025, 0.0100}, {-0.0100, -0.0025, 0.00, 0.0025, 0.0100}, {-0.0150, -0.0075, -0.0025, 0.0025, 0.0075, 0.0150}
  private static final int NB_STRIKE = MONEYNESS.length;

  private static final double[] AMORTIZATION = new double[SWAP_TENOR];
  private static final SwaptionPhysicalFixedIbor SWAPTION_AMORTIZED;
  private static final SwapFixedIborDefinition SWAP_DEFINITION;
  static {
    for (int loopp = 0; loopp < SWAP_TENOR; loopp++) {
      AMORTIZATION[loopp] = 1.0 - 1.0 * loopp / SWAP_TENOR;
    }
    SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, Period.ofYears(SWAP_TENOR), EUR1YEURIBOR6M, NOTIONAL, RATE, FIXED_IS_PAYER);
    final CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[SWAP_TENOR];
    final AnnuityCouponFixedDefinition legFixed = SWAP_DEFINITION.getFixedLeg();
    final CouponIborDefinition[] cpnIbor = new CouponIborDefinition[2 * SWAP_TENOR];
    final AnnuityDefinition<? extends PaymentDefinition> legIbor = SWAP_DEFINITION.getSecondLeg();
    for (int loopexp = 0; loopexp < SWAP_TENOR; loopexp++) {
      cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * AMORTIZATION[loopexp]);
      cpnIbor[2 * loopexp] = ((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp))
          .withNotional(((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp)).getNotional() * AMORTIZATION[loopexp]);
      cpnIbor[2 * loopexp + 1] = ((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp + 1)).getNotional()
          * AMORTIZATION[loopexp]);
    }
    final SwapFixedIborDefinition swapAmortizedDefinition = new SwapFixedIborDefinition(new AnnuityCouponFixedDefinition(cpnFixed, TARGET), new AnnuityCouponIborDefinition(cpnIbor, EURIBOR6M, TARGET));
    final SwaptionPhysicalFixedIborDefinition swaptionAmortizedDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapAmortizedDefinition, FIXED_IS_PAYER, IS_LONG);
    SWAPTION_AMORTIZED = swaptionAmortizedDefinition.toDerivative(REFERENCE_DATE);
  }

  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();
  private static final LiborMarketModelDisplacedDiffusionParameters LMM_PARAM_INIT = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE, SWAP_DEFINITION.getIborLeg());
  private static final SwaptionPhysicalFixedIborSABRLMMLeastSquareMethod METHOD_CALIBRATION = new SwaptionPhysicalFixedIborSABRLMMLeastSquareMethod(MONEYNESS, LMM_PARAM_INIT);
  private static final SwaptionPhysicalFixedIborBasketMethod METHOD_BASKET = SwaptionPhysicalFixedIborBasketMethod.getInstance();

  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();

  private static final double TOLERANCE_LS = 1.0E+5; // The fit is not exact.
  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  /**
   * Tests the correctness of LMM DD calibration to swaptions with SABR price.
   */
  public void calibration() {
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = LMM_PARAM_INIT.copy();
    final SuccessiveLeastSquareLMMDDCalibrationObjective objective = new SuccessiveLeastSquareLMMDDCalibrationObjective(lmmParameters, EUR);
    final CalibrationEngineWithCalculators<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveLeastSquareLMMDDCalibrationEngine<>(objective, NB_STRIKE);
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = METHOD_BASKET.calibrationBasketFixedLegPeriod(SWAPTION_AMORTIZED, MONEYNESS);
    calibrationEngine.addInstrument(swaptionCalibration, PVSSC);
    calibrationEngine.calibrate(SABR_MULTICURVES);
    final MultipleCurrencyAmount[][] pvSabr = new MultipleCurrencyAmount[SWAP_TENOR_YEAR.length][NB_STRIKE];
    final MultipleCurrencyAmount[][] pvLmm = new MultipleCurrencyAmount[SWAP_TENOR_YEAR.length][NB_STRIKE];
    final double[][] pvDiff = new double[SWAP_TENOR_YEAR.length][NB_STRIKE];
    final double[] pvDiffTot = new double[SWAP_TENOR_YEAR.length];
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
        pvSabr[loopexp][loopstrike] = METHOD_SABR.presentValue(swaptionCalibration[loopexp * NB_STRIKE + loopstrike], SABR_MULTICURVES);
        pvLmm[loopexp][loopstrike] = METHOD_LMM.presentValue(swaptionCalibration[loopexp * NB_STRIKE + loopstrike], objective.getLMMProvider());
        pvDiff[loopexp][loopstrike] = pvSabr[loopexp][loopstrike].getAmount(EUR) - pvLmm[loopexp][loopstrike].getAmount(EUR);
        pvDiffTot[loopexp] += pvDiff[loopexp][loopstrike];
      }
      assertEquals("LMM calibration least-square: swaption " + loopexp, 0, pvDiffTot[loopexp], TOLERANCE_LS);
    }
    // Comparison with method
    final MultipleCurrencyAmount pvDirect = METHOD_LMM.presentValue(SWAPTION_AMORTIZED, objective.getLMMProvider());
    final MultipleCurrencyAmount pvMethod = METHOD_CALIBRATION.presentValue(SWAPTION_AMORTIZED, SABR_MULTICURVES);
    assertEquals("LMM calibration least-square: swaption ", pvDirect.getAmount(EUR), pvMethod.getAmount(EUR), TOLERANCE_PV);
  }

}
