/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

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
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCouponSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
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
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated @see MultiYieldCurveFunction
 */
@Deprecated
public class MarketInstrumentImpliedYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MarketInstrumentImpliedYieldCurveFunction.class);

  private static final LastTimeCalculator LAST_DATE_CALCULATOR = LastTimeCalculator.getInstance();

  private static final String RESULT_PROPERTY_TYPE = "Type";
  private static final String REQUIREMENT_PROPERTY_TYPE = ValuePropertyNames.OUTPUT_RESERVED_PREFIX + RESULT_PROPERTY_TYPE;
  private static final String TYPE_FORWARD = "Forward";
  private static final String TYPE_FUNDING = "Funding";

  /** Label setting this function to use the par rate of the instruments in root-finding */
  public static final String PAR_RATE_STRING = "ParRate";
  /** Label setting this function to use the present value of the instruments in root-finding */
  public static final String PRESENT_VALUE_STRING = "PresentValue";

  private final InstrumentDerivativeVisitor<YieldCurveBundle, Double> _calculator;
  private final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> _sensitivityCalculator;
  private final PresentValueCouponSensitivityCalculator _couponSensitivityCalculator;
  private final String _calculationType;
  private final boolean _calcTypeParRate;

  private FixedIncomeConverterDataProvider _definitionConverter;

  public MarketInstrumentImpliedYieldCurveFunction(final String calculationType) {
    _calculationType = calculationType;
    if (calculationType.equals(PAR_RATE_STRING)) {
      _calculator = ParRateCalculator.getInstance();
      _sensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();
      _couponSensitivityCalculator = null;
      _calcTypeParRate = true;
    } else if (calculationType.equals(PRESENT_VALUE_STRING)) {
      _calculator = PresentValueCalculator.getInstance();
      _sensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();
      _couponSensitivityCalculator = PresentValueCouponSensitivityCalculator.getInstance();
      _calcTypeParRate = false;
    } else {
      throw new IllegalArgumentException("Unrecognized calculator type: " + calculationType + ". In order of preference, try ParRate then PresentValue");
    }
  }

  protected String getCalculationType() {
    return _calculationType;
  }

  protected InstrumentDerivativeVisitor<YieldCurveBundle, Double> getCalculator() {
    return _calculator;
  }

  protected InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> getSensitivityCalculator() {
    return _sensitivityCalculator;
  }

  protected PresentValueCouponSensitivityCalculator getCouponSensitivityCalculator() {
    return _couponSensitivityCalculator;
  }

  protected InterestRateInstrumentTradeOrSecurityConverter getSecurityConverter(final FunctionExecutionContext context) {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    return new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource, true, context.getComputationTargetResolver().getVersionCorrection());
  }

  protected FixedIncomeConverterDataProvider getDefinitionConverter() {
    return _definitionConverter;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    if (holidaySource == null) {
      throw new UnsupportedOperationException("A holiday source is required");
    }
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    if (regionSource == null) {
      throw new UnsupportedOperationException("A region source is required");
    }
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    if (conventionSource == null) {
      throw new UnsupportedOperationException("A convention bundle source is required");
    }
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    if (securitySource == null) {
      throw new UnsupportedOperationException("A security source is required");
    }
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    if (timeSeriesResolver == null) {
      throw new UnsupportedOperationException("A historical time series resolver is required");
    }
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(4);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, createValueProperties().withAny(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, getCalculationType()).withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .with(RESULT_PROPERTY_TYPE, TYPE_FORWARD).get()));
    results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, createValueProperties().withAny(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, getCalculationType()).withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE).withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .with(RESULT_PROPERTY_TYPE, TYPE_FUNDING).get()));
    results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, createValueProperties().withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, getCalculationType()).get()));
    if (getCalculationType().equals(PRESENT_VALUE_STRING)) {
      results.add(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, createValueProperties().withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
          .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get()));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String forwardCurveName;
    final String fundingCurveName;
    if (ValueRequirementNames.YIELD_CURVE.equals(desiredValue.getValueName())) {
      final Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
      if ((curveNames == null) || (curveNames.size() != 1)) {
        return null;
      }
      final String curveName = curveNames.iterator().next();
      final Set<String> fundingCurveNames = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      if ((fundingCurveNames == null) || fundingCurveNames.isEmpty()) {
        fundingCurveName = curveName;
      } else {
        if (fundingCurveNames.size() != 1) {
          return null;
        }
        fundingCurveName = fundingCurveNames.iterator().next();
      }
      final Set<String> forwardCurveNames = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      if ((forwardCurveNames == null) || forwardCurveNames.isEmpty()) {
        forwardCurveName = curveName;
      } else {
        if (forwardCurveNames.size() != 1) {
          return null;
        }
        forwardCurveName = forwardCurveNames.iterator().next();
      }
    } else {
      // Jacobian and Coupon sensitivities must specify a funding and forward curve (possibly the same name)
      final Set<String> fundingCurveNames = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      if ((fundingCurveNames == null) || (fundingCurveNames.size() != 1)) {
        return null;
      }
      final Set<String> forwardCurveNames = desiredValue.getConstraints().getValues(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      if ((forwardCurveNames == null) || (forwardCurveNames.size() != 1)) {
        return null;
      }
      fundingCurveName = fundingCurveNames.iterator().next();
      forwardCurveName = forwardCurveNames.iterator().next();
    }
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, fundingCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).get()));
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_DATA, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, fundingCurveName).get()));
    } else {
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_DATA, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, fundingCurveName)
          .withOptional(REQUIREMENT_PROPERTY_TYPE).with(REQUIREMENT_PROPERTY_TYPE, TYPE_FUNDING).get()));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_DATA, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, forwardCurveName)
          .withOptional(REQUIREMENT_PROPERTY_TYPE).with(REQUIREMENT_PROPERTY_TYPE, TYPE_FORWARD).get()));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, ValueProperties
          .with(ValuePropertyNames.CURVE, forwardCurveName)
          .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).get()));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String forwardCurveName = null;
    String fundingCurveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE_DATA.equals(input.getKey().getValueName())) {
        final String curveName = input.getKey().getProperty(ValuePropertyNames.CURVE);
        final String type = input.getValue().getConstraint(REQUIREMENT_PROPERTY_TYPE);
        if (type == null) {
          assert forwardCurveName == null;
          assert fundingCurveName == null;
          forwardCurveName = curveName;
          fundingCurveName = curveName;
        } else {
          if (TYPE_FORWARD.equals(type)) {
            assert forwardCurveName == null;
            forwardCurveName = curveName;
          } else {
            assert TYPE_FUNDING.equals(type);
            assert fundingCurveName == null;
            fundingCurveName = curveName;
          }
        }
      }
    }
    assert forwardCurveName != null;
    assert fundingCurveName != null;
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(4);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, getCalculationType())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName);
    results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties.get()));
    if (getCalculationType().equals(PRESENT_VALUE_STRING)) {
      results.add(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, createValueProperties().withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
          .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE).get()));
    }
    results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties.with(ValuePropertyNames.CURVE, forwardCurveName).get()));
    if (!forwardCurveName.equals(fundingCurveName)) {
      results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties.withoutAny(ValuePropertyNames.CURVE).with(ValuePropertyNames.CURVE, fundingCurveName).get()));
    }
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    String forwardCurveName = null;
    String fundingCurveName = null;
    boolean createForward = false;
    boolean createFunding = false;
    boolean createJacobian = false;
    boolean createSensitivities = false;
    for (final ValueRequirement desiredValue : desiredValues) {
      if (forwardCurveName == null) {
        forwardCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FORWARD_CURVE);
        fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      }
      if (ValueRequirementNames.YIELD_CURVE.equals(desiredValue.getValueName())) {
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        if (curveName.equals(forwardCurveName)) {
          assert !createForward;
          createForward = true;
        }
        if (curveName.equals(fundingCurveName)) {
          assert !createFunding;
          createFunding = true;
        }
      } else if (ValueRequirementNames.YIELD_CURVE_JACOBIAN.equals(desiredValue.getValueName())) {
        assert !createJacobian;
        createJacobian = true;
      } else if (ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY.equals(desiredValue.getValueName())) {
        assert !createSensitivities;
        createSensitivities = true;
      } else {
        assert false;
      }
    }
    assert forwardCurveName != null;
    assert fundingCurveName != null;
    YieldCurveData fundingCurveData = null;
    YieldCurveData forwardCurveData = null;
    HistoricalTimeSeriesBundle fundingTimeSeries = null;
    HistoricalTimeSeriesBundle forwardTimeSeries = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      final String curveName = input.getSpecification().getProperty(ValuePropertyNames.CURVE);
      if (ValueRequirementNames.YIELD_CURVE_DATA.equals(input.getSpecification().getValueName())) {
        if (curveName.equals(fundingCurveName)) {
          assert fundingCurveData == null;
          fundingCurveData = (YieldCurveData) input.getValue();
        }
        if (curveName.equals(forwardCurveName)) {
          assert forwardCurveData == null;
          forwardCurveData = (YieldCurveData) input.getValue();
        }
      } else if (ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES.equals(input.getSpecification().getValueName())) {
        if (curveName.equals(fundingCurveName)) {
          assert fundingTimeSeries == null;
          fundingTimeSeries = (HistoricalTimeSeriesBundle) input.getValue();
        }
        if (curveName.equals(forwardCurveName)) {
          assert forwardTimeSeries == null;
          forwardTimeSeries = (HistoricalTimeSeriesBundle) input.getValue();
        }
      }
    }
    assert fundingCurveData != null;
    assert forwardCurveData != null;
    if (forwardCurveName.equals(fundingCurveName)) {
      return execute(executionContext,
                     target.toSpecification(),
                     forwardCurveName,
                     forwardCurveData,
                     forwardTimeSeries,
                     createForward,
                     createJacobian,
                     createSensitivities);
    }
    return execute(executionContext,
                   target.toSpecification(),
                   forwardCurveName,
                   forwardCurveData,
                   forwardTimeSeries,
                   fundingCurveName,
                   fundingCurveData,
                   fundingTimeSeries,
                   createForward,
                   createFunding,
                   createJacobian,
                   createSensitivities);
  }

  private static Interpolator1D getInterpolator(final InterpolatedYieldCurveSpecificationWithSecurities specification) {
    return specification.getInterpolator();
  }

  private Set<ComputedValue> execute(FunctionExecutionContext executionContext,
                                     ComputationTargetSpecification targetSpec,
                                     String curveName,
                                     YieldCurveData curveData,
                                     HistoricalTimeSeriesBundle timeSeries,
                                     boolean createYieldCurve,
                                     boolean createJacobian,
                                     boolean createSensitivities) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final List<InstrumentDerivative> derivatives = new ArrayList<>();
    final int n = curveData.getCurveSpecification().getStrips().size();
    final double[] initialRatesGuess = new double[n];
    final double[] nodeTimes = new double[n];
    final double[] marketValues = new double[n];
    int i = 0;
    final InterestRateInstrumentTradeOrSecurityConverter securityConverter = getSecurityConverter(executionContext);
    for (final FixedIncomeStripWithSecurity strip : curveData.getCurveSpecification().getStrips()) {
      Double marketValue = curveData.getDataPoint(strip.getSecurityIdentifier());
      if (marketValue == null) {
        throw new NullPointerException("Could not get market data for " + strip);
      }
      InstrumentDerivative derivative;

      final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
      final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForFundingCurveInstrument(strip.getInstrumentType(), curveName, curveName);

      final InstrumentDefinition<?> definition = securityConverter.visit(financialSecurity);
      if (strip.getSecurity().getSecurityType().equals("FUTURE")) {
        marketValue = 1 - marketValue; // transform to rate for initial rates guess
      }
      try {
        derivative = getDefinitionConverter().convert(financialSecurity, definition, now, curveNames, timeSeries);
      } catch (final OpenGammaRuntimeException ogre) {
        s_logger.error("Error thrown by convertor for security {}, definition {}, time {}, curveNames {}, dataSource {}",
                       financialSecurity, definition, now, curveNames, timeSeries);
        throw ogre;
      }
      if (derivative == null) {
        throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
      }
      if (_calcTypeParRate) {
        marketValues[i] = marketValue;
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = marketValue;
      nodeTimes[i] = derivative.accept(LAST_DATE_CALCULATOR);
      i++;
    }
    ParallelArrayBinarySort.parallelBinarySort(nodeTimes, initialRatesGuess);
    final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<>();
    curveKnots.put(curveName, nodeTimes);
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<>();
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
    curveNodes.put(curveName, nodeTimes);
    interpolators.put(curveName, getInterpolator(curveData.getCurveSpecification()));
    // TODO have use finite difference or not as an input [FIN-147]
    final Currency currency = Currency.of(targetSpec.getUniqueId().getValue());
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues, null, curveNodes, interpolators, false, new FXMatrix(currency));
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, getCalculator());
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, getSensitivityCalculator());
    NewtonVectorRootFinder rootFinder;
    double[] yields = null;
    try {
      // TODO have the decomposition as an optional input [FIN-146]
      rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
      final DoubleMatrix1D result = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess));
      yields = result.getData();
    } catch (final Exception eLU) {
      try {
        s_logger.warn("Could not find root using LU decomposition and present value method for curve " + curveName + "; trying SV. Error was: " + eLU.getMessage());
        rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
        yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess)).getData();
      } catch (final Exception eSV) {
        s_logger.warn("Could not find root using SV decomposition and present value method for curve " + curveName + ". Error was: " + eSV.getMessage());
        throw new OpenGammaRuntimeException(eSV.getMessage());
      }
    }
    final YieldAndDiscountCurve curve;
    if (createSensitivities || createYieldCurve) {
      curve = YieldCurve.from(InterpolatedDoublesCurve.from(nodeTimes, yields, getInterpolator(curveData.getCurveSpecification())));
    } else {
      curve = null;
    }
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(4);
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, getCalculationType())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, curveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, curveName);
    if (createJacobian) {
      final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(yields));
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties.get()), jacobianMatrix.getData()));
    }
    if (createSensitivities) {
      final double[] couponSensitivities = new double[derivatives.size()];
      int ii = 0;
      final String[] curveNames = new String[] {curveName, curveName };
      final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {curve, curve };
      final YieldCurveBundle curveBundle = new YieldCurveBundle(curveNames, curves);
      for (final InstrumentDerivative derivative : derivatives) {
        couponSensitivities[ii++] = derivative.accept(getCouponSensitivityCalculator(), curveBundle);
      }
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, properties.get()), new DoubleMatrix1D(couponSensitivities)));
    }
    if (createYieldCurve) {
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties.with(ValuePropertyNames.CURVE, curveName).get()), curve));
    }
    return result;
  }

  private Set<ComputedValue> execute(FunctionExecutionContext executionContext,
                                     ComputationTargetSpecification targetSpec,
                                     String forwardCurveName,
                                     YieldCurveData forwardCurveData,
                                     HistoricalTimeSeriesBundle forwardTimeSeries,
                                     String fundingCurveName,
                                     YieldCurveData fundingCurveData,
                                     HistoricalTimeSeriesBundle fundingTimeSeries,
                                     boolean createForwardYieldCurve,
                                     boolean createFundingYieldCurve,
                                     boolean createJacobian,
                                     boolean createSensitivities) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final List<InstrumentDerivative> derivatives = new ArrayList<>();
    final int nFunding = fundingCurveData.getCurveSpecification().getStrips().size();
    final int nForward = forwardCurveData.getCurveSpecification().getStrips().size();
    final double[] initialRatesGuess = new double[nFunding + nForward];
    final double[] fundingNodeTimes = new double[nFunding];
    final double[] forwardNodeTimes = new double[nForward];
    final double[] marketValues = new double[nFunding + nForward];
    int i = 0, fundingIndex = 0, forwardIndex = 0;
    final InterestRateInstrumentTradeOrSecurityConverter securityConverter = getSecurityConverter(executionContext);
    for (final FixedIncomeStripWithSecurity strip : fundingCurveData.getCurveSpecification().getStrips()) {
      final Double fundingMarketValue = fundingCurveData.getDataPoint(strip.getSecurityIdentifier());
      if (fundingMarketValue == null) {
        throw new OpenGammaRuntimeException("Could not get funding market data for " + strip);
      }
      final double marketValue = fundingMarketValue;
      final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
      InstrumentDerivative derivative;
      final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForFundingCurveInstrument(strip.getInstrumentType(), fundingCurveName, forwardCurveName);
      final InstrumentDefinition<?> definition = securityConverter.visit(financialSecurity);
      if (strip.getSecurity().getSecurityType().equals("FUTURE")) {
        throw new OpenGammaRuntimeException("We do not currently support FundingCurves containing FUTURES. Contact QR if you desire this.");
      }
      derivative = getDefinitionConverter().convert(financialSecurity, definition, now, curveNames, fundingTimeSeries);
      if (derivative == null) {
        throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
      }
      if (_calcTypeParRate) { // set market value to the rate
        marketValues[i] = marketValue;
      } // else PV, leave at 0

      derivatives.add(derivative);
      initialRatesGuess[i] = marketValue;
      i++;
      fundingNodeTimes[fundingIndex] = derivative.accept(LAST_DATE_CALCULATOR);
      fundingIndex++;
    }
    for (final FixedIncomeStripWithSecurity strip : forwardCurveData.getCurveSpecification().getStrips()) {
      final Double forwardMarketValue = forwardCurveData.getDataPoint(strip.getSecurityIdentifier());
      if (forwardMarketValue == null) {
        throw new OpenGammaRuntimeException("Could not get forward market data for " + strip);
      }
      double marketValue = forwardMarketValue;
      final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
      InstrumentDerivative derivative = null;
      final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForForwardCurveInstrument(strip.getInstrumentType(), fundingCurveName, forwardCurveName);
      try {
        InstrumentDefinition<?> definition = securityConverter.visit(financialSecurity);
        if (strip.getSecurity().getSecurityType().equals("FUTURE")) {
          if (!_calcTypeParRate) {
            // Scale notional to 1 - this is to better condition the jacobian matrix
            // Set trade price to current market value - so the present value will be zero once fit
            definition = ((InterestRateFutureTransactionDefinition) definition).withNewNotionalAndTransactionPrice(1.0, marketValue);
          }
          marketValue = 1 - marketValue; // transform to rate for initial rates guess
        }
        derivative = getDefinitionConverter().convert(financialSecurity, definition, now, curveNames, forwardTimeSeries);
      } catch (final Exception e) {
        s_logger.error("Caught exception for " + financialSecurity, e);
      }
      if (derivative == null) {
        throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
      }
      if (_calcTypeParRate) { // set market value to the rate, else leave at 0
        marketValues[i] = marketValue;
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = marketValue;
      i++;
      forwardNodeTimes[forwardIndex] = derivative.accept(LAST_DATE_CALCULATOR);
      forwardIndex++;
    }
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<>();
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
    curveNodes.put(fundingCurveName, fundingNodeTimes);
    interpolators.put(fundingCurveName, getInterpolator(fundingCurveData.getCurveSpecification()));
    curveNodes.put(forwardCurveName, forwardNodeTimes);
    interpolators.put(forwardCurveName, getInterpolator(forwardCurveData.getCurveSpecification()));
    // TODO have use finite difference or not as an input [FIN-147]
    final Currency currency = Currency.of(targetSpec.getUniqueId().getValue());
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues, null, curveNodes, interpolators, false, new FXMatrix(currency));
    // TODO have the calculator and sensitivity calculators as an input [FIN-144], [FIN-145]
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, getCalculator());
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, getSensitivityCalculator());
    NewtonVectorRootFinder rootFinder;
    double[] yields = null;
    // TODO have the decomposition as an optional input [FIN-146]
    try {
      rootFinder = new BroydenVectorRootFinder(1e-4, 1e-4, 10000, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
      yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess)).getData();
    } catch (final Exception eSV) {
      s_logger.warn("Could not find root using SV decomposition and " + _calculationType + " method for curves " + fundingCurveName + " and " + forwardCurveName + ". Error was: " + eSV.getMessage());
      throw new OpenGammaRuntimeException("Could not find curves " + fundingCurveName + " (" + targetSpec.getUniqueId().getValue() + "), " + forwardCurveName + " ("
          + targetSpec.getUniqueId().getValue() + ") using SV decomposition and calculation method " + _calculationType);
    }
    final YieldAndDiscountCurve fundingCurve;
    if (createSensitivities || createFundingYieldCurve) {
      final double[] fundingYields = Arrays.copyOfRange(yields, 0, fundingNodeTimes.length);
      fundingCurve = YieldCurve.from(InterpolatedDoublesCurve.from(fundingNodeTimes, fundingYields, getInterpolator(fundingCurveData.getCurveSpecification())));
    } else {
      fundingCurve = null;
    }
    final YieldAndDiscountCurve forwardCurve;
    if (createSensitivities || createForwardYieldCurve) {
      final double[] forwardYields = Arrays.copyOfRange(yields, fundingNodeTimes.length, yields.length);
      forwardCurve = YieldCurve.from(InterpolatedDoublesCurve.from(forwardNodeTimes, forwardYields, getInterpolator(forwardCurveData.getCurveSpecification())));
    } else {
      forwardCurve = null;
    }
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(4);
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, getCalculationType())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName);
    if (createJacobian) {
      final DoubleMatrix2D jacobian = jacobianCalculator.evaluate(new DoubleMatrix1D(yields));
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties.get()), jacobian.getData()));
    }
    if (createSensitivities) { // calcType is PresentValue. Compute CouponSens ( dPrice / dParRate ) used in conjunction with Jacobian to get Yield Curve Node (Par Rate) Sensitivities
      final double[] couponSensitivities = new double[derivatives.size()];
      int ii = 0;
      final String[] curveNames = new String[] {forwardCurveName, fundingCurveName };
      final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {forwardCurve, fundingCurve };
      final YieldCurveBundle curveBundle = new YieldCurveBundle(curveNames, curves);
      for (final InstrumentDerivative derivative : derivatives) {
        couponSensitivities[ii++] = derivative.accept(getCouponSensitivityCalculator(), curveBundle);
      }
      final ValueProperties couponProperties = createValueProperties().with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
          .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get();
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, couponProperties), new DoubleMatrix1D(couponSensitivities)));
    }
    if (createForwardYieldCurve) {
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties.with(ValuePropertyNames.CURVE, forwardCurveName).get()), forwardCurve));
    }
    if (createFundingYieldCurve) {
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties.withoutAny(ValuePropertyNames.CURVE)
          .with(ValuePropertyNames.CURVE, fundingCurveName).get()), fundingCurve));
    }
    return result;
  }

}
