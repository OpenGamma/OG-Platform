/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

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
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates {@link CommodityFutureOptionFunction} with defaults appropriate for pricing using an interpolated Black lognormal volatility surface.
 */
public class CommodityFutureOptionSurfaceCalculationMethodDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(CommodityFutureOptionSurfaceCalculationMethodDefaults.class);
  /** Map of currency name to surface calculation method */
  private final Map<String, String> _currencyToSurfaceCalculationMethod;
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
      ValueRequirementNames.VEGA
  };

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other, not null
   * @param perCurrencyConfig Default values of surface calculation method per currency, not null
   */
  public CommodityFutureOptionSurfaceCalculationMethodDefaults(final String priority, final String... perCurrencyConfig) {
    super(FinancialSecurityTypes.COMMODITY_FUTURE_OPTION_SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perCurrencyConfig, "per currency configuration");
    _priority = PriorityClass.valueOf(priority);
    final int nPairs = perCurrencyConfig.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have surface calculation method per currency");
    _currencyToSurfaceCalculationMethod = Maps.newHashMap();
    for (int i = 0; i < perCurrencyConfig.length; i += 2) {
      final String currency = perCurrencyConfig[i].toUpperCase();
      _currencyToSurfaceCalculationMethod.put(currency, perCurrencyConfig[i + 1]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    return _currencyToSurfaceCalculationMethod.containsKey(currency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SURFACE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (!_currencyToSurfaceCalculationMethod.containsKey(currency)) {
      s_logger.error("Could not find defaults for {}", currency);
      return null;
    }
    if (ValuePropertyNames.SURFACE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_currencyToSurfaceCalculationMethod.get(currency));
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
    return OpenGammaFunctionExclusions.SURFACE_CALCULATION_METHOD_DEFAULTS;
  }

}
