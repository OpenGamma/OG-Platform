/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the InterpolationMethod enum
 */
public class InterpolationMethodUserType extends EnumUserType<InterpolationMethod> {

  public InterpolationMethodUserType() {
    super(InterpolationMethod.class, InterpolationMethod.values());
  }
  
  @Override
  protected String enumToStringNoCache(InterpolationMethod value) {
    switch (value) {
      case MONTH_START_LINEAR:
        return "month_start_linear";
      case NONE:
        return "none";
      default:
        throw new OpenGammaRuntimeException("unexpected value " + value);
    }
  }
}
