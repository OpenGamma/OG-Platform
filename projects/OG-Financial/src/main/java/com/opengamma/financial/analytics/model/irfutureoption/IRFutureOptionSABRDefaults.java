/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Supplies default property values for interest rate future option calculations using the SABR model.
 */
public class IRFutureOptionSABRDefaults extends DefaultPropertyFunction {
  /** A logger */
  private static final Logger s_logger = LoggerFactory.getLogger(IRFutureOptionSABRDefaults.class);
  /** The value requirement names for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_BETA_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES
  };
  /** The curve calculation configuration names to be used for each currency */
  private final Map<String, String> _curveConfigPerCurrency;
  /** The surface names to be used for each currency */
  private final Map<String, String> _surfacePerCurrency;
  /** The SABR fitting methods to be used for each currency */
  private final Map<String, String> _fittingMethodPerCurrency;

  /**
   * @param defaultsPerCurrency The default values per currency, not null. Must contain elements in the order: currency, curve calculation configuration name, surface name
   * and fitting method name.
   */
  public IRFutureOptionSABRDefaults(final String... defaultsPerCurrency) {
    super(ComputationTargetType.TRADE, true);
    ArgumentChecker.notNull(defaultsPerCurrency, "defaults per currency");
    ArgumentChecker.isTrue(defaultsPerCurrency.length % 4 == 0, "Must have one curve configuration name, surface name and fitting method name per currency");
    _curveConfigPerCurrency = Maps.newLinkedHashMap();
    _surfacePerCurrency = Maps.newLinkedHashMap();
    _fittingMethodPerCurrency = Maps.newLinkedHashMap();
    for (int i = 0; i < defaultsPerCurrency.length; i += 4) {
      final String currency = defaultsPerCurrency[i];
      _curveConfigPerCurrency.put(currency, defaultsPerCurrency[i + 1]);
      _surfacePerCurrency.put(currency, defaultsPerCurrency[i + 2]);
      _fittingMethodPerCurrency.put(currency, defaultsPerCurrency[i + 3]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    if (!(security instanceof IRFutureOptionSecurity)) {
      return false;
    }
    final IRFutureOptionSecurity irFutureOption = (IRFutureOptionSecurity) security;
    final String currency = FinancialSecurityUtils.getCurrency(irFutureOption).getCode();
    if (_curveConfigPerCurrency.keySet().contains(currency)) {
      return true;
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final IRFutureOptionSecurity irFutureOption = (IRFutureOptionSecurity) target.getTrade().getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(irFutureOption).getCode();
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_curveConfigPerCurrency.get(currency));
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfacePerCurrency.get(currency));
    }
    if (SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD.equals(propertyName)) {
      return Collections.singleton(_fittingMethodPerCurrency.get(currency));
    }
    s_logger.error("Could not get default value for {}", propertyName);
    return null;
  }

}
