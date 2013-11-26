/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.obligor;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;

/**
 * Gets the country of an {@link Obligor}.
 */
public class ObligorCountry implements ObligorMeta<Obligor, Country> {

  @Override
  public int compare(final Obligor o1, final Obligor o2) {
    return o1.getCountry().getCode().compareTo(o2.getCountry().getCode());
  }

  @Override
  public Country getMetaData(final Obligor obligor) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getCountry();
  }

  @Override
  public Country getMetaData(final Obligor obligor, final Country element) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getCountry();
  }

}
