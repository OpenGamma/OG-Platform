/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates {@link ListedEquityOptionFunction} with defaults. Basic configuration for clients that use one forward curve/config pair and one discounting curve/curve pair.
 * <p>
 * This mimics {@link EquityOptionInterpolatedBlackLognormalDefaults}, which populates fields for the EquityOptionFunction family, 
 * but this doesn't set volatility surface properties.
 */
public abstract class ListedEquityOptionDefaults extends DefaultPropertyFunction {

  /** The value requirement names for which these defaults apply */
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.DELTA,
    ValueRequirementNames.GAMMA,
    ValueRequirementNames.VEGA,
    ValueRequirementNames.VOMMA,
    ValueRequirementNames.VANNA,
    ValueRequirementNames.RHO,
    ValueRequirementNames.CARRY_RHO,
    ValueRequirementNames.THETA,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.FORWARD,
    ValueRequirementNames.IMPLIED_VOLATILITY,
    ValueRequirementNames.PNL // Produced by EquityOption*ScenarioFunction
  };

  /** Map of id name to discounting curve configuration */
  private final Map<String, Set<String>> _idToDiscountingCurveConfig;
  /** Map of id name to discounting curve name */
  private final Map<String, Set<String>> _idToDiscountingCurveName;
  /** Map of id name to forward curve name */
  private final Map<String, Set<String>> _idToForwardCurveName;
  /** Map of id name to forward curve calculation method name */
  private final Map<String, Set<String>> _idToForwardCurveCalculationMethodName;
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ListedEquityOptionDefaults.class);

  /**
   * Basic constructor for configurations with just a single
   * 
   * @param priority PriorityClass name (e.g. PriorityClass.NORMAL.name())
   * @param perIdConfig Map of strings consisting of groups of 5 inputs: <p>
   * 0) id the unique string used to define a set of inputs<p>
   * 1) discountingCurveName Name of the discounting curve (e.g. "Discounting")<p>
   * 2) discountingCurveConfig Name of the curve configuration (e.g. "ExchangeTradedSingleCurveUSDConfig")<p>
   * 3) forwardCurveName Name of the forward curve (e.g. "Futures3M")<p>
   * 4) forwardCurveCalculationMethodName Calculation method for the Equity Forward Curve (e.g. "YieldCurveImplied")
   */
  public ListedEquityOptionDefaults(final String priority, final String... perIdConfig) {
    super(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY
        .or(FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY)
        .or(FinancialSecurityTypes.EQUITY_OPTION_SECURITY)
        .or(FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY), true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perIdConfig, "per id configuration");
    _priority = PriorityClass.valueOf(priority);

    final int nPairs = perIdConfig.length;
    ArgumentChecker.isTrue(nPairs % 5 == 0, "Must have discounting name, discounting curve config, surface name, surface interpolation method, forward curve name" +
        "and forward curve calculation method per id");
    _idToDiscountingCurveName = Maps.newHashMap();
    _idToDiscountingCurveConfig = Maps.newHashMap();
    _idToForwardCurveName = Maps.newHashMap();
    _idToForwardCurveCalculationMethodName = Maps.newHashMap();
    for (int i = 0; i < perIdConfig.length; i += 5) {
      final String id = perIdConfig[i].toUpperCase();
      _idToDiscountingCurveName.put(id, Collections.singleton(perIdConfig[i + 1]));
      _idToDiscountingCurveConfig.put(id, Collections.singleton(perIdConfig[i + 2]));
      _idToForwardCurveName.put(id, Collections.singleton(perIdConfig[i + 3]));
      _idToForwardCurveCalculationMethodName.put(id, Collections.singleton(perIdConfig[i + 4]));
    }
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security eqSec = target.getSecurity();
    final String id = getId(eqSec);
    return _idToDiscountingCurveName.containsKey(id);
  }
  
  /**
   * @return All ids for which a default is available
   */
  protected Set<String> getAllIds() {
    return _idToDiscountingCurveConfig.keySet();
  }

  /**
   * @param security The security
   * @return The id for the security
   */
  protected abstract String getId(Security security);  
  @Override
  protected void getDefaults(PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG);
      defaults.addValuePropertyName(valueName, EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME);
      defaults.addValuePropertyName(valueName, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME);
      defaults.addValuePropertyName(valueName, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    if (!constraints.isDefined(ValuePropertyNames.CALCULATION_METHOD)) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String id = getId(target.getSecurity());
    switch (propertyName) {
      case EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG:
        return _idToDiscountingCurveConfig.get(id);
      case EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME:
        return _idToDiscountingCurveName.get(id);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME:
        return _idToForwardCurveName.get(id);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD:
        return _idToForwardCurveCalculationMethodName.get(id);
      default:
        s_logger.error("Cannot get a default value for {}", propertyName);
        return null;
    }
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_OPTION_DEFAULTS;
  }

}
