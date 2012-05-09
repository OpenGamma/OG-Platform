/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCouponSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateInstrumentTradeOrSecurityConverter;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunctionHelper;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class MultiYieldCurvePresentValueMethodFunction extends AbstractFunction.NonCompiledInvoker {
  /** Root finder absolute tolerance property name */
  public static final String PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE = "RootFinderAbsoluteTolerance";
  public static final String PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE = "RootFinderRelativeTolerance";
  public static final String PROPERTY_ROOT_FINDER_MAX_ITERATIONS = "RootFinderMaximumIterations";
  public static final String PROPERTY_DECOMPOSITION = "MatrixDecomposition";
  public static final String PROPERTY_USE_FINITE_DIFFERENCE = "UseFiniteDifferenceSensitivities";
  private static final PresentValueCalculator PV_CALCULATOR = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PV_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator PV_COUPON_SENSITIVITY_CALCULATOR = PresentValueCouponSensitivityCalculator.getInstance();
  private static final LastTimeCalculator LAST_DATE_CALCULATOR = LastTimeCalculator.getInstance();
  private static final String LEFT_EXTRAPOLATOR_NAME = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final String RIGHT_EXTRAPOLATOR_NAME = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private InterestRateInstrumentTradeOrSecurityConverter _securityConverter;
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _securityConverter = new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource, true);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String absoluteToleranceName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    final double absoluteTolerance = Double.parseDouble(absoluteToleranceName);
    final String relativeToleranceName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    final double relativeTolerance = Double.parseDouble(relativeToleranceName);
    final String iterationsName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    final int iterations = Integer.parseInt(iterationsName);
    final String decompositionName = desiredValue.getConstraint(PROPERTY_DECOMPOSITION);
    final String useFiniteDifferenceName = desiredValue.getConstraint(PROPERTY_USE_FINITE_DIFFERENCE);
    final boolean useFiniteDifference = Boolean.parseBoolean(useFiniteDifferenceName);
    final Decomposition<?> decomposition = DecompositionFactory.getDecomposition(decompositionName);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final MultiCurveCalculationConfig curveCalculationConfig = new ConfigDBCurveCalculationConfigSource(configSource).getConfig(curveCalculationConfigName);
    final List<InstrumentDerivative> derivatives = new ArrayList<InstrumentDerivative>();
    final DoubleArrayList marketValues = new DoubleArrayList();
    final DoubleArrayList initialRatesGuess = new DoubleArrayList();
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<String, Interpolator1D>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Map<String, Integer> nodesPerCurve = new HashMap<String, Integer>();
    for (final String curveName : curveNames) {
      int nInstruments = 0;
      final InterpolatedYieldCurveSpecificationWithSecurities spec = getYieldCurveSpecification(inputs, targetSpec, curveName);
      final Map<ExternalId, Double> marketDataMap = getMarketData(inputs, targetSpec, curveName);
      final DoubleArrayList nodeTimes = new DoubleArrayList();
      for (final FixedIncomeStripWithSecurity strip : spec.getStrips()) {
        final Double marketValue = marketDataMap.get(strip.getSecurityIdentifier());
        if (marketValue == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + strip);
        }
        final Security security = strip.getSecurity();
        final String[] curveNamesForSecurity = curveCalculationConfig.getCurveExposureForInstrument(curveName, strip.getInstrumentType());
        final InstrumentDefinition<?> definition = _securityConverter.visit(security);
        final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, curveNamesForSecurity, dataSource);
        if (derivative != null) {
          marketValues.add(0.);
          derivatives.add(derivative);
          nodeTimes.add(LAST_DATE_CALCULATOR.visit(derivative));
          initialRatesGuess.add(marketValue);
          nInstruments++;
        }
      }
      nodesPerCurve.put(curveName, nInstruments);
      curveNodes.put(curveName, nodeTimes.toDoubleArray());
      interpolators.put(curveName,
          CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.getInterpolatorName(spec.getInterpolator()), LEFT_EXTRAPOLATOR_NAME, RIGHT_EXTRAPOLATOR_NAME));
    }
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues.toDoubleArray(), null, curveNodes, interpolators, useFiniteDifference);
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(absoluteTolerance, relativeTolerance, iterations, decomposition);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PV_CALCULATOR);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, PV_SENSITIVITY_CALCULATOR);
    final double[] fittedYields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess.toDoubleArray())).getData();
    final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(fittedYields));
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    final ValueProperties properties = getProperties(curveCalculationConfigName, absoluteToleranceName, relativeToleranceName, iterationsName,
        decompositionName, useFiniteDifferenceName);
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties), jacobianMatrix.getData()));
    int i = 0;
    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    for (final String curveName : curveNames) {
      final Integer offset = nodesPerCurve.get(curveName);
      final double[] yields = Arrays.copyOfRange(fittedYields, i, i + offset);
      final YieldCurve yieldCurve = new YieldCurve(InterpolatedDoublesCurve.from(curveNodes.get(curveName), yields, interpolators.get(curveName)));
      final ValueProperties curveProperties = getCurveProperties(curveCalculationConfigName, curveName, absoluteToleranceName,
          relativeToleranceName, iterationsName, decompositionName, useFiniteDifferenceName);
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, curveProperties);
      results.add(new ComputedValue(spec, yieldCurve));
      curveBundle.setCurve(curveName, yieldCurve);
      i += offset;
    }
    i = 0;
    final double[] couponSensitivities = new double[derivatives.size()];
    for (final InstrumentDerivative derivative : derivatives) {
      couponSensitivities[i++] = PV_COUPON_SENSITIVITY_CALCULATOR.visit(derivative, curveBundle);
    }
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, properties), new DoubleMatrix1D(couponSensitivities)));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE || target.getUniqueId() == null) {
      return false;
    }
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties curveProperties = getCurveProperties();
    final ValueProperties properties = getProperties();
    final ValueSpecification curve = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), curveProperties);
    final ValueSpecification jacobian = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, target.toSpecification(), properties);
    final ValueSpecification couponSensitivities = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, target.toSpecification(), properties);
    return Sets.newHashSet(curve, jacobian, couponSensitivities);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> rootFinderAbsoluteTolerance = constraints.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    if (rootFinderAbsoluteTolerance == null || rootFinderAbsoluteTolerance.size() != 1) {
      return null;
    }
    final Set<String> rootFinderRelativeTolerance = constraints.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    if (rootFinderRelativeTolerance == null || rootFinderRelativeTolerance.size() != 1) {
      return null;
    }
    final Set<String> maxIterations = constraints.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    if (maxIterations == null || maxIterations.size() != 1) {
      return null;
    }
    final Set<String> decomposition = constraints.getValues(PROPERTY_DECOMPOSITION);
    if (decomposition == null || decomposition.size() != 1) {
      return null;
    }
    final Set<String> useFiniteDifference = constraints.getValues(PROPERTY_USE_FINITE_DIFFERENCE);
    if (useFiniteDifference == null || useFiniteDifference.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final String curveName : curveNames) {
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get()));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get()));
    }
    return requirements;
  }

  private ValueProperties getProperties() {
    return createValueProperties()
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
        .withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE).get();
  }

  private ValueProperties getCurveProperties() {
    return createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
        .withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE).get();
  }

  private ValueProperties getProperties(final String curveCalculationConfigName, final String absoluteTolerance, final String relativeTolerance, final String maxIterations,
      final String decomposition, final String useFiniteDifference) {
    return createValueProperties()
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance)
        .with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance)
        .with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIterations)
        .with(PROPERTY_DECOMPOSITION, decomposition)
        .with(PROPERTY_USE_FINITE_DIFFERENCE, useFiniteDifference).get();
  }

  private ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName, final String absoluteTolerance, final String relativeTolerance,
      final String maxIterations, final String decomposition, final String useFiniteDifference) {
    return createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance)
        .with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance)
        .with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIterations)
        .with(PROPERTY_DECOMPOSITION, decomposition)
        .with(PROPERTY_USE_FINITE_DIFFERENCE, useFiniteDifference).get();
  }

  private Map<ExternalId, Double> getMarketData(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement marketDataRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get());
    final Object marketDataMapObject = inputs.getValue(marketDataRequirement);
    if (marketDataMapObject == null) {
      throw new OpenGammaRuntimeException("Could not get a value for requirement " + marketDataRequirement);
    }
    final Map<ExternalId, Double> marketDataMap = YieldCurveFunctionHelper.buildMarketDataMap((SnapshotDataBundle) marketDataMapObject);
    return marketDataMap;
  }

  private InterpolatedYieldCurveSpecificationWithSecurities getYieldCurveSpecification(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement specRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get());
    final Object specObject = inputs.getValue(specRequirement);
    if (specObject == null) {
      throw new OpenGammaRuntimeException("Could not get a value for requirement " + specRequirement);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities spec = (InterpolatedYieldCurveSpecificationWithSecurities) specObject;
    return spec;
  }
}
