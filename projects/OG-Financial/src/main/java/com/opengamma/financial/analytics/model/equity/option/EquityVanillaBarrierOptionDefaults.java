/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 * Default properties to define the choices of overhedge (shift of strike)
 * and smoothing (width of ramp created by pricing binary as call or put spread)
 */
public class EquityVanillaBarrierOptionDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityVanillaBarrierOptionDefaults.class);
  /** Default value for the call spread width */
  private final String _callSpreadFullWidth;
  /** Default value for the overhedge */
  private final String _barrierOverhedge;

  /**
   * Value requirement names for which these properties apply
   */
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FORWARD,
    ValueRequirementNames.SPOT,
    ValueRequirementNames.VEGA_QUOTE_MATRIX,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.IMPLIED_VOLATILITY,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.FORWARD,
    ValueRequirementNames.SPOT,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_VOMMA,
    ValueRequirementNames.VALUE_VANNA,
    ValueRequirementNames.VALUE_RHO
  };

  /**
   * @param barrierOverhedge The overhedge value, not null
   * @param callSpreadFullWidth The call spread width, not null
   */
  public EquityVanillaBarrierOptionDefaults(final String barrierOverhedge, final String callSpreadFullWidth) {
    super(FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY, true);
    ArgumentChecker.notNull(barrierOverhedge, "barrier overhedge");
    ArgumentChecker.notNull(callSpreadFullWidth, "call spread width");
    _barrierOverhedge = barrierOverhedge;
    _callSpreadFullWidth = callSpreadFullWidth;
  }


  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.BINARY_OVERHEDGE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.BINARY_OVERHEDGE.equals(propertyName)) {
      return Collections.singleton(_barrierOverhedge);
    }
    if (ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH.equals(propertyName)) {
      return Collections.singleton(_callSpreadFullWidth);
    }
    s_logger.error("Could not get default value for {}", propertyName);
    return null;
  }

}
