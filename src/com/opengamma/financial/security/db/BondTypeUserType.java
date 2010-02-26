/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Custom Hibernate usertype for the BondType enum
 * 
 * @author andrew
 */
public class BondTypeUserType extends EnumUserType<BondType> {

  public BondTypeUserType () {
    super (BondType.class);
  }

  @Override
  protected String enumToString(BondType value) {
    switch (value) {
    case CORPORATE : return "Corporate";
    case MUNICIPAL : return "Municipal";
    case GOVERNMENT : return "Government";
    default : throw new OpenGammaRuntimeException ("unexpected value " + value);
    }
  }

  @Override
  protected BondType stringToEnum(String string) {
    if (string.equals ("Corporate")) {
      return BondType.CORPORATE;
    } else if (string.equals ("Municipal")) {
      return BondType.MUNICIPAL;
    } else if (string.equals ("Government")) {
      return BondType.GOVERNMENT;
    } else {
      throw new OpenGammaRuntimeException ("unexpected value " + string);
    }
  }
  
}