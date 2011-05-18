/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.fixedincome.CashSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FRASecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FutureSecurityConverter;
import com.opengamma.financial.analytics.swap.FixedFloatSwapSecurityToSwapConverter;
import com.opengamma.financial.analytics.swap.TenorSwapSecurityToTenorSwapConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeFutureInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.LastDateCalculator;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class MarketInstrumentImpliedYieldCurveFunction extends AbstractFunction {
  
  private static final LastDateCalculator LAST_DATE_CALCULATOR = LastDateCalculator.getInstance();
  
  private final YieldCurveFunctionHelper _fundingHelper;
  private final  YieldCurveFunctionHelper _forwardHelper;
    
  private final ComputationTargetSpecification _currencySpec;
  private final String _fundingCurveDefinitionName;
  private final String _forwardCurveDefinitionName;
  
  private ValueSpecification _fundingCurveResult;
  private ValueSpecification _forwardCurveResult;
  private ValueSpecification _jacobianResult;
  private ValueSpecification _fundingCurveSpecResult;
  private ValueSpecification _forwardCurveSpecResult;
  private Set<ValueSpecification> _results;
  
  private YieldCurveDefinition _forwardCurveDefinition;
  private YieldCurveDefinition _fundingCurveDefinition;
  
  
  public MarketInstrumentImpliedYieldCurveFunction(final String currency, final String curveDefinitionName) {
    this(currency, curveDefinitionName, curveDefinitionName);
  }

  public MarketInstrumentImpliedYieldCurveFunction(final String currency, final String fundingCurveDefinitionName,
      final String forwardCurveDefinitionName) {
    this(Currency.of(currency), fundingCurveDefinitionName, forwardCurveDefinitionName);
  }

  public MarketInstrumentImpliedYieldCurveFunction(final Currency currency, final String curveDefinitionName) {
    this(currency, curveDefinitionName, curveDefinitionName);
  }

  public MarketInstrumentImpliedYieldCurveFunction(final Currency currency, final String fundingCurveDefinitionName,
      final String forwardCurveDefinitionName) {
    _fundingHelper = new YieldCurveFunctionHelper(currency, fundingCurveDefinitionName);
    _forwardHelper = new YieldCurveFunctionHelper(currency, forwardCurveDefinitionName);
    
    _fundingCurveDefinitionName = fundingCurveDefinitionName;
    _forwardCurveDefinitionName = forwardCurveDefinitionName;
    _currencySpec = new ComputationTargetSpecification(currency);
  }


  @Override
  public void init(final FunctionCompilationContext context) {
    
    
    _fundingCurveResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, _currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, _fundingCurveDefinitionName).get());
    _forwardCurveResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, _currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, _forwardCurveDefinitionName).get());
    _jacobianResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, _currencySpec,
        createValueProperties().with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, _forwardCurveDefinitionName)
            .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, _fundingCurveDefinitionName).get());
    _fundingCurveSpecResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, _currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, _fundingCurveDefinitionName).get());
    _forwardCurveSpecResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, _currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, _forwardCurveDefinitionName).get());
    _results = Sets.newHashSet(_fundingCurveResult, _forwardCurveResult, _jacobianResult, _fundingCurveSpecResult,
        _forwardCurveSpecResult);
    
    _forwardCurveDefinition = _forwardHelper.init(context, this);
    _fundingCurveDefinition = _fundingHelper.init(context, this);
  }

    
  /**
   *
   */
  public final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final InterpolatedYieldCurveSpecification _fundingCurveSpecification;
    private final InterpolatedYieldCurveSpecification _forwardCurveSpecification;
    private final Map<Identifier, Double> _identifierToFundingNodeTimes = new HashMap<Identifier, Double>();
    private final Map<Identifier, Double> _identifierToForwardNodeTimes = new HashMap<Identifier, Double>();

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest,
        final InterpolatedYieldCurveSpecification fundingCurveSpecification,
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

    public Map<Identifier, Double> getIdentifierToFundingNodeTimesMap() {
      return _identifierToFundingNodeTimes;
    }

    public Map<Identifier, Double> getIdentifierToForwardNodeTimesMap() {
      return _identifierToForwardNodeTimes;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(
          OpenGammaExecutionContext.getRegionSource(executionContext),
          OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource());
      final Clock snapshotClock = executionContext.getSnapshotClock();
      final ZonedDateTime now = snapshotClock.zonedDateTime();
      final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
      final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
      final ExchangeSource exchangeSource = OpenGammaExecutionContext.getExchangeSource(executionContext);
      final ConventionBundleSource conventionSource = OpenGammaExecutionContext
          .getConventionBundleSource(executionContext);
      final FixedFloatSwapSecurityToSwapConverter swapConverter = new FixedFloatSwapSecurityToSwapConverter(
          holidaySource, regionSource, conventionSource);
      final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, conventionSource);
      final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, conventionSource);
      final FutureSecurityConverter futureConverter = new FutureSecurityConverter(holidaySource, conventionSource,
          exchangeSource);
      //final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource,
      //    regionSource);
      final FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> instrumentAdapter = FinancialSecurityVisitorAdapter.<FixedIncomeInstrumentConverter<?>>builder().cashSecurityVisitor(
          cashConverter).fraSecurityVisitor(fraConverter).create();
      final FinancialSecurityVisitorAdapter<FixedIncomeFutureInstrumentDefinition<?>> futureAdapter = FinancialSecurityVisitorAdapter.<FixedIncomeFutureInstrumentDefinition<?>>builder()
          .futureSecurityVisitor(futureConverter).create();
      final TenorSwapSecurityToTenorSwapConverter tenorSwapConverter = new TenorSwapSecurityToTenorSwapConverter(
          holidaySource, regionSource, conventionSource);
      //final LocalDate localNow = now.toLocalDate();
      
      
      
      if (_fundingCurveDefinitionName.equals(_forwardCurveDefinitionName)) {
        Map<Identifier, Double> marketDataMap = _fundingHelper.buildMarketDataMap(inputs).getDataPoints();
        return getSingleCurveResult(marketDataMap, builder, swapConverter, tenorSwapConverter, instrumentAdapter, futureAdapter, now);
      }

      Map<Identifier, Double> fundingMarketDataMap = _fundingHelper.buildMarketDataMap(inputs).getDataPoints();
      Map<Identifier, Double> forwardMarketDataMap = _forwardHelper.buildMarketDataMap(inputs).getDataPoints();
      
      final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpecificationWithSecurities = builder
          .resolveToSecurity(_fundingCurveSpecification, fundingMarketDataMap);
      final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpecificationWithSecurities = builder
          .resolveToSecurity(_forwardCurveSpecification, forwardMarketDataMap);
      final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
      final Set<FixedIncomeStrip> fundingStrips = _fundingCurveDefinition.getStrips();
      final Set<FixedIncomeStrip> forwardStrips = _forwardCurveDefinition.getStrips();
      final int nFunding = fundingStrips.size();
      final int nForward = forwardStrips.size();
      final double[] initialRatesGuess = new double[nFunding + nForward];
      final double[] fundingNodeTimes = new double[nFunding];
      final double[] forwardNodeTimes = new double[nForward];
      final double[] parRates = new double[nFunding + nForward];
      _identifierToFundingNodeTimes.clear();
      _identifierToForwardNodeTimes.clear();
      int i = 0, fundingIndex = 0, forwardIndex = 0;
      for (final FixedIncomeStripWithSecurity strip : fundingCurveSpecificationWithSecurities.getStrips()) {
        
        final Double fundingMarketValue = fundingMarketDataMap.get(strip.getSecurityIdentifier());
        if (fundingMarketValue == null) {
          throw new NullPointerException("Could not get funding market data for " + strip);
        }
        double marketValue = fundingMarketValue; //TODO is this right
        
        

        
        final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
        InterestRateDerivative derivative;
        if (strip.getInstrumentType() == StripInstrumentType.SWAP) {
          //derivative = financialSecurity.accept(instrumentAdapter).toDerivative(localNow, _fundingCurveDefinitionName,
          //    _forwardCurveDefinitionName);
          derivative = swapConverter.getSwap((SwapSecurity) strip.getSecurity(), _fundingCurveDefinitionName,
              _forwardCurveDefinitionName, marketValue / 100., 0.0, now);
        } else if (strip.getInstrumentType() == StripInstrumentType.CASH) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, _fundingCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.FRA) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, _fundingCurveDefinitionName,
              _forwardCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
          derivative = financialSecurity.accept(futureAdapter).toDerivative(now, marketValue,
              _fundingCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.LIBOR) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, _fundingCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.TENOR_SWAP) {
          derivative = tenorSwapConverter.getSwap((SwapSecurity) strip.getSecurity(), _fundingCurveDefinitionName,
              _forwardCurveDefinitionName, _fundingCurveDefinitionName, marketValue / 10000., now);
        } else {
          throw new OpenGammaRuntimeException(
              "Can only handle swap, cash, LIBOR, FRA, IR futures and tenor swaps at the moment");
        }
        if (derivative == null) {
          throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
        }
        if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
          parRates[i] = 1.0 - marketValue / 100;
        } else {
          parRates[i] = marketValue / 100.;
        }
        derivatives.add(derivative);
        initialRatesGuess[i++] = 0.01;
        fundingNodeTimes[fundingIndex] = LAST_DATE_CALCULATOR.visit(derivative);
        _identifierToFundingNodeTimes.put(strip.getSecurityIdentifier(), fundingNodeTimes[fundingIndex]); // just for debugging.
        fundingIndex++;
      }
      
      for (final FixedIncomeStripWithSecurity strip : forwardCurveSpecificationWithSecurities.getStrips()) {
        
        final Double forwardMarketValue = forwardMarketDataMap.get(strip.getSecurityIdentifier());
        if (forwardMarketValue == null) {
          throw new NullPointerException("Could not get funding market data for " + strip);
        }
        double marketValue = forwardMarketValue; //TODO is this right
        
        final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
        InterestRateDerivative derivative;
        if (strip.getInstrumentType() == StripInstrumentType.SWAP) {
          //derivative = financialSecurity.accept(instrumentAdapter).toDerivative(localNow, _fundingCurveDefinitionName,
          //    _forwardCurveDefinitionName);
          derivative = swapConverter.getSwap((SwapSecurity) strip.getSecurity(), _fundingCurveDefinitionName,
              _forwardCurveDefinitionName, marketValue / 100., 0.0, now);
        } else if (strip.getInstrumentType() == StripInstrumentType.CASH) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, _forwardCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.FRA) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, _fundingCurveDefinitionName,
              _forwardCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
          derivative = financialSecurity.accept(futureAdapter).toDerivative(now, marketValue,
              _forwardCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.LIBOR) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, _forwardCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.TENOR_SWAP) {
          derivative = tenorSwapConverter.getSwap((SwapSecurity) strip.getSecurity(), _fundingCurveDefinitionName,
              _fundingCurveDefinitionName, _forwardCurveDefinitionName, marketValue / 10000., now);
        } else {
          throw new OpenGammaRuntimeException(
              "Can only handle swap, cash, LIBOR, FRA, IR futures and tenor swaps at the moment");
        }
        if (derivative == null) {
          throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
        }
        derivatives.add(derivative);
        initialRatesGuess[i++] = 0.01;
        forwardNodeTimes[forwardIndex] = LAST_DATE_CALCULATOR.visit(derivative);
        _identifierToForwardNodeTimes.put(strip.getSecurityIdentifier(), forwardNodeTimes[forwardIndex]); // just for debugging.
        forwardIndex++;
      }
      Arrays.sort(fundingNodeTimes);
      Arrays.sort(forwardNodeTimes);
      // ParallelArrayBinarySort.parallelBinarySort(fundingNodeTimes, initialRatesGuess); //TODO will eventually need two sets of rates guesses
      // ParallelArrayBinarySort.parallelBinarySort(fundingNodeTimes, initialRatesGuess); //TODO will eventually need two sets of rates guesses
      final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<String, double[]>();
      curveKnots.put(_fundingCurveDefinitionName, fundingNodeTimes);
      curveKnots.put(_forwardCurveDefinitionName, forwardNodeTimes);
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
      final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
      final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> sensitivityCalculators =
          new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
      final Interpolator1D<? extends Interpolator1DDataBundle> fundingInterpolator = CombinedInterpolatorExtrapolatorFactory
          .getInterpolator(_fundingCurveDefinition.getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
              Interpolator1DFactory.FLAT_EXTRAPOLATOR);
      final Interpolator1D<? extends Interpolator1DDataBundle> forwardInterpolator = CombinedInterpolatorExtrapolatorFactory
          .getInterpolator(_forwardCurveDefinition.getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
              Interpolator1DFactory.FLAT_EXTRAPOLATOR);
      curveNodes.put(_fundingCurveDefinitionName, fundingNodeTimes);
      interpolators.put(_fundingCurveDefinitionName, fundingInterpolator);
      curveNodes.put(_forwardCurveDefinitionName, forwardNodeTimes);
      interpolators.put(_forwardCurveDefinitionName, forwardInterpolator);
      // TODO have use finite difference or not as an input [FIN-147]
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> fundingSensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
          .getSensitivityCalculator(_fundingCurveDefinition.getInterpolatorName(),
              Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, false);
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> forwardSensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
          .getSensitivityCalculator(_forwardCurveDefinition.getInterpolatorName(),
              Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, false);
      sensitivityCalculators.put(_fundingCurveDefinitionName, fundingSensitivityCalculator);
      sensitivityCalculators.put(_forwardCurveDefinitionName, forwardSensitivityCalculator);
      final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, parRates,
          null, curveNodes, interpolators, sensitivityCalculators);
      // TODO have the calculator and sensitivity calculators as an input [FIN-144], [FIN-145]
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data,
          PresentValueCalculator.getInstance());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data,
          PresentValueSensitivityCalculator.getInstance());
      NewtonVectorRootFinder rootFinder;
      double[] yields = null;
      try {
        // TODO have the decomposition as an optional input [FIN-146]
        rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100,
            DecompositionFactory.getDecomposition(DecompositionFactory.LU_COMMONS_NAME));
        yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess))
            .getData();
      } catch (final Exception e) {
        rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100,
            DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
        yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess))
            .getData();

      }
      final double[] fundingYields = Arrays.copyOfRange(yields, 0, fundingNodeTimes.length);
      final double[] forwardYields = Arrays.copyOfRange(yields, fundingNodeTimes.length, yields.length);
      final YieldAndDiscountCurve fundingCurve = new YieldCurve(InterpolatedDoublesCurve.from(fundingNodeTimes,
          fundingYields, fundingInterpolator));
      final YieldAndDiscountCurve forwardCurve = new YieldCurve(InterpolatedDoublesCurve.from(forwardNodeTimes,
          forwardYields, forwardInterpolator));
      final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(yields));
      return Sets.newHashSet(new ComputedValue(_fundingCurveResult, fundingCurve), new ComputedValue(
          _forwardCurveResult, forwardCurve), new ComputedValue(_jacobianResult, jacobianMatrix.getData()),
          new ComputedValue(_fundingCurveSpecResult, fundingCurveSpecificationWithSecurities), new ComputedValue(
              _forwardCurveSpecResult, forwardCurveSpecificationWithSecurities));

    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _forwardHelper.canApplyTo(context, target) && _fundingHelper.canApplyTo(context, target);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context,
        final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
      
      result.add(_forwardHelper.getMarketDataValueRequirement());
      result.add(_fundingHelper.getMarketDataValueRequirement());
      return result;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return _results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    private Set<ComputedValue> getSingleCurveResult(Map<Identifier, Double> marketDataMap, FixedIncomeStripIdentifierAndMaturityBuilder builder,
        FixedFloatSwapSecurityToSwapConverter swapConverter, TenorSwapSecurityToTenorSwapConverter tenorSwapConverter,
        FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> instrumentAdapter,
        FinancialSecurityVisitorAdapter<FixedIncomeFutureInstrumentDefinition<?>> futureAdapter,
        ZonedDateTime now) {
      // TODO going to arbitrarily use funding curve - will give the same result as forward curve
      final InterpolatedYieldCurveSpecificationWithSecurities specificationWithSecurities = builder
          .resolveToSecurity(_fundingCurveSpecification, marketDataMap);
      final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
      final Set<FixedIncomeStrip> strips = _fundingCurveDefinition.getStrips();
      final int n = strips.size();
      final double[] initialRatesGuess = new double[n];
      final double[] nodeTimes = new double[n];
      final double[] parRates = new double[n];
      _identifierToFundingNodeTimes.clear();
      _identifierToForwardNodeTimes.clear();
      int i = 0;
      for (final FixedIncomeStripWithSecurity strip : specificationWithSecurities.getStrips()) {
        final Double marketValue = marketDataMap.get(strip.getSecurityIdentifier());
        if (marketValue == null) {
          throw new NullPointerException("Could not get market data for " + strip);
        }
        InterestRateDerivative derivative;
        final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
        if (strip.getInstrumentType() == StripInstrumentType.SWAP) {
          //derivative = financialSecurity.accept(instrumentAdapter).toDerivative(localNow,
          //    _fundingCurveDefinitionName, _fundingCurveDefinitionName);
          derivative = swapConverter.getSwap((SwapSecurity) strip.getSecurity(), _fundingCurveDefinitionName,
              _fundingCurveDefinitionName, marketValue / 100., 0.0, now);
        } else if (strip.getInstrumentType() == StripInstrumentType.CASH) {
          derivative = financialSecurity.accept(instrumentAdapter)
              .toDerivative(now, _fundingCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.FRA) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now,
              _fundingCurveDefinitionName, _fundingCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
          derivative = financialSecurity.accept(futureAdapter).toDerivative(now, marketValue,
              _fundingCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.LIBOR) {
          derivative = financialSecurity.accept(instrumentAdapter)
              .toDerivative(now, _fundingCurveDefinitionName);
        } else if (strip.getInstrumentType() == StripInstrumentType.TENOR_SWAP) {
          derivative = tenorSwapConverter.getSwap((SwapSecurity) strip.getSecurity(), _fundingCurveDefinitionName,
              _fundingCurveDefinitionName, _fundingCurveDefinitionName, marketValue / 10000., now);
        } else {
          throw new OpenGammaRuntimeException(
              "Can only handle swap, cash, LIBOR, FRA, IR futures and tenor swaps at the moment");
        }
        if (derivative == null) {
          throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
        }
        if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
          parRates[i] = 1.0 - marketValue / 100;
        } else {
          parRates[i] = marketValue / 100.;
        }

        derivatives.add(derivative);
        initialRatesGuess[i] = 0.01;
        nodeTimes[i] = LAST_DATE_CALCULATOR.visit(derivative);
        _identifierToFundingNodeTimes.put(strip.getSecurityIdentifier(), nodeTimes[i]); // just for debugging.
        _identifierToForwardNodeTimes.put(strip.getSecurityIdentifier(), nodeTimes[i]); // just for debugging.
        i++;
      }
      ParallelArrayBinarySort.parallelBinarySort(nodeTimes, initialRatesGuess);
      final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<String, double[]>();
      curveKnots.put(_fundingCurveDefinitionName, nodeTimes);
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
      final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
      final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> sensitivityCalculators =
          new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator = CombinedInterpolatorExtrapolatorFactory
          .getInterpolator(_fundingCurveDefinition.getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
              Interpolator1DFactory.FLAT_EXTRAPOLATOR);
      curveNodes.put(_fundingCurveDefinitionName, nodeTimes);
      interpolators.put(_fundingCurveDefinitionName, interpolator);
      // TODO have use finite difference or not as an input [FIN-147]
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> sensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
          .getSensitivityCalculator(_fundingCurveDefinition.getInterpolatorName(),
              Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, false);
      sensitivityCalculators.put(_fundingCurveDefinitionName, sensitivityCalculator);

      // TODO have the calculator and sensitivity calculators as an input [FIN-144], [FIN-145]
      // final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, null, curveNodes, interpolators, sensitivityCalculators);
      // final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PresentValueCalculator.getInstance());
      // final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, PresentValueSensitivityCalculator.getInstance());
      // TODO check this ////////////////////////////////////////////////////////////////////////////////////////////////////////

      final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, parRates,
          null, curveNodes, interpolators, sensitivityCalculators);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data,
          ParRateCalculator.getInstance());
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(
          data, ParRateCurveSensitivityCalculator.getInstance());
      NewtonVectorRootFinder rootFinder;
      double[] yields = null;
      try {
        // TODO have the decomposition as an optional input [FIN-146]
        rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100,
            DecompositionFactory.getDecomposition(DecompositionFactory.LU_COMMONS_NAME));
        yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess))
            .getData();
      } catch (final Exception e) {
        rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100,
            DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
        yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess))
            .getData();

      }

      final YieldAndDiscountCurve fundingCurve = new YieldCurve(InterpolatedDoublesCurve.from(nodeTimes, yields,
          interpolator));
      final YieldAndDiscountCurve forwardCurve = new YieldCurve(InterpolatedDoublesCurve.from(nodeTimes, yields,
          interpolator));
      final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(yields));
      return Sets.newHashSet(new ComputedValue(_fundingCurveResult, fundingCurve), new ComputedValue(
          _forwardCurveResult, forwardCurve), new ComputedValue(_jacobianResult, jacobianMatrix.getData()),
          new ComputedValue(_fundingCurveSpecResult, specificationWithSecurities), new ComputedValue(
              _forwardCurveSpecResult, specificationWithSecurities));
    }
  }

  

  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, InstantProvider atInstant) {
    Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> forwardCompile = _forwardHelper.compile(context, atInstant);
    Triple<InstantProvider, InstantProvider, InterpolatedYieldCurveSpecification> fundingCompile = _fundingHelper.compile(context, atInstant);
    
    InstantProvider earliest = max(forwardCompile.getFirst(), fundingCompile.getFirst());
    InstantProvider latest = min(forwardCompile.getSecond(), fundingCompile.getSecond());
    
    return new CompiledImpl(earliest, latest, fundingCompile.getThird(), forwardCompile.getThird());
  }

  private InstantProvider max(InstantProvider a, InstantProvider b) {
    return a.toInstant().compareTo(b.toInstant()) > 0 ? a : b;
  }
  
  private InstantProvider min(InstantProvider a, InstantProvider b) {
    return a.toInstant().compareTo(b.toInstant()) > 0 ? b : a;
  }
}
