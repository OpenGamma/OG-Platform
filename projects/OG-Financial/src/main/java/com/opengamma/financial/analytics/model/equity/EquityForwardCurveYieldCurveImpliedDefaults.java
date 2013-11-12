/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates {@link EquityForwardCurveFunction} with default properties
 */
public class EquityForwardCurveYieldCurveImpliedDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCurveYieldCurveImpliedDefaults.class);
  /** The value requirements for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FORWARD_CURVE,
    ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA
  };
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** The defaults to apply */
  private final String _currency;
  private final String _curveName;
  private final String _curveCalculationConfig;
  private final String _dividendType;
  /** The possible schemes of the primitive target */
  /** The supported schemes */
  private static final Set<ExternalScheme> s_validSchemes = ImmutableSet.of(ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.ACTIVFEED_TICKER);

  /**
   * @param priority The priority, not null
   * @param defaultArray The default values, not null: currency, 
   */
  public EquityForwardCurveYieldCurveImpliedDefaults(final String priority, final String... defaultArray) {
    super(ComputationTargetType.PRIMITIVE, true); // // [PLAT-2286]: change to correct type; should this be SECURITY?
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(defaultArray, "per currency config");
    final int n = defaultArray.length;
    ArgumentChecker.isTrue(n == 4, "Must have 4 defaults: currency, curve, curve config, dividend type");
    _priority = PriorityClass.valueOf(priority);
    _currency = defaultArray[0];
    _curveName = defaultArray[1];
    _curveCalculationConfig = defaultArray[2];
    _dividendType = defaultArray[3];
    ArgumentChecker.isTrue(_dividendType.equals(ValuePropertyNames.DIVIDEND_TYPE_CONTINUOUS) || _dividendType.equals(ValuePropertyNames.DIVIDEND_TYPE_DISCRETE), 
        "4th String in defaultArray must be either Discrete or Continuous");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getValue() instanceof ExternalIdentifiable) {
      final ExternalId identifier = ((ExternalIdentifiable) target.getValue()).getExternalId();
      return s_validSchemes.contains(identifier.getScheme());
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    // Properties For all ValueRequirement's
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CURRENCY);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    }
    // Properties specific to FORWARD_CURVE
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.DIVIDEND_TYPE);
    //  Properties specific to STANDARD_VOLATILITY_SURFACE_DATA
    defaults.addValuePropertyName(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ValuePropertyNames.DISCOUNTING_CURVE_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ValuePropertyNames.FORWARD_CURVE_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);    
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    switch (propertyName) {
      case ValuePropertyNames.CURVE_CURRENCY:
        return Collections.singleton(_currency);
      case ValuePropertyNames.CURVE_CALCULATION_CONFIG:
        return Collections.singleton(_curveCalculationConfig); 
      case ValuePropertyNames.CURVE:
        return Collections.singleton(_curveName);
      case ValuePropertyNames.DIVIDEND_TYPE:
        return Collections.singleton(_dividendType);
      case ValuePropertyNames.DISCOUNTING_CURVE_NAME:
        return Collections.singleton(_curveName);
      case ValuePropertyNames.FORWARD_CURVE_NAME:
        return Collections.singleton(_curveName);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD:
        return  Collections.singleton(ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
      default:
        s_logger.error("Could not find default value for {} in this function", propertyName);
        return null;
    }
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_FORWARD_CURVE_DEFAULTS;
  }
}
