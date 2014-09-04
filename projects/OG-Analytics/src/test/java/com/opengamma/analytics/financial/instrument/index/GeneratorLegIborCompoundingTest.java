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
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

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
  private static final CompoundingMethod CMP_STRAIGHT = CompoundingMethod.STRAIGHT;
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
  
  @Test
  /** Tests the instrument generated by the generator with compounding method FLAT. */
  public void generateInstrumentFlatCompounding() {
    double notional = 1000000;
    double spread = 0.0025;
    int legTenorYear = 3;
    Period legTenor = Period.ofYears(legTenorYear);
    GeneratorAttributeIR attribute = new GeneratorAttributeIR(legTenor);
    ZonedDateTime valuationDate = DateUtils.getUTCDate(2014, 1, 22);
    AnnuityDefinition<?> instrumentDefinition = GENERATOR.generateInstrument(valuationDate, spread, notional, attribute);
    assertEquals("GeneratorLegIborCompounding: generate -  number of coupons", 
        instrumentDefinition.getNumberOfPayments(), legTenorYear * 4); // Quarterly payments
    ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(valuationDate, GENERATOR.getSpotOffset(), NYC);
    ZonedDateTime startDate = spotDate;
    ZonedDateTime endDate;
    for(int loopcpn = 0 ; loopcpn<legTenorYear; loopcpn++) {
      assertTrue("GeneratorLegIborCompounding: generate - coupon type", 
          instrumentDefinition.getNthPayment(loopcpn) instanceof CouponIborCompoundingFlatSpreadDefinition);
      CouponIborCompoundingFlatSpreadDefinition cpn = (CouponIborCompoundingFlatSpreadDefinition)instrumentDefinition.getNthPayment(loopcpn);
      endDate = ScheduleCalculator.getAdjustedDate(spotDate, GENERATOR.getPaymentPeriod().multipliedBy(loopcpn+1), 
          GENERATOR.getBusinessDayConvention(), NYC, GENERATOR.isEndOfMonth());
      // Sub-periods dates
      ZonedDateTime subperiodStartDate = startDate;
      ZonedDateTime subperiodEndDate;
      for(int loopcmp=0; loopcmp<3; loopcmp++) {
        subperiodEndDate = ScheduleCalculator.getAdjustedDate(startDate, 
            GENERATOR.getIndexIbor().getTenor().multipliedBy(loopcmp+1), 
            GENERATOR.getIndexIbor().getBusinessDayConvention(), NYC, GENERATOR.getIndexIbor().isEndOfMonth());
        assertEquals("GeneratorLegIborCompounding: generate - start sub period", subperiodStartDate, 
            cpn.getFixingSubperiodStartDates()[loopcmp]);
        ZonedDateTime endFixing = ScheduleCalculator.getAdjustedDate(subperiodStartDate, GENERATOR.getIndexIbor(), NYC);
        assertEquals("GeneratorLegIborCompounding: generate - end sub period", endFixing, 
            cpn.getFixingSubperiodEndDates()[loopcmp]);
        subperiodStartDate = subperiodEndDate;
      }
      assertEquals("GeneratorLegIborCompounding: generate - start accrual date", startDate, cpn.getAccrualStartDate());
      assertEquals("GeneratorLegIborCompounding: generate - end accrual date", endDate, cpn.getAccrualEndDate());
      ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(cpn.getAccrualEndDate(), 
          GENERATOR.getPaymentOffset(), NYC);
      assertEquals("GeneratorLegIborCompounding: generate - payment date", paymentDate, cpn.getPaymentDate());
      assertEquals("GeneratorLegIborCompounding: generate - spread", spread, cpn.getSpread());
      startDate = endDate;
    }
  }
  
  @Test
  /** Tests the instrument generated by the generator with compounding method FLAT. */
  public void generateInstrumentStraightCompounding() {
    double notional = 1000000;
    double spread = 0.0025;
    int legTenorYear = 3;
    Period legTenor = Period.ofYears(legTenorYear);
    GeneratorAttributeIR attribute = new GeneratorAttributeIR(legTenor);
    ZonedDateTime valuationDate = DateUtils.getUTCDate(2014, 1, 22);
    GeneratorLegIborCompounding generator = new GeneratorLegIborCompounding(NAME, USD, USDLIBOR1M, 
        P3M, CMP_STRAIGHT, OFFSET_SPOT, OFFSET_PAYMENT, BusinessDayConventions.MODIFIED_FOLLOWING, true, 
        StubType.SHORT_START, false, NYC, NYC);
    AnnuityDefinition<?> instrumentDefinition = generator.generateInstrument(valuationDate, spread, notional, attribute);
    assertEquals("GeneratorLegONCompounded: generate -  number of coupons", 
        instrumentDefinition.getNumberOfPayments(), legTenorYear * 4); // Quarterly payments
    ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(valuationDate, generator.getSpotOffset(), NYC);
    ZonedDateTime startDate = spotDate;
    ZonedDateTime endDate;
    for(int loopcpn = 0 ; loopcpn<legTenorYear; loopcpn++) {
      assertTrue("GeneratorLegONCompounded: generate - coupon type", 
          instrumentDefinition.getNthPayment(loopcpn) instanceof CouponIborCompoundingSpreadDefinition);
      CouponIborCompoundingSpreadDefinition cpn = (CouponIborCompoundingSpreadDefinition)instrumentDefinition.getNthPayment(loopcpn);
      endDate = ScheduleCalculator.getAdjustedDate(spotDate, generator.getPaymentPeriod().multipliedBy(loopcpn+1), 
          generator.getBusinessDayConvention(), NYC, generator.isEndOfMonth());
      // Sub-periods dates
      ZonedDateTime subperiodStartDate = startDate;
      ZonedDateTime subperiodEndDate;
      for(int loopcmp=0; loopcmp<3; loopcmp++) {
        subperiodEndDate = ScheduleCalculator.getAdjustedDate(startDate, 
            GENERATOR.getIndexIbor().getTenor().multipliedBy(loopcmp+1), 
            GENERATOR.getIndexIbor().getBusinessDayConvention(), NYC, GENERATOR.getIndexIbor().isEndOfMonth());
        assertEquals("GeneratorLegIborCompounding: generate - start sub period", subperiodStartDate, 
            cpn.getFixingPeriodStartDates()[loopcmp]);
        ZonedDateTime endFixing = ScheduleCalculator.getAdjustedDate(subperiodStartDate, GENERATOR.getIndexIbor(), NYC);
        assertEquals("GeneratorLegIborCompounding: generate - end sub period", endFixing, 
            cpn.getFixingPeriodEndDates()[loopcmp]);
        subperiodStartDate = subperiodEndDate;
      }
      assertEquals("GeneratorLegONCompounded: generate - start accrual date", startDate, cpn.getAccrualStartDate());
      assertEquals("GeneratorLegONCompounded: generate - end accrual date", endDate, cpn.getAccrualEndDate());
      ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(cpn.getAccrualEndDate(), 
          generator.getPaymentOffset(), NYC);
      assertEquals("GeneratorLegONCompounded: generate - payment date", paymentDate, cpn.getPaymentDate());
      assertEquals("GeneratorLegONCompounded: generate - spread", spread, cpn.getSpread());
      startDate = endDate;
    }
  }
  
}
