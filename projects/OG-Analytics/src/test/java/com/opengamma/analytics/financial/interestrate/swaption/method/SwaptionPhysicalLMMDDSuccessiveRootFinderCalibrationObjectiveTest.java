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

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetLiborMarketModelDisplacedDiffusion;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
 * Tests related to the calibration engine for LMM DD calibration to European swaptions (by calibrating volatility parameters with a common multiplicative factor).
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjectiveTest {
  // Swaption description
  private static final boolean IS_LONG = true;
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final int[] SWAP_TENOR_YEAR = {1, 2, 3, 4, 5};
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final int EXPIRY_TENOR = 5;
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(EXPIRY_TENOR), BUSINESS_DAY, CALENDAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final SwapFixedIborDefinition[] SWAP_PAYER_DEFINITION = new SwapFixedIborDefinition[SWAP_TENOR_YEAR.length];
  private static final SwaptionPhysicalFixedIborDefinition[] SWAPTION_LONG_PAYER_DEFINITION = new SwaptionPhysicalFixedIborDefinition[SWAP_TENOR_YEAR.length];
  private static final IndexSwap[] CMS_INDEX = new IndexSwap[SWAP_TENOR_YEAR.length];
  static {
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      CMS_INDEX[loopexp] = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, Period.ofYears(SWAP_TENOR_YEAR[loopexp]), CALENDAR);
      SWAP_PAYER_DEFINITION[loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX[loopexp], NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
      SWAPTION_LONG_PAYER_DEFINITION[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION[loopexp], FIXED_IS_PAYER, IS_LONG);
    }
  }
  // to derivatives
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final SwaptionPhysicalFixedIbor[] SWAPTION_LONG_PAYER = new SwaptionPhysicalFixedIbor[SWAP_TENOR_YEAR.length];
  static {
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      SWAPTION_LONG_PAYER[loopexp] = SWAPTION_LONG_PAYER_DEFINITION[loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
  }
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();

  @Test
  /**
   * Tests the correctness of Hull-White one factor calibration to swaptions with SABR price.
   */
  public void calibration() {
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION[SWAP_TENOR_YEAR.length - 1].getIborLeg());
    final SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective objective = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective(lmmParameters);
    final SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[loopexp], METHOD_SABR);
    }
    calibrationEngine.calibrate(SABR_BUNDLE);
    final CurrencyAmount[] pvSabr = new CurrencyAmount[SWAP_TENOR_YEAR.length];
    final CurrencyAmount[] pvLmm = new CurrencyAmount[SWAP_TENOR_YEAR.length];
    for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
      pvSabr[loopexp] = METHOD_SABR.presentValue(SWAPTION_LONG_PAYER[loopexp], SABR_BUNDLE);
      pvLmm[loopexp] = METHOD_LMM.presentValue(SWAPTION_LONG_PAYER[loopexp], objective.getLMMBundle());
      assertEquals("Hull-White calibration: swaption " + loopexp, pvSabr[loopexp].getAmount(), pvLmm[loopexp].getAmount(), 1E-2);
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
      final SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective objective = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective(lmmParameters);
      final SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
      for (int loopexp = 0; loopexp < SWAP_TENOR_YEAR.length; loopexp++) {
        calibrationEngine.addInstrument(SWAPTION_LONG_PAYER[loopexp], METHOD_SABR);
      }
      calibrationEngine.calibrate(SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " LMM calibration to swaption (5 swaptions): " + (endTime - startTime) + " ms");
    // Performance note: calibration: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 130 ms for 100 calibration with 5 swaptions.
  }

}
