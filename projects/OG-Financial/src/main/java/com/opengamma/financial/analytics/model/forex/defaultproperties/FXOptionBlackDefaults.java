/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.defaultproperties;

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
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Default properties for FX options priced using the Black functions.
 * @deprecated Use the versions for curve and surfaces
 */
@Deprecated
public class FXOptionBlackDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionBlackDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_GAMMA_P,
    ValueRequirementNames.VEGA_MATRIX,
    ValueRequirementNames.VEGA_QUOTE_MATRIX,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY,
    ValueRequirementNames.VALUE_THETA,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_RHO,
    ValueRequirementNames.VALUE_PHI,
    ValueRequirementNames.VALUE_VOMMA,
    ValueRequirementNames.VALUE_VANNA,
    ValueRequirementNames.THETA
  };
  private final PriorityClass _priority;
  private final String _interpolatorName;
  private final String _leftExtrapolatorName;
  private final String _rightExtrapolatorName;
  private final Map<String, Pair<String, String>> _propertyValuesByCurrency;
  private final Map<Pair<String, String>, String> _surfaceNameByCurrencyPair;

  /**
   * @param priority The priority of the functions
   * @param interpolatorName The volatility surface interpolator name
   * @param leftExtrapolatorName The volatility surface left extrapolator name
   * @param rightExtrapolatorName The volatility surface right extrapolator name
   * @param propertyValuesByCurrencies Values for the properties per currency: an array of strings where the <i>i<sup>th</sup></i> currency has properties:
   * <ul>
   * <li><i>i</i> = first currency name,
   * <li><i>i + 1</i> = first currency curve configuration name
   * <li><i>i + 2</i> = first currency discounting curve name
   * <li><i>i + 3</i> = second currency name,
   * <li><i>i + 4</i> = second currency curve configuration name
   * <li><i>i + 5</i> = second currency discounting curve name
   * <li><i>i + 6</i> = surface name
   * </ul>
   */
  public FXOptionBlackDefaults(final String priority, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName,
      final String... propertyValuesByCurrencies) {
    super(FinancialSecurityTypes.FX_OPTION_SECURITY.or(FinancialSecurityTypes.FX_BARRIER_OPTION_SECURITY).or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY)
        .or(FinancialSecurityTypes.NON_DELIVERABLE_FX_OPTION_SECURITY).or(FinancialSecurityTypes.NON_DELIVERABLE_FX_DIGITAL_OPTION_SECURITY), true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(interpolatorName, "interpolator name");
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    ArgumentChecker.notNull(propertyValuesByCurrencies, "property values by currency");
    ArgumentChecker.isTrue(propertyValuesByCurrencies.length % 7 == 0, "Must have two currencies, one curve config and discounting curve name per currency pair and one surface name");
    _priority = PriorityClass.valueOf(priority);
    _interpolatorName = interpolatorName;
    _leftExtrapolatorName = leftExtrapolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
    _propertyValuesByCurrency = new HashMap<>();
    _surfaceNameByCurrencyPair = new HashMap<>();
    for (int i = 0; i < propertyValuesByCurrencies.length; i += 7) {
      final String firstCurrency = propertyValuesByCurrencies[i];
      final Pair<String, String> firstCurrencyValues = Pairs.of(propertyValuesByCurrencies[i + 1], propertyValuesByCurrencies[i + 2]);
      final String secondCurrency = propertyValuesByCurrencies[i + 3];
      ArgumentChecker.isFalse(firstCurrency.equals(secondCurrency), "The two currencies must not be equal; have {} and {}", firstCurrency, secondCurrency);
      final Pair<String, String> secondCurrencyValues = Pairs.of(propertyValuesByCurrencies[i + 4], propertyValuesByCurrencies[i + 5]);
      final String surfaceName = propertyValuesByCurrencies[i + 6];
      _propertyValuesByCurrency.put(firstCurrency, firstCurrencyValues);
      _propertyValuesByCurrency.put(secondCurrency, secondCurrencyValues);
      _surfaceNameByCurrencyPair.put(Pairs.of(firstCurrency, secondCurrency), surfaceName);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    return (_propertyValuesByCurrency.containsKey(putCurrency) && _propertyValuesByCurrency.containsKey(callCurrency));
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunction.PUT_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunction.CALL_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
      defaults.addValuePropertyName(valueRequirement, FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor()).getCode();
    final String callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor()).getCode();
    if (!_propertyValuesByCurrency.containsKey(putCurrency)) {
      s_logger.error("Could not get config for put currency " + putCurrency + "; should never happen");
      return null;
    }
    if (!_propertyValuesByCurrency.containsKey(callCurrency)) {
      s_logger.error("Could not get config for call currency " + callCurrency + "; should never happen");
      return null;
    }
    final String putCurveConfig, callCurveConfig, putCurve, callCurve;
    final Pair<String, String> firstCurrencyValues = _propertyValuesByCurrency.get(putCurrency);
    putCurveConfig = firstCurrencyValues.getFirst();
    putCurve = firstCurrencyValues.getSecond();
    final Pair<String, String> secondCurrencyValues = _propertyValuesByCurrency.get(callCurrency);
    callCurveConfig = secondCurrencyValues.getFirst();
    callCurve = secondCurrencyValues.getSecond();
    if (FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(putCurveConfig);
    }
    if (FXOptionBlackFunction.PUT_CURVE.equals(propertyName)) {
      return Collections.singleton(putCurve);
    }
    if (FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(callCurveConfig);
    }
    if (FXOptionBlackFunction.CALL_CURVE.equals(propertyName)) {
      return Collections.singleton(callCurve);
    }
    if (InterpolatedDataProperties.X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_interpolatorName);
    }
    if (InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftExtrapolatorName);
    }
    if (InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightExtrapolatorName);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      Pair<String, String> pair = Pairs.of(putCurrency, callCurrency);
      if (_surfaceNameByCurrencyPair.containsKey(pair)) {
        return Collections.singleton(_surfaceNameByCurrencyPair.get(pair));
      }
      pair = Pairs.of(callCurrency, putCurrency);
      if (_surfaceNameByCurrencyPair.containsKey(pair)) {
        return Collections.singleton(_surfaceNameByCurrencyPair.get(pair));
      }
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.FX_OPTION_BLACK_DEFAULTS;
  }
}
