/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.config.ConfigSource;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class MulticurveProviderDiscountingFunction extends AbstractFunction {
  private static final String CALCULATION_METHOD = "Discounting"; //TODO move me
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private final String _curveCalculationConfigName;

  public MulticurveProviderDiscountingFunction(final String curveCalculationConfigName) {
    ArgumentChecker.notNull(curveCalculationConfigName, "curve calculation config name");
    _curveCalculationConfigName = curveCalculationConfigName;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final MultiCurveCalculationConfig curveConfig = CurveUtils.getCurveCalculationConfig(atInstant, configSource, atZDT.toLocalDate(), _curveCalculationConfigName);
    final String[] curveNames = curveConfig.getCurveNames();
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        ValueProperties curveProperties = null;
        for (final ValueRequirement desiredValue : desiredValues) {
          if (desiredValue.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
            curveProperties = desiredValue.getConstraints();
            break;
          }
        }
        if (curveProperties == null) {
          throw new OpenGammaRuntimeException("Could not get curve properties from desired values");
        }
        final double absoluteTolerance = Double.parseDouble(Iterables.getOnlyElement(curveProperties.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)));
        final double relativeTolerance = Double.parseDouble(Iterables.getOnlyElement(curveProperties.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)));
        final int maxIterations = Integer.parseInt(Iterables.getOnlyElement(curveProperties.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)));
        final MulticurveDiscountBuildingRepository builder = new MulticurveDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);

        return null;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.NULL;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Set<ValueSpecification> results = new HashSet<>();
        for (final String curveName : curveNames) {
          final ValueProperties properties = getProperties(curveName);
          results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL, properties));
          results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.NULL, properties));
        }
        return results;
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
        final Set<ValueRequirement> requirements = new HashSet<>();
        for (final String curveName : curveNames) {
          final ValueProperties properties = ValueProperties.builder()
              .with(CURVE, curveName)
              .get();
          requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
          requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
        }
        return requirements;
      }

      @SuppressWarnings("synthetic-access")
      private ValueProperties getProperties(final String curveName) {
        return createValueProperties()
            .with(CURVE, curveName)
            .with(CURVE_CALCULATION_METHOD, CALCULATION_METHOD)
            .with(CURVE_CALCULATION_CONFIG, _curveCalculationConfigName)
            .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
            .withAny(PROPERTY_DECOMPOSITION)
            .get();
      }

      private InstrumentDefinition<?>[][][] getInstruments() {
        return null;
      }

      private GeneratorYDCurve[][] getGenerators() {
        return null;
      }

      private String[][] getCurveNames() {
        return null;
      }

      private double[][] getInitialGuesses() {
        return null;
      }

      private MulticurveProviderDiscount getKnownData() {
        return null;
      }

      private Map<String, Currency> getDiscountingMap() {
        return null;
      }

      private Map<String, IborIndex[]> getForwardIborMap() {
        return null;
      }

      private Map<String, IndexON[]> getForwardOvernightMap() {
        return null;
      }

      private Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurves() {
        return null;
      }
    };
  }


}
