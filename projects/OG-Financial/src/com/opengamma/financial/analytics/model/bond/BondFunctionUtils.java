/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class BondFunctionUtils {

  public static UniqueIdentifier getCountryID(ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    final String codeAlpha2 = bond.getIssuerDomicile();
    return UniqueIdentifier.of(RegionUtils.ISO_COUNTRY_ALPHA2.getName(), codeAlpha2);
  }
  
  public static UniqueIdentifier getCurrencyID(final ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    return bond.getCurrency().getUniqueId();
  }
}
