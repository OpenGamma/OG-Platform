/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import java.util.Collections;
import java.util.Comparator;

import com.opengamma.core.region.impl.RegionComparator;

/**
 * Region comparator based on the region type, then name.
 */
public final class RegionDocumentComparator implements Comparator<RegionDocument> {

  /**
   * A singleton region comparator sorting in ascending order.
   */
  public static final Comparator<RegionDocument> ASC = new RegionDocumentComparator();
  /**
   * A singleton region comparator sorting in descending order.
   */
  public static final Comparator<RegionDocument> DESC = Collections.reverseOrder(ASC);

  /**
   * Restrictive constructor.
   */
  private RegionDocumentComparator() {
  }

  //-------------------------------------------------------------------------
  @Override
  public int compare(RegionDocument r1, RegionDocument r2) {
    return RegionComparator.ASC.compare(r1.getRegion(), r2.getRegion());
  }

}
