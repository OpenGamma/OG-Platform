/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.VanillaFutureSecurity;


public enum FutureType {
  BOND,
  FX,
  VANILLA;
  
  public static FutureType identify (final Object o) {
    if (o instanceof BondFutureSecurity) {
      return BOND;
    } else if (o instanceof FXFutureSecurity) {
      return FX;
    } else if (o instanceof VanillaFutureSecurity) {
      return VANILLA;
    } else {
      throw new OpenGammaRuntimeException ("can't identify " + o);
    }
  }
  
}
