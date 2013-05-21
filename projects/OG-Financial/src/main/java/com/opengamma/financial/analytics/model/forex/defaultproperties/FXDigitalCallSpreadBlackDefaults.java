/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.FXDigitalCallSpreadBlackFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class FXDigitalCallSpreadBlackDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_GAMMA_P,
    ValueRequirementNames.VEGA_MATRIX,
    ValueRequirementNames.VEGA_QUOTE_MATRIX,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.CALL_SPREAD_VALUE_VEGA,
    ValueRequirementNames.VALUE_THETA
  };
  private final String _spread;

  /**
   * @param spread The spread to use
   */
  public FXDigitalCallSpreadBlackDefaults(final String spread) {
    super(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY.or(FinancialSecurityTypes.NON_DELIVERABLE_FX_DIGITAL_OPTION_SECURITY), true);
    ArgumentChecker.notNull(spread, "spread");
    _spread = spread;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, FXDigitalCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (FXDigitalCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE.equals(propertyName)) {
      return Collections.singleton(_spread);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.FX_DIGITAL_OPTION_CALL_SPREAD_BLACK_DEFAULTS;
  }
}
