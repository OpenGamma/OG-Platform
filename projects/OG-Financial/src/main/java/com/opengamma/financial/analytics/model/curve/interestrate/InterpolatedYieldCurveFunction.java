/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class InterpolatedYieldCurveFunction extends AbstractFunction {

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        String curveName = null;
        String curveCalculationConfig = null;
        for (final ValueRequirement desiredValue : desiredValues) {
          if (desiredValue.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
            curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
            curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
            break;
          }
        }
        final Object specificationObject = inputs.getValue(ValueRequirementNames.CURVE_SPECIFICATION);
        if (specificationObject == null) {
          throw new OpenGammaRuntimeException("Could not get curve specification");
        }
        if (!(specificationObject instanceof InterpolatedCurveSpecification)) {
          throw new OpenGammaRuntimeException("Curve specification was not an InterpolatedCurveSpecification");
        }
        final Object dataObject = inputs.getValue(ValueRequirementNames.CURVE_MARKET_DATA);
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Could not get yield curve data");
        }
        final InterpolatedCurveSpecification specification = (InterpolatedCurveSpecification) specificationObject;
        final SnapshotDataBundle marketData = (SnapshotDataBundle) dataObject;
        final int n = marketData.size();
        final double[] times = new double[n];
        final double[] yields = new double[n];
        final double[][] jacobian = new double[n][n];
        Boolean isYield = null;
        int i = 0;
        for (final CurveNodeWithIdentifier node : specification.getNodes()) {
          if (node.getCurveNode() instanceof ContinuouslyCompoundedRateNode) {
            if (i == 0) {
              isYield = true;
            } else {
              if (!isYield) {
                throw new OpenGammaRuntimeException("Was expecting only continuously-compounded rate nodes; have " + node.getCurveNode());
              }
            }
          } else if (node.getCurveNode() instanceof DiscountFactorNode) {
            if (i == 0) {
              isYield = false;
            } else {
              if (isYield) {
                throw new OpenGammaRuntimeException("Was expecting only discount factor nodes; have " + node.getCurveNode());
              }
            }
          } else {
            throw new OpenGammaRuntimeException("Can only handle discount factor or continuously-compounded rate nodes; have " + node.getCurveNode());
          }
          //TODO add check to make sure it's only discounting or rate curve nodes
          final Double marketValue = marketData.getDataPoint(node.getIdentifier());
          final Tenor maturity = node.getCurveNode().getResolvedMaturity();
          if (marketValue == null) {
            throw new OpenGammaRuntimeException("Could not get market data for " + node);
          }
          times[i] = DateUtils.estimatedDuration(maturity.getPeriod()).toDays() / 365.0; //TODO check if this is correct
          yields[i] = marketValue;
          jacobian[i][i] = 1;
          i++;
        }
        final String interpolatorName = specification.getInterpolatorName();
        final String rightExtrapolatorName = specification.getRightExtrapolatorName();
        final String leftExtrapolatorName = specification.getLeftExtrapolatorName();
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(times, yields, interpolator, curveName);
        final ValueProperties curveProperties = createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, InterpolatedDataProperties.CALCULATION_METHOD_NAME).get();
        final ValueProperties jacobianProperties = createValueProperties()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, InterpolatedDataProperties.CALCULATION_METHOD_NAME).get();
        final ValueSpecification curveSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), curveProperties);
        final ValueSpecification jacobianSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, target.toSpecification(), jacobianProperties);
        final YieldAndDiscountCurve yieldCurve = isYield ? YieldCurve.from(curve) : DiscountCurve.from(curve);
        return Sets.newHashSet(new ComputedValue(curveSpec, yieldCurve), new ComputedValue(jacobianSpec, jacobian));
      }

      //TODO should eventually be NULL?
      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.CURRENCY;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties curveProperties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, InterpolatedDataProperties.CALCULATION_METHOD_NAME)
            .get();
        final ValueProperties jacobianProperties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, InterpolatedDataProperties.CALCULATION_METHOD_NAME)
            .get();
        final ValueSpecification curveSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), curveProperties);
        final ValueSpecification jacobianSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, target.toSpecification(), jacobianProperties);
        return Sets.newHashSet(curveSpec, jacobianSpec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final String curveName = constraints.getStrictValue(ValuePropertyNames.CURVE);
        if (curveName == null) {
          return null;
        }
        final Set<ValueRequirement> requirements = new HashSet<>();
        final ValueProperties properties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName).get();
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
        return requirements;
      }

    };
  }

}
