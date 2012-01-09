/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.LastTimeCalculator;
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
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class MarketInstrumentImpliedYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {

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

  private InterestRateInstrumentTradeOrSecurityConverter _securityConverter;
  private FixedIncomeConverterDataProvider _definitionConverter;

  public MarketInstrumentImpliedYieldCurveFunction(final String calculationType) {
    _calculationType = calculationType;
    if (calculationType.equals(PAR_RATE_STRING)) {
      _calculator = ParRateCalculator.getInstance();
      _sensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();
      _couponSensitivityCalculator = null;
    } else if (calculationType.equals(PRESENT_VALUE_STRING)) {
      _calculator = PresentValueCalculator.getInstance();
      _sensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();
      _couponSensitivityCalculator = PresentValueCouponSensitivityCalculator.getInstance();
    } else {
      throw new IllegalArgumentException("Could not get calculator type " + calculationType);
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

  protected InterestRateInstrumentTradeOrSecurityConverter getSecurityConverter() {
    return _securityConverter;
  }

  protected FixedIncomeConverterDataProvider getDefinitionConverter() {
    return _definitionConverter;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _securityConverter = new InterestRateInstrumentTradeOrSecurityConverter(holidaySource, conventionSource, regionSource, securitySource);
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
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
    if (getCouponSensitivityCalculator() != null) {
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
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(4);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, fundingCurveName).get()));
    if (forwardCurveName.equals(fundingCurveName)) {
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, fundingCurveName).get()));
    } else {
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, fundingCurveName)
          .withOptional(REQUIREMENT_PROPERTY_TYPE).with(REQUIREMENT_PROPERTY_TYPE, TYPE_FUNDING).get()));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, forwardCurveName)
          .withOptional(REQUIREMENT_PROPERTY_TYPE).with(REQUIREMENT_PROPERTY_TYPE, TYPE_FORWARD).get()));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, forwardCurveName).get()));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String forwardCurveName = null;
    String fundingCurveName = null;
    for (Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE_SPEC.equals(input.getKey().getValueName())) {
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
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(4);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, getCalculationType())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName);
    results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties.get()));
    if (getCouponSensitivityCalculator() != null) {
      results.add(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, properties.get()));
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
    for (ValueRequirement desiredValue : desiredValues) {
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
    InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpecificationWithSecurities = null;
    InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpecificationWithSecurities = null;
    Map<ExternalId, Double> fundingMarketDataMap = null;
    Map<ExternalId, Double> forwardMarketDataMap = null;
    for (ComputedValue input : inputs.getAllValues()) {
      final String curveName = input.getSpecification().getProperty(ValuePropertyNames.CURVE);
      if (ValueRequirementNames.YIELD_CURVE_SPEC.equals(input.getSpecification().getValueName())) {
        if (curveName.equals(fundingCurveName)) {
          assert fundingCurveSpecificationWithSecurities == null;
          fundingCurveSpecificationWithSecurities = (InterpolatedYieldCurveSpecificationWithSecurities) input.getValue();
        }
        if (curveName.equals(forwardCurveName)) {
          assert forwardCurveSpecificationWithSecurities == null;
          forwardCurveSpecificationWithSecurities = (InterpolatedYieldCurveSpecificationWithSecurities) input.getValue();
        }
      } else {
        assert ValueRequirementNames.YIELD_CURVE_MARKET_DATA.equals(input.getSpecification().getValueName());
        if (curveName.equals(fundingCurveName)) {
          assert fundingMarketDataMap == null;
          fundingMarketDataMap = YieldCurveFunctionHelper.buildMarketDataMap((SnapshotDataBundle) input.getValue());
        }
        if (curveName.equals(forwardCurveName)) {
          assert forwardMarketDataMap == null;
          forwardMarketDataMap = YieldCurveFunctionHelper.buildMarketDataMap((SnapshotDataBundle) input.getValue());
        }
      }
    }
    assert fundingCurveSpecificationWithSecurities != null;
    assert forwardCurveSpecificationWithSecurities != null;
    assert fundingMarketDataMap != null;
    assert forwardMarketDataMap != null;
    if (forwardCurveName.equals(fundingCurveName)) {
      return execute(executionContext, target.toSpecification(), forwardCurveName, forwardCurveSpecificationWithSecurities, forwardMarketDataMap, createForward, createJacobian, createSensitivities);
    } else {
      return execute(executionContext, target.toSpecification(), forwardCurveName, forwardCurveSpecificationWithSecurities, forwardMarketDataMap, fundingCurveName,
          fundingCurveSpecificationWithSecurities, fundingMarketDataMap, createForward, createFunding, createJacobian, createSensitivities);
    }
  }

  private static Interpolator1D getInterpolator(final InterpolatedYieldCurveSpecificationWithSecurities specification) {
    return new CombinedInterpolatorExtrapolator(specification.getInterpolator(), new FlatExtrapolator1D(), new FlatExtrapolator1D());
  }

  private Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final ComputationTargetSpecification targetSpec, final String curveName,
      final InterpolatedYieldCurveSpecificationWithSecurities specificationWithSecurities, final Map<ExternalId, Double> marketDataMap, final boolean createYieldCurve, final boolean createJacobian,
      final boolean createSensitivities) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final List<InstrumentDerivative> derivatives = new ArrayList<InstrumentDerivative>();
    final int n = specificationWithSecurities.getStrips().size();
    final double[] initialRatesGuess = new double[n];
    final double[] nodeTimes = new double[n];
    final double[] marketValues = new double[n];
    int i = 0;
    for (final FixedIncomeStripWithSecurity strip : specificationWithSecurities.getStrips()) {
      final Double marketValue = marketDataMap.get(strip.getSecurityIdentifier());
      if (marketValue == null) {
        throw new NullPointerException("Could not get market data for " + strip);
      }
      InstrumentDerivative derivative;
      final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
      final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForFundingCurveInstrument(strip.getInstrumentType(), curveName, curveName);
      final InstrumentDefinition<?> definition = getSecurityConverter().visit(financialSecurity);
      derivative = getDefinitionConverter().convert(financialSecurity, definition, now, curveNames, dataSource);
      if (derivative == null) {
        throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
      }
      if (PRESENT_VALUE_STRING.equals(getCalculationType())) {
        marketValues[i] = 0;
      } else {
        marketValues[i] = marketValue;
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = 0.01;
      nodeTimes[i] = LAST_DATE_CALCULATOR.visit(derivative);
      i++;
    }
    ParallelArrayBinarySort.parallelBinarySort(nodeTimes, initialRatesGuess);
    final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<String, double[]>();
    curveKnots.put(curveName, nodeTimes);
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<String, Interpolator1D>();
    curveNodes.put(curveName, nodeTimes);
    interpolators.put(curveName, getInterpolator(specificationWithSecurities));
    // TODO have use finite difference or not as an input [FIN-147]
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues, null, curveNodes, interpolators, false);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, getCalculator());
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, getSensitivityCalculator());
    NewtonVectorRootFinder rootFinder;
    double[] yields = null;
    try {
      // TODO have the decomposition as an optional input [FIN-146]
      rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.LU_COMMONS_NAME));
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
      curve = new YieldCurve(InterpolatedDoublesCurve.from(nodeTimes, yields, getInterpolator(specificationWithSecurities)));
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
      final String[] curveNames = new String[] {curveName, curveName};
      final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {curve, curve};
      final YieldCurveBundle curveBundle = new YieldCurveBundle(curveNames, curves);
      for (final InstrumentDerivative derivative : derivatives) {
        couponSensitivities[ii++] = getCouponSensitivityCalculator().visit(derivative, curveBundle);
      }
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, properties.get()), new DoubleMatrix1D(couponSensitivities)));
    }
    if (createYieldCurve) {
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties.with(ValuePropertyNames.CURVE, curveName).get()), curve));
    }
    return result;
  }

  private Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final ComputationTargetSpecification targetSpec, final String forwardCurveName,
      final InterpolatedYieldCurveSpecificationWithSecurities forwardCurveSpecificationWithSecurities, final Map<ExternalId, Double> forwardMarketDataMap, final String fundingCurveName,
      final InterpolatedYieldCurveSpecificationWithSecurities fundingCurveSpecificationWithSecurities, final Map<ExternalId, Double> fundingMarketDataMap, final boolean createForwardYieldCurve,
      final boolean createFundingYieldCurve, final boolean createJacobian, final boolean createSensitivities) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final List<InstrumentDerivative> derivatives = new ArrayList<InstrumentDerivative>();
    final int nFunding = fundingCurveSpecificationWithSecurities.getStrips().size();
    final int nForward = forwardCurveSpecificationWithSecurities.getStrips().size();
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
      InstrumentDerivative derivative;
      final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForFundingCurveInstrument(strip.getInstrumentType(), fundingCurveName, forwardCurveName);
      final InstrumentDefinition<?> definition = getSecurityConverter().visit(financialSecurity);
      derivative = getDefinitionConverter().convert(financialSecurity, definition, now, curveNames, dataSource);
      if (derivative == null) {
        throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
      }
      if (PRESENT_VALUE_STRING.equals(getCalculationType())) {
        marketValues[i] = 0;
      } else {
        marketValues[i] = marketValue;
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = marketValue;
      i++;
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
      InstrumentDerivative derivative = null;
      final String[] curveNames = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForForwardCurveInstrument(strip.getInstrumentType(), fundingCurveName, forwardCurveName);
      try {
        final InstrumentDefinition<?> definition = getSecurityConverter().visit(financialSecurity);
        derivative = getDefinitionConverter().convert(financialSecurity, definition, now, curveNames, dataSource);
      } catch (Exception e) {
        s_logger.error("Caught exception {} for {}", e, financialSecurity);
      }
      if (derivative == null) {
        throw new OpenGammaRuntimeException("Had a null InterestRateDefinition for " + strip);
      }
      if (PRESENT_VALUE_STRING.equals(getCalculationType())) {
        marketValues[i] = 0;
      } else {
        marketValues[i] = marketValue;
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = marketValues[i];
      i++;
      forwardNodeTimes[forwardIndex] = LAST_DATE_CALCULATOR.visit(derivative);
      forwardIndex++;
    }
    //Arrays.sort(fundingNodeTimes);
    //Arrays.sort(forwardNodeTimes);
    // ParallelArrayBinarySort.parallelBinarySort(fundingNodeTimes, initialRatesGuess); //TODO will eventually need two sets of rates guesses
    // ParallelArrayBinarySort.parallelBinarySort(fundingNodeTimes, initialRatesGuess); //TODO will eventually need two sets of rates guesses
    final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<String, double[]>();
    curveKnots.put(fundingCurveName, fundingNodeTimes);
    curveKnots.put(forwardCurveName, forwardNodeTimes);
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<String, Interpolator1D>();
    curveNodes.put(fundingCurveName, fundingNodeTimes);
    interpolators.put(fundingCurveName, getInterpolator(fundingCurveSpecificationWithSecurities));
    curveNodes.put(forwardCurveName, forwardNodeTimes);
    interpolators.put(forwardCurveName, getInterpolator(forwardCurveSpecificationWithSecurities));
    // TODO have use finite difference or not as an input [FIN-147]
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues, null, curveNodes, interpolators, false);
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
      fundingCurve = new YieldCurve(InterpolatedDoublesCurve.from(fundingNodeTimes, fundingYields, getInterpolator(fundingCurveSpecificationWithSecurities)));
    } else {
      fundingCurve = null;
    }
    final YieldAndDiscountCurve forwardCurve;
    if (createSensitivities || createForwardYieldCurve) {
      final double[] forwardYields = Arrays.copyOfRange(yields, fundingNodeTimes.length, yields.length);
      forwardCurve = new YieldCurve(InterpolatedDoublesCurve.from(forwardNodeTimes, forwardYields, getInterpolator(forwardCurveSpecificationWithSecurities)));
    } else {
      forwardCurve = null;
    }
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(4);
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, getCalculationType())
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName).with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName);
    if (createJacobian) {
      result
          .add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties.get()), jacobianCalculator.evaluate(new DoubleMatrix1D(yields)).getData()));
    }
    if (createSensitivities) {
      final double[] couponSensitivities = new double[derivatives.size()];
      int ii = 0;
      final String[] curveNames = new String[] {forwardCurveName, fundingCurveName};
      final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {forwardCurve, fundingCurve};
      final YieldCurveBundle curveBundle = new YieldCurveBundle(curveNames, curves);
      for (final InstrumentDerivative derivative : derivatives) {
        couponSensitivities[ii++] = getCouponSensitivityCalculator().visit(derivative, curveBundle);
      }
      result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, targetSpec, properties.get()), new DoubleMatrix1D(couponSensitivities)));
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
