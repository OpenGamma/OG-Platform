/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security;

import com.google.common.base.Preconditions;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.cash.CashBalanceSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Obtain notional from a security, null if not applicable.
 */
public class NotionalVisitor extends FinancialSecurityVisitorAdapter<CurrencyAmount> {

  private final CurrencyPairs _currencyPairs;
  private final SecuritySource _securitySource;

  public NotionalVisitor(final CurrencyPairs currencyPairs, final SecuritySource securitySource) {
    _currencyPairs = currencyPairs;
    _securitySource = securitySource;
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
  public CurrencyAmount visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    //TODO: Handle more than 2 legs
    final InterestRateSwapLeg payNotional = security.getPayLeg();
    final InterestRateSwapLeg receiveNotional = security.getReceiveLeg();
    final InterestRateNotional pay = payNotional.getNotional();
    final InterestRateNotional receive = receiveNotional.getNotional();
    if (Double.compare(pay.getAmount(), receive.getAmount()) == 0) {
      return CurrencyAmount.of(pay.getCurrency(), pay.getAmount());
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
  public CurrencyAmount visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
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

  @Override
  public CurrencyAmount visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    final InterestRateNotional notional = security.getNotional();
    final int sign = security.isBuy() ? -1 : 1;
    return CurrencyAmount.of(notional.getCurrency(), sign * notional.getAmount());
  }

  @Override
  public CurrencyAmount visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    final InterestRateNotional notional = security.getNotional();
    final int sign = security.isBuy() ? -1 : 1;
    return CurrencyAmount.of(notional.getCurrency(), sign * notional.getAmount());
  }

  @Override
  public CurrencyAmount visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getMinimumAmount();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitCorporateBondSecurity(final CorporateBondSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getMinimumAmount();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getMinimumAmount();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitSwaptionSecurity(final SwaptionSecurity security) {
    final Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    Preconditions.checkState(underlying instanceof SwapSecurity,
        "Failed to resolve underlying SwapSecurity. DB record potentially corrupted. '%s' returned.",
        underlying);
    final CurrencyAmount notional = ((SwapSecurity) underlying).accept(this);
    if (security.isLong()) {
      return notional;
    }
    return notional.multipliedBy(-1);
  }

  @Override
  public CurrencyAmount visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getPointValue();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitEquityOptionSecurity(final EquityOptionSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getPointValue();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitEquitySecurity(final EquitySecurity security) {
    final Currency currency = security.getCurrency();
    return CurrencyAmount.of(currency, 1.0);
  }

  @Override
  public CurrencyAmount visitEquityFutureSecurity(final EquityFutureSecurity security) {
    final Currency currency = security.getCurrency();
    return CurrencyAmount.of(currency, security.getUnitAmount());
  }

  @Override
  public CurrencyAmount visitMetalFutureSecurity(final MetalFutureSecurity security) {
    final Currency currency = security.getCurrency();
    return CurrencyAmount.of(currency, security.getUnitAmount());
  }

  @Override
  public CurrencyAmount visitBondFutureSecurity(final BondFutureSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getUnitAmount();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getUnitAmount();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    final Security underlying = _securitySource.getSingle(security.getUnderlyingId().toBundle());
    Preconditions.checkState(underlying instanceof InterestRateFutureSecurity,
        "Failed to resolve underlying InterestRateFutureSecurity. " +
            "DB record potentially corrupted. '%s' returned.",
        underlying);
    return ((InterestRateFutureSecurity) underlying).accept(this);
  }

  @Override
  public CurrencyAmount visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getUnitAmount();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    final InterestRateNotional notional = security.getNotional();
    final int sign = security.isBuy() ? -1 : 1;
    return CurrencyAmount.of(notional.getCurrency(), sign * notional.getAmount());
  }

  @Override
  public CurrencyAmount visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    final Currency currency = security.getCurrency();
    final double notional = security.getNotional();
    return CurrencyAmount.of(currency, notional);
  }

  @Override
  public CurrencyAmount visitCashFlowSecurity(final CashFlowSecurity security) {
    return CurrencyAmount.of(security.getCurrency(), security.getAmount());
  }

  @Override
  public CurrencyAmount visitCashBalanceSecurity(final CashBalanceSecurity security) {
    return CurrencyAmount.of(security.getCurrency(), security.getAmount());
  }

  @Override
  public CurrencyAmount visitCashSecurity(final CashSecurity security) {
    return CurrencyAmount.of(security.getCurrency(), security.getAmount());
  }

  @Override
  public CurrencyAmount visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    return CurrencyAmount.of(security.getCurrency(), security.getNotional());
  }

  @Override
  public CurrencyAmount visitFRASecurity(final FRASecurity security) {
    return CurrencyAmount.of(security.getCurrency(), security.getAmount());
  }

  @Override
  public CurrencyAmount visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    return CurrencyAmount.of(security.getCurrency(), security.getAmount());
  }

  @Override
  public CurrencyAmount visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
    return CurrencyAmount.of(security.getNotionalCurrency(), security.getNotionalAmount());
  }

  @Override
  public CurrencyAmount visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
    return CurrencyAmount.of(security.getNotionalCurrency(), security.getNotionalAmount());
  }

  @Override
  public CurrencyAmount visitIndexCDSSecurity(IndexCDSSecurity security) {
    final InterestRateNotional notional = security.getNotional();
    final int sign = security.isBuyProtection() ? -1 : 1;
    return CurrencyAmount.of(notional.getCurrency(), sign * notional.getAmount());
  }

  @Override
  public CurrencyAmount visitLegacyCDSSecurity(LegacyCDSSecurity security) {
    final InterestRateNotional notional = security.getNotional();
    final int sign = security.isBuyProtection() ? -1 : 1;
    return CurrencyAmount.of(notional.getCurrency(), sign * notional.getAmount());
  }

  @Override
  public CurrencyAmount visitStandardCDSSecurity(StandardCDSSecurity security) {
    final InterestRateNotional notional = security.getNotional();
    final int sign = security.isBuyProtection() ? -1 : 1;
    return CurrencyAmount.of(notional.getCurrency(), sign * notional.getAmount());
  }
}
