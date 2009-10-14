package com.opengamma.util.time;

import javax.time.period.Period;

public class Tenor {
  private final Period _period;
  public static final Tenor DAY = new Tenor(Period.days(1));
  public static final Tenor WORKING_WEEK = new Tenor(Period.days(5));
  public static final Tenor WORKING_DAYS_IN_YEAR = new Tenor(Period.hours(250 * 24));
  public static final Tenor WORKING_DAYS_IN_MONTH = new Tenor(WORKING_DAYS_IN_YEAR.getPeriod().dividedBy(12));
  public static final Tenor FINANCIAL_YEAR = new Tenor(Period.hours((int) (365.25 * 24)));
  public static final Tenor YEAR = new Tenor(Period.days(365));
  public static final Tenor LEAP_YEAR = new Tenor(Period.days(366));
  public static final Tenor TWO_FINANCIAL_YEARS = new Tenor(FINANCIAL_YEAR.getPeriod().multipliedBy(2));

  public Tenor(final Period period) {
    _period = period;
  }

  public Period getPeriod() {
    return _period;
  }
}
