/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.PDELocalVolatilityCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class LocalVolatilityPDEFunction extends AbstractFunction.NonCompiledInvoker {
  /** The name of this local volatility calculation method */
  public static final String CALCULATION_METHOD = "LocalVolatilityPDE";
  private final String _blackSmileInterpolatorName;

  public LocalVolatilityPDEFunction(final String blackSmileInterpolatorName) {
    ArgumentChecker.notNull(blackSmileInterpolatorName, "Black smile interpolator name");
    _blackSmileInterpolatorName = blackSmileInterpolatorName;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(getRequirementName(), target.toSpecification(), properties));
  }

  protected abstract String getRequirementName();

  protected abstract ComputationTargetReference getVolatilitySurfaceAndForwardCurveTarget(final ComputationTarget target);

  protected abstract ComputationTargetReference getDiscountingCurveTarget(final ComputationTarget target);

  protected abstract String getInstrumentType();

  protected abstract EuropeanVanillaOption getOption(final FinancialSecurity security, final ZonedDateTime date);

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final ValueRequirement desiredValue);

  protected ValueRequirement getVolatilitySurfaceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(ValueProperties.builder().get(), getInstrumentType(), _blackSmileInterpolatorName,
        LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS, desiredValue).get();
    return new ValueRequirement(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, getVolatilitySurfaceAndForwardCurveTarget(target), properties);
  }

  protected ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String calculationMethod = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final ValueProperties properties = ValueProperties.builder()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, calculationMethod)
        .with(CURVE, forwardCurveName).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, getVolatilitySurfaceAndForwardCurveTarget(target), properties);
  }

  protected ValueRequirement getDiscountingCurveRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, desiredValue.getConstraint(PROPERTY_DISCOUNTING_CURVE_NAME))
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG)).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, getDiscountingCurveTarget(target), properties);
  }

  protected Object getResult(final PDELocalVolatilityCalculator<?> calculator, final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve,
      final EuropeanVanillaOption option, final YieldAndDiscountCurve discountingCurve) {
    return calculator.getResult(localVolatility, forwardCurve, option, discountingCurve);
  }

  protected String getBlackSmileInterpolatorName() {
    return _blackSmileInterpolatorName;
  }
}
