/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Custom Hibernate usertype for the EquityOptionType enum
 * 
 * @author andrew
 */
public class EquityOptionTypeUserType extends EnumUserType<EquityOptionType> {

  public EquityOptionTypeUserType () {
    super (EquityOptionType.class);
  }

  @Override
  protected String enumToString(EquityOptionType value) {
    switch (value) {
    case AMERICAN : return "American";
    case EUROPEAN : return "European";
    default : throw new OpenGammaRuntimeException ("unexpected value " + value);
    }
  }

  @Override
  protected EquityOptionType stringToEnum(String string) {
    if (string.equals ("American")) {
      return EquityOptionType.AMERICAN;
    } else if (string.equals ("European")) {
      return EquityOptionType.EUROPEAN;
    } else {
      throw new OpenGammaRuntimeException ("unexpected value " + string);
    }
  }
  
}