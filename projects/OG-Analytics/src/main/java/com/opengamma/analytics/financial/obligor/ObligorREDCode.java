/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.obligor;

import com.opengamma.util.ArgumentChecker;

/**
 * Gets the region of an {@link Obligor}.
 */
public class ObligorREDCode implements ObligorMeta<ObligorWithREDCode, String> {

  @Override
  public int compare(final ObligorWithREDCode o1, final ObligorWithREDCode o2) {
    return o1.getRedCode().compareTo(o2.getRedCode());
  }

  @Override
  public String getMetaData(final ObligorWithREDCode obligor) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getRedCode();
  }

  @Override
  public String getMetaData(final ObligorWithREDCode obligor, final String element) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getRedCode();
  }

}
