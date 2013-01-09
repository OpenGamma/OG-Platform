/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults aapropriate
 * for pricing using an interpolated Black lognormal volatility surface.
 */
public class EquityOptionInterpolatedBlackLognormalDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionInterpolatedBlackLognormalDefaults.class);
  /** Map of equity name to discounting curve configuration */
  private final Map<String, String> _equityToDiscountingCurveConfig;
  /** Map of equity name to discounting curve name */
  private final Map<String, String> _equityToDiscountingCurveName;
  /** Map of equity name to volatility surface name */
  private final Map<String, String> _equityToSurfaceName;
  /** Map of equity name to volatility surface calculation method name */
  private final Map<String, String> _equityToSurfaceInterpolatorName;
  /** Map of equity name to forward curve name */
  private final Map<String, String> _equityToForwardCurveName;
  /** Map of equity name to forward curve calculation method name */
  private final Map<String, String> _equityToForwardCurveCalculationMethodName;
  /** The priority of this set of defaults */
  private final PriorityClass _priority;

  /** The value requirement names for which these defaults apply */
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
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
    ValueRequirementNames.VALUE_RHO,
    ValueRequirementNames.VALUE_CARRY_RHO,
    ValueRequirementNames.VALUE_THETA,
    ValueRequirementNames.VALUE_DUAL_DELTA
  };

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other, not null
   * @param perEquityConfig Defaults values of curve configuration, discounting curve, surface name and interpolation method per equity, not null
   */
  public EquityOptionInterpolatedBlackLognormalDefaults(final String priority, final String... perEquityConfig) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perEquityConfig, "per equity configuration");
    _priority = PriorityClass.valueOf(priority);

    final int nPairs = perEquityConfig.length;
    ArgumentChecker.isTrue(nPairs % 7 == 0, "Must have discounting name, discounting curve config, surface name, surface interpolation method, forward curve name" +
      "and forward curve calculation method per equity");
    _equityToDiscountingCurveName = Maps.newHashMap();
    _equityToDiscountingCurveConfig = Maps.newHashMap();
    _equityToSurfaceName = Maps.newHashMap();
    _equityToSurfaceInterpolatorName = Maps.newHashMap();
    _equityToForwardCurveName = Maps.newHashMap();
    _equityToForwardCurveCalculationMethodName = Maps.newHashMap();
    for (int i = 0; i < perEquityConfig.length; i += 7) {
      final String equity = perEquityConfig[i].toUpperCase();
      _equityToDiscountingCurveName.put(equity, perEquityConfig[i + 1]);
      _equityToDiscountingCurveConfig.put(equity, perEquityConfig[i + 2]);
      _equityToSurfaceName.put(equity, perEquityConfig[i + 3]);
      _equityToSurfaceInterpolatorName.put(equity, perEquityConfig[i + 4]);
      _equityToForwardCurveName.put(equity, perEquityConfig[i + 5]);
      _equityToForwardCurveCalculationMethodName.put(equity, perEquityConfig[i + 6]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security eqSec = target.getSecurity();
    if (!((eqSec instanceof EquityIndexOptionSecurity) || (eqSec instanceof EquityBarrierOptionSecurity) || (eqSec instanceof EquityOptionSecurity))) {
      return false;
    }
    final String equity = EquitySecurityUtils.getIndexOrEquityName(eqSec);
    return _equityToDiscountingCurveName.containsKey(equity);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG);
      defaults.addValuePropertyName(valueName, EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME);
      defaults.addValuePropertyName(valueName, EquityOptionFunction.PROPERTY_FORWARD_CURVE_NAME);
      defaults.addValuePropertyName(valueName, ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueName, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Set<String> surfaceCalculationMethod = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE_CALCULATION_METHOD);
    if (surfaceCalculationMethod != null && surfaceCalculationMethod.size() == 1) {
      if (!Iterables.getOnlyElement(surfaceCalculationMethod).equals(BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL)) {
        return null;
      }
    }
    final String equity = EquitySecurityUtils.getIndexOrEquityName(target.getSecurity());
    if (!_equityToDiscountingCurveConfig.containsKey(equity)) {
      s_logger.error("Could not find defaults for {}", equity);
      return null;
    }
    if (EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG.equals(propertyName)) {
      return Collections.singleton(_equityToDiscountingCurveConfig.get(equity));
    }
    if (EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_equityToDiscountingCurveName.get(equity));
    }
    if (EquityOptionFunction.PROPERTY_FORWARD_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_equityToForwardCurveName.get(equity));
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_equityToForwardCurveCalculationMethodName.get(equity));
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_equityToSurfaceName.get(equity));
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_equityToSurfaceInterpolatorName.get(equity));
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
    return OpenGammaFunctionExclusions.EQUITY_OPTION_INTERPOLATED_BLACK_LOGNORMAL_DEFAULTS;
  }
}
