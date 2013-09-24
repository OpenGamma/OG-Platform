/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXForwardYieldCurveNodePnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDBCurveCalculationConfigSource.reinitOnChanges(context, this);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_PNL_SERIES, targetSpec, ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveCalculationConfigNames = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    if (payCurveCalculationConfigNames == null || payCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveCalculationConfigNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    if (receiveCurveCalculationConfigNames == null || receiveCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> curveCurrencies = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
    if (curveCurrencies == null || curveCurrencies.size() != 1) {
      return null;
    }
    final String payCurveCalculationConfigName = Iterables.getOnlyElement(payCurveCalculationConfigNames);
    final String receiveCurveCalculationConfigName = Iterables.getOnlyElement(receiveCurveCalculationConfigNames);
    final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
    final String payCurveName = Iterables.getOnlyElement(payCurveNames);
    final String receiveCurveName = Iterables.getOnlyElement(receiveCurveNames);
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final Currency curveCurrency = Currency.parse(Iterables.getOnlyElement(curveCurrencies));
    final String curveName;
    final String curveCalculationConfigName;
    if (curveCurrency.equals(payCurrency)) {
      curveName = payCurveName;
      curveCalculationConfigName = payCurveCalculationConfigName;
    } else if (curveCurrency.equals(receiveCurrency)) {
      curveName = receiveCurveName;
      curveCalculationConfigName = receiveCurveCalculationConfigName;
    } else {
      return null;
    }
    final Set<String> curveCalculationMethods = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String curveCalculationMethod;
    if (curveCalculationMethods == null) {
      final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
      final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
      final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
      curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    } else {
      curveCalculationMethod = Iterables.getOnlyElement(curveCalculationMethods);
    }
    final Set<String> calculationMethods = constraints.getValues(ValuePropertyNames.CALCULATION_METHOD);
    final ValueRequirement ycnsRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
        curveCurrency.getCode(), curveName, curveCalculationMethods, calculationMethods, security);
    final ValueProperties returnSeriesBaseConstraints = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.RECEIVE_CURVE)
        .withoutAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
        .withoutAny(ValuePropertyNames.PAY_CURVE)
        .withoutAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .withoutAny(ValuePropertyNames.CURVE_CURRENCY)
        .withoutAny(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS)
        .withoutAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withoutAny(ValuePropertyNames.CALCULATION_METHOD).get();
    final ValueRequirement returnSeriesRequirement;
    if (curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
      final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
      final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
      final LinkedHashMap<String, String[]> exogenousConfigData = curveCalculationConfig.getExogenousConfigData();
      if (exogenousConfigData != null) {
        final String underlyingCurveConfigName = Iterables.getOnlyElement(exogenousConfigData.entrySet()).getKey();
        final MultiCurveCalculationConfig underlyingCurveConfig = curveCalculationConfigSource.getConfig(underlyingCurveConfigName);
        final Currency baseCurrency = Currency.of(underlyingCurveConfig.getTarget().getUniqueId().getValue());
        returnSeriesRequirement = getReturnSeriesRequirement(curveName, baseCurrency, curveCurrency, curveCalculationConfigName,
            returnSeriesBaseConstraints);
      } else {
        returnSeriesRequirement = getReturnSeriesRequirement(curveName, curveCurrency, curveCalculationConfigName, returnSeriesBaseConstraints);
      }
    } else {
      returnSeriesRequirement = getReturnSeriesRequirement(curveName, curveCurrency, curveCalculationConfigName, returnSeriesBaseConstraints);
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(ycnsRequirement);
    requirements.add(returnSeriesRequirement);
    return requirements;
  }

  private static ValueRequirement getYCNSRequirement(final String payCurveName, final String payCurveCalculationConfigName, final String receiveCurveName,
      final String receiveCurveCalculationConfigName, final String currencyName, final String curveName, final Set<String> curveCalculationMethods,
      final Set<String> calculationMethods, final Security security) {
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfigName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfigName)
        .with(ValuePropertyNames.CURRENCY, currencyName)
        .with(ValuePropertyNames.CURVE_CURRENCY, currencyName)
        .with(ValuePropertyNames.CURVE, curveName);
    if (curveCalculationMethods != null) {
      properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethods);
    }
    if (calculationMethods != null) {
      properties.with(ValuePropertyNames.CALCULATION_METHOD, calculationMethods);
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, ComputationTargetType.SECURITY, security.getUniqueId(), properties.get());
  }

  private static ValueRequirement getReturnSeriesRequirement(final String curveName, final Currency curveCurrency, final String curveCalculationConfigName, final ValueProperties baseConstraints) {
    final ComputationTargetSpecification targetSpec = ComputationTargetType.CURRENCY.specification(curveCurrency);
    final ValueProperties constraints = baseConstraints.copy()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES, targetSpec, constraints);
  }

  private static ValueRequirement getReturnSeriesRequirement(final String curveName, final Currency payCurrency, final Currency receiveCurrency, final String curveCalculationConfig,
      final ValueProperties baseConstraints) {
    final ComputationTargetSpecification targetSpec = ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(UnorderedCurrencyPair.of(payCurrency, receiveCurrency));
    final ValueProperties constraints = baseConstraints.copy()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .withoutAny(ValuePropertyNames.CURRENCY)
        .get();
    return new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES, targetSpec, constraints);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Builder builder = createValueProperties();
    for (final ValueSpecification inputSpec : inputs.keySet()) {
      for (final String propertyName : inputSpec.getProperties().getProperties()) {
        if (ValuePropertyNames.FUNCTION.equals(propertyName)) {
          continue;
        }
        final Set<String> values = inputSpec.getProperties().getValues(propertyName);
        if (values == null || values.isEmpty()) {
          builder.withAny(propertyName);
        } else {
          builder.with(propertyName, values);
        }
      }
    }
    builder.with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    final ValueProperties properties = builder.get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_PNL_SERIES, targetSpec, properties));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D ycReturnSeries = (TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) inputs.getValue(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES);
    final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D fcReturnSeries = (TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES);
    final DoubleLabelledMatrix1D sensitivities = (DoubleLabelledMatrix1D) inputs.getValue(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);

    final ValueProperties resultProperties = desiredValues.iterator().next().getConstraints();
    TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeries;
    if (ycReturnSeries != null) {
      returnSeries = ycReturnSeries;
    } else if (fcReturnSeries != null) {
      returnSeries = fcReturnSeries;
    } else {
      throw new OpenGammaRuntimeException("Could not get return series for either yield curve or FX forward curve");
    }
    if (returnSeries.size() != sensitivities.size()) {
      throw new OpenGammaRuntimeException("Yield Curve Node Sensitivites vector of size " + sensitivities.size() + " but return series vector of size " + returnSeries.size());
    }

    final int size = returnSeries.size();
    final LocalDateDoubleTimeSeries[] nodesPnlSeries = new LocalDateDoubleTimeSeries[size];
    for (int i = 0; i < size; i++) {
      final LocalDateDoubleTimeSeries nodePnlSeries = returnSeries.getValues()[i].multiply(sensitivities.getValues()[i]);
      nodesPnlSeries[i] = nodePnlSeries;
    }
    final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D pnlSeriesVector = new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(returnSeries.getKeys(), returnSeries.getLabels(), nodesPnlSeries);

    return ImmutableSet.of(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_PNL_SERIES, target.toSpecification(), resultProperties), pnlSeriesVector));
  }

}
