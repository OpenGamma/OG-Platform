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

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class EquityForwardCurvePerTickerDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityForwardCurvePerTickerDefaults.class);
  /** The value requirements for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FORWARD_CURVE,
    ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA
  };
  /** The priority of this set of defaults */
  private final PriorityClass _priority;
  /** Map from ticker to curve configuration, curve name and currency */
  private final Map<String, String[]> _perEquityConfig;

  /**
   * @param priority The priority, not null
   * @param perEquityConfig The default values per equity, not null
   */
  public EquityForwardCurvePerTickerDefaults(final String priority, final String... perEquityConfig) {
    super(ComputationTargetType.PRIMITIVE, true); // REVIEW Andrew 2012-11-06 -- Is PRIMITIVE correct, shouldn't it be SECURITY or even EquitySecurity?
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perEquityConfig, "per equity config");
    final int nPairs = perEquityConfig.length;
    ArgumentChecker.isTrue(nPairs % 5 == 0, "Must have one curve config, discounting curve name and currency per equity");
    _priority = PriorityClass.valueOf(priority);
    _perEquityConfig = new HashMap<>();
    for (int i = 0; i < perEquityConfig.length; i += 5) {
      final String[] config = new String[] {perEquityConfig[i + 1], perEquityConfig[i + 2], perEquityConfig[i + 3], perEquityConfig[i + 4]};
      _perEquityConfig.put(perEquityConfig[i].toUpperCase(), config);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getValue() instanceof ExternalIdentifiable)) {
      return false;
    }
    ExternalId id = ((ExternalIdentifiable) target.getValue()).getExternalId();
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(id);
    if (ticker == null) {
      return false;
    }
    return _perEquityConfig.containsKey(ticker.toUpperCase());
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
    final String equityId = EquitySecurityUtils.getIndexOrEquityName(((ExternalIdentifiable) target.getValue()).getExternalId());
    if (!_perEquityConfig.containsKey(equityId)) {
      s_logger.error("Could not get config for equity " + equityId + "; should never happen");
      return null;
    }
    final String[] config = _perEquityConfig.get(equityId);
    if (ValuePropertyNames.CURVE_CURRENCY.equals(propertyName)) {
      return Collections.singleton(config[0]);
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(config[1]);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(config[2]);
    }

    if (ValuePropertyNames.DIVIDEND_TYPE.equals(propertyName)) {
      return Collections.singleton(config[3]);
    }
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
