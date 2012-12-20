/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.swap.VarianceSwapType;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Hibernate user type for the {@link VarianceSwapType} enum.
 */
public class VarianceSwapTypeUserType extends EnumUserType<VarianceSwapType> {

  public VarianceSwapTypeUserType() {
    super(VarianceSwapType.class, VarianceSwapType.values());
  }

  @Override
  protected String enumToStringNoCache(VarianceSwapType value) {
    switch (value) {
      case VARIANCE:
        return "variance";
      case VEGA:
        return "vega";
      default:
        throw new OpenGammaRuntimeException("unexpected value " + value);
    }
  }
}
