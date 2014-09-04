/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the generator of legs based on Ibor compounding.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorLegIborCompoundingTest {

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final IborIndex USDLIBOR1M = IndexIborMaster.getInstance().getIndex("USDLIBOR1M");
  private static final Currency USD = Currency.USD;
  
  private static final String NAME = "LEG_USDLIBOR1MCMP3M";
  private static final int OFFSET_SPOT = 2;
  private static final int OFFSET_PAYMENT = 0;
  private static final Period P3M = Period.ofMonths(3);
  private static final CompoundingMethod CMP_FLAT = CompoundingMethod.FLAT;
//  private static final CompoundingMethod CMP_STRAIGHT = CompoundingMethod.STRAIGHT;
  private static final GeneratorLegIborCompounding GENERATOR = new GeneratorLegIborCompounding(NAME, USD, USDLIBOR1M, 
      P3M, CMP_FLAT, OFFSET_SPOT, OFFSET_PAYMENT, BusinessDayConventions.MODIFIED_FOLLOWING, true, 
      StubType.SHORT_START, false, NYC, NYC);
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new GeneratorLegIborCompounding(null, USD, USDLIBOR1M, P3M, CompoundingMethod.FLAT, OFFSET_SPOT, OFFSET_PAYMENT, 
        BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new GeneratorLegIborCompounding(NAME, null, USDLIBOR1M, P3M, CompoundingMethod.FLAT, OFFSET_SPOT, OFFSET_PAYMENT, 
        BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new GeneratorLegIborCompounding(NAME, USD, null, P3M, CompoundingMethod.FLAT, OFFSET_SPOT, OFFSET_PAYMENT, 
        BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTenor() {
    new GeneratorLegIborCompounding(NAME, USD, USDLIBOR1M, null, CompoundingMethod.FLAT, OFFSET_SPOT, OFFSET_PAYMENT, 
        BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCompoundingMethod() {
    new GeneratorLegIborCompounding(NAME, USD, USDLIBOR1M, P3M, null, OFFSET_SPOT, OFFSET_PAYMENT, 
        BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorLegIborCompounding(NAME, USD, USDLIBOR1M, P3M, CompoundingMethod.FLAT, OFFSET_SPOT, OFFSET_PAYMENT, 
        null, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStub() {
    new GeneratorLegIborCompounding(NAME, USD, USDLIBOR1M, P3M, CompoundingMethod.FLAT, OFFSET_SPOT, OFFSET_PAYMENT, 
        BusinessDayConventions.MODIFIED_FOLLOWING, true, null, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendarIndex() {
    new GeneratorLegIborCompounding(NAME, USD, USDLIBOR1M, P3M, CompoundingMethod.FLAT, OFFSET_SPOT, OFFSET_PAYMENT, 
        BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, null, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendarPayment() {
    new GeneratorLegIborCompounding(NAME, USD, USDLIBOR1M, P3M, CompoundingMethod.FLAT, OFFSET_SPOT, OFFSET_PAYMENT, 
        BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, null);
  }
  
  @Test
  public void getter() {
    assertEquals("GeneratorLegIborCompounding: getter", NAME, GENERATOR.getName());
    assertEquals("GeneratorLegIborCompounding: getter", USD, GENERATOR.getCurrency());
    assertEquals("GeneratorLegIborCompounding: getter", USDLIBOR1M, GENERATOR.getIndexIbor());
    assertEquals("GeneratorLegIborCompounding: getter", P3M, GENERATOR.getPaymentPeriod());
    assertEquals("GeneratorLegIborCompounding: getter", CMP_FLAT, GENERATOR.getCompoundingMethod());
    assertEquals("GeneratorLegIborCompounding: getter", USDLIBOR1M, GENERATOR.getIndexIbor());
    assertEquals("GeneratorLegIborCompounding: getter", OFFSET_SPOT, GENERATOR.getSpotOffset());
    assertEquals("GeneratorLegIborCompounding: getter", OFFSET_PAYMENT, GENERATOR.getPaymentOffset());
    assertEquals("GeneratorLegIborCompounding: getter", BusinessDayConventions.MODIFIED_FOLLOWING, GENERATOR.getBusinessDayConvention());
    assertEquals("GeneratorLegIborCompounding: getter", true, GENERATOR.isEndOfMonth());
    assertEquals("GeneratorLegIborCompounding: getter", StubType.SHORT_START, GENERATOR.getStubType());
    assertEquals("GeneratorLegIborCompounding: getter", false, GENERATOR.isExchangeNotional());
    assertEquals("GeneratorLegIborCompounding: getter", NYC, GENERATOR.getIndexCalendar());
    assertEquals("GeneratorLegIborCompounding: getter", NYC, GENERATOR.getPaymentCalendar());
  }
  
}
