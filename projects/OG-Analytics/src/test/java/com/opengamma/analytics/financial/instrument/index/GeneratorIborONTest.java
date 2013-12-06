/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the construction of Ibor/ON generators.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorIborONTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final String USD_NAME = "USDLIBOR3MFEDFUND";
  private static final BusinessDayConvention USD_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean USD_IS_EOM = true;
  private static final int USD_SPOT_LAG = 2;

  private static final GeneratorSwapIborON USDLIBOR3MFEDFUND = new GeneratorSwapIborON(USD_NAME, USDLIBOR3M, FEDFUND, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC, NYC);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new GeneratorSwapIborON(null, USDLIBOR3M, FEDFUND, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIbor() {
    new GeneratorSwapIborON(USD_NAME, null, FEDFUND, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullON() {
    new GeneratorSwapIborON(USD_NAME, USDLIBOR3M, null, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusDay() {
    new GeneratorSwapIborON(USD_NAME, USDLIBOR3M, FEDFUND, null, USD_IS_EOM, USD_SPOT_LAG, NYC, NYC);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals("Generator Ibor/ON: getter", USD_NAME, USDLIBOR3MFEDFUND.getName());
    assertEquals("Generator Ibor/ON: getter", USDLIBOR3M, USDLIBOR3MFEDFUND.getIndexIbor());
    assertEquals("Generator Ibor/ON: getter", FEDFUND, USDLIBOR3MFEDFUND.getIndexON());
    assertEquals("Generator Ibor/ON: getter", USD_BUSINESS_DAY, USDLIBOR3MFEDFUND.getBusinessDayConvention());
    assertEquals("Generator Ibor/ON: getter", USD_IS_EOM, USDLIBOR3MFEDFUND.isEndOfMonth());
    assertEquals("Generator Ibor/ON: getter", USD_SPOT_LAG, USDLIBOR3MFEDFUND.getSpotLag());
    assertEquals("Generator Ibor/ON: getter", USD_SPOT_LAG, USDLIBOR3MFEDFUND.getPaymentLag());
  }

}
