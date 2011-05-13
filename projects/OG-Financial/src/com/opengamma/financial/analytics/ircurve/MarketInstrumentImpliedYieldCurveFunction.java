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

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
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

/**
 * 
 */
public class MarketInstrumentImpliedYieldCurveFunction extends MarketInstrumentImpliedYieldCurveFunctionHelper {

  private ValueSpecification _fundingCurveResult;
  private ValueSpecification _forwardCurveResult;
  private ValueSpecification _jacobianResult;
  private ValueSpecification _fundingCurveSpecResult;
  private ValueSpecification _forwardCurveSpecResult;
  private Set<ValueSpecification> _results;
  private static final LastDateCalculator LAST_DATE_CALCULATOR = LastDateCalculator.getInstance();

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
    super(currency, fundingCurveDefinitionName, forwardCurveDefinitionName);
  }


  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    
    final ComputationTargetSpecification currencySpec = new ComputationTargetSpecification(getCurrency());
    
    _fundingCurveResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, getFundingCurveDefinitionName()).get());
    _forwardCurveResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, getForwardCurveDefinitionName()).get());
    _jacobianResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, currencySpec,
        createValueProperties().with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveDefinitionName())
            .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveDefinitionName()).get());
    _fundingCurveSpecResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, getFundingCurveDefinitionName()).get());
    _forwardCurveSpecResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_SPEC, currencySpec,
        createValueProperties().with(ValuePropertyNames.CURVE, getForwardCurveDefinitionName()).get());
    _results = Sets.newHashSet(_fundingCurveResult, _forwardCurveResult, _jacobianResult, _fundingCurveSpecResult,
        _forwardCurveSpecResult);
  }

  private ValueRequirement getMarketDataValueRequirement() {
    ValueRequirement marketDataValueRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, new ComputationTargetSpecification(getCurrency()),
        ValueProperties.with(ValuePropertyNames.CURVE, getFundingCurveDefinitionName()).with(ValuePropertyNames.CURVE, getForwardCurveDefinitionName()).get());
    return marketDataValueRequirement;
  }
  
  /**
   *
   */
  public final class CompiledImpl extends Compiled {

    private final InterpolatedYieldCurveSpecification _fundingCurveSpecification;
    private final InterpolatedYieldCurveSpecification _forwardCurveSpecification;
    private final Map<Identifier, Double> _identifierToFundingNodeTimes = new HashMap<Identifier, Double>();
    private final Map<Identifier, Double> _identifierToForwardNodeTimes = new HashMap<Identifier, Double>();

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest,
        final Currency targetCurrency,
        final InterpolatedYieldCurveSpecification fundingCurveSpecification,
        final Set<ValueRequirement> fundingCurveRequirements,
        final InterpolatedYieldCurveSpecification forwardCurveSpecification,
        final Set<ValueRequirement> forwardCurveRequirements) {
      super(earliest, latest, targetCurrency, fundingCurveRequirements, forwardCurveRequirements);
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
      
      Map<Identifier, Double> marketDataMap = buildMarketDataMap(inputs).getDataPoints();
      
      if (getFundingCurveDefinitionName().equals(getForwardCurveDefinitionName())) {
        return getSingleCurveResult(marketDataMap, builder, swapConverter, tenorSwapConverter, instrumentAdapter, futureAdapter, now);
      }

      final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpecificationWithSecurities = builder
          .resolveToSecurity(_fundingCurveSpecification, marketDataMap);
      final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpecificationWithSecurities = builder
          .resolveToSecurity(_forwardCurveSpecification, marketDataMap);
      final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
      final Set<FixedIncomeStrip> fundingStrips = getFundingCurveDefinition().getStrips();
      final Set<FixedIncomeStrip> forwardStrips = getForwardCurveDefinition().getStrips();
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
        final Double marketValue = marketDataMap.get(strip.getSecurityIdentifier());
        if (marketValue == null) {
          throw new NullPointerException("Could not get market data for " + strip);
        }
        final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
        InterestRateDerivative derivative;
        if (strip.getInstrumentType() == StripInstrumentType.SWAP) {
          //derivative = financialSecurity.accept(instrumentAdapter).toDerivative(localNow, getFundingCurveDefinitionName(),
          //    getForwardCurveDefinitionName());
          derivative = swapConverter.getSwap((SwapSecurity) strip.getSecurity(), getFundingCurveDefinitionName(),
              getForwardCurveDefinitionName(), marketValue / 100., 0.0, now);
        } else if (strip.getInstrumentType() == StripInstrumentType.CASH) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, getFundingCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.FRA) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, getFundingCurveDefinitionName(),
              getForwardCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
          derivative = financialSecurity.accept(futureAdapter).toDerivative(now, marketValue,
              getFundingCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.LIBOR) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, getFundingCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.TENOR_SWAP) {
          derivative = tenorSwapConverter.getSwap((SwapSecurity) strip.getSecurity(), getFundingCurveDefinitionName(),
              getForwardCurveDefinitionName(), getFundingCurveDefinitionName(), marketValue / 10000., now);
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
        final Double marketValue = marketDataMap.get(strip.getSecurityIdentifier());
        if (marketValue == null) {
          throw new NullPointerException("Could not get market data for " + strip);
        }
        InterestRateDerivative derivative;
        final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
        if (strip.getInstrumentType() == StripInstrumentType.SWAP) {
          //derivative = financialSecurity.accept(instrumentAdapter).toDerivative(localNow, getFundingCurveDefinitionName(),
          //    getForwardCurveDefinitionName());
          derivative = swapConverter.getSwap((SwapSecurity) strip.getSecurity(), getFundingCurveDefinitionName(),
              getForwardCurveDefinitionName(), marketValue / 100., 0.0, now);
        } else if (strip.getInstrumentType() == StripInstrumentType.CASH) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, getForwardCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.FRA) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, getFundingCurveDefinitionName(),
              getForwardCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
          derivative = financialSecurity.accept(futureAdapter).toDerivative(now, marketValue,
              getForwardCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.LIBOR) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now, getForwardCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.TENOR_SWAP) {
          derivative = tenorSwapConverter.getSwap((SwapSecurity) strip.getSecurity(), getFundingCurveDefinitionName(),
              getFundingCurveDefinitionName(), getForwardCurveDefinitionName(), marketValue / 10000., now);
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
      curveKnots.put(getFundingCurveDefinitionName(), fundingNodeTimes);
      curveKnots.put(getForwardCurveDefinitionName(), forwardNodeTimes);
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
      final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
      final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> sensitivityCalculators =
          new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
      final Interpolator1D<? extends Interpolator1DDataBundle> fundingInterpolator = CombinedInterpolatorExtrapolatorFactory
          .getInterpolator(getFundingCurveDefinition().getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
              Interpolator1DFactory.FLAT_EXTRAPOLATOR);
      final Interpolator1D<? extends Interpolator1DDataBundle> forwardInterpolator = CombinedInterpolatorExtrapolatorFactory
          .getInterpolator(getForwardCurveDefinition().getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
              Interpolator1DFactory.FLAT_EXTRAPOLATOR);
      curveNodes.put(getFundingCurveDefinitionName(), fundingNodeTimes);
      interpolators.put(getFundingCurveDefinitionName(), fundingInterpolator);
      curveNodes.put(getForwardCurveDefinitionName(), forwardNodeTimes);
      interpolators.put(getForwardCurveDefinitionName(), forwardInterpolator);
      // TODO have use finite difference or not as an input [FIN-147]
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> fundingSensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
          .getSensitivityCalculator(getFundingCurveDefinition().getInterpolatorName(),
              Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, false);
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> forwardSensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
          .getSensitivityCalculator(getForwardCurveDefinition().getInterpolatorName(),
              Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, false);
      sensitivityCalculators.put(getFundingCurveDefinitionName(), fundingSensitivityCalculator);
      sensitivityCalculators.put(getForwardCurveDefinitionName(), forwardSensitivityCalculator);
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
      if (target.getType() != ComputationTargetType.PRIMITIVE) {
        return false;
      }
      return ObjectUtils.equals(target.getUniqueId(), _fundingCurveSpecification.getCurrency().getUniqueId());
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context,
        final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
      
      result.add(getMarketDataValueRequirement());
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
      final Set<FixedIncomeStrip> strips = getFundingCurveDefinition().getStrips();
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
          //    getFundingCurveDefinitionName(), getFundingCurveDefinitionName());
          derivative = swapConverter.getSwap((SwapSecurity) strip.getSecurity(), getFundingCurveDefinitionName(),
              getFundingCurveDefinitionName(), marketValue / 100., 0.0, now);
        } else if (strip.getInstrumentType() == StripInstrumentType.CASH) {
          derivative = financialSecurity.accept(instrumentAdapter)
              .toDerivative(now, getFundingCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.FRA) {
          derivative = financialSecurity.accept(instrumentAdapter).toDerivative(now,
              getFundingCurveDefinitionName(), getFundingCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
          derivative = financialSecurity.accept(futureAdapter).toDerivative(now, marketValue,
              getFundingCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.LIBOR) {
          derivative = financialSecurity.accept(instrumentAdapter)
              .toDerivative(now, getFundingCurveDefinitionName());
        } else if (strip.getInstrumentType() == StripInstrumentType.TENOR_SWAP) {
          derivative = tenorSwapConverter.getSwap((SwapSecurity) strip.getSecurity(), getFundingCurveDefinitionName(),
              getFundingCurveDefinitionName(), getFundingCurveDefinitionName(), marketValue / 10000., now);
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
      curveKnots.put(getFundingCurveDefinitionName(), nodeTimes);
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
      final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
      final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> sensitivityCalculators =
          new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator = CombinedInterpolatorExtrapolatorFactory
          .getInterpolator(getFundingCurveDefinition().getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
              Interpolator1DFactory.FLAT_EXTRAPOLATOR);
      curveNodes.put(getFundingCurveDefinitionName(), nodeTimes);
      interpolators.put(getFundingCurveDefinitionName(), interpolator);
      // TODO have use finite difference or not as an input [FIN-147]
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> sensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
          .getSensitivityCalculator(getFundingCurveDefinition().getInterpolatorName(),
              Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, false);
      sensitivityCalculators.put(getFundingCurveDefinitionName(), sensitivityCalculator);

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

  

  @SuppressWarnings("unchecked")
  private SnapshotDataBundle buildMarketDataMap(final FunctionInputs inputs) {
    
    Object marketDataBundle = inputs.getValue(getMarketDataValueRequirement());
    return (SnapshotDataBundle) marketDataBundle;
  }

  @Override
  protected Compiled compileImpl(InstantProvider earliest, InstantProvider latest,
      InterpolatedYieldCurveSpecification fundingCurveSpecification, Set<ValueRequirement> fundingCurveRequirements,
      InterpolatedYieldCurveSpecification forwardCurveSpecification, Set<ValueRequirement> forwardCurveRequirements) {
    return new CompiledImpl(earliest, latest, getCurrency(), fundingCurveSpecification, fundingCurveRequirements,
        forwardCurveSpecification, forwardCurveRequirements);
  }

  

}
