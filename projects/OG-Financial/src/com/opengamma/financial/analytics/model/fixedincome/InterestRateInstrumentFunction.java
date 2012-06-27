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

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class InterestRateInstrumentFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateInstrumentFunction.class);
  private FixedIncomeConverterDataProvider _definitionConverter;
  private final String _valueRequirementName;
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;

  public InterestRateInstrumentFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter();
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter).interestRateFutureSecurityVisitor(irFutureConverter).bondSecurityVisitor(bondConverter)
        .bondFutureSecurityVisitor(bondFutureConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final YieldCurveBundle bundle = YieldCurveFunctionUtils.getAllYieldCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InstrumentDerivative derivative = getDerivative(security, now, dataSource, curveNames, definition, _definitionConverter);
    return getComputedValues(derivative, bundle, security, target, curveCalculationConfigName, currency.getCode());
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    //TODO remove this when we've checked that removing IR futures from the fixed income instrument types
    // doesn't break curves
    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      return false;
    }
    if (security instanceof SwapSecurity) {
      try {
        final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
        return type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD
            || type == InterestRateInstrumentType.SWAP_IBOR_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_OIS;
      } catch (final OpenGammaRuntimeException ogre) {
        return false;
      }
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType(security);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties.Builder properties = getResultProperties(currency);
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    if (!currency.equals(curveCalculationConfig.getUniqueId())) {
      s_logger.error("Security currency and curve calculation config id were not equal; have {} and {}", currency, curveCalculationConfig.getUniqueId());
    }
    return YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource);
  }

  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getVisitor() {
    return _visitor;
  }

  protected FixedIncomeConverterDataProvider getConverter() {
    return _definitionConverter;
  }

  protected String getValueRequirementName() {
    return _valueRequirementName;
  }

  protected abstract Set<ComputedValue> getComputedValues(InstrumentDerivative derivative, YieldCurveBundle bundle, FinancialSecurity security, ComputationTarget target,
      String curveCalculationConfigName, String currency);

  protected ValueProperties.Builder getResultProperties(final String currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  protected ValueProperties.Builder getResultProperties(final String currency, final String curveCalculationConfigName) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURRENCY, currency);
    return properties;
  }

  protected ValueSpecification getResultSpec(final ComputationTarget target, final String curveCalculationConfigName, final String currency) {
    return new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(currency, curveCalculationConfigName).get());
  }

  protected static Set<ValueRequirement> getCurveRequirements(final MultiCurveCalculationConfig curveConfig, final ConfigDBCurveCalculationConfigSource configSource) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    if (curveConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurves = curveConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurves.entrySet()) {
        final String exogenousConfigName = entry.getKey();
        final MultiCurveCalculationConfig exogenousConfig = configSource.getConfig(exogenousConfigName);
        final UniqueIdentifiable id = exogenousConfig.getUniqueId();
        final String curveCalculationMethod = exogenousConfig.getCalculationMethod();
        for (final String exogenousCurveName : entry.getValue()) {
          requirements.add(getCurveRequirement(id, exogenousCurveName, exogenousConfigName, curveCalculationMethod));
        }
        requirements.addAll(getCurveRequirements(exogenousConfig, configSource));
      }
    }
    final String[] yieldCurveNames = curveConfig.getYieldCurveNames();
    final String curveCalculationConfigName = curveConfig.getCalculationConfigName();
    final String curveCalculationMethod = curveConfig.getCalculationMethod();
    final UniqueIdentifiable uniqueId = curveConfig.getUniqueId();
    for (final String yieldCurveName : yieldCurveNames) {
      requirements.add(getCurveRequirement(uniqueId, yieldCurveName, curveCalculationConfigName, curveCalculationMethod));
    }
    return requirements;
  }

  protected static ValueRequirement getCurveRequirement(final UniqueIdentifiable id, final String yieldCurveName, final String curveCalculationConfigName,
      final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, yieldCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, id.getUniqueId(), properties);
  }

  //TODO won't work if curves have different currencies
  protected static YieldCurveBundle getYieldCurves(final FunctionInputs inputs, final MultiCurveCalculationConfig curveConfig, final ConfigDBCurveCalculationConfigSource configSource) {
    final YieldCurveBundle curves = new YieldCurveBundle();
    if (curveConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurves = curveConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurves.entrySet()) {
        final String exogenousConfigName = entry.getKey();
        final MultiCurveCalculationConfig exogenousConfig = configSource.getConfig(exogenousConfigName);
        final UniqueIdentifiable exogenousId = exogenousConfig.getUniqueId();
        final String exogenousCalculationMethod = exogenousConfig.getCalculationMethod();
        for (final String curveName : entry.getValue()) {
          final ValueRequirement curveRequirement = getCurveRequirement(exogenousId, curveName, exogenousConfigName, exogenousCalculationMethod);
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
    final UniqueIdentifiable id = curveConfig.getUniqueId();
    for (final String curveName : curveNames) {
      final ValueRequirement curveRequirement = getCurveRequirement(id, curveName, curveConfig.getCalculationConfigName(), curveConfig.getCalculationMethod());
      final Object curveObject = inputs.getValue(curveRequirement);
      if (curveObject == null) {
        throw new OpenGammaRuntimeException("Could not get curve called " + curveName);
      }
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
      curves.setCurve(curveName, curve);
    }
    return curves;
  }

  static InstrumentDerivative getDerivative(final FinancialSecurity security, final ZonedDateTime now, final HistoricalTimeSeriesSource dataSource, final String[] curveNames,
      final InstrumentDefinition<?> definition, final FixedIncomeConverterDataProvider definitionConverter) {
    final InstrumentDerivative derivative;
    if (security instanceof SwapSecurity) {
      SwapSecurity swapSecurity = (SwapSecurity) security;
      InterestRateInstrumentType type = SwapSecurityUtils.getSwapType(swapSecurity);
      if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD) {
        final Frequency resetFrequency;
        if (swapSecurity.getPayLeg() instanceof FloatingInterestRateLeg) {
          resetFrequency = ((FloatingInterestRateLeg) swapSecurity.getPayLeg()).getFrequency();
        } else {
          resetFrequency = ((FloatingInterestRateLeg) swapSecurity.getReceiveLeg()).getFrequency();
        }
        derivative = definitionConverter.convert(security, definition, now,
            FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames, resetFrequency), dataSource);   
      } else {
        derivative = definitionConverter.convert(security, definition, now,
            FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames), dataSource);        
      }
    } else {
      derivative = definitionConverter.convert(security, definition, now,
        FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security, curveNames), dataSource);
    }
    return derivative;
  }

}
