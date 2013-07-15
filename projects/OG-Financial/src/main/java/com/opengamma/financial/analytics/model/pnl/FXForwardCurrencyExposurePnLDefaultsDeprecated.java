/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use the version that does not refer to pay or receive currency
 * @see FXForwardCurrencyExposurePnLFunction
 */
@Deprecated
public class FXForwardCurrencyExposurePnLDefaultsDeprecated extends DefaultPropertyFunction {
  private final String _payCurveName;
  private final String _receiveCurveName;
  private final String _payCurveConfig;
  private final String _receiveCurveConfig;
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingFunction;
  private final String _payCurrency;
  private final String _receiveCurrency;

  public FXForwardCurrencyExposurePnLDefaultsDeprecated(final String payCurveName, final String receiveCurveName, final String payCurveConfig, final String receiveCurveConfig,
      final String samplingPeriod, final String scheduleCalculator, final String samplingFunction, final String payCurrency, final String receiveCurrency) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(payCurveName, "pay curve name");
    ArgumentChecker.notNull(receiveCurveName, "receive curve name");
    ArgumentChecker.notNull(payCurveConfig, "pay curve config");
    ArgumentChecker.notNull(receiveCurveConfig, "receive curve config");
    ArgumentChecker.notNull(samplingPeriod, "sampling period");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator");
    ArgumentChecker.notNull(samplingFunction, "sampling function");
    ArgumentChecker.notNull(payCurrency, "pay currency");
    ArgumentChecker.notNull(receiveCurrency, "receive currency");
    _payCurveName = payCurveName;
    _receiveCurveName = receiveCurveName;
    _payCurveConfig = payCurveConfig;
    _receiveCurveConfig = receiveCurveConfig;
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
    _payCurrency = payCurrency;
    _receiveCurrency = receiveCurrency;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getPosition().getSecurity() instanceof FXForwardSecurity)) {
      return false;
    }
    final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    return payCurrency.getCode().equals(_payCurrency) && receiveCurrency.getCode().equals(_receiveCurrency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.PAY_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.RECEIVE_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_FUNCTION);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.PAY_CURVE.equals(propertyName)) {
      return Collections.singleton(_payCurveName);
    }
    if (ValuePropertyNames.RECEIVE_CURVE.equals(propertyName)) {
      return Collections.singleton(_receiveCurveName);
    }
    if (ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_payCurveConfig);
    }
    if (ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_receiveCurveConfig);
    }
    if (ValuePropertyNames.SAMPLING_PERIOD.equals(propertyName)) {
      return Collections.singleton(_samplingPeriod);
    }
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculator);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingFunction);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PNL_SERIES;
  }
}
