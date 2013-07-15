/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * If you wish to extend this class, compare to {@link EquityOptionInterpolatedBlackLognormalDefaults}. Unlike that Function, which populates fields for the EquityOptionFunction family, this one
 * doesn't set volatility surface properties.
 */
public class ListedEquityOptionDefaults extends DefaultPropertyFunction {

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
  private final String _discountingCurveConfig;
  /** Map of id name to discounting curve name */
  private final String _discountingCurveName;
  /** Map of id name to forward curve name */
  private final String _forwardCurveName;
  /** Map of id name to forward curve calculation method name */
  private final String _forwardCurveCalculationMethodName;
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ListedEquityOptionDefaults.class);

  /**
   * Basic constructor for configurations with just a single
   * 
   * @param priority PriorityClass name (e.g. PriorityClass.NORMAL.name())
   * @param discountingCurveName Name of the discounting curve (e.g. "Discounting")
   * @param discountingCurveConfig Name of the curve configuration (e.g. "ExchangeTradedSingleCurveUSDConfig")
   * @param forwardCurveName Name of the forward curve (e.g. "Futures3M")
   * @param forwardCurveCalculationMethodName Calculation method for the Equity Forward Curve (e.g. "YieldCurveImplied")
   */
  public ListedEquityOptionDefaults(final String priority,
      final String discountingCurveName,
      final String discountingCurveConfig,
      final String forwardCurveName,
      final String forwardCurveCalculationMethodName) {
    super(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY
        .or(FinancialSecurityTypes.EQUITY_OPTION_SECURITY).or(FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY), true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(discountingCurveConfig, "discountingCurveConfig");
    ArgumentChecker.notNull(discountingCurveName, "discountingCurveName");
    ArgumentChecker.notNull(forwardCurveCalculationMethodName, "forwardCurveCalculationMethodName");
    ArgumentChecker.notNull(forwardCurveName, "forwardCurveName");
    _priority = PriorityClass.valueOf(priority);
    _discountingCurveConfig = discountingCurveConfig;
    _discountingCurveName = discountingCurveName;
    _forwardCurveName = forwardCurveName;
    _forwardCurveCalculationMethodName = forwardCurveCalculationMethodName;
  }

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
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName) {

    if (EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG.equals(propertyName)) {
      return Collections.singleton(_discountingCurveConfig);
    }
    if (EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_discountingCurveName);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_forwardCurveName);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_forwardCurveCalculationMethodName);
    }
    s_logger.error("Cannot get a default value for {}", propertyName);
    return null;
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
