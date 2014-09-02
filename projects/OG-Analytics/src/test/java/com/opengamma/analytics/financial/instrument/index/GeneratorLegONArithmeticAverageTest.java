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
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the generator of legs based on Overnight coupons with arithmetic average (Fed Fund style).
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorLegONArithmeticAverageTest {

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final Currency USD = Currency.USD;
  
  private static final String NAME = "LEG_USD1YFEDFUND";
  private static final int OFFSET_SPOT = 2;
  private static final int OFFSET_PAYMENT = 0;
  private static final GeneratorLegONArithmeticAverage GENERATOR = new GeneratorLegONArithmeticAverage(NAME, USD, 
      FEDFUND, Period.ofMonths(12), OFFSET_SPOT, OFFSET_PAYMENT, BusinessDayConventions.MODIFIED_FOLLOWING, true, 
      StubType.SHORT_START, false, NYC, NYC);
  private static final GeneratorLegONArithmeticAverageSimplified GENERATOR_SIMPLE = 
      new GeneratorLegONArithmeticAverageSimplified(NAME, USD, FEDFUND,
      Period.ofMonths(12), OFFSET_SPOT, OFFSET_PAYMENT, BusinessDayConventions.MODIFIED_FOLLOWING, true, 
      StubType.SHORT_START, false, NYC, NYC);
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new GeneratorLegONArithmeticAverage(null, USD, FEDFUND, 
        Period.ofMonths(12), 2, 2, BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new GeneratorLegONArithmeticAverage("LEG_USD1YFEDFUND", USD, null, 
        Period.ofMonths(12), 2, 2, BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTenor() {
    new GeneratorLegONArithmeticAverage("LEG_USD1YFEDFUND", USD, FEDFUND, 
        null, 2, 2, BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorLegONArithmeticAverage("LEG_USD1YFEDFUND", USD, FEDFUND, 
        Period.ofMonths(12), 2, 2, null, true, StubType.SHORT_START, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStub() {
    new GeneratorLegONArithmeticAverage("LEG_USD1YFEDFUND", USD, FEDFUND, 
        Period.ofMonths(12), 2, 2, BusinessDayConventions.MODIFIED_FOLLOWING, true, null, false, NYC, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendarIndex() {
    new GeneratorLegONArithmeticAverage("LEG_USD1YFEDFUND", USD, FEDFUND, 
        Period.ofMonths(12), 2, 2, BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, null, NYC);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCalendarPayment() {
    new GeneratorLegONArithmeticAverage("LEG_USD1YFEDFUND", USD, FEDFUND, 
        Period.ofMonths(12), 2, 2, BusinessDayConventions.MODIFIED_FOLLOWING, true, StubType.SHORT_START, false, NYC, null);
  }
  
  @Test
  public void getter() {
    assertEquals("GeneratorLegONArithmeticAverage: getter", NAME, GENERATOR.getName());
    assertEquals("GeneratorLegONArithmeticAverage: getter", FEDFUND, GENERATOR.getIndexON());
    assertEquals("GeneratorLegONArithmeticAverage: getter", Period.ofMonths(12), GENERATOR.getPaymentPeriod());
    assertEquals("GeneratorLegONArithmeticAverage: getter", OFFSET_SPOT, GENERATOR.getSpotOffset());
    assertEquals("GeneratorLegONArithmeticAverage: getter", OFFSET_PAYMENT, GENERATOR.getPaymentOffset());
    assertEquals("GeneratorLegONArithmeticAverage: getter", 
        BusinessDayConventions.MODIFIED_FOLLOWING, GENERATOR.getBusinessDayConvention());
    assertEquals("GeneratorLegONArithmeticAverage: getter", true, GENERATOR.isEndOfMonth());
    assertEquals("GeneratorLegONArithmeticAverage: getter", StubType.SHORT_START, GENERATOR.getStubType());
    assertEquals("GeneratorLegONArithmeticAverage: getter", false, GENERATOR.isExchangeNotional());
    assertEquals("GeneratorLegONArithmeticAverage: getter", NYC, GENERATOR.getIndexCalendar());
    assertEquals("GeneratorLegONArithmeticAverage: getter", NYC, GENERATOR.getPaymentCalendar());
  }

  
  @Test
  /** Tests the instrument generated by the generator. */
  public void generateInstrument() {
    double notional = 1000000;
    double spread = 0.0025;
    int legTenorYear = 3;
    Period legTenor = Period.ofYears(legTenorYear);
    GeneratorAttributeIR attribute = new GeneratorAttributeIR(legTenor);
    ZonedDateTime valuationDate = DateUtils.getUTCDate(2014, 1, 22);
    AnnuityDefinition<?> instrumentDefinition = GENERATOR.generateInstrument(valuationDate, spread, notional, attribute);
    AnnuityDefinition<?> instrumentDefinitionSimplified = 
        GENERATOR_SIMPLE.generateInstrument(valuationDate, spread, notional, attribute);
    assertEquals("GeneratorLegONCompounded: generate -  number of coupons", 
        instrumentDefinition.getNumberOfPayments(), legTenorYear);
    ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(valuationDate, GENERATOR.getSpotOffset(), NYC);
    ZonedDateTime startDate = spotDate;
    ZonedDateTime endDate;
    for(int loopcpn = 0 ; loopcpn<legTenorYear; loopcpn++) {
      assertTrue("GeneratorLegONCompounded: generate - coupon type", 
          instrumentDefinition.getNthPayment(loopcpn) instanceof CouponONArithmeticAverageSpreadDefinition);
      CouponONArithmeticAverageSpreadDefinition cpn = 
          (CouponONArithmeticAverageSpreadDefinition) instrumentDefinition.getNthPayment(loopcpn);
      endDate = ScheduleCalculator.getAdjustedDate(spotDate, GENERATOR.getPaymentPeriod().multipliedBy(loopcpn+1), 
          GENERATOR.getBusinessDayConvention(), NYC, GENERATOR.isEndOfMonth());
      assertEquals("GeneratorLegONCompounded: generate - start accrual date", startDate, cpn.getAccrualStartDate());
      assertEquals("GeneratorLegONCompounded: generate - end accrual date", endDate, cpn.getAccrualEndDate());
      ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(
          cpn.getFixingPeriodDates()[cpn.getFixingPeriodDates().length-1], GENERATOR.getPaymentOffset(), NYC);
      assertEquals("GeneratorLegONCompounded: generate - payment date", paymentDate, cpn.getPaymentDate());
      assertEquals("GeneratorLegONCompounded: generate - spread", spread, cpn.getSpread());
      assertTrue("GeneratorLegONCompounded: generate - coupon type", 
          instrumentDefinitionSimplified.getNthPayment(loopcpn) instanceof CouponONArithmeticAverageSpreadSimplifiedDefinition);
      CouponONArithmeticAverageSpreadSimplifiedDefinition cpnSim = 
          (CouponONArithmeticAverageSpreadSimplifiedDefinition) instrumentDefinitionSimplified.getNthPayment(loopcpn);
      assertEquals("GeneratorLegONCompounded: generate - start accrual date", startDate, cpnSim.getAccrualStartDate());
      assertEquals("GeneratorLegONCompounded: generate - end accrual date", endDate, cpnSim.getAccrualEndDate());
      assertEquals("GeneratorLegONCompounded: generate - payment date", paymentDate, cpnSim.getPaymentDate());
      assertEquals("GeneratorLegONCompounded: generate - spread", spread, cpnSim.getSpread());
      startDate = endDate;
    }
  }
  
}
