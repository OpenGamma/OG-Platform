/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.BondFutureCalculator;
import com.opengamma.financial.interestrate.bond.BondPriceCalculator;
import com.opengamma.financial.interestrate.bond.BondYieldCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.security.DateTimeWithZone;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class BondFutureTest {
  private static final BondYieldCalculator YIELD_CALCULATOR = new BondYieldCalculator();
  
  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());
  private static final BondSecurityToBondConverter CONVERTER = new BondSecurityToBondConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE);
  
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2001, 12, 10);
  private static final ZonedDateTime ACCRUAL_DATE = DateUtil.getUTCDate(2001, 8, 15);
  private static final ZonedDateTime FIRST_COUPON_DATE = DateUtil.getUTCDate(2002, 2, 15);
  private static final ZonedDateTime MATURITY_DATE = DateUtil.getUTCDate(2011, 8, 15);
  
  private static double COUPON = 5.0;
  private static final double C_FACTOR = 0.9297;
  
  //values as of 07/12/2001 from page 362 of Fixed-income securities 
  private static final double CLEAN_PRICE = 98.9688;
  private static final double CONV_YIELD = 5.136;
  private static final double GROSS_BASIS = 2.149;
  private static final double NET_BASIS = 1.190;
  private static final double IMPLIED_REPO = -2.25;
  private static final double ACTUAL_REPO = 1.73;
  
  private static final double FUTURE_PRICE = 104.1406;
  private static final ZonedDateTime FIRST_DELIVERY_DATE = DateUtil.getUTCDate(2002,3,01);
  private static final ZonedDateTime LAST_DELIVERY_DATE = DateUtil.getUTCDate(2002,3,28);
 
  private static final BondSecurity BOND = new GovernmentBondSecurity("US",
      "Government",
      "US",
      "Treasury",
      Currency.getInstance("USD"),
      YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"),
      new Expiry(MATURITY_DATE),
      "",
      COUPON,
      SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME),
      DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
      new DateTimeWithZone(ACCRUAL_DATE),
      new DateTimeWithZone(SETTLEMENT_DATE),
      new DateTimeWithZone(FIRST_COUPON_DATE),
      100,
      100000000,
      5000,
      1000,
      100,
      100);
  
  
  @Test
  public void test() {
    final Bond bond = CONVERTER.getBond(BOND, "some curve", SETTLEMENT_DATE);
    final Bond fwdBond = CONVERTER.getBond(BOND, "some curve", LAST_DELIVERY_DATE);
    
    double dirtyPrice = BondPriceCalculator.dirtyPrice(bond, CLEAN_PRICE/100.0);
    double yield = YIELD_CALCULATOR.calculate(bond, dirtyPrice);
    yield = 2 * (Math.exp(yield / 2) - 1.0);
    assertEquals(CONV_YIELD/100.0,yield,1e-5);
    
    double grossBasis = BondFutureCalculator.grossBasis(CLEAN_PRICE/100.0, FUTURE_PRICE/100.0, C_FACTOR);
    assertEquals(GROSS_BASIS/100.0,grossBasis,1e-5);
    
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360"); //TODO this needs to be pulled from a convention 
    double deliveryDate = dayCount.getDayCountFraction(SETTLEMENT_DATE, LAST_DELIVERY_DATE);
    
    double netBasis = BondFutureCalculator.netBasis(bond, deliveryDate, CLEAN_PRICE/100., FUTURE_PRICE/100., C_FACTOR, 
        fwdBond.getAccruedInterestFraction(), ACTUAL_REPO/100.0);
    
    assertEquals(NET_BASIS/100.0,netBasis,1e-5);
  }
  
  
  private static class MyHolidaySource implements HolidaySource {

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final IdentifierBundle regionOrExchangeIds) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final Identifier regionOrExchangeId) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public Holiday getHoliday(final UniqueIdentifier uid) {
      return null;
    }

  }
      
}
