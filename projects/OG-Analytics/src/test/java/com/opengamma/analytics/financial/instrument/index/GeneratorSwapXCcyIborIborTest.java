/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorSwapXCcyIborIborTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR3M = IBOR_MASTER.getIndex("USDLIBOR3M");
  private static final IborIndex EURIBOR3M = IBOR_MASTER.getIndex("EURIBOR3M");
  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR3M, USDLIBOR3M, TARGET, NYC);

  @Test
  /**
   * Tests the getter for the swap generator.
   */
  public void getter() {
    assertEquals("GeneratorSwapIborIbor: getter", EURIBOR3M, EURIBOR3MUSDLIBOR3M.getIborIndex1());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR3M, EURIBOR3MUSDLIBOR3M.getIborIndex2());
    assertTrue("GeneratorSwapIborIbor: getter", EURIBOR3MUSDLIBOR3M.getName().equals("EURIBOR3MUSDLIBOR3M"));
    assertEquals("GeneratorSwapIborIbor: getter", EURIBOR3M.getBusinessDayConvention(), EURIBOR3MUSDLIBOR3M.getBusinessDayConvention());
    assertEquals("GeneratorSwapIborIbor: getter", EURIBOR3M.getSpotLag(), EURIBOR3MUSDLIBOR3M.getSpotLag());
    assertTrue("GeneratorSwapIborIbor: getter", EURIBOR3M.isEndOfMonth() == EURIBOR3MUSDLIBOR3M.isEndOfMonth());
  }

  @Test
  /**
   * Tests the constructor with business day convention and end-of-month.
   */
  public void constructor() {
    final GeneratorSwapXCcyIborIbor generator2 = new GeneratorSwapXCcyIborIbor("Generator 2", EURIBOR3M, USDLIBOR3M, BusinessDayConventions.FOLLOWING, false, 1, NYC, NYC);
    assertEquals("GeneratorSwapIborIbor: getter", EURIBOR3M, EURIBOR3MUSDLIBOR3M.getIborIndex1());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR3M, EURIBOR3MUSDLIBOR3M.getIborIndex2());
    assertTrue("GeneratorSwapIborIbor: getter", generator2.getName().equals("Generator 2"));
    assertEquals("GeneratorSwapIborIbor: getter", BusinessDayConventions.FOLLOWING, generator2.getBusinessDayConvention());
    assertTrue("GeneratorSwapIborIbor: getter", generator2.isEndOfMonth() == false);
    assertEquals("GeneratorSwapIborIbor: getter", generator2.getSpotLag(), 1);
  }

  @Test
  public void generateInstrument() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    final Period tenor = Period.ofMonths(6);
    final double spread = -0.0050;
    final double notional = 12345;
    final double fxRateEURUSD = 1.25;
    final FXMatrix fxMatrix = new FXMatrix(EURIBOR3M.getCurrency(), USDLIBOR3M.getCurrency(), fxRateEURUSD);
    final GeneratorAttributeFX attribute = new GeneratorAttributeFX(tenor, fxMatrix);
    final SwapXCcyIborIborDefinition insGenerated = EURIBOR3MUSDLIBOR3M.generateInstrument(referenceDate, spread, notional, attribute);
    final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(referenceDate, EURIBOR3MUSDLIBOR3M.getSpotLag(), NYC);
    final SwapXCcyIborIborDefinition insExpected = SwapXCcyIborIborDefinition.from(settleDate, tenor, EURIBOR3MUSDLIBOR3M, notional, notional * fxRateEURUSD, spread, true, NYC, NYC);
    assertEquals("Generator Deposit: generate instrument", insExpected, insGenerated);
  }

}
