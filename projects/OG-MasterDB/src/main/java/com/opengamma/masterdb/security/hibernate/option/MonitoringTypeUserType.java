/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the MonitoringType enum
 */
public class MonitoringTypeUserType extends EnumUserType<MonitoringType> {

  public MonitoringTypeUserType() {
    super(MonitoringType.class, MonitoringType.values());
  }

  @Override
  protected String enumToStringNoCache(MonitoringType value) {
    switch (value) {
      case CONTINUOUS:
        return "continuous";
      case DISCRETE:
        return "descrete";
      default:
        throw new OpenGammaRuntimeException("unexpected value " + value);
    }
  }

}
