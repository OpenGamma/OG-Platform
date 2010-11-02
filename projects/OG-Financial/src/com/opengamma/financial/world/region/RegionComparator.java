/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.world.region;

import java.util.Collections;
import java.util.Comparator;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Region comparator based on the region classification, then name.
 */
public final class RegionComparator implements Comparator<Region> {

  /**
   * A singleton region comparator sorting in ascending order.
   */
  public static final Comparator<Region> ASC = new RegionComparator();
  /**
   * A singleton region comparator sorting in descending order.
   */
  public static final Comparator<Region> DESC = Collections.reverseOrder(ASC);

  /**
   * Restrictive constructor.
   */
  private RegionComparator() {
  }

  //-------------------------------------------------------------------------
  @Override
  public int compare(Region r1, Region r2) {
    int type = r1.getClassification().compareTo(r2.getClassification());
    if (type != 0) {
      return type;
    }
    int name = r1.getName().compareTo(r2.getName());
    if (name != 0) {
      return name;
    }
    if (r1.equals(r2)) {
      return 0;
    } else {
      throw new OpenGammaRuntimeException("Non-unique name/classification present");
    }
  }

}
