/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.OptionType;

/**
 * Custom Hibernate usertype for the OptionType enum
 * 
 * @author andrew
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