/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the OptionType enum
 */
public class OptionTypeUserType extends EnumUserType<OptionType> {

  public OptionTypeUserType() {
    super(OptionType.class, OptionType.values());
  }

  @Override
  protected String enumToStringNoCache(OptionType value) {
    switch (value) {
      case CALL:
        return "call";
      case PUT:
        return "put";
      default:
        throw new OpenGammaRuntimeException("unexpected value " + value);
    }
  }

}
