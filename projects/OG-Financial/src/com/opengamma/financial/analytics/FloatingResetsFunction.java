/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.instrument.FloatingCashFlowVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class FloatingResetsFunction extends AbstractFunction.NonCompiledInvoker {
  private static final FloatingCashFlowVisitor FLOATING_CASH_FLOW_CALCULATOR = FloatingCashFlowVisitor.getInstance();
  private static final ReferenceIndexVisitor INDEX_VISITOR = new ReferenceIndexVisitor();
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter)
        .create();
  }
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate date = snapshotClock.zonedDateTime().toLocalDate();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final InstrumentDefinition<?> definition = security.accept(_visitor);
    final Map<LocalDate, MultipleCurrencyAmount> cashFlows = definition.accept(FLOATING_CASH_FLOW_CALCULATOR, date);
    final String label = security.accept(INDEX_VISITOR);
    final Map<LocalDate, List<Pair<CurrencyAmount, String>>> result = Maps.newHashMap();
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : cashFlows.entrySet()) {
      final MultipleCurrencyAmount mca = entry.getValue();
      final List<Pair<CurrencyAmount, String>> list = Lists.newArrayListWithCapacity(mca.size());
      for (final CurrencyAmount ca : mca) {
        list.add(Pair.of(ca, label));
      }
      result.put(entry.getKey(), list);
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.RESET_DATES, target.toSpecification(), createValueProperties().get()),
        new ResetScheduleMatrix(result)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FINANCIAL_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.RESET_DATES, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  private static final class ReferenceIndexVisitor extends FinancialSecurityVisitorAdapter<String> {
    private static final String STRING = " + ";

    public ReferenceIndexVisitor() {
    }

    @Override
    public String visitSwapSecurity(final SwapSecurity security) {
      if (security.getPayLeg() instanceof FixedInterestRateLeg) {
        return null;
      }
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) security.getPayLeg();
      final StringBuilder sb = new StringBuilder(floatingLeg.getFloatingReferenceRateId().getValue());
      if (floatingLeg instanceof FloatingSpreadIRLeg) {
        sb.append(STRING);
        sb.append(((FloatingSpreadIRLeg) floatingLeg).getSpread());
      }
      return sb.toString();
    }
  }

  public String visitFRASecurity(final FRASecurity security) {
    return security.getUnderlyingId().getValue();
  }
}
