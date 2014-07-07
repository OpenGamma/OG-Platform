/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
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
import com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.AmericanDepositaryReceiptSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.ExchangeTradedFundSecurity;
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
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
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
import com.opengamma.financial.security.option.EquityWarrantSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.BillTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class UnderlyingExposureFunction implements ExposureFunction {
  private final SecuritySource _securitySource;

  public UnderlyingExposureFunction(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _securitySource = securitySource;
  }

  @Override
  public String getName() {
    return "Underlying";
  }

  @Override
  public List<ExternalId> visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitBondFutureSecurity(final BondFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitFXFutureSecurity(final FXFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitStockFutureSecurity(final StockFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return Arrays.asList(security.getUnderlyingId());
  }

  @Override
  public List<ExternalId> visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return Arrays.asList(security.getUnderlyingId());
  }

  @Override
  public List<ExternalId> visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    final List<ExternalId> result = new ArrayList<>();
    result.add(security.getLongId());
    result.add(security.getShortId());
    return result;
  }

  @Override
  public List<ExternalId> visitCapFloorSecurity(final CapFloorSecurity security) {
    return Arrays.asList(security.getUnderlyingId());
  }

  @Override
  public List<ExternalId> visitCashBalanceSecurity(final CashBalanceSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCashSecurity(final CashSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCashFlowSecurity(final CashFlowSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquitySecurity(final EquitySecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitFRASecurity(final FRASecurity security) {
    return Arrays.asList(security.getUnderlyingId());
  }

  @Override
  public List<ExternalId> visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    return Arrays.asList(security.getUnderlyingId());
  }

  @Override
  public List<ExternalId> visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitFXForwardSecurity(final FXForwardSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitFXOptionSecurity(final FXOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    final List<ExternalId> result = new ArrayList<>();
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (payLeg instanceof FloatingInterestRateLeg) {
      result.add(((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId());
    }
    if (receiveLeg instanceof FloatingInterestRateLeg) {
      result.add(((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
    }
    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  @Override
  public List<ExternalId> visitBillSecurity(final BillSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitInflationBondSecurity(final InflationBondSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitSwapSecurity(final SwapSecurity security) {
    final List<ExternalId> result = new ArrayList<>();
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (payLeg instanceof FloatingInterestRateLeg) {
      result.add(((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId());
    }
    if (receiveLeg instanceof FloatingInterestRateLeg) {
      result.add(((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
    }
    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  @Override
  public List<ExternalId> visitSwaptionSecurity(final SwaptionSecurity security) {
    final List<ExternalId> result = new ArrayList<>();
    final SwapSecurity underlyingSwap = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId())); //TODO version
    final SwapLeg payLeg = underlyingSwap.getPayLeg();
    final SwapLeg receiveLeg = underlyingSwap.getReceiveLeg();
    if (payLeg instanceof FloatingInterestRateLeg) {
      result.add(((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId());
    }
    if (receiveLeg instanceof FloatingInterestRateLeg) {
      result.add(((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId());
    }
    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  @Override
  public List<ExternalId> visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitMetalForwardSecurity(final MetalForwardSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCDSSecurity(final CDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    final List<ExternalId> result = new ArrayList<>();
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (payLeg instanceof InflationIndexSwapLeg) {
      result.add(((InflationIndexSwapLeg) payLeg).getIndexId());
    }
    if (receiveLeg instanceof InflationIndexSwapLeg) {
      result.add(((InflationIndexSwapLeg) receiveLeg).getIndexId());
    }
    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  @Override
  public List<ExternalId> visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    final List<ExternalId> result = new ArrayList<>();
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    if (payLeg instanceof InflationIndexSwapLeg) {
      result.add(((InflationIndexSwapLeg) payLeg).getIndexId());
    }
    if (receiveLeg instanceof InflationIndexSwapLeg) {
      result.add(((InflationIndexSwapLeg) receiveLeg).getIndexId());
    }
    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  @Override
  public List<ExternalId> visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    final List<ExternalId> result = new ArrayList<>();
    for (final InterestRateSwapLeg leg : security.getLegs()) {
      if (leg instanceof FloatingInterestRateSwapLeg) {
        final ExternalIdBundle ids = ((FloatingInterestRateSwapLeg) leg).getFloatingReferenceRateId().toBundle();
        for (final ExternalId id : ids) {
          result.add(id);
          // only add the first id per leg, if multiple ids resolving to the same rate were returned
          // the caller could over estimate their exposure
          break;
        }
      }
    }
    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  @Override
  public List<ExternalId> visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitExchangeTradedFundSecurity(final ExchangeTradedFundSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitAmericanDepositaryReceiptSecurity(final AmericanDepositaryReceiptSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitEquityWarrantSecurity(final EquityWarrantSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
    return Collections.singletonList(security.getBenchmarkRateId());
  }

  @Override
  public List<ExternalId> visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
    return Collections.singletonList(security.getFundingLeg().getFloatingReferenceRateId());
  }

  @Override
  public List<ExternalId> visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
    return Collections.singletonList(security.getFundingLeg().getFloatingReferenceRateId());
  }

  @Override
  public List<ExternalId> visitBillTotalReturnSwapSecurity(BillTotalReturnSwapSecurity security) {
    return Collections.singletonList(security.getFundingLeg().getFloatingReferenceRateId());
  }

  @Override
  public List<ExternalId> visitStandardCDSSecurity(final StandardCDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitLegacyCDSSecurity(final LegacyCDSSecurity security) {
    return null;
  }

  @Override
  public List<ExternalId> visitIndexCDSSecurity(IndexCDSSecurity security) {
    return Arrays.asList(security.getUnderlyingIndex());
  }

  @Override
  public List<ExternalId> visitIndexCDSDefinitionSecurity(IndexCDSDefinitionSecurity security) {
    return null;
  }
}
