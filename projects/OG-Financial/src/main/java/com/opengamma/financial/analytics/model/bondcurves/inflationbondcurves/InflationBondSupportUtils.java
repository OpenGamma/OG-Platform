/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;

/**
 * tility methods for establishing if a bond or bill is supported.
 */
public class InflationBondSupportUtils {
  private static final Set<String> s_supportedYieldConventions = new HashSet<>();
  private static final Set<String> s_supportedCouponTypes = new HashSet<>();

  static {
    s_supportedYieldConventions.add(SimpleYieldConvention.US_STREET.getName());
    s_supportedYieldConventions.add(SimpleYieldConvention.GERMAN_BOND.getName());
    s_supportedYieldConventions.add(SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND.getName());
    s_supportedYieldConventions.add(SimpleYieldConvention.UK_BUMP_DMO_METHOD.getName());
    s_supportedYieldConventions.add(SimpleYieldConvention.FRANCE_COMPOUND_METHOD.getName());
    s_supportedYieldConventions.add(SimpleYieldConvention.ITALY_TREASURY_BONDS.getName());
    s_supportedYieldConventions.add(SimpleYieldConvention.INDEX_LINKED_FLOAT.getName());
    s_supportedYieldConventions.add(SimpleYieldConvention.UK_IL_BOND.getName());
    s_supportedCouponTypes.add("FIXED");
  }

  public static boolean isSupported(Security security) {
    if (security instanceof InflationBondSecurity) {
      BondSecurity bondSecurity = (BondSecurity) security;
      if (s_supportedYieldConventions.contains(bondSecurity.getYieldConvention().getName()) && s_supportedCouponTypes.contains(bondSecurity.getCouponType())) {
        return true;
      }
      return false;
    }
    return false;
  }

}
