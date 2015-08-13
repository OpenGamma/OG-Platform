/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_JACOBIAN;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
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
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
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
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Constructs a single yield curve and its Jacobian from an FX-implied yield curve calculation configuration and a yield curve definition that contains <b>only</b> {@link StripInstrumentType#CASH}
 * strips. The transformation of the yield curve allows risk to be displayed with respect to implied deposit rates, not FX forwards.
 */
public class ImpliedDepositCurveFunction extends AbstractFunction {
  /** The calculation method property value */
  public static final String IMPLIED_DEPOSIT = "ImpliedDeposit";
  /** The Cash instrument method */
  private static final CashDiscountingMethod METHOD_CASH = CashDiscountingMethod.getInstance();
  /** Calculates the par rate */
  private static final ParRateCalculator PAR_RATE_CALCULATOR = ParRateCalculator.getInstance();
  /** Calculates the sensitivity of the par rate to the curves */
  private static final ParRateCurveSensitivityCalculator PAR_RATE_SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();
  /** The business day convention used for FX forward dates computation **/
  private static final BusinessDayConvention MOD_FOL = BusinessDayConventions.MODIFIED_FOLLOWING;
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ImpliedDepositCurveFunction.class);
  /** The curve name */
  private final String _curveCalculationConfig;

  private ConfigSourceQuery<MultiCurveCalculationConfig> _multiCurveCalculationConfig;
  private ConfigSourceQuery<YieldCurveDefinition> _yieldCurveDefinition;

  /**
   * @param curveCalculationConfig The curve name, not null
   */
  public ImpliedDepositCurveFunction(final String curveCalculationConfig) {
    ArgumentChecker.notNull(curveCalculationConfig, "curve name");
    _curveCalculationConfig = curveCalculationConfig;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _multiCurveCalculationConfig = ConfigSourceQuery.init(context, this, MultiCurveCalculationConfig.class);
    _yieldCurveDefinition = ConfigSourceQuery.init(context, this, YieldCurveDefinition.class);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final MultiCurveCalculationConfig impliedConfiguration = _multiCurveCalculationConfig.get(_curveCalculationConfig);
    if (impliedConfiguration == null) {
      throw new OpenGammaRuntimeException("Multi-curve calculation called " + _curveCalculationConfig + " was null");
    }
    ComputationTarget target = context.getComputationTargetResolver().resolve(impliedConfiguration.getTarget());
    if (!(target.getValue() instanceof Currency)) {
      throw new OpenGammaRuntimeException("Target of curve calculation configuration was not a currency");
    }
    final Currency impliedCurrency = (Currency) target.getValue();
    if (!IMPLIED_DEPOSIT.equals(impliedConfiguration.getCalculationMethod())) {
      throw new OpenGammaRuntimeException("Curve calculation method was not " + IMPLIED_DEPOSIT + " for configuration called " + _curveCalculationConfig);
    }
    final String[] impliedCurveNames = impliedConfiguration.getYieldCurveNames();
    if (impliedCurveNames.length != 1) {
      throw new OpenGammaRuntimeException("Can only handle configurations with a single implied curve");
    }
    final LinkedHashMap<String, String[]> originalConfigurationName = impliedConfiguration.getExogenousConfigData();
    if (originalConfigurationName == null || originalConfigurationName.size() != 1) {
      throw new OpenGammaRuntimeException("Need a configuration with one exogenous configuration");
    }
    final Map.Entry<String, String[]> entry = Iterables.getOnlyElement(originalConfigurationName.entrySet());
    final String[] originalCurveNames = entry.getValue();
    if (originalCurveNames.length != 1) {
      s_logger.warn("Found more than one exogenous configuration name; using only the first");
    }
    final MultiCurveCalculationConfig originalConfiguration = _multiCurveCalculationConfig.get(entry.getKey());
    if (originalConfiguration == null) {
      throw new OpenGammaRuntimeException("Multi-curve calculation called " + entry.getKey() + " was null");
    }
    target = context.getComputationTargetResolver().resolve(originalConfiguration.getTarget());
    if (!(target.getValue() instanceof Currency)) {
      throw new OpenGammaRuntimeException("Target of curve calculation configuration was not a currency");
    }
    final Currency originalCurrency = (Currency) target.getValue();
    if (!originalCurrency.equals(impliedCurrency)) {
      throw new OpenGammaRuntimeException("Currency targets for configurations " + _curveCalculationConfig + " and " + entry.getKey() + " did not match");
    }
    final YieldCurveDefinition impliedDefinition = _yieldCurveDefinition.get(impliedCurveNames[0] + "_" + impliedCurrency.getCode());
    if (impliedDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get implied definition called " + impliedCurveNames[0] + "_" + impliedCurrency.getCode());
    }
    final Set<FixedIncomeStrip> strips = impliedDefinition.getStrips();
    for (final FixedIncomeStrip strip : strips) {
      if (strip.getInstrumentType() != StripInstrumentType.CASH) {
        throw new OpenGammaRuntimeException("Can only handle yield curve definitions with CASH strips");
      }
    }
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new MyCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), impliedConfiguration, impliedDefinition,
        originalConfiguration, originalCurveNames[0]);
  };

  private class MyCompiledFunction extends AbstractInvokingCompiledFunction {
    /** The definition of the implied curve */
    private final YieldCurveDefinition _impliedDefinition;
    /** The implied curve calculation configuration */
    private final MultiCurveCalculationConfig _impliedConfiguration;
    /** The original curve calculation configuration */
    private final MultiCurveCalculationConfig _originalConfiguration;
    /** The implied curve name */
    private final String _impliedCurveName;
    /** The original curve name */
    private final String _originalCurveName;
    /** The currency */
    private final Currency _currency;
    /** The interpolator */
    private final String _interpolatorName;
    /** The left extrapolator */
    private final String _leftExtrapolatorName;
    /** The right extrapolator */
    private final String _rightExtrapolatorName;

    public MyCompiledFunction(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final MultiCurveCalculationConfig impliedConfiguration,
        final YieldCurveDefinition impliedDefinition, final MultiCurveCalculationConfig originalConfiguration, final String originalCurveName) {
      super(earliestInvokation, latestInvokation);
      _impliedConfiguration = impliedConfiguration;
      _impliedDefinition = impliedDefinition;
      _originalConfiguration = originalConfiguration;
      _impliedCurveName = impliedDefinition.getName();
      _originalCurveName = originalCurveName;
      _currency = impliedDefinition.getCurrency();
      _interpolatorName = impliedDefinition.getInterpolatorName();
      _leftExtrapolatorName = impliedDefinition.getLeftExtrapolatorName();
      _rightExtrapolatorName = impliedDefinition.getRightExtrapolatorName();
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
        throws AsynchronousExecution {
      final Object originalCurveObject = inputs.getValue(YIELD_CURVE);
      if (originalCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get original curve");
      }
      ValueProperties resultCurveProperties = null;
      String absoluteToleranceName = null;
      String relativeToleranceName = null;
      String iterationsName = null;
      String decompositionName = null;
      String useFiniteDifferenceName = null;
      for (final ValueRequirement desiredValue : desiredValues) {
        if (desiredValue.getValueName().equals(YIELD_CURVE)) {
          absoluteToleranceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
          relativeToleranceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
          iterationsName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
          decompositionName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION);
          useFiniteDifferenceName = desiredValue.getConstraint(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE);
          resultCurveProperties = desiredValue.getConstraints().copy().get();
          break;
        }
      }
      if (resultCurveProperties == null) {
        throw new OpenGammaRuntimeException("Could not get result curve properties");
      }
      final ValueProperties resultJacobianProperties = resultCurveProperties.withoutAny(CURVE);
      ZonedDateTime valuationDateTime = executionContext.getValuationTime().atZone(executionContext.getValuationClock().getZone());
      final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
      final Calendar calendar = CalendarUtils.getCalendar(holidaySource, _currency);
      final DepositConvention convention = conventionSource.getSingle(ExternalId.of(SCHEME_NAME, getConventionName(_currency, DEPOSIT)), DepositConvention.class);
      final int spotLag = convention.getSettlementDays();
      final ExternalId conventionSettlementRegion = convention.getRegionCalendar();
      ZonedDateTime spotDate;
      if (spotLag == 0 && conventionSettlementRegion == null) {
        spotDate = valuationDateTime;
      } else {
        spotDate = ScheduleCalculator.getAdjustedDate(valuationDateTime, spotLag, calendar);;
      }
      final YieldCurveBundle curves = new YieldCurveBundle();
      final String fullYieldCurveName = _originalCurveName + "_" + _currency;
      curves.setCurve(fullYieldCurveName, (YieldAndDiscountCurve) originalCurveObject);
      final int n = _impliedDefinition.getStrips().size();
      final double[] t = new double[n];
      final double[] r = new double[n];
      int i = 0;
      final DayCount dayCount = DayCounts.ACT_360; //TODO: Get the convention from the curve.

      final String impliedDepositCurveName = _curveCalculationConfig + "_" + _currency.getCode();
      final List<InstrumentDerivative> derivatives = new ArrayList<>();

      for (final FixedIncomeStrip strip : _impliedDefinition.getStrips()) {
        final Tenor tenor = strip.getCurveNodePointTime();
        final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(spotDate, tenor.getPeriod(), MOD_FOL, calendar, true);
        final double startTime = TimeCalculator.getTimeBetween(valuationDateTime, spotDate);
        final double endTime = TimeCalculator.getTimeBetween(valuationDateTime, paymentDate);
        final double accrualFactor = dayCount.getDayCountFraction(spotDate, paymentDate, calendar);
        final Cash cashFXCurve = new Cash(_currency, startTime, endTime, 1, 0, accrualFactor, fullYieldCurveName);
        final double parRate = METHOD_CASH.parRate(cashFXCurve, curves);
        final Cash cashDepositCurve = new Cash(_currency, startTime, endTime, 1, 0, accrualFactor, impliedDepositCurveName);
        derivatives.add(cashDepositCurve);
        t[i] = endTime;
        r[i++] = parRate;
      }
      final CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(_interpolatorName, _leftExtrapolatorName, _rightExtrapolatorName);
      final double absoluteTolerance = Double.parseDouble(absoluteToleranceName);
      final double relativeTolerance = Double.parseDouble(relativeToleranceName);
      final int iterations = Integer.parseInt(iterationsName);
      final Decomposition<?> decomposition = DecompositionFactory.getDecomposition(decompositionName);
      final boolean useFiniteDifference = Boolean.parseBoolean(useFiniteDifferenceName);
      final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<>();
      final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
      curveNodes.put(impliedDepositCurveName, t);
      interpolators.put(impliedDepositCurveName, interpolator);
      final FXMatrix fxMatrix = new FXMatrix();
      final YieldCurveBundle knownCurve = new YieldCurveBundle();
      final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, r, knownCurve, curveNodes, interpolators, useFiniteDifference, fxMatrix);
      final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(absoluteTolerance, relativeTolerance, iterations, decomposition);
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PAR_RATE_CALCULATOR);
      final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, PAR_RATE_SENSITIVITY_CALCULATOR);
      final double[] fittedYields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(r)).getData();
      final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(fittedYields));
      final YieldCurve impliedDepositCurve = new YieldCurve(impliedDepositCurveName, InterpolatedDoublesCurve.from(t, fittedYields, interpolator));
      final ValueSpecification curveSpec = new ValueSpecification(YIELD_CURVE, target.toSpecification(), resultCurveProperties);
      final ValueSpecification jacobianSpec = new ValueSpecification(YIELD_CURVE_JACOBIAN, target.toSpecification(), resultJacobianProperties);
      return Sets.newHashSet(new ComputedValue(curveSpec, impliedDepositCurve), new ComputedValue(jacobianSpec, jacobianMatrix));
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.CURRENCY;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      final ValueProperties curveProperties = getCurveProperties(_impliedCurveName, _impliedConfiguration.getCalculationConfigName());
      final ValueProperties jacobianProperties = getJacobianProperties(_impliedConfiguration.getCalculationConfigName());
      final ComputationTargetSpecification targetSpec = target.toSpecification();
      final ValueSpecification curve = new ValueSpecification(YIELD_CURVE, targetSpec, curveProperties);
      final ValueSpecification jacobian = new ValueSpecification(YIELD_CURVE_JACOBIAN, targetSpec, jacobianProperties);
      return Sets.newHashSet(curve, jacobian);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
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
      if (!_originalConfiguration.getTarget().equals(target.toSpecification())) {
        s_logger.info("Invalid target, was {} - expected {}", target, _originalConfiguration.getTarget());
        return null;
      }
      final ValueProperties properties = ValueProperties.builder().with(CURVE_CALCULATION_METHOD, _originalConfiguration.getCalculationMethod())
          .with(CURVE_CALCULATION_CONFIG, _originalConfiguration.getCalculationConfigName()).with(CURVE, _originalCurveName).get();
      return Collections.singleton(new ValueRequirement(YIELD_CURVE, target.toSpecification(), properties));
    }

    /**
     * Gets the properties of the implied yield curve.
     * 
     * @param curveName The implied curve name
     * @return The properties
     */
    private ValueProperties getCurveProperties(final String curveName, final String curveCalculationConfig) {
      return createValueProperties().with(CURVE_CALCULATION_METHOD, IMPLIED_DEPOSIT).with(CURVE, curveName).with(CURVE_CALCULATION_CONFIG, curveCalculationConfig)
          .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(PROPERTY_DECOMPOSITION)
          .withAny(PROPERTY_USE_FINITE_DIFFERENCE).with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, _interpolatorName)
          .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, _leftExtrapolatorName).with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, _rightExtrapolatorName).get();
    }

    /**
     * Gets the properties of the Jacobian with no values set.
     * 
     * @return The properties.
     */
    private ValueProperties getJacobianProperties(final String curveCalculationConfig) {
      return createValueProperties().with(CURVE_CALCULATION_METHOD, IMPLIED_DEPOSIT).with(CURVE_CALCULATION_CONFIG, curveCalculationConfig).withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE).withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(PROPERTY_DECOMPOSITION).withAny(PROPERTY_USE_FINITE_DIFFERENCE)
          .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, _interpolatorName).with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, _leftExtrapolatorName)
          .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, _rightExtrapolatorName).get();
    }
  }
}
