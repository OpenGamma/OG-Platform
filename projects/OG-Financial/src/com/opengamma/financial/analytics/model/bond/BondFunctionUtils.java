/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public class BondFunctionUtils {

  public static UniqueId getCountryID(ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    final String codeAlpha2 = bond.getIssuerDomicile();
    return UniqueId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2.getName(), codeAlpha2);
  }
  
  public static UniqueId getCurrencyID(final ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    return bond.getCurrency().getUniqueId();
  }
  
  public static String getCountryName(ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    return bond.getIssuerDomicile();
  }
  
  public static String getCurrencyName(final ComputationTarget target) {
    final BondSecurity bond = (BondSecurity) target.getSecurity();
    return bond.getCurrency().getCode();
  }
}
