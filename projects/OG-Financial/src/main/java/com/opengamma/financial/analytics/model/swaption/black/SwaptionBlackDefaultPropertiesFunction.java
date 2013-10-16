/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class SwaptionBlackDefaultPropertiesFunction extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SwaptionBlackDefaultPropertiesFunction.class);
  /** The value requirement names */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY,
    ValueRequirementNames.DELTA,
    ValueRequirementNames.FORWARD_DELTA,
    ValueRequirementNames.GAMMA,
    ValueRequirementNames.FORWARD_GAMMA,
    ValueRequirementNames.THETA,
    ValueRequirementNames.DRIFTLESS_THETA,
    ValueRequirementNames.VEGA,
    ValueRequirementNames.FORWARD_VEGA,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_THETA,
    ValueRequirementNames.GAMMA_PV01,
    ValueRequirementNames.FORWARD
  };
  /** Map from currency to curve calculation configuration and surface names */
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndSurfaceNames;

  /**
   * A list of (currency, curve calculation configuration name, surface name) triples.
   * @param currencyCurveConfigAndSurfaceNames The names, not null
   */
  public SwaptionBlackDefaultPropertiesFunction(final String... currencyCurveConfigAndSurfaceNames) {
    super(FinancialSecurityTypes.SWAPTION_SECURITY, true);
    ArgumentChecker.notNull(currencyCurveConfigAndSurfaceNames, "currency, curve config and surface names");
    final int nPairs = currencyCurveConfigAndSurfaceNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and surface name per currency");
    _currencyCurveConfigAndSurfaceNames = new HashMap<>();
    for (int i = 0; i < currencyCurveConfigAndSurfaceNames.length; i += 3) {
      final Pair<String, String> pair = Pair.of(currencyCurveConfigAndSurfaceNames[i + 1], currencyCurveConfigAndSurfaceNames[i + 2]);
      _currencyCurveConfigAndSurfaceNames.put(currencyCurveConfigAndSurfaceNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final SwaptionSecurity swaption = (SwaptionSecurity) target.getSecurity();
    final String currencyName = FinancialSecurityUtils.getCurrency(swaption).getCode();
    return _currencyCurveConfigAndSurfaceNames.containsKey(currencyName);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currencyName = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (!_currencyCurveConfigAndSurfaceNames.containsKey(currencyName)) {
      s_logger.error("Could not config and surface names for currency " + currencyName + "; should never happen");
      return null;
    }
    final Pair<String, String> pair = _currencyCurveConfigAndSurfaceNames.get(currencyName);
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(pair.getFirst());
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(pair.getSecond());
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SWAPTION_BLACK_DEFAULTS;
  }
}
