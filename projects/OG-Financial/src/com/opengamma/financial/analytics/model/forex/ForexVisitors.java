/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class ForexVisitors {
  private static final FinancialSecurityVisitor<Currency> s_callCurrencyInstance = new CallCurrencyVisitor();
  private static final FinancialSecurityVisitor<Currency> s_putCurrencyInstance = new PutCurrencyVisitor();
  private static final FinancialSecurityVisitor<ValueRequirement> s_spotIdentifierInstance = new SpotIdentifierVisitor();
  private static final FinancialSecurityVisitor<ValueRequirement> s_inverseSpotIdentifierInstance = new InverseSpotIdentifierVisitor();

  public static FinancialSecurityVisitor<Currency> getCallCurrencyVisitor() {
    return s_callCurrencyInstance;
  }

  public static FinancialSecurityVisitor<Currency> getPutCurrencyVisitor() {
    return s_putCurrencyInstance;
  }

  public static FinancialSecurityVisitor<ValueRequirement> getSpotIdentifierVisitor() {
    return s_spotIdentifierInstance;
  }

  public static FinancialSecurityVisitor<ValueRequirement> getInverseSpotIdentifierVisitor() {
    return s_inverseSpotIdentifierInstance;
  }

  private static class CallCurrencyVisitor extends FinancialSecurityVisitorAdapter<Currency> {

    @Override
    public Currency visitFXOptionSecurity(final FXOptionSecurity security) {
      return security.getCallCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      return security.getCallCurrency();
    }

    @Override
    public Currency visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return security.getCallCurrency();
    }

    @Override
    public Currency visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      return security.getCallCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      return security.getCallCurrency();
    }
  }

  private static class PutCurrencyVisitor extends FinancialSecurityVisitorAdapter<Currency> {

    @Override
    public Currency visitFXOptionSecurity(final FXOptionSecurity security) {
      return security.getPutCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      return security.getPutCurrency();
    }

    @Override
    public Currency visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return security.getPutCurrency();
    }

    @Override
    public Currency visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      return security.getPutCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      return security.getPutCurrency();
    }
  }

  private static class SpotIdentifierVisitor extends FinancialSecurityVisitorAdapter<ValueRequirement> {

    @Override
    public ValueRequirement visitFXOptionSecurity(final FXOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitFXForwardSecurity(final FXForwardSecurity security) {
      final Currency payCurrency = security.getPayCurrency();
      final Currency receiveCurrency = security.getReceiveCurrency();
      return getSpotIdentifierRequirement(payCurrency, receiveCurrency);
    }

    @Override
    public ValueRequirement visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      final Currency payCurrency = security.getPayCurrency();
      final Currency receiveCurrency = security.getReceiveCurrency();
      return getSpotIdentifierRequirement(payCurrency, receiveCurrency);
    }

    @Override
    public ValueRequirement visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getSpotIdentifierRequirement(putCurrency, callCurrency);
    }
  }

  private static ValueRequirement getSpotIdentifierRequirement(final Currency putCurrency, final Currency callCurrency) {
    UnorderedCurrencyPair currencyPair;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) {
      currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    } else {
      currencyPair = UnorderedCurrencyPair.of(callCurrency, putCurrency);
    }
    return new ValueRequirement(ValueRequirementNames.SPOT_RATE, currencyPair);
  }

  private static class InverseSpotIdentifierVisitor extends FinancialSecurityVisitorAdapter<ValueRequirement> {

    @Override
    public ValueRequirement visitFXOptionSecurity(final FXOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getInverseSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getInverseSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getInverseSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getInverseSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getInverseSpotIdentifierRequirement(putCurrency, callCurrency);
    }

  }

  private static ValueRequirement getInverseSpotIdentifierRequirement(final Currency putCurrency, final Currency callCurrency) {
    UnorderedCurrencyPair currencyPair;
    if (!FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) {
      currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    } else {
      currencyPair = UnorderedCurrencyPair.of(callCurrency, putCurrency);
    }
    return new ValueRequirement(ValueRequirementNames.SPOT_RATE, currencyPair);
  }
}
