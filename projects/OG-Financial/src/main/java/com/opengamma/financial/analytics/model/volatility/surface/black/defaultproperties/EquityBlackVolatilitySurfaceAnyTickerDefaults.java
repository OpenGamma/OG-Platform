/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class EquityBlackVolatilitySurfaceDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityBlackVolatilitySurfaceDefaults.class);
  /** The value requirements for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
    ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
    ValueRequirementNames.PURE_VOLATILITY_SURFACE,
    ValueRequirementNames.FORWARD_DELTA,
    ValueRequirementNames.DUAL_DELTA,
    ValueRequirementNames.DUAL_GAMMA,
    ValueRequirementNames.FORWARD_GAMMA,
    ValueRequirementNames.FORWARD_VEGA,
    ValueRequirementNames.FORWARD_VOMMA,
    ValueRequirementNames.FORWARD_VANNA,
    ValueRequirementNames.PRESENT_VALUE,
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
  /** Ids to curve names */
  private final Map<String, String> _idToCurveName;
  /** Ids to curve calculation method names */
  private final Map<String, String> _idToCurveCalculationMethodName;
  /** Ids to curve currency names */
  private final Map<String, String> _idToCurveCurrency;
  /** Ids to curve calculation configuration names */
  private final Map<String, String> _idToCurveCalculationConfig;
  /** Ids to surface names */
  private final Map<String, String> _idToSurfaceName;
  /** The priority of these defaults */
  private final PriorityClass _priority;

  /**
   * @param target The computation target
   * @param priority The priority of these defaults, not null
   * @param defaultsPerId The defaults per id, not null.
   */
  public EquityBlackVolatilitySurfaceDefaults(final ComputationTargetType target, final String priority, final String... defaultsPerId) {
    super(target, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(defaultsPerId, "defaults per id");
    final int n = defaultsPerId.length;
    ArgumentChecker.isTrue(n % 6 == 0, "Need one forward curve name, forward curve calculation method, curve currency, curve calculation config name and surface name per id value");
    _priority = PriorityClass.valueOf(priority);
    _idToCurveName = Maps.newLinkedHashMap();
    _idToCurveCalculationMethodName = Maps.newLinkedHashMap();
    _idToCurveCurrency = Maps.newLinkedHashMap();
    _idToCurveCalculationConfig = Maps.newLinkedHashMap();
    _idToSurfaceName = Maps.newLinkedHashMap();
    for (int i = 0; i < n; i += 6) {
      final String id = defaultsPerId[i];
      _idToCurveName.put(id, defaultsPerId[i + 1]);
      _idToCurveCalculationMethodName.put(id, defaultsPerId[i + 2]);
      _idToCurveCurrency.put(id, defaultsPerId[i + 3]);
      _idToCurveCalculationConfig.put(id, defaultsPerId[i + 4]);
      _idToSurfaceName.put(id, defaultsPerId[i + 5]);
    }
    int temp = 0;
    temp = temp + 1;
  }

  @Override
  public abstract boolean canApplyTo(FunctionCompilationContext context, final ComputationTarget target);

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CURRENCY);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String id = getId(target);
    final String curveName = _idToCurveName.get(id);
    if (curveName == null) {
      s_logger.error("Could not get curve name for {}; should never happen", target.getValue());
      return null;
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(curveName);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_idToCurveCalculationMethodName.get(id));
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_idToCurveCalculationConfig.get(id));
    }
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(_idToCurveCurrency.get(id));
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_idToSurfaceName.get(id));
    }
    s_logger.error("Could not find default value for {} in this function", propertyName);
    return null;
  }

  /**
   * Gets a set containing all ids.
   * @return A set containing all of the ids
   */
  protected Collection<String> getAllIds() {
    return _idToCurveName.keySet();
  }

  /**
   * @param target The target
   * @return The id for the target
   */
  protected abstract String getId(ComputationTarget target);

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_BLACK_VOLATILITY_SURFACE_DEFAULTS;
  }

}
