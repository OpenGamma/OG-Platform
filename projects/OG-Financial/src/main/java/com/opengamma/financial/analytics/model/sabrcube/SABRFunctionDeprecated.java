/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
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
import com.opengamma.financial.analytics.conversion.CapFloorCMSSpreadSecurityConverter;
import com.opengamma.financial.analytics.conversion.CapFloorSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see SABRFunction
 */
@Deprecated
public abstract class SABRFunctionDeprecated extends AbstractFunction.NonCompiledInvoker {
  /** String labelling the type of SABR calculation (with right extrapolation) */
  public static final String SABR_RIGHT_EXTRAPOLATION = "SABRRightExtrapolation";
  /** String labelling the type of SABR extrapolation (none) */
  public static final String SABR_NO_EXTRAPOLATION = "SABRNoExtrapolation";

  private FinancialSecurityVisitor<InstrumentDefinition<?>> _securityVisitor;
  private SecuritySource _securitySource;
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final SwaptionSecurityConverter swaptionConverter = new SwaptionSecurityConverter(_securitySource, swapConverter);
    final CapFloorSecurityConverter capFloorVisitor = new CapFloorSecurityConverter(holidaySource, conventionSource, regionSource);
    final CapFloorCMSSpreadSecurityConverter capFloorCMSSpreadSecurityVisitor = new CapFloorCMSSpreadSecurityConverter(holidaySource, conventionSource, regionSource);
    _securityVisitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swapSecurityVisitor(swapConverter).swaptionVisitor(swaptionConverter).capFloorVisitor(capFloorVisitor)
        .capFloorCMSSpreadVisitor(capFloorCMSSpreadSecurityVisitor).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(getVisitor());
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String conventionName = currency.getCode() + "_SWAP";
    final ConventionBundle convention = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention named " + conventionName);
    }
    final DayCount dayCount = convention.getSwapFloatingLegDayCount();
    if (dayCount == null) {
      throw new OpenGammaRuntimeException("Could not get daycount");
    }
    final SABRInterestRateDataBundle data = getModelParameters(target, inputs, currency, dayCount, desiredValue);
    final InstrumentDerivative derivative = getConverter().convert(security, definition, now, new String[] {fundingCurveName, forwardCurveName }, timeSeries);
    final Object result = getResult(derivative, data, desiredValue);
    final ValueProperties properties = getResultProperties(createValueProperties().get(), currency.getCode(), desiredValue);
    final ValueSpecification spec = new ValueSpecification(getValueRequirement(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final ValueProperties properties = getResultProperties(createValueProperties().get(), currency);
    return Collections.singleton(new ValueSpecification(getValueRequirement(), target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> forwardCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> fundingCurveNames = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurveNames == null || fundingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> cubeNames = constraints.getValues(ValuePropertyNames.CUBE);
    if (cubeNames == null || cubeNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationMethods = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethods == null || curveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> fittingMethods = constraints.getValues(SmileFittingProperties.PROPERTY_FITTING_METHOD);
    if (fittingMethods == null || fittingMethods.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurveNames.iterator().next();
    final String fundingCurveName = fundingCurveNames.iterator().next();
    final String cubeName = cubeNames.iterator().next();
    final String curveCalculationMethod = curveCalculationMethods.iterator().next();
    final String fittingMethod = fittingMethods.iterator().next();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getCurveRequirement(forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency));
    requirements.add(getCurveRequirement(fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency));
    requirements.add(getCubeRequirement(cubeName, currency, fittingMethod));
    final Set<ValueRequirement> timeSeriesRequirements = getConverter()
        .getConversionTimeSeriesRequirements(security, security.accept(getVisitor()));
    if (timeSeriesRequirements == null) {
      return null;
    }
    requirements.addAll(timeSeriesRequirements);
    return requirements;
  }

  protected abstract String getValueRequirement();

  protected abstract Object getResult(final InstrumentDerivative derivative, final SABRInterestRateDataBundle data, final ValueRequirement desiredValue);

  protected ValueRequirement getCurveRequirement(final String curveName, final String advisoryForward, final String advisoryFunding, final String calculationMethod,
      final Currency currency) {
    return YieldCurveFunction.getCurveRequirement(currency, curveName, advisoryForward, advisoryFunding, calculationMethod);
  }

  protected ValueRequirement getCubeRequirement(final String cubeName, final Currency currency, final String fittingMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(ValuePropertyNames.CURRENCY, Currency.USD.getCode()) // TODO should be 'currency.getCode()' when non-USD currencies supported
        .with(SmileFittingProperties.PROPERTY_VOLATILITY_MODEL, SmileFittingProperties.SABR)
        .with(SmileFittingProperties.PROPERTY_FITTING_METHOD, fittingMethod).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, Currency.USD, properties); // TODO should be 'currency' when non-USD currencies supported
  }

  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getVisitor() {
    return _securityVisitor;
  }

  protected FixedIncomeConverterDataProvider getConverter() {
    return _definitionConverter;
  }

  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

  protected YieldCurveBundle getYieldCurves(final FunctionInputs inputs, final Currency currency, final ValueRequirement desiredValue) {
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final Object forwardCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final Object fundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    if (fundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get funding curve");
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName }, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve });
  }

  protected abstract SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final DayCount dayCount, final ValueRequirement desiredValue);

  protected abstract ValueProperties getResultProperties(final ValueProperties properties, final String currency);

  protected abstract ValueProperties getResultProperties(final ValueProperties properties, final String currency, final ValueRequirement desiredValue);
}
