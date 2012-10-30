/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.greeks;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AvailableGreeks {
  private static final BiMap<String, Greek> AVAILABLE_GREEKS;

  static {
    AVAILABLE_GREEKS = HashBiMap.create();
    AVAILABLE_GREEKS.put(ValueRequirementNames.FAIR_VALUE, Greek.FAIR_PRICE);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DELTA, Greek.DELTA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DELTA_BLEED, Greek.DELTA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.STRIKE_DELTA, Greek.STRIKE_DELTA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DRIFTLESS_THETA, Greek.DRIFTLESS_THETA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA, Greek.GAMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_P, Greek.GAMMA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.STRIKE_GAMMA, Greek.STRIKE_GAMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_BLEED, Greek.GAMMA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_P_BLEED, Greek.GAMMA_P_BLEED);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA, Greek.VEGA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA_P, Greek.VEGA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VEGA, Greek.VARIANCE_VEGA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA_BLEED, Greek.VEGA_BLEED);

    AVAILABLE_GREEKS.put(ValueRequirementNames.THETA, Greek.THETA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.RHO, Greek.RHO);
    AVAILABLE_GREEKS.put(ValueRequirementNames.CARRY_RHO, Greek.CARRY_RHO);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ZETA, Greek.ZETA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.ZETA_BLEED, Greek.ZETA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DZETA_DVOL, Greek.DZETA_DVOL);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ELASTICITY, Greek.ELASTICITY);
    AVAILABLE_GREEKS.put(ValueRequirementNames.PHI, Greek.PHI);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ZOMMA, Greek.ZOMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.ZOMMA_P, Greek.ZOMMA_P);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ULTIMA, Greek.ULTIMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_ULTIMA, Greek.VARIANCE_ULTIMA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.SPEED, Greek.SPEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.SPEED_P, Greek.SPEED_P);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VANNA, Greek.VANNA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VANNA, Greek.VARIANCE_VANNA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DVANNA_DVOL, Greek.DVANNA_DVOL);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VOMMA, Greek.VOMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VOMMA_P, Greek.VOMMA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VOMMA, Greek.VARIANCE_VOMMA);
  }

  public static Greek getGreekForValueRequirementName(final String valueName) {
    if (!AVAILABLE_GREEKS.containsKey(valueName)) {
      throw new IllegalArgumentException("Could not get greek for ValueRequirementName " + valueName);
    }
    return AVAILABLE_GREEKS.get(valueName);
  }

  public static Greek getGreekForValueRequirement(final ValueRequirement requirement) {
    final String greekName = requirement.getValueName();
    if (!AVAILABLE_GREEKS.containsKey(greekName)) {
      throw new IllegalArgumentException("Could not get greek for ValueRequirement " + requirement.toString());
    }
    return AVAILABLE_GREEKS.get(greekName);
  }

  public static Set<String> getAllGreekNames() {
    return AVAILABLE_GREEKS.keySet();
  }

  public static Map<String, Greek> getAllGreekNamesAndGreeks() {
    return AVAILABLE_GREEKS;
  }

  public static String getValueRequirementNameForGreek(final Greek greek) {
    ArgumentChecker.notNull(greek, "greek");
    return AVAILABLE_GREEKS.inverse().get(greek);
  }
}
