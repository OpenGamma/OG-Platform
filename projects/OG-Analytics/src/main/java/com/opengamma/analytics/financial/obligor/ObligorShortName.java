/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.obligor;

import com.opengamma.util.ArgumentChecker;

/**
 * Gets the short name of an {@link Obligor}.
 */
public class ObligorShortName implements ObligorMeta<Obligor, String> {

  @Override
  public int compare(final Obligor o1, final Obligor o2) {
    return o1.getShortName().compareTo(o2.getShortName());
  }

  @Override
  public String getMetaData(final Obligor obligor) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getShortName();
  }

  @Override
  public String getMetaData(final Obligor obligor, final String element) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getShortName();
  }

}
