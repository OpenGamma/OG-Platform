/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
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

  private static class CallCurrencyVisitor implements FinancialSecurityVisitor<Currency> {

    public CallCurrencyVisitor() {
    }

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

    @Override
    public Currency visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      return security.getCallCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      return security.getCallCurrency();
    }

    @Override
    public Currency visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }
  }

  private static class PutCurrencyVisitor implements FinancialSecurityVisitor<Currency> {

    public PutCurrencyVisitor() {
    }

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

    @Override
    public Currency visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      return security.getPutCurrency();
    }

    @Override
    public Currency visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      return security.getPutCurrency();
    }

    @Override
    public Currency visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Currency visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }
  }

  private static class SpotIdentifierVisitor implements FinancialSecurityVisitor<ValueRequirement> {

    public SpotIdentifierVisitor() {
    }

    @Override
    public ValueRequirement visitBondSecurity(final BondSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitCashSecurity(final CashSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquitySecurity(final EquitySecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitFRASecurity(final FRASecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitFutureSecurity(final FutureSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitSwapSecurity(final SwapSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityOptionSecurity(final EquityOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitFXOptionSecurity(final FXOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitSwaptionSecurity(final SwaptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
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
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitCapFloorSecurity(final CapFloorSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      throw new UnsupportedOperationException();
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

    @Override
    public ValueRequirement visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
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

  private static class InverseSpotIdentifierVisitor implements FinancialSecurityVisitor<ValueRequirement> {

    public InverseSpotIdentifierVisitor() {
    }

    @Override
    public ValueRequirement visitBondSecurity(final BondSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitCashSecurity(final CashSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquitySecurity(final EquitySecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitFRASecurity(final FRASecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitFutureSecurity(final FutureSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitSwapSecurity(final SwapSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityOptionSecurity(final EquityOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitFXOptionSecurity(final FXOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getInverseSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitSwaptionSecurity(final SwaptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      final Currency putCurrency = security.getPutCurrency();
      final Currency callCurrency = security.getCallCurrency();
      return getInverseSpotIdentifierRequirement(putCurrency, callCurrency);
    }

    @Override
    public ValueRequirement visitFXForwardSecurity(final FXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitCapFloorSecurity(final CapFloorSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      throw new UnsupportedOperationException();
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

    @Override
    public ValueRequirement visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ValueRequirement visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
      throw new UnsupportedOperationException();
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
