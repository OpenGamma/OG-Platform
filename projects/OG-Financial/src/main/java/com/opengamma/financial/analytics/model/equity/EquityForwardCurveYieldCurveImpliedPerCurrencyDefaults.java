/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

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
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class EquityForwardCurveYieldCurveImpliedPerCurrencyDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCurveYieldCurveImpliedPerCurrencyDefaults.class);
  /** The value requirements for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FORWARD_CURVE,
    ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA
  };
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** Map from currency to curve configuration, curve name and currency */
  private final Map<String, Triple<String, String, String>> _perCurrencyConfig;

  /**
   * @param priority The priority, not null
   * @param perCurrencyConfig The default values per currency, not null
   */
  public EquityForwardCurveYieldCurveImpliedPerCurrencyDefaults(final String priority, final String... perCurrencyConfig) {
    super(ComputationTargetType.PRIMITIVE, true); // // [PLAT-2286]: change to correct type; should this be SECURITY?
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perCurrencyConfig, "per currency config");
    final int nPairs = perCurrencyConfig.length;
    ArgumentChecker.isTrue(nPairs % 4 == 0, "Must have one curve config and discounting curve name per currency");
    _priority = PriorityClass.valueOf(priority);
    _perCurrencyConfig = new HashMap<>();
    for (int i = 0; i < perCurrencyConfig.length; i += 4) {
      final Triple<String, String, String> config = Triple.of(perCurrencyConfig[i + 1], perCurrencyConfig[i + 2], perCurrencyConfig[i + 3]);
      _perCurrencyConfig.put(perCurrencyConfig[i].toUpperCase(), config);
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
    return _perCurrencyConfig.containsKey(currency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CURRENCY);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.DIVIDEND_TYPE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    //TODO put back in when calculation method property is added to EquityOptionVolatilitySurfaceDataFunction
//    if (!ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD.equals(desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD))) {
//      return null;
//    }
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final String currency = EquitySecurityUtils.getCurrency(securitySource, target.getUniqueId());
    if (currency == null) {
      s_logger.error("Could not get currency for {}; should never happen", target.getUniqueId());
      return null;
    }
    final Triple<String, String, String> config = _perCurrencyConfig.get(currency);
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(config.getFirst());
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(config.getSecond());
    }
    if (ValuePropertyNames.DIVIDEND_TYPE.equals(propertyName)) {
      return Collections.singleton(config.getThird());
    }
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(currency);
    }
    s_logger.error("Could not find default value for {} in this function", propertyName);
    return null;
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
