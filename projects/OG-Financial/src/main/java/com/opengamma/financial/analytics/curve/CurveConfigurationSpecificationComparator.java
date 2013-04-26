/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Comparator;

/**
 * Comparator for curve configuration specifications. Note that this will sort in low-to-high order.
 */
public class CurveConfigurationSpecificationComparator implements Comparator<CurveConfigurationSpecification> {

  /**
   * 
   */
  public CurveConfigurationSpecificationComparator() {
  }

  @Override
  public int compare(final CurveConfigurationSpecification ccs1, final CurveConfigurationSpecification ccs2) {
    if (ccs1.getPriority() == ccs2.getPriority()) {
      return ccs1.getTargetId().compareTo(ccs2.getTargetId());
    }
    return ccs1.getPriority() - ccs2.getPriority();
  }

}
