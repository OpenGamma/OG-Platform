/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.GridInterpolator2DSensitivity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterestRateFutureOptionVegaFunction extends InterestRateFutureOptionFunction {
  private static final LinearInterpolator1D LINEAR = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final GridInterpolator2DSensitivity<Interpolator1DDataBundle, Interpolator1DDataBundle> NODE_SENSITIVITY_CALCULATOR =
      new GridInterpolator2DSensitivity<Interpolator1DDataBundle, Interpolator1DDataBundle>(LINEAR, LINEAR);

  public InterestRateFutureOptionVegaFunction(final String surfaceName) {
    super(surfaceName, ValueRequirementNames.VEGA_MATRIX);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Set<ComputedValue> getResults(final InterestRateDerivative irFutureOption, final SABRInterestRateDataBundle data, final ValueSpecification[] specifications,
      final Set<ValueRequirement> desiredValues, final String ccy, final FunctionInputs inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final ValueProperties sensitivityProperties = ValueProperties
        .with(ValuePropertyNames.CURRENCY, ccy)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    final Object alphaSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, sensitivityProperties));
    if (alphaSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get alpha sensitivity");
    }
    final Object nuSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, sensitivityProperties));
    if (nuSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get nu sensitivity");
    }
    final Object rhoSensitivityObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, sensitivityProperties));
    if (rhoSensitivityObject == null) {
      throw new OpenGammaRuntimeException("Could not get rho sensitivity");
    }
    final DoubleLabelledMatrix2D alphaSensitivity = (DoubleLabelledMatrix2D) alphaSensitivityObject;
    final DoubleLabelledMatrix2D nuSensitivity = (DoubleLabelledMatrix2D) nuSensitivityObject;
    final DoubleLabelledMatrix2D rhoSensitivity = (DoubleLabelledMatrix2D) rhoSensitivityObject;
    final double t = alphaSensitivity.getXKeys()[0];
    final double k = alphaSensitivity.getYKeys()[1];
    final DoubleMatrix1D modelParameterSensitivities = new DoubleMatrix1D(new double[] {alphaSensitivity.getValues()[0][0], nuSensitivity.getValues()[0][0], rhoSensitivity.getValues()[0][0]});
    final InterpolatedDoublesSurface alphaSurface = (InterpolatedDoublesSurface) data.getSABRParameter().getAlphaSurface().getSurface();
    final Map<Double, Interpolator1DDataBundle> alphaDataBundle = (Map<Double, Interpolator1DDataBundle>) alphaSurface.getInterpolatorData();
    final InterpolatedDoublesSurface nuSurface = (InterpolatedDoublesSurface) data.getSABRParameter().getNuSurface().getSurface();
    final Map<Double, Interpolator1DDataBundle> nuDataBundle = (Map<Double, Interpolator1DDataBundle>) nuSurface.getInterpolatorData();
    final InterpolatedDoublesSurface rhoSurface = (InterpolatedDoublesSurface) data.getSABRParameter().getRhoSurface().getSurface();
    final Map<Double, Interpolator1DDataBundle> rhoDataBundle = (Map<Double, Interpolator1DDataBundle>) rhoSurface.getInterpolatorData();
    final DoublesPair expiryStrike = DoublesPair.of(t, k);
    final Map<DoublesPair, Double> alphaGridNodeSensitivities = NODE_SENSITIVITY_CALCULATOR.calculate(alphaDataBundle, expiryStrike);
    final Map<DoublesPair, Double> nuGridNodeSensitivities = NODE_SENSITIVITY_CALCULATOR.calculate(nuDataBundle, expiryStrike);
    final Map<DoublesPair, Double> rhoGridNodeSensitivities = NODE_SENSITIVITY_CALCULATOR.calculate(rhoDataBundle, expiryStrike);
    final DoubleMatrix2D parameterInterpolationSensitivities; //
    final DoubleMatrix2D parameterDataSensitivities; //get from other function
    return null;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    final ValueProperties sensitivityProperties = ValueProperties
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, sensitivityProperties));

    return requirements;
  }
}
