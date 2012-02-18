/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.ircurve.InterpolatedYieldCurveFunction;
import com.opengamma.financial.analytics.model.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InterestRateFutureOptionYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  @SuppressWarnings("unchecked")
  private static final VolatilityFunctionProvider<SABRFormulaData> SABR_FUNCTION = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory
  .getCalculator(VolatilityFunctionFactory.HAGAN);
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivitySABRCalculator.getInstance());
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  private InterestRateFutureOptionTradeConverter _converter;
  private FixedIncomeConverterDataProvider _dataConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _converter = new InterestRateFutureOptionTradeConverter(new InterestRateFutureOptionSecurityConverter(holidaySource, conventionSource, regionSource, securitySource));
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Trade trade = target.getTrade();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String forwardCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE).iterator().next();
    final String fundingCurveName = constraints.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE).iterator().next();
    final String surfaceName = constraints.getValues(ValuePropertyNames.SURFACE).iterator().next();
    final String calculationMethod = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD).iterator().next();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement forwardCurveSpecRequirement = getCurveSpecRequirement(target, forwardCurveName);
    final Object forwardCurveSpecObject = inputs.getValue(forwardCurveSpecRequirement);
    if (forwardCurveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveSpecRequirement);
    }
    Object fundingCurveSpecObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveSpecRequirement = getCurveSpecRequirement(target, fundingCurveName);
      fundingCurveSpecObject = inputs.getValue(fundingCurveSpecRequirement);
      if (fundingCurveSpecObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveSpecRequirement);
      }
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) forwardCurveSpecObject;
    final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpec = fundingCurveSpecObject == null ? forwardCurveSpec
        : (InterpolatedYieldCurveSpecificationWithSecurities) fundingCurveSpecObject;
    final InstrumentDefinition<?> definition = _converter.convert(trade);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for trade " + trade + " was null");
    }
    final FinancialSecurity security = (FinancialSecurity) trade.getSecurity();
    final InstrumentDerivative derivative = _dataConverter.convert(security, definition, now, new String[] {fundingCurveName, forwardCurveName}, dataSource);
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivitiesForCurves;
    final Object couponSensitivityObject = inputs.getValue(getCouponSensitivityRequirement(target));
    if (couponSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
    }
    final SABRInterestRateDataBundle bundle = new SABRInterestRateDataBundle(getModelParameters(target, inputs, surfaceName),
        getYieldCurves(target, inputs, forwardCurveName, fundingCurveName, calculationMethod));
    final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
    if (calculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
      sensitivitiesForCurves = CALCULATOR.calculateFromPresentValue(derivative, null, bundle, couponSensitivity, jacobian, NSC);
    } else if (calculationMethod.equals(InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME)) {
      sensitivitiesForCurves = CALCULATOR.calculateFromSimpleInterpolatedCurve(derivative, bundle, NSC);
    } else {
      throw new OpenGammaRuntimeException("Should not happen");
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    if (fundingCurveName.equals(forwardCurveName)) {
      return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(forwardCurveName, bundle, sensitivitiesForCurves, forwardCurveSpec,
          getForwardResultSpec(target, currency, forwardCurveName, fundingCurveName, surfaceName, calculationMethod));
    }
    final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs = new HashMap<String, InterpolatedYieldCurveSpecificationWithSecurities>();
    curveSpecs.put(forwardCurveName, forwardCurveSpec);
    curveSpecs.put(fundingCurveName, fundingCurveSpec);
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForMultipleCurves(forwardCurveName, fundingCurveName,
        getForwardResultSpec(target, currency, forwardCurveName, fundingCurveName, surfaceName, calculationMethod),
        getFundingResultSpec(target, currency, forwardCurveName, fundingCurveName, surfaceName, calculationMethod), bundle, sensitivitiesForCurves, curveSpecs);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    return Sets.newHashSet(getFundingResultSpec(target, ccy), getForwardResultSpec(target, ccy));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> forwardCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    if (forwardCurves == null || forwardCurves.size() != 1) {
      return null;
    }
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> calculationMethodNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (calculationMethodNames == null || calculationMethodNames.size() != 1) {
      return null;
    }
    final String forwardCurveName = forwardCurves.iterator().next();
    final String fundingCurveName = fundingCurves.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final String calculationMethod = calculationMethodNames.iterator().next();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(getSurfaceRequirement(target, surfaceName));
    if (calculationMethod.equals(InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME)) {
      requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, forwardCurveName, calculationMethod));
    } else {
      if (forwardCurveName.equals(fundingCurveName)) {
        requirements.add(getCurveRequirement(target, forwardCurveName, null, null, calculationMethod));
        requirements.add(getCouponSensitivityRequirement(target));
        requirements.add(getCurveSpecRequirement(target, forwardCurveName));
        requirements.add(getJacobianRequirement(target, calculationMethod));
        return requirements;
      }
      requirements.add(getCurveRequirement(target, forwardCurveName, forwardCurveName, fundingCurveName, calculationMethod));
      requirements.add(getCurveRequirement(target, fundingCurveName, forwardCurveName, fundingCurveName, calculationMethod));
      requirements.add(getSurfaceRequirement(target, surfaceName));
      requirements.add(getCouponSensitivityRequirement(target, forwardCurveName, fundingCurveName));
      requirements.add(getCurveSpecRequirement(target, forwardCurveName));
      requirements.add(getCurveSpecRequirement(target, fundingCurveName));
      requirements.add(getJacobianRequirement(target, forwardCurveName, fundingCurveName, calculationMethod));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String forwardCurveName = null;
    String fundingCurveName = null;
    String surfaceName = null;
    String calculationMethod = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      final ValueProperties properties = input.getKey().getProperties();
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getKey().getValueName())) {
        calculationMethod = properties.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD).iterator().next();
        if (properties.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE) != null) {
          forwardCurveName = properties.getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE).iterator().next();
          fundingCurveName = properties.getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE).iterator().next();
        } else {
          if (calculationMethod.equals(InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME)) {
            forwardCurveName = properties.getValues(ValuePropertyNames.CURVE).iterator().next();
            fundingCurveName = forwardCurveName;
          }
        }
        if (surfaceName != null) {
          break;
        }
      }
      if (ValueRequirementNames.SABR_SURFACES.equals(input.getKey().getValueName())) {
        surfaceName = properties.getValues(ValuePropertyNames.SURFACE).iterator().next();
        if (forwardCurveName != null) {
          break;
        }
      }
    }
    assert forwardCurveName != null;
    assert fundingCurveName != null;
    assert surfaceName != null;
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    if (calculationMethod.equals(InterpolatedYieldCurveFunction.CALCULATION_METHOD_NAME)) {
      return Sets.newHashSet(getForwardResultSpec(target, ccy, forwardCurveName, fundingCurveName, surfaceName, calculationMethod));
    }
    if (fundingCurveName.equals(forwardCurveName)) {
      return Sets.newHashSet(getForwardResultSpec(target, ccy, forwardCurveName, fundingCurveName, surfaceName, calculationMethod));
    }
    return Sets.newHashSet(getFundingResultSpec(target, ccy, forwardCurveName, fundingCurveName, surfaceName, calculationMethod),
        getForwardResultSpec(target, ccy, forwardCurveName, fundingCurveName, surfaceName, calculationMethod));
  }

  private ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding, final String calculationMethod) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), curveName, advisoryForward, advisoryFunding,
        calculationMethod);
  }

  protected ValueRequirement getSurfaceRequirement(final ComputationTarget target, final String surfaceName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "IR_FUTURE_OPTION").get();
    return new ValueRequirement(ValueRequirementNames.SABR_SURFACES, currency, properties);
  }

  protected YieldCurveBundle getYieldCurves(final ComputationTarget target, final FunctionInputs inputs, final String forwardCurveName, final String fundingCurveName, final String calculationMethod) {
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null, calculationMethod);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null, calculationMethod);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve : (YieldAndDiscountCurve) fundingCurveObject;
    return new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName}, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve});
  }

  private SABRInterestRateParameters getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final String surfaceName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueRequirement surfacesRequirement = getSurfaceRequirement(target, surfaceName);
    final Object surfacesObject = inputs.getValue(surfacesRequirement);
    if (surfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + surfacesRequirement);
    }
    final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) surfacesObject;
    if (!surfaces.getCurrency().equals(currency)) {
      throw new OpenGammaRuntimeException("Don't know how this happened");
    }
    final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
    final DayCount dayCount = surfaces.getDayCount();
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount, SABR_FUNCTION);
  }

  private ValueSpecification getForwardResultSpec(final ComputationTarget target, final Currency ccy) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueSpecification getFundingResultSpec(final ComputationTarget target, final Currency ccy) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueSpecification getForwardResultSpec(final ComputationTarget target, final Currency ccy, final String forwardCurveName, final String fundingCurveName, final String surfaceName,
      final String calculationMethod) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod)
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueSpecification getFundingResultSpec(final ComputationTarget target, final Currency ccy, final String forwardCurveName, final String fundingCurveName, final String surfaceName,
      final String calculationMethod) {
    final ValueProperties result = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod)
        .with(ValuePropertyNames.CURVE, fundingCurveName)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).get();
    return new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), result);
  }

  private ValueRequirement getJacobianRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String calculationMethod) {
    return YieldCurveFunction.getJacobianRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), forwardCurveName, fundingCurveName,
        calculationMethod);
  }

  private ValueRequirement getJacobianRequirement(final ComputationTarget target, final String calculationMethod) {
    return YieldCurveFunction.getJacobianRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()),
        calculationMethod);
  }

  private ValueRequirement getCouponSensitivityRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getCouponSensitivityRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()), forwardCurveName, fundingCurveName);
  }

  private ValueRequirement getCouponSensitivityRequirement(final ComputationTarget target) {
    return YieldCurveFunction.getCouponSensitivityRequirement(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()));
  }

  private ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }
}
