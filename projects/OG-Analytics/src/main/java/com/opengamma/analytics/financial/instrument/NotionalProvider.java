/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.instrument;

import org.threeten.bp.LocalDate;
import com.opengamma.util.money.CurrencyAmount;

/**
 * An interface that can return the notional for a given date.
 */
public interface NotionalProvider {

  public abstract double getAmount(final LocalDate date);

}
