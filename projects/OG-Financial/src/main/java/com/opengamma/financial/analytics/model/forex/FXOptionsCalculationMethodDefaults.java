/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class FXOptionsCalculationMethodDefaults extends StaticDefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      ValueRequirementNames.PRESENT_VALUE,
      ValueRequirementNames.FX_PRESENT_VALUE,
      ValueRequirementNames.FX_CURRENCY_EXPOSURE,
      ValueRequirementNames.VALUE_DELTA,
      ValueRequirementNames.VALUE_VEGA,
      ValueRequirementNames.VALUE_GAMMA,
      ValueRequirementNames.VALUE_GAMMA_P,
      ValueRequirementNames.VEGA_MATRIX,
      ValueRequirementNames.VEGA_QUOTE_MATRIX,
      ValueRequirementNames.FX_CURVE_SENSITIVITIES,
      ValueRequirementNames.PV01,
      ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY,
      ValueRequirementNames.VALUE_THETA,
      ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
      ValueRequirementNames.VALUE_RHO,
      ValueRequirementNames.VALUE_PHI,
      ValueRequirementNames.VALUE_VOMMA,
      ValueRequirementNames.VALUE_VANNA
  };
  private final Set<String> _fxOptionCalculationMethod;
  private final Set<String> _fxDigitalOptionCalculationMethod;

  public FXOptionsCalculationMethodDefaults(final String fxOptionCalculationMethod, final String fxDigitalOptionCalculationMethod) {
    super(FinancialSecurityTypes.FX_OPTION_SECURITY
        .or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY)
        .or(FinancialSecurityTypes.NON_DELIVERABLE_FX_OPTION_SECURITY)
        .or(FinancialSecurityTypes.NON_DELIVERABLE_FX_DIGITAL_OPTION_SECURITY), ValuePropertyNames.CALCULATION_METHOD, true, VALUE_REQUIREMENTS);
    ArgumentChecker.notNull(fxOptionCalculationMethod, "FX option calculation method");
    ArgumentChecker.notNull(fxDigitalOptionCalculationMethod, "FX digital option calculation method");
    _fxOptionCalculationMethod = Collections.singleton(fxOptionCalculationMethod);
    _fxDigitalOptionCalculationMethod = Collections.singleton(fxDigitalOptionCalculationMethod);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, ValueRequirement desiredValue) {
    final Security security = target.getSecurity();
    if (security instanceof FXOptionSecurity || security instanceof NonDeliverableFXOptionSecurity) {
      return _fxOptionCalculationMethod;
    }
    if (security instanceof FXDigitalOptionSecurity || security instanceof NonDeliverableFXDigitalOptionSecurity) {
      return _fxDigitalOptionCalculationMethod;
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.CALCULATION_METHOD_DEFAULTS;
  }

}
