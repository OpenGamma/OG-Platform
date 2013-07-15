/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ForexVisitors {
  private static final FinancialSecurityVisitor<Currency> s_payCurrencyInstance = new PayCurrencyVisitor();
  private static final FinancialSecurityVisitor<Currency> s_receiveCurrencyInstance = new ReceiveCurrencyVisitor();
  private static final FinancialSecurityVisitor<Currency> s_callCurrencyInstance = new CallCurrencyVisitor();
  private static final FinancialSecurityVisitor<Currency> s_putCurrencyInstance = new PutCurrencyVisitor();
  private static final FinancialSecurityVisitor<ZonedDateTime> s_expiryInstance = new ExpiryVisitor();

  public static FinancialSecurityVisitor<Currency> getPayCurrencyVisitor() {
    return s_payCurrencyInstance;
  }

  public static FinancialSecurityVisitor<Currency> getReceiveCurrencyVisitor() {
    return s_receiveCurrencyInstance;
  }

  public static FinancialSecurityVisitor<Currency> getCallCurrencyVisitor() {
    return s_callCurrencyInstance;
  }

  public static FinancialSecurityVisitor<Currency> getPutCurrencyVisitor() {
    return s_putCurrencyInstance;
  }

  public static FinancialSecurityVisitor<ZonedDateTime> getExpiryVisitor() {
    return s_expiryInstance;
  }

  private static class PayCurrencyVisitor extends FinancialSecurityVisitorAdapter<Currency> {

    public PayCurrencyVisitor() {
    }

    @Override
    public Currency visitFXForwardSecurity(final FXForwardSecurity security) {
      return security.getPayCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      return security.getPayCurrency();
    }

    @Override
    // Marc
    public Currency visitSwapSecurity(final SwapSecurity security) {
      return ((InterestRateNotional) security.getPayLeg().getNotional()).getCurrency();
    }
  }

  private static class ReceiveCurrencyVisitor extends FinancialSecurityVisitorAdapter<Currency> {

    public ReceiveCurrencyVisitor() {
    }

    @Override
    public Currency visitFXForwardSecurity(final FXForwardSecurity security) {
      return security.getReceiveCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      return security.getReceiveCurrency();
    }

    @Override
    // Marc
    public Currency visitSwapSecurity(final SwapSecurity security) {
      return ((InterestRateNotional) security.getReceiveLeg().getNotional()).getCurrency();
    }
  }

  private static class CallCurrencyVisitor extends FinancialSecurityVisitorAdapter<Currency> {

    public CallCurrencyVisitor() {
    }

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

    public PutCurrencyVisitor() {
    }

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

  private static class ExpiryVisitor extends FinancialSecurityVisitorAdapter<ZonedDateTime> {

    public ExpiryVisitor() {
    }

    @Override
    public ZonedDateTime visitFXForwardSecurity(final FXForwardSecurity security) {
      return security.getForwardDate();
    }

    @Override
    public ZonedDateTime visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      return security.getForwardDate();
    }

    @Override
    public ZonedDateTime visitFXOptionSecurity(final FXOptionSecurity security) {
      return security.getExpiry().getExpiry();
    }

    @Override
    public ZonedDateTime visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      return security.getExpiry().getExpiry();
    }

    @Override
    public ZonedDateTime visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return security.getExpiry().getExpiry();
    }

    @Override
    public ZonedDateTime visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      return security.getExpiry().getExpiry();
    }

    @Override
    public ZonedDateTime visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      return security.getExpiry().getExpiry();
    }

  }
}
