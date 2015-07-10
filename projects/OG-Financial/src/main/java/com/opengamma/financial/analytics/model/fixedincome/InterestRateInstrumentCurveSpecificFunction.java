/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.core.holiday.HolidaySource;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashFlowSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base function for calculating curve-specific risk factors for interest-rate instruments without optionality.
 *
 * @deprecated Use descendants of {@link MultiCurvePricingFunction}
 */
@Deprecated
public abstract class InterestRateInstrumentCurveSpecificFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateInstrumentCurveSpecificFunction.class);
  /** The requested curve property */
  protected static final String PROPERTY_REQUESTED_CURVE = ValuePropertyNames.OUTPUT_RESERVED_PREFIX + "RequestedCurve";
  /** The value requirement produced by this function */
  private final String _valueRequirement;
  /** Converts securities to instrument definitions */
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
  /** Converts instrument definitions to instrument derivatives */
  private FixedIncomeConverterDataProvider _definitionConverter;
  /** A source for curve calculation configurations */
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  /**
   * @param valueRequirement The value requirement produced by this function, not null
   */
  public InterestRateInstrumentCurveSpecificFunction(final String valueRequirement) {
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
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final CashFlowSecurityConverter cashFlowConverter = new CashFlowSecurityConverter();
    final FRASecurityConverterDeprecated fraConverter = new FRASecurityConverterDeprecated(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverterDeprecated irFutureConverter = new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .cashSecurityVisitor(cashConverter)
        .cashFlowSecurityVisitor(cashFlowConverter)
        .fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter)
        .interestRateFutureSecurityVisitor(irFutureConverter)
        .bondSecurityVisitor(bondConverter)
        .bondFutureSecurityVisitor(bondFutureConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final String fullCurveName = curveName + "_" + currency;
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String[] fullCurveNames = new String[curveNames.length];
    for (int i = 0; i < curveNames.length; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency;
    }
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final InstrumentDerivative derivative = InterestRateInstrumentFunction.getDerivative(security, now, timeSeries, fullCurveNames, definition, _definitionConverter);
    final YieldCurveBundle bundle = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final ValueProperties properties = createValueProperties(target, curveName, curveCalculationConfigName).get();
    final ValueSpecification resultSpec = new ValueSpecification(_valueRequirement, target.toSpecification(), properties);
    return getResults(derivative, fullCurveName, bundle, curveCalculationConfigName, curveCalculationMethod, inputs, target, resultSpec);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return InterestRateInstrumentType.FIXED_INCOME_INSTRUMENT_TARGET_TYPE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    //TODO remove this when we've checked that removing IR futures from the fixed income instrument types
    // doesn't break curves
    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      return false;
    }
    if (security instanceof SwapSecurity) {
      try {
        final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
        return type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD || type == InterestRateInstrumentType.SWAP_IBOR_IBOR ||
            type == InterestRateInstrumentType.SWAP_FIXED_OIS;
      } catch (final OpenGammaRuntimeException ogre) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties(target).get();
    return Collections.singleton(new ValueSpecification(_valueRequirement, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder properties = createValueProperties(target);
    if (OpenGammaCompilationContext.isPermissive(context)) {
      for (final ValueRequirement input : inputs.values()) {
        final String curve = input.getConstraint(PROPERTY_REQUESTED_CURVE);
        if (curve != null) {
          properties.withoutAny(ValuePropertyNames.CURVE).with(ValuePropertyNames.CURVE, curve);
          break;
        }
      }
    }
    return Collections.singleton(new ValueSpecification(_valueRequirement, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    Set<String> requestedCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    final boolean permissive = OpenGammaCompilationContext.isPermissive(context);
    if (!permissive && ((requestedCurveNames == null) || requestedCurveNames.isEmpty())) {
      s_logger.debug("Must specify a curve name");
      return null;
    }
    final String curveCalculationConfigName = constraints.getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigName == null) {
      s_logger.debug("Must specify a curve calculation config");
      return null;
    }
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.debug("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final String[] availableCurveNames = curveCalculationConfig.getYieldCurveNames();
    if ((requestedCurveNames == null) || requestedCurveNames.isEmpty()) {
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
    if (!permissive && (curveNames.size() != 1)) {
      s_logger.debug("Must specify single curve name constraint, got {}", curveNames);
      return null;
    }
    final String curve = curveNames.iterator().next();
    final Set<ValueRequirement> curveRequirements = YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource);
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(curveRequirements.size());
    for (final ValueRequirement curveRequirement : curveRequirements) {
      final ValueProperties.Builder properties = curveRequirement.getConstraints().copy();
      properties.with(PROPERTY_REQUESTED_CURVE, curve).withOptional(PROPERTY_REQUESTED_CURVE);
      requirements.add(new ValueRequirement(curveRequirement.getValueName(), curveRequirement.getTargetReference(), properties.get()));
    }
    try {
      final Set<ValueRequirement> timeSeriesRequirements = InterestRateInstrumentFunction.getDerivativeTimeSeriesRequirements(security, security.accept(_visitor), _definitionConverter);
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

  protected abstract Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final YieldCurveBundle curves, final String curveCalculationConfigName,
      final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target, final ValueSpecification resultSpec);

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURRENCY, currency).with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .withAny(ValuePropertyNames.CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    return properties;
  }

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final String curveName, final String curveCalculationConfigName) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURRENCY, currency).with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName);
    return properties;
  }

  protected FixedIncomeConverterDataProvider getConverter() {
    return _definitionConverter;
  }

  protected String getValueRequirement() {
    return _valueRequirement;
  }

  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getVisitor() {
    return _visitor;
  }

  protected ConfigDBCurveCalculationConfigSource getCurveCalculationConfigSource() {
    return _curveCalculationConfigSource;
  }

}
