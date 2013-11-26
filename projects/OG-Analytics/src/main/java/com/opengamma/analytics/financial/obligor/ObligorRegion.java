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
public class ObligorRegion implements ObligorMeta<Obligor, Region> {

  @Override
  public int compare(final Obligor o1, final Obligor o2) {
    return o1.getRegion().getName().compareTo(o2.getRegion().getName());
  }

  @Override
  public Region getMetaData(final Obligor obligor) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getRegion();
  }

  @Override
  public Region getMetaData(final Obligor obligor, final Region element) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getRegion();
  }

}
