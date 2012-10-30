/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.volatility.VolatilityDataFittingDefaults;
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
 * @see SABRYCNSFunction
 */
@Deprecated
public abstract class SABRYCNSFunctionDeprecated extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRYCNSFunctionDeprecated.class);
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();

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
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final PresentValueNodeSensitivityCalculator nodeCalculator = getNodeSensitivityCalculator(desiredValue);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String forwardCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE).iterator().next();
    final String fundingCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE).iterator().next();
    final String curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
    final String calculationMethod = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD).iterator().next();
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InstrumentDefinition<?> definition = security.accept(_securityVisitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final String conventionName = currency.getCode() + "_SWAP";
    final ConventionBundle convention = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName));
    if (convention == null) {
      throw new OpenGammaRuntimeException("Could not get convention named " + conventionName);
    }
    final DayCount dayCount = convention.getSwapFloatingLegDayCount();
    if (dayCount == null) {
      throw new OpenGammaRuntimeException("Could not get daycount");
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final SABRInterestRateDataBundle data = getModelParameters(target, inputs, currency, dayCount, desiredValue);
    final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, new String[] {fundingCurveName, forwardCurveName }, timeSeries);
    final ValueProperties properties = getResultProperties(target, desiredValue);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
    if (calculationMethod.equals(InterpolatedDataProperties.CALCULATION_METHOD_NAME)) {
      final DoubleMatrix1D sensitivities = CALCULATOR.calculateFromSimpleInterpolatedCurve(derivative, data, nodeCalculator);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, data, sensitivities, curveSpec, spec);
    }
    final Object jacobianObject = inputs.getValue(getJacobianRequirement(currency, forwardCurveName, fundingCurveName, calculationMethod));
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    if (calculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
      final Object couponSensitivityObject = inputs.getValue(getCouponSensitivitiesRequirement(currency, forwardCurveName, fundingCurveName));
      if (couponSensitivityObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
      sensitivities = CALCULATOR.calculateFromPresentValue(derivative, null, data, couponSensitivity, jacobian, nodeCalculator);
    } else {
      sensitivities = CALCULATOR.calculateFromParRate(derivative, null, data, jacobian, nodeCalculator);
    }
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, data, sensitivities, curveSpec, spec);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties properties = getResultProperties(ccy);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      s_logger.error("Must ask for a single named curve");
      return null;
    }
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
    final Set<String> fittingMethods = constraints.getValues(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD);
    if (fittingMethods == null || fittingMethods.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurveNames.iterator().next();
    final String fundingCurveName = fundingCurveNames.iterator().next();
    final String cubeName = cubeNames.iterator().next();
    final String curveName = curveNames.iterator().next();
    final String curveCalculationMethod = curveCalculationMethods.iterator().next();
    final String fittingMethod = fittingMethods.iterator().next();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency);
    final ValueRequirement fundingCurveRequirement = getCurveRequirement(fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency);
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
    final ValueRequirement cubeRequirement = getCubeRequirement(cubeName, currency, fittingMethod);
    final Set<ValueRequirement> requirements = Sets.newHashSet(forwardCurveRequirement, fundingCurveRequirement, cubeRequirement, curveSpecRequirement);
    if (curveCalculationMethod.equals(InterpolatedDataProperties.CALCULATION_METHOD_NAME)) {
      return requirements;
    }
    final ValueRequirement jacobianRequirement = getJacobianRequirement(currency, forwardCurveName, fundingCurveName, curveCalculationMethod);
    requirements.add(jacobianRequirement);
    if (curveCalculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
      final ValueRequirement couponSensitivitiesRequirement = getCouponSensitivitiesRequirement(currency, forwardCurveName, fundingCurveName);
      requirements.add(couponSensitivitiesRequirement);
    }
    final Set<ValueRequirement> timeSeriesRequirements = _definitionConverter.getConversionTimeSeriesRequirements(security, security.accept(_securityVisitor));
    if (timeSeriesRequirements == null) {
      return null;
    }
    requirements.addAll(timeSeriesRequirements);
    return requirements;
  }

  protected abstract SABRInterestRateDataBundle getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final Currency currency,
      final DayCount dayCount, final ValueRequirement desiredValue);

  private ValueRequirement getCurveRequirement(final String curveName, final String advisoryForward, final String advisoryFunding, final String calculationMethod,
      final Currency currency) {
    return YieldCurveFunction.getCurveRequirement(currency, curveName, advisoryForward, advisoryFunding, calculationMethod);
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

  protected ValueRequirement getCubeRequirement(final String cubeName, final Currency currency, final String fittingMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(ValuePropertyNames.CURRENCY, Currency.USD.getCode()) // TODO should be 'currency.getCode()' when non-USD currencies supported
        .with(VolatilityDataFittingDefaults.PROPERTY_VOLATILITY_MODEL, VolatilityDataFittingDefaults.SABR_FITTING)
        .with(VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD, fittingMethod).get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, ComputationTargetSpecification.of(Currency.USD), properties); // TODO should be 'currency' when non-USD currencies supported
  }

  protected abstract ValueProperties getResultProperties(final Currency currency);

  protected abstract ValueProperties getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue);

  protected abstract PresentValueNodeSensitivityCalculator getNodeSensitivityCalculator(final ValueRequirement desiredValue);

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties);
  }

  private ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getCouponSensitivityRequirement(currency, forwardCurveName, fundingCurveName);
  }

  private ValueRequirement getJacobianRequirement(final Currency currency, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod) {
    return YieldCurveFunction.getJacobianRequirement(currency, forwardCurveName, fundingCurveName, curveCalculationMethod);
  }

}
