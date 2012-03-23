/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.text.DecimalFormat;
import java.util.Random;

import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

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
    final Currency[] currencies = getCurrencies();
    return currencies[getRandom(currencies.length)];
  }

  public Tenor[] getTenors() {
    return new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.ofYears(7), Tenor.ofYears(10), Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20) };
  }

  protected Tenor getRandomTenor() {
    final Tenor[] tenors = getTenors();
    return tenors[getRandom(tenors.length)];
  }

  /**
   * Creates a new random, but reasonable, security.
   * 
   * @return the new security, or null if no security can be generated
   */
  public abstract T createSecurity();

}
