/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.CorporateBondSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.MunicipalBondSecurity;

public enum BondType {
  CORPORATE,
  MUNICIPAL,
  GOVERNMENT;
  
  public static BondType identify (final BondSecurity object) {
    if (object instanceof CorporateBondSecurity) {
      return CORPORATE;
    } else if (object instanceof MunicipalBondSecurity) {
      return MUNICIPAL;
    } else if (object instanceof GovernmentBondSecurity) {
      return GOVERNMENT;
    } else {
      throw new OpenGammaRuntimeException ("can't identify " + object);
    }
  }
  
}