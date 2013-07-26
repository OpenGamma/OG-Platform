package com.opengamma.financial.analytics.model.curve;

/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Produces an ISDA compatible yield curve
 */
// non compiled for now to allow dynamic configdb lookup
public class ISDACompliantCurveFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Period swapIvl = Period.ofMonths(6); //FIXME: hardcoded
  private static final BusinessDayConvention badDayConv = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(
      "Modified Following");
  //private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter();
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final DayCount DCC_30_360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final DayCount MONEY_MARKET_DCC = ACT_360;
  private static final DayCount SWAP_DCC = DCC_30_360;
  private static final DayCount CURVE_DCC = ACT_365;
  private static final ISDACompliantYieldCurveBuild BUILDER = new ISDACompliantYieldCurveBuild();

  /** the curve configuration name */
  private String _curveName;

  public ISDACompliantCurveFunction(final String curveName) {
    ArgumentChecker.notNull(curveName, "curve name");
    _curveName = curveName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
                                    final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);

    final CurveSpecification specification = (CurveSpecification) inputs.getValue(ValueRequirementNames.CURVE_SPECIFICATION);
    final SnapshotDataBundle snapshot = (SnapshotDataBundle) inputs.getValue(ValueRequirementNames.CURVE_MARKET_DATA);

    final int nNodes = specification.getNodes().size();
    final double[] marketDataForCurve = new double[nNodes];
    final ISDAInstrumentTypes[] instruments = new ISDAInstrumentTypes[nNodes];
    final Period[] tenors = new Period[nNodes];
    int k = 0;
    for (final CurveNodeWithIdentifier node : specification.getNodes()) { // Node points - start
      final Double marketData = snapshot.getDataPoint(node.getIdentifier()); //FIXME
      if (marketData == null) {
        throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
      }
      marketDataForCurve[k] = marketData;
      tenors[k] = node.getCurveNode().getResolvedMaturity().getPeriod();
      if (node.getCurveNode() instanceof CashNode) {
        instruments[k] = ISDAInstrumentTypes.MoneyMarket;
      } else if (node.getCurveNode() instanceof SwapNode) {
        instruments[k] = ISDAInstrumentTypes.Swap;
      } else {
        throw new OpenGammaRuntimeException("Can't handle node type " + node.getCurveNode().getClass().getSimpleName() + " at node " + node);
      }
      k++;
    }

    // TODO: Fix spot date
    final ISDACompliantYieldCurve yieldCurve = BUILDER.build(now.toLocalDate(), now.toLocalDate(), instruments, tenors, marketDataForCurve, MONEY_MARKET_DCC, SWAP_DCC, swapIvl, CURVE_DCC, badDayConv);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, _curveName)
        //.with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, offsetString)
        //.with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, yieldCurve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        //.withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        //.withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_NEW)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
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
    // Exogenous needed?
    //final ValueProperties properties = ValueProperties.builder()
    //    .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
    //    .get();
    //requirements.add(new ValueRequirement(CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, properties));
    //requirements.add(new ValueRequirement(FX_MATRIX, ComputationTargetSpecification.NULL, properties));
    //requirements.addAll(_exogenousRequirements);
    return requirements;
  }

  //@Override
  //public boolean canHandleMissingInputs() {
  //  return true;
  //}
  //
  //@Override
  //public boolean canHandleMissingRequirements() {
  //  return true;
  //}

}
