/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the SamplingFrequency enum
 */
public class SamplingFrequencyUserType extends EnumUserType<SamplingFrequency> {

  public SamplingFrequencyUserType() {
    super(SamplingFrequency.class, SamplingFrequency.values());
  }

  @Override
  protected String enumToStringNoCache(SamplingFrequency value) {
    switch (value) {
      case DAILY_CLOSE:
        return "daily_close";
      case FRIDAY:
        return "friday";
      case WEEKLY_CLOSE:
        return "weekly_close";
      case CONTINUOUS:
        return "continuous";
      case ONE_LOOK:
        return "one_look";
      default:
        throw new OpenGammaRuntimeException("unexpected value " + value);
    }
  }

}
