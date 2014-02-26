/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Base class for cross-currency swap analytics
 * 
 * @deprecated Use functions descending from {@link DiscountingFunction}
 */
@Deprecated
public abstract class CrossCurrencySwapFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(CrossCurrencySwapFunction.class);
  /** The value requirements this function produces */
  private final String[] _valueRequirements;
  /** Converts securities to definitions */
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
  /** Converts definitions to derivatives */
  private FixedIncomeConverterDataProvider _definitionConverter;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  /**
   * @param valueRequirements The value requirements, not null
   */
  public CrossCurrencySwapFunction(final String... valueRequirements) {
    ArgumentChecker.notNull(valueRequirements, "value requirements");
    _valueRequirements = valueRequirements;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FRASecurityConverterDeprecated fraConverter = new FRASecurityConverterDeprecated(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverterDeprecated irFutureConverter = new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter)
        .interestRateFutureSecurityVisitor(irFutureConverter).bondSecurityVisitor(bondConverter).bondFutureSecurityVisitor(bondFutureConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    //TODO won't need to call into database again when calculation configurations are a requirement
    final String payCurveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig payCurveCalculationConfig = _curveCalculationConfigSource.getConfig(payCurveCalculationConfigName);
    if (payCurveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + payCurveCalculationConfigName);
    }
    final String receiveCurveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig receiveCurveCalculationConfig = _curveCalculationConfigSource.getConfig(receiveCurveCalculationConfigName);
    if (receiveCurveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + receiveCurveCalculationConfigName);
    }
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    //    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, _valueRequirements, timeSeries);
    final String[] payCurveNames = payCurveCalculationConfig.getYieldCurveNames();
    final String[] receiveCurveNames = receiveCurveCalculationConfig.getYieldCurveNames();
    final String payCurveSuffix = payCurveCalculationConfig.getTarget().getUniqueId().getValue();
    final String receiveCurveSuffix = receiveCurveCalculationConfig.getTarget().getUniqueId().getValue();
    final List<String> curveNames = new ArrayList<>();
    if (payCurveNames.length == 1) {
      final String curveName = payCurveNames[0] + "_" + payCurveSuffix; // <-
      curveNames.add(curveName);
      curveNames.add(curveName);
    } else {
      for (final String curveName : payCurveNames) {
        curveNames.add(curveName + "_" + payCurveSuffix);
      }
    }
    if (receiveCurveNames.length == 1) {
      final String curveName = receiveCurveNames[0] + "_" + receiveCurveSuffix; // <-
      curveNames.add(curveName);
      curveNames.add(curveName);
    } else {
      for (final String curveName : receiveCurveNames) {
        curveNames.add(curveName + "_" + receiveCurveSuffix);
      }
    }
    final String[] curveNamesArray = curveNames.toArray(new String[0]);
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, curveNamesArray, timeSeries);
    final YieldCurveBundle payCurveBundle = YieldCurveFunctionUtils.getAllYieldCurves(inputs, payCurveCalculationConfig, _curveCalculationConfigSource);
    final YieldCurveBundle receiveCurveBundle = YieldCurveFunctionUtils.getAllYieldCurves(inputs, receiveCurveCalculationConfig, _curveCalculationConfigSource);
    final YieldCurveBundle bundle = new YieldCurveBundle(payCurveBundle);
    bundle.addAll(receiveCurveBundle);
    final ValueProperties properties = desiredValues.iterator().next().getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    return getComputedValues(derivative, bundle, target.toSpecification(), properties);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAP_SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    try {
      final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
      if (type == InterestRateInstrumentType.SWAP_CROSS_CURRENCY) {
        return true;
      }
    } catch (final OpenGammaRuntimeException e) {
      return false;
    }
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirements) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties));
    }
    return results;
  }

  //TODO add curve calculation configurations as requirements
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> payCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    if (payCurveCalculationConfigs == null || payCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    if (receiveCurveCalculationConfigs == null || receiveCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final String payCurveCalculationConfigName = Iterables.getOnlyElement(payCurveCalculationConfigs);
    final String receiveCurveCalculationConfigName = Iterables.getOnlyElement(receiveCurveCalculationConfigs);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final MultiCurveCalculationConfig payCurveCalculationConfig = _curveCalculationConfigSource.getConfig(payCurveCalculationConfigName);
    if (payCurveCalculationConfig == null) {
      s_logger.info("Could not find curve calculation configuration named " + payCurveCalculationConfigName);
      return null;
    }
    final MultiCurveCalculationConfig receiveCurveCalculationConfig = _curveCalculationConfigSource.getConfig(receiveCurveCalculationConfigName);
    if (receiveCurveCalculationConfig == null) {
      s_logger.info("Could not find curve calculation configuration named " + receiveCurveCalculationConfigName);
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(security, securitySource);
    boolean payCurrencyMatched = false;
    boolean receiveCurrencyMatched = false;
    for (final Currency currency : currencies) {
      final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(currency);
      if (targetSpec.equals(payCurveCalculationConfig.getTarget())) {
        payCurrencyMatched = true;
      } else if (targetSpec.equals(receiveCurveCalculationConfig.getTarget())) {
        receiveCurrencyMatched = true;
      }
    }
    if (!payCurrencyMatched) {
      s_logger.info("Pay currency calculation config target {} was not found in {}", payCurveCalculationConfig.getTarget().getUniqueId().getValue(), currencies);
      return null;
    }
    if (!receiveCurrencyMatched) {
      s_logger.info("Receive currency calculation config target {} was not found in {}", receiveCurveCalculationConfig.getTarget().getUniqueId().getValue(), currencies);
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(payCurveCalculationConfig, _curveCalculationConfigSource));
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(receiveCurveCalculationConfig, _curveCalculationConfigSource));
    try {
      final Set<ValueRequirement> timeSeriesRequirements = _definitionConverter.getConversionTimeSeriesRequirements(security, security.accept(_visitor));
      if (timeSeriesRequirements == null) {
        return null;
      }
      requirements.addAll(timeSeriesRequirements);
      return requirements;
    } catch (final Exception e) {
      s_logger.error(e.getMessage());
      return null;
    }
  }

  //TODO work out a sensible way to get calculation properties for all curves into result properties. Prefix each property name with PAY_ and RECEIVE_?
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<String> payLegCurveNames = new HashSet<>();
    final Set<String> receiveLegCurveNames = new HashSet<>();
    String payCurveCalculationConfig = null;
    String receiveCurveCalculationConfig = null;
    final SwapSecurity swap = (SwapSecurity) target.getSecurity();
    final ComputationTargetSpecification payCurrencySpec = ComputationTargetSpecification.of(((InterestRateNotional) swap.getPayLeg().getNotional()).getCurrency());
    final ComputationTargetSpecification receiveCurrencySpec = ComputationTargetSpecification.of(((InterestRateNotional) swap.getReceiveLeg().getNotional()).getCurrency());
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification valueSpec = entry.getKey();
      final String valueName = valueSpec.getValueName();
      if (valueName.equals(ValueRequirementNames.YIELD_CURVE)) {
        final String curveName = valueSpec.getProperty(ValuePropertyNames.CURVE);
        final String curveCalculationConfig = valueSpec.getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        if (valueSpec.getTargetSpecification().equals(payCurrencySpec)) {
          payLegCurveNames.add(curveName);
          payCurveCalculationConfig = curveCalculationConfig;
        } else if (valueSpec.getTargetSpecification().equals(receiveCurrencySpec)) {
          receiveLegCurveNames.add(curveName);
          receiveCurveCalculationConfig = curveCalculationConfig;
        }
      }
    }
    if (payCurveCalculationConfig == null) {
      return null;
    }
    if (receiveCurveCalculationConfig == null) {
      return null;
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig).with(ValuePropertyNames.PAY_CURVE, payLegCurveNames)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveLegCurveNames).get();
    final Set<ValueSpecification> results = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final String valueRequirement : _valueRequirements) {
      results.add(new ValueSpecification(valueRequirement, targetSpec, properties));
    }
    return results;
  }

  /**
   * Calculates the results.
   * 
   * @param derivative The derivative
   * @param bundle The yield curves
   * @param targetSpec The target specification of the results
   * @param properties The result properties
   * @return The results
   */
  protected abstract Set<ComputedValue> getComputedValues(InstrumentDerivative derivative, final YieldCurveBundle bundle, final ComputationTargetSpecification targetSpec,
      final ValueProperties properties);

  /**
   * Gets the value requirement names.
   * 
   * @return The value requirement names
   */
  protected String[] getValueRequirementNames() {
    return _valueRequirements;
  }
}
