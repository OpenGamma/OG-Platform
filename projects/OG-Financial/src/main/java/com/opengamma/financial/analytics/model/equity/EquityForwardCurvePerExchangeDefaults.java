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

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class EquityForwardCurvePerExchangeDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCurvePerExchangeDefaults.class);
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** Map from exchange to curve configuration, curve name and currency */
  private final Map<String, Triple<String, String, String>> _perExchangeConfig;

  /**
   * @param priority The priority, not null
   * @param perExchangeConfig The default values per exchange, not null
   */
  public EquityForwardCurvePerExchangeDefaults(final String priority, final String... perExchangeConfig) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perExchangeConfig, "per equity config");
    final int nPairs = perExchangeConfig.length;
    ArgumentChecker.isTrue(nPairs % 4 == 0, "Must have one curve config, discounting curve name and currency per exchange");
    _priority = PriorityClass.valueOf(priority);
    _perExchangeConfig = new HashMap<>();
    for (int i = 0; i < perExchangeConfig.length; i += 4) {
      final Triple<String, String, String> config = new Triple<>(perExchangeConfig[i + 1], perExchangeConfig[i + 2], perExchangeConfig[i + 3]);
      _perExchangeConfig.put(perExchangeConfig[i].toUpperCase(), config);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId id = target.getUniqueId();
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final String exchange = getExchange(securitySource, id);
    if (exchange == null) {
      return false;
    }
    return _perExchangeConfig.containsKey(exchange);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE_CURRENCY);
    defaults.addValuePropertyName(ValueRequirementNames.FORWARD_CURVE, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final String exchange = getExchange(securitySource, target.getUniqueId());
    if (exchange == null) {
      s_logger.error("Could not get exchange for {}; should never happen", target.getUniqueId());
      return null;
    }
    final Triple<String, String, String> config = _perExchangeConfig.get(exchange);
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(config.getFirst());
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(config.getSecond());
    }
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(config.getThird());
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  private String getExchange(final SecuritySource securitySource, final UniqueId id) {
    final String scheme;
    final String originalScheme = id.getScheme();
    if (originalScheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) {
      scheme = ExternalSchemes.BLOOMBERG_TICKER.getName();
    } else if (originalScheme.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK.getName())) {
      scheme = ExternalSchemes.BLOOMBERG_BUID.getName();
    } else {
      scheme = originalScheme;
    }
    final String value = id.getValue();
    final ExternalId ticker = ExternalId.of(scheme, value);
    final Security security = securitySource.getSingle(ExternalIdBundle.of(ticker));
    if (security == null) {
      return null;
    }
    return FinancialSecurityUtils.getExchange(security).getValue();
  }

//  @Override
//  public String getMutualExclusionGroup() {
//    return OpenGammaFunctionExclusions.EQUITY_FORWARD_CURVE_DEFAULTS;
//  }

}
