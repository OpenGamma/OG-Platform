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
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults appropriate for pricing using an interpolated Black lognormal volatility surface.
 */
public abstract class EquityOptionSurfaceCalculationMethodDefaults extends StaticDefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionSurfaceCalculationMethodDefaults.class);
  /** Map of id name to surface calculation method */
  private final Map<String, Set<String>> _idToSurfaceCalculationMethod;
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
      ValueRequirementNames.VALUE_DUAL_DELTA,
      ValueRequirementNames.DELTA,
      ValueRequirementNames.GAMMA,
      ValueRequirementNames.VOMMA,
      ValueRequirementNames.VANNA,
      ValueRequirementNames.RHO,
      ValueRequirementNames.CARRY_RHO,
      ValueRequirementNames.THETA,
      ValueRequirementNames.DUAL_DELTA,
      ValueRequirementNames.VEGA,
      ValueRequirementNames.BARRIER_DISTANCE,
      ValueRequirementNames.PNL, // Produced by EquityOption*ScenarioFunction
      ValueRequirementNames.POSITION_DELTA,
      ValueRequirementNames.POSITION_GAMMA,
      ValueRequirementNames.POSITION_RHO,
      ValueRequirementNames.POSITION_THETA,
      ValueRequirementNames.POSITION_VEGA,
      ValueRequirementNames.POSITION_WEIGHTED_VEGA
  };

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other, not null
   * @param perIdConfig Default values of curve configuration, discounting curve, surface name and interpolation method per id, not null
   */
  public EquityOptionSurfaceCalculationMethodDefaults(final String priority, final String... perIdConfig) {
    super(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY
        .or(FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY)
        .or(FinancialSecurityTypes.EQUITY_OPTION_SECURITY), ValuePropertyNames.SURFACE_CALCULATION_METHOD, true, s_valueNames);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perIdConfig, "per id configuration");
    _priority = PriorityClass.valueOf(priority);
    final int nPairs = perIdConfig.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have surface calculation method per id");
    _idToSurfaceCalculationMethod = Maps.newHashMap();
    for (int i = 0; i < perIdConfig.length; i += 2) {
      final String id = perIdConfig[i].toUpperCase();
      _idToSurfaceCalculationMethod.put(id, Collections.singleton(perIdConfig[i + 1]));
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security eqSec = target.getSecurity();
    final String currency = getId(eqSec);
    return getAllIds().contains(currency);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (desiredValue.getConstraints().isDefined(ValuePropertyNames.CALCULATION_METHOD)) {
      return super.getRequirements(context, target, desiredValue);
    } else {
      return null;
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String id = getId(target.getSecurity());
    return _idToSurfaceCalculationMethod.get(id);
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SURFACE_CALCULATION_METHOD_DEFAULTS;
  }

  /**
   * @return All ids for which a default is available
   */
  protected Set<String> getAllIds() {
    return _idToSurfaceCalculationMethod.keySet();
  }

  /**
   * @param security The security
   * @return The id for the security
   */
  protected abstract String getId(Security security);
}
