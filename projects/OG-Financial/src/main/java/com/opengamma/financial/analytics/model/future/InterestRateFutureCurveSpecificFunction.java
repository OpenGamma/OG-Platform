/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.InterestRateFutureTradeConverterDeprecated;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base function for calculating curve-specific risk factors for interest-rate futures.
 * 
 * @deprecated Use descendants of {@link MultiCurvePricingFunction}
 */
@Deprecated
public abstract class InterestRateFutureCurveSpecificFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureCurveSpecificFunction.class);
  private final String _valueRequirement;
  private InterestRateFutureTradeConverterDeprecated _converter;
  private FixedIncomeConverterDataProvider _dataConverter;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  public InterestRateFutureCurveSpecificFunction(final String valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "value requirement");
    _valueRequirement = valueRequirement;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _converter = new InterestRateFutureTradeConverterDeprecated(new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource));
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Trade trade = target.getTrade();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String currency = FinancialSecurityUtils.getCurrency(trade.getSecurity()).getCode();
    final String fullCurveName = curveName + "_" + currency;
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String[] fullCurveNames = new String[curveNames.length];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency;
    }
    final YieldCurveBundle data = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, _curveCalculationConfigSource);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(target, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final InstrumentDefinition<InstrumentDerivative> irFutureDefinition = _converter.convert(trade);
    final InstrumentDerivative irFuture = _dataConverter.convert(trade.getSecurity(), irFutureDefinition, now, fullCurveNames, timeSeries);
    final ValueSpecification spec = new ValueSpecification(_valueRequirement, target.toSpecification(), createValueProperties(target, curveName, curveCalculationConfigName));
    return getResults(irFuture, fullCurveName, curveSpec, data, spec, trade.getSecurity());
  }

  protected abstract Set<ComputedValue> getResults(final InstrumentDerivative irFuture, final String curveName, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldCurveBundle curves, final ValueSpecification resultSpec, final Security security);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof InterestRateFutureSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties(target);
    return Collections.singleton(new ValueSpecification(_valueRequirement, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    Set<String> requestedCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (requestedCurveNames == null || requestedCurveNames.size() != 1) {
      s_logger.error("Must specify a curve against which to calculate the desired value " + _valueRequirement);
      return null;
    }
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
    }
    final String[] availableCurveNames = curveCalculationConfig.getYieldCurveNames();
    if (requestedCurveNames.isEmpty()) {
      requestedCurveNames = Sets.newHashSet(availableCurveNames);
    } else {
      final Set<String> intersection = YieldCurveFunctionUtils.intersection(requestedCurveNames, availableCurveNames);
      if (intersection.isEmpty()) {
        s_logger.debug("None of the requested curves {} are available in curve calculation configuration called {}", requestedCurveNames, curveCalculationConfigName);
        return null;
      }
      requestedCurveNames = intersection;
    }
    final String[] applicableCurveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, availableCurveNames);
    final Set<String> curveNames = YieldCurveFunctionUtils.intersection(requestedCurveNames, applicableCurveNames);
    if (curveNames.isEmpty()) {
      s_logger.debug("{} {} security is not sensitive to the curves {}", new Object[] {currency, security.getClass(), curveNames });
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource));
    requirements.add(getCurveSpecRequirement(target, Iterables.getOnlyElement(requestedCurveNames)));
    final Set<ValueRequirement> tsRequirements = _dataConverter.getConversionTimeSeriesRequirements(target.getTrade().getSecurity(), _converter.convert(target.getTrade()));
    if (tsRequirements == null) {
      return null;
    }
    requirements.addAll(tsRequirements);
    return requirements;
  }

  private static ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties.get());
  }

  private ValueProperties createValueProperties(final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURRENCY, currency).with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .withAny(ValuePropertyNames.CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    return properties.get();
  }

  private ValueProperties createValueProperties(final ComputationTarget target, final String curveName, final String curveCalculationConfig) {
    final Security security = target.getTrade().getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURRENCY, currency).with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig);
    return properties.get();
  }
}
