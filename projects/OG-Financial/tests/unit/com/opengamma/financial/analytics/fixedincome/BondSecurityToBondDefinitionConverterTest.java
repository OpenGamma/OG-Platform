/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.bond.BondSecurityToBondDefinitionConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class BondSecurityToBondDefinitionConverterTest {
  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(
      new InMemoryConventionBundleMaster());
  private static final RegionSource REGION_SOURCE = new MasterRegionSource(RegionFileReader.createPopulated().getRegionMaster());
  private static final BondSecurityToBondDefinitionConverter CONVERTER = new BondSecurityToBondDefinitionConverter(
      HOLIDAY_SOURCE, CONVENTION_SOURCE);
  private static final ZonedDateTime FIRST_ACCRUAL_DATE = DateUtil.getUTCDate(2007, 9, 30);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2007, 10, 2);
  private static final ZonedDateTime FIRST_COUPON_DATE = DateUtil.getUTCDate(2008, 3, 31);
  private static final ZonedDateTime LAST_TRADE_DATE = DateUtil.getUTCDate(2008, 9, 30);
  private static final double COUPON = 4.0;
  private static final BondSecurityConverter NEW_CONVERTER = new BondSecurityConverter(HOLIDAY_SOURCE,
      CONVENTION_SOURCE, REGION_SOURCE);
  private static final GovernmentBondSecurity BOND = new GovernmentBondSecurity("US", "Government", "US", "Treasury", Currency.USD,
      YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"), new Expiry(LAST_TRADE_DATE), "", COUPON,
      SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME), DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), FIRST_ACCRUAL_DATE,
      SETTLEMENT_DATE, FIRST_COUPON_DATE, 100, 100000000, 5000, 1000, 100, 100);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidaySource() {
    new BondSecurityToBondDefinitionConverter(null, CONVENTION_SOURCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionSource() {
    new BondSecurityToBondDefinitionConverter(HOLIDAY_SOURCE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity1() {
    CONVERTER.getBond((BondSecurity) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity2() {
    CONVERTER.getBond((BondSecurity) null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity3() {
    CONVERTER.getBond((BondSecurity) null, false, CONVENTION_SOURCE.getConventionBundle(ExternalId.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_TREASURY_BOND_CONVENTION")));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    CONVERTER.getBond(BOND, false, null);
  }

  @Test
  public void test() {
    final BondDefinition definition = CONVERTER.getBond(BOND, true);
    assertArrayEquals(definition.getNominalDates(), new LocalDate[] {FIRST_ACCRUAL_DATE.toLocalDate(),
        FIRST_COUPON_DATE.toLocalDate(), LAST_TRADE_DATE.toLocalDate()});
    assertEquals(definition.getSettlementDates()[0], SETTLEMENT_DATE.toLocalDate());
  }

  @Test
  public void testNew() {
    //    final BondDefinition definition = NEW_CONVERTER.visitGovernmentBondSecurity(BOND);
    //    assertArrayEquals(definition.getNominalDates(), new LocalDate[] {FIRST_ACCRUAL_DATE.toLocalDate(),
    //        FIRST_COUPON_DATE.toLocalDate(), LAST_TRADE_DATE.toLocalDate()});
    //    assertEquals(definition.getSettlementDates()[0], SETTLEMENT_DATE.toLocalDate());
  }

  private static class MyHolidaySource implements HolidaySource {

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
        final ExternalIdBundle regionOrExchangeIds) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
        final ExternalId regionOrExchangeId) {
      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public Holiday getHoliday(final UniqueId uid) {
      return null;
    }

  }

}
