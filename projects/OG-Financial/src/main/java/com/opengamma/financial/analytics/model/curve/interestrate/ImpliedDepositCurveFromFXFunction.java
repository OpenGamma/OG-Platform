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
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction.FX_IMPLIED;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE;

import java.util.HashSet;
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
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.view.ConfigDocumentWatchSetProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Constructs a single yield curve and its Jacobian from an FX-implied yield curve calculation configuration
 * and a yield curve definition that contains <b>only</b> {@link StripInstrumentType#CASH} strips. The transformation
 * of the yield curve allows risk to be displayed with respect to implied deposit rates, not FX forwards.
 */
public class ImpliedDepositCurveFromFXFunction extends AbstractFunction {
  /** The calculation method property value */
  public static final String IMPLIED_DEPOSIT = "ImpliedDeposit";
  /** The value property name for the yield curve definitions that the implied curve is transformed into */
  public static final String PROPERTY_IMPLIED_DEPOSIT_CURVE = "ImpliedCurveDefinition";
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ImpliedDepositCurveFromFXFunction.class);
  /** The curve name */
  private final String _curveName;
  /** The currency */
  private final Currency _currency;

  /**
   * @param curveName The curve name, not null
   * @param currency The currency string, not null
   */
  public ImpliedDepositCurveFromFXFunction(final String curveName, final String currency) {
    ArgumentChecker.notNull(curveName, "curve name");
    ArgumentChecker.notNull(currency, "currency");
    _curveName = curveName;
    _currency = Currency.of(currency);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, this, YieldCurveDefinition.class);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ConfigSource configurationSource = OpenGammaCompilationContext.getConfigSource(context);
    final YieldCurveDefinition definition = configurationSource.getLatestByName(YieldCurveDefinition.class, _curveName + "_" + _currency.getCode());
    final Set<FixedIncomeStrip> strips = definition.getStrips();
    for (final FixedIncomeStrip strip : strips) {
      if (strip.getInstrumentType() == StripInstrumentType.CASH) {
        throw new OpenGammaRuntimeException("Can only handle yield curve definitions with CASH strips");
      }
    }
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        Object domesticCurveObject = null;
        Object foreignCurveObject = null;
        final Currency domesticCurrency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
        Currency foreignCurrency = null;
        ComputationTargetSpecification foreignSpec = null;
        for (final ComputedValue input : inputs.getAllValues()) {
          if (input.getSpecification().getValueName().equals(YIELD_CURVE)) {
            foreignSpec = input.getSpecification().getTargetSpecification();
            final Currency currencySpec = ComputationTargetType.CURRENCY.resolve(foreignSpec.getUniqueId());
            if (!currencySpec.equals(domesticCurrency)) {
              if (foreignCurrency != null) {
                throw new OpenGammaRuntimeException("Foreign currency was already set");
              }
              foreignCurrency = currencySpec;
              foreignCurveObject = input.getValue();
            } else {
              domesticCurveObject = input.getValue();
            }
          }
        }
        if (foreignCurrency == null) {
          throw new OpenGammaRuntimeException("Could not find foreign currency curves");
        }
        final Object domesticJacobianObject = inputs.getValue(new ValueRequirement(YIELD_CURVE_JACOBIAN, target.toSpecification()));
        final Object foreignJacobianObject = inputs.getValue(new ValueRequirement(YIELD_CURVE_JACOBIAN, foreignSpec));
        final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
        final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
        final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
        final Calendar calendar = CalendarUtils.getCalendar(holidaySource, domesticCurrency, foreignCurrency);
        final FXSpotConvention fxSpotConvention = (FXSpotConvention) conventionSource.getConvention(ExternalId.of("CONVENTION", "FX Spot"));
        final int spotLag = fxSpotConvention.getSettlementDays();
        final ExternalId conventionSettlementRegion = fxSpotConvention.getSettlementRegion();
        ZonedDateTime spotDate;
        if (spotLag == 0 && conventionSettlementRegion == null) {
          spotDate = now;
        } else {
          spotDate = ScheduleCalculator.getAdjustedDate(now, spotLag, calendar);
        }

        for (final FixedIncomeStrip strip : definition.getStrips()) {

//          final Cash cash = new Cash(domesticCurrency, startTime, endTime, 1, 0, accrualFactor, yieldCurveName);
        }
        return null;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.CURRENCY;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final ValueProperties curveProperties = getCurveProperties();
        final ValueProperties jacobianProperties = getJacobianProperties();
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        final ValueSpecification curve = new ValueSpecification(YIELD_CURVE, targetSpec, curveProperties);
        final ValueSpecification jacobian = new ValueSpecification(YIELD_CURVE_JACOBIAN, targetSpec, jacobianProperties);
        return Sets.newHashSet(curve, jacobian);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> impliedDepositDefinitionName = constraints.getValues(PROPERTY_IMPLIED_DEPOSIT_CURVE);
        if (impliedDepositDefinitionName == null || impliedDepositDefinitionName.size() != 1) {
          return null;
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
        final Set<String> domesticCurveCalculationConfigNames = constraints.getValues(CURVE_CALCULATION_CONFIG);
        if (domesticCurveCalculationConfigNames == null || domesticCurveCalculationConfigNames.size() != 1) {
          return null;
        }
        final String domesticCurveCalculationConfigName = Iterables.getOnlyElement(domesticCurveCalculationConfigNames);
        final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
        final MultiCurveCalculationConfig domesticConfig = configSource.getLatestByName(MultiCurveCalculationConfig.class, domesticCurveCalculationConfigName);
        if (domesticConfig == null) {
          s_logger.error("Could not get domestic curve calculation config called {}", domesticCurveCalculationConfigName);
          return null;
        }
        if (!domesticConfig.getCalculationMethod().equals(FX_IMPLIED)) {
          s_logger.error("Curve calculation method for " + domesticCurveCalculationConfigName + " was not FXImplied; cannot produce implied deposit curve");
          return null;
        }
        if (domesticConfig.getExogenousConfigData() == null) {
          s_logger.error("Need an externally-supplied curve to imply data; tried {}", domesticCurveCalculationConfigName);
          return null;
        }
        if (domesticConfig.getYieldCurveNames().length != 1) {
          s_logger.error("Can only handle one curve at the moment");
          return null;
        }
        if (!domesticConfig.getTarget().equals(target.toSpecification())) {
          s_logger.info("Invalid target, was {} - expected {}", target, domesticConfig.getTarget());
          return null;
        }
        final Map<String, String[]> exogenousConfigs = domesticConfig.getExogenousConfigData();
        if (exogenousConfigs.size() != 1) {
          s_logger.error("Can only handle curves with one foreign curve config");
          return null;
        }
        final Map.Entry<String, String[]> foreignCurveConfigNames = exogenousConfigs.entrySet().iterator().next();
        final MultiCurveCalculationConfig foreignConfig = configSource.getLatestByName(MultiCurveCalculationConfig.class, foreignCurveConfigNames.getKey());
        if (foreignConfig == null) {
          s_logger.error("Foreign config was null; tried {}", foreignCurveConfigNames.getKey());
          return null;
        }
        final ComputationTargetSpecification foreignCurrencySpec = foreignConfig.getTarget();
        if (!foreignCurrencySpec.getType().isTargetType(ComputationTargetType.CURRENCY)) {
          s_logger.error("Can only handle curves with currencies as ids at the moment");
          return null;
        }
        final String domesticCurveName = domesticConfig.getYieldCurveNames()[0];
        final String foreignCurveName = foreignCurveConfigNames.getValue()[0];
        final Currency domesticCurrency = target.getValue(ComputationTargetType.CURRENCY);
        final Set<ValueRequirement> requirements = new HashSet<>();
        final Currency foreignCurrency = ComputationTargetType.CURRENCY.resolve(foreignCurrencySpec.getUniqueId());
        final ValueProperties domesticCurveProperties = getInputCurveProperties(domesticConfig, domesticCurveName);
        final ValueProperties domesticJacobianProperties = getInputJacobianProperties(domesticConfig);
        final ValueProperties foreignCurveProperties = getInputCurveProperties(foreignConfig, foreignCurveName);
        final ValueProperties foreignJacobianProperties = getInputJacobianProperties(foreignConfig);
        final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(domesticCurrency, foreignCurrency);
        final ComputationTargetSpecification domesticCurrencyTarget = target.toSpecification();
        final ComputationTargetSpecification foreignCurrencyTarget = ComputationTargetSpecification.of(foreignCurrency);
        requirements.add(new ValueRequirement(YIELD_CURVE, domesticCurrencyTarget, domesticCurveProperties));
        requirements.add(new ValueRequirement(YIELD_CURVE_JACOBIAN, domesticCurrencyTarget, domesticJacobianProperties));
        requirements.add(new ValueRequirement(YIELD_CURVE, foreignCurrencyTarget, foreignCurveProperties));
        requirements.add(new ValueRequirement(YIELD_CURVE_JACOBIAN, foreignCurrencyTarget, foreignJacobianProperties));
        return requirements;
      }

      /**
       * Gets the properties for an input curve.
       * @param configName The curve configuration name
       * @param curveName The curve name
       * @return The curve properties
       */
      private ValueProperties getInputCurveProperties(final MultiCurveCalculationConfig configName, final String curveName) {
        return ValueProperties.builder()
            .with(CURVE, curveName)
            .with(CURVE_CALCULATION_CONFIG, configName.getCalculationConfigName())
            .with(CURVE_CALCULATION_METHOD, configName.getCalculationMethod())
            .get();
      }

      /**
       * Gets the properties for the curve configuration Jacobian.
       * @param configName The curve configuration name
       * @return The Jacobian properties
       */
      private ValueProperties getInputJacobianProperties(final MultiCurveCalculationConfig configName) {
        return ValueProperties.builder()
            .with(CURVE_CALCULATION_CONFIG, configName.getCalculationConfigName())
            .with(CURVE_CALCULATION_METHOD, configName.getCalculationMethod())
            .get();
      }

      /**
       * Gets the properties of the implied yield curve with no values set.
       * @return The properties
       */
      private ValueProperties getCurveProperties() {
        return createValueProperties()
            .with(CURVE_CALCULATION_METHOD, IMPLIED_DEPOSIT)
            .with(PROPERTY_IMPLIED_DEPOSIT_CURVE, _curveName)
            .withAny(CURVE)
            .withAny(CURVE_CALCULATION_CONFIG)
            .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
            .withAny(PROPERTY_DECOMPOSITION)
            .withAny(PROPERTY_USE_FINITE_DIFFERENCE)
            .withAny(X_INTERPOLATOR_NAME)
            .withAny(LEFT_X_EXTRAPOLATOR_NAME)
            .withAny(RIGHT_X_EXTRAPOLATOR_NAME).get();
      }

      /**
       * Gets the properties of the Jacobian with no values set.
       * @return The properties.
       */
      private ValueProperties getJacobianProperties() {
        return createValueProperties()
            .with(CURVE_CALCULATION_METHOD, IMPLIED_DEPOSIT)
            .with(PROPERTY_IMPLIED_DEPOSIT_CURVE, _curveName)
            .withAny(CURVE_CALCULATION_CONFIG)
            .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
            .withAny(PROPERTY_DECOMPOSITION)
            .withAny(PROPERTY_USE_FINITE_DIFFERENCE)
            .withAny(X_INTERPOLATOR_NAME)
            .withAny(LEFT_X_EXTRAPOLATOR_NAME)
            .withAny(RIGHT_X_EXTRAPOLATOR_NAME).get();
      }
    };
  }
}
