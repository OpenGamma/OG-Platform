/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrcap.PresentValueSABRCapCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderHullWhiteCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderHullWhiteCalibrationObjective;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the calibration engine for Hull-White one factor calibration to cap/floor.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorHullWhiteCalibrationObjectiveTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  // Cap/floor description
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 9);
  private static final ZonedDateTime MATURITY_DATE = SETTLEMENT_DATE.plusYears(5);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final boolean IS_PAYER = false;
  private static final AnnuityCapFloorIborDefinition CAP_DEFINITION = AnnuityCapFloorIborDefinition.fromWithNoInitialCaplet(SETTLEMENT_DATE, MATURITY_DATE, NOTIONAL, EURIBOR3M, IS_PAYER, STRIKE,
      IS_CAP, CALENDAR);
  // To derivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final SABRCapProviderDiscount SABR_MULTICURVES = new SABRCapProviderDiscount(MULTICURVES, SABR_PARAMETER, EURIBOR3M);

  private static final Annuity<? extends Payment> CAP = CAP_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final PresentValueSABRCapCalculator PVSCC = PresentValueSABRCapCalculator.getInstance();
  private static final CapFloorIborHullWhiteMethod METHOD_CAP_HW = CapFloorIborHullWhiteMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  /**
   * Tests the correctness of Hull-White one factor calibration to swaptions with SABR price.
   */
  public void calibration() {
    final double meanReversion = 0.01;
    final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(meanReversion, new double[] {0.01}, new double[0]);
    final SuccessiveRootFinderHullWhiteCalibrationObjective objective = new SuccessiveRootFinderHullWhiteCalibrationObjective(hwParameters, EUR);
    final SuccessiveRootFinderHullWhiteCalibrationEngine<SABRCapProviderInterface> calibrationEngine = new SuccessiveRootFinderHullWhiteCalibrationEngine<>(objective);
    for (int loopexp = 0; loopexp < CAP.getNumberOfPayments(); loopexp++) {
      calibrationEngine.addInstrument(CAP.getNthPayment(loopexp), PVSCC);
    }
    calibrationEngine.calibrate(SABR_MULTICURVES);
    final MultipleCurrencyAmount[] pvSabr = new MultipleCurrencyAmount[CAP.getNumberOfPayments()];
    final MultipleCurrencyAmount[] pvHw = new MultipleCurrencyAmount[CAP.getNumberOfPayments()];
    for (int loopexp = 0; loopexp < CAP.getNumberOfPayments(); loopexp++) {
      pvSabr[loopexp] = CAP.getNthPayment(loopexp).accept(PVSCC, SABR_MULTICURVES);
      pvHw[loopexp] = METHOD_CAP_HW.presentValue((CapFloorIbor) CAP.getNthPayment(loopexp), objective.getHwProvider());
      assertEquals("Hull-White calibration: cap/floor " + loopexp, pvSabr[loopexp].getAmount(EUR), pvHw[loopexp].getAmount(EUR), TOLERANCE_PV);
    }
  }

}
