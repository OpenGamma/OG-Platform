/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexVisitors {
  private static final FinancialSecurityVisitor<Currency> s_callCurrencyInstance = new CallCurrencyVisitor();
  private static final FinancialSecurityVisitor<Currency> s_putCurrencyInstance = new PutCurrencyVisitor();
  private static final FinancialSecurityVisitor<ExternalId> s_spotIdentifierInstance = new SpotIdentifierVisitor();
  private static final FinancialSecurityVisitor<ExternalId> s_inverseSpotIdentifierInstance = new InverseSpotIdentifierVisitor();

  public static FinancialSecurityVisitor<Currency> getCallCurrencyVisitor() {
    return s_callCurrencyInstance;
  }

  public static FinancialSecurityVisitor<Currency> getPutCurrencyVisitor() {
    return s_putCurrencyInstance;
  }

  public static FinancialSecurityVisitor<ExternalId> getSpotIdentifierVisitor() {
    return s_spotIdentifierInstance;
  }

  public static FinancialSecurityVisitor<ExternalId> getInverseSpotIdentifierVisitor() {
    return s_inverseSpotIdentifierInstance;
  }

  private static class CallCurrencyVisitor implements FinancialSecurityVisitor<Currency> {

    @Override
    public Currency visitBondSecurity(final BondSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitCashSecurity(final CashSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquitySecurity(final EquitySecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitFRASecurity(final FRASecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitFutureSecurity(final FutureSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitSwapSecurity(final SwapSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityOptionSecurity(final EquityOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      throw new UnsupportedOperationException();
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
    public Currency visitSwaptionSecurity(final SwaptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return security.getCallCurrency();
    }

    @Override
    public Currency visitFXSecurity(final FXSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitFXForwardSecurity(final FXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitCapFloorSecurity(final CapFloorSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      throw new UnsupportedOperationException();
    }
  }

  private static class PutCurrencyVisitor implements FinancialSecurityVisitor<Currency> {

    @Override
    public Currency visitBondSecurity(final BondSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitCashSecurity(final CashSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquitySecurity(final EquitySecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitFRASecurity(final FRASecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitFutureSecurity(final FutureSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitSwapSecurity(final SwapSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityOptionSecurity(final EquityOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      throw new UnsupportedOperationException();
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
    public Currency visitSwaptionSecurity(final SwaptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return security.getPutCurrency();
    }

    @Override
    public Currency visitFXSecurity(final FXSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitFXForwardSecurity(final FXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitCapFloorSecurity(final CapFloorSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      throw new UnsupportedOperationException();
    }
  }

  private static class SpotIdentifierVisitor implements FinancialSecurityVisitor<ExternalId> {

    @Override
    public ExternalId visitBondSecurity(final BondSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitCashSecurity(final CashSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquitySecurity(final EquitySecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFRASecurity(final FRASecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFutureSecurity(final FutureSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitSwapSecurity(final SwapSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityOptionSecurity(final EquityOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFXOptionSecurity(final FXOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      ExternalId bloomberg;
      if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
        bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
      } else {
        bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
      }
      return bloomberg;
    }

    @Override
    public ExternalId visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitSwaptionSecurity(final SwaptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      ExternalId bloomberg;
      if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
        bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
      } else {
        bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
      }
      return bloomberg;
    }

    @Override
    public ExternalId visitFXSecurity(final FXSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFXForwardSecurity(final FXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitCapFloorSecurity(final CapFloorSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      throw new UnsupportedOperationException();
    }
  }

  private static class InverseSpotIdentifierVisitor implements FinancialSecurityVisitor<ExternalId> {

    @Override
    public ExternalId visitBondSecurity(final BondSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitCashSecurity(final CashSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquitySecurity(final EquitySecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFRASecurity(final FRASecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFutureSecurity(final FutureSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitSwapSecurity(final SwapSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityOptionSecurity(final EquityOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFXOptionSecurity(final FXOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      ExternalId bloomberg;
      if (!ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
        bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
      } else {
        bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
      }
      return bloomberg;
    }

    @Override
    public ExternalId visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitSwaptionSecurity(final SwaptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      ExternalId bloomberg;
      if (!ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
        bloomberg = SecurityUtils.bloombergTickerSecurityId(putCurrency.getCode() + callCurrency.getCode() + " Curncy");
      } else {
        bloomberg = SecurityUtils.bloombergTickerSecurityId(callCurrency.getCode() + putCurrency.getCode() + " Curncy");
      }
      return bloomberg;
    }

    @Override
    public ExternalId visitFXSecurity(final FXSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitFXForwardSecurity(final FXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitCapFloorSecurity(final CapFloorSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ExternalId visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      throw new UnsupportedOperationException();
    }

  }
}
