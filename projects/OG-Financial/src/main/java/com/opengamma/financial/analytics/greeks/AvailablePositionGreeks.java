/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.greeks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.sensitivity.PositionGreek;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class AvailablePositionGreeks {
  private static final Map<String, PositionGreek> AVAILABLE_POSITION_GREEKS;

  static {
    AVAILABLE_POSITION_GREEKS = new HashMap<String, PositionGreek>();
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_FAIR_VALUE, new PositionGreek(Greek.FAIR_PRICE));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_DELTA, new PositionGreek(Greek.DELTA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_DELTA_BLEED, new PositionGreek(Greek.DELTA_BLEED));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_STRIKE_DELTA, new PositionGreek(Greek.STRIKE_DELTA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_DRIFTLESS_THETA, new PositionGreek(Greek.DRIFTLESS_THETA));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_GAMMA, new PositionGreek(Greek.GAMMA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_GAMMA_P, new PositionGreek(Greek.GAMMA_P));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_STRIKE_GAMMA, new PositionGreek(Greek.STRIKE_GAMMA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_GAMMA_BLEED, new PositionGreek(Greek.GAMMA_BLEED));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_GAMMA_P_BLEED, new PositionGreek(Greek.GAMMA_P_BLEED));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VEGA, new PositionGreek(Greek.VEGA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VEGA_P, new PositionGreek(Greek.VEGA_P));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VARIANCE_VEGA, new PositionGreek(Greek.VARIANCE_VEGA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VEGA_BLEED, new PositionGreek(Greek.VEGA_BLEED));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_THETA, new PositionGreek(Greek.THETA));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_RHO, new PositionGreek(Greek.RHO));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_CARRY_RHO, new PositionGreek(Greek.CARRY_RHO));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_ZETA, new PositionGreek(Greek.ZETA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_ZETA_BLEED, new PositionGreek(Greek.ZETA_BLEED));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_DZETA_DVOL, new PositionGreek(Greek.DZETA_DVOL));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_ELASTICITY, new PositionGreek(Greek.ELASTICITY));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_PHI, new PositionGreek(Greek.PHI));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_ZOMMA, new PositionGreek(Greek.ZOMMA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_ZOMMA_P, new PositionGreek(Greek.ZOMMA_P));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_ULTIMA, new PositionGreek(Greek.ULTIMA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VARIANCE_ULTIMA, new PositionGreek(Greek.VARIANCE_ULTIMA));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_SPEED, new PositionGreek(Greek.SPEED));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_SPEED_P, new PositionGreek(Greek.SPEED_P));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VANNA, new PositionGreek(Greek.VANNA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VARIANCE_VANNA, new PositionGreek(Greek.VARIANCE_VANNA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_DVANNA_DVOL, new PositionGreek(Greek.DVANNA_DVOL));

    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VOMMA, new PositionGreek(Greek.VOMMA));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VOMMA_P, new PositionGreek(Greek.VOMMA_P));
    AVAILABLE_POSITION_GREEKS.put(ValueRequirementNames.POSITION_VARIANCE_VOMMA, new PositionGreek(Greek.VARIANCE_VOMMA));
  }

  public static PositionGreek getPositionGreekForValueRequirementName(final String valueName) {
    if (!AVAILABLE_POSITION_GREEKS.containsKey(valueName)) {
      throw new IllegalArgumentException("Could not get position greek for ValueRequirementName " + valueName);
    }
    return AVAILABLE_POSITION_GREEKS.get(valueName);
  }

  public static PositionGreek getPositionGreekForValueRequirement(final ValueRequirement requirement) {
    final String greekName = requirement.getValueName();
    if (!AVAILABLE_POSITION_GREEKS.containsKey(greekName)) {
      throw new IllegalArgumentException("Could not get position greek for ValueRequirement " + requirement.toString());
    }
    return AVAILABLE_POSITION_GREEKS.get(greekName);
  }

  public static Set<String> getAllPositionGreekNames() {
    return AVAILABLE_POSITION_GREEKS.keySet();
  }

  public static Map<String, PositionGreek> getAllpositionGreekNamesAndPositionGreeks() {
    return AVAILABLE_POSITION_GREEKS;
  }
}
