/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;

public class GeneratorSwapXCcyIborIborTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IndexIborTestsMaster IBOR_MASTER = IndexIborTestsMaster.getInstance();
  private static final IborIndex USDLIBOR3M = IBOR_MASTER.getIndex("USDLIBOR3M", NYC);
  private static final IborIndex EURIBOR3M = IBOR_MASTER.getIndex("EURIBOR3M", TARGET);
  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR3M, USDLIBOR3M);

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
    GeneratorSwapXCcyIborIbor generator2 = new GeneratorSwapXCcyIborIbor("Generator 2", EURIBOR3M, USDLIBOR3M, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false, 1);
    assertEquals("GeneratorSwapIborIbor: getter", EURIBOR3M, EURIBOR3MUSDLIBOR3M.getIborIndex1());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR3M, EURIBOR3MUSDLIBOR3M.getIborIndex2());
    assertTrue("GeneratorSwapIborIbor: getter", generator2.getName().equals("Generator 2"));
    assertEquals("GeneratorSwapIborIbor: getter", BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), generator2.getBusinessDayConvention());
    assertTrue("GeneratorSwapIborIbor: getter", generator2.isEndOfMonth() == false);
    assertEquals("GeneratorSwapIborIbor: getter", generator2.getSpotLag(), 1);
  }

  @Test
  public void generateInstrument() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 7, 17);
    Period tenor = Period.ofMonths(6);
    double spread = -0.0050;
    double notional = 12345;
    double fxRateUSDEUR = 0.75;
    FXMatrix fxMatrix = new FXMatrix(USDLIBOR3M.getCurrency(), EURIBOR3M.getCurrency(), fxRateUSDEUR);
    SwapXCcyIborIborDefinition insGenerated = EURIBOR3MUSDLIBOR3M.generateInstrument(referenceDate, tenor, spread, notional, fxRateUSDEUR);
    SwapXCcyIborIborDefinition insGenerated2 = EURIBOR3MUSDLIBOR3M.generateInstrument(referenceDate, tenor, spread, notional, fxMatrix);
    ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(referenceDate, EURIBOR3MUSDLIBOR3M.getSpotLag(), NYC);
    SwapXCcyIborIborDefinition insExpected = SwapXCcyIborIborDefinition.from(settleDate, tenor, EURIBOR3MUSDLIBOR3M, fxRateUSDEUR * notional, notional, spread, true);
    assertEquals("Generator Deposit: generate instrument", insExpected, insGenerated);
    assertEquals("Generator Deposit: generate instrument", insExpected, insGenerated2);
  }

}
