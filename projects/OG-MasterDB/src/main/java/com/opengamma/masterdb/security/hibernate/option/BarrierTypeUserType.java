/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the BarrierType enum
 */
public class BarrierTypeUserType extends EnumUserType<BarrierType> {

  public BarrierTypeUserType() {
    super(BarrierType.class, BarrierType.values());
  }
  
  @Override
  protected String enumToStringNoCache(BarrierType value) {
    switch (value) {
      case UP:
        return "up";
      case DOWN:
        return "down";
      case DOUBLE:
        return "double";
      default:
        throw new OpenGammaRuntimeException("unexpected value " + value);
    }
  }

}
