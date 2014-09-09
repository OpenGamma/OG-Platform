/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the generator of fixed legs.
 */
public class GeneratorLegFixedTest {

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  
  private static final String NAME = "LEG_USD6M";
  private static final int OFFSET_SPOT = 2;
  private static final int OFFSET_PAYMENT = 0;
  private static final Period P6M = Period.ofMonths(6);
  private static final DayCount DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final StubType STUB_SHORT_START = StubType.SHORT_START;
  private static final BusinessDayConvention BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final GeneratorLegFixed GENERATOR = new GeneratorLegFixed(NAME, USD, OFFSET_SPOT, P6M, DAY_COUNT, 
      BDC, OFFSET_PAYMENT, true, STUB_SHORT_START, false, NYC);
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new GeneratorLegFixed(null, USD, OFFSET_SPOT, P6M, DayCounts.THIRTY_U_360, 
        BusinessDayConventions.MODIFIED_FOLLOWING, OFFSET_PAYMENT, true, StubType.SHORT_START, false, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new GeneratorLegFixed(NAME, null, OFFSET_SPOT, P6M, DayCounts.THIRTY_U_360, 
        BusinessDayConventions.MODIFIED_FOLLOWING, OFFSET_PAYMENT, true, StubType.SHORT_START, false, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTenor() {
    new GeneratorLegFixed(NAME, USD, OFFSET_SPOT, null, DayCounts.THIRTY_U_360, 
        BusinessDayConventions.MODIFIED_FOLLOWING, OFFSET_PAYMENT, true, StubType.SHORT_START, false, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    new GeneratorLegFixed(NAME, USD, OFFSET_SPOT, P6M, null,  
        BusinessDayConventions.MODIFIED_FOLLOWING, OFFSET_PAYMENT, true, StubType.SHORT_START, false, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorLegFixed(NAME, USD, OFFSET_SPOT, P6M, DayCounts.THIRTY_U_360, 
        null, OFFSET_PAYMENT, true, StubType.SHORT_START, false, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStub() {
    new GeneratorLegFixed(NAME, USD, OFFSET_SPOT, P6M, DayCounts.THIRTY_U_360, 
        BusinessDayConventions.MODIFIED_FOLLOWING, OFFSET_PAYMENT, true, null, false, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendarPayment() {
    new GeneratorLegFixed(NAME, USD, OFFSET_SPOT, P6M, DayCounts.THIRTY_U_360, 
        BusinessDayConventions.MODIFIED_FOLLOWING, OFFSET_PAYMENT, true, StubType.SHORT_START, false, null);
  }
  
  @Test
  public void getter() {
    assertEquals("GeneratorLegIborCompounding: getter", NAME, GENERATOR.getName());
    assertEquals("GeneratorLegIborCompounding: getter", USD, GENERATOR.getCurrency());
    assertEquals("GeneratorLegIborCompounding: getter", OFFSET_SPOT, GENERATOR.getSpotOffset());
    assertEquals("GeneratorLegIborCompounding: getter", P6M, GENERATOR.getPaymentPeriod());
    assertEquals("GeneratorLegIborCompounding: getter", DAY_COUNT, GENERATOR.getDayCount());
    assertEquals("GeneratorLegIborCompounding: getter", BDC, GENERATOR.getBusinessDayConvention());
    assertEquals("GeneratorLegIborCompounding: getter", OFFSET_PAYMENT, GENERATOR.getPaymentOffset());
    assertEquals("GeneratorLegIborCompounding: getter", true, GENERATOR.isEndOfMonth());
    assertEquals("GeneratorLegIborCompounding: getter", STUB_SHORT_START, GENERATOR.getStubType());
    assertEquals("GeneratorLegIborCompounding: getter", false, GENERATOR.isExchangeNotional());
    assertEquals("GeneratorLegIborCompounding: getter", NYC, GENERATOR.getPaymentCalendar());
  }
  
  @Test
  /** Tests the instrument generated by the generator - no notional exchange. */
  public void generateInstrumentNoNotional() {
    double notional = 1000000;
    double fixedRate = 0.0125;
    int legTenorYear = 3;
    Period legTenor = Period.ofYears(legTenorYear);
    GeneratorAttributeIR attribute = new GeneratorAttributeIR(legTenor);
    ZonedDateTime valuationDate = DateUtils.getUTCDate(2014, 1, 22);
    AnnuityDefinition<?> instrumentDefinition = GENERATOR.generateInstrument(valuationDate, fixedRate, notional, attribute);
    assertEquals("GeneratorLegIborCompounding: generate -  number of coupons", 
        instrumentDefinition.getNumberOfPayments(), legTenorYear * 2); // Semi-annual payments
    ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(valuationDate, GENERATOR.getSpotOffset(), NYC);
    ZonedDateTime startDate = spotDate;
    ZonedDateTime endDate;
    for(int loopcpn = 0 ; loopcpn<legTenorYear*2; loopcpn++) {
      assertTrue("GeneratorLegIborCompounding: generate - coupon type", 
          instrumentDefinition.getNthPayment(loopcpn) instanceof CouponFixedDefinition);
      CouponFixedDefinition cpn = (CouponFixedDefinition)instrumentDefinition.getNthPayment(loopcpn);
      endDate = ScheduleCalculator.getAdjustedDate(spotDate, GENERATOR.getPaymentPeriod().multipliedBy(loopcpn+1), 
          GENERATOR.getBusinessDayConvention(), NYC, GENERATOR.isEndOfMonth());
      assertEquals("GeneratorLegIborCompounding: generate - start accrual date", startDate, cpn.getAccrualStartDate());
      assertEquals("GeneratorLegIborCompounding: generate - end accrual date", endDate, cpn.getAccrualEndDate());
      ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(cpn.getAccrualEndDate(), 
          GENERATOR.getPaymentOffset(), NYC);
      assertEquals("GeneratorLegIborCompounding: generate - payment date", paymentDate, cpn.getPaymentDate());
      assertEquals("GeneratorLegIborCompounding: generate - payment date", fixedRate, cpn.getRate());
      startDate = endDate;
    }    
  }
  
  @Test
  /** Tests the instrument generated by the generator - notional exchange */
  public void generateInstrumentNotional() {
    double notional = 1000000;
    double fixedRate = 0.0125;
    int legTenorYear = 3;
    Period legTenor = Period.ofYears(legTenorYear);
    GeneratorAttributeIR attribute = new GeneratorAttributeIR(legTenor);
    ZonedDateTime valuationDate = DateUtils.getUTCDate(2014, 1, 22);
    GeneratorLegFixed generatorNotional = new GeneratorLegFixed(NAME, USD, OFFSET_SPOT, P6M, DAY_COUNT, 
        BDC, OFFSET_PAYMENT, true, STUB_SHORT_START, true, NYC);
    AnnuityDefinition<?> instrumentDefinition = generatorNotional.generateInstrument(valuationDate, fixedRate, 
        notional, attribute);
    assertEquals("GeneratorLegIborCompounding: generate -  number of coupons", 
        legTenorYear * 2 + 2, instrumentDefinition.getNumberOfPayments()); // Semi-annual payments + notional
    ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(valuationDate, GENERATOR.getSpotOffset(), NYC);
    ZonedDateTime startDate = spotDate;
    ZonedDateTime endDate;
    assertTrue("GeneratorLegIborCompounding: generate - coupon type", 
        instrumentDefinition.getNthPayment(0) instanceof CouponFixedDefinition);
    CouponFixedDefinition cpn0 = (CouponFixedDefinition)instrumentDefinition.getNthPayment(0);
    assertEquals("GeneratorLegIborCompounding: generate - notional", -notional, cpn0.getNotional());
    for(int loopcpn = 1 ; loopcpn<legTenorYear*2+1; loopcpn++) {
      assertTrue("GeneratorLegIborCompounding: generate - coupon type", 
          instrumentDefinition.getNthPayment(loopcpn) instanceof CouponFixedDefinition);
      CouponFixedDefinition cpn = (CouponFixedDefinition)instrumentDefinition.getNthPayment(loopcpn);
      endDate = ScheduleCalculator.getAdjustedDate(spotDate, GENERATOR.getPaymentPeriod().multipliedBy(loopcpn), 
          GENERATOR.getBusinessDayConvention(), NYC, GENERATOR.isEndOfMonth());
      assertEquals("GeneratorLegIborCompounding: generate - start accrual date", startDate, cpn.getAccrualStartDate());
      assertEquals("GeneratorLegIborCompounding: generate - end accrual date", endDate, cpn.getAccrualEndDate());
      ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(cpn.getAccrualEndDate(), 
          GENERATOR.getPaymentOffset(), NYC);
      assertEquals("GeneratorLegIborCompounding: generate - payment date", paymentDate, cpn.getPaymentDate());
      assertEquals("GeneratorLegIborCompounding: generate - payment date", fixedRate, cpn.getRate());
      startDate = endDate;
    }    
    assertTrue("GeneratorLegIborCompounding: generate - coupon type", 
        instrumentDefinition.getNthPayment(legTenorYear*2+1) instanceof CouponFixedDefinition);
    CouponFixedDefinition cpnLast = (CouponFixedDefinition)instrumentDefinition.getNthPayment(legTenorYear*2+1);
    assertEquals("GeneratorLegIborCompounding: generate - notional", notional, cpnLast.getNotional());
  }
  
}
