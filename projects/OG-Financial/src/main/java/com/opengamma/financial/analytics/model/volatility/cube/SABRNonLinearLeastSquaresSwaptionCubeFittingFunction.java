/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_DEFINITION;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.SABR_SURFACES;
import static com.opengamma.engine.value.ValueRequirementNames.STANDARD_VOLATILITY_CUBE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.SURFACE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.marketdatasnapshot.SurfaceData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.cube.fitted.FittedSmileDataPoints;
import com.opengamma.financial.analytics.volatility.VolatilityQuoteUnits;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class SABRNonLinearLeastSquaresSwaptionCubeFittingFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SABRNonLinearLeastSquaresSwaptionCubeFittingFunction.class);
  /** The error on volatility quotes */
  private static final double ERROR = 0.0001;
  /** The fixed parameters, where 1 means fixed. The order is (alpha, beta, nu, rho) */
  private static final BitSet FIXED = new BitSet();
  /** The SABR function */
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  /** The starting point */
  private static final DoubleMatrix1D SABR_INITIAL_VALUES = new DoubleMatrix1D(new double[] {0.05, 0.5, 0.7, 0.3 });
  /** The parameter surface x and y interpolator */
  private static final LinearInterpolator1D LINEAR = (LinearInterpolator1D) Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR);
  /** The parameter surface x and y extrapolator */
  private static final FlatExtrapolator1D FLAT = new FlatExtrapolator1D();
  /** The surface interpolator */
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(LINEAR, LINEAR, FLAT, FLAT);

  static {
    FIXED.set(1);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final ValueProperties properties = desiredValues.iterator().next().getConstraints().copy().get();
    final VolatilityCubeData<Tenor, Tenor, Double> volatilityCubeData = (VolatilityCubeData<Tenor, Tenor, Double>) inputs.getValue(STANDARD_VOLATILITY_CUBE_DATA);
    final SurfaceData<Tenor, Tenor> forwardSwapSurface = (SurfaceData<Tenor, Tenor>) inputs.getValue(SURFACE_DATA);
    final DoubleArrayList swapMaturitiesList = new DoubleArrayList();
    final DoubleArrayList swaptionExpiriesList = new DoubleArrayList();
    final DoubleArrayList alphaList = new DoubleArrayList();
    final DoubleArrayList betaList = new DoubleArrayList();
    final DoubleArrayList nuList = new DoubleArrayList();
    final DoubleArrayList rhoList = new DoubleArrayList();
    final DoubleArrayList chiSqList = new DoubleArrayList();
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<>();
    final Map<Pair<Tenor, Tenor>, Double[]> fittedRelativeStrikes = new HashMap<>();
    for (final Tenor expiry : volatilityCubeData.getUniqueXValues()) {
      final double swaptionExpiry = getTime(expiry);
      for (final Tenor maturity : volatilityCubeData.getUniqueYValues()) {
        final double swapMaturity = getTime(maturity);
        final double forward = forwardSwapSurface.getValue(expiry, maturity);
        if (volatilityCubeData.asMap().containsKey(Triple.of(expiry, maturity, forward))) {
          final List<ObjectsPair<Double, Double>> strikeVol = volatilityCubeData.getZValuesForXandY(expiry, maturity);
          final int nVols = strikeVol.size();
          if (nVols < 4) {
            s_logger.info("Smile had less than 4 points for expiry = {} and maturity = {}", expiry, maturity);
            continue;
          }
          final double[] strikes = new double[nVols];
          final Double[] strikeCopy = new Double[nVols]; //TODO
          final double[] blackVols = new double[nVols];
          final double[] errors = new double[nVols];
          int i = 0;
          for (final ObjectsPair<Double, Double> sv : strikeVol) {
            strikes[i] = sv.getFirst();
            strikeCopy[i] = sv.getFirst();
            blackVols[i] = sv.getSecond();
            errors[i++] = ERROR;
          }
          final LeastSquareResultsWithTransform fittedResult = new SABRModelFitter(forward, strikes, swaptionExpiry, blackVols, errors, SABR_FUNCTION).solve(SABR_INITIAL_VALUES, FIXED);
          final DoubleMatrix1D parameters = fittedResult.getModelParameters();
          swapMaturitiesList.add(swapMaturity);
          swaptionExpiriesList.add(swaptionExpiry);
          alphaList.add(parameters.getEntry(0));
          betaList.add(parameters.getEntry(1));
          rhoList.add(parameters.getEntry(2));
          nuList.add(parameters.getEntry(3));
          final DoublesPair expiryMaturityPair = DoublesPair.of(swaptionExpiry, swapMaturity);
          inverseJacobians.put(expiryMaturityPair, fittedResult.getModelParameterSensitivityToData());
          chiSqList.add(fittedResult.getChiSq());
          fittedRelativeStrikes.put(Pairs.of(expiry, maturity), strikeCopy);
        }
      }
    }
    if (swapMaturitiesList.size() < 5) { //don't have sufficient fits to construct a surface
      throw new OpenGammaRuntimeException("Could not construct SABR parameter surfaces; have under 5 surface points");
    }
    final double[] swapMaturities = swapMaturitiesList.toDoubleArray();
    final double[] swaptionExpiries = swaptionExpiriesList.toDoubleArray();
    final double[] alpha = alphaList.toDoubleArray();
    final double[] beta = betaList.toDoubleArray();
    final double[] nu = nuList.toDoubleArray();
    final double[] rho = rhoList.toDoubleArray();
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, alpha, INTERPOLATOR, "SABR alpha surface");
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, beta, INTERPOLATOR, "SABR beta surface");
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, nu, INTERPOLATOR, "SABR nu surface");
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, rho, INTERPOLATOR, "SABR rho surface");
    final SABRFittedSurfaces fittedSurfaces = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, inverseJacobians);
    final ValueSpecification sabrSurfacesSpecification = new ValueSpecification(SABR_SURFACES, target.toSpecification(), properties);
    final ValueSpecification smileIdsSpecification = new ValueSpecification(VOLATILITY_CUBE_FITTED_POINTS, target.toSpecification(), properties);
    return Sets.newHashSet(new ComputedValue(sabrSurfacesSpecification, fittedSurfaces), new ComputedValue(smileIdsSpecification, new FittedSmileDataPoints(fittedRelativeStrikes)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    final ValueSpecification sabrSurfacesSpecification = new ValueSpecification(SABR_SURFACES, target.toSpecification(), properties);
    final ValueSpecification smileIdsSpecification = new ValueSpecification(VOLATILITY_CUBE_FITTED_POINTS, target.toSpecification(), properties);
    return Sets.newHashSet(sabrSurfacesSpecification, smileIdsSpecification);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cubeDefinitionNames = constraints.getValues(PROPERTY_CUBE_DEFINITION);
    if (cubeDefinitionNames == null || cubeDefinitionNames.size() != 1) {
      return null;
    }
    final Set<String> cubeSpecificationNames = constraints.getValues(PROPERTY_CUBE_SPECIFICATION);
    if (cubeSpecificationNames == null || cubeSpecificationNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceDefinitionNames = constraints.getValues(PROPERTY_SURFACE_DEFINITION);
    if (surfaceDefinitionNames == null || surfaceDefinitionNames.size() != 1) {
      return null;
    }
    final Set<String> surfaceSpecificationNames = constraints.getValues(PROPERTY_SURFACE_SPECIFICATION);
    if (surfaceSpecificationNames == null || surfaceSpecificationNames.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(getCubeDataRequirement(cubeDefinitionNames, cubeSpecificationNames, surfaceDefinitionNames, surfaceSpecificationNames));
    requirements.add(getForwardSwapDataRequirement(surfaceDefinitionNames, surfaceSpecificationNames));
    return requirements;
  }

  /**
   * Gets the year fraction from a tenor. Assumes twelve months in a year.
   * @param tenor The tenor
   * @return The year fraction
   */
  private static double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    final double months = period.toTotalMonths();
    return months / 12.;
  }

  /**
   * Constructs the volatility cube requirement.
   * @param definitionNames The cube definition name
   * @param specificationNames The cube specification name
   * @param surfaceDefinitionNames The surface definition name
   * @param surfaceSpecificationNames The surface specification name
   * @return The volatility cube requirement
   */
  private static ValueRequirement getCubeDataRequirement(final Set<String> definitionNames, final Set<String> specificationNames,
      final Set<String> surfaceDefinitionNames, final Set<String> surfaceSpecificationNames) {
    final ValueProperties cubeProperties = ValueProperties.builder()
        .with(PROPERTY_CUBE_DEFINITION, definitionNames)
        .with(PROPERTY_CUBE_SPECIFICATION, specificationNames)
        .with(PROPERTY_SURFACE_DEFINITION, surfaceDefinitionNames)
        .with(PROPERTY_SURFACE_SPECIFICATION, surfaceSpecificationNames)
        .with(PROPERTY_CUBE_UNITS, VolatilityQuoteUnits.LOGNORMAL.getName())
        .get();
    return new ValueRequirement(STANDARD_VOLATILITY_CUBE_DATA, ComputationTargetSpecification.NULL, cubeProperties);
  }

  /**
   * Constructs the forward swap surface requirement.
   * @param definitionNames The forward swap surface definition name
   * @param specificationNames The forward swap surface definition name
   * @return The forward swap surface requirement
   */
  private static ValueRequirement getForwardSwapDataRequirement(final Set<String> definitionNames, final Set<String> specificationNames) {
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(PROPERTY_SURFACE_DEFINITION, definitionNames)
        .with(PROPERTY_SURFACE_SPECIFICATION, specificationNames)
        .get();
    return new ValueRequirement(SURFACE_DATA, ComputationTargetSpecification.NULL, surfaceProperties);
  }

  /**
   * Constructs the SABR surface result properties.
   * @return The SABR surface result properties
   */
  private ValueProperties getResultProperties() {
    return createValueProperties()
        .withAny(PROPERTY_CUBE_DEFINITION)
        .withAny(PROPERTY_CUBE_SPECIFICATION)
        .withAny(PROPERTY_SURFACE_DEFINITION)
        .withAny(PROPERTY_SURFACE_SPECIFICATION)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL, SmileFittingPropertyNamesAndValues.SABR)
        .with(SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD, SmileFittingPropertyNamesAndValues.NON_LINEAR_LEAST_SQUARES).get();
  }

}
