/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class EquityBlackVolatilitySurfacePerTickerDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityBlackVolatilitySurfacePerTickerDefaults.class);
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
  /** Ids to forward curve names */
  private final Map<String, Set<String>> _idToForwardCurveName;
  /** Ids to curve calculation method names */
  private final Map<String, Set<String>> _idToForwardCurveCalculationMethodName;
  /** Ids to surface names */
  private final Map<String, Set<String>> _idToSurfaceName;
  /** The priority of these defaults */
  private final PriorityClass _priority;

  /**
   * @param priority The priority of these defaults, not null
   * @param defaultsPerId The defaults per id, not null.
   */
  public EquityBlackVolatilitySurfacePerTickerDefaults(final String priority, final String... defaultsPerId) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(defaultsPerId, "defaults per id");
    final int n = defaultsPerId.length;
    ArgumentChecker.isTrue(n % 4 == 0, "Need one forward curve name, forward curve calculation method and surface name per id value");
    _priority = PriorityClass.valueOf(priority);
    _idToForwardCurveName = Maps.newLinkedHashMap();
    _idToForwardCurveCalculationMethodName = Maps.newLinkedHashMap();
    _idToSurfaceName = Maps.newLinkedHashMap();
    for (int i = 0; i < n; i += 4) {
      final String id = defaultsPerId[i];
      _idToForwardCurveName.put(id, Collections.singleton(defaultsPerId[i + 1]));
      _idToForwardCurveCalculationMethodName.put(id, Collections.singleton(defaultsPerId[i + 2]));
      _idToSurfaceName.put(id, Collections.singleton(defaultsPerId[i + 3]));
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    //FIXME: Modify to take ExternalId to avoid incorrect cast to UniqueId
    if (!(target.getValue() instanceof ExternalIdentifiable)) {
      return false;
    }
    ExternalId id = ((ExternalIdentifiable) target.getValue()).getExternalId();
    final String equityId = EquitySecurityUtils.getIndexOrEquityName(id);
    if (equityId == null) {
      return false;
    }
    return _idToForwardCurveName.containsKey(equityId.toUpperCase());
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (!desiredValue.getConstraint(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE).equals(InstrumentTypeProperties.EQUITY_OPTION)) {
      return null;
    }
    final String id = target.getUniqueId().getValue();
    switch (propertyName) {
      case ValuePropertyNames.CURVE:
        return _idToForwardCurveName.get(id);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD:
        return _idToForwardCurveCalculationMethodName.get(id);
      case ValuePropertyNames.SURFACE:
        return _idToSurfaceName.get(id);
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
    return OpenGammaFunctionExclusions.BLACK_VOLATILITY_SURFACE_DEFAULTS;
  }

}
