/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.obligor;

import com.opengamma.util.ArgumentChecker;

/**
 * Gets the ticker of an {@link Obligor}.
 */
public class ObligorTicker implements ObligorMeta<Obligor, String> {

  @Override
  public int compare(final Obligor o1, final Obligor o2) {
    return o1.getTicker().compareTo(o2.getTicker());
  }

  @Override
  public String getMetaData(final Obligor obligor) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getTicker();
  }

  @Override
  public String getMetaData(final Obligor obligor, final String element) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getTicker();
  }

}
