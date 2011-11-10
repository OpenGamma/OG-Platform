/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.MarketDataNormalizationUtils;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateInstrumentTradeOrSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.LastDateCalculator;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueCouponSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class MarketInstrumentImpliedYieldCurveFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(MarketInstrumentImpliedYieldCurveFunction.class);
  private static final LastDateCalculator LAST_DATE_CALCULATOR = LastDateCalculator.getInstance();

  /** Label setting this function to use the par rate of the instruments in root-finding */
  public static final String PAR_RATE_STRING = "ParRate";
  /** Label setting this function to use the present value of the instruments in root-finding */
  public static final String PRESENT_VALUE_STRING = "PresentValue";

  private final YieldCurveFunctionHelper _fundingHelper;
  private final YieldCurveFunctionHelper _forwardHelper;

  private final ComputationTargetSpecification _currencySpec;
  private final String _fundingCurveDefinitionName;
  private final String _forwardCurveDefinitionName;
  private final InterestRateDerivativeVisitor<YieldCurveBundle, Double> _calculator;
  private final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> _sensitivityCalculator;

  private ValueSpecification _fundingCurveResult;
  private ValueSpecification _forwardCurveResult;
  private ValueSpecification _jacobianResult;
  private ValueSpecification _fundingCurveSpecResult;
  private ValueSpecification _forwardCurveSpecResult;
  private ValueSpecification _couponSensitivityResult;
  private PresentValueCouponSensitivityCalculator _couponSensitivityCalculator;
  private Set<ValueSpecification> _results;

  private YieldCurveDefinition _forwardCurveDefinition;
  private YieldCurveDefinition _fundingCurveDefinition;
  private InterestRateInstrumentTradeOrSecurityConverter _securityConverter;
  private FixedIncomeConverterDataProvider _definitionConverter;
  private CombinedInterpolatorExtrapolator _fundingInterpolator;
  private CombinedInterpolatorExtrapolator _forwardInterpolator;
  private final String _calculationType;

  public MarketInstrumentImpliedYieldCurveFunction(final String currency, final String curveDefinitionName, final String calculatorType) {
    this(currency, curveDefinitionName, curveDefinitionName, calculatorType);
  }

  public MarketInstrumentImpliedYieldCurveFunction(final String currency, final String fundingCurveDefinitionName, final String forwardCurveDefinitionName, final String calculatorType) {
    this(Currency.of(currency), fundingCurveDefinitionName, forwardCurveDefinitionName, calculatorType);
  }

  public MarketInstrumentImpliedYieldCurveFunction(final Currency currency, final String curveDefinitionName, final String calculatorType) {
    this(currency, curveDefinitionName, curveDefinitionName, calculatorType);
  }

  public MarketInstrumentImpliedYieldCurveFunction(final Currency currency, final String fundingCurveDefinitionName, final String forwardCurveDefinitionName, final String calculationType) {
    _fundingHelper = new YieldCurveFunctionHelper(currency, fundingCurveDefinitionName);
    _forwardHelper = new YieldCurveFunctionHelper(currency, forwardCurveDefinitionName);
    _fundingCurveDefinitionName = fundingCurveDefinitionName;
    _forwardCurveDefinitionName = forwardCurveDefinitionName;
    _currencySpec = new ComputationTargetSpecification(currency);
    _calculationType = calculationType;
    if (calculationType.equals(PAR_RATE_STRING)) {
      _calculator = ParRateCalculator.getInstance();
      _sensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();
    } else if (calculationType.equals(PRESENT_VALUE_STRING)) {
      _calculator = PresentValueCalculator.getInstance();
      _sensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();
      _couponSensitivityCalculator = PresentValueCouponSensitivityCalculator.getInstance();
    } else {
      throw new IllegalArgumentException("Could not get calculator type " + calculationType);
    }
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _fundingCurveResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, _currencySpec, createValueProperties().with(ValuePropertyNames.CURVE, _fundingCurveDefinitionName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _calculationType).get());
    _forwardCurveResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, _currencySpec, createValueProperties().with(ValuePropertyNames.CURVE, _forwardCurveDefinitionName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _calculationType).get());
    _jacobianResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, _currencySpec, createValueProperties()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, _forwardCurveDefinitionName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, _fundingCurveDefinitionName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, _calculationType).get());
    _fundingCurveSpecResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, _currencySpec, createValueProperties().with(ValuePropertyNames.CURVE, _fundingCurveDefinitionName).get());
    _forwardCurveSpecResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, _currencySpec, createValueProperties().with(ValuePropertyNames.CURVE, _forwardCurveDefinitionName).get());
    _results = Sets.newHashSet(_fundingCurveResult, _forwardCurveResult, _jacobianResult, _fundingCurveSpecResult, _forwardCurveSpecResult);
    if (_calculationType.equals(PRESENT_VALUE_STRING)) {
      _couponSensitivityResult = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, _currencySpec, createValueProperties()
          .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, _forwardCurveDefinitionName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, _fundingCurveDefinitionName).get());
      _results.add(_couponSensitivityResult);
    }

    _forwardCurveDefinition = _forwardHelper.init(context, this);
    _fundingCurveDefinition = _fundingHelper.init(context, this);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _securityConverter = new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource);
    _fundingInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(_fundingCurveDefinition.getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    if (!_fundingCurveDefinitionName.equals(_forwardCurveDefinitionName)) {
      _forwardInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(_forwardCurveDefinition.getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    }
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  /**
   * 
   */
  public final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final InterpolatedYieldCurveSpecification _fundingCurveSpecification;
    private final InterpolatedYieldCurveSpecification _forwardCurveSpecification;

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest, final InterpolatedYieldCurveSpecification fundingCurveSpecification,
        final InterpolatedYieldCurveSpecification forwardCurveSpecification) {
      super(earliest, latest);
      _fundingCurveSpecification = fundingCurveSpecification;
      _forwardCurveSpecification = forwardCurveSpecification;
    }

    public InterpolatedYieldCurveSpecification getFundingCurveSpecification() {
      return _fundingCurveSpecification;
    }

    public InterpolatedYieldCurveSpecification getForwardCurveSpecification() {
      return _forwardCurveSpecification;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
          OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource());
      final Clock snapshotClock = executionContext.getValuationClock();
      final ZonedDateTime now = snapshotClock.zonedDateTime();
      final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
      if (_fundingCurveDefinitionName.equals(_forwardCurveDefinitionName)) {
        final Map<ExternalId, Double> marketDataMap = _fundingHelper.buildMarketDataMap(inputs);
        return getSingleCurveResult(marketDataMap, builder, now, dataSource);
      }

      final Map<ExternalId, Double> fundingMarketDataMap = _fundingHelper.buildMarketDataMap(inputs);
      final Map<ExternalId, Double> forwardMarketDataMap = _forwardHelper.buildMarketDataMap(inputs);

      final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpecificationWithSecurities = builder.resolveToSecurity(_fundingCurveSpecification, fundingMarketDataMap);
      final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpecificationWithSecurities = builder.resolveToSecurity(_forwardCurveSpecification, forwardMarketDataMap);
      final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
      final Set<FixedIncomeStrip> fundingStrips = _fundingCurveDefinition.getStrips();
      final Set<FixedIncomeStrip> forwardStrips = _forwardCurveDefinition.getStrips();
      final int nFunding = fundingStrips.size();
      final int nForward = forwardStrips.size();
      final double[] initialRatesGuess = new double[nFunding + nForward];
      final double[] fundingNodeTimes = new double[nFunding];
      final double[] forwardNodeTimes = new double[nForward];
      final double[] marketValues = new double[nFunding + nForward];
      int i = 0, fundingIndex = 0, forwardIndex = 0;
      for (final FixedIncomeStripWithSecurity strip : fundingCurveSpecificationWithSecurities.getStrips()) {
        final Double fundingMarketValue = fundingMarketDataMap.get(strip.getSecurityIdentifier());
        if (fundingMarketValue == null) {
          throw new OpenGammaRuntimeException("Could not get funding market data for " + strip);
        }
        final double marketValue = fundingMarketValue;
        final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
        InterestRateDerivative derivative;
        final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper
            .getCurveNamesForFundingCurveInstrument(strip.getInstrumentType(), _fundingCurveDefinitionName, _forwardCurveDefinitionName);
        final FixedIncomeInstrumentDefinition<?> definition = _securityConverter.visit(financialSecurity);
        derivative = _definitionConverter.convert(financialSecurity, definition, now, curveNames, dataSource);
        if (derivative == null) {
          throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
        }
        if (_calculationType.equals(PRESENT_VALUE_STRING)) {
          marketValues[i] = 0;
        } else {
          marketValues[i] = MarketDataNormalizationUtils.normalizeRateForFixedIncomeStrip(strip.getInstrumentType(), marketValue);
        }
        derivatives.add(derivative);
        initialRatesGuess[i++] = 0.01;
        fundingNodeTimes[fundingIndex] = LAST_DATE_CALCULATOR.visit(derivative);
        fundingIndex++;
      }

      for (final FixedIncomeStripWithSecurity strip : forwardCurveSpecificationWithSecurities.getStrips()) {
        final Double forwardMarketValue = forwardMarketDataMap.get(strip.getSecurityIdentifier());
        if (forwardMarketValue == null) {
          throw new OpenGammaRuntimeException("Could not get forward market data for " + strip);
        }
        final double marketValue = forwardMarketValue;
        final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
        InterestRateDerivative derivative;
        final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper
            .getCurveNamesForForwardCurveInstrument(strip.getInstrumentType(), _fundingCurveDefinitionName, _forwardCurveDefinitionName);
        final FixedIncomeInstrumentDefinition<?> definition = _securityConverter.visit(financialSecurity);
        derivative = _definitionConverter.convert(financialSecurity, definition, now, curveNames, dataSource);
        if (derivative == null) {
          throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
        }
        if (_calculationType.equals(PRESENT_VALUE_STRING)) {
          marketValues[i] = 0;
        } else {
          marketValues[i] = MarketDataNormalizationUtils.normalizeRateForFixedIncomeStrip(strip.getInstrumentType(), marketValue);
        }
        derivatives.add(derivative);
        initialRatesGuess[i++] = 0.01;
        forwardNodeTimes[forwardIndex] = LAST_DATE_CALCULATOR.visit(derivative);
        forwardIndex++;
      }
      //Arrays.sort(fundingNodeTimes);
      //Arrays.sort(forwardNodeTimes);
      // ParallelArrayBinarySort.parallelBinarySort(fundingNodeTimes, initialRatesGuess); //TODO will eventually need two sets of rates guesses
      // ParallelArrayBinarySort.parallelBinarySort(fundingNodeTimes, initialRatesGuess); //TODO will eventually need two sets of rates guesses
      final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<String, double[]>();
      curveKnots.put(_fundingCurveDefinitionName, fundingNodeTimes);
      curveKnots.put(_forwardCurveDefinitionName, forwardNodeTimes);
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
      final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<String, Interpolator1D>();
      curveNodes.put(_fundingCurveDefinitionName, fundingNodeTimes);
      interpolators.put(_fundingCurveDefinitionName, _fundingInterpolator);
      curveNodes.put(_forwardCurveDefinitionName, forwardNodeTimes);
      interpolators.put(_forwardCurveDefinitionName, _forwardInterpolator);
      // TODO have use finite difference or not as an input [FIN-147]
      final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues, null, curveNodes, interpolators, false);
      // TODO have the calculator and sensitivity calculators as an input [FIN-144], [FIN-145]
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, _calculator);
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, _sensitivityCalculator);
      NewtonVectorRootFinder rootFinder;
      double[] yields = null;
      // TODO have the decomposition as an optional input [FIN-146]
      try {
        rootFinder = new BroydenVectorRootFinder(1e-4, 1e-4, 10000, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
        yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess)).getData();
      } catch (final Exception eSV) {
        s_logger.warn("Could not find root using SV decomposition and " + _calculationType + " method for curves " + _fundingCurveDefinitionName + " and " + _forwardCurveDefinitionName
            + ". Error was: " + eSV.getMessage());
        throw new OpenGammaRuntimeException("Could not find curves " + _fundingCurveDefinition.getName() + " (" + _fundingCurveDefinition.getCurrency() + "), " + _forwardCurveDefinitionName + " ("
            + _forwardCurveDefinition.getCurrency() + ") using SV decomposition", eSV);
      }
      final double[] fundingYields = Arrays.copyOfRange(yields, 0, fundingNodeTimes.length);
      final double[] forwardYields = Arrays.copyOfRange(yields, fundingNodeTimes.length, yields.length);
      final YieldAndDiscountCurve fundingCurve = new YieldCurve(InterpolatedDoublesCurve.from(fundingNodeTimes, fundingYields, _fundingInterpolator));
      final YieldAndDiscountCurve forwardCurve = new YieldCurve(InterpolatedDoublesCurve.from(forwardNodeTimes, forwardYields, _forwardInterpolator));
      final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(yields));
      final Set<ComputedValue> result = Sets.newHashSet(
          new ComputedValue(_fundingCurveResult, fundingCurve), 
          new ComputedValue(_forwardCurveResult, forwardCurve), 
          new ComputedValue(_jacobianResult, jacobianMatrix.getData()), 
          new ComputedValue(_fundingCurveSpecResult, fundingCurveSpecificationWithSecurities), 
          new ComputedValue(_forwardCurveSpecResult, forwardCurveSpecificationWithSecurities));
      if (_calculationType.equals(PRESENT_VALUE_STRING)) {
        if (_couponSensitivityCalculator == null) {
          throw new OpenGammaRuntimeException("Should never happen - coupon sensitivity calculator was null but requested calculation method was present value");
        }
        final double[] couponSensitivities = new double[derivatives.size()];
        int ii = 0;
        final String[] curveNames = new String[] {_forwardCurveDefinitionName, _fundingCurveDefinitionName};
        final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {forwardCurve, fundingCurve};
        final YieldCurveBundle curveBundle = new YieldCurveBundle(curveNames, curves);
        for (final InterestRateDerivative derivative : derivatives) {
          couponSensitivities[ii++] = _couponSensitivityCalculator.visit(derivative, curveBundle);
        }
        final ComputedValue couponSensitivitiesValue = new ComputedValue(_couponSensitivityResult, new DoubleMatrix1D(couponSensitivities));
        result.add(couponSensitivitiesValue);
      }
      return result;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _forwardHelper.canApplyTo(target) && _fundingHelper.canApplyTo(target);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> result = new HashSet<ValueRequirement>();

      result.add(_forwardHelper.getMarketDataValueRequirement());
      result.add(_fundingHelper.getMarketDataValueRequirement());
      return result;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return _results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    @SuppressWarnings("synthetic-access")
    private Set<ComputedValue> getSingleCurveResult(final Map<ExternalId, Double> marketDataMap, final FixedIncomeStripIdentifierAndMaturityBuilder builder, final ZonedDateTime now,
        final HistoricalTimeSeriesSource dataSource) {
      // TODO going to arbitrarily use funding curve - will give the same result as forward curve
      final InterpolatedYieldCurveSpecificationWithSecurities specificationWithSecurities = builder.resolveToSecurity(_fundingCurveSpecification, marketDataMap);
      final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
      final Set<FixedIncomeStrip> strips = _fundingCurveDefinition.getStrips();
      final int n = strips.size();
      final double[] initialRatesGuess = new double[n];
      final double[] nodeTimes = new double[n];
      final double[] marketValues = new double[n];
      int i = 0;
      for (final FixedIncomeStripWithSecurity strip : specificationWithSecurities.getStrips()) {
        final Double marketValue = marketDataMap.get(strip.getSecurityIdentifier());
        if (marketValue == null) {
          throw new NullPointerException("Could not get market data for " + strip);
        }
        InterestRateDerivative derivative;
        final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
        final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper
            .getCurveNamesForFundingCurveInstrument(strip.getInstrumentType(), _fundingCurveDefinitionName, _forwardCurveDefinitionName);
        final FixedIncomeInstrumentDefinition<?> definition = _securityConverter.visit(financialSecurity);
        derivative = _definitionConverter.convert(financialSecurity, definition, now, curveNames, dataSource);
        if (derivative == null) {
          throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
        }
        if (_calculationType.equals(PRESENT_VALUE_STRING)) {
          marketValues[i] = 0;
        } else {
          marketValues[i] = MarketDataNormalizationUtils.normalizeRateForFixedIncomeStrip(strip.getInstrumentType(), marketValue);
        }
        derivatives.add(derivative);
        initialRatesGuess[i] = 0.01;
        nodeTimes[i] = LAST_DATE_CALCULATOR.visit(derivative);
        i++;
      }
      ParallelArrayBinarySort.parallelBinarySort(nodeTimes, initialRatesGuess);
      final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<String, double[]>();
      curveKnots.put(_fundingCurveDefinitionName, nodeTimes);
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
      final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<String, Interpolator1D>();
      curveNodes.put(_fundingCurveDefinitionName, nodeTimes);
      interpolators.put(_fundingCurveDefinitionName, _fundingInterpolator);
      // TODO have use finite difference or not as an input [FIN-147]

      final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues, null, curveNodes, interpolators, false);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, _calculator);
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, _sensitivityCalculator);
      NewtonVectorRootFinder rootFinder;
      double[] yields = null;
      try {
        // TODO have the decomposition as an optional input [FIN-146]
        rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.LU_COMMONS_NAME));
        final DoubleMatrix1D result = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess));

        yields = result.getData();
      } catch (final Exception eLU) {
        try {
          s_logger.warn("Could not find root using LU decomposition and present value method for curve " + _fundingCurveDefinitionName + "; trying SV. Error was: " + eLU.getMessage());
          rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
          yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess)).getData();
        } catch (final Exception eSV) {
          s_logger.warn("Could not find root using SV decomposition and present value method for curve " + _fundingCurveDefinitionName + ". Error was: " + eSV.getMessage());
          throw new OpenGammaRuntimeException(eSV.getMessage());
        }
      }

      final YieldAndDiscountCurve fundingCurve = new YieldCurve(InterpolatedDoublesCurve.from(nodeTimes, yields, _fundingInterpolator));
      final YieldAndDiscountCurve forwardCurve = new YieldCurve(InterpolatedDoublesCurve.from(nodeTimes, yields, _fundingInterpolator));
      final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(yields));
      final Set<ComputedValue> result = Sets.newHashSet(new ComputedValue(_fundingCurveResult, fundingCurve), new ComputedValue(_forwardCurveResult, forwardCurve), new ComputedValue(_jacobianResult,
          jacobianMatrix.getData()), new ComputedValue(_fundingCurveSpecResult, specificationWithSecurities), new ComputedValue(_forwardCurveSpecResult, specificationWithSecurities));
      if (_calculationType.equals(PRESENT_VALUE_STRING)) {
        if (_couponSensitivityCalculator == null) {
          throw new OpenGammaRuntimeException("Should never happen - coupon sensitivity calculator was null but requested calculation method was present value");
        }
        final double[] couponSensitivities = new double[derivatives.size()];
        int ii = 0;
        final String[] curveNames = new String[] {_forwardCurveDefinitionName, _fundingCurveDefinitionName};
        final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {forwardCurve, fundingCurve};
        final YieldCurveBundle curveBundle = new YieldCurveBundle(curveNames, curves);
        for (final InterestRateDerivative derivative : derivatives) {
          couponSensitivities[ii++] = _couponSensitivityCalculator.visit(derivative, curveBundle);
        }
        final ComputedValue couponSensitivitiesValue = new ComputedValue(_couponSensitivityResult, new DoubleMatrix1D(couponSensitivities));
        result.add(couponSensitivitiesValue);
      }
      return result;
    }
  }

  @SuppressWarnings("synthetic-access")
  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    final Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> forwardCompile = _forwardHelper.compile(context, atInstant);
    final Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> fundingCompile = _fundingHelper.compile(context, atInstant);
    final InstantProvider earliest = earlyBound(forwardCompile.getFirst(), fundingCompile.getFirst());
    final InstantProvider latest = lateBound(forwardCompile.getSecond(), fundingCompile.getSecond());
    return new CompiledImpl(earliest, latest, fundingCompile.getThird(), forwardCompile.getThird());
  }

  private InstantProvider earlyBound(final InstantProvider a, final InstantProvider b) {
    if (a == null) {
      return b;
    } else if (b == null) {
      return a;
    } else {
      return a.toInstant().compareTo(b.toInstant()) > 0 ? a : b;
    }
  }

  private InstantProvider lateBound(final InstantProvider a, final InstantProvider b) {
    if (a == null) {
      return b;
    } else if (b == null) {
      return a;
    } else {
      return a.toInstant().compareTo(b.toInstant()) > 0 ? b : a;
    }
  }

  public int getPriority() {
    if (isSecondary()) {
      return -1;
    } 
    return 0;
  }

  private boolean isSecondary() {
    return _fundingCurveDefinitionName.equals("SECONDARY") && _forwardCurveDefinitionName.equals("SECONDARY");
  }
}
