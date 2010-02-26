/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.OptionType;

/**
 * Custom Hibernate usertype for the OptionType enum
 * 
 * @author andrew
 */
public class OptionTypeUserType extends EnumUserType<OptionType> {

  public OptionTypeUserType () {
    super (OptionType.class);
  }

  @Override
  protected String enumToString(OptionType value) {
    switch (value) {
    case CALL : return "call";
    case PUT : return "put";
    default : throw new OpenGammaRuntimeException ("unexpected value " + value);
    }
  }

  @Override
  protected OptionType stringToEnum(String string) {
    if (string.equals ("call")) {
      return OptionType.CALL;
    } else if (string.equals ("put")) {
      return OptionType.PUT;
    } else {
      throw new OpenGammaRuntimeException ("unexpected value " + string);
    }
  }
  
}