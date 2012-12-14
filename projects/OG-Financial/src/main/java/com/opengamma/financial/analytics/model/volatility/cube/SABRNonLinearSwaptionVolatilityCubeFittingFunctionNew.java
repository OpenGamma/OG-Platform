/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Period;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
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
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.forward.ForwardSwapCurveMarketDataFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.analytics.model.volatility.cube.fitted.FittedSmileDataPoints;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.analytics.volatility.cube.ConfigDBSwaptionVolatilityCubeSpecificationSource;
import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeSpecification;
import com.opengamma.financial.analytics.volatility.cube.SyntheticSwaptionVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubePropertyNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.class);
  /** Name of the property that determines whether to fix alpha to the start value */
  public static final String PROPERTY_USE_FIXED_ALPHA = "UseFixedAlpha";
  /** Name of the property that determines whether to fix beta to the start value */
  public static final String PROPERTY_USE_FIXED_BETA = "UseFixedBeta";
  /** Name of the property that determines whether to fix rho to the start value */
  public static final String PROPERTY_USE_FIXED_RHO = "UseFixedRho";
  /** Name of the property that determines whether to fix nu to the start value */
  public static final String PROPERTY_USE_FIXED_NU = "UseFixedNu";
  /** Name of the property that supplies a starting value for alpha */
  public static final String PROPERTY_ALPHA_START_VALUE = "AlphaStartValue";
  /** Name of the property that supplies a starting value for beta */
  public static final String PROPERTY_BETA_START_VALUE = "BetaStartValue";
  /** Name of the property that supplies a starting value for rho */
  public static final String PROPERTY_RHO_START_VALUE = "RhoStartValue";
  /** Name of the property that supplies a starting value for nu */
  public static final String PROPERTY_NU_START_VALUE = "NuStartValue";
  /** Name of the property that supplies the error for the fit */
  public static final String PROPERTY_EPS = "eps";
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = VolatilityFunctionFactory.HAGAN_FORMULA;
  private static final ExternalId[] EMPTY_ARRAY = new ExternalId[0];

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final VolatilityCubeDefinitionSource definitionSource = new SyntheticSwaptionVolatilityCubeDefinitionSource(configSource);
    final ConfigDBSwaptionVolatilityCubeSpecificationSource specificationSource = new ConfigDBSwaptionVolatilityCubeSpecificationSource(configSource);
    final ValueRequirement desiredSurface = getDesiredSurfaceRequirement(desiredValues);
    final String definitionName = desiredSurface.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
    final String specificationName = desiredSurface.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
    final String cubeName = desiredSurface.getConstraint(ValuePropertyNames.CUBE);
    final String curveName = desiredSurface.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationMethod = desiredSurface.getConstraint(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    final Object volatilityDataObject = inputs.getValue(getCubeDataRequirement(target, cubeName, definitionName, specificationName));
    if (volatilityDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube data");
    }
    final String uniqueId = target.getUniqueId().getValue();
    final Currency currency = Currency.of(uniqueId);
    final String fullSpecificationName = specificationName + "_" + uniqueId;
    final String fullDefinitionName = definitionName + "_" + uniqueId;
    final SwaptionVolatilityCubeSpecification specification = specificationSource.getSpecification(fullSpecificationName);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube specification name " + fullSpecificationName);
    }
    final VolatilityCubeDefinition definition = definitionSource.getDefinition(currency, fullDefinitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube definition name " + fullDefinitionName);
    }
    final DoubleMatrix1D initialValues = getInitialParameters(desiredSurface);
    final BitSet fixed = getFixedParameters(desiredSurface);
    final String epsValue = desiredSurface.getConstraint(PROPERTY_EPS);
    final double eps = Double.parseDouble(epsValue);
    final GridInterpolator2D interpolator = getInterpolator(desiredSurface);
    final DoubleArrayList swapMaturitiesList = new DoubleArrayList();
    final DoubleArrayList swaptionExpiriesList = new DoubleArrayList();
    final DoubleArrayList alphaList = new DoubleArrayList();
    final DoubleArrayList betaList = new DoubleArrayList();
    final DoubleArrayList nuList = new DoubleArrayList();
    final DoubleArrayList rhoList = new DoubleArrayList();
    final DoubleArrayList chiSqList = new DoubleArrayList();
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    final Map<Pair<Tenor, Tenor>, ExternalId[]> fittedSmileIds = new HashMap<Pair<Tenor, Tenor>, ExternalId[]>();
    final Map<Pair<Tenor, Tenor>, Double[]> fittedRelativeStrikes = new HashMap<Pair<Tenor, Tenor>, Double[]>();
    final List<Tenor> swapTenorData = definition.getSwapTenors();
    final List<Tenor> swaptionExpiryData = definition.getOptionExpiries();
    final List<Double> relativeStrikeData = definition.getRelativeStrikes();
    final VolatilityCubeData volatilityCubeData = (VolatilityCubeData) volatilityDataObject;
    final Map<VolatilityPoint, Double> dataPoint = volatilityCubeData.getDataPoints();
    final Map<VolatilityPoint, ExternalId> idPoint = volatilityCubeData.getDataIds();
    for (final Tenor swapTenor : swapTenorData) {
      final double maturity = getTime(swapTenor);
      for (final Tenor swaptionExpiry : swaptionExpiryData) {
        final Object forwardCurveObject = inputs.getValue(getForwardCurveRequirement(target, curveName, curveCalculationMethod, swaptionExpiry.getPeriod().toString()));
        if (forwardCurveObject == null) {
          s_logger.error("Could not get forward curve for swap tenor " + swapTenor);
          continue;
        }
        final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
        final double expiry = getTime(swaptionExpiry);
        final double forward = forwardCurve.getForward(maturity);
        final DoubleArrayList smileStrikes = new DoubleArrayList();
        final DoubleArrayList smileBlackVols = new DoubleArrayList();
        final DoubleArrayList errors = new DoubleArrayList();
        final ObjectArrayList<ExternalId> externalIds = new ObjectArrayList<ExternalId>();
        final ObjectArrayList<Double> smileDeltas = new ObjectArrayList<Double>();
        for (final double relativeStrike : relativeStrikeData) {
          final VolatilityPoint volatilityPoint = new VolatilityPoint(swapTenor, swaptionExpiry, relativeStrike);
          final Double vol = dataPoint.get(volatilityPoint);
          if (vol != null) {
            final double strike = forward + relativeStrike;
            smileStrikes.add(strike);
            smileBlackVols.add(vol);
            errors.add(eps);
            externalIds.add(idPoint.get(volatilityPoint));
          }
        }
        if (smileStrikes.size() > 4 && forward > 0) { //don't fit those smiles with insufficient data
          final LeastSquareResultsWithTransform fittedResult = new SABRModelFitter(forward, smileStrikes.toDoubleArray(), expiry, smileBlackVols.toDoubleArray(), errors.toDoubleArray(), SABR_FUNCTION)
              .solve(initialValues, fixed);
          final DoubleMatrix1D parameters = fittedResult.getModelParameters();
          swapMaturitiesList.add(maturity);
          swaptionExpiriesList.add(expiry);
          alphaList.add(parameters.getEntry(0));
          betaList.add(parameters.getEntry(1));
          rhoList.add(parameters.getEntry(2));
          nuList.add(parameters.getEntry(3));
          final Pair<Tenor, Tenor> tenorPair = Pair.of(swapTenor, swaptionExpiry);
          final DoublesPair expiryMaturityPair = new DoublesPair(expiry, maturity);
          inverseJacobians.put(expiryMaturityPair, fittedResult.getModelParameterSensitivityToData());
          chiSqList.add(fittedResult.getChiSq());
          fittedSmileIds.put(tenorPair, externalIds.toArray(EMPTY_ARRAY));
          fittedRelativeStrikes.put(tenorPair, smileDeltas.toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
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
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, alpha, interpolator, "SABR alpha surface");
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, beta, interpolator, "SABR beta surface");
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, nu, interpolator, "SABR nu surface");
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(swaptionExpiries, swapMaturities, rho, interpolator, "SABR rho surface");
    final SABRFittedSurfaces fittedSurfaces = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, inverseJacobians);
    final ValueProperties properties = getResultProperties(target, desiredSurface);
    final ValueSpecification sabrSurfacesSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, target.toSpecification(), properties);
    final ValueSpecification smileIdsSpecification = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, target.toSpecification(), properties);
    return Sets.newHashSet(new ComputedValue(sabrSurfacesSpecification, fittedSurfaces), new ComputedValue(smileIdsSpecification, new FittedSmileDataPoints(fittedSmileIds, fittedRelativeStrikes)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final UniqueId uid = target.getUniqueId();
    return (uid != null) && Currency.OBJECT_SCHEME.equals(uid.getScheme());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target);
    final ValueSpecification sabrSurfaceSpecification = new ValueSpecification(ValueRequirementNames.SABR_SURFACES, target.toSpecification(), properties);
    final ValueSpecification smileIdsSpecification = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_FITTED_POINTS, target.toSpecification(), properties);
    return Sets.newHashSet(sabrSurfaceSpecification, smileIdsSpecification);
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cubeNames = constraints.getValues(ValuePropertyNames.CUBE);
    if (cubeNames == null || cubeNames.size() != 1) {
      s_logger.error("Did not provide a single cube name; asked for {}", cubeNames);
      return null;
    }
    final Set<String> cubeDefinitions = constraints.getValues(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
    if (cubeDefinitions == null || cubeDefinitions.size() != 1) {
      return null;
    }
    final String cubeName = cubeNames.iterator().next();
    final String uniqueId = target.getUniqueId().getValue();
    final Currency currency = Currency.of(uniqueId);
    final String definitionName = cubeDefinitions.iterator().next();
    final String fullDefinitionName = definitionName + "_" + uniqueId;
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final SyntheticSwaptionVolatilityCubeDefinitionSource definitionSource = new SyntheticSwaptionVolatilityCubeDefinitionSource(configSource);
    final VolatilityCubeDefinition definition = definitionSource.getDefinition(currency, fullDefinitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube definition name " + fullDefinitionName);
    }
    final Set<String> cubeSpecifications = constraints.getValues(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
    if (cubeSpecifications == null || cubeSpecifications.size() != 1) {
      return null;
    }
    final Set<String> xInterpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (xInterpolatorNames == null || xInterpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> xExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.X_EXTRAPOLATOR_NAME);
    if (xExtrapolatorNames == null || xExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> yInterpolatorNames = constraints.getValues(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    if (yInterpolatorNames == null || yInterpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> yExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.Y_EXTRAPOLATOR_NAME);
    if (yExtrapolatorNames == null || yExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveCalculationMethodNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethodNames == null || forwardCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveInterpolatorNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR);
    if (forwardCurveInterpolatorNames == null || forwardCurveInterpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveLeftExtrapolatorNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR);
    if (forwardCurveLeftExtrapolatorNames == null || forwardCurveLeftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveRightExtrapolatorNames = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR);
    if (forwardCurveRightExtrapolatorNames == null || forwardCurveRightExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> epsValues = constraints.getValues(PROPERTY_EPS);
    if (epsValues == null || epsValues.size() != 1) {
      return null;
    }
    final Set<String> alphaStartValues = constraints.getValues(PROPERTY_ALPHA_START_VALUE);
    if (alphaStartValues == null || alphaStartValues.size() != 1) {
      return null;
    }
    final Set<String> betaStartValues = constraints.getValues(PROPERTY_BETA_START_VALUE);
    if (betaStartValues == null || betaStartValues.size() != 1) {
      return null;
    }
    final Set<String> rhoStartValues = constraints.getValues(PROPERTY_RHO_START_VALUE);
    if (rhoStartValues == null || rhoStartValues.size() != 1) {
      return null;
    }
    final Set<String> nuStartValues = constraints.getValues(PROPERTY_NU_START_VALUE);
    if (nuStartValues == null || nuStartValues.size() != 1) {
      return null;
    }
    final Set<String> fixedAlpha = constraints.getValues(PROPERTY_USE_FIXED_ALPHA);
    if (fixedAlpha == null || fixedAlpha.size() != 1) {
      return null;
    }
    final Set<String> fixedBeta = constraints.getValues(PROPERTY_USE_FIXED_BETA);
    if (fixedBeta == null || fixedBeta.size() != 1) {
      return null;
    }
    final Set<String> fixedRho = constraints.getValues(PROPERTY_USE_FIXED_RHO);
    if (fixedRho == null || fixedRho.size() != 1) {
      return null;
    }
    final Set<String> fixedNu = constraints.getValues(PROPERTY_USE_FIXED_NU);
    if (fixedNu == null || fixedNu.size() != 1) {
      return null;
    }
    final String specificationName = cubeSpecifications.iterator().next();
    final String curveName = forwardCurveNames.iterator().next();
    final String curveCalculationMethodName = forwardCurveCalculationMethodNames.iterator().next();
    final ValueRequirement swaptionCubeRequirement = getCubeDataRequirement(target, cubeName, definitionName, specificationName);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(swaptionCubeRequirement);
    for (final Tenor tenor : definition.getOptionExpiries()) {
      requirements.add(getForwardCurveRequirement(target, curveName, curveCalculationMethodName, tenor.getPeriod().toString()));
    }
    return requirements;
  }

  private ValueProperties getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(PROPERTY_ALPHA_START_VALUE)
        .withAny(PROPERTY_BETA_START_VALUE)
        .withAny(PROPERTY_NU_START_VALUE)
        .withAny(PROPERTY_RHO_START_VALUE)
        .withAny(PROPERTY_USE_FIXED_ALPHA)
        .withAny(PROPERTY_USE_FIXED_BETA)
        .withAny(PROPERTY_USE_FIXED_NU)
        .withAny(PROPERTY_USE_FIXED_RHO)
        .withAny(PROPERTY_EPS)
        .withAny(ValuePropertyNames.CUBE)
        .with(ValuePropertyNames.CURRENCY, target.getUniqueId().getValue())
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.Y_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.Y_EXTRAPOLATOR_NAME)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION)
        .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_INTERPOLATOR)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_LEFT_EXTRAPOLATOR)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_RIGHT_EXTRAPOLATOR)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_METHOD)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE, SurfaceAndCubeQuoteType.RELATIVE_STRIKE)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE)
        .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.SABR)
        .with(SmileFittingProperties.PROPERTY_FITTING_METHOD, SmileFittingProperties.NON_LINEAR_LEAST_SQUARES).get();
  }

  private ValueProperties getResultProperties(final ComputationTarget target, final ValueRequirement desiredSurface) {
    return desiredSurface.getConstraints().compose(createValueProperties().get());
  }

  private ValueRequirement getCubeDataRequirement(final ComputationTarget target, final String cubeName, final String definitionName, final String specificationName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CUBE, cubeName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, definitionName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, specificationName)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE, SurfaceAndCubeQuoteType.CALL_DELTA)
        .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE).get();
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties);
  }

  private ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final String curveName, final String curveCalculationMethod, final String forwardTenor) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ForwardSwapCurveMarketDataFunction.PROPERTY_FORWARD_TENOR, forwardTenor).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }

  private double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    final double months = period.totalMonths();
    return months / 12.;
  }

  private ValueRequirement getDesiredSurfaceRequirement(final Set<ValueRequirement> desiredValues) {
    ValueRequirement desiredSurface = null;
    for (final ValueRequirement desiredValue : desiredValues) {
      if (desiredValue.getValueName().equals(ValueRequirementNames.SABR_SURFACES)) {
        desiredSurface = desiredValue;
        break;
      }
    }
    if (desiredSurface == null) {
      throw new OpenGammaRuntimeException("Could not get desired surfaces requirement");
    }
    return desiredSurface;
  }

  private GridInterpolator2D getInterpolator(final ValueRequirement desiredSurface) {
    final String xInterpolatorName = desiredSurface.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final Interpolator1D xInterpolator = Interpolator1DFactory.getInterpolator(xInterpolatorName);
    final String xExtrapolatorName = desiredSurface.getConstraint(InterpolatedDataProperties.X_EXTRAPOLATOR_NAME);
    final Interpolator1D xExtrapolator = CombinedInterpolatorExtrapolatorFactory.getExtrapolator(xExtrapolatorName, xInterpolator);
    final String yInterpolatorName = desiredSurface.getConstraint(InterpolatedDataProperties.Y_INTERPOLATOR_NAME);
    final Interpolator1D yInterpolator = Interpolator1DFactory.getInterpolator(yInterpolatorName);
    final String yExtrapolatorName = desiredSurface.getConstraint(InterpolatedDataProperties.Y_EXTRAPOLATOR_NAME);
    final Interpolator1D yExtrapolator = CombinedInterpolatorExtrapolatorFactory.getExtrapolator(yExtrapolatorName, yInterpolator);
    final GridInterpolator2D interpolator = new GridInterpolator2D(xInterpolator, yInterpolator, xExtrapolator, yExtrapolator);
    return interpolator;
  }

  private DoubleMatrix1D getInitialParameters(final ValueRequirement desiredSurface) {
    final String alphaStartValue = desiredSurface.getConstraint(PROPERTY_ALPHA_START_VALUE);
    final double alphaStart = Double.parseDouble(alphaStartValue);
    final String betaStartValue = desiredSurface.getConstraint(PROPERTY_BETA_START_VALUE);
    final double betaStart = Double.parseDouble(betaStartValue);
    final String rhoStartValue = desiredSurface.getConstraint(PROPERTY_RHO_START_VALUE);
    final double rhoStart = Double.parseDouble(rhoStartValue);
    final String nuStartValue = desiredSurface.getConstraint(PROPERTY_NU_START_VALUE);
    final double nuStart = Double.parseDouble(nuStartValue);
    final DoubleMatrix1D initialValues = new DoubleMatrix1D(new double[] {alphaStart, betaStart, rhoStart, nuStart });
    return initialValues;
  }

  private BitSet getFixedParameters(final ValueRequirement desiredSurface) {
    final BitSet fixed = new BitSet(4);
    final String useFixedAlpha = desiredSurface.getConstraint(PROPERTY_USE_FIXED_ALPHA);
    if (Boolean.parseBoolean(useFixedAlpha)) {
      fixed.set(0);
    }
    final String useFixedBeta = desiredSurface.getConstraint(PROPERTY_USE_FIXED_BETA);
    if (Boolean.parseBoolean(useFixedBeta)) {
      fixed.set(1);
    }
    final String useFixedRho = desiredSurface.getConstraint(PROPERTY_USE_FIXED_RHO);
    if (Boolean.parseBoolean(useFixedRho)) {
      fixed.set(2);
    }
    final String useFixedNu = desiredSurface.getConstraint(PROPERTY_USE_FIXED_NU);
    if (Boolean.parseBoolean(useFixedNu)) {
      fixed.set(3);
    }
    return fixed;
  }
}
