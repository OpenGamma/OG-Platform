/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.analytics.financial.instrument.Convention;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
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
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Base class for ISDA CDS pricing functions
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see ISDAApproxCDSPriceHazardCurveFunction
 * @see ISDAApproxCDSPriceFlatSpreadFunction
 */
public abstract class ISDAApproxCDSPriceFunction extends NonCompiledInvoker {

  protected abstract String getHazardRateStructure();

  protected abstract DoublesPair executeImpl(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues);

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CDS_SECURITY;
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    return super.createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME)
                .with(ISDAFunctionConstants.ISDA_IMPLEMENTATION, ISDAFunctionConstants.ISDA_IMPLEMENTATION_APPROX)
                .with(ISDAFunctionConstants.ISDA_HAZARD_RATE_STRUCTURE, getHazardRateStructure());
  }

  private ValueProperties.Builder createValueProperties(final CDSSecurity security) {
    return createValueProperties()
        .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    final ValueProperties properties = createValueProperties(cds).get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueSpecification cleanPriceSpec = new ValueSpecification(ValueRequirementNames.CLEAN_PRICE, targetSpec, properties);
    final ValueSpecification dirtyPriceSpec = new ValueSpecification(ValueRequirementNames.DIRTY_PRICE, targetSpec, properties);
    final ValueSpecification presentValueSpec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, targetSpec, properties);
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    results.add(cleanPriceSpec);
    results.add(dirtyPriceSpec);
    results.add(presentValueSpec);
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    final CDSSecurity cds = (CDSSecurity) target.getSecurity();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = createValueProperties(cds).get();

    final DoublesPair calculationResult = executeImpl(executionContext, inputs, target, desiredValues);
    final Double cleanPrice = calculationResult.getFirst();
    final Double dirtyPrice = calculationResult.getSecond();

    // Pack up the results
    final Set<ComputedValue> results = new HashSet<ComputedValue>();

    final ComputedValue cleanPriceValue = new ComputedValue(new ValueSpecification(ValueRequirementNames.CLEAN_PRICE, targetSpec, properties), cleanPrice);
    final ComputedValue dirtyPriceValue = new ComputedValue(new ValueSpecification(ValueRequirementNames.DIRTY_PRICE, targetSpec, properties), dirtyPrice);
    final ComputedValue presentValue = new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, targetSpec, properties), cleanPrice);

    results.add(cleanPriceValue);
    results.add(dirtyPriceValue);
    results.add(presentValue);

    return results;
  }

  protected ZonedDateTime findSettlementDate(final ZonedDateTime startDate, final Convention convention) {

    final TemporalAdjuster adjuster = convention.getBusinessDayConvention().getTemporalAdjuster(convention.getWorkingDayCalendar());

    ZonedDateTime result = startDate;

    for (int i = 0, n = convention.getSettlementDays(); i < n; ++i) {
      result = result.plusDays(1).with(adjuster);
    }

    return result;
  }

}
