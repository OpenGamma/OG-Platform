/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAInstrumentTypes;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Produces an ISDA compatible yield curve
 */
// non compiled for now to allow dynamic config db lookup
public class ISDACompliantCurveFunction extends AbstractFunction.NonCompiledInvoker {

  /** ISDA fixes yield curve daycout to Act/365 */
  private static final DayCount ACT_365 = DayCounts.ACT_365;

  /** the curve configuration name */
  private final String _curveName;

  /**
   * The name of the curve produced by this function.
   * @param curveName The curve name, not null
   */
  public ISDACompliantCurveFunction(final String curveName) {
    ArgumentChecker.notNull(curveName, "curve name");
    _curveName = curveName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
                                    final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);

    final CurveSpecification specification = (CurveSpecification) inputs.getValue(ValueRequirementNames.CURVE_SPECIFICATION);
    final SnapshotDataBundle snapshot = (SnapshotDataBundle) inputs.getValue(ValueRequirementNames.CURVE_MARKET_DATA);
    final LocalDate spotDate = (!desiredValue.getConstraints().getValues(ISDAFunctionConstants.ISDA_CURVE_DATE).isEmpty())
        ? LocalDate.parse(desiredValue.getConstraint(ISDAFunctionConstants.ISDA_CURVE_DATE))
        : now.toLocalDate();

    DepositConvention cashConvention = null;
    VanillaIborLegConvention floatLegConvention = null;
    SwapFixedLegConvention fixLegConvention = null;
    IborIndexConvention liborConvention = null;

    final int nNodes = specification.getNodes().size();
    final double[] marketDataForCurve = new double[nNodes];
    final ISDAInstrumentTypes[] instruments = new ISDAInstrumentTypes[nNodes];
    final Period[] tenors = new Period[nNodes];
    int k = 0;
    for (final CurveNodeWithIdentifier node : specification.getNodes()) {
      final Double marketData = snapshot.getDataPoint(node.getIdentifier());
      if (marketData == null) {
        throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
      }
      marketDataForCurve[k] = marketData;
      tenors[k] = node.getCurveNode().getResolvedMaturity().getPeriod();
      if (node.getCurveNode() instanceof CashNode) {
        instruments[k] = ISDAInstrumentTypes.MoneyMarket;
        final ExternalId cashConventionId = ((CashNode) node.getCurveNode()).getConvention();
        if (cashConvention == null) {
          try {
            cashConvention = conventionSource.getSingle(cashConventionId, DepositConvention.class);
          } catch (DataNotFoundException ex) {
            // ignore, continue around loop
          }
        } else if (!cashConvention.getExternalIdBundle().contains(cashConventionId)) {
          throw new OpenGammaRuntimeException("Got 2 types of cash convention: " + cashConvention.getExternalIdBundle() + " " + cashConventionId);
        }
      } else if (node.getCurveNode() instanceof SwapNode) {
        instruments[k] = ISDAInstrumentTypes.Swap;
        final ExternalId payConventionId = ((SwapNode) node.getCurveNode()).getPayLegConvention();
        final Convention payConvention = conventionSource.getSingle(payConventionId);
        final ExternalId receiveConventionId = ((SwapNode) node.getCurveNode()).getReceiveLegConvention();
        final Convention receiveConvention = conventionSource.getSingle(receiveConventionId);
        if (payConvention instanceof VanillaIborLegConvention) {  // float leg
          if (floatLegConvention == null) {
            floatLegConvention = (VanillaIborLegConvention) payConvention;
          } else if (!floatLegConvention.getExternalIdBundle().contains(payConventionId)) {
            throw new OpenGammaRuntimeException("Got 2 types of float leg convention: " + payConvention.getExternalIdBundle() + " " + payConventionId);
          }
        } else if (payConvention instanceof SwapFixedLegConvention) {
          if (fixLegConvention == null) {
            fixLegConvention = (SwapFixedLegConvention) payConvention;
          } else if (!fixLegConvention.getExternalIdBundle().contains(payConventionId)) {
            throw new OpenGammaRuntimeException("Got 2 types of fixed leg convention: " + payConvention.getExternalIdBundle() + " " + payConventionId);
          }
        } else {
          throw new OpenGammaRuntimeException("Unexpected swap convention type: " + payConvention);
        }
        if (receiveConvention instanceof VanillaIborLegConvention) {  // float leg
          if (floatLegConvention == null) {
            floatLegConvention = (VanillaIborLegConvention) receiveConvention;
          } else if (!floatLegConvention.getExternalIdBundle().contains(receiveConventionId)) {
            throw new OpenGammaRuntimeException("Got 2 types of float leg convention: " + receiveConvention.getExternalIdBundle() + " " + receiveConventionId);
          }
        } else if (receiveConvention instanceof SwapFixedLegConvention) {
          if (fixLegConvention == null) {
            fixLegConvention = (SwapFixedLegConvention) receiveConvention;
          } else if (!fixLegConvention.getExternalIdBundle().contains(receiveConventionId)) {
            throw new OpenGammaRuntimeException("Got 2 types of fixed leg convention: " + receiveConvention.getExternalIdBundle() + " " + receiveConventionId);
          }
        } else {
          throw new OpenGammaRuntimeException("Unexpected swap convention type: " + receiveConvention);
        }
      } else {
        throw new OpenGammaRuntimeException("Can't handle node type " + node.getCurveNode().getClass().getSimpleName() + " at node " + node);
      }
      k++;
    }

    ArgumentChecker.notNull(cashConvention, "Cash convention");
    ArgumentChecker.notNull(floatLegConvention, "Floating leg convention");
    ArgumentChecker.notNull(fixLegConvention, "Fixed leg convention");
    liborConvention = conventionSource.getSingle(floatLegConvention.getIborIndexConvention(), IborIndexConvention.class);
    ArgumentChecker.notNull(liborConvention, floatLegConvention.getIborIndexConvention().toString());

    final ISDACompliantYieldCurve yieldCurve = ISDACompliantYieldCurveBuild.build(spotDate, spotDate, instruments, tenors, marketDataForCurve, cashConvention.getDayCount(),
        fixLegConvention.getDayCount(), fixLegConvention.getPaymentTenor().getPeriod(), ACT_365, liborConvention.getBusinessDayConvention());

    final ValueProperties properties = desiredValue.getConstraints().copy()
        .with(ISDAFunctionConstants.ISDA_CURVE_DATE, spotDate.toString())
        .get();

    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, yieldCurve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, _curveName)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_DATE)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE,
                                                        target.toSpecification(),
                                                        properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ValueProperties properties = ValueProperties.builder()
        .with(CURVE, _curveName)
        .get();
    requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
    requirements.add(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
    requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
    return requirements;
  }

}
