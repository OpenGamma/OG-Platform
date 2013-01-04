/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class NotionalFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final NotionalVisitor visitor = new NotionalVisitor(currencyPairs);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final CurrencyAmount ca = security.accept(visitor);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.NOTIONAL, target.toSpecification(), createValueProperties().get());
    return Collections.singleton(new ComputedValue(spec, ca));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FinancialSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.NOTIONAL, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  private static class NotionalVisitor extends FinancialSecurityVisitorAdapter<CurrencyAmount> {
    private final CurrencyPairs _currencyPairs;

    public NotionalVisitor(final CurrencyPairs currencyPairs) {
      _currencyPairs = currencyPairs;
    }

    @Override
    public CurrencyAmount visitSwapSecurity(final SwapSecurity security) {
      final SwapLeg payNotional = security.getPayLeg();
      final SwapLeg receiveNotional = security.getReceiveLeg();
      if (payNotional.getNotional() instanceof InterestRateNotional && receiveNotional.getNotional() instanceof InterestRateNotional) {
        final InterestRateNotional pay = (InterestRateNotional) payNotional.getNotional();
        final InterestRateNotional receive = (InterestRateNotional) receiveNotional.getNotional();
        if (Double.compare(pay.getAmount(), receive.getAmount()) == 0) {
          return CurrencyAmount.of(pay.getCurrency(), pay.getAmount());
        }
      }
      throw new OpenGammaRuntimeException("Can only handle interest rate notionals with the same amounts");
    }

    @Override
    public CurrencyAmount visitFXOptionSecurity(final FXOptionSecurity security) {
      final Currency currency1 = security.getPutCurrency();
      final double amount1 = security.getPutAmount();
      final Currency currency2 = security.getCallCurrency();
      final double amount2 = security.getCallAmount();
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(currency1, currency2);
      if (currencyPair.getBase().equals(currency1)) {
        return CurrencyAmount.of(currency1, amount1);
      }
      return CurrencyAmount.of(currency2, amount2);
    }

    @Override
    public CurrencyAmount visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      final Currency currency = security.getDeliveryCurrency();
      final double amount = security.getCallCurrency().equals(currency) ? security.getCallAmount() : security.getPutAmount();
      return CurrencyAmount.of(currency, amount);
    }

    @Override
    public CurrencyAmount visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      final Currency currency1 = security.getPutCurrency();
      final double amount1 = security.getPutAmount();
      final Currency currency2 = security.getCallCurrency();
      final double amount2 = security.getCallAmount();
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(currency1, currency2);
      if (currencyPair.getBase().equals(currency1)) {
        return CurrencyAmount.of(currency1, amount1);
      }
      return CurrencyAmount.of(currency2, amount2);
    }

    @Override
    public CurrencyAmount visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      final Currency currency = security.getPaymentCurrency();
      final double amount = security.getCallCurrency().equals(currency) ? security.getCallAmount() : security.getPutAmount();
      return CurrencyAmount.of(currency, amount);
    }

    @Override
    public CurrencyAmount visitFXForwardSecurity(final FXForwardSecurity security) {
      final Currency currency1 = security.getPayCurrency();
      final double amount1 = security.getPayAmount();
      final Currency currency2 = security.getReceiveCurrency();
      final double amount2 = security.getReceiveAmount();
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(currency1, currency2);
      if (currencyPair.getBase().equals(currency1)) {
        return CurrencyAmount.of(currency1, amount1);
      }
      return CurrencyAmount.of(currency2, amount2);
    }
  }
}
