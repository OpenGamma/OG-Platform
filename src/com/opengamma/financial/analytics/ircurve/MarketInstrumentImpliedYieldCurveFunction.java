/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.swap.FixedFloatSwapSecurityToSwapConverter;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.LastDateCalculator;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.ParRateDifferenceCalculator;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.financial.world.region.RegionSource;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculatorFactory;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.RootNotFoundException;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 */
public class MarketInstrumentImpliedYieldCurveFunction extends AbstractFunction implements FunctionInvoker {
  private final LocalDate _curveDate;
  private final Currency _currency;
  private final String _name;
  private YieldCurveDefinition _definition;
  private Set<ValueRequirement> _requirements;
  private ValueSpecification _curveResult;
  private ValueSpecification _jacobianResult;
  private Set<ValueSpecification> _results;
  private InterpolatedYieldCurveSpecification _specification;
  private static final LastDateCalculator LAST_DATE_CALCULATOR = new LastDateCalculator();

  public MarketInstrumentImpliedYieldCurveFunction(final LocalDate curveDate, final Currency currency, final String name) {
    Validate.notNull(curveDate, "curve date");
    Validate.notNull(currency, "curve currency");
    Validate.notNull(name, "curve name");
    _curveDate = curveDate;
    _currency = currency;
    _name = name;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBInterpolatedYieldCurveDefinitionSource curveDefinitionSource = new ConfigDBInterpolatedYieldCurveDefinitionSource(configSource);
    _definition = curveDefinitionSource.getDefinition(_currency, _name);
    final ConfigDBInterpolatedYieldCurveSpecificationBuilder curveSpecificationBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
    _specification = curveSpecificationBuilder.buildCurve(_curveDate, _definition);
    _requirements = buildRequirements(_specification, context);
    //TODO not sure if the value requirement name should be yield curve
    _curveResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, _currency), getUniqueIdentifier());
    _jacobianResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, _currency), getUniqueIdentifier());
    _results = new HashSet<ValueSpecification>();
    _results = Sets.newHashSet(_curveResult, _jacobianResult);
  }

  @SuppressWarnings("unchecked")
  public static Set<ValueRequirement> buildRequirements(final InterpolatedYieldCurveSpecification specification, final FunctionCompilationContext context) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStripWithIdentifier strip : specification.getStrips()) {
      result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurity()));
    }
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionBundle conventionBundle = conventionBundleSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, specification.getCurrency().getISOCode()
        + "_SWAP"));
    final ConventionBundle referenceRateConvention = conventionBundleSource.getConventionBundle(IdentifierBundle.of(conventionBundle.getSwapFloatingLegInitialRate()));
    final Identifier initialRefRateId = Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, referenceRateConvention.getIdentifiers().getIdentifier(IdentificationScheme.BLOOMBERG_TICKER));
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, initialRefRateId));
    return Collections.unmodifiableSet(result);
  }

  public InterpolatedYieldCurveSpecification getSpecification() {
    return _specification;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    //TODO copied from SimpleInterpolatedYieldAndDiscountCurveFunction:
    // REVIEW: jim 23-July-2010 is this enough?  Probably not, but I'm not entirely sure what the deal with the Ids is...
    return ObjectUtils.equals(target.getUniqueIdentifier(), getSpecification().getCurrency().getUniqueIdentifier());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _results;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return _name + "_MarketInstrumentImpliedYieldCurveFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  private Map<Identifier, Double> buildMarketDataMap(final FunctionInputs inputs) {
    final Map<Identifier, Double> marketDataMap = new HashMap<Identifier, Double>();
    for (final ComputedValue value : inputs.getAllValues()) {
      final ComputationTargetSpecification targetSpecification = value.getSpecification().getRequirementSpecification().getTargetSpecification();
      if (value.getValue() instanceof Double) {
        marketDataMap.put(targetSpecification.getIdentifier(), (Double) value.getValue());
      }
    }
    return marketDataMap;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FixedIncomeStripIdentifierAndMaturityBuilder builder = new FixedIncomeStripIdentifierAndMaturityBuilder(OpenGammaExecutionContext.getRegionSource(executionContext),
        OpenGammaExecutionContext.getConventionBundleSource(executionContext), executionContext.getSecuritySource());
    final InterpolatedYieldCurveSpecificationWithSecurities specificationWithSecurities = builder.resolveToSecurity(getSpecification(), buildMarketDataMap(inputs));
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
    final Set<FixedIncomeStrip> strips = _definition.getStrips();
    final int n = strips.size();
    final double[] initialRatesGuess = new double[n];
    final double[] nodeTimes = new double[n];
    InterestRateDerivative derivative;
    ValueRequirement stripRequirement;
    int i = 0;
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final FixedFloatSwapSecurityToSwapConverter swapConverter = new FixedFloatSwapSecurityToSwapConverter(holidaySource, regionSource, conventionSource);
    for (final FixedIncomeStripWithSecurity strip : specificationWithSecurities.getStrips()) {
      stripRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurityIdentifier());
      final Double rate = (Double) inputs.getValue(stripRequirement) / 100d;
      if (rate == null) {
        throw new NullPointerException("Could not get market data for " + strip);
      }
      if (strip.getInstrumentType() == StripInstrumentType.SWAP) {
        derivative = swapConverter.getSwap((SwapSecurity) strip.getSecurity(), _name, _name, rate, now);
      } else {
        throw new OpenGammaRuntimeException("Can only handle swaps at the moment");
      }
      if (derivative == null) {
        throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = 0.01;
      nodeTimes[i] = LAST_DATE_CALCULATOR.getValue(derivative); 
      System.err.println("LAST_DATE_CALCULATOR.getValue(derivative) = " + nodeTimes[i]);
      i++;
    }
    ParallelArrayBinarySort.parallelBinarySort(nodeTimes, initialRatesGuess);
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> sensitivityCalculators = 
      new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
//    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName());
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(_definition.getInterpolatorName(), Interpolator1DFactory.LINEAR_EXTRAPOLATOR, 
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    curveNodes.put(_name, nodeTimes);
    interpolators.put(_name, interpolator);
    //TODO have use finite difference or not as an input [FIN-147]
    Interpolator1DNodeSensitivityCalculator sensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory.getSensitivityCalculator(_definition.getInterpolatorName(),
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, false);
    sensitivityCalculators.put(_name, sensitivityCalculator);
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, null, curveNodes, interpolators, sensitivityCalculators);
    //TODO have the calculator and sensitivity calculators as an input [FIN-144], [FIN-145]
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, ParRateDifferenceCalculator.getInstance());
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, ParRateCurveSensitivityCalculator.getInstance());
    NewtonVectorRootFinder rootFinder;
    double[] yields = null;
    try {
      //TODO have the decomposition as an optional input [FIN-146]
      rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.LU_COMMONS_NAME));
      yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess)).getData();
    } catch (final RootNotFoundException e) {  
      rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
      yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess)).getData();
    }
    
    final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(nodeTimes, yields, interpolator);
    final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(yields));
    return Sets.newHashSet(new ComputedValue(_curveResult, curve), new ComputedValue(_jacobianResult, jacobianMatrix.getData()));
  }
}
