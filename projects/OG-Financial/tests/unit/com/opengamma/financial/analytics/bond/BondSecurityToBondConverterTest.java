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
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
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
public class BondSecurityToBondConverterTest {
  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());
  private static final BondSecurityToBondConverter CONVERTER = new BondSecurityToBondConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2007, 10, 2);
 private static final double COUPON = 4.0;
  private static final BondSecurity BOND = new GovernmentBondSecurity("US",
                                                                        "Government",
                                                                        "US",
                                                                        "Treasury",
                                                                        Currency.getInstance("USD"),
                                                                        YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"),
                                                                        new Expiry(DateUtil.getUTCDate(2008, 9, 30)),
                                                                        "",
                                                                        COUPON,
                                                                        SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME),
                                                                        DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"),
                                                                        new DateTimeWithZone(DateUtil.getUTCDate(2007, 9, 30)),
                                                                        new DateTimeWithZone(DateUtil.getUTCDate(2007, 10, 3)),
                                                                        new DateTimeWithZone(DateUtil.getUTCDate(2008, 3, 31)),
                                                                        100,
                                                                        100000000,
                                                                        5000,
                                                                        1000,
                                                                        100,
                                                                        100);
  private static final String NAME = "BOND_YIELD";
  private static final double EPS = 1e-12;

  @Test(expected = IllegalArgumentException.class)
  public void testNullHolidaySource() {
    new BondSecurityToBondConverter(null, CONVENTION_SOURCE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConventionSource() {
    new BondSecurityToBondConverter(HOLIDAY_SOURCE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecurity() {
    CONVERTER.getBond(null, "", DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName() {
    CONVERTER.getBond(BOND, null, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNow() {
    CONVERTER.getBond(BOND, "", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExpiredBond() {
    CONVERTER.getBond(BOND, "", DateUtil.getUTCDate(2010, 1, 1));
  }

  @Test
  public void test() {
    final Bond bond = CONVERTER.getBond(BOND, NAME, DATE);
    assertEquals(bond.getAccruedInterest(), COUPON / 100 / 2.0* 3. / 183, EPS);
    final GenericAnnuity<FixedPayment> annuity = bond.getAnnuity();
    assertEquals(annuity.getNumberOfPayments(), 3);
    final FixedPayment[] payments = annuity.getPayments();
    for (int i = 0; i < 2; i++) {
      assertEquals(payments[i].getAmount(), 0.02, 0);
      assertEquals(payments[i].getPaymentTime(), 0.5 + (i / 2.), 1e-2);
      assertEquals(payments[i].getFundingCurveName(), NAME);
    }
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
