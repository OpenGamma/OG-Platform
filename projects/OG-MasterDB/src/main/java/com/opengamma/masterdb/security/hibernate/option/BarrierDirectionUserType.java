/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the BarrierDirection enum
 */
public class BarrierDirectionUserType extends EnumUserType<BarrierDirection> {

  public BarrierDirectionUserType() {
    super(BarrierDirection.class, BarrierDirection.values());
  }

  @Override
  protected String enumToStringNoCache(BarrierDirection value) {
    switch (value) {
      case KNOCK_IN:
        return "knock_in";
      case KNOCK_OUT:
        return "knock_out";
      default:
        throw new OpenGammaRuntimeException("unexpected value " + value);
    }
  }

}
