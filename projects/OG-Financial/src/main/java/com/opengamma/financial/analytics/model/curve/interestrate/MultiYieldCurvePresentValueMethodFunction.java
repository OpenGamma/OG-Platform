/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCouponSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.MultiCurveFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;

/**
 * Constructs yield curves and the Jacobian from {@link YieldCurveDefinition}s. Multiple curves can be constructed simultaneously using root-finding. The configuration object that control the
 * construction is {@link MultiCurveCalculationConfig}. The root-finder uses present value = 0 as its target.
 * 
 * @deprecated This function uses configuration objects that have been superseded. Use functions that descend from {@link MultiCurveFunction}.
 */
@Deprecated
public class MultiYieldCurvePresentValueMethodFunction extends MultiYieldCurveFunction {
  /** Calculates the present value of instruments on the curve */
  private static final PresentValueCalculator PV_CALCULATOR = PresentValueCalculator.getInstance();
  /** Calculates the sensitivity of the present value of the instruments to the curve */
  private static final PresentValueCurveSensitivityCalculator PV_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  /** Calculates the sensitivity of the present value to the coupons of the instruments */
  private static final PresentValueCouponSensitivityCalculator PV_COUPON_SENSITIVITY_CALCULATOR = PresentValueCouponSensitivityCalculator.getInstance();
  /** Calculates the maturity time of the instruments on the curve */
  private static final LastTimeCalculator LAST_TIME_CALCULATOR = LastTimeCalculator.getInstance();
  /** Converts definitions to derivatives */
  private FixedIncomeConverterDataProvider _definitionConverter;

  private InterestRateInstrumentTradeOrSecurityConverter getSecurityConverter(final FunctionExecutionContext context) {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    return new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource, true, context.getComputationTargetResolver()
        .getVersionCorrection());
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String absoluteToleranceName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    final String relativeToleranceName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    final String iterationsName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    final String decompositionName = desiredValue.getConstraint(PROPERTY_DECOMPOSITION);
    final String useFiniteDifferenceName = desiredValue.getConstraint(PROPERTY_USE_FINITE_DIFFERENCE);
    final MultiCurveCalculationConfig curveCalculationConfig = getCurveCalculationConfigSource().getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final List<InstrumentDerivative> derivatives = new ArrayList<>();
    final DoubleArrayList marketValues = new DoubleArrayList();
    final DoubleArrayList initialRatesGuess = new DoubleArrayList();
    final LinkedHashSet<String> curveNames = new LinkedHashSet<>();
    for (final String curveName : curveCalculationConfig.getYieldCurveNames()) {
      curveNames.add(curveName);
    }
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<>();
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Map<String, Integer> nodesPerCurve = new HashMap<>();
    final HistoricalTimeSeriesBundle timeSeries = getTimeSeriesBundle(inputs, targetSpec, curveCalculationConfigName);
    final InterestRateInstrumentTradeOrSecurityConverter securityConverter = getSecurityConverter(executionContext);
    for (final String curveName : curveNames) {
      final InterpolatedYieldCurveSpecificationWithSecurities spec = getYieldCurveSpecification(inputs, targetSpec, curveName);
      if (spec == null) {
        continue;
      }
      int nInstruments = 0;
      final Interpolator1D interpolator = spec.getInterpolator();
      final YieldCurveData marketData = getMarketData(inputs, targetSpec, curveName);
      final DoubleArrayList nodeTimes = new DoubleArrayList();
      FixedIncomeStripWithSecurity previousStrip = null;
      for (final FixedIncomeStripWithSecurity strip : spec.getStrips()) {
        final Double marketValue = marketData.getDataPoint(strip.getSecurityIdentifier());
        if (marketValue == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + strip);
        }
        final Security security = strip.getSecurity();
        final String[] curveNamesForSecurity = curveCalculationConfig.getCurveExposureForInstrument(curveName, strip.getInstrumentType());
        final InstrumentDefinition<?> definition = securityConverter.visit(security);
        final InstrumentDerivative derivative = _definitionConverter.convert(security, definition, now, curveNamesForSecurity, timeSeries);
        if (derivative != null) {
          if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
            InstrumentDefinition<?> unitNotional;
            if (definition instanceof InterestRateFutureSecurityDefinition) {
              final InterestRateFutureSecurityDefinition securityDefinition = (InterestRateFutureSecurityDefinition) definition;
              unitNotional = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, marketValue);
            } else {
              unitNotional = ((InterestRateFutureTransactionDefinition) definition).withNewNotionalAndTransactionPrice(1, marketValue);
              // Implementation note: to have the same notional for OTC and futures (and thus not near-singular Jacobian)
            }
            final InstrumentDerivative unitNotionalDerivative = _definitionConverter.convert(security, unitNotional, now, curveNamesForSecurity, timeSeries);
            derivatives.add(unitNotionalDerivative);
            initialRatesGuess.add(1 - marketValue);
          } else {
            derivatives.add(derivative);
            initialRatesGuess.add(marketValue);
          }
          final double t = derivative.accept(LAST_TIME_CALCULATOR);
          if (nInstruments > 0 && CompareUtils.closeEquals(nodeTimes.get(nInstruments - 1), t, 1e-12)) {
            throw new OpenGammaRuntimeException("Strip " + strip + " has same maturity as one already added (" + previousStrip + ") - will lead to" +
                "equal nodes in the curve. Remove one of these strips.");
          }
          nodeTimes.add(t);
          marketValues.add(0.0);
          previousStrip = strip;
          nInstruments++;
        }
      }
      nodesPerCurve.put(curveName, nInstruments);
      curveNodes.put(curveName, nodeTimes.toDoubleArray());
      interpolators.put(curveName, interpolator);
    }
    final YieldCurveBundle knownCurves = getKnownCurves(curveCalculationConfig, targetSpec, inputs);
    final double absoluteTolerance = Double.parseDouble(absoluteToleranceName);
    final double relativeTolerance = Double.parseDouble(relativeToleranceName);
    final int iterations = Integer.parseInt(iterationsName);
    final boolean useFiniteDifference = Boolean.parseBoolean(useFiniteDifferenceName);
    final Decomposition<?> decomposition = DecompositionFactory.getDecomposition(decompositionName);
    final Set<ComputedValue> results = new HashSet<>();
    final Currency currency = Currency.of(targetSpec.getUniqueId().getValue());
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues.toDoubleArray(), knownCurves, curveNodes, interpolators,
        useFiniteDifference, new FXMatrix(currency));
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(absoluteTolerance, relativeTolerance, iterations, decomposition);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PV_CALCULATOR);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, PV_SENSITIVITY_CALCULATOR);
    final double[] fittedYields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess.toDoubleArray())).getData();
    final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(fittedYields));
    final ValueProperties properties = getJacobianProperties(curveCalculationConfigName, absoluteToleranceName, relativeToleranceName, iterationsName, decompositionName,
        useFiniteDifferenceName);
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties), jacobianMatrix.getData()));
    int i = 0;
    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    for (final String curveName : curveNames) {
      final Integer offset = nodesPerCurve.get(curveName);
      if (offset == null) {
        continue;
      }
      final double[] yields = Arrays.copyOfRange(fittedYields, i, i + offset);
      final YieldCurve yieldCurve = YieldCurve.from(InterpolatedDoublesCurve.from(curveNodes.get(curveName), yields, interpolators.get(curveName)));
      final ValueProperties curveProperties = getCurveProperties(curveCalculationConfigName, curveName, absoluteToleranceName, relativeToleranceName, iterationsName, decompositionName,
          useFiniteDifferenceName);
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, curveProperties);
      results.add(new ComputedValue(spec, yieldCurve));
      curveBundle.setCurve(curveName, yieldCurve);
      i += offset;
    }
    i = 0;
    final double[] couponSensitivities = new double[derivatives.size()];
    for (final InstrumentDerivative derivative : derivatives) {
      couponSensitivities[i++] = derivative.accept(PV_COUPON_SENSITIVITY_CALCULATOR, curveBundle);
    }
    results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, properties), new DoubleMatrix1D(couponSensitivities)));
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<>();
    results.addAll(super.getResults(context, target));
    final ValueProperties properties = getJacobianProperties();
    final ValueSpecification couponSensitivities = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, target.toSpecification(), properties);
    results.add(couponSensitivities);
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> results = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    String curveCalculationConfigName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement value = entry.getValue();
      if (value.getValueName().equals(ValueRequirementNames.YIELD_CURVE_SPEC)) {
        if (curveCalculationConfigName == null) {
          curveCalculationConfigName = value.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        } else {
          if (!value.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG).equals(curveCalculationConfigName)) {
            throw new OpenGammaRuntimeException("Had different curve calculation configuration names; should never happen");
          }
        }
        final String curveName = value.getConstraint(ValuePropertyNames.CURVE);
        final ValueProperties curveProperties = getCurveProperties(curveCalculationConfigName, curveName);
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, curveProperties);
        results.add(spec);
      }
    }
    assert curveCalculationConfigName != null;
    final ValueProperties properties = getJacobianProperties(curveCalculationConfigName);
    final ValueSpecification jacobian = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties);
    final ValueSpecification sensitivities = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, properties);
    results.add(jacobian);
    results.add(sensitivities);
    return results;
  }

  @Override
  protected ValueProperties getJacobianProperties() {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PRESENT_VALUE_STRING).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE).get();
  }

  @Override
  protected ValueProperties getCurveProperties() {
    return createValueProperties().withAny(ValuePropertyNames.CURVE).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PRESENT_VALUE_STRING)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(PROPERTY_DECOMPOSITION).withAny(PROPERTY_USE_FINITE_DIFFERENCE).get();
  }

  @Override
  protected ValueProperties getJacobianProperties(final String curveCalculationConfigName) {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PRESENT_VALUE_STRING).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE).get();
  }

  @Override
  protected ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName) {
    return createValueProperties().with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PRESENT_VALUE_STRING)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(PROPERTY_DECOMPOSITION).withAny(PROPERTY_USE_FINITE_DIFFERENCE).get();
  }

  @Override
  protected ValueProperties getJacobianProperties(final String curveCalculationConfigName, final String absoluteTolerance, final String relativeTolerance, final String maxIterations,
      final String decomposition, final String useFiniteDifference) {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PRESENT_VALUE_STRING).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance).with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance)
        .with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIterations).with(PROPERTY_DECOMPOSITION, decomposition).with(PROPERTY_USE_FINITE_DIFFERENCE, useFiniteDifference).get();
  }

  @Override
  protected ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName, final String absoluteTolerance, final String relativeTolerance,
      final String maxIterations, final String decomposition, final String useFiniteDifference) {
    return createValueProperties().with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PRESENT_VALUE_STRING)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance)
        .with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance).with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIterations).with(PROPERTY_DECOMPOSITION, decomposition)
        .with(PROPERTY_USE_FINITE_DIFFERENCE, useFiniteDifference).get();
  }

  @Override
  protected String getCalculationMethod() {
    return PRESENT_VALUE_STRING;
  }
}
