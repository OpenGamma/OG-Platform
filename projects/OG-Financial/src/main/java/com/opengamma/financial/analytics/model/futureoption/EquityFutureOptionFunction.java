/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 *
 */
public abstract class EquityFutureOptionFunction extends FutureOptionFunction {

  /**
   * @param valueRequirementNames The value requirement names
   */
  public EquityFutureOptionFunction(final String... valueRequirementNames) {
    super(valueRequirementNames);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> calculationMethod = constraints.getValues(ValuePropertyNames.CALCULATION_METHOD);
    if (calculationMethod == null || calculationMethod.isEmpty()) {
      return null;
    }
    if (calculationMethod != null && calculationMethod.size() == 1) {
      if (!getCalculationMethod().equals(Iterables.getOnlyElement(calculationMethod))) {
        return null;
      }
    }
    final EquityIndexFutureOptionSecurity security = (EquityIndexFutureOptionSecurity) target.getSecurity();
    final Set<String> discountingCurveNames = constraints.getValues(EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_NAME);
    if (discountingCurveNames == null || discountingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> discountingCurveConfigs = constraints.getValues(EquityOptionFunction.PROPERTY_DISCOUNTING_CURVE_CONFIG);
    if (discountingCurveConfigs == null || discountingCurveConfigs.size() != 1) {
      return null;
    }
    final String discountingCurveName = Iterables.getOnlyElement(discountingCurveNames);
    final String discountingCurveConfig = Iterables.getOnlyElement(discountingCurveConfigs);
    final ValueRequirement discountingReq = getDiscountCurveRequirement(discountingCurveName, discountingCurveConfig, security);
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String volSurfaceName = Iterables.getOnlyElement(surfaceNames);
    final Set<String> surfaceCalculationMethods = constraints.getValues(ValuePropertyNames.SURFACE_CALCULATION_METHOD);
    if (surfaceCalculationMethods == null || surfaceCalculationMethods.size() != 1) {
      return null;
    }
    final String surfaceCalculationMethod = Iterables.getOnlyElement(surfaceCalculationMethods);
    final Set<String> forwardCurveNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_NAME);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveCalculationMethods = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethods == null || forwardCurveCalculationMethods.size() != 1) {
      return null;
    }
    final ExternalIdBundle underlyingFutureId = ExternalIdBundle.of(security.getUnderlyingId());
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    
    final ExternalId underlyingIndexId;
    Security underlyingFuture = securitySource.getSingle(underlyingFutureId);
    if (underlyingFuture == null) {
      throw new OpenGammaRuntimeException("The underlying (" + underlyingFutureId.toString() + ") of EquityIndexFutureOption (" + security.getName() + 
          ") was not found in security source. Please try to reload.");
    } else if (underlyingFuture instanceof EquityFutureSecurity) {
      underlyingIndexId = ((EquityFutureSecurity) underlyingFuture).getUnderlyingId();
    } else if (underlyingFuture instanceof IndexFutureSecurity) {
      underlyingIndexId = ((IndexFutureSecurity) underlyingFuture).getUnderlyingId();
    } else {
      throw new OpenGammaRuntimeException("The Security type of the future underlying the Index Future Option must be added to this function: " 
              + underlyingFuture.getClass());
    }
    
    final String forwardCurveCalculationMethod = Iterables.getOnlyElement(forwardCurveCalculationMethods);
    final String forwardCurveName = Iterables.getOnlyElement(forwardCurveNames);
    final ValueRequirement volReq = getVolatilitySurfaceRequirement(desiredValue, volSurfaceName, forwardCurveName, surfaceCalculationMethod, underlyingIndexId);
    final ValueRequirement forwardCurveReq = getForwardCurveRequirement(forwardCurveName, forwardCurveCalculationMethod, underlyingIndexId);
    return Sets.newHashSet(discountingReq, forwardCurveReq, volReq);
  }

  @Override
  protected ValueRequirement getVolatilitySurfaceRequirement(final ValueRequirement desiredValue, final FinancialSecurity security, final String surfaceName,
      final String forwardCurveName, final String surfaceCalculationMethod) {
    throw new UnsupportedOperationException();
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final ValueRequirement desiredValue, final String surfaceName, final String forwardCurveName,
      final String surfaceCalculationMethod, final ExternalId underlyingBuid) {
    // REVIEW Andrew 2012-01-17 -- Could we pass a CTRef to the getSurfaceRequirement and use the underlyingBuid external identifier directly with a target type of SECURITY
    // TODO Casey - Replace desiredValue with smileInterpolatorName in BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement
    return BlackVolatilitySurfacePropertyUtils.getSurfaceRequirement(desiredValue, ValueProperties.none(), surfaceName, forwardCurveName,
        InstrumentTypeProperties.EQUITY_FUTURE_OPTION, ComputationTargetType.PRIMITIVE, underlyingBuid);
  }


  @Override
  protected ValueRequirement getForwardCurveRequirement(final FinancialSecurity security, final String forwardCurveName, final String forwardCurveCalculationMethod) {
    throw new UnsupportedOperationException();
  }

  private ValueRequirement getForwardCurveRequirement(final String forwardCurveName, final String forwardCurveCalculationMethod, final ExternalId underlyingIndexId) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, forwardCurveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, forwardCurveCalculationMethod)
        .get();
    // REVIEW Andrew 2012-01-17 -- Why can't we just use the underlyingBuid external identifier directly here, with a target type of SECURITY, and shift the logic into the reference resolver?
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.PRIMITIVE, underlyingIndexId, properties);
  }

  @Override
  protected String getSurfaceName(final FinancialSecurity security, final String surfaceName) {
    return surfaceName;
  }
}
