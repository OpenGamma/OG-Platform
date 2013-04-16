/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.currency.CurrencyMatrixSpotSourcingFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Calculates Present Value on FX Forward instruments.
 */
public class FXForwardPresentValueFunction extends AbstractFunction.NonCompiledInvoker {

  private final FinancialSecurityVisitor<Currency> _payCurrencyVisitor = new FinancialSecurityVisitorAdapter<Currency>() {

    @Override
    public Currency visitFXForwardSecurity(FXForwardSecurity security) {
      return security.getPayCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
      return security.getPayCurrency();
    }
    
  };
  
  private final FinancialSecurityVisitor<Currency> _receiveCurrencyVisitor = new FinancialSecurityVisitorAdapter<Currency>() {

    @Override
    public Currency visitFXForwardSecurity(FXForwardSecurity security) {
      return security.getReceiveCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
      return security.getReceiveCurrency();
    }
    
  };
  
  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_FORWARD_SECURITY.or(FinancialSecurityTypes.NON_DELIVERABLE_FX_FORWARD_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(getResultSpec(target));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    ValueRequirement fxPvRequirement = new ValueRequirement(ValueRequirementNames.FX_PRESENT_VALUE, target.toSpecification());
    FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    Currency payCurrency = getPayCurrency(security);
    Currency receiveCurrency = getReceiveCurrency(security);
    ValueRequirement spotRateRequirement = CurrencyMatrixSpotSourcingFunction.getConversionRequirement(payCurrency, receiveCurrency);
    return ImmutableSet.of(fxPvRequirement, spotRateRequirement);
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    Currency payCurrency = getPayCurrency(security);
    Currency receiveCurrency = getReceiveCurrency(security);
    
    CurrencyLabelledMatrix1D fxPresentValue = (CurrencyLabelledMatrix1D) inputs.getValue(ValueRequirementNames.FX_PRESENT_VALUE);
    if (fxPresentValue.size() != 2) {
      throw new OpenGammaRuntimeException("Expected " + ValueRequirementNames.FX_PRESENT_VALUE + " input to contain 2 currency values, but found " + fxPresentValue.size());
    }
    int payIndex = -1;
    int receiveIndex = -1;
    for (int i = 0; i < 2; i++) {
      Currency currency = fxPresentValue.getKeys()[i];
      if (payCurrency.equals(currency)) {
        payIndex = i;
      } else if (receiveCurrency.equals(currency)) {
        receiveIndex = i;
      } else {
        throw new OpenGammaRuntimeException(ValueRequirementNames.FX_PRESENT_VALUE + " contains unexpected currency " + currency + ". Expected " + payCurrency + " or " + receiveCurrency + ".");
      }
    }
    
    double payValue = fxPresentValue.getValues()[payIndex];
    double receiveValue = fxPresentValue.getValues()[receiveIndex];
    
    double spot = (Double) inputs.getValue(ValueRequirementNames.SPOT_RATE);
    double pv = payValue + spot * receiveValue;
    return ImmutableSet.of(new ComputedValue(getResultSpec(target), pv));
  }
  
  protected ValueSpecification getResultSpec(final ComputationTarget target) {
    Currency currency = getPayCurrency((FinancialSecurity) target.getSecurity());
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), getResultProperties(currency).get());
  }
  
  protected ValueProperties.Builder getResultProperties(final Currency currency) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency.getCode());
    return properties;
  }
  
  protected Currency getPayCurrency(FinancialSecurity security) {
    return security.accept(_payCurrencyVisitor);
  }
  
  protected Currency getReceiveCurrency(FinancialSecurity security) {
    return security.accept(_receiveCurrencyVisitor);
  }

}
