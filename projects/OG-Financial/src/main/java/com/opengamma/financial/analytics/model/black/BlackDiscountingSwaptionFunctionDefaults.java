/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.SwaptionSecurity;

/**
 * Provides "Surface" (DEFAULT) and "VolatilityModel" (Black)
 * TODO - Remove or refactor this Function. Created for purpose of demonstration.
 */
public class BlackDiscountingSwaptionFunctionDefaults extends DefaultPropertyFunction {

  /** The value requirement names that the function applies to */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY,
    ValueRequirementNames.BUCKETED_PV01
  };
  
  public BlackDiscountingSwaptionFunctionDefaults() {
    super(FinancialSecurityTypes.SWAPTION_SECURITY, true);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof SwaptionSecurity;
  }
  
  @Override
  protected void getDefaults(PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL);
    }
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName) {
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton("DEFAULT");
    }
    if (SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL.equals(propertyName)) {
      return Collections.singleton("Black");
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SWAPTION_BLACK_DEFAULTS;
  }
}
