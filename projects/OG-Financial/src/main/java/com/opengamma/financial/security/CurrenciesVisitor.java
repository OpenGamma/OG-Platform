/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashBalanceSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;

/**
 * Get the currencies associated with this security, returns null if not applicable.
 */
public class CurrenciesVisitor extends FinancialSecurityVisitorSameValueAdapter<Collection<Currency>> {

  private static final CurrenciesVisitor INSTANCE = new CurrenciesVisitor();

  public CurrenciesVisitor() {
    super(null);
  }

  public static CurrenciesVisitor getInstance() {
    return INSTANCE;
  }

  /**
   * @param security the security to be examined.
   * @param securitySource a security source
   * @return a Currency, where it is possible to determine a Currency association, null otherwise.
   */
  public static Collection<Currency> getCurrencies(final Security security, final SecuritySource securitySource) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(INSTANCE);
    } else if (security instanceof RawSecurity) {
      final RawSecurity rawSecurity = (RawSecurity) security;
      if (SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE.equals(security.getSecurityType())) {
        final FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
        final SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class,
            msg.getMessage());
        return Collections.singleton(securityEntryData.getCurrency());
      }
    }
    return null;
  }

  @Override
  public Collection<Currency> visitBillSecurity(final BillSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitInflationBondSecurity(final InflationBondSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCashBalanceSecurity(final CashBalanceSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCashSecurity(final CashSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCashFlowSecurity(final CashFlowSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEquitySecurity(final EquitySecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitFRASecurity(final FRASecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitSwapSecurity(final SwapSecurity security) {
    if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
      final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
      if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
        return Collections.singletonList(payLeg.getCurrency());
      }
      final Collection<Currency> collection = new ArrayList<>();
      collection.add(payLeg.getCurrency());
      collection.add(receiveLeg.getCurrency());
      return collection;
    }
    return null;
  }

  @Override
  public Collection<Currency> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
      final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
      if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
        return Collections.singletonList(payLeg.getCurrency());
      }
      final Collection<Currency> collection = new ArrayList<>();
      collection.add(payLeg.getCurrency());
      collection.add(receiveLeg.getCurrency());
      return collection;
    }
    return null;
  }

  @Override
  public Collection<Currency> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitFXOptionSecurity(final FXOptionSecurity security) {
    final Collection<Currency> currencies = new ArrayList<>();
    currencies.add(security.getCallCurrency());
    currencies.add(security.getPutCurrency());
    return currencies;
  }

  @Override
  public Collection<Currency> visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    final Collection<Currency> currencies = new ArrayList<>();
    currencies.add(security.getCallCurrency());
    currencies.add(security.getPutCurrency());
    //deliveryCurrency is always already covered
    return currencies;
  }

  @Override
  public Collection<Currency> visitSwaptionSecurity(final SwaptionSecurity security) {
    // REVIEW: jim 1-Aug-2011 -- should we include the currencies of the underlying?
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity commodityFutureOptionSecurity) {
    return Collections.singleton(commodityFutureOptionSecurity.getCurrency());
  }

  @Override
  public Collection<Currency> visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return Collections.singleton(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    final Collection<Currency> currencies = new ArrayList<>();
    currencies.add(security.getCallCurrency());
    currencies.add(security.getPutCurrency());
    return currencies;
  }

  @Override
  public Collection<Currency> visitFXForwardSecurity(final FXForwardSecurity security) {
    final Collection<Currency> currencies = new ArrayList<>();
    currencies.add(security.getPayCurrency());
    currencies.add(security.getReceiveCurrency());
    return currencies;
  }

  @Override
  public Collection<Currency> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    final Collection<Currency> currencies = new ArrayList<>();
    currencies.add(security.getPayCurrency());
    currencies.add(security.getReceiveCurrency());
    return currencies;
  }

  @Override
  public Collection<Currency> visitCapFloorSecurity(final CapFloorSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    final Collection<Currency> currencies = new ArrayList<>();
    currencies.add(security.getCallCurrency());
    currencies.add(security.getPutCurrency());
    return currencies;
  }

  @Override
  public Collection<Currency> visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
    final Collection<Currency> currencies = new ArrayList<>();
    currencies.add(security.getCallCurrency());
    currencies.add(security.getPutCurrency());
    return currencies;
  }

  @Override
  public Collection<Currency> visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitBondFutureSecurity(final BondFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitFXFutureSecurity(final FXFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitStockFutureSecurity(final StockFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitMetalForwardSecurity(final MetalForwardSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCDSSecurity(final CDSSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    return Collections.singletonList(security.getNotional().getCurrency());
  }

  @Override
  public Collection<Currency> visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
    return Collections.singletonList(security.getNotional().getCurrency());
  }

  @Override
  public Collection<Currency> visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
    return Collections.singletonList(security.getNotional().getCurrency());
  }

  @Override
  public Collection<Currency> visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    return Collections.singletonList(security.getNotional().getCurrency());
  }

  @Override
  public Collection<Currency> visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
    return Collections.singletonList(security.getNotional().getCurrency());
  }

  @Override
  public Collection<Currency> visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
    return Collections.singletonList(security.getNotional().getCurrency());
  }

  @Override
  public Collection<Currency> visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    return Collections.singletonList(security.getNotional().getCurrency());
  }

  @Override
  public Collection<Currency> visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }

  @Override
  public Collection<Currency> visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
      final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
      if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
        return Collections.singletonList(payLeg.getCurrency());
      }
      final Collection<Currency> collection = new ArrayList<>();
      collection.add(payLeg.getCurrency());
      collection.add(receiveLeg.getCurrency());
      return collection;
    }
    return null;
  }

  @Override
  public Collection<Currency> visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
      final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
      if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
        return Collections.singletonList(payLeg.getCurrency());
      }
      final Collection<Currency> collection = new ArrayList<>();
      collection.add(payLeg.getCurrency());
      collection.add(receiveLeg.getCurrency());
      return collection;
    }
    return null;
  }

  @Override
  public Collection<Currency> visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    final Collection<Currency> collection = new HashSet<>();
    for (final InterestRateSwapLeg leg : security.getLegs()) {
      collection.add(leg.getNotional().getCurrency());
    }
    return collection;
  }

  @Override
  public Collection<Currency> visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    return Collections.singletonList(security.getCurrency());
  }
}
