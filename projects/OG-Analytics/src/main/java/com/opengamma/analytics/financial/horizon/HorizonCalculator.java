/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the difference in present value between one day and the next, giving a theta-like result.
 * Possible implementations could be a constant spread-type calculation, where there is no forward slide
 * of the market data, or one where all market data has slide.
 * @param <U> The type of instrument
 * @param <V> The type of the market data
 * @param <W> The type of any additional market data required to calculate the horizon.
 */
public abstract class HorizonCalculator<U extends InstrumentDefinition<?>, V extends ParameterProviderInterface, W> {

  /**
   * Calculates the theta for an instrument.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param data The initial yield curve data, not null
   * @param daysForward The number of days to roll, must be +/-1
   * @param calendar The holiday calendar, not null
   * @return The theta
   */
  public abstract MultipleCurrencyAmount getTheta(U definition, ZonedDateTime date, V data, int daysForward, Calendar calendar);

  /**
   * Calculates the theta for an instrument.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param data The initial yield curve data, not null
   * @param daysForward The number of days to roll, must be +/-1
   * @param calendar The holiday calendar, may be null
   * @param additionalData Any additional data that are needed for pricing (e.g. fixing series for swaps).
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final U definition, final ZonedDateTime date, final V data, final int daysForward, final Calendar calendar,
      final W additionalData) {
    return getTheta(definition, date, data, daysForward, calendar);
  }

  /**
   * Convenience method that subtracts two multiple currency amounts.
   * @param a The first currency amount
   * @param b The second currency amount
   * @return a - b
   */
  protected static MultipleCurrencyAmount subtract(final MultipleCurrencyAmount a, final MultipleCurrencyAmount b) {
    return a.plus(b.multipliedBy(-1));
  }

}
