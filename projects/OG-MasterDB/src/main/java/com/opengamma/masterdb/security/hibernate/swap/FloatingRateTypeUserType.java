/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the FloatingRateType enum
 */
public class FloatingRateTypeUserType extends EnumUserType<FloatingRateType> {

  public FloatingRateTypeUserType() {
    super(FloatingRateType.class, FloatingRateType.values());
  }

  @Override
  protected String enumToStringNoCache(FloatingRateType value) {
    switch (value) {
      case IBOR:
        return "ibor";
      case CMS:
        return "cms";
      case OIS:
        return "ois";
      default:
        throw new OpenGammaRuntimeException("unexpected value " + value);
    }
  }

}
