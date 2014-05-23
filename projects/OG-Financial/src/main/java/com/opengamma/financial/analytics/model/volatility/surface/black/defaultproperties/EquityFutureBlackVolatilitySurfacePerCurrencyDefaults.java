/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class EquityFutureBlackVolatilitySurfacePerCurrencyDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityFutureBlackVolatilitySurfacePerCurrencyDefaults.class);
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
  private final Map<String, Set<String>> _forwardCurveNames;
  /** Ids to curve calculation method names */
  private final Map<String, Set<String>> _forwardCurveCalculationMethodNames;
  /** Ids to surface names */
  private final Map<String, Set<String>> _surfaceNames;
  /** The priority of these defaults */
  private final PriorityClass _priority;

  /**
   * @param priority The priority of these defaults, not null
   * @param defaults The defaults, not null.
   */
  public EquityFutureBlackVolatilitySurfacePerCurrencyDefaults(final String priority, final String... defaults) {
    super(ComputationTargetType.PRIMITIVE, true); //TODO [PLAT-2286]: change to correct type; should this be SECURITY?
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(defaults, "defaults");
    final int n = defaults.length;
    ArgumentChecker.isTrue(n % 4 == 0, "Need one forward curve name, forward curve calculation method and surface name per currency");
    _priority = PriorityClass.valueOf(priority);
    _forwardCurveNames = new HashMap<>();
    _forwardCurveCalculationMethodNames = new HashMap<>();
    _surfaceNames = new HashMap<>();
    for (int i = 0; i < n; i += 4) {
      final String currencyName = defaults[i];
      _forwardCurveNames.put(currencyName, Collections.singleton(defaults[i + 1]));
      _forwardCurveCalculationMethodNames.put(currencyName, Collections.singleton(defaults[i + 2]));
      _surfaceNames.put(currencyName, Collections.singleton(defaults[i + 3]));
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    // TODO [PLAT-2286] If the target type is security then the resolver will do half the work that EquitySecurityUtils.getCurrency is doing
    // and it will just need to apply the currency lookup visitor to the resolved security object
    final UniqueId id = target.getUniqueId();
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final String currency = EquitySecurityUtils.getCurrency(securitySource, id);
    if (currency == null) {
      return false;
    }
    return _forwardCurveNames.containsKey(currency);
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
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final String currency = EquitySecurityUtils.getCurrency(securitySource, target.getUniqueId());
    if (currency == null) {
      s_logger.error("Could not get currency for {}; should never happen", target.getUniqueId());
      return null;
    }
    switch (propertyName) {
      case ValuePropertyNames.CURVE:
        return _forwardCurveNames.get(currency);
      case ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD:
        return _forwardCurveCalculationMethodNames.get(currency);
      case ValuePropertyNames.SURFACE:
        return _surfaceNames.get(currency);
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
