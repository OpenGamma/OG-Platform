/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;

public enum EquityOptionType {
  AMERICAN,
  EUROPEAN;
  
  public static EquityOptionType identify (Object o) {
    if (o instanceof AmericanVanillaEquityOptionSecurity) {
      return AMERICAN;
    } else if (o instanceof EuropeanVanillaEquityOptionSecurity) {
      return EUROPEAN;
    } else {
      throw new OpenGammaRuntimeException ("can't identify " + o);
    }
  }
  
}