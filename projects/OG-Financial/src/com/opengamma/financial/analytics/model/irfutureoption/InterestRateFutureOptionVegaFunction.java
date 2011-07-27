/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.GridInterpolator2DSensitivity;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.MatrixAlgebraFactory;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 */
public class InterestRateFutureOptionVegaFunction extends InterestRateFutureOptionFunction {
  private static final LinearInterpolator1D LINEAR = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final GridInterpolator2DSensitivity<Interpolator1DDataBundle, Interpolator1DDataBundle> NODE_SENSITIVITY_CALCULATOR =
      new GridInterpolator2DSensitivity<Interpolator1DDataBundle, Interpolator1DDataBundle>(LINEAR, LINEAR);
  private static final DoublesPairComparator COMPARATOR = new DoublesPairComparator();
  private static final MatrixAlgebra ALGEBRA = MatrixAlgebraFactory.OG_ALGEBRA;

  public InterestRateFutureOptionVegaFunction(final String surfaceName) {
    super(surfaceName, ValueRequirementNames.VEGA_MATRIX);
  }

  @SuppressWarnings({"unchecked" })
  @Override
  protected Set<ComputedValue> getResults(final InterestRateDerivative irFutureOption, final SABRInterestRateDataBundle data, final ValueSpecification[] specifications,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ComputationTarget target) {
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final String ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    final ValueProperties sensitivityProperties = ValueProperties
        .with(ValuePropertyNames.CURRENCY, ccy)
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveNames.getFirst())
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveNames.getSecond())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
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
    final SABRFittedSurfaces sabrFittedSurfaces = (SABRFittedSurfaces) sabrSurfacesObject;
    final Map<Double, List<Pair<Double, DoubleMatrix2D>>> inverseJacobians = getMaturityExpiryValueMap(sabrFittedSurfaces.getInverseJacobians());
    if (inverseJacobians.size() != 1) {
      throw new OpenGammaRuntimeException("Cannot handle volatility cubes");
    }
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
    final Map<Double, List<Pair<Double, Double>>> alphaGridNodeSensitivities = getMaturityExpiryValueMap(NODE_SENSITIVITY_CALCULATOR.calculate(alphaDataBundle, expiryMaturity));
    final Map<Double, List<Pair<Double, Double>>> nuGridNodeSensitivities = getMaturityExpiryValueMap(NODE_SENSITIVITY_CALCULATOR.calculate(nuDataBundle, expiryMaturity));
    final Map<Double, List<Pair<Double, Double>>> rhoGridNodeSensitivities = getMaturityExpiryValueMap(NODE_SENSITIVITY_CALCULATOR.calculate(rhoDataBundle, expiryMaturity));
    if (alphaGridNodeSensitivities.size() != 1) {
      throw new OpenGammaRuntimeException("Cannot handle volatility cubes");
    }
    final double[] expiryValues = alphaSurface.getXDataAsPrimitive();
    final DoubleMatrix2D alphaResult = getVegaSurfaceForParameter(alpha, alphaGridNodeSensitivities, inverseJacobians, 0, expiryValues);
    final DoubleMatrix2D nuResult = getVegaSurfaceForParameter(nu, nuGridNodeSensitivities, inverseJacobians, 1, expiryValues);
    final DoubleMatrix2D rhoResult = getVegaSurfaceForParameter(rho, rhoGridNodeSensitivities, inverseJacobians, 2, expiryValues);
    final DoubleMatrix2D result = (DoubleMatrix2D) ALGEBRA.add(alphaResult, ALGEBRA.add(nuResult, rhoResult));
    final DoubleLabelledMatrix2D formatted = getTempFormatting(result, expiryValues);
    return Collections.singleton(new ComputedValue(specifications[0], formatted));
  }

  private DoubleMatrix2D getVegaSurfaceForParameter(final double parameter,
      final Map<Double, List<Pair<Double, Double>>> gridNodeSensitivities,
      final Map<Double, List<Pair<Double, DoubleMatrix2D>>> inverseJacobians,
      final int parameterNumber,
      final double[] expiryValues) {
    //only works if this is a surface
    final List<Pair<Double, Double>> gns = gridNodeSensitivities.values().iterator().next();
    final List<Pair<Double, DoubleMatrix2D>> invJac = inverseJacobians.values().iterator().next();
    final int rows = gns.size();
    final double[][] result = new double[rows][];
    for (int i = 0; i < rows; i++) {
      final Pair<Double, Double> expirySensitivity = gns.get(i);
      final double expiry = expirySensitivity.getFirst();
      final double sensitivity = expirySensitivity.getSecond();
      final Pair<Double, DoubleMatrix2D> expiryMatrix = invJac.get(i);
      if (Double.doubleToLongBits(expiry) != Double.doubleToLongBits(expiryMatrix.getFirst())) {
        throw new OpenGammaRuntimeException("Should never happen");
      }
      final DoubleMatrix2D m = expiryMatrix.getSecond();
      result[i] = new double[m.getNumberOfColumns()];
      for (int j = 0; j < m.getNumberOfColumns(); j++) {
        result[i][j] = m.getEntry(parameterNumber, j) * sensitivity * parameter;
      }
    }
    return new DoubleMatrix2D(result);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>(super.getRequirements(context, target, desiredValue));
    final String forwardCurveName = YieldCurveFunction.getForwardCurveName(context, desiredValue);
    final String fundingCurveName = YieldCurveFunction.getFundingCurveName(context, desiredValue);
    final ValueProperties sensitivityProperties = ValueProperties
              .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
              .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
              .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
              .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, target.getTrade(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY, target.getTrade(), sensitivityProperties));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY, target.getTrade(), sensitivityProperties));
    return requirements;
  }

  private <T> Map<Double, List<Pair<Double, T>>> getMaturityExpiryValueMap(final Map<DoublesPair, T> data) {
    final TreeMap<DoublesPair, T> sorted = new TreeMap<DoublesPair, T>(COMPARATOR);
    sorted.putAll(data);
    final Map<Double, List<Pair<Double, T>>> result = new TreeMap<Double, List<Pair<Double, T>>>();
    for (final Map.Entry<DoublesPair, T> entry : sorted.entrySet()) {
      final double maturity = entry.getKey().second;
      if (!result.containsKey(maturity)) {
        final List<Pair<Double, T>> expiryValue = new ArrayList<Pair<Double, T>>();
        expiryValue.add(Pair.of(entry.getKey().first, entry.getValue()));
        result.put(maturity, expiryValue);
      } else {
        final List<Pair<Double, T>> expiryValue = result.get(maturity);
        expiryValue.add(Pair.of(entry.getKey().first, entry.getValue()));
      }
    }
    return result;
  }

  private static final class DoublesPairComparator implements Comparator<DoublesPair> {

    @Override
    public int compare(final DoublesPair p1, final DoublesPair p2) {
      if (Double.compare(p1.second, p2.second) == 0) {
        return Double.compare(p1.first, p2.first);
      }
      return Double.compare(p1.second, p2.second);
    }
  }

  private DoubleLabelledMatrix2D getTempFormatting(final DoubleMatrix2D matrix, final double[] expiryValues) {
    final DoubleMatrix2D transpose = DoubleMatrixUtils.getTranspose(matrix);
    final int columns = transpose.getNumberOfColumns();
    if (columns != expiryValues.length) {
      throw new OpenGammaRuntimeException("Should never happen");
    }
    final double firstTime = expiryValues[0];
    final int rows = transpose.getNumberOfRows();
    final Double[] rowValues = new Double[rows];
    final Double[] columnValues = new Double[columns];
    final Object[] rowLabels = new Object[rows];
    final Object[] columnLabels = new Object[columns];
    final double[][] values = new double[rows][columns];
    for (int i = 0; i < rows; i++) {
      rowValues[i] = Double.valueOf(i);
      rowLabels[i] = "k" + (i + 1);
      for (int j = 0; j < columns; j++) {
        if (i == 0) {
          columnValues[j] = expiryValues[j];
          columnLabels[j] = Integer.toString((int) (1 + 4 * (expiryValues[j] - firstTime)));
        }
        values[i][j] = transpose.getEntry(i, j);
      }
    }
    return new DoubleLabelledMatrix2D(columnValues, columnLabels, rowValues, rowLabels, values);
  }
}
