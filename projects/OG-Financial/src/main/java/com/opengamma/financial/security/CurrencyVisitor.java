/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security;

import java.util.Iterator;

import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.core.security.Security;
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
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
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
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
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
 * Determine the currency of the security, return null if not applicable.
 */
public class CurrencyVisitor extends FinancialSecurityVisitorSameValueAdapter<Currency> {

  private static final CurrencyVisitor INSTANCE = new CurrencyVisitor();

  public CurrencyVisitor() {
    super(null);
  }

  public static CurrencyVisitor getInstance() {
    return INSTANCE;
  }

  /**
   * @param security the security to be examined.
   * @return a Currency, where it is possible to determine a single Currency association, null otherwise.
   */
  public static Currency getCurrency(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(INSTANCE);
    } else if (security instanceof RawSecurity) {
      final RawSecurity rawSecurity = (RawSecurity) security;
      if (SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE.equals(security.getSecurityType())) {
        final FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
        final SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class,
            msg.getMessage());
        return securityEntryData.getCurrency();
      }
    }
    return null;
  }

  @Override
  public Currency visitBillSecurity(final BillSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitInflationBondSecurity(final InflationBondSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCashBalanceSecurity(final CashBalanceSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCashSecurity(final CashSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCashFlowSecurity(final CashFlowSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEquitySecurity(final EquitySecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitFRASecurity(final FRASecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitSwapSecurity(final SwapSecurity security) {
    if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
      final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
      if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
        return payLeg.getCurrency();
      }
    }
    return null;
  }

  @Override
  public Currency visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
      final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
      if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
        return payLeg.getCurrency();
      }
    }
    return null;
  }

  @Override
  public Currency visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitFXOptionSecurity(final FXOptionSecurity security) {
    throw new UnsupportedOperationException("FX securities do not have a currency");
  }

  @Override
  public Currency visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    throw new UnsupportedOperationException("FX securities do not have a currency");
  }

  @Override
  public Currency visitSwaptionSecurity(final SwaptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
    return equityIndexDividendFutureOptionSecurity.getCurrency();
  }

  @Override
  public Currency visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity equityIndexFutureOptionSecurity) {
    return equityIndexFutureOptionSecurity.getCurrency();
  }

  @Override
  public Currency visitCapFloorSecurity(final CapFloorSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitBondFutureSecurity(final BondFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitFXFutureSecurity(final FXFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitStockFutureSecurity(final StockFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitMetalForwardSecurity(final MetalForwardSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCDSSecurity(final CDSSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Currency visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Currency visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Currency visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Currency visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Currency visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Currency visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    return security.getNotional().getCurrency();
  }

  @Override
  public Currency visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    return security.getCurrency();
  }

  @Override
  public Currency visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
      final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
      if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
        return payLeg.getCurrency();
      }
    }
    return null;
  }

  @Override
  public Currency visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
      final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
      final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
      if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
        return payLeg.getCurrency();
      }
    }
    return null;
  }

  @Override
  public Currency visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    final Iterator<InterestRateSwapLeg> iterator = security.getLegs().iterator();
    if (iterator.hasNext()) {
      final Currency ccy = iterator.next().getNotional().getCurrency();
      for (final InterestRateSwapLeg leg = iterator.next(); iterator.hasNext(); iterator.next()) {
        if (!leg.getNotional().getCurrency().equals(ccy)) {
          return null; // FX swap
        }
      }
      return ccy;
    }
    return null;
  }

  @Override
  public Currency visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    return security.getCurrency();
  }
}
