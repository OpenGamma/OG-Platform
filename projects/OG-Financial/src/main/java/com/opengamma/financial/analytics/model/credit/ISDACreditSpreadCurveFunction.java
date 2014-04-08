/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.curve.NodalObjectsCurve;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class ISDACreditSpreadCurveFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ISDACreditSpreadCurveFunction.class);

  private CurveDefinitionSource _curveDefinitionSource;
  private CurveSpecificationBuilder _curveSpecificationBuilder;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
    _curveSpecificationBuilder = ConfigDBCurveSpecificationBuilder.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        //TODO
        CurveSpecification curveSpecification;
        final String idName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        String curveName;
        try {
          curveName = "SAMEDAY_" + idName;
          curveSpecification = CurveUtils.getCurveSpecification(now.toInstant(), _curveDefinitionSource, _curveSpecificationBuilder, now.toLocalDate(), curveName);
        } catch (final Exception e) {
          curveName = idName;
          curveSpecification = CurveUtils.getCurveSpecification(now.toInstant(), _curveDefinitionSource, _curveSpecificationBuilder, now.toLocalDate(), idName);
        }

        final List<Tenor> tenors = new ArrayList<>();
        final List<Double> marketSpreads = new ArrayList<>();
        for (final CurveNodeWithIdentifier strip : curveSpecification.getNodes()) {
          final Object marketSpreadObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, strip.getIdentifier()));
          if (marketSpreadObject != null) {
            tenors.add(strip.getCurveNode().getResolvedMaturity());
            marketSpreads.add((Double) marketSpreadObject);
          } else {
            s_logger.warn("Could not get spread data for {}, defaulting", strip.getIdentifier());
            tenors.add(strip.getCurveNode().getResolvedMaturity());
            throw new OpenGammaRuntimeException("Couldn't get spreads for " + strip.getIdentifier());
          }
        }
        if (tenors.size() == 0) {
          throw new OpenGammaRuntimeException("Could not get any credit spread data for curve called " + curveName);
        }
        final NodalObjectsCurve<Tenor, Double> curve = NodalObjectsCurve.from(tenors, marketSpreads);
        final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, idName).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CREDIT_SPREAD_CURVE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, curve));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.NULL;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.CREDIT_SPREAD_CURVE, target.toSpecification(), properties));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        //TODO
        String curveName = "SAMEDAY_" + Iterables.getOnlyElement(curveNames);
        final Set<ValueRequirement> requirements = new HashSet<>();
        try {
          final CurveSpecification specification = CurveUtils.getCurveSpecification(atInstant, _curveDefinitionSource, _curveSpecificationBuilder, atZDT.toLocalDate(), curveName);
          for (final CurveNodeWithIdentifier strip : specification.getNodes()) {
            if (strip.getCurveNode() instanceof CreditSpreadNode) {
              requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, strip.getIdentifier()));
            }
          }
          return requirements;
        } catch (final Exception e) {
          s_logger.error(e.getMessage());
          //TODO backwards compatibility - remove when upstream functions select the correct prefix
          curveName = Iterables.getOnlyElement(curveNames);
          try {
            final CurveSpecification specification = CurveUtils.getCurveSpecification(atInstant, _curveDefinitionSource, _curveSpecificationBuilder, atZDT.toLocalDate(), curveName);
            for (final CurveNodeWithIdentifier strip : specification.getNodes()) {
              if (strip.getCurveNode() instanceof CreditSpreadNode) {
                requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, strip.getIdentifier()));
              }
            }
            return requirements;
          } catch (final Exception e1) {
            s_logger.error(e1.getMessage());
            return null;
          }
        }
      }

      @Override
      public boolean canHandleMissingRequirements() {
        return true;
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }

    };
  }

}
