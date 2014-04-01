/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValueRequirementNames.FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_SERIES;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.curve.MultiCurveFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * Constructs a single yield curve and its Jacobian from exogenously-supplied yield curves and
 * a {@link FXForwardCurveDefinition} and {@link FXForwardCurveSpecification}.
 * @deprecated This function uses configuration objects that have been superseded. Use functions
 * that descend from {@link MultiCurveFunction}. Curves that use FX forwards directly in
 * {@link CurveDefinition} (see {@link FXForwardNode}) are constructed in these classes.
 */
@Deprecated
public class FXImpliedYieldCurveSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property name for the calculation method */
  public static final String FX_IMPLIED = "FXImplied";
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXImpliedYieldCurveSeriesFunction.class);
  /** Calculates the par rate */
  private static final ParRateCalculator PAR_RATE_CALCULATOR = ParRateCalculator.getInstance();
  /** Calculates the sensitivity of the par rate to the curves */
  private static final ParRateCurveSensitivityCalculator PAR_RATE_SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();
  /** The business day convention used for FX forward dates computation **/
  private static final BusinessDayConvention MOD_FOL = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDBCurveCalculationConfigSource.reinitOnChanges(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String domesticCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Currency domesticCurrency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
    Object foreignCurveObject = null;
    Currency foreignCurrency = null;
    String foreignCurveName = null;
    for (final ComputedValue values : inputs.getAllValues()) {
      final ValueSpecification specification = values.getSpecification();
      if (specification.getValueName().equals(ValueRequirementNames.YIELD_CURVE_SERIES)) {
        foreignCurveObject = values.getValue();
        foreignCurrency = Currency.of(specification.getTargetSpecification().getUniqueId().getValue());
        foreignCurveName = specification.getProperty(ValuePropertyNames.CURVE);
        break;
      }
    }
    if (foreignCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get foreign yield curve");
    }
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String absoluteToleranceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    final double absoluteTolerance = Double.parseDouble(absoluteToleranceName);
    final String relativeToleranceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    final double relativeTolerance = Double.parseDouble(relativeToleranceName);
    final String iterationsName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    final int iterations = Integer.parseInt(iterationsName);
    final String decompositionName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION);
    final String useFiniteDifferenceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE);
    final boolean useFiniteDifference = Boolean.parseBoolean(useFiniteDifferenceName);
    final Decomposition<?> decomposition = DecompositionFactory.getDecomposition(decompositionName);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBFXForwardCurveDefinitionSource fxCurveDefinitionSource = new ConfigDBFXForwardCurveDefinitionSource(configSource);
    final ConfigDBFXForwardCurveSpecificationSource fxCurveSpecificationSource = new ConfigDBFXForwardCurveSpecificationSource(configSource);
    final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final Currency baseCurrency = currencyPairs.getCurrencyPair(domesticCurrency, foreignCurrency).getBase();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    boolean invertFXQuotes;
    if (baseCurrency.equals(foreignCurrency)) {
      invertFXQuotes = false;
    } else {
      invertFXQuotes = true;
    }
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(domesticCurrency, foreignCurrency);
    final FXForwardCurveDefinition definition = fxCurveDefinitionSource.getDefinition(domesticCurveName, currencyPair.toString());
    if (definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + domesticCurveName + " for target " + currencyPair);
    }
    final FXForwardCurveSpecification specification = fxCurveSpecificationSource.getSpecification(domesticCurveName, currencyPair.toString());
    if (specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + domesticCurveName + " for target " + currencyPair);
    }
    final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
    final HistoricalTimeSeriesBundle timeSeriesBundle = getTimeSeriesBundle(inputs, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencyPair), domesticCurveName);
    final HistoricalTimeSeries spotTimeSeries = timeSeriesBundle.get(provider.getMarketDataField(), provider.getSpotInstrument());
    if (spotTimeSeries == null) {
      throw new OpenGammaRuntimeException("Could not get spot FX time series");
    }
    final LocalDateDoubleTimeSeries spotTS = spotTimeSeries.getTimeSeries();
    final Map<LocalDate, YieldAndDiscountCurve> foreignCurves = (Map<LocalDate, YieldAndDiscountCurve>) foreignCurveObject;
    final Map<LocalDate, YieldAndDiscountCurve> domesticCurves = new LinkedHashMap<>();
    final Calendar calendar = CalendarUtils.getCalendar(holidaySource, domesticCurrency, foreignCurrency);
    final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
    final FXSpotConvention fxSpotConvention = (FXSpotConvention) conventionSource.getConvention(ExternalId.of("CONVENTION", "FX Spot"));
    final int spotLag = fxSpotConvention.getSettlementDays();
    final boolean isRegular = specification.isMarketQuoteConvention();
    final ExternalId conventionSettlementRegion = fxSpotConvention.getSettlementRegion();
    final String fullDomesticCurveName = domesticCurveName + "_" + domesticCurrency.getCode();
    final String fullForeignCurveName = foreignCurveName + "_" + foreignCurrency.getCode();
    for (final Map.Entry<LocalDate, YieldAndDiscountCurve> entry : foreignCurves.entrySet()) {
      final LocalDate valuationDate = entry.getKey();
      try {
        final ZonedDateTime valuationDateTime = ZonedDateTime.of(valuationDate, LocalTime.MIDNIGHT, executionContext.getValuationClock().getZone());
        final Double spotValue = spotTS.getValue(valuationDate);
        if (spotValue == null) {
          continue;
        }
        final double spotFX = invertFXQuotes ? 1 / spotValue : spotValue;
        final YieldAndDiscountCurve foreignCurve = entry.getValue();
        final DoubleArrayList marketValues = new DoubleArrayList();
        final DoubleArrayList nodeTimes = new DoubleArrayList();
        final DoubleArrayList initialRatesGuess = new DoubleArrayList();
        final List<InstrumentDerivative> derivatives = new ArrayList<>();
        int nInstruments = 0;
        ZonedDateTime spotDate;
        if (spotLag == 0 && conventionSettlementRegion == null) {
          spotDate = valuationDateTime;
        } else {
          spotDate = ScheduleCalculator.getAdjustedDate(valuationDateTime, spotLag, calendar);
        }
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(valuationDate, tenor);
          final HistoricalTimeSeries forwardFXTS = timeSeriesBundle.get(provider.getMarketDataField(), identifier);
          if (forwardFXTS == null) {
            throw new OpenGammaRuntimeException("Could not get time series for " + identifier);
          }
          final LocalDateDoubleTimeSeries forwardTS = forwardFXTS.getTimeSeries();
          final ZonedDateTime paymentDate;
  
          if (spotLag == 0 && conventionSettlementRegion == null) {
            paymentDate = spotDate.plus(tenor.getPeriod()); //This preserves the old behaviour that ignored holidays and settlement days.
          } else {
            paymentDate = ScheduleCalculator.getAdjustedDate(spotDate, tenor.getPeriod(), MOD_FOL, calendar, false);
          }
  
          final Double forwardValue = forwardTS.getValue(valuationDate);
          if (forwardValue == null) {
            break;
          }
          double forwardFX;
          switch (specification.getQuoteType()) {
            case Points:
              forwardFX = isRegular ? spotFX + forwardValue : 1 / (spotFX + forwardValue);
              break;
            case Outright:
              forwardFX = isRegular ? forwardValue : 1 / forwardValue;
              break;
            default:
              throw new OpenGammaRuntimeException("Cannot handle quote type " + specification.getQuoteType());
          }
          forwardFX = invertFXQuotes ? 1 / forwardFX : forwardFX;
          final double quotedSpotFX = invertFXQuotes ? 1 / spotFX : spotFX;
          final double paymentTime = TimeCalculator.getTimeBetween(valuationDateTime, paymentDate);
          derivatives.add(getFXForward(domesticCurrency, foreignCurrency, paymentTime, quotedSpotFX, forwardFX, fullDomesticCurveName, fullForeignCurveName));
          marketValues.add(forwardFX);
          nodeTimes.add(paymentTime);
          if (nInstruments > 1 && CompareUtils.closeEquals(nodeTimes.get(nInstruments - 1), paymentTime, 1e-12)) {
            throw new OpenGammaRuntimeException("FX forward with tenor " + tenor + " has already been added - will lead to equal nodes in the curve. Remove one of these tenors.");
          }
          nInstruments++;
          initialRatesGuess.add(0.02);
        }
        if (marketValues.size() == 0) {
          s_logger.error("Could not get market values for {}", valuationDate);
          continue;
        }
        final YieldCurveBundle knownCurve = new YieldCurveBundle(new String[] {fullForeignCurveName }, new YieldAndDiscountCurve[] {foreignCurve });
        final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<>();
        curveKnots.put(fullDomesticCurveName, nodeTimes.toDoubleArray());
        final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<>();
        final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
        final CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName,
            rightExtrapolatorName);
        curveNodes.put(fullDomesticCurveName, nodeTimes.toDoubleArray());
        interpolators.put(fullDomesticCurveName, interpolator);
        final FXMatrix fxMatrix = new FXMatrix();
        fxMatrix.addCurrency(foreignCurrency, domesticCurrency, invertFXQuotes ? spotFX : 1 / spotFX);
        final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues.toDoubleArray(), knownCurve, curveNodes,
            interpolators, useFiniteDifference, fxMatrix);
        final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(absoluteTolerance, relativeTolerance, iterations, decomposition);
        final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PAR_RATE_CALCULATOR);
        final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, PAR_RATE_SENSITIVITY_CALCULATOR);
        final double[] fittedYields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess.toDoubleArray())).getData();
        final YieldCurve curve = YieldCurve.from(InterpolatedDoublesCurve.from(nodeTimes.toDoubleArray(), fittedYields, interpolator));
  
        domesticCurves.put(valuationDate, curve);
      } catch (Exception e) {
        s_logger.error("Exception building domestic curve for valuation date " + valuationDate, e);
      }
    }
    final Set<ComputedValue> result = new HashSet<>();
    result.add(new ComputedValue(new ValueSpecification(YIELD_CURVE_SERIES, targetSpec, desiredValue.getConstraints().copy().get()), domesticCurves));
    return result;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties curveSeriesProperties = getCurveSeriesProperties();
    final ValueSpecification curve = new ValueSpecification(YIELD_CURVE_SERIES, target.toSpecification(), curveSeriesProperties);
    return Sets.newHashSet(curve);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigNames = constraints.getValues(CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String domesticCurveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig domesticCurveCalculationConfig = curveCalculationConfigSource.getConfig(domesticCurveCalculationConfigName);
    if (domesticCurveCalculationConfig == null) {
      s_logger.error("Could not get domestic curve calculation config called {}", domesticCurveCalculationConfigName);
      return null;
    }
    if (domesticCurveCalculationConfig.getExogenousConfigData() == null) {
      s_logger.error("Need an externally-supplied curve to imply data; tried {}", domesticCurveCalculationConfigName);
      return null;
    }
    if (domesticCurveCalculationConfig.getYieldCurveNames().length != 1) {
      s_logger.error("Can only handle one curve at the moment");
      return null;
    }
    if (!domesticCurveCalculationConfig.getTarget().equals(target.toSpecification())) {
      s_logger.info("Invalid target, was {} - expected {}", target, domesticCurveCalculationConfig.getTarget());
      return null;
    }
    final Map<String, String[]> exogenousConfigs = domesticCurveCalculationConfig.getExogenousConfigData();
    if (exogenousConfigs.size() != 1) {
      s_logger.error("Can only handle curves with one foreign curve config");
      return null;
    }
    if (!domesticCurveCalculationConfig.getCalculationMethod().equals(FX_IMPLIED)) {
      return null;
    }
    ValueProperties.Builder seriesConstraints = null;
    Set<String> values = desiredValue.getConstraints().getValues(DATA_FIELD_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      seriesConstraints = desiredValue.getConstraints().copy().with(DATA_FIELD_PROPERTY, MarketDataRequirementNames.MARKET_VALUE);
    } else if (values.size() > 1) {
      seriesConstraints = desiredValue.getConstraints().copy().withoutAny(DATA_FIELD_PROPERTY)
          .with(DATA_FIELD_PROPERTY, values.iterator().next());
    }
    values = desiredValue.getConstraints().getValues(RESOLUTION_KEY_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(RESOLUTION_KEY_PROPERTY, "");
    } else if (values.size() > 1) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.withoutAny(RESOLUTION_KEY_PROPERTY).with(RESOLUTION_KEY_PROPERTY, values.iterator().next());
    }
    values = desiredValue.getConstraints().getValues(START_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(START_DATE_PROPERTY, "Null");
    }
    values = desiredValue.getConstraints().getValues(INCLUDE_START_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(INCLUDE_START_PROPERTY, YES_VALUE);
    }
    values = desiredValue.getConstraints().getValues(END_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(END_DATE_PROPERTY, "Now");
    }
    values = desiredValue.getConstraints().getValues(INCLUDE_END_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(INCLUDE_END_PROPERTY, YES_VALUE);
    }
    if (seriesConstraints != null) {
      Set<String> propertyValue = constraints.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
      } else {
        seriesConstraints.with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, propertyValue);
      }
      propertyValue = constraints.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
      } else {
        seriesConstraints.with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, propertyValue);
      }
      propertyValue = constraints.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
      } else {
        seriesConstraints.with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, propertyValue);
      }
      propertyValue = constraints.getValues(PROPERTY_DECOMPOSITION);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_DECOMPOSITION);
      } else {
        seriesConstraints.with(PROPERTY_DECOMPOSITION, propertyValue);
      }
      propertyValue = constraints.getValues(PROPERTY_USE_FINITE_DIFFERENCE);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_USE_FINITE_DIFFERENCE);
      } else {
        seriesConstraints.with(PROPERTY_USE_FINITE_DIFFERENCE, propertyValue);
      }
      propertyValue = constraints.getValues(X_INTERPOLATOR_NAME);
      if (propertyValue == null) {
        seriesConstraints.withAny(X_INTERPOLATOR_NAME);
      } else {
        seriesConstraints.with(X_INTERPOLATOR_NAME, propertyValue);
      }
      propertyValue = constraints.getValues(LEFT_X_EXTRAPOLATOR_NAME);
      if (propertyValue == null) {
        seriesConstraints.withAny(LEFT_X_EXTRAPOLATOR_NAME);
      } else {
        seriesConstraints.with(LEFT_X_EXTRAPOLATOR_NAME, propertyValue);
      }
      propertyValue = constraints.getValues(RIGHT_X_EXTRAPOLATOR_NAME);
      if (propertyValue == null) {
        seriesConstraints.withAny(RIGHT_X_EXTRAPOLATOR_NAME);
      } else {
        seriesConstraints.with(RIGHT_X_EXTRAPOLATOR_NAME, propertyValue);
      }
      return Collections.singleton(new ValueRequirement(YIELD_CURVE_SERIES, target.toSpecification(), seriesConstraints.get()));
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
    final Set<String> interpolatorName = constraints.getValues(X_INTERPOLATOR_NAME);
    if (interpolatorName == null || interpolatorName.size() != 1) {
      return null;
    }
    final Set<String> leftExtrapolatorName = constraints.getValues(LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorName == null || leftExtrapolatorName.size() != 1) {
      return null;
    }
    final Set<String> rightExtrapolatorName = constraints.getValues(RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorName == null || rightExtrapolatorName.size() != 1) {
      return null;
    }
    final ConfigDBFXForwardCurveDefinitionSource fxCurveDefinitionSource = new ConfigDBFXForwardCurveDefinitionSource(configSource);
    final ConfigDBFXForwardCurveSpecificationSource fxCurveSpecificationSource = new ConfigDBFXForwardCurveSpecificationSource(configSource);
    final Map.Entry<String, String[]> foreignCurveConfigNames = exogenousConfigs.entrySet().iterator().next();
    final MultiCurveCalculationConfig foreignConfig = curveCalculationConfigSource.getConfig(foreignCurveConfigNames.getKey());
    if (foreignConfig == null) {
      s_logger.error("Foreign config was null; tried {}", foreignCurveConfigNames.getKey());
      return null;
    }
    final ComputationTargetSpecification foreignCurrencySpec = foreignConfig.getTarget();
    if (!foreignCurrencySpec.getType().isTargetType(ComputationTargetType.CURRENCY)) {
      s_logger.error("Can only handle curves with currencies as ids at the moment");
      return null;
    }
    final String domesticCurveName = domesticCurveCalculationConfig.getYieldCurveNames()[0];
    final Currency domesticCurrency = target.getValue(ComputationTargetType.CURRENCY);
    final Set<ValueRequirement> requirements = new HashSet<>();
    final Currency foreignCurrency = ComputationTargetType.CURRENCY.resolve(foreignCurrencySpec.getUniqueId());
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(domesticCurrency, foreignCurrency);
    final FXForwardCurveDefinition definition = fxCurveDefinitionSource.getDefinition(domesticCurveName, currencyPair.toString());
    if (definition == null) {
      s_logger.error("Couldn't find FX forward curve definition called " + domesticCurveName + " with target " + currencyPair);
      return null;
    }
    final FXForwardCurveSpecification fxForwardCurveSpec = fxCurveSpecificationSource.getSpecification(domesticCurveName, currencyPair.toString());
    if (fxForwardCurveSpec == null) {
      s_logger.error("Couldn't find FX forward curve specification called " + domesticCurveName + " with target " + currencyPair);
      return null;
    }
    final ValueProperties fxForwardCurveProperties = getFXForwardCurveProperties(domesticCurveName, constraints);
    final ValueProperties fxForwardCurveSeriesProperties = fxForwardCurveProperties.copy()
        .with(CURVE_CALCULATION_CONFIG, domesticCurveCalculationConfigName)
        .withOptional(CURVE_CALCULATION_CONFIG)
        .get();
    final String foreignCurveName = foreignCurveConfigNames.getValue()[0];
    final ValueProperties foreignCurveProperties = getForeignCurveProperties(foreignConfig, foreignCurveName, constraints);
    final FXForwardCurveInstrumentProvider provider = fxForwardCurveSpec.getCurveInstrumentProvider();
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(foreignCurrency);
    requirements.add(new ValueRequirement(FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencyPair),
        fxForwardCurveSeriesProperties));
    requirements.add(new ValueRequirement(YIELD_CURVE_SERIES, currencyTarget, foreignCurveProperties));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 1) {
      final ValueSpecification input = Iterables.getOnlyElement(inputs.entrySet()).getKey();
      if (YIELD_CURVE_SERIES.equals(input.getValueName())) {
        // Use the substituted result
        return Collections.singleton(input);
      }
    }
    final Set<ValueSpecification> results = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    String curveCalculationConfigName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement value = entry.getValue();
      if (value.getValueName().equals(FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES)) {
        if (curveCalculationConfigName == null) {
          curveCalculationConfigName = value.getConstraint(CURVE_CALCULATION_CONFIG);
        }
        final String curveName = value.getConstraint(CURVE);
        final ValueProperties curveProperties = getCurveSeriesProperties(curveCalculationConfigName, curveName);
        final ValueSpecification spec = new ValueSpecification(YIELD_CURVE_SERIES, targetSpec, curveProperties);
        results.add(spec);
      }
    }
    if (curveCalculationConfigName == null) {
      return null;
    }
    return results;
  }

  /**
   * Gets the properties for the FX forward curve
   * @param forwardCurveName The forward curve name
   * @return The forward curve properties
   */
  private static ValueProperties getFXForwardCurveProperties(final String forwardCurveName, final ValueProperties curveConstraints) {
    return ValueProperties.builder()
        .with(CURVE, forwardCurveName)
        .with(DATA_FIELD_PROPERTY, curveConstraints.getValues(DATA_FIELD_PROPERTY))
        .with(RESOLUTION_KEY_PROPERTY, curveConstraints.getValues(RESOLUTION_KEY_PROPERTY))
        .with(START_DATE_PROPERTY, curveConstraints.getValues(START_DATE_PROPERTY))
        .with(INCLUDE_START_PROPERTY, curveConstraints.getValues(INCLUDE_START_PROPERTY))
        .with(END_DATE_PROPERTY, curveConstraints.getValues(END_DATE_PROPERTY))
        .with(INCLUDE_END_PROPERTY, curveConstraints.getValues(INCLUDE_END_PROPERTY))
        .get();
  }

  /**
   * Gets the properties for the foreign curve i.e. the fixed yield curve that is being used to imply the yield curve.
   * @param foreignConfig The foreign curve configuration name
   * @param foreignCurveName The foreign curve name
   * @return The foreign curve properties
   */
  private static ValueProperties getForeignCurveProperties(final MultiCurveCalculationConfig foreignConfig, final String foreignCurveName,
      final ValueProperties curveConstraints) {
    return ValueProperties.builder()
        .with(CURVE, foreignCurveName)
        .with(CURVE_CALCULATION_CONFIG, foreignConfig.getCalculationConfigName())
        .with(CURVE_CALCULATION_METHOD, foreignConfig.getCalculationMethod())
        .with(DATA_FIELD_PROPERTY, curveConstraints.getValues(DATA_FIELD_PROPERTY))
        .with(RESOLUTION_KEY_PROPERTY, curveConstraints.getValues(RESOLUTION_KEY_PROPERTY))
        .with(START_DATE_PROPERTY, curveConstraints.getValues(START_DATE_PROPERTY))
        .with(INCLUDE_START_PROPERTY, curveConstraints.getValues(INCLUDE_START_PROPERTY))
        .with(END_DATE_PROPERTY, curveConstraints.getValues(END_DATE_PROPERTY))
        .with(INCLUDE_END_PROPERTY, curveConstraints.getValues(INCLUDE_END_PROPERTY))
        .get();
  }

  /**
   * Gets the properties of the implied yield curve with no values set.
   * @return The properties
   */
  private ValueProperties getCurveSeriesProperties() {
    return createValueProperties()
        .with(CURVE_CALCULATION_METHOD, FX_IMPLIED)
        .withAny(CURVE)
        .withAny(CURVE_CALCULATION_CONFIG)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
        .withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE)
        .withAny(X_INTERPOLATOR_NAME)
        .withAny(LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(DATA_FIELD_PROPERTY)
        .withAny(RESOLUTION_KEY_PROPERTY)
        .withAny(START_DATE_PROPERTY)
        .with(INCLUDE_START_PROPERTY, YES_VALUE, NO_VALUE)
        .withAny(END_DATE_PROPERTY)
        .with(INCLUDE_END_PROPERTY, YES_VALUE, NO_VALUE)
        .get();
  }

  /**
   * Gets the properties of the implied yield curve with no values set.
   * @return The properties
   */
  private ValueProperties getCurveSeriesProperties(final String curveCalculationConfigName, final String curveName) {
    return createValueProperties()
        .with(CURVE_CALCULATION_METHOD, FX_IMPLIED)
        .with(CURVE, curveName)
        .with(CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
        .withAny(PROPERTY_DECOMPOSITION)
        .withAny(PROPERTY_USE_FINITE_DIFFERENCE)
        .withAny(X_INTERPOLATOR_NAME)
        .withAny(LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(DATA_FIELD_PROPERTY)
        .withAny(RESOLUTION_KEY_PROPERTY)
        .withAny(START_DATE_PROPERTY)
        .with(INCLUDE_START_PROPERTY, YES_VALUE, NO_VALUE)
        .withAny(END_DATE_PROPERTY)
        .with(INCLUDE_END_PROPERTY, YES_VALUE, NO_VALUE)
        .get();
  }

  /**
   * Gets the bundle containing historical values for the FX forward curve.
   * @param inputs The inputs
   * @param targetSpec The specification of the historical data
   * @param curveName The curve name
   * @return The bundle
   * @throws OpenGammaRuntimeException if the bundle is not present in the inputs
   */
  protected HistoricalTimeSeriesBundle getTimeSeriesBundle(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement timeSeriesRequirement = new ValueRequirement(FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES, targetSpec, ValueProperties.with(
        CURVE, curveName).get());
    final Object timeSeriesObject = inputs.getValue(timeSeriesRequirement);
    if (timeSeriesObject == null) {
      throw new OpenGammaRuntimeException("Could not get conversion time series for requirement " + timeSeriesRequirement);
    }
    return (HistoricalTimeSeriesBundle) timeSeriesObject;
  }

  /**
   * @param ccy1 The domestic currency
   * @param ccy2 The foreign currency
   * @param paymentTime The payment time
   * @param spotFX The spot FX rate
   * @param forwardFX The forward FX rate
   * @param curveName1 The domestic curve name
   * @param curveName2 The foreign curve name
   * @return
   */
  //TODO determine domestic and notional from dominance data
  private static ForexForward getFXForward(final Currency ccy1, final Currency ccy2, final double paymentTime, final double spotFX, final double forwardFX,
      final String curveName1, final String curveName2) {
    final PaymentFixed paymentCurrency1 = new PaymentFixed(ccy1, paymentTime, 1, curveName1);
    final PaymentFixed paymentCurrency2 = new PaymentFixed(ccy2, paymentTime, -1. / forwardFX, curveName2);
    return new ForexForward(paymentCurrency1, paymentCurrency2, spotFX);
  }
}
