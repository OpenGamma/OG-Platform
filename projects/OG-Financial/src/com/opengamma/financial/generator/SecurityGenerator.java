/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.text.DecimalFormat;
import java.util.Random;

import javax.time.calendar.DateProvider;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

/**
 * Utility class for constructing parameters to random (but reasonable) securities.
 * 
 * @param <T> the security type, or a common super type if multiple types are being produced
 */
public abstract class SecurityGenerator<T extends ManageableSecurity> {

  /**
   * Format dates.
   */
  public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();

  /**
   * Format rates.
   */
  public static final DecimalFormat RATE_FORMATTER = new DecimalFormat("0.###%");

  /**
   * Format notionals.
   */
  public static final DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("0,000");

  /**
   * Constant for the length of a year in days.
   */
  protected static final double YEAR_LENGTH = 365.25;

  private Random _random = new Random();
  private ConventionBundleSource _conventionSource;
  private ConfigSource _configSource;
  private HolidaySource _holidaySource;
  private HistoricalTimeSeriesSource _historicalSource;

  public Random getRandom() {
    return _random;
  }

  public void setRandom(final Random random) {
    _random = random;
  }

  protected int getRandom(final int n) {
    return getRandom().nextInt(n);
  }

  protected double getRandom(final double low, final double high) {
    return low + (high - low) * getRandom().nextDouble();
  }

  protected <X> X getRandom(final X[] xs) {
    return xs[getRandom(xs.length)];
  }

  protected int getRandom(final int[] xs) {
    return xs[getRandom(xs.length)];
  }

  protected double getRandom(final double[] xs) {
    return xs[getRandom(xs.length)];
  }

  public ConventionBundleSource getConventionSource() {
    return _conventionSource;
  }

  public void setConventionSource(final ConventionBundleSource conventionSource) {
    _conventionSource = conventionSource;
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public void setConfigSource(final ConfigSource configSource) {
    _configSource = configSource;
  }

  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  public void setHolidaySource(final HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }

  public HistoricalTimeSeriesSource getHistoricalSource() {
    return _historicalSource;
  }

  public void setHistoricalSource(final HistoricalTimeSeriesSource historicalSource) {
    _historicalSource = historicalSource;
  }

  public static Currency[] getDefaultCurrencies() {
    return new Currency[] {Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF };
  }

  public Currency[] getCurrencies() {
    return getDefaultCurrencies();
  }

  protected Currency getRandomCurrency() {
    return getRandom(getCurrencies());
  }

  private boolean isWorkday(final DayOfWeek dow, final Currency currency) {
    // TODO: use a proper convention/holiday source
    return dow.getValue() < 6;
  }

  private boolean isHoliday(final DateProvider ldp, final Currency currency) {
    return getHolidaySource().isHoliday(ldp.toLocalDate(), currency);
  }

  // TODO: replace this with a date adjuster
  protected ZonedDateTime nextWorkingDay(ZonedDateTime zdt, final Currency currency) {
    while (!isWorkday(zdt.getDayOfWeek(), currency) || isHoliday(zdt, currency)) {
      zdt = zdt.plusDays(1);
    }
    return zdt;
  }

  // TODO: replace this with a date adjuster
  protected ZonedDateTime previousWorkingDay(ZonedDateTime zdt, final Currency currency) {
    while (!isWorkday(zdt.getDayOfWeek(), currency) || isHoliday(zdt, currency)) {
      zdt = zdt.minusDays(1);
    }
    return zdt;
  }

  /**
   * Creates a new random, but reasonable, security.
   * 
   * @return the new security, or null if no security can be generated
   */
  public abstract T createSecurity();

}
