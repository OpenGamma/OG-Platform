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
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;

/**
 * Utility methods for establishing if a bond or bill is supported.
 */
public class InflationBondSupportUtils {
  private static final Set<YieldConvention> s_supportedYieldConventions = new HashSet<>();
  private static final Set<String> s_supportedCouponTypes = new HashSet<>();

  static {
    s_supportedYieldConventions.add(SimpleYieldConvention.US_STREET);
    s_supportedYieldConventions.add(SimpleYieldConvention.GERMAN_BOND);
    s_supportedYieldConventions.add(SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND);
    s_supportedYieldConventions.add(SimpleYieldConvention.UK_BUMP_DMO_METHOD);
    s_supportedYieldConventions.add(SimpleYieldConvention.FRANCE_COMPOUND_METHOD);
    s_supportedYieldConventions.add(SimpleYieldConvention.ITALY_TREASURY_BONDS);
    s_supportedYieldConventions.add(SimpleYieldConvention.INDEX_LINKED_FLOAT);
    s_supportedYieldConventions.add(SimpleYieldConvention.UK_IL_BOND);
    s_supportedYieldConventions.add(SimpleYieldConvention.BRAZIL_IL_BOND);
    s_supportedCouponTypes.add("FIXED");
  }

  public static boolean isSupported(Security security) {
    if (security instanceof InflationBondSecurity) {
      BondSecurity bondSecurity = (BondSecurity) security;
      if (s_supportedYieldConventions.contains(bondSecurity.getYieldConvention()) && s_supportedCouponTypes.contains(bondSecurity.getCouponType())) {
        return true;
      }
      return false;
    }
    return false;
  }

}
