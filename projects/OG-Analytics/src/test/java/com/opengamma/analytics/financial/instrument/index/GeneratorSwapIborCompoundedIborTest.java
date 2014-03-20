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

import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorSwapIborCompoundedIborTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR3M = IBOR_MASTER.getIndex("USDLIBOR3M");
  private static final IborIndex USDLIBOR6M = IBOR_MASTER.getIndex("USDLIBOR6M");
  private static final Period CMP_PERIOD = Period.ofMonths(6);
  private static final String NAME = "USD6MLIBOR3MLIBOR6M";
  private static final GeneratorSwapIborCompoundingIbor USD6MLIBOR3MLIBOR6M = new GeneratorSwapIborCompoundingIbor(NAME, USDLIBOR3M,
      CMP_PERIOD, USDLIBOR6M, NYC, NYC);

  @Test
  /**
   * Tests the getter for the swap generator.
   */
  public void getter() {
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR3M, USD6MLIBOR3MLIBOR6M.getIborIndex1());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR6M, USD6MLIBOR3MLIBOR6M.getIborIndex2());
    assertTrue("GeneratorSwapIborIbor: getter", USD6MLIBOR3MLIBOR6M.getName().equals(NAME));
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR6M.getBusinessDayConvention(), USD6MLIBOR3MLIBOR6M.getBusinessDayConvention());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR6M.getSpotLag(), USD6MLIBOR3MLIBOR6M.getSpotLag());
    assertTrue("GeneratorSwapIborIbor: getter", USDLIBOR6M.isEndOfMonth() == USD6MLIBOR3MLIBOR6M.isEndOfMonth());
    assertEquals("GeneratorSwapIborIbor: getter", CMP_PERIOD, USD6MLIBOR3MLIBOR6M.getCompoundingPeriod1());
  }

  @Test
  /**
   * Tests the constructor with business day convention and end-of-month.
   */
  public void constructor() {
    final GeneratorSwapIborCompoundingIbor generator2 = new GeneratorSwapIborCompoundingIbor("Generator 2", USDLIBOR3M, CMP_PERIOD, USDLIBOR6M,
        BusinessDayConventions.FOLLOWING, false, 1, NYC, NYC);
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR3M, generator2.getIborIndex1());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR6M, generator2.getIborIndex2());
    assertTrue("GeneratorSwapIborIbor: getter", generator2.getName().equals("Generator 2"));
    assertEquals("GeneratorSwapIborIbor: getter", BusinessDayConventions.FOLLOWING, generator2.getBusinessDayConvention());
    assertTrue("GeneratorSwapIborIbor: getter", generator2.isEndOfMonth() == false);
    assertEquals("GeneratorSwapIborIbor: getter", generator2.getSpotLag(), 1);
    assertEquals("GeneratorSwapIborIbor: getter", CMP_PERIOD, generator2.getCompoundingPeriod1());
  }

}
