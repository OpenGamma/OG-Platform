/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.generator.GeneratorSwapTestsMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveLeastSquareCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.LiborMarketModelDisplacedDiffusionTestsDataSet;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the calibration engine for LMM DD calibration at best to European swaptions. The calibration is obtained 
 * by changing volatility parameters with a common multiplicative factor and displacement with a common additive term.
 */
public class SwaptionPhysicalLMMDDSuccessiveLeastSquareCalibrationObjectiveTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapTestsMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();

  private static final int[] SWAP_TENOR_YEAR = {1, 2, 3, 4, 5};
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final int EXPIRY_TENOR = 5;
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(EXPIRY_TENOR), USDLIBOR3M);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, USDLIBOR3M.getSpotLag(), NYC);

  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final double NOTIONAL = 100000000; //100m
  private static final boolean IS_LONG = true;

  private static final double[] MONEYNESS = new double[] {-0.01, 0.0, 0.01};
  private static final int NB_STRIKE = MONEYNESS.length;
  private static final SwapFixedIborDefinition[][] SWAP_PAYER_DEFINITION = new SwapFixedIborDefinition[SWAP_TENOR_YEAR.length][NB_STRIKE];
  private static final SwaptionPhysicalFixedIborDefinition[][] SWAPTION_LONG_PAYER_DEFINITION = new SwaptionPhysicalFixedIborDefinition[SWAP_TENOR_YEAR.length][NB_STRIKE];

  static {
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
        SWAP_PAYER_DEFINITION[loopexp][loopstrike] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, Period.ofYears(SWAP_TENOR_YEAR[loopexp]), USD6MLIBOR3M, NOTIONAL, RATE + MONEYNESS[loopstrike],
            FIXED_IS_PAYER);
        SWAPTION_LONG_PAYER_DEFINITION[loopexp][loopstrike] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION[loopexp][loopstrike], IS_LONG);
      }
    }
  }

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves1Names();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR3();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final SwaptionPhysicalFixedIbor[][] SWAPTION_LONG_PAYER = new SwaptionPhysicalFixedIbor[SWAP_TENOR_YEAR.length][NB_STRIKE];
  static {
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
        SWAPTION_LONG_PAYER[loopexp][loopstrike] = SWAPTION_LONG_PAYER_DEFINITION[loopexp][loopstrike].toDerivative(REFERENCE_DATE, CURVE_NAMES);
      }
    }
  }
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();
  private static final double TOLERANCE_LS = 1.0E+4; // The fit is not exact.

  @Test
  /**
   * Tests the correctness of LMM DD calibration to swaptions with SABR price.
   */
  public void calibration() {
    LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION[SWAP_TENOR_YEAR.length - 1][NB_STRIKE - 1].getIborLeg());
    SwaptionPhysicalLMMDDSuccessiveLeastSquareCalibrationObjective objective = new SwaptionPhysicalLMMDDSuccessiveLeastSquareCalibrationObjective(lmmParameters);
    SuccessiveLeastSquareCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveLeastSquareCalibrationEngine(objective, NB_STRIKE);
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[loopexp], METHOD_SABR);
    }
    calibrationEngine.calibrate(SABR_BUNDLE);
    CurrencyAmount[][] pvSabr = new CurrencyAmount[SWAP_TENOR_YEAR.length][NB_STRIKE];
    CurrencyAmount[][] pvLmm = new CurrencyAmount[SWAP_TENOR_YEAR.length][NB_STRIKE];
    double[][] pvDiff = new double[SWAP_TENOR_YEAR.length][NB_STRIKE];
    double[] pvDiffTot = new double[SWAP_TENOR_YEAR.length];
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      for (int loopstrike = 0; loopstrike < NB_STRIKE; loopstrike++) {
        pvSabr[loopexp][loopstrike] = METHOD_SABR.presentValue(SWAPTION_LONG_PAYER[loopexp][loopstrike], SABR_BUNDLE);
        pvLmm[loopexp][loopstrike] = METHOD_LMM.presentValue(SWAPTION_LONG_PAYER[loopexp][loopstrike], objective.getLMMBundle());
        pvDiff[loopexp][loopstrike] = pvSabr[loopexp][loopstrike].getAmount() - pvLmm[loopexp][loopstrike].getAmount();
        pvDiffTot[loopexp] += pvDiff[loopexp][loopstrike];
      }
      assertEquals("LMM calibration least-square: swaption " + loopexp, 0, pvDiffTot[loopexp], TOLERANCE_LS);
    }
  }

}
