/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.NO_VALUE;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.YES_VALUE;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderIRSJacobian;
import com.opengamma.analytics.financial.interestrate.ParSpreadRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.RateReplacingInterestRateDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
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
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.MultiCurveFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;

/**
 * Constructs yield curves from {@link YieldCurveDefinition}s. Multiple curves can be constructed simultaneously using root-finding. The configuration object that control the construction is
 * {@link MultiCurveCalculationConfig}. The root-finder uses present value = 0 as its target, where an appropriate spread is added to the fixed rate or yield of an instrument.
 * 
 * @deprecated This function uses configuration objects that have been superseded. Use functions that descend from {@link MultiCurveFunction}.
 */
@Deprecated
public class MultiYieldCurveParRateMethodSeriesFunction extends MultiYieldCurveSeriesFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MultiYieldCurveParRateMethodSeriesFunction.class);
  /** Calculates the spread to the par rate of instruments on the curve */
  private static final ParSpreadRateCalculator PAR_SPREAD_RATE_CALCULATOR = ParSpreadRateCalculator.getInstance();
  /** Calculates the sensitivity of the par rate spread to the curve */
  private static final ParSpreadRateCurveSensitivityCalculator PAR_SPREAD_RATE_SENSITIVITY_CALCULATOR = ParSpreadRateCurveSensitivityCalculator.getInstance();
  /** Calculates the maturity time of the instruments on the curve */
  private static final LastTimeCalculator LAST_TIME_CALCULATOR = LastTimeCalculator.getInstance();
  /** Converts instrument definitions to derivatives */
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
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
  }

  private class CurveStripDataBundle {

    private final FixedIncomeStripWithSecurity _strip;
    private final HistoricalTimeSeries _historicalTimeSeries;
    private final String[] _curveNames;
    private final InstrumentDefinition<?> _definition;
    private final InstrumentDerivative _derivative;

    public CurveStripDataBundle(final ZonedDateTime now, final InterestRateInstrumentTradeOrSecurityConverter securityConverter, final MultiCurveCalculationConfig curveCalculationConfig,
        final CurveDataBundle curve, final FixedIncomeStripWithSecurity strip, final HistoricalTimeSeriesBundle marketData) {
      _strip = strip;
      _historicalTimeSeries = marketData.get(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurityIdentifier());
      if (_historicalTimeSeries == null) {
        throw new OpenGammaRuntimeException("Could not get historical time series for " + strip);
      }

      final Security security = strip.getSecurity();
      _curveNames = curveCalculationConfig.getCurveExposureForInstrument(curve._name, strip.getInstrumentType());
      _definition = securityConverter.visit(security);
      _derivative = _definitionConverter.convert(security, _definition, now, _curveNames, curve._timeSeries);
    }

  }

  private class CurveDataBundle {
    // inputs
    private final String _name;
    private final Interpolator1D _interpolator;
    private final CurveStripDataBundle[] _strips;
    private final HistoricalTimeSeriesBundle _timeSeries;
    private final int _nodes;
    // temporary data
    private double[] _nodeTimes;
    // outputs

    private final Map<LocalDate, YieldAndDiscountCurve> _dateCurveMap = new LinkedHashMap<>();

    public CurveDataBundle(final ZonedDateTime now, final InterestRateInstrumentTradeOrSecurityConverter securityConverter, final MultiCurveCalculationConfig curveCalculationConfig,
        final String name, final FunctionInputs inputs, final ComputationTargetSpecification targetSpec) {
      _name = name;
      final InterpolatedYieldCurveSpecificationWithSecurities spec = getYieldCurveSpecification(inputs, targetSpec, name);
      _interpolator = spec.getInterpolator();
      _timeSeries = getTimeSeriesBundle(inputs, targetSpec, name);
      final HistoricalTimeSeriesBundle marketDataBundle = getHistoricalMarketData(inputs, targetSpec, name);
      final Set<FixedIncomeStripWithSecurity> strips = spec.getStrips();
      _strips = new CurveStripDataBundle[strips.size()];
      int i = 0;
      int nodes = 0;
      for (final FixedIncomeStripWithSecurity strip : strips) {
        final CurveStripDataBundle stripData = new CurveStripDataBundle(now, securityConverter, curveCalculationConfig, this, strip, marketDataBundle);
        if (stripData._derivative != null) {
          nodes++;
        }
        _strips[i++] = stripData;
      }
      _nodes = nodes;
    }
  }

  private CurveDataBundle[] getCurveData(final ZonedDateTime now, final InterestRateInstrumentTradeOrSecurityConverter securityConverter,
      final MultiCurveCalculationConfig curveCalculationConfig, final FunctionInputs inputs, final ComputationTargetSpecification targetSpec) {
    final String[] names = curveCalculationConfig.getYieldCurveNames();
    final CurveDataBundle[] curves = new CurveDataBundle[names.length];
    for (int i = 0; i < names.length; i++) {
      curves[i] = new CurveDataBundle(now, securityConverter, curveCalculationConfig, names[i], inputs, targetSpec);
    }
    return curves;
  }

  private LinkedHashMap<String, Interpolator1D> getInterpolators(final CurveDataBundle[] curves) {
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>(curves.length + (curves.length / 4));
    for (final CurveDataBundle curve : curves) {
      interpolators.put(curve._name, curve._interpolator);
    }
    return interpolators;
  }

  private int getTotalStrips(final CurveDataBundle[] curves) {
    int totalStrips = 0;
    for (final CurveDataBundle curve : curves) {
      totalStrips += curve._strips.length;
    }
    return totalStrips;
  }

  private Set<ComputedValue> getResults(final CurveDataBundle[] curves, final ComputationTargetSpecification targetSpec, final ValueProperties.Builder commonProperties) {
    final Set<ComputedValue> results = new HashSet<>();
    for (final CurveDataBundle curve : curves) {
      final ValueProperties curveProperties = commonProperties.withoutAny(CURVE).with(CURVE, curve._name).get();
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SERIES, targetSpec, curveProperties);
      results.add(new ComputedValue(spec, curve._dateCurveMap));
    }
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String absoluteToleranceName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    final String relativeToleranceName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    final String iterationsName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    final String decompositionName = desiredValue.getConstraint(PROPERTY_DECOMPOSITION);
    final String useFiniteDifferenceName = desiredValue.getConstraint(PROPERTY_USE_FINITE_DIFFERENCE);
    final LocalDate startDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(START_DATE_PROPERTY));
    final LocalDate endDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(END_DATE_PROPERTY));
    final MultiCurveCalculationConfig curveCalculationConfig = getCurveCalculationConfigSource().getConfig(curveCalculationConfigName);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final double absoluteTolerance = Double.parseDouble(absoluteToleranceName);
    final double relativeTolerance = Double.parseDouble(relativeToleranceName);
    final int iterations = Integer.parseInt(iterationsName);
    final boolean useFiniteDifference = Boolean.parseBoolean(useFiniteDifferenceName);
    final Decomposition<?> decomposition = DecompositionFactory.getDecomposition(decompositionName);
    final Currency currency = Currency.of(targetSpec.getUniqueId().getValue());
    final CurveDataBundle[] curves = getCurveData(now, getSecurityConverter(executionContext), curveCalculationConfig, inputs, targetSpec);
    final LinkedHashMap<String, Interpolator1D> interpolators = getInterpolators(curves);
    LocalDate valuationDate = startDate;
    final DoubleArrayList nodeTimes = new DoubleArrayList();
    while (!valuationDate.isAfter(endDate)) {
      final YieldCurveBundle knownCurves = getKnownCurves(curveCalculationConfig, targetSpec, inputs);
      final List<InstrumentDerivative> derivatives = new ArrayList<>();
      final DoubleArrayList marketValues = new DoubleArrayList();
      final DoubleArrayList initialRatesGuess = new DoubleArrayList();
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<>();
      for (final CurveDataBundle curve : curves) {
        int nInstruments = 0;
        FixedIncomeStripWithSecurity previousStrip = null;
        nodeTimes.clear();
        for (final CurveStripDataBundle strip : curve._strips) {
          final LocalDateDoubleTimeSeries ts = strip._historicalTimeSeries.getTimeSeries();
          final Double marketValueObject = ts.getValue(valuationDate);
          if (marketValueObject == null) {
            break;
          }
          final double marketValue = marketValueObject;
          InstrumentDerivative derivative = strip._derivative;
          if (derivative != null) {
            if (strip._strip.getInstrumentType() == StripInstrumentType.FUTURE) {
              final InterestRateFutureSecurityDefinition securityDefinition = (InterestRateFutureSecurityDefinition) strip._definition;
              InterestRateFutureTransactionDefinition unitNotional = new InterestRateFutureTransactionDefinition(securityDefinition, now, marketValue, 1);
              unitNotional = unitNotional.withNewNotionalAndTransactionPrice(1, marketValue);
              InstrumentDerivative unitNotionalDerivative = _definitionConverter.convert(strip._strip.getSecurity(), unitNotional, now, strip._curveNames, curve._timeSeries);
              unitNotionalDerivative = unitNotionalDerivative.accept(RateReplacingInterestRateDerivativeVisitor.getInstance(), marketValueObject);
              derivatives.add(unitNotionalDerivative);
              initialRatesGuess.add(1 - marketValue);
            } else {
              derivative = derivative.accept(RateReplacingInterestRateDerivativeVisitor.getInstance(), marketValueObject);
              derivatives.add(derivative);
              initialRatesGuess.add(marketValue);
            }
            final double t = derivative.accept(LAST_TIME_CALCULATOR);
            if (nInstruments > 0 && CompareUtils.closeEquals(nodeTimes.getDouble(nInstruments - 1), t, 1e-12)) {
              throw new OpenGammaRuntimeException("Strip " + strip + " has same maturity as one already added (" + previousStrip + ") - will lead to" +
                  "equal nodes in the curve. Remove one of these strips.");
            }
            nodeTimes.add(Math.abs(t));
            marketValues.add(0.0);
            previousStrip = strip._strip;
            nInstruments++;
          }
        }
        curve._nodeTimes = nodeTimes.toDoubleArray();
        curveNodes.put(curve._name, curve._nodeTimes);
      }
      if (marketValues.size() != getTotalStrips(curves)) {
        s_logger.info("Could not get market values for {}", valuationDate);
        valuationDate = valuationDate.plusDays(1);
        continue;
      }
      try {
        final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues.toDoubleArray(), knownCurves, curveNodes, interpolators,
            useFiniteDifference, new FXMatrix(currency));
        final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(absoluteTolerance, relativeTolerance, iterations, decomposition);
        final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PAR_SPREAD_RATE_CALCULATOR);
        final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderIRSJacobian(data, PAR_SPREAD_RATE_SENSITIVITY_CALCULATOR);
        final double[] fittedYields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess.toDoubleArray())).getData();
        int i = 0;
        for (final CurveDataBundle curve : curves) {
          final int offset = curve._nodes;
          final double[] yields = Arrays.copyOfRange(fittedYields, i, i + offset);
          final YieldCurve yieldCurve = YieldCurve.from(InterpolatedDoublesCurve.from(curve._nodeTimes, yields, curve._interpolator));
          curve._dateCurveMap.put(valuationDate, yieldCurve);
          i += offset;
        }
        valuationDate = valuationDate.plusDays(1);
      } catch (final Exception e) {
        s_logger.error("Could not fit curve on {}", valuationDate);
        valuationDate = valuationDate.plusDays(1);
        continue;
      }
    }
    return getResults(curves, targetSpec, desiredValue.getConstraints().copy());
  }

  @Override
  protected ValueProperties getCurveSeriesProperties() {
    return createValueProperties().withAny(ValuePropertyNames.CURVE).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PAR_RATE_STRING).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE).withAny(DATA_FIELD_PROPERTY).withAny(RESOLUTION_KEY_PROPERTY).withAny(START_DATE_PROPERTY).with(INCLUDE_START_PROPERTY, YES_VALUE, NO_VALUE)
        .withAny(END_DATE_PROPERTY).with(INCLUDE_END_PROPERTY, YES_VALUE, NO_VALUE).get();
  }

  @Override
  protected ValueProperties getCurveSeriesProperties(final String curveCalculationConfigName, final String curveName) {
    return createValueProperties().with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(PROPERTY_DECOMPOSITION).withAny(PROPERTY_USE_FINITE_DIFFERENCE).withAny(DATA_FIELD_PROPERTY).withAny(RESOLUTION_KEY_PROPERTY)
        .withAny(START_DATE_PROPERTY).with(INCLUDE_START_PROPERTY, YES_VALUE, NO_VALUE).withAny(END_DATE_PROPERTY).with(INCLUDE_END_PROPERTY, YES_VALUE, NO_VALUE).get();
  }

  @Override
  protected String getCalculationMethod() {
    return PAR_RATE_STRING;
  }

}
