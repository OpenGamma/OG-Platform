/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAInstrumentTypes;
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
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ISDAYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {

  // ISDA fixes yield curve daycout to Act/365
  private static final DayCount ACT_365 = DayCounts.ACT_365;

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final String offsetString = desiredValue.getConstraint(ISDAFunctionConstants.ISDA_CURVE_OFFSET);
    final int offset = Integer.parseInt(offsetString);
    final Object dataObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_DATA);
    if (dataObject == null) {
      throw new OpenGammaRuntimeException("Couldn't get yield curve data for " + curveName);
    }
    final YieldCurveData yieldCurveData = (YieldCurveData) dataObject;
    final InterpolatedYieldCurveSpecificationWithSecurities yieldCurveSpec = yieldCurveData.getCurveSpecification();

    final int nNodes = yieldCurveSpec.getStrips().size();
    final double[] marketDataForCurve = new double[nNodes];
    final ISDAInstrumentTypes[] instruments = new ISDAInstrumentTypes[nNodes];
    final Period[] tenors = new Period[nNodes];
    int k = 0;

    DayCount cashDCC = null;
    DayCount fixDCC = null;
    BusinessDayConvention floatBadDayConv = null;
    Period paymentTenor = null;

    for (final FixedIncomeStripWithSecurity strip : yieldCurveSpec.getStrips()) {
      final String securityType = strip.getSecurity().getSecurityType();
      if (!(securityType.equals(CashSecurity.SECURITY_TYPE) || securityType.equals(SwapSecurity.SECURITY_TYPE)/* || securityType.equals(specObject)*/)) {
        throw new OpenGammaRuntimeException("ISDA curves should only use Libor and swap rates");
      }
      final Double marketValue = yieldCurveData.getDataPoint(strip.getSecurityIdentifier());
      if (marketValue == null) {
        throw new OpenGammaRuntimeException("Could not get market data for " + strip);
      }
      final FinancialSecurity financialSecurity = (FinancialSecurity) strip.getSecurity();
      instruments[k] = securityType.equals(CashSecurity.SECURITY_TYPE) ? ISDAInstrumentTypes.MoneyMarket : ISDAInstrumentTypes.Swap;
      if (financialSecurity instanceof SwapSecurity) {
        SwapSecurity swap = (SwapSecurity) financialSecurity;
        if (swap.getPayLeg() instanceof FixedInterestRateLeg) {
          fixDCC = swap.getPayLeg().getDayCount();
          paymentTenor = ((SimpleFrequency) swap.getPayLeg().getFrequency()).toPeriodFrequency().getPeriod();
          floatBadDayConv = swap.getReceiveLeg().getBusinessDayConvention();
        } else {
          fixDCC = swap.getReceiveLeg().getDayCount();
          paymentTenor = ((SimpleFrequency) swap.getReceiveLeg().getFrequency()).toPeriodFrequency().getPeriod();
          floatBadDayConv = swap.getPayLeg().getBusinessDayConvention();
        }
      } else {
        cashDCC = ((CashSecurity) financialSecurity).getDayCount();
      }
      marketDataForCurve[k] = marketValue;
      tenors[k] = strip.getResolvedTenor().getPeriod();
      k++;
    }
    //TODO: Check spot date logic
    final ISDACompliantYieldCurve yieldCurve = ISDACompliantYieldCurveBuild.build(now.toLocalDate(), now.toLocalDate().minusDays(offset), instruments, tenors, marketDataForCurve, cashDCC,
        fixDCC, paymentTenor, ACT_365, floatBadDayConv);
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURVE, curveName).with(ISDAFunctionConstants.ISDA_CURVE_OFFSET, offsetString)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig).with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
        .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, yieldCurve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ISDAFunctionConstants.ISDA_CURVE_OFFSET).with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationMethod = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethod == null) {
      return null;
    }
    if (curveCalculationMethod.size() == 1) {
      if (!ISDAFunctionConstants.ISDA_METHOD_NAME.equals(Iterables.getOnlyElement(curveCalculationMethod))) {
        return null;
      }
      final Set<String> implementationMethod = constraints.getValues(ISDAFunctionConstants.ISDA_IMPLEMENTATION);
      if (implementationMethod != null && implementationMethod.size() == 1) {
        if (!ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX.equals(Iterables.getOnlyElement(implementationMethod))) {
          return null;
        }
      }
    }
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationConfigs = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigs == null || curveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> isdaCurveOffsets = constraints.getValues(ISDAFunctionConstants.ISDA_CURVE_OFFSET);
    if (isdaCurveOffsets == null || isdaCurveOffsets.size() != 1) {
      return null;
    }
    final String curveName = Iterables.getOnlyElement(curveNames);
    final String curveCalculationConfig = Iterables.getOnlyElement(curveCalculationConfigs);
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .withOptional(ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
    final ValueProperties curveTSProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig).get();
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_DATA, targetSpec, properties));
    requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, curveTSProperties));
    return requirements;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    // historical time series likely to be absent
    return true;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

}
