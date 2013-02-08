/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_CENTRE_MONEYNESS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_MAX_PROXY_DELTA;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_NUMBER_SPACE_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_NUMBER_TIME_STEPS;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_SPACE_DIRECTION_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_SPACE_STEPS_BUNCHING;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_THETA;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_TIME_STEP_BUNCHING;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilityForwardPDECalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.PDELocalVolatilityCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.security.FinancialSecurity;

/**
 *
 */
public abstract class LocalVolatilityForwardPDEFunction extends LocalVolatilityPDEFunction {

  public LocalVolatilityForwardPDEFunction(final String blackSmileInterpolatorName) {
    super(blackSmileInterpolatorName);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final double theta = Double.parseDouble(desiredValue.getConstraint(PROPERTY_THETA));
    final int nTimeSteps = Integer.parseInt(desiredValue.getConstraint(PROPERTY_NUMBER_TIME_STEPS));
    final int nSpaceSteps = Integer.parseInt(desiredValue.getConstraint(PROPERTY_NUMBER_SPACE_STEPS));
    final double timeStepBunching = Double.parseDouble(desiredValue.getConstraint(PROPERTY_TIME_STEP_BUNCHING));
    final double spaceStepBunching = Double.parseDouble(desiredValue.getConstraint(PROPERTY_SPACE_STEPS_BUNCHING));
    final double maxProxyDelta = Double.parseDouble(desiredValue.getConstraint(PROPERTY_MAX_PROXY_DELTA));
    final double centreMoneyness = Double.parseDouble(desiredValue.getConstraint(PROPERTY_CENTRE_MONEYNESS));
    final String interpolatorName = desiredValue.getConstraint(PROPERTY_SPACE_DIRECTION_INTERPOLATOR);
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    final PDELocalVolatilityCalculator<?> pdeCalculator =
        getPDECalculator(new LocalVolatilityForwardPDECalculator(theta, nTimeSteps, nSpaceSteps, timeStepBunching, spaceStepBunching, maxProxyDelta, centreMoneyness), interpolator);
    final Object localVolatilityObject = inputs.getValue(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE);
    if (localVolatilityObject == null) {
      throw new OpenGammaRuntimeException("Could not get local volatility surface");
    }
    final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final Object discountingCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (discountingCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get discounting curve");
    }
    final LocalVolatilitySurfaceMoneyness localVolatility = (LocalVolatilitySurfaceMoneyness) localVolatilityObject;
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    final EuropeanVanillaOption option = getOption(security, now);
    final YieldAndDiscountCurve discountingCurve = (YieldAndDiscountCurve) discountingCurveObject;
    final Object result = getResult(pdeCalculator, localVolatility, forwardCurve, option, discountingCurve);
    final ValueProperties properties = getResultProperties(desiredValue);
    final ValueSpecification spec = new ValueSpecification(getRequirementName(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties constraints = desiredValue.getConstraints();
    final Set<ValueRequirement> pdeRequirements = PDEFunctionUtils.ensureForwardPDEFunctionProperties(constraints);
    if (pdeRequirements == null) {
      return null;
    }
    final Set<ValueRequirement> localVolSurfaceRequirements = LocalVolatilitySurfaceUtils.ensureDupireLocalVolatilitySurfaceProperties(constraints);
    if (localVolSurfaceRequirements == null) {
      return null;
    }
    if (OpenGammaCompilationContext.isPermissive(context)) {
      ValueProperties.Builder constraintsBuilder = null;
      Set<String> values = constraints.getValues(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
      if (values == null) {
        constraintsBuilder = constraints.copy();
        constraintsBuilder.with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, getBlackSmileInterpolatorName());
      } else if (values.size() != 1) {
        constraintsBuilder = constraints.copy();
        constraintsBuilder
          .withoutAny(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR)
          .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, getBlackSmileInterpolatorName());
      }
      values = constraints.getValues(PDEPropertyNamesAndValues.PROPERTY_PDE_DIRECTION);
      if (values == null) {
        if (constraintsBuilder == null) {
          constraintsBuilder = constraints.copy();
        }
        constraintsBuilder.with(PDEPropertyNamesAndValues.PROPERTY_PDE_DIRECTION, PDEPropertyNamesAndValues.FORWARDS);
      } else if (values.size() != 1) {
        if (constraintsBuilder == null) {
          constraintsBuilder = constraints.copy();
        }
        constraintsBuilder.withoutAny(PDEPropertyNamesAndValues.PROPERTY_PDE_DIRECTION).with(PDEPropertyNamesAndValues.PROPERTY_PDE_DIRECTION, PDEPropertyNamesAndValues.FORWARDS);
      }
      if (constraintsBuilder != null) {
        constraints = constraintsBuilder.get();
      }
    }
    final Set<ValueRequirement> requirements = PDEFunctionUtils.ensureForwardPDEFunctionProperties(constraints);
    if (requirements == null) {
      return null;
    }
    final ValueRequirement volatilitySurfaceRequirement = getVolatilitySurfaceRequirement(target, desiredValue);
    final ValueRequirement forwardCurveRequirement = getForwardCurveRequirement(target, desiredValue);
    final ValueRequirement discountingCurveRequirement = getDiscountingCurveRequirement(target, desiredValue);
    return Sets.newHashSet(volatilitySurfaceRequirement, forwardCurveRequirement, discountingCurveRequirement);
  }

  protected abstract PDELocalVolatilityCalculator<?> getPDECalculator(final LocalVolatilityForwardPDECalculator calculator, final Interpolator1D interpolator);

  @Override
  protected ValueProperties getResultProperties() {
    ValueProperties result = createValueProperties().get();
    result = LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(result, getInstrumentType(), getBlackSmileInterpolatorName(),
        LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS).get();
    result = PDEFunctionUtils.addForwardPDEProperties(result)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEFunction.CALCULATION_METHOD).get();
    return result;
  }

  @Override
  protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    ValueProperties result = createValueProperties().get();
    result = LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(result, getInstrumentType(), getBlackSmileInterpolatorName(),
        LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS, desiredValue).get();
    result = PDEFunctionUtils.addForwardPDEProperties(result, desiredValue)
        .with(ValuePropertyNames.CALCULATION_METHOD, LocalVolatilityPDEFunction.CALCULATION_METHOD).get();
    return result;
  }

}
