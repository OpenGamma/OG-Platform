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

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
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
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderLMMDDCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderLMMDDCalibrationObjective;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the calibration engine for LMM DD calibration to European swaptions (by calibrating volatility parameters with a common multiplicative factor).
 */
@Test(groups = TestGroup.UNIT)
public class SuccessiveRootFinderSwaptionPhysicalLMMDDCalibrationObjectiveTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", CALENDAR);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER, EUR1YEURIBOR3M);

  // Swaption description
  private static final boolean IS_LONG = true;
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final int[] SWAP_TENOR_YEAR = {1, 2, 3, 4, 5 };
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final int EXPIRY_TENOR = 5;
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(EXPIRY_TENOR), EURIBOR3M, CALENDAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, EURIBOR3M.getSpotLag(), CALENDAR);
  private static final SwapFixedIborDefinition[] SWAP_PAYER_DEFINITION = new SwapFixedIborDefinition[SWAP_TENOR_YEAR.length];
  private static final SwaptionPhysicalFixedIborDefinition[] SWAPTION_LONG_PAYER_DEFINITION = new SwaptionPhysicalFixedIborDefinition[SWAP_TENOR_YEAR.length];
  private static final IndexSwap[] SWAP_INDEX = new IndexSwap[SWAP_TENOR_YEAR.length];
  static {
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      SWAP_INDEX[loopexp] = new IndexSwap(EUR1YEURIBOR3M, Period.ofYears(SWAP_TENOR_YEAR[loopexp]));
      SWAP_PAYER_DEFINITION[loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, SWAP_INDEX[loopexp], NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
      SWAPTION_LONG_PAYER_DEFINITION[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION[loopexp], FIXED_IS_PAYER, IS_LONG);
    }
  }
  // to derivatives
  private static final SwaptionPhysicalFixedIbor[] SWAPTION_LONG_PAYER = new SwaptionPhysicalFixedIbor[SWAP_TENOR_YEAR.length];
  static {
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      SWAPTION_LONG_PAYER[loopexp] = SWAPTION_LONG_PAYER_DEFINITION[loopexp].toDerivative(REFERENCE_DATE);
    }
  }
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  //  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  /**
   * Tests the correctness of Hull-White one factor calibration to swaptions with SABR price.
   */
  public void calibration() {
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION[SWAP_TENOR_YEAR.length - 1].getIborLeg());
    final SuccessiveRootFinderLMMDDCalibrationObjective objective = new SuccessiveRootFinderLMMDDCalibrationObjective(lmmParameters, EUR);
    final CalibrationEngineWithCalculators<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderLMMDDCalibrationEngine<>(objective);
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[loopexp], PVSSC);
    }
    calibrationEngine.calibrate(SABR_MULTICURVES);
    final MultipleCurrencyAmount[] pvSabr = new MultipleCurrencyAmount[SWAP_TENOR_YEAR.length];
    final MultipleCurrencyAmount[] pvLmm = new MultipleCurrencyAmount[SWAP_TENOR_YEAR.length];
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      pvSabr[loopexp] = SWAPTION_LONG_PAYER[loopexp].accept(PVSSC, SABR_MULTICURVES);
      pvLmm[loopexp] = METHOD_LMM.presentValue(SWAPTION_LONG_PAYER[loopexp], objective.getLMMProvider());
      assertEquals("Hull-White calibration: swaption " + loopexp, pvSabr[loopexp].getAmount(EUR), pvLmm[loopexp].getAmount(EUR), TOLERANCE_PV);
    }
  }

  @Test(enabled = false)
  /**
   * Test of performance. In normal testing, "enabled = false".
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      final LiborMarketModelDisplacedDiffusionParameters lmmParameters = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE,
          SWAP_PAYER_DEFINITION[SWAP_TENOR_YEAR.length - 1].getIborLeg());
      final SuccessiveRootFinderLMMDDCalibrationObjective objective = new SuccessiveRootFinderLMMDDCalibrationObjective(lmmParameters, EUR);
      final CalibrationEngineWithCalculators<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderLMMDDCalibrationEngine<>(objective);
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[loopexp], PVSSC);
      }
      calibrationEngine.calibrate(SABR_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM calibration to swaption (5 swaptions): " + (endTime - startTime) + " ms");
    // Performance note: calibration: 12-Dec-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 200 ms for 100 calibration with 5 swaptions.
  }

}
