/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
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
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * @deprecated Use the version where calculation parameters can be set
 * @see FXImpliedYieldCurveFunction
 */
@Deprecated
public class FXImpliedYieldCurveFunctionDeprecated extends AbstractFunction {
  /** Property name for the calculation method */
  public static final String FX_IMPLIED = "FXImplied";
  private static final Logger s_logger = LoggerFactory.getLogger(FXImpliedYieldCurveFunctionDeprecated.class);

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBFXForwardCurveDefinitionSource fxCurveDefinitionSource = new ConfigDBFXForwardCurveDefinitionSource(configSource);
    final ConfigDBFXForwardCurveSpecificationSource fxCurveSpecificationSource = new ConfigDBFXForwardCurveSpecificationSource(configSource);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final String interpolatorName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_INTERPOLATOR);
        final String leftExtrapolatorName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_LEFT_EXTRAPOLATOR);
        final String rightExtrapolatorName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_RIGHT_EXTRAPOLATOR);
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final Currency currency = Currency.of(target.getUniqueId().getValue());
        final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(currency, Currency.USD);
        final FXForwardCurveDefinition definition = fxCurveDefinitionSource.getDefinition(curveName, currencyPair.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + curveName + " for target " + target);
        }
        final FXForwardCurveSpecification specification = fxCurveSpecificationSource.getSpecification(curveName, currencyPair.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + curveName + " for target " + target);
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
        final Object usdCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
        if (usdCurveObject == null) {
          throw new OpenGammaRuntimeException("Could not get USD yield curve");
        }
        final YieldAndDiscountCurve usdCurve = (YieldAndDiscountCurve) usdCurveObject;
        @SuppressWarnings("unchecked")
        final
        Map<ExternalId, Double> fxForwardData = (Map<ExternalId, Double>) dataObject;
        final int n = fxForwardData.size();
        final double[] marketValues = new double[n];
        final double[] nodeTimes = new double[n];
        final double[] initialRatesGuess = new double[n];
        final String fullCurveName = curveName + "_" + currency.getCode();
        final Currency currency1 = currencyPair.getFirstCurrency(); //TODO
        final Currency currency2 = currencyPair.getSecondCurrency(); //TODO
        final List<InstrumentDerivative> derivatives = new ArrayList<InstrumentDerivative>();
        int i = 0;
        for (final Tenor tenor : definition.getTenors()) {
          final ExternalId identifier = provider.getInstrument(now.toLocalDate(), tenor);
          if (fxForwardData.containsKey(identifier)) {
            final double paymentTime = TimeCalculator.getTimeBetween(now, now.plus(tenor.getPeriod())); //TODO
            derivatives.add(getFXForward(currency1, currency2, paymentTime, spotFX, fullCurveName, "FUNDING_USD"));
            marketValues[i] = fxForwardData.get(identifier);
            nodeTimes[i] = paymentTime; //TODO
            initialRatesGuess[i++] = 0.02;
          }
        }
        final YieldCurveBundle knownCurve = new YieldCurveBundle(new String[] {"FUNDING_USD"}, new YieldAndDiscountCurve[] {usdCurve});
        final LinkedHashMap<String, double[]> curveKnots = new LinkedHashMap<String, double[]>();
        curveKnots.put(fullCurveName, nodeTimes);
        final LinkedHashMap<String, double[]> curveNodes = new LinkedHashMap<String, double[]>();
        final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<String, Interpolator1D>();
        final CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        curveNodes.put(fullCurveName, nodeTimes);
        interpolators.put(fullCurveName, interpolator);
        // TODO have use finite difference or not as an input [FIN-147]
        final MultipleYieldCurveFinderDataBundle data = new MultipleYieldCurveFinderDataBundle(derivatives, marketValues, knownCurve, curveNodes, interpolators, false);
        final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderFunction(data, ParRateCalculator.getInstance()); //TODO
        final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderJacobian(data, ParRateCurveSensitivityCalculator.getInstance()); //TODO
        NewtonVectorRootFinder rootFinder;
        double[] yields = null;
        try {
          // TODO have the decomposition as an optional input [FIN-146]
          rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.LU_COMMONS_NAME));
          final DoubleMatrix1D result = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess));
          yields = result.getData();
        } catch (final Exception eLU) {
          try {
            s_logger.warn("Could not find root using LU decomposition for curve " + curveName + "; trying SV. Error was: " + eLU.getMessage());
            rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
            yields = rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initialRatesGuess)).getData();
          } catch (final Exception eSV) {
            s_logger.warn("Could not find root using SV decomposition for curve " + curveName + ". Error was: " + eSV.getMessage());
            throw new OpenGammaRuntimeException(eSV.getMessage());
          }
        }
        final YieldCurve curve = YieldCurve.from(InterpolatedDoublesCurve.from(nodeTimes, yields, interpolator));
        final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(4);
        final ValueProperties properties = getResultProperties(curveName, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        final DoubleMatrix2D jacobianMatrix = jacobianCalculator.evaluate(new DoubleMatrix1D(yields));
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties), jacobianMatrix.getData()));
        result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties), curve));
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
        final ValueProperties properties = getResultProperties();
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        final ValueSpecification yieldCurveSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, properties);
        final ValueSpecification jacobianSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties);
        return Sets.newHashSet(yieldCurveSpec, jacobianSpec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final Set<String> interpolatorNames = constraints.getValues(YieldCurveFunction.PROPERTY_INTERPOLATOR);
        if (interpolatorNames == null || interpolatorNames.size() != 1) {
          return null;
        }
        final Set<String> leftExtrapolatorNames = constraints.getValues(YieldCurveFunction.PROPERTY_LEFT_EXTRAPOLATOR);
        if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
          return null;
        }
        final Set<String> rightExtrapolatorNames = constraints.getValues(YieldCurveFunction.PROPERTY_RIGHT_EXTRAPOLATOR);
        if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
          return null;
        }
        final String curveName = curveNames.iterator().next();
        final ValueProperties underlyingCurveProperties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, "FUNDING")
            .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, "FORWARD_3M")
            .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, "FUNDING")
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "PresentValue").get();
        final ValueProperties fxForwardsProperties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName).get();
        final Currency underlyingCurrency = Currency.USD;
        final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(Currency.of(target.getUniqueId().getValue()), Currency.USD);
        final String fxCurveName = curveNames.iterator().next();
        final FXForwardCurveDefinition definition = fxCurveDefinitionSource.getDefinition(fxCurveName, currencyPair.toString());
        if (definition == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve definition called " + fxCurveName + " with target " + target);
        }
        final FXForwardCurveSpecification specification = fxCurveSpecificationSource.getSpecification(fxCurveName, currencyPair.toString());
        if (specification == null) {
          throw new OpenGammaRuntimeException("Couldn't find FX forward curve specification called " + fxCurveName + " with target " + target);
        }
        final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
        final FXForwardCurveInstrumentProvider provider = specification.getCurveInstrumentProvider();
        requirements.add(new ValueRequirement(provider.getDataFieldName(), provider.getSpotInstrument()));
        requirements.add(new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_MARKET_DATA, new ComputationTargetSpecification(currencyPair), fxForwardsProperties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, new ComputationTargetSpecification(underlyingCurrency), underlyingCurveProperties));
        return requirements;
      }

      private ValueProperties getResultProperties() {
        return createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .withAny(YieldCurveFunction.PROPERTY_INTERPOLATOR)
            .withAny(YieldCurveFunction.PROPERTY_LEFT_EXTRAPOLATOR)
            .withAny(YieldCurveFunction.PROPERTY_RIGHT_EXTRAPOLATOR)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).get(); //TODO add name of fixed curve
      }

      private ValueProperties getResultProperties(final String curveName, final String interpolator, final String leftExtrapolator, final String rightExtrapolator) {
        return createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(YieldCurveFunction.PROPERTY_INTERPOLATOR, interpolator)
            .with(YieldCurveFunction.PROPERTY_LEFT_EXTRAPOLATOR, leftExtrapolator)
            .with(YieldCurveFunction.PROPERTY_RIGHT_EXTRAPOLATOR, rightExtrapolator)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, FX_IMPLIED).get();
      }

      //TODO determine domestic and notional from dominance data
      private ForexForward getFXForward(final Currency currency1, final Currency currency2, final double paymentTime, final double spotFX, final String curveName1, final String curveName2) {
        final PaymentFixed paymentCurrency1 = new PaymentFixed(currency1, paymentTime, 1, curveName1);
        final PaymentFixed paymentCurrency2 = new PaymentFixed(currency2, paymentTime, -1. / spotFX, curveName2);
        return new ForexForward(paymentCurrency1, paymentCurrency2, spotFX);
      }
    };
  }
}
