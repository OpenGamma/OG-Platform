/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Maps;
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
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
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
import com.opengamma.financial.analytics.conversion.YieldCurveFixingSeriesProvider;
import com.opengamma.financial.analytics.ircurve.ConfigDBInterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripIdentifierAndMaturityBuilder;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.MultiCurveFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;

/**
 * Constructs yield curves and the Jacobian from {@link YieldCurveDefinition}s. Multiple curves can
 * be constructed simultaneously using root-finding. The configuration object that control the construction is
 * {@link MultiCurveCalculationConfig}. The root-finder uses present value = 0 as its target, where an appropriate spread
 * is added to the fixed rate or yield of an instrument.
 * @deprecated This function uses configuration objects that have been superseded. Use functions
 * that descend from {@link MultiCurveFunction}.
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
  /** Converts securities to instrument definitions */
  private InterestRateInstrumentTradeOrSecurityConverter _securityConverter;
  /** Converts instrument definitions to derivatives */
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    _securityConverter = new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource, true, executionContext.getComputationTargetResolver()
        .getVersionCorrection());
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties.Builder commonProperties = desiredValue.getConstraints().copy().withoutAny(CURVE);
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String absoluteToleranceName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    final String relativeToleranceName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    final String iterationsName = desiredValue.getConstraint(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    final String decompositionName = desiredValue.getConstraint(PROPERTY_DECOMPOSITION);
    final String useFiniteDifferenceName = desiredValue.getConstraint(PROPERTY_USE_FINITE_DIFFERENCE);
    final LocalDate startDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(START_DATE_PROPERTY));
    final LocalDate endDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(END_DATE_PROPERTY));
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final MultiCurveCalculationConfig curveCalculationConfig = new ConfigDBCurveCalculationConfigSource(configSource).getConfig(curveCalculationConfigName);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ConventionBundleSource conventionBundleSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final YieldCurveFixingSeriesProvider provider = new YieldCurveFixingSeriesProvider(conventionBundleSource);
    
    final Set<ComputedValue> results = new HashSet<>();
    final double absoluteTolerance = Double.parseDouble(absoluteToleranceName);
    final double relativeTolerance = Double.parseDouble(relativeToleranceName);
    final int iterations = Integer.parseInt(iterationsName);
    final boolean useFiniteDifference = Boolean.parseBoolean(useFiniteDifferenceName);
    final Decomposition<?> decomposition = DecompositionFactory.getDecomposition(decompositionName);
    final Currency currency = Currency.of(targetSpec.getUniqueId().getValue());
    final LinkedHashSet<String> curveNames = new LinkedHashSet<>();
    Map<String, YieldCurveDefinition> ycDefs = Maps.newHashMap();
    int totalStrips = 0;
    final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
        OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource(), OpenGammaExecutionContext.getHolidaySource(executionContext));
    for (final String curveName : curveCalculationConfig.getYieldCurveNames()) {
      curveNames.add(curveName);
      totalStrips += getYieldCurveSpecification(inputs, targetSpec, curveName).getStrips().size();
      YieldCurveDefinition ycDef = configSource.getLatestByName(YieldCurveDefinition.class, curveName + "_" + currency.getCode());
      ycDefs.put(curveName, ycDef);
    }
    
    final Map<String, Map<LocalDate, YieldAndDiscountCurve>> curveSeries = new HashMap<>();
    LocalDate valuationDate = startDate;
    ConfigDBInterpolatedYieldCurveSpecificationBuilder ycSpecBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
    
    VAL: //CSIGNORE
    while (!valuationDate.isAfter(endDate)) {
      
      final YieldCurveBundle knownCurves = getKnownCurves(curveCalculationConfig, targetSpec, inputs);
      final List<InstrumentDerivative> derivatives = new ArrayList<>();
      final DoubleArrayList marketValues = new DoubleArrayList();
      final DoubleArrayList initialRatesGuess = new DoubleArrayList();
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<>();
      final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
      final Map<String, Integer> nodesPerCurve = new HashMap<>();
      for (final String curveName : curveNames) {
        
        
        YieldCurveDefinition curveDefinition = ycDefs.get(curveName);
        InterpolatedYieldCurveSpecification ycSpec = ycSpecBuilder.buildCurve(valuationDate, curveDefinition, VersionCorrection.LATEST);
        
        HistoricalTimeSeriesBundle hts = getHts(curveName, inputs);
        final HistoricalTimeSeriesBundle timeSeries = getTimeSeriesBundle(inputs, targetSpec, curveName);
        SnapshotDataBundle marketDataSnapshot = getMarketDataSnapshot(hts, ycSpec);
        if (marketDataSnapshot.getDataPointSet().isEmpty()) {
          valuationDate = valuationDate.plusDays(1);
          continue VAL;
        }
        if (ycSpec.getStrips().size() != marketDataSnapshot.getDataPointSet().size()) {
          valuationDate = valuationDate.plusDays(1);
          s_logger.info("Unable to resolve all curve points for " + curveName + " on " + valuationDate + ". Not producing curve for this date.");
          continue VAL;
        }
        final InterpolatedYieldCurveSpecificationWithSecurities spec = builder.resolveToSecurity(ycSpec, marketDataSnapshot);
        int nInstruments = 0;
        final Interpolator1D interpolator = spec.getInterpolator();
        final HistoricalTimeSeriesBundle marketData = getHistoricalMarketData(inputs, targetSpec, curveName);
        final DoubleArrayList nodeTimes = new DoubleArrayList();
        FixedIncomeStripWithSecurity previousStrip = null;
        for (final FixedIncomeStripWithSecurity strip : spec.getStrips()) {
          //TODO a lot of this can be moved outside the date loop
          final HistoricalTimeSeries historicalTimeSeries = marketData.get(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurityIdentifier());
          if (historicalTimeSeries == null) {
            throw new OpenGammaRuntimeException("Could not get historical time series for " + strip);
          }
          final LocalDateDoubleTimeSeries ts = historicalTimeSeries.getTimeSeries();
          final Double marketValue = ts.getValue(valuationDate);
          if (marketValue == null) {
            break;
          }
          final Security security = strip.getSecurity();
          final String[] curveNamesForSecurity = curveCalculationConfig.getCurveExposureForInstrument(curveName, strip.getInstrumentType());
          final InstrumentDefinition<?> definition = _securityConverter.visit(security);
          final ZonedDateTime valuationDateTime = ZonedDateTime.of(valuationDate, LocalTime.MIDNIGHT, executionContext.getValuationClock().getZone());
          InstrumentDerivative derivative = _definitionConverter.convert(security, definition, valuationDateTime, curveNamesForSecurity, timeSeries);
          if (derivative != null) {
            if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
              final InterestRateFutureSecurityDefinition securityDefinition = (InterestRateFutureSecurityDefinition) definition;
              InterestRateFutureTransactionDefinition unitNotional = new InterestRateFutureTransactionDefinition(securityDefinition, 1, valuationDateTime, marketValue);
              unitNotional = unitNotional.withNewNotionalAndTransactionPrice(1, marketValue);
              InstrumentDerivative unitNotionalDerivative = _definitionConverter.convert(security, unitNotional, valuationDateTime, curveNamesForSecurity, timeSeries);
              unitNotionalDerivative = unitNotionalDerivative.accept(RateReplacingInterestRateDerivativeVisitor.getInstance(), marketValue);
              derivatives.add(unitNotionalDerivative);
              initialRatesGuess.add(1 - marketValue);
            } else {
              derivative = derivative.accept(RateReplacingInterestRateDerivativeVisitor.getInstance(), marketValue);
              derivatives.add(derivative);
              initialRatesGuess.add(marketValue);
            }
            final double t = derivative.accept(LAST_TIME_CALCULATOR);
            if (nInstruments > 0 && CompareUtils.closeEquals(nodeTimes.get(nInstruments - 1), t, 1e-12)) {
              throw new OpenGammaRuntimeException("Strip " + strip + " has same maturity as one already added (" + previousStrip + ") - will lead to" +
                  "equal nodes in the curve. Remove one of these strips.");
            }
            nodeTimes.add(Math.abs(t));
            marketValues.add(0.0);
            previousStrip = strip;
            nInstruments++;
          }
        }
        nodesPerCurve.put(curveName, nInstruments);
        curveNodes.put(curveName, nodeTimes.toDoubleArray());
        interpolators.put(curveName, interpolator);
      }
      if (marketValues.size() != totalStrips) {
        s_logger.info("Could not get market values for {}", valuationDate);
        valuationDate = valuationDate.plusDays(1);
        continue;
      }
      try {
        final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues.toDoubleArray(), knownCurves, curveNodes, interpolators, useFiniteDifference,
            new FXMatrix(currency));
        final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(absoluteTolerance, relativeTolerance, iterations, decomposition);
        final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PAR_SPREAD_RATE_CALCULATOR);
        final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderIRSJacobian(data, PAR_SPREAD_RATE_SENSITIVITY_CALCULATOR);
        final double[] fittedYields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess.toDoubleArray())).getData();
        final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(fittedYields));
        int i = 0;
        for (final String curveName : curveNames) {
          final Integer offset = nodesPerCurve.get(curveName);
          if (offset == null) {
            continue;
          }
          final double[] yields = Arrays.copyOfRange(fittedYields, i, i + offset);
          final YieldCurve yieldCurve = YieldCurve.from(InterpolatedDoublesCurve.from(curveNodes.get(curveName), yields, interpolators.get(curveName)));
          if (curveSeries.containsKey(curveName)) {
            final Map<LocalDate, YieldAndDiscountCurve> dateCurveMap = curveSeries.get(curveName);
            dateCurveMap.put(valuationDate, yieldCurve);
          } else {
            final LinkedHashMap<LocalDate, YieldAndDiscountCurve> dateCurveMap = new LinkedHashMap<>();
            dateCurveMap.put(valuationDate, yieldCurve);
            curveSeries.put(curveName, dateCurveMap);
          }
          i += offset;
        }
        valuationDate = valuationDate.plusDays(1);
      } catch (final Exception e) {
        s_logger.error("Could not fit curve on {}", valuationDate);
        valuationDate = valuationDate.plusDays(1);
        continue;
      }
    }
    for (final String curveName : curveNames) {
      final ValueProperties curveProperties = commonProperties.with(CURVE, curveName).get();
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SERIES, targetSpec, curveProperties);
      results.add(new ComputedValue(spec, curveSeries.get(curveName)));
    }
    return results;
  }

  private SnapshotDataBundle getMarketDataSnapshot(HistoricalTimeSeriesBundle hts, InterpolatedYieldCurveSpecification ycSpec) {
    SnapshotDataBundle dataBundle = new SnapshotDataBundle();
    
    for (FixedIncomeStripWithIdentifier strip : ycSpec.getStrips()) {
      ExternalId id = strip.getSecurity();
      HistoricalTimeSeries timeSeries = hts.get("Market_Value", id);
      Double value = timeSeries.getTimeSeries().getValue(ycSpec.getCurveDate());
      if (value == null) {
        continue;
      }
      dataBundle.setDataPoint(id, value);
    }
    
    return dataBundle;
  }

  private HistoricalTimeSeriesBundle getHts(String curveName, FunctionInputs inputs) {
    for (ComputedValue value : inputs.getAllValues()) {
      ValueSpecification spec = value.getSpecification();
      if (spec.getValueName().equals(YIELD_CURVE_HISTORICAL_TIME_SERIES) && curveName.equals(spec.getProperty("Curve")) && spec.getProperty("Start") != null) {
        return (HistoricalTimeSeriesBundle) value.getValue();
      }
    }
    throw new IllegalStateException("Couldn't find required YCHTS for " + curveName);
  }

  @Override
  protected ValueProperties getCurveSeriesProperties() {
    return createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
        .withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE)
        .withAny(DATA_FIELD_PROPERTY)
        .withAny(RESOLUTION_KEY_PROPERTY)
        .withAny(START_DATE_PROPERTY)
        .with(INCLUDE_START_PROPERTY, YES_VALUE, NO_VALUE)
        .withAny(END_DATE_PROPERTY)
        .with(INCLUDE_END_PROPERTY, YES_VALUE, NO_VALUE)
        .get();
  }

  @Override
  protected ValueProperties getCurveSeriesProperties(final String curveCalculationConfigName, final String curveName) {
    return createValueProperties()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
        .withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE)
        .withAny(DATA_FIELD_PROPERTY)
        .withAny(RESOLUTION_KEY_PROPERTY)
        .withAny(START_DATE_PROPERTY)
        .with(INCLUDE_START_PROPERTY, YES_VALUE, NO_VALUE)
        .withAny(END_DATE_PROPERTY)
        .with(INCLUDE_END_PROPERTY, YES_VALUE, NO_VALUE)
        .get();
  }

  @Override
  protected String getCalculationMethod() {
    return PAR_RATE_STRING;
  }
  
  

}
