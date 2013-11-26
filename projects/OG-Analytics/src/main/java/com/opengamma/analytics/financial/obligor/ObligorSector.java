/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.obligor;

import com.opengamma.util.ArgumentChecker;

/**
 * Gets the sector of an {@link Obligor}.
 */
public class ObligorSector implements ObligorMeta<Obligor, Sector> {

  @Override
  public int compare(final Obligor o1, final Obligor o2) {
    return o1.getSector().getName().compareTo(o2.getSector().getName());
  }

  @Override
  public Sector getMetaData(final Obligor obligor) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getSector();
  }

  @Override
  public Sector getMetaData(final Obligor obligor, final Sector element) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getSector();
  }

}
