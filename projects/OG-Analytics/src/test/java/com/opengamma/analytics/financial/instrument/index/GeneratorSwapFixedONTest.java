/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the construction of Fixed/ON (OIS) generators.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorSwapFixedONTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON INDEX_FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final String USD_NAME = "USD1YFEDFUND";
  private static final Period USD_PERIOD = Period.ofMonths(12);
  private static final DayCount USD_DAYCOUNT_FIXED = INDEX_FEDFUND.getDayCount();
  private static final BusinessDayConvention USD_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean USD_IS_EOM = true;
  private static final int USD_SPOT_LAG = 2;

  private static final GeneratorSwapFixedON USD_GENERATOR_OIS = new GeneratorSwapFixedON(USD_NAME, INDEX_FEDFUND, USD_PERIOD, USD_DAYCOUNT_FIXED, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new GeneratorSwapFixedON(null, INDEX_FEDFUND, USD_PERIOD, USD_DAYCOUNT_FIXED, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPeriod() {
    new GeneratorSwapFixedON(USD_NAME, INDEX_FEDFUND, null, USD_DAYCOUNT_FIXED, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    new GeneratorSwapFixedON(USD_NAME, INDEX_FEDFUND, USD_PERIOD, null, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorSwapFixedON(USD_NAME, INDEX_FEDFUND, USD_PERIOD, USD_DAYCOUNT_FIXED, null, USD_IS_EOM, USD_SPOT_LAG, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new GeneratorSwapFixedON(USD_NAME, null, USD_PERIOD, USD_DAYCOUNT_FIXED, USD_BUSINESS_DAY, USD_IS_EOM, USD_SPOT_LAG, NYC);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals("Generator OIS: getter", USD_NAME, USD_GENERATOR_OIS.getName());
    assertEquals("Generator OIS: getter", USD_PERIOD, USD_GENERATOR_OIS.getLegsPeriod());
    assertEquals("Generator OIS: getter", USD_DAYCOUNT_FIXED, USD_GENERATOR_OIS.getFixedLegDayCount());
    assertEquals("Generator OIS: getter", USD_BUSINESS_DAY, USD_GENERATOR_OIS.getBusinessDayConvention());
    assertEquals("Generator OIS: getter", USD_IS_EOM, USD_GENERATOR_OIS.isEndOfMonth());
    assertEquals("Generator OIS: getter", INDEX_FEDFUND, USD_GENERATOR_OIS.getIndex());
    assertEquals("Generator OIS: getter", true, USD_GENERATOR_OIS.isStubShort());
    assertEquals("Generator OIS: getter", true, USD_GENERATOR_OIS.isFromEnd());
    assertEquals("Generator OIS: getter", USD_SPOT_LAG, USD_GENERATOR_OIS.getSpotLag());
  }

  @Test
  /**
   * Tests the standard USD OIS builders.
   */
  public void usdStandard() {
    final GeneratorSwapFixedON usdStandard = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
    assertEquals("Generator OIS: standard", USD_NAME, usdStandard.getName());
    assertEquals("Generator OIS: standard", USD_PERIOD, usdStandard.getLegsPeriod());
    assertEquals("Generator OIS: standard", USD_DAYCOUNT_FIXED, usdStandard.getFixedLegDayCount());
    assertEquals("Generator OIS: standard", USD_BUSINESS_DAY, usdStandard.getBusinessDayConvention());
    assertEquals("Generator OIS: standard", USD_IS_EOM, usdStandard.isEndOfMonth());
    assertEquals("Generator OIS: standard", INDEX_FEDFUND, usdStandard.getIndex());
    assertEquals("Generator OIS: standard", true, usdStandard.isStubShort());
    assertEquals("Generator OIS: standard", true, usdStandard.isFromEnd());
  }

}
