/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 * Default properties for cross-currency swaps.
 */
@Deprecated
public class CrossCurrencySwapDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(CrossCurrencySwapDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.PRESENT_VALUE
  };
  private final Map<String, String> _currencyAndCurveConfigNames; // Ccy - config

  public CrossCurrencySwapDefaults(final String... currencyAndCurveConfigNames) {
    super(FinancialSecurityTypes.SWAP_SECURITY, true);
    ArgumentChecker.notNull(currencyAndCurveConfigNames, "currency and curve config names");
    final int nPairs = currencyAndCurveConfigNames.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have one curve config name per currency");
    _currencyAndCurveConfigNames = new HashMap<>();
    for (int i = 0; i < currencyAndCurveConfigNames.length; i += 2) {
      _currencyAndCurveConfigNames.put(currencyAndCurveConfigNames[i], currencyAndCurveConfigNames[i + 1]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity xccySecurity = (FinancialSecurity) target.getSecurity();
    try {
      final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(xccySecurity);
      if (type == InterestRateInstrumentType.SWAP_CROSS_CURRENCY) {
        final String payCurrency = xccySecurity.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
        final String receiveCurrency = xccySecurity.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
        // Check Ibor indexes?
        return _currencyAndCurveConfigNames.containsKey(payCurrency) && _currencyAndCurveConfigNames.containsKey(receiveCurrency);
      }
    } catch (final OpenGammaRuntimeException e) {
      return false;
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
    final String receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
    if (!_currencyAndCurveConfigNames.containsKey(payCurrency)) {
      s_logger.error("Could not get config for pay currency " + payCurrency + "; should never happen");
      return null;
    }
    if (!_currencyAndCurveConfigNames.containsKey(receiveCurrency)) {
      s_logger.error("Could not get config for receive currency " + receiveCurrency + "; should never happen");
      return null;
    }
    final String payConfig = _currencyAndCurveConfigNames.get(payCurrency);
    final String recConfig = _currencyAndCurveConfigNames.get(receiveCurrency);
    if (ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(payConfig);
    }
    if (ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(recConfig);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.XCCY_SWAP_DEFAULTS;
  }

}
