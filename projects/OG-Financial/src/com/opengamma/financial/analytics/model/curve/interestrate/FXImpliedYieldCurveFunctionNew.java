/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.InterpolatedCurveAndSurfaceProperties;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class FXImpliedYieldCurveFunctionNew extends AbstractFunction.NonCompiledInvoker {
  /** Property name for the calculation method */
  public static final String FX_IMPLIED = "FXImplied";
  private static final Logger s_logger = LoggerFactory.getLogger(FXImpliedYieldCurveFunctionNew.class);
  private static final ParRateCalculator PAR_RATE_CALCULATOR = ParRateCalculator.getInstance();
  private static final ParRateCurveSensitivityCalculator PAR_RATE_SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String domesticCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final Currency domesticCurrency = Currency.of(target.getUniqueId().getValue());
    Object foreignCurveObject = null;
    Currency foreignCurrency = null;
    String foreignCurveName = null;
    for (final ComputedValue values : inputs.getAllValues()) {
      final ValueSpecification specification = values.getSpecification();
      if (specification.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
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
    final String interpolatorName = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBFXForwardCurveDefinitionSource fxCurveDefinitionSource = new ConfigDBFXForwardCurveDefinitionSource(configSource);
    final ConfigDBFXForwardCurveSpecificationSource fxCurveSpecificationSource = new ConfigDBFXForwardCurveSpecificationSource(configSource);
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(domesticCurrency, foreignCurrency);
    final FXForwardCurveDefinition definition = fxCurveDefinitionSource.getDefinition(domesticCurveName, currencyPair.toString());
    if (definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + domesticCurveName + " for target " + target);
    }
    final FXForwardCurveSpecification specification = fxCurveSpecificationSource.getSpecification(domesticCurveName, currencyPair.toString());
    if (specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + domesticCurveName + " for target " + target);
    }
    final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
    final ValueRequirement spotRequirement = new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument());
    if (inputs.getValue(spotRequirement) == null) {
      throw new OpenGammaRuntimeException("Could not get value for spot; requirement was " + spotRequirement);
    }
    final double spotFX = (Double) inputs.getValue(spotRequirement);
    final Object dataObject = inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA);
    if (dataObject == null) {
      throw new OpenGammaRuntimeException("Could not get FX forward market data");
    }
    final YieldAndDiscountCurve foreignCurve = (YieldAndDiscountCurve) foreignCurveObject;
    @SuppressWarnings("unchecked")
    final Map<ExternalId, Double> fxForwardData = (Map<ExternalId, Double>) dataObject;
    final DoubleArrayList marketValues = new DoubleArrayList();
    final DoubleArrayList nodeTimes = new DoubleArrayList();
    final DoubleArrayList initialRatesGuess = new DoubleArrayList();
    final String fullDomesticCurveName = domesticCurveName + "_" + domesticCurrency.getCode();
    final String fullForeignCurveName = foreignCurveName + "_" + foreignCurrency.getCode();
    final List<InstrumentDerivative> derivatives = new ArrayList<InstrumentDerivative>();
    for (final Tenor tenor : definition.getTenors()) {
      final ExternalId identifier = provider.getInstrument(now.toLocalDate(), tenor);
      if (fxForwardData.containsKey(identifier)) {
        final double paymentTime = TimeCalculator.getTimeBetween(now, now.plus(tenor.getPeriod())); //TODO
        derivatives.add(getFXForward(domesticCurrency, foreignCurrency, paymentTime, spotFX, fullDomesticCurveName, fullForeignCurveName));
        marketValues.add(fxForwardData.get(identifier));
        nodeTimes.add(paymentTime); //TODO
        initialRatesGuess.add(0.02);
      }
    }
    final YieldCurveBundle knownCurve = new YieldCurveBundle(new String[] {fullForeignCurveName }, new YieldAndDiscountCurve[] {foreignCurve });
    final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<String, double[]>();
    curveKnots.put(fullDomesticCurveName, nodeTimes.toDoubleArray());
    final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<String, Interpolator1D>();
    final CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    curveNodes.put(fullDomesticCurveName, nodeTimes.toDoubleArray());
    interpolators.put(fullDomesticCurveName, interpolator);
    final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues.toDoubleArray(), knownCurve, curveNodes, interpolators, useFiniteDifference);
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(absoluteTolerance, relativeTolerance, iterations, decomposition);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, PAR_RATE_CALCULATOR);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, PAR_RATE_SENSITIVITY_CALCULATOR);
    final double[] fittedYields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess.toDoubleArray())).getData();
    final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(fittedYields));
    final YieldCurve curve = new YieldCurve(InterpolatedDoublesCurve.from(nodeTimes.toDoubleArray(), fittedYields, interpolator));
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(2);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties curveProperties = getCurveProperties(curveCalculationConfigName, domesticCurveName, absoluteToleranceName, relativeToleranceName, iterationsName, decompositionName,
        useFiniteDifferenceName, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final ValueProperties properties = getProperties(curveCalculationConfigName, absoluteToleranceName, relativeToleranceName, iterationsName, decompositionName, useFiniteDifferenceName,
        interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties), jacobianMatrix.getData()));
    result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, curveProperties), curve));
    return result;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE || target.getUniqueId() == null) {
      return false;
    }
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties curveProperties = getCurveProperties();
    final ValueProperties properties = getProperties();
    final ValueSpecification curve = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), curveProperties);
    final ValueSpecification jacobian = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, target.toSpecification(), properties);
    return Sets.newHashSet(curve, jacobian);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> rootFinderAbsoluteTolerance = constraints.getValues(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    if (rootFinderAbsoluteTolerance == null || rootFinderAbsoluteTolerance.size() != 1) {
      return null;
    }
    final Set<String> rootFinderRelativeTolerance = constraints.getValues(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    if (rootFinderRelativeTolerance == null || rootFinderRelativeTolerance.size() != 1) {
      return null;
    }
    final Set<String> maxIterations = constraints.getValues(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    if (maxIterations == null || maxIterations.size() != 1) {
      return null;
    }
    final Set<String> decomposition = constraints.getValues(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION);
    if (decomposition == null || decomposition.size() != 1) {
      return null;
    }
    final Set<String> useFiniteDifference = constraints.getValues(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE);
    if (useFiniteDifference == null || useFiniteDifference.size() != 1) {
      return null;
    }
    final Set<String> interpolatorName = constraints.getValues(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME);
    if (interpolatorName == null || interpolatorName.size() != 1) {
      return null;
    }
    final Set<String> leftExtrapolatorName = constraints.getValues(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorName == null || leftExtrapolatorName.size() != 1) {
      return null;
    }
    final Set<String> rightExtrapolatorName = constraints.getValues(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorName == null || rightExtrapolatorName.size() != 1) {
      return null;
    }
    final String domesticCurveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBFXForwardCurveDefinitionSource fxCurveDefinitionSource = new ConfigDBFXForwardCurveDefinitionSource(configSource);
    final ConfigDBFXForwardCurveSpecificationSource fxCurveSpecificationSource = new ConfigDBFXForwardCurveSpecificationSource(configSource);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig domesticCurveCalculationConfig = curveCalculationConfigSource.getConfig(domesticCurveCalculationConfigName);
    if (domesticCurveCalculationConfig.getExogenousConfigData() == null) {
      s_logger.info("Need an externally-supplied curve to imply data; tried {} " + domesticCurveCalculationConfigName);
      return null;
    }
    if (domesticCurveCalculationConfig.getYieldCurveNames().length != 1) {
      throw new OpenGammaRuntimeException("Can only handle one curve at the moment");
    }
    final String domesticCurveName = domesticCurveCalculationConfig.getYieldCurveNames()[0];
    final UniqueIdentifiable domesticId = domesticCurveCalculationConfig.getUniqueIds()[0];
    if (!(domesticId instanceof Currency)) {
      throw new OpenGammaRuntimeException("Can only handle curves with currencies as ids at the moment");
    }
    final Currency domesticCurrency = (Currency) domesticId;
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final Map<String, String[]> exogenousConfigs = domesticCurveCalculationConfig.getExogenousConfigData();
    if (exogenousConfigs.size() != 1) {
      throw new OpenGammaRuntimeException("Can only handle curves with one foreign curve config");
    }
    final Map.Entry<String, String[]> entry = exogenousConfigs.entrySet().iterator().next();
    final MultiCurveCalculationConfig foreignConfig = curveCalculationConfigSource.getConfig(entry.getKey());
    final UniqueIdentifiable foreignId = foreignConfig.getUniqueIds()[0];
    if (!(foreignId instanceof Currency)) {
      throw new OpenGammaRuntimeException("Can only handle curves with currencies as ids at the moment");
    }
    final Currency foreignCurrency = (Currency) foreignId;
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(domesticCurrency, foreignCurrency);
    final FXForwardCurveDefinition definition = fxCurveDefinitionSource.getDefinition(domesticCurveName, currencyPair.toString());
    if (definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + domesticCurveName + " with target " + target);
    }
    final FXForwardCurveSpecification fxForwardCurveSpec = fxCurveSpecificationSource.getSpecification(domesticCurveName, currencyPair.toString());
    if (fxForwardCurveSpec == null) {
      throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + domesticCurveName + " with target " + target);
    }
    final ValueProperties fxForwardCurveProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE, domesticCurveName).get();
    final String foreignCurveName = foreignConfig.getYieldCurveNames()[0];
    final ValueProperties foreignCurveProperties = getForeignCurveProperties(foreignConfig, foreignCurveName);
    final FXForwardCurveInstrumentProvider provider = fxForwardCurveSpec.getCurveInstrumentProvider();
    requirements.add(new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA, new ComputationTargetSpecification(currencyPair), fxForwardCurveProperties));
    requirements.add(new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument()));
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, new ComputationTargetSpecification(foreignCurrency), foreignCurveProperties));
    return requirements;
  }

  private ValueProperties getForeignCurveProperties(final MultiCurveCalculationConfig foreignConfig, final String foreignCurveName) {
    return ValueProperties.builder().with(ValuePropertyNames.CURVE, foreignCurveName).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, foreignConfig.getCalculationConfigName()).get();
  }

  private ValueProperties getCurveProperties() {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).withAny(ValuePropertyNames.CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE).withAny(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME).withAny(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME).get();
  }

  private ValueProperties getProperties() {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE).withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS).withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION)
        .withAny(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE).withAny(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME).withAny(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME).get();
  }

  private ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName, final String absoluteTolerance, final String relativeTolerance,
      final String maxIterations, final String decomposition, final String useFiniteDifference, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIterations).with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION, decomposition)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE, useFiniteDifference).with(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName).with(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName).get();
  }

  private ValueProperties getProperties(final String curveCalculationConfigName, final String absoluteTolerance, final String relativeTolerance, final String maxIterations,
      final String decomposition, final String useFiniteDifference, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
    return createValueProperties().with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIterations).with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION, decomposition)
        .with(MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE, useFiniteDifference).with(InterpolatedCurveAndSurfaceProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedCurveAndSurfaceProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName).with(InterpolatedCurveAndSurfaceProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName).get();
  }

  //TODO determine domestic and notional from dominance data
  private ForexForward getFXForward(final Currency currency1, final Currency currency2, final double paymentTime, final double spotFX, final String curveName1, final String curveName2) {
    final PaymentFixed paymentCurrency1 = new PaymentFixed(currency1, paymentTime, 1, curveName1);
    final PaymentFixed paymentCurrency2 = new PaymentFixed(currency2, paymentTime, -1. / spotFX, curveName2);
    return new ForexForward(paymentCurrency1, paymentCurrency2, spotFX);
  }
}
