/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.CurrencyPairsFunction;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base class for FX forward functions
 * @deprecated Use {@link DiscountingFunction}
 */
@Deprecated
public abstract class FXForwardFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardFunction.class);
  /**
   * @deprecated Deprecated value property name - has been moved to {@link ValuePropertyNames#PAY_CURVE_CALCULATION_CONFIG}
   */
  @Deprecated
  public static final String PAY_CURVE_CALC_CONFIG = ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG;
  /**
   * @deprecated Deprecated value property name - has been moved to {@link ValuePropertyNames#RECEIVE_CURVE_CALCULATION_CONFIG}
   */
  @Deprecated
  public static final String RECEIVE_CURVE_CALC_CONFIG = ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG;
  /** The computation target types for this function */
  private static final ComputationTargetType TYPE = FinancialSecurityTypes.FX_FORWARD_SECURITY.or(FinancialSecurityTypes.NON_DELIVERABLE_FX_FORWARD_SECURITY);

  /** The value requirement produced by this function */
  private final String _valueRequirementName;

  /**
   * @param valueRequirementName The value requirement name, not null
   */
  public FXForwardFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    if (now.isAfter(security.accept(ForexVisitors.getExpiryVisitor()))) {
      throw new OpenGammaRuntimeException("FX forward " + payCurrency.getCode() + "/" + receiveCurrency + " has expired");
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveConfig = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    final String receiveCurveConfig = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    final String fullPayCurveName = payCurveName + "_" + payCurrency.getCode();
    final String fullReceiveCurveName = receiveCurveName + "_" + receiveCurrency.getCode();
    final YieldAndDiscountCurve payCurve = getPayCurve(inputs, payCurrency, payCurveName, payCurveConfig);
    final YieldAndDiscountCurve receiveCurve = getReceiveCurve(inputs, receiveCurrency, receiveCurveName, receiveCurveConfig);
    final Map<String, Currency> curveCurrency = new HashMap<>();
    curveCurrency.put(fullPayCurveName, payCurrency);
    curveCurrency.put(fullReceiveCurveName, receiveCurrency);
    final Object baseQuotePairsObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
    if (baseQuotePairsObject == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair data");
    }
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final YieldAndDiscountCurve[] curves;
    final String[] allCurveNames;
    curves = new YieldAndDiscountCurve[] {payCurve, receiveCurve };
    allCurveNames = new String[] {fullPayCurveName, fullReceiveCurveName };
    // Implementation note: The ForexSecurityConverter create the Forex with currency order pay/receive. The curve are passed in the same order.
    final ForexSecurityConverter converter = new ForexSecurityConverter(baseQuotePairs);
    final InstrumentDefinition<?> definition = security.accept(converter);
    final Forex forex = (Forex) definition.toDerivative(now);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final ValueProperties.Builder properties = getResultProperties(target, desiredValue);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties.get());
    return getResult(forex, yieldCurves, target, desiredValues, inputs, spec, executionContext);
  }

  //TODO clumsy. Push the execute() method down into the functions and have getForward() and getData() methods
  /**
   * Performs the calculation.
   *
   * @param fxForward The FX forward
   * @param data The yield curve data
   * @param target The computation target
   * @param desiredValues The desired values
   * @param inputs The function inputs
   * @param spec The specification of the result
   * @param executionContext The execution context
   * @return A set of computed values
   */
  protected abstract Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext);

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target).get();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveConfigNames = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    if (payCurveConfigNames == null || payCurveConfigNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveConfigNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    if (receiveCurveConfigNames == null || receiveCurveConfigNames.size() != 1) {
      return null;
    }
    final String payCurveName = payCurveNames.iterator().next();
    final String receiveCurveName = receiveCurveNames.iterator().next();
    final String payCurveCalculationConfig = payCurveConfigNames.iterator().next();
    final String receiveCurveCalculationConfig = receiveCurveConfigNames.iterator().next();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final ValueRequirement payFundingCurve = YieldCurveFunctionUtils.getCurveRequirementForFXForward(ComputationTargetSpecification.of(payCurrency), payCurveName, payCurveCalculationConfig, true);
    final ValueRequirement receiveFundingCurve = YieldCurveFunctionUtils.getCurveRequirementForFXForward(ComputationTargetSpecification.of(receiveCurrency),
        receiveCurveName, receiveCurveCalculationConfig, false);
    final ValueRequirement pairQuoteRequirement = new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetSpecification.NULL);
    return Sets.newHashSet(pairQuoteRequirement, payFundingCurve, receiveFundingCurve);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String currencyPairConfigName = null;
    String payCurveName = null;
    String payCurveCalculationConfig = null;
    String receiveCurveName = null;
    String receiveCurveCalculationConfig = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification specification = entry.getKey();
      final ValueRequirement requirement = entry.getValue();
      if (specification.getValueName().equals(ValueRequirementNames.CURRENCY_PAIRS)) {
        currencyPairConfigName = specification.getProperty(CurrencyPairsFunction.CURRENCY_PAIRS_NAME);
      } else if (requirement.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        final ValueProperties constraints = requirement.getConstraints();
        if (constraints.getProperties().contains(ValuePropertyNames.PAY_CURVE)) {
          payCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          payCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        } else if (constraints.getProperties().contains(ValuePropertyNames.RECEIVE_CURVE)) {
          receiveCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          receiveCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        }
      }
    }
    if (currencyPairConfigName == null || payCurveName == null || receiveCurveName == null) {
      return null;
    }
    final CurrencyPairs baseQuotePairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(currencyPairConfigName);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(payCurrency, receiveCurrency);
    if (baseQuotePair == null) {
      s_logger.error("Could not get base/quote pair for currency pair (" + payCurrency + ", " + receiveCurrency + ")");
      return null;
    }
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(target,
        payCurveName, receiveCurveName, payCurveCalculationConfig, receiveCurveCalculationConfig, baseQuotePair).get());
    return Collections.singleton(resultSpec);
  }

  /**
   * Gets the general result properties.
   *
   * @param target The target
   * @return The result properties
   */
  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target);

  /**
   * Gets the result properties with curve information set.
   *
   * @param target The target
   * @param payCurve The name of the pay curve
   * @param payCurveCalculationConfig The name of the pay curve calculation configuration
   * @param receiveCurve The name of the receive curve
   * @param receiveCurveCalculationConfig The name of the receive curve calculation configuration
   * @param baseQuotePair The base / counter information for the currency pair
   * @return The result properties
   */
  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurve, final String payCurveCalculationConfig,
      final String receiveCurve, final String receiveCurveCalculationConfig, final CurrencyPair baseQuotePair);

  /**
   * Gets the result properties.
   *
   * @param target The target
   * @param desiredValue The desired value
   * @return The result properties
   */
  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue);

  /**
   * Gets the value requirement name.
   *
   * @return The value requirement name
   */
  protected String getValueRequirementName() {
    return _valueRequirementName;
  }

  /**
   * Gets the pay curve.
   *
   * @param inputs The function inputs
   * @param currency The pay currency
   * @param curveName The pay curve name
   * @param curveCalculationConfig The pay curve calculation configuration name
   * @return The pay curve
   */
  protected static YieldAndDiscountCurve getPayCurve(final FunctionInputs inputs, final Currency currency, final String curveName, final String curveCalculationConfig) {
    final Object curveObject = inputs.getValue(YieldCurveFunctionUtils.getCurveRequirementForFXForward(ComputationTargetSpecification.of(currency), curveName, curveCalculationConfig, true));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }

  /**
   * Gets the receive curve.
   *
   * @param inputs The function inputs
   * @param currency The receive currency
   * @param curveName The receive curve name
   * @param curveCalculationConfig The receive curve calculation configuration name
   * @return The receive curve
   */
  protected static YieldAndDiscountCurve getReceiveCurve(final FunctionInputs inputs, final Currency currency, final String curveName, final String curveCalculationConfig) {
    final Object curveObject = inputs.getValue(YieldCurveFunctionUtils.getCurveRequirementForFXForward(ComputationTargetSpecification.of(currency),
        curveName, curveCalculationConfig, false));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }
}
