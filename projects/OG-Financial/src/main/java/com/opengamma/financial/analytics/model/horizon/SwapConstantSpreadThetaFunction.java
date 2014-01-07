/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_TIME_SERIES;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.THETA_CONSTANT_SPREAD;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.horizon.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FixingTimeSeriesVisitor;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class SwapConstantSpreadThetaFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SwapConstantSpreadThetaFunction.class);
  /** Converts securities to definitions */
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
  /** Converts definitions to derivatives */
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swapSecurityVisitor(swapConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
    ConfigDBCurveCalculationConfigSource.reinitOnChanges(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final SwapSecurity security = (SwapSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String daysForward = desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final int numCurveNames = curveNames.length;
    final String[] fullCurveNames = new String[numCurveNames];
    for (int i = 0; i < numCurveNames; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    final YieldCurveBundle bundle = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final Calendar calendar = CalendarUtils.getCalendar(regionSource, holidaySource, ExternalSchemes.currencyRegionId(currency));
    final ZonedDateTimeDoubleTimeSeries[] fixingSeries = new ZonedDateTimeDoubleTimeSeries[] {FixingTimeSeriesVisitor.convertTimeSeries(
        (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES)) };
    final String[] yieldCurveNames = numCurveNames == 1 ? new String[] {fullCurveNames[0], fullCurveNames[0] } : fullCurveNames;
    final String[] curveNamesForSecurity = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, yieldCurveNames[0], yieldCurveNames[1]);
    final ConstantSpreadHorizonThetaCalculator calculator = ConstantSpreadHorizonThetaCalculator.getInstance();
    if (definition instanceof SwapDefinition) {
      final MultipleCurrencyAmount theta = calculator.getTheta((SwapDefinition) definition, now, curveNamesForSecurity, bundle, fixingSeries, Integer.parseInt(daysForward),
          calendar);
      if (theta.size() != 1) {
        throw new OpenGammaRuntimeException("Got multi-currency amount for theta " + theta + "; should only use this function for single currency swaps");
      }
      return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, currency.getCode(), daysForward), theta.getAmount(currency)));
    }
    throw new OpenGammaRuntimeException("Can only handle fixed / float ibor and ois swaps; have " + definition.getClass());
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAP_SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof SwapSecurity
        && InterestRateInstrumentType.isFixedIncomeInstrumentType((SwapSecurity) target.getSecurity()))) {
      return false;
    }
    final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity((FinancialSecurity) target.getSecurity());
    return type == InterestRateInstrumentType.SWAP_FIXED_IBOR ||
        type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD ||
        type == InterestRateInstrumentType.SWAP_FIXED_OIS;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties.Builder properties = getResultProperties(currency);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> daysForwardNames = desiredValue.getConstraints().getValues(PROPERTY_DAYS_TO_MOVE_FORWARD);
    if (daysForwardNames == null || daysForwardNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.info("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final Set<ValueRequirement> requirements = YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource);
    try {
      final Set<ValueRequirement> fixingRequirements = getDerivativeTimeSeriesRequirements(security, security.accept(_visitor), _definitionConverter);
      if (fixingRequirements == null) {
        return null;
      }
      final Set<ValueRequirement> timeSeriesRequirements = new HashSet<>();
      for (final ValueRequirement fixingRequirement : fixingRequirements) {
        final ValueProperties properties = fixingRequirement.getConstraints().copy()
            .with(PROPERTY_DAYS_TO_MOVE_FORWARD, daysForwardNames)
            .withOptional(PROPERTY_DAYS_TO_MOVE_FORWARD)
            .with(CURVE_CALCULATION_CONFIG, curveCalculationConfigNames)
            .withOptional(CURVE_CALCULATION_CONFIG)
            .get();
        timeSeriesRequirements.add(new ValueRequirement(fixingRequirement.getValueName(), fixingRequirement.getTargetReference(), properties));
      }
      requirements.addAll(timeSeriesRequirements);
      return requirements;
    } catch (final Exception e) {
      s_logger.error(e.getMessage());
      return null;
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveCalculationConfig = null;
    String daysForward = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      final ValueRequirement requirement = input.getValue();
      if (requirement.getValueName().equals(HISTORICAL_TIME_SERIES)) {
        curveCalculationConfig = requirement.getConstraint(CURVE_CALCULATION_CONFIG);
        daysForward = requirement.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD);
        break;
      }
    }
    if (curveCalculationConfig == null) {
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    return Collections.singleton(getResultSpec(target, curveCalculationConfig, currency.getCode(), daysForward));
  }

  /**
   * Gets the result properties.
   * @param currency The currency
   * @return The result properties
   */
  private ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD)
        .withAny(PROPERTY_DAYS_TO_MOVE_FORWARD);
    return properties;
  }

  /**
   * Gets the result properties.
   * @param currency The currency
   * @param curveCalculationConfig The curve calculation configuration
   * @param daysForward The days forward
   * @return The result properties
   */
  private ValueProperties.Builder getResultProperties(final String currency, final String curveCalculationConfig, final String daysForward) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD)
        .with(PROPERTY_DAYS_TO_MOVE_FORWARD, daysForward);
    return properties;
  }

  /**
   * Gets the result specification.
   * @param target The target
   * @param curveCalculationConfig The curve calculation configuration
   * @param currency The currency
   * @param daysForward The days forward
   * @return The result properties
   */
  private ValueSpecification getResultSpec(final ComputationTarget target, final String curveCalculationConfig, final String currency, final String daysForward) {
    return new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), getResultProperties(currency, curveCalculationConfig, daysForward).get());
  }

  private static InstrumentDerivative getDerivative(final FinancialSecurity security, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries, final String[] curveNames,
      final InstrumentDefinition<?> definition, final FixedIncomeConverterDataProvider definitionConverter) {
    final InstrumentDerivative derivative;
    final SwapSecurity swapSecurity = (SwapSecurity) security;
    final InterestRateInstrumentType type = SwapSecurityUtils.getSwapType(swapSecurity);
    if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD || type == InterestRateInstrumentType.SWAP_FIXED_OIS) {
      final Frequency resetFrequency;
      if (swapSecurity.getPayLeg() instanceof FloatingInterestRateLeg) {
        resetFrequency = ((FloatingInterestRateLeg) swapSecurity.getPayLeg()).getFrequency();
      } else {
        resetFrequency = ((FloatingInterestRateLeg) swapSecurity.getReceiveLeg()).getFrequency();
      }
      derivative = definitionConverter.convert(security, definition, now,
          FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames, resetFrequency), timeSeries);
    } else {
      derivative = definitionConverter.convert(security, definition, now,
          FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames), timeSeries);
    }
    return derivative;
  }

  /**
   * Gets the fixing time series requirements for a swap
   * @param security The security
   * @param definition The definition
   * @param definitionConverter The definition converter
   * @return The set of fixing time series requirements
   */
  private static Set<ValueRequirement> getDerivativeTimeSeriesRequirements(final FinancialSecurity security, final InstrumentDefinition<?> definition,
      final FixedIncomeConverterDataProvider definitionConverter) {
    return definitionConverter.getConversionTimeSeriesRequirements(security, definition);
  }
}
