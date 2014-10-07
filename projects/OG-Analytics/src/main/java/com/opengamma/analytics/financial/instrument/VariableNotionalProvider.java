/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Arrays;

import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * The class describes variable notionals. 
 */
public class VariableNotionalProvider implements NotionalProvider {

  private final LocalDate[] _dates;
  private final double[] _notionals;

  /**
   * Construct notional provider for variable notional
   * @param dates Array of dates specifying variable notionals
   * @param notionals The notionals
   */
  public VariableNotionalProvider(final LocalDate[] dates, final double[] notionals) {
    ArgumentChecker.notEmpty(dates, "dates");
    ArgumentChecker.notEmpty(notionals, "notionals");
    int nDates = dates.length;
    ArgumentChecker.isTrue(notionals.length == nDates, "dates and notionals should have the same length");

    _dates = Arrays.copyOf(dates, nDates);
    _notionals = Arrays.copyOf(notionals, nDates);
  }

  @Override
  public double getAmount(final LocalDate date) {
    for (int i = 0; i < _dates.length; ++i) {
      if (_dates[i].equals(date)) {
        return _notionals[i];
      }
    }
    throw new IllegalArgumentException("The date " + date + " is not found in the date set");
  }

  /**
   * Access notionals 
   * @return _notionals
   */
  public double[] getNotionals() {
    return _notionals;
  }

  /**
   * Access dates
   * @return _dates
   */
  public LocalDate[] getDates() {
    return _dates;
  }
}
