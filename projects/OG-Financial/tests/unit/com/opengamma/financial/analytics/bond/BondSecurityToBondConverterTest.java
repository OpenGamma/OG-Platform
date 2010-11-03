/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;


/**
 * 
 */
public class BondSecurityToBondConverterTest {
  //  private static final HolidaySource HOLIDAY_SOURCE = new MyHolidaySource();
  //  private static final ConventionBundleSource CONVENTION_SOURCE = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());
  //  private static final BondSecurityToBondConverter CONVERTER = new BondSecurityToBondConverter(HOLIDAY_SOURCE, CONVENTION_SOURCE);
  //  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2007, 10, 1);
  //  private static final BondSecurity BOND = new GovernmentBondSecurity("US",
  //                                                                      "Government",
  //                                                                      "US",
  //                                                                      "Treasury",
  //                                                                      Currency.getInstance("USD"),
  //                                                                      YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury equivalent"),
  //                                                                      new Expiry(DateUtil.getUTCDate(2009, 9, 30)),
  //                                                                      "",
  //                                                                      0.04,
  //                                                                      SimpleFrequencyFactory.INSTANCE.getFrequency(SimpleFrequency.SEMI_ANNUAL_NAME),
  //                                                                      DayCountFactory.INSTANCE.getDayCount("Actual/Actual"),
  //                                                                      new DateTimeWithZone(DateUtil.getUTCDate(2007, 9, 30)),
  //                                                                      new DateTimeWithZone(DateUtil.getUTCDate(2007, 10, 3)),
  //                                                                      new DateTimeWithZone(DateUtil.getUTCDate(2008, 3, 31)),
  //                                                                      100,
  //                                                                      100000000,
  //                                                                      5000,
  //                                                                      1000,
  //                                                                      100,
  //                                                                      100);
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testNullHolidaySource() {
  //    new BondSecurityToBondConverter(null, CONVENTION_SOURCE);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testNullConventionSource() {
  //    new BondSecurityToBondConverter(HOLIDAY_SOURCE, null);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testNullSecurity() {
  //    CONVERTER.getBond(null, "", DATE);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testNullName() {
  //    CONVERTER.getBond(BOND, null, DATE);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testNullNow() {
  //    CONVERTER.getBond(BOND, "", null);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testExpiredBond() {
  //    CONVERTER.getBond(BOND, "", DateUtil.getUTCDate(2010, 1, 1));
  //  }
  //
  //  @Test
  //  public void test() {
  //    final Bond bond = CONVERTER.getBond(BOND, "", DATE);
  //    System.out.println(bond.getAccruedInterestFraction() * 182);
  //  }
  //
  //  private static class MyHolidaySource implements HolidaySource {
  //
  //    @Override
  //    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
  //      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
  //    }
  //
  //    @Override
  //    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final IdentifierBundle regionOrExchangeIds) {
  //      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
  //    }
  //
  //    @Override
  //    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final Identifier regionOrExchangeId) {
  //      return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
  //    }
  //
  //    @Override
  //    public Holiday getHoliday(final UniqueIdentifier uid) {
  //      return null;
  //    }
  //
  //  }
}
