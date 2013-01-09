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

import com.google.common.collect.Maps;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults aapropriate
 * for pricing using an interpolated Black lognormal volatility surface.
 */
public class EquityOptionSurfaceCalculationMethodDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionSurfaceCalculationMethodDefaults.class);
  /** Map of equity name to surface calculation method */
  private final Map<String, String> _equityToSurfaceCalculationMethod;
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
  public EquityOptionSurfaceCalculationMethodDefaults(final String priority, final String... perEquityConfig) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perEquityConfig, "per equity configuration");
    _priority = PriorityClass.valueOf(priority);

    final int nPairs = perEquityConfig.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have surface calculation method per equity");
    _equityToSurfaceCalculationMethod = Maps.newHashMap();
    for (int i = 0; i < perEquityConfig.length; i += 2) {
      final String equity = perEquityConfig[i].toUpperCase();
      _equityToSurfaceCalculationMethod.put(equity, perEquityConfig[i + 1]);
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
    return _equityToSurfaceCalculationMethod.containsKey(equity);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SURFACE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String equity = EquitySecurityUtils.getIndexOrEquityName(target.getSecurity());
    if (!_equityToSurfaceCalculationMethod.containsKey(equity)) {
      s_logger.error("Could not find defaults for {}", equity);
      return null;
    }
    if (ValuePropertyNames.SURFACE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_equityToSurfaceCalculationMethod.get(equity));
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
    return OpenGammaFunctionExclusions.EQUITY_OPTION_SURFACE_CALCULATION_METHOD_DEFAULTS;
  }
}
