/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix3D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.SABRVegaCalculationUtils;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;

/**
 * 
 */
public class SwaptionSABRVegaFunction extends SwaptionSABRFunction {
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator("Linear", "FlatExtrapolator", "FlatExtrapolator");
  private static final GridInterpolator2D NODE_SENSITIVITY_CALCULATOR = new GridInterpolator2D(INTERPOLATOR, INTERPOLATOR);
  private VolatilityCubeDefinition _definition;

  public SwaptionSABRVegaFunction(final Currency currency, final String cubeName, final boolean useSABRExtrapolation, String forwardCurveName, String fundingCurveName) {
    super(currency, cubeName, useSABRExtrapolation, forwardCurveName, fundingCurveName);
  }

  public SwaptionSABRVegaFunction(final String currency, final String cubeName, final String useSABRExtrapolation, String forwardCurveName, String fundingCurveName) {
    super(currency, cubeName, useSABRExtrapolation, forwardCurveName, fundingCurveName);
  }

  @Override 
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    _definition = getHelper().init(context, this);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility cube definition for swaption cube called " + getHelper().getDefinitionName());
    }
  }
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final SABRInterestRateDataBundle data = new SABRInterestRateDataBundle(getModelParameters(target, inputs), getYieldCurves(target, inputs));
    final ValueProperties sensitivityProperties = getModelSensitivityProperties(FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    final Object alphaSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    if (alphaSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get alpha sensitivity");
    }
    final Object nuSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    if (nuSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get nu sensitivity");
    }
    final Object rhoSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    if (rhoSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get rho sensitivity");
    }
    final ValueRequirement cubeRequirement = getCubeRequirement(target);
    final Object sabrSurfacesObject = inputs.getValue(cubeRequirement);
    if (sabrSurfacesObject == null) {
      throw new OpenGammaRuntimeException("Could not get SABR fitted surfaces");
    }
    final SABRFittedSurfaces sabrFittedSurfaces = (SABRFittedSurfaces) sabrSurfacesObject;
    final ValueRequirement externalIdsRequirement = new ValueRequirement(ValueRequirementNames.EXTERNAL_IDS, FinancialSecurityUtils.getCurrency(target.getSecurity()), getExternalIdProperties());
    final Object externalIdsObject = inputs.getValue(externalIdsRequirement);
    if (externalIdsObject == null) {
      throw new OpenGammaRuntimeException("Could not get external ids for cube");
    }
    final SortedMap<DoublesPair, ExternalId[]> fittedDataIds = new TreeMap<DoublesPair, ExternalId[]>(new FirstThenSecondPairComparator<Double, Double>()); 
    fittedDataIds.putAll((Map<DoublesPair, ExternalId[]>) externalIdsObject);
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
    @SuppressWarnings("unchecked")
    final Map<Double, Interpolator1DDataBundle> alphaDataBundle = (Map<Double, Interpolator1DDataBundle>) alphaSurface.getInterpolatorData();
    final InterpolatedDoublesSurface nuSurface = (InterpolatedDoublesSurface) data.getSABRParameter().getNuSurface().getSurface();
    @SuppressWarnings("unchecked")
    final Map<Double, Interpolator1DDataBundle> nuDataBundle = (Map<Double, Interpolator1DDataBundle>) nuSurface.getInterpolatorData();
    final InterpolatedDoublesSurface rhoSurface = (InterpolatedDoublesSurface) data.getSABRParameter().getRhoSurface().getSurface();
    @SuppressWarnings("unchecked")
    final Map<Double, Interpolator1DDataBundle> rhoDataBundle = (Map<Double, Interpolator1DDataBundle>) rhoSurface.getInterpolatorData();
    final DoublesPair expiryMaturity = DoublesPair.of(expiry, maturity);
    
    final Map<Double, DoubleMatrix2D> result = 
      SABRVegaCalculationUtils.getVegaCube(alpha, rho, nu, alphaDataBundle, rhoDataBundle, nuDataBundle, inverseJacobians, expiryMaturity, NODE_SENSITIVITY_CALCULATOR);
    final DoubleLabelledMatrix3D labelledMatrix = VegaMatrixHelper.getVegaSwaptionCubeQuoteMatrixInStandardForm(fittedDataIds, result);
    return Collections.singleton(new ComputedValue(getResultSpec(target), labelledMatrix));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>(super.getRequirements(context, target, desiredValue));
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final String ccyCode = ccy.getCode();
    final ValueProperties sensitivityProperties = getModelSensitivityProperties(ccyCode);
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getSecurity(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.EXTERNAL_IDS, ccy, getExternalIdProperties()));
    return requirements;
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getResultSpec(target));
  }

  private ValueProperties getModelSensitivityProperties(String ccyCode) {
    return ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, ccyCode)
      .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName())
      .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
      .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get();    
  }

  private ValueProperties getExternalIdProperties() {
    return ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, getHelper().getCurrency().getCode())
        .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get();
  }


  private ValueSpecification getResultSpec(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.VEGA_QUOTE_MATRIX, target.toSpecification(),
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
            .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName())
            .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
            .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get());
  }
}
