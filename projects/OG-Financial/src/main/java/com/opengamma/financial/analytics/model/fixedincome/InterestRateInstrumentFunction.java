/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
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
import com.opengamma.financial.analytics.conversion.CashFlowSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base function for pricing interest-rate instruments without optionality.
 *
 * @deprecated Use descendants of {@link MultiCurvePricingFunction}
 */
@Deprecated
public abstract class InterestRateInstrumentFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateInstrumentFunction.class);
  /** Converts instrument definitions to instrument derivatives */
  private FixedIncomeConverterDataProvider _definitionConverter;
  /** The value requirement produced by this function */
  private final String _valueRequirementName;
  /** Converts securities to instrument definitions */
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
  /** A source for curve calculation configurations */
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  /**
   * @param valueRequirementName The value requirement produced by this function, not null
   */
  public InterestRateInstrumentFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
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
    final InterestRateFutureSecurityConverterDeprecated irFutureConverter = new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .cashSecurityVisitor(cashConverter)
        .cashFlowSecurityVisitor(cashFlowConverter)
        .fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter)
        .interestRateFutureSecurityVisitor(irFutureConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final int numCurveNames = curveNames.length;
    final String[] fullCurveNames = new String[numCurveNames];
    for (int i = 0; i < numCurveNames; i++) {
      fullCurveNames[i] = curveNames[i] + "_" + currency.getCode();
    }
    final String[] yieldCurveNames = numCurveNames == 1 ? new String[] {fullCurveNames[0], fullCurveNames[0] } : fullCurveNames;
    final String[] curveNamesForSecurity = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, yieldCurveNames[0], yieldCurveNames[1]);
    final YieldCurveBundle bundle = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, _curveCalculationConfigSource);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InstrumentDerivative derivative = getDerivative(security, now, timeSeries, curveNamesForSecurity, definition, _definitionConverter);
    return getComputedValues(derivative, bundle, security, target, curveCalculationConfigName, currency.getCode());
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
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    if (currency == null) {
      return null;
    }
    final ValueProperties.Builder properties = getResultProperties(currency.getCode());
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String curveCalculationConfigName = constraints.getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigName == null) {
      return null;
    }
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.debug("Could not find curve calculation configuration named {}", curveCalculationConfigName);
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationConfig.getTarget())) {
      s_logger.info("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getTarget());
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource));
    try {
      final Set<ValueRequirement> timeSeriesRequirements = getDerivativeTimeSeriesRequirements(security, security.accept(_visitor), _definitionConverter);
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

  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getVisitor() {
    return _visitor;
  }

  protected FixedIncomeConverterDataProvider getConverter() {
    return _definitionConverter;
  }

  protected ConfigDBCurveCalculationConfigSource getCurveCalculationConfigSource() {
    return _curveCalculationConfigSource;
  }

  protected String getValueRequirementName() {
    return _valueRequirementName;
  }

  protected abstract Set<ComputedValue> getComputedValues(InstrumentDerivative derivative, YieldCurveBundle bundle, FinancialSecurity security, ComputationTarget target,
      String curveCalculationConfigName, String currency);

  protected ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties().withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  protected ValueProperties.Builder getResultProperties(final String currency, final String curveCalculationConfigName) {
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).with(ValuePropertyNames.CURRENCY,
        currency);
    return properties;
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target, final String curveCalculationConfigName, final String currency) {
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(currency, curveCalculationConfigName).get());
  }

  protected static Set<ValueRequirement> getCurveRequirements(final MultiCurveCalculationConfig curveConfig, final ConfigDBCurveCalculationConfigSource configSource) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    if (curveConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurves = curveConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurves.entrySet()) {
        final String exogenousConfigName = entry.getKey();
        final MultiCurveCalculationConfig exogenousConfig = configSource.getConfig(exogenousConfigName);
        final ComputationTargetSpecification target = exogenousConfig.getTarget();
        final String curveCalculationMethod = exogenousConfig.getCalculationMethod();
        for (final String exogenousCurveName : entry.getValue()) {
          requirements.add(getCurveRequirement(target, exogenousCurveName, exogenousConfigName, curveCalculationMethod));
        }
        requirements.addAll(getCurveRequirements(exogenousConfig, configSource));
      }
    }
    final String[] yieldCurveNames = curveConfig.getYieldCurveNames();
    final String curveCalculationConfigName = curveConfig.getCalculationConfigName();
    final String curveCalculationMethod = curveConfig.getCalculationMethod();
    final ComputationTargetSpecification target = curveConfig.getTarget();
    for (final String yieldCurveName : yieldCurveNames) {
      requirements.add(getCurveRequirement(target, yieldCurveName, curveCalculationConfigName, curveCalculationMethod));
    }
    return requirements;
  }

  protected static ValueRequirement getCurveRequirement(final ComputationTargetSpecification target, final String yieldCurveName, final String curveCalculationConfigName,
      final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, yieldCurveName).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, target, properties);
  }

  //TODO won't work if curves have different currencies
  protected static YieldCurveBundle getYieldCurves(final FunctionInputs inputs, final MultiCurveCalculationConfig curveConfig, final ConfigDBCurveCalculationConfigSource configSource) {
    final YieldCurveBundle curves = new YieldCurveBundle();
    if (curveConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurves = curveConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurves.entrySet()) {
        final String exogenousConfigName = entry.getKey();
        final MultiCurveCalculationConfig exogenousConfig = configSource.getConfig(exogenousConfigName);
        final ComputationTargetSpecification target = exogenousConfig.getTarget();
        final String exogenousCalculationMethod = exogenousConfig.getCalculationMethod();
        for (final String curveName : entry.getValue()) {
          final ValueRequirement curveRequirement = getCurveRequirement(target, curveName, exogenousConfigName, exogenousCalculationMethod);
          final Object curveObject = inputs.getValue(curveRequirement);
          if (curveObject == null) {
            throw new OpenGammaRuntimeException("Could not get curve called " + curveName);
          }
          final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
          curves.setCurve(curveName, curve);
        }
        curves.addAll(getYieldCurves(inputs, exogenousConfig, configSource));
      }
    }
    final String[] curveNames = curveConfig.getYieldCurveNames();
    final ComputationTargetSpecification target = curveConfig.getTarget();
    for (final String curveName : curveNames) {
      final ValueRequirement curveRequirement = getCurveRequirement(target, curveName, curveConfig.getCalculationConfigName(), curveConfig.getCalculationMethod());
      final Object curveObject = inputs.getValue(curveRequirement);
      if (curveObject == null) {
        throw new OpenGammaRuntimeException("Could not get curve called " + curveName);
      }
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
      curves.setCurve(curveName, curve);
    }
    return curves;
  }

  static InstrumentDerivative getDerivative(final FinancialSecurity security, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries, final String[] curveNames,
      final InstrumentDefinition<?> definition, final FixedIncomeConverterDataProvider definitionConverter) {
    final InstrumentDerivative derivative;
    if (security instanceof SwapSecurity) {
      final SwapSecurity swapSecurity = (SwapSecurity) security;
      final InterestRateInstrumentType type = SwapSecurityUtils.getSwapType(swapSecurity);
      if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD || type == InterestRateInstrumentType.SWAP_FIXED_OIS) {
        final Frequency resetFrequency;
        if (swapSecurity.getPayLeg() instanceof FloatingInterestRateLeg) {
          resetFrequency = ((FloatingInterestRateLeg) swapSecurity.getPayLeg()).getFrequency();
        } else {
          resetFrequency = ((FloatingInterestRateLeg) swapSecurity.getReceiveLeg()).getFrequency();
        }
        derivative = definitionConverter.convert(security, definition, now, FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames, resetFrequency),
            timeSeries);
      } else {
        derivative = definitionConverter.convert(security, definition, now, FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames), timeSeries);
      }
    } else {
      derivative = definitionConverter.convert(security, definition, now, FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames), timeSeries);
    }
    return derivative;
  }

  static Set<ValueRequirement> getDerivativeTimeSeriesRequirements(final FinancialSecurity security, final InstrumentDefinition<?> definition,
      final FixedIncomeConverterDataProvider definitionConverter) {
    return definitionConverter.getConversionTimeSeriesRequirements(security, definition);
  }

}
