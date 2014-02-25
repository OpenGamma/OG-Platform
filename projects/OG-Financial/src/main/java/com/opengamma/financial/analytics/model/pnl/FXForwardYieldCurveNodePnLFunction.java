/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_RETURN_SERIES;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Function that calculates the P&L for an FX forward due to movements in the yield curves used for pricing.
 */
public class FXForwardYieldCurveNodePnLFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardYieldCurveNodePnLFunction.class);
  /** Temporary property used to set the result currency */
  private static final String RESULT_CURRENCY = "ResultCurrency";

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDBCurveCalculationConfigSource.reinitOnChanges(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    return new Compiled(currencyPairs);
  }

  /**
   * Compiled function that calculates the P&L for an FX forward due to movements in the yield curves used for pricing.
   */
  protected class Compiled extends AbstractInvokingCompiledFunction {
    /** The currency pairs */
    private final CurrencyPairs _currencyPairs;

    /**
     * @param currencyPairs The currency pairs, not null
     */
    public Compiled(final CurrencyPairs currencyPairs) {
      ArgumentChecker.notNull(currencyPairs, "currency pairs");
      _currencyPairs = currencyPairs;
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
      final Set<String> resultCurrencies = constraints.getValues(CURRENCY);
      final String resultCurrency;
      final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
      final Currency tradeBaseCurrency = baseQuotePair.getBase();
      final Currency tradeNonBaseCurrency = baseQuotePair.getCounter();
      final Set<ValueRequirement> requirements = new HashSet<>();
      if (resultCurrencies != null && resultCurrencies.size() == 1) {
        final Currency ccy = Currency.of(Iterables.getOnlyElement(resultCurrencies));
        if (!(ccy.equals(payCurrency) || ccy.equals(receiveCurrency))) {
          requirements.add(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(UnorderedCurrencyPair.of(tradeBaseCurrency, ccy)));
          resultCurrency = ccy.getCode();
        } else if (ccy.equals(tradeNonBaseCurrency)) {
          requirements.add(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(UnorderedCurrencyPair.of(tradeNonBaseCurrency, tradeBaseCurrency)));
          resultCurrency = tradeNonBaseCurrency.getCode();
        } else {
          requirements.add(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(UnorderedCurrencyPair.of(tradeNonBaseCurrency, tradeBaseCurrency)));
          resultCurrency = tradeBaseCurrency.getCode();
        }
      } else {
        resultCurrency = tradeBaseCurrency.getCode();
      }
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
              returnSeriesBaseConstraints, resultCurrency);
        } else {
          returnSeriesRequirement = getReturnSeriesRequirement(curveName, curveCurrency, curveCalculationConfigName, returnSeriesBaseConstraints,
              resultCurrency);
        }
      } else {
        returnSeriesRequirement = getReturnSeriesRequirement(curveName, curveCurrency, curveCalculationConfigName, returnSeriesBaseConstraints,
            resultCurrency);
      }
      requirements.add(ycnsRequirement);
      requirements.add(returnSeriesRequirement);
      requirements.add(new ValueRequirement(ValueRequirementNames.SPOT_RATE, CurrencyPair.TYPE.specification(CurrencyPair.of(tradeBaseCurrency, tradeNonBaseCurrency))));
      return requirements;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      final Builder builder = createValueProperties();
      final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency());
      if (currencyPair == null) {
        return null;
      }
      final Currency currencyBase = currencyPair.getBase();
      String resultCurrency = null;
      for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
        final ValueSpecification inputSpec = entry.getKey();
        final ValueRequirement inputReq = entry.getValue();
        if (inputReq.getValueName().equals(YIELD_CURVE_RETURN_SERIES) || inputReq.getValueName().equals(FX_FORWARD_CURVE_RETURN_SERIES)) {
          final Set<String> resultCurrencies = inputReq.getConstraints().getValues(RESULT_CURRENCY);
          if (resultCurrencies != null && resultCurrencies.size() == 1) {
            resultCurrency = inputReq.getConstraint(RESULT_CURRENCY);
          } else {
            // should never reach here, but just in case
            resultCurrency = currencyBase.getCode();
          }
        }
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
      if (resultCurrency == null) {
        return null;
      }
      builder.with(ValuePropertyNames.CURRENCY, resultCurrency)
          .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
      final ValueProperties properties = builder.get();
      final ComputationTargetSpecification targetSpec = target.toSpecification();
      return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_PNL_SERIES, targetSpec, properties));
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> resultCurrencies = constraints.getValues(CURRENCY);
      final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D ycReturnSeries = (TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) inputs.getValue(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES);
      final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D fcReturnSeries = (TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES);
      final DoubleLabelledMatrix1D sensitivities = (DoubleLabelledMatrix1D) inputs.getValue(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
      final String curveCurrency = desiredValue.getConstraint(CURVE_CURRENCY);
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
        throw new OpenGammaRuntimeException("Yield Curve Node Sensitivities vector of size " + sensitivities.size() + " but return series vector of size " + returnSeries.size());
      }
      TenorLabelledLocalDateDoubleTimeSeriesMatrix1D pnlSeriesVector;
      if (resultCurrencies == null || resultCurrencies.size() != 1) {
        s_logger.warn("No Currency property - returns result in base currency");
        pnlSeriesVector = getPnLVector(returnSeries, sensitivities);
      } else {
        final String resultCurrency = Iterables.getOnlyElement(resultCurrencies);
        boolean resultEqualsCurveCurrency = resultCurrency.equals(curveCurrency);
        final double currentFxRate = (double) inputs.getValue(ValueRequirementNames.SPOT_RATE);
        pnlSeriesVector = getPnLVector(returnSeries, currentFxRate, sensitivities, resultEqualsCurveCurrency);
      }
      return ImmutableSet.of(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_PNL_SERIES, target.toSpecification(), resultProperties), pnlSeriesVector));
    }

    /**
     * Gets the yield curve node sensitivities requirement
     * @param payCurveName The pay curve name
     * @param payCurveCalculationConfigName The pay curve calculation configuration name
     * @param receiveCurveName The receive curve name
     * @param receiveCurveCalculationConfigName The receive curve calculation configuration name
     * @param currencyName The curve currency (i.e. the currency that the sensitivities were calculated in)
     * @param curveName The curve name
     * @param curveCalculationMethods The curve calculation methods
     * @param calculationMethods The sensitivities calculation methods
     * @param security The security
     * @return The requirement
     */
    private ValueRequirement getYCNSRequirement(final String payCurveName, final String payCurveCalculationConfigName, final String receiveCurveName,
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

    /**
     * Gets the requirement for the return series of a yield curve that is not FX-implied and adds an
     * optional property for the result currency.
     * @param curveName The curve name
     * @param curveCurrency The curve currency
     * @param curveCalculationConfigName The curve calculation configuration name
     * @param baseConstraints The base constraints of the results
     * @param resultCurrency The result currency
     * @return The requirement
     */
    private ValueRequirement getReturnSeriesRequirement(final String curveName, final Currency curveCurrency, final String curveCalculationConfigName, final ValueProperties baseConstraints,
        final String resultCurrency) {
      final ComputationTargetSpecification targetSpec = ComputationTargetType.CURRENCY.specification(curveCurrency);
      final ValueProperties constraints = baseConstraints.copy()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .withoutAny(CURRENCY)
          .with(CURRENCY, curveCurrency.getCode())
          .with(RESULT_CURRENCY, resultCurrency)
          .withOptional(RESULT_CURRENCY)
          .get();
      return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES, targetSpec, constraints);
    }

    /**
     * Gets the requirement for the return series of a yield curve that is FX-implied and adds an
     * optional property for the result currency.
     * @param curveName The curve name
     * @param payCurrency The pay currency
     * @param receiveCurrency The receive currency
     * @param curveCalculationConfigName The curve calculation configuration name
     * @param baseConstraints The base constraints of the results
     * @param resultCurrency The result currency
     * @return The requirement
     */
    private ValueRequirement getReturnSeriesRequirement(final String curveName, final Currency payCurrency, final Currency receiveCurrency, final String curveCalculationConfigName,
        final ValueProperties baseConstraints, final String resultCurrency) {
      final ComputationTargetSpecification targetSpec = ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(UnorderedCurrencyPair.of(payCurrency, receiveCurrency));
      final ValueProperties constraints = baseConstraints.copy()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .withoutAny(CURRENCY)
          .with(RESULT_CURRENCY, resultCurrency)
          .withOptional(RESULT_CURRENCY)
          .get();
      return new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES, targetSpec, constraints);
    }

    /**
     * Calculates the P&L vector without currency conversion.
     * @param returnSeries The return series for the nodes in a curve
     * @param sensitivities The sensitivities to the curve
     * @return The P&L vector for each curve node tenor
     */
    private TenorLabelledLocalDateDoubleTimeSeriesMatrix1D getPnLVector(final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeries, final DoubleLabelledMatrix1D sensitivities) {
      final int size = returnSeries.size();
      final LocalDateDoubleTimeSeries[] nodesPnlSeries = new LocalDateDoubleTimeSeries[size];
      for (int i = 0; i < size; i++) {
        final LocalDateDoubleTimeSeries nodePnlSeries = returnSeries.getValues()[i].multiply(sensitivities.getValues()[i]);
        nodesPnlSeries[i] = nodePnlSeries;
      }
      return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(returnSeries.getKeys(), returnSeries.getLabels(), nodesPnlSeries);
    }

//    /**
//     * Calculates the P&L vector with currency conversion.
//     * @param returnSeries The return series for the nodes in a curve
//     * @param conversionSeries The FX conversion series
//     * @param sensitivities The sensitivities to the curve
//     * @return The P&L vector for each curve node tenor converted into the desired currency
//     */
//    private TenorLabelledLocalDateDoubleTimeSeriesMatrix1D getPnLVector(final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeries, final LocalDateDoubleTimeSeries conversionSeries,
//        final DoubleLabelledMatrix1D sensitivities, final boolean resultCurveCurrency) {
//      final int size = returnSeries.size();
//      final LocalDateDoubleTimeSeries[] nodesPnlSeries = new LocalDateDoubleTimeSeries[size];
//      for (int i = 0; i < size; i++) {
//        if (resultCurveCurrency) {
//          final LocalDateDoubleTimeSeries nodePnlSeries = returnSeries.getValues()[i].multiply(sensitivities.getValues()[i]);
//          nodesPnlSeries[i] = nodePnlSeries;
//        }  else {
//          final LocalDateDoubleTimeSeries convertedSeries = conversionSeries.reciprocal().multiply(sensitivities.getValues()[i]);
//          final LocalDateDoubleTimeSeries nodePnlSeries = returnSeries.getValues()[i].multiply(convertedSeries);
//          nodesPnlSeries[i] = nodePnlSeries;
//        }
//      }
//      return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(returnSeries.getKeys(), returnSeries.getLabels(), nodesPnlSeries);
//    }

    /**
     * Calculates the P&L vector with currency conversion at the spot exchange rate.
     * @param returnSeries The return series for the nodes in a curve
     * @param conversionSeries The FX conversion series
     * @param sensitivities The sensitivities to the curve
     * @return The P&L vector for each curve node tenor converted into the desired currency
     */
    private TenorLabelledLocalDateDoubleTimeSeriesMatrix1D getPnLVector(final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeries, final double exchangeRate,
        final DoubleLabelledMatrix1D sensitivities, final boolean resultCurveCurrency) {
      final int size = returnSeries.size();
      final LocalDateDoubleTimeSeries[] nodesPnlSeries = new LocalDateDoubleTimeSeries[size];
      for (int i = 0; i < size; i++) {
        if (resultCurveCurrency) {
          final LocalDateDoubleTimeSeries nodePnlSeries = returnSeries.getValues()[i].multiply(sensitivities.getValues()[i]);
          nodesPnlSeries[i] = nodePnlSeries;
        }  else {
          final double convertedSensitivity = sensitivities.getValues()[i] / exchangeRate;
          final LocalDateDoubleTimeSeries nodePnlSeries = returnSeries.getValues()[i].multiply(convertedSensitivity);
          nodesPnlSeries[i] = nodePnlSeries;
        }
      }
      return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(returnSeries.getKeys(), returnSeries.getLabels(), nodesPnlSeries);
    }
    
  }
  
}
