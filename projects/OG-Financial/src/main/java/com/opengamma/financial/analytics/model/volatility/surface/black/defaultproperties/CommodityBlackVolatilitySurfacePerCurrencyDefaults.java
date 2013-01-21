/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CommodityBlackVolatilitySurfacePerCurrencyDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(CommodityBlackVolatilitySurfacePerCurrencyDefaults.class);
  /** The value requirements for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
    ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
  };
  /** Currencies to discounting curve names */
  private final Map<String, String> _currencyToCurveName;
  /** Currencies to discounting curve calculation configuration names */
  private final Map<String, String> _currencyToCurveCalculationMethodName;
  /** Currencies to surface names */
  private final Map<String, String> _currencyToSurfaceName;
  /** The priority of these defaults */

  /**
   * @param priority The priority of the defaults, not null
   * @param defaultsPerCurrency The defaults, not null
   */
  public CommodityBlackVolatilitySurfacePerCurrencyDefaults(final String priority, final String... defaultsPerCurrency) {
    super(ComputationTargetType.CURRENCY, true);
    ArgumentChecker.notNull(defaultsPerCurrency, "defaults per currency");
    final int n = defaultsPerCurrency.length;
    ArgumentChecker.isTrue(n % 4 == 0, "Need one forward curve name, forward curve calculation method and surface name per currency");
    _currencyToCurveName = new LinkedHashMap<>();
    _currencyToCurveCalculationMethodName = new LinkedHashMap<>();
    _currencyToSurfaceName = new LinkedHashMap<>();
    for (int i = 0; i < n; i += 4) {
      final String currencyPair = defaultsPerCurrency[i];
      _currencyToCurveName.put(currencyPair, defaultsPerCurrency[i + 1]);
      _currencyToCurveCalculationMethodName.put(currencyPair, defaultsPerCurrency[i + 2]);
      _currencyToSurfaceName.put(currencyPair, defaultsPerCurrency[i + 3]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final UniqueId currencyId = target.getUniqueId();
    return _currencyToCurveName.containsKey(currencyId.getValue());
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
    final String currency = target.getUniqueId().getValue();
    final String curveName = _currencyToCurveName.get(currency);
    if (curveName == null) {
      s_logger.error("Could not get curve name for {}; should never happen", target.getValue());
      return null;
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(curveName);
    }
    if (ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_currencyToCurveCalculationMethodName.get(currency));
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_currencyToSurfaceName.get(currency));
    }
    s_logger.error("Could not find default value for {} in this function", propertyName);
    return null;
  }

  /**
   * Gets all currencies for which defaults are available.
   * @return The currencies
   */
  protected Collection<String> getAllCurrencies() {
    return _currencyToCurveName.keySet();
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.COMMODITY_FUTURE_OPTION_BLACK_VOLATILITY_SURFACE_DEFAULTS;
  }

}
