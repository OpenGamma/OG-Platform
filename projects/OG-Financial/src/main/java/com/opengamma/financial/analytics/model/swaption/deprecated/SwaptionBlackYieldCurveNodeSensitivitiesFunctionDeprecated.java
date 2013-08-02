/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.deprecated;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackFunctionDeprecated;
import com.opengamma.financial.analytics.model.swaption.SwaptionUtils;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Use the version of this function that does not refer to funding and forward curves
 * @see SwaptionBlackYieldCurveNodeSensitivitiesFunction
 */
@Deprecated
public class SwaptionBlackYieldCurveNodeSensitivitiesFunctionDeprecated extends AbstractFunction.NonCompiledInvoker {
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivityBlackCalculator.getInstance());
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private SwaptionSecurityConverter _visitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    _visitor = new SwaptionSecurityConverter(securitySource, swapConverter);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final SwaptionSecurity security = (SwaptionSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final String forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final Object forwardCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final Object fundingCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod));
    if (fundingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get funding curve");
    }
    final Object volatilitySurfaceObject = inputs.getValue(getVolatilityRequirement(surfaceName, currency));
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final VolatilitySurface volatilitySurface = (VolatilitySurface) volatilitySurfaceObject;
    if (!(volatilitySurface.getSurface() instanceof InterpolatedDoublesSurface)) {
      throw new OpenGammaRuntimeException("Expecting an InterpolatedDoublesSurface; got " + volatilitySurface.getSurface().getClass());
    }
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(target, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) fundingCurveObject;
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    final InstrumentDerivative swaption = definition.toDerivative(now, new String[] {fundingCurveName, forwardCurveName});
    final ValueProperties properties = getResultProperties(currency.getCode(), forwardCurveName, fundingCurveName, curveCalculationMethod, surfaceName, curveName);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
    final YieldCurveBundle curves = new YieldCurveBundle(new String[] {fundingCurveName, forwardCurveName}, new YieldAndDiscountCurve[] {fundingCurve, forwardCurve});
    final BlackFlatSwaptionParameters parameters = new BlackFlatSwaptionParameters(volatilitySurface.getSurface(),
        SwaptionUtils.getSwapGenerator(security, definition, securitySource));
    final YieldCurveWithBlackSwaptionBundle data = new YieldCurveWithBlackSwaptionBundle(parameters, curves);
    if (curveCalculationMethod.equals(InterpolatedDataProperties.CALCULATION_METHOD_NAME)) {
      final DoubleMatrix1D sensitivities = CALCULATOR.calculateFromSimpleInterpolatedCurve(swaption, data, NSC);
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, data, sensitivities, curveSpec, spec);
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    if (curveCalculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
      final Object couponSensitivityObject = inputs.getValue(getCouponSensitivityRequirement(target, forwardCurveName, fundingCurveName));
      if (couponSensitivityObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = (DoubleMatrix1D) couponSensitivityObject;
      sensitivities = CALCULATOR.calculateFromPresentValue(swaption, null, data, couponSensitivity, jacobian, NSC);
    } else {
      sensitivities = CALCULATOR.calculateFromParRate(swaption, null, data, jacobian, NSC);
    }
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, data, sensitivities, curveSpec, spec);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.SWAPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), getResultProperties(currency)));
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
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationMethods = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethods == null || curveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    final String forwardCurveName = forwardCurveNames.iterator().next();
    final String fundingCurveName = fundingCurveNames.iterator().next();
    final String curveName;
    if ((curves == null) || curves.isEmpty()) {
      // Curve not constrained, so make arbitrary choice.
      curveName = forwardCurveName;
    } else {
      if (curves.contains(forwardCurveName)) {
        curveName = forwardCurveName;
      } else if (curves.contains(fundingCurveName)) {
        curveName = fundingCurveName;
      } else {
        // Instrument isn't sensitive to the requested curve.
        return null;
      }
    }
    final String surfaceName = surfaceNames.iterator().next();
    final String curveCalculationMethod = curveCalculationMethods.iterator().next();
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(4);
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    requirements.add(getCurveRequirement(forwardCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency));
    requirements.add(getCurveRequirement(fundingCurveName, forwardCurveName, fundingCurveName, curveCalculationMethod, currency));
    requirements.add(getCurveSpecRequirement(target, curveName));
    if (!curveCalculationMethod.equals(InterpolatedDataProperties.CALCULATION_METHOD_NAME)) {
      requirements.add(getJacobianRequirement(target, forwardCurveName, fundingCurveName, curveCalculationMethod));
      if (curveCalculationMethod.equals(MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)) {
        requirements.add(getCouponSensitivityRequirement(target, forwardCurveName, fundingCurveName));
      }
    }
    requirements.add(getVolatilityRequirement(surfaceName, currency));
    return requirements;
  }

  private ValueProperties getResultProperties(final String currency) {
    return createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, FXOptionBlackFunctionDeprecated.BLACK_METHOD)
        .withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .withAny(ValuePropertyNames.CURVE).get();
  }

  private ValueProperties getResultProperties(final String currency, final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod, final String surfaceName,
      final String curveName) {
    return createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, FXOptionBlackFunctionDeprecated.BLACK_METHOD)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.CURVE_CURRENCY, currency)
        .with(ValuePropertyNames.CURVE, curveName).get();
  }

  private ValueRequirement getCurveRequirement(final String curveName, final String forwardCurveName, final String fundingCurveName, final String calculationMethod, final Currency currency) {
    final ValueProperties.Builder properties;
    if (calculationMethod == InterpolatedDataProperties.CALCULATION_METHOD_NAME) {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
          .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod);
    } else {
      properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
          .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod);
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties.get());
  }

  private ValueRequirement getVolatilityRequirement(final String surface, final Currency currency) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surface)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_ATM).get();
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
  }

  private static ValueRequirement getJacobianRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName, final String calculationMethod) {
    return YieldCurveFunction.getJacobianRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), forwardCurveName, fundingCurveName, calculationMethod);
  }

  private ValueRequirement getCouponSensitivityRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getCouponSensitivityRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), forwardCurveName, fundingCurveName);
  }

  private ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties.get());
  }
}
