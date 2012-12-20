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
import com.opengamma.analytics.financial.sensitivity.ValueGreek;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class AvailableValueGreeks {
  private static final Map<String, ValueGreek> AVAILABLE_VALUE_GREEKS;

  static {
    AVAILABLE_VALUE_GREEKS = new HashMap<String, ValueGreek>();
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_FAIR_VALUE, new ValueGreek(Greek.FAIR_PRICE));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_DELTA, new ValueGreek(Greek.DELTA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_DELTA_BLEED, new ValueGreek(Greek.DELTA_BLEED));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_STRIKE_DELTA, new ValueGreek(Greek.STRIKE_DELTA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_DRIFTLESS_DELTA, new ValueGreek(Greek.DRIFTLESS_THETA));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_GAMMA, new ValueGreek(Greek.GAMMA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_GAMMA_P, new ValueGreek(Greek.GAMMA_P));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_STRIKE_GAMMA, new ValueGreek(Greek.STRIKE_GAMMA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_GAMMA_BLEED, new ValueGreek(Greek.GAMMA_BLEED));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_GAMMA_P_BLEED, new ValueGreek(Greek.GAMMA_P_BLEED));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VEGA, new ValueGreek(Greek.VEGA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VEGA_P, new ValueGreek(Greek.VEGA_P));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VARIANCE_VEGA, new ValueGreek(Greek.VARIANCE_VEGA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VEGA_BLEED, new ValueGreek(Greek.VEGA_BLEED));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_THETA, new ValueGreek(Greek.THETA));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_RHO, new ValueGreek(Greek.RHO));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_CARRY_RHO, new ValueGreek(Greek.CARRY_RHO));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_ZETA, new ValueGreek(Greek.ZETA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_ZETA_BLEED, new ValueGreek(Greek.ZETA_BLEED));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_DZETA_DVOL, new ValueGreek(Greek.DZETA_DVOL));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_ELASTICITY, new ValueGreek(Greek.ELASTICITY));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_PHI, new ValueGreek(Greek.PHI));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_ZOMMA, new ValueGreek(Greek.ZOMMA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_ZOMMA_P, new ValueGreek(Greek.ZOMMA_P));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_ULTIMA, new ValueGreek(Greek.ULTIMA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VARIANCE_ULTIMA, new ValueGreek(Greek.VARIANCE_ULTIMA));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_SPEED, new ValueGreek(Greek.SPEED));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_SPEED_P, new ValueGreek(Greek.SPEED_P));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VANNA, new ValueGreek(Greek.VANNA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VARIANCE_VANNA, new ValueGreek(Greek.VARIANCE_VANNA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_DVANNA_DVOL, new ValueGreek(Greek.DVANNA_DVOL));

    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VOMMA, new ValueGreek(Greek.VOMMA));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VOMMA_P, new ValueGreek(Greek.VOMMA_P));
    AVAILABLE_VALUE_GREEKS.put(ValueRequirementNames.VALUE_VARIANCE_VOMMA, new ValueGreek(Greek.VARIANCE_VOMMA));
  }

  public static ValueGreek getValueGreekForValueRequirementName(final String valueName) {
    if (!AVAILABLE_VALUE_GREEKS.containsKey(valueName)) {
      throw new IllegalArgumentException("Could not get value greek for ValueRequirementName " + valueName);
    }
    return AVAILABLE_VALUE_GREEKS.get(valueName);
  }

  public static String getGreekRequirementNameForValueGreekName(final String valueName) {
    final ValueGreek valueGreek = AVAILABLE_VALUE_GREEKS.get(valueName);
    if (valueGreek == null) {
      throw new IllegalArgumentException("Could not get value greek for ValueRequirementName " + valueName);
    }
    return AvailableGreeks.getValueRequirementNameForGreek(valueGreek.getUnderlyingGreek());
  }

  public static ValueGreek getValueGreekForValueRequirement(final ValueRequirement requirement) {
    final String greekName = requirement.getValueName();
    if (!AVAILABLE_VALUE_GREEKS.containsKey(greekName)) {
      throw new IllegalArgumentException("Could not get value greek for ValueRequirement " + requirement.toString());
    }
    return AVAILABLE_VALUE_GREEKS.get(greekName);
  }

  public static Set<String> getAllValueGreekNames() {
    return AVAILABLE_VALUE_GREEKS.keySet();
  }

  public static Map<String, ValueGreek> getAllValueGreekNamesAndValueGreeks() {
    return AVAILABLE_VALUE_GREEKS;
  }
}
