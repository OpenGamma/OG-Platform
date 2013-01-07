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
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults.
 */
public class EquityOptionPerCurrencyDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionPerCurrencyDefaults.class);
  /** Map of currency name to discounting curve name */
  private final Map<String, String> _currencyToCurveName;
  /** Map of currency name to discounting curve calculation configuration name */
  private final Map<String, String> _currencyToCurveCalculationConfig;
  /** Map of currency name to volatility surface name */
  private final Map<String, String> _currencyToSurfaceName;
  /** Map of currency name to Black volatility surface interpolation method name */
  private final Map<String, String> _currencyToInterpolationMethodName;
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
    ValueRequirementNames.VALUE_RHO
  };

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other, not null
   * @param perCurrencyConfig Defaults values of curve configuration, discounting curve, surface name and interpolation method per currency, not null
   */
  public EquityOptionPerCurrencyDefaults(final String priority, final String... perCurrencyConfig) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perCurrencyConfig, "per currency configuration");
    _priority = PriorityClass.valueOf(priority);

    final int nPairs = perCurrencyConfig.length;
    ArgumentChecker.isTrue(nPairs % 5 == 0, "Must have one curve config, discounting name, surface name and interpolation method name per currency");
    _currencyToCurveName = Maps.newHashMap();
    _currencyToCurveCalculationConfig = Maps.newHashMap();
    _currencyToSurfaceName = Maps.newHashMap();
    _currencyToInterpolationMethodName = Maps.newHashMap();
    for (int i = 0; i < perCurrencyConfig.length; i += 5) {
      final String currency = perCurrencyConfig[i].toUpperCase();
      _currencyToCurveCalculationConfig.put(currency, perCurrencyConfig[i + 1]);
      _currencyToCurveName.put(currency, perCurrencyConfig[i + 2]);
      _currencyToSurfaceName.put(currency, perCurrencyConfig[i + 3]);
      _currencyToInterpolationMethodName.put(currency, perCurrencyConfig[i + 4]);
    }
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueName, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (!_currencyToCurveCalculationConfig.containsKey(currency)) {
      s_logger.error("Could not find defaults for {}", currency);
      return null;
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_currencyToCurveName.get(currency));
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_currencyToCurveCalculationConfig.get(currency));
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_currencyToSurfaceName.get(currency));
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_currencyToInterpolationMethodName.get(currency));
    }
    s_logger.error("Cannot get a default value for {}", propertyName);
    return null;
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
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    return _currencyToCurveName.containsKey(currency);
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_OPTION_DEFAULTS;
  }
}
