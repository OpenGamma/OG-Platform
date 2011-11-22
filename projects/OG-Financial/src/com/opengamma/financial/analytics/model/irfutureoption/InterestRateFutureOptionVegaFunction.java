/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.SABRVegaCalculationUtils;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterestRateFutureOptionVegaFunction extends InterestRateFutureOptionFunction {
  private static final LinearInterpolator1D LINEAR = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final GridInterpolator2D NODE_SENSITIVITY_CALCULATOR = new GridInterpolator2D(LINEAR, LINEAR);
  private VolatilitySurfaceDefinition<?, ?> _definition;

  public InterestRateFutureOptionVegaFunction(String forwardCurveName, String fundingCurveName, final String surfaceName) {
    super(forwardCurveName, fundingCurveName, surfaceName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceDefinitionSource volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    _definition = volSurfaceDefinitionSource.getDefinition(getSurfaceName(), "IR_FUTURE_OPTION");
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find volatility surface definition for IR future option surface called " + getSurfaceName());
    }  
  }
  
  @SuppressWarnings({"unchecked" })
  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative irFutureOption, final SABRInterestRateDataBundle data, 
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ComputationTarget target) {
    final ValueProperties sensitivityProperties = getModelSensitivityProperties(FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode());
    final Object alphaSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getTrade(), sensitivityProperties));
    if (alphaSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get alpha sensitivity");
    }
    final Object nuSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getTrade(), sensitivityProperties));
    if (nuSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get nu sensitivity");
    }
    final Object rhoSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getTrade(), sensitivityProperties));
    if (rhoSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get rho sensitivity");
    }
    final ValueRequirement surfacesRequirement = getSurfaceRequirement(target);
    final Object sabrSurfacesObject = inputs.getValue(surfacesRequirement);
    if (sabrSurfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get SABR fitted surfaces");
    }
    final SABRFittedSurfaces sabrFittedSurfaces = (SABRFittedSurfaces) sabrSurfacesObject;
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = sabrFittedSurfaces.getInverseJacobians();
    final DoubleLabelledMatrix2D alphaSensitivity = (DoubleLabelledMatrix2D) alphaSensitivityObject;
    final DoubleLabelledMatrix2D nuSensitivity = (DoubleLabelledMatrix2D) nuSensitivityObject;
    final DoubleLabelledMatrix2D rhoSensitivity = (DoubleLabelledMatrix2D) rhoSensitivityObject;
    final double expiry = alphaSensitivity.getXKeys()[0];
    final double maturity = alphaSensitivity.getYKeys()[0];
    final double alpha = alphaSensitivity.getValues()[0][0];
    final double nu = nuSensitivity.getValues()[0][0];
    final double rho = rhoSensitivity.getValues()[0][0];
    final InterpolatedDoublesSurface alphaSurface = (InterpolatedDoublesSurface) data.getSABRParameter().getAlphaSurface().getSurface();
    final Map<Double, Interpolator1DDataBundle> alphaDataBundle = (Map<Double, Interpolator1DDataBundle>) alphaSurface.getInterpolatorData();
    final InterpolatedDoublesSurface nuSurface = (InterpolatedDoublesSurface) data.getSABRParameter().getNuSurface().getSurface();
    final Map<Double, Interpolator1DDataBundle> nuDataBundle = (Map<Double, Interpolator1DDataBundle>) nuSurface.getInterpolatorData();
    final InterpolatedDoublesSurface rhoSurface = (InterpolatedDoublesSurface) data.getSABRParameter().getRhoSurface().getSurface();
    final Map<Double, Interpolator1DDataBundle> rhoDataBundle = (Map<Double, Interpolator1DDataBundle>) rhoSurface.getInterpolatorData();
    final DoublesPair expiryMaturity = DoublesPair.of(expiry, maturity);
    final double[] expiryValues = alphaSurface.getXDataAsPrimitive();
    final DoubleMatrix2D result = SABRVegaCalculationUtils.getVegaSurface(alpha, rho, nu, alphaDataBundle, rhoDataBundle, nuDataBundle, inverseJacobians, expiryMaturity, NODE_SENSITIVITY_CALCULATOR);
    return Collections.singleton(new ComputedValue(getResultSpec(target), VegaMatrixHelper.getVegaIRFutureOptionQuoteMatrixInStandardForm(_definition, result, expiryValues)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target));
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>(super.getRequirements(context, target, desiredValue));
    final String ccyCode = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    final ValueProperties sensitivityProperties = getModelSensitivityProperties(ccyCode);
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getTrade(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getTrade(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getTrade(), sensitivityProperties));
    return requirements;
  }
  
  private ValueSpecification getResultSpec(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(), 
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
            .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName())
            .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
            .with(ValuePropertyNames.SURFACE, getSurfaceName()).get());
  }
  
  private ValueProperties getModelSensitivityProperties(String ccyCode) {
    return ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, ccyCode)
      .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName())
      .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
      .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();    
  }
}
