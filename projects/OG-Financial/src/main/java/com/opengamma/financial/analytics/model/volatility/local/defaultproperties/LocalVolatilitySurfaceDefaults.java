/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.defaultproperties;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class LocalVolatilitySurfaceDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(LocalVolatilitySurfaceDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
      ValueRequirementNames.FORWARD_DELTA,
      ValueRequirementNames.DUAL_DELTA,
      ValueRequirementNames.DUAL_GAMMA,
      ValueRequirementNames.FORWARD_GAMMA,
      ValueRequirementNames.FOREX_DOMESTIC_PRICE,
      ValueRequirementNames.FOREX_PV_QUOTES,
      ValueRequirementNames.FORWARD_VEGA,
      ValueRequirementNames.FORWARD_VOMMA,
      ValueRequirementNames.FORWARD_VANNA,
      ValueRequirementNames.PRESENT_VALUE,
      ValueRequirementNames.FX_PRESENT_VALUE,
      ValueRequirementNames.IMPLIED_VOLATILITY,
      ValueRequirementNames.GRID_DUAL_DELTA,
      ValueRequirementNames.GRID_DUAL_GAMMA,
      ValueRequirementNames.GRID_FORWARD_DELTA,
      ValueRequirementNames.GRID_FORWARD_GAMMA,
      ValueRequirementNames.GRID_FORWARD_VEGA,
      ValueRequirementNames.GRID_FORWARD_VANNA,
      ValueRequirementNames.GRID_FORWARD_VOMMA,
      ValueRequirementNames.GRID_IMPLIED_VOLATILITY,
      ValueRequirementNames.GRID_PRESENT_VALUE
  };
  private final String _eps;

  public LocalVolatilitySurfaceDefaults(final String eps) {
    super(ComputationTargetType.CURRENCY
        .or(ComputationTargetType.UNORDERED_CURRENCY_PAIR)
        .or(FinancialSecurityTypes.FX_OPTION_SECURITY)
        .or(FinancialSecurityTypes.EQUITY_VARIANCE_SWAP_SECURITY), true);
    ArgumentChecker.notNull(eps, "eps");
    _eps = eps;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_DERIVATIVE_EPS);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_DERIVATIVE_EPS.equals(propertyName)) {
      return Collections.singleton(_eps);
    }
    s_logger.error("Could not get default value for {}", propertyName);
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.LOCAL_VOLATILITY_SURFACE_DEFAULTS;
  }

}
