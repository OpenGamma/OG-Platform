/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.digest.SecurityTypeTargetDigests;
import com.opengamma.engine.target.digest.TargetDigests;
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
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;

/**
 * Extension to the basic target digests from OG-Engine that supplements the digest with the instrument's currency. This is based on the observation that many of the default constraints are configured
 * with per-currency information.
 * <p>
 * Note that this is not currently appropriate for use alongside some of the default value injecting functions, for example the ones that pick identifiers out of the calculation configuration and
 * inject constraints based on those.
 */
public class FinancialSecurityTargetDigests extends SecurityTypeTargetDigests implements FinancialSecurityVisitor<Object>, NotionalVisitor<Object> {

  // TODO: Should items with an exchange use that in the digest too to capture the per-exchange default constraints?

  // TODO: Fix for the classes of functions that do behave differently (or suppress the logic if items are in the calculation configuration that drive those functions)

  private final MapImpl<Object, Digests> _pairs = new MapImpl<Object, Digests>() {
    @Override
    protected Digests createValue(final Object key) {
      return new Digests(key);
    }
  };

  public FinancialSecurityTargetDigests() {
    addHandler(ComputationTargetType.CURRENCY, new TargetDigests() {
      @Override
      public Object getDigest(final FunctionCompilationContext context, final ComputationTargetSpecification targetSpec) {
        return targetSpec.getUniqueId().getValue();
      }
    });
  }

  protected Object pair(final Object a, final Object b) {
    if (b == null) {
      return null;
    }
    final Digests digests = _pairs.get(a);
    if (digests != null) {
      return digests.get(b);
    } else {
      return null;
    }
  }

  // SecurityTypeTargetDigests

  @Override
  protected Object getSecurityDigest(final Security security) {
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(this);
    } else {
      return super.getSecurityDigest(security);
    }
  }

  // FinancialSecurityVisitor

  private final Digests _deliverableSwapFutureSecurity = new Digests("DeliverableSwapFutureSecurity");

  @Override
  public Object visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    return _deliverableSwapFutureSecurity.get(security.getCurrency());
  }

  private final Digests _energyForwardSecurity = new Digests("EnergyForwardSecurity");

  @Override
  public Object visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
    return _energyForwardSecurity.get(security.getCurrency());
  }

  private final Digests _agricultureForwardSecurity = new Digests("AgricultureForwardSecurity");

  @Override
  public Object visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
    return _agricultureForwardSecurity.get(security.getCurrency());
  }

  private final Digests _metalForwardSecurity = new Digests("MetalForwardSecurity");

  @Override
  public Object visitMetalForwardSecurity(final MetalForwardSecurity security) {
    return _metalForwardSecurity.get(security.getCurrency());
  }

  private final Digests _cdsSecurity = new Digests("CDSSecurity");

  @Override
  public Object visitCDSSecurity(final CDSSecurity security) {
    return _cdsSecurity.get(security.getCurrency());
  }

  private final Digests _standardVanillaCDSSecurity = new Digests("Security");

  @Override
  public Object visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    return _standardVanillaCDSSecurity.get(security.getNotional().getCurrency());
  }

  private final Digests _standardFixedRecoveryCDSSecurity = new Digests("Security");

  @Override
  public Object visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
    return _standardFixedRecoveryCDSSecurity.get(security.getNotional().getCurrency());
  }

  private final Digests _standardRecoveryLockCDSSecurity = new Digests("Security");

  @Override
  public Object visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
    return _standardRecoveryLockCDSSecurity.get(security.getNotional().getCurrency());
  }

  private final Digests _legacyVanillaCDSSecurity = new Digests("Security");

  @Override
  public Object visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    return _legacyVanillaCDSSecurity.get(security.getNotional().getCurrency());
  }

  private final Digests _legacyFixedRecoveryCDSSecurity = new Digests("Security");

  @Override
  public Object visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
    return _legacyFixedRecoveryCDSSecurity.get(security.getNotional().getCurrency());
  }

  private final Digests _legacyRecoveryLockCDSSecurity = new Digests("Security");

  @Override
  public Object visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
    return _legacyRecoveryLockCDSSecurity.get(security.getNotional().getCurrency());
  }

  private final Digests _creditDefaultSwapIndexDefinitionSecurity = new Digests("Security");

  @Override
  public Object visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
    return _creditDefaultSwapIndexDefinitionSecurity.get(security.getCurrency());
  }

  private final Digests _creditDefaultSwapIndexSecurity = new Digests("Security");

  @Override
  public Object visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    return _creditDefaultSwapIndexSecurity.get(security.getNotional().getCurrency());
  }

  private final Digests _creditDefaultSwapOptionSecurity = new Digests("Security");

  @Override
  public Object visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    return _creditDefaultSwapOptionSecurity.get(security.getCurrency());
  }

  private final Digests _agricultureFutureSecurity = new Digests("Security");

  @Override
  public Object visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return _agricultureFutureSecurity.get(security.getCurrency());
  }

  private final Digests _bondFutureSecurity = new Digests("Security");

  @Override
  public Object visitBondFutureSecurity(final BondFutureSecurity security) {
    return _bondFutureSecurity.get(security.getCurrency());
  }

  private final Digests _equityIndexDividendFutureSecurity = new Digests("Security");

  @Override
  public Object visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return _equityIndexDividendFutureSecurity.get(security.getCurrency());
  }

  private final Digests _fxFutureSecurity = new Digests("Security");

  @Override
  public Object visitFXFutureSecurity(final FXFutureSecurity security) {
    return _fxFutureSecurity.get(security.getCurrency());
  }

  private final Digests _stockFutureSecurity = new Digests("Security");

  @Override
  public Object visitStockFutureSecurity(final StockFutureSecurity security) {
    return _stockFutureSecurity.get(security.getCurrency());
  }

  private final Digests _equityFutureSecurity = new Digests("Security");

  @Override
  public Object visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return _equityFutureSecurity.get(security.getCurrency());
  }

  private final Digests _energyFutureSecurity = new Digests("Security");

  @Override
  public Object visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return _energyFutureSecurity.get(security.getCurrency());
  }

  private final Digests _indexFutureSecurity = new Digests("Security");

  @Override
  public Object visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return _indexFutureSecurity.get(security.getCurrency());
  }

  private final Digests _interestRateFutureSecurity = new Digests("Security");

  @Override
  public Object visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return _interestRateFutureSecurity.get(security.getCurrency());
  }

  private final Digests _federalFundsFutureSecurity = new Digests("Security");

  @Override
  public Object visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    return _federalFundsFutureSecurity.get(security.getCurrency());
  }

  private final Digests _metalFutureSecurity = new Digests("Security");

  @Override
  public Object visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return _metalFutureSecurity.get(security.getCurrency());
  }

  private final Digests _capFloorCMSSpreadSecurity = new Digests("Security");

  @Override
  public Object visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    return _capFloorCMSSpreadSecurity.get(security.getCurrency());
  }

  private final Digests _capFloorSecurity = new Digests("Security");

  @Override
  public Object visitCapFloorSecurity(final CapFloorSecurity security) {
    return _capFloorSecurity.get(security.getCurrency());
  }

  private final Digests _cashBalanceSecurity = new Digests("Security");

  @Override
  public Object visitCashBalanceSecurity(final CashBalanceSecurity security) {
    return _cashBalanceSecurity.get(security.getCurrency());
  }

  private final Digests _cashSecurity = new Digests("Security");

  @Override
  public Object visitCashSecurity(final CashSecurity security) {
    return _cashSecurity.get(security.getCurrency());
  }

  private final Digests _cashFlowSecurity = new Digests("Security");

  @Override
  public Object visitCashFlowSecurity(final CashFlowSecurity security) {
    return _cashFlowSecurity.get(security.getCurrency());
  }

  private final Digests _commodityFutureOptionSecurity = new Digests("Security");

  @Override
  public Object visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    return _commodityFutureOptionSecurity.get(security.getCurrency());
  }

  private final Digests _fxFutureOptionSecurity = new Digests("Security");

  @Override
  public Object visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    return _fxFutureOptionSecurity.get(security.getCurrency());
  }

  private final Digests _bondFutureOptionSecurity = new Digests("Security");

  @Override
  public Object visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    return _bondFutureOptionSecurity.get(security.getCurrency());
  }

  private final Digests _continuousZeroDepositSecurity = new Digests("Security");

  @Override
  public Object visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    return _continuousZeroDepositSecurity.get(security.getCurrency());
  }

  private final Digests _corporateBondSecurity = new Digests("Security");

  @Override
  public Object visitCorporateBondSecurity(final CorporateBondSecurity security) {
    return _corporateBondSecurity.get(security.getCurrency());
  }

  private final Digests _equityBarrierOptionSecurity = new Digests("Security");

  @Override
  public Object visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    return _equityBarrierOptionSecurity.get(security.getCurrency());
  }

  private final Digests _equityIndexDividendFutureOptionSecurity = new Digests("Security");

  @Override
  public Object visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    return _equityIndexDividendFutureOptionSecurity.get(security.getCurrency());
  }

  private final Digests _equityIndexFutureOptionSecurity = new Digests("Security");

  @Override
  public Object visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    return _equityIndexFutureOptionSecurity.get(security.getCurrency());
  }

  private final Digests _equityIndexOptionSecurity = new Digests("Security");

  @Override
  public Object visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    return _equityIndexOptionSecurity.get(security.getCurrency());
  }

  private final Digests _equityOptionSecurity = new Digests("Security");

  @Override
  public Object visitEquityOptionSecurity(final EquityOptionSecurity security) {
    return _equityOptionSecurity.get(security.getCurrency());
  }

  private final Digests _equitySecurity = new Digests("Security");

  @Override
  public Object visitEquitySecurity(final EquitySecurity security) {
    return _equitySecurity.get(security.getCurrency());
  }

  private final Digests _equityVarianceSwapSecurity = new Digests("Security");

  @Override
  public Object visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    return _equityVarianceSwapSecurity.get(security.getCurrency());
  }

  private final Digests _fraSecurity = new Digests("Security");

  @Override
  public Object visitFRASecurity(final FRASecurity security) {
    return _fraSecurity.get(security.getCurrency());
  }

  private final Digests _forwardRateAgreementSecurity = new Digests("Security");

  @Override
  public Object visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    return _forwardRateAgreementSecurity.get(security.getCurrency());
  }

  private final Digests _fxBarrierOptionSecurity = new Digests("Security");

  @Override
  public Object visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    return _fxBarrierOptionSecurity.get(pair(security.getPutCurrency(), security.getCallCurrency()));
  }

  private final Digests _fxDigitalSecurity = new Digests("Security");

  @Override
  public Object visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    return _fxDigitalSecurity.get(pair(security.getPutCurrency(), security.getCallCurrency()));
  }

  private final Digests _fxForwardSecurity = new Digests("Security");

  @Override
  public Object visitFXForwardSecurity(final FXForwardSecurity security) {
    return _fxForwardSecurity.get(pair(security.getPayCurrency(), security.getReceiveCurrency()));
  }

  private final Digests _fxOptionSecurity = new Digests("Security");

  @Override
  public Object visitFXOptionSecurity(final FXOptionSecurity security) {
    return _fxOptionSecurity.get(pair(security.getPutCurrency(), security.getCallCurrency()));
  }

  private final Digests _forwardSwapSecurity = new Digests("Security");

  @Override
  public Object visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    return _forwardSwapSecurity.get(pair(security.getPayLeg().getNotional().accept(this), security.getReceiveLeg().getNotional().accept(this)));
  }

  private final Digests _billSecurity = new Digests("Security");

  @Override
  public Object visitBillSecurity(final BillSecurity security) {
    return _billSecurity.get(security.getCurrency());
  }

  private final Digests _governmentBondSecurity = new Digests("Security");

  @Override
  public Object visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    return _governmentBondSecurity.get(security.getCurrency());
  }

  private final Digests _irFutureOptionSecurity = new Digests("Security");

  @Override
  public Object visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    return _irFutureOptionSecurity.get(security.getCurrency());
  }

  private final Digests _municipalBondSecurity = new Digests("Security");

  @Override
  public Object visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    return _municipalBondSecurity.get(security.getCurrency());
  }

  private final Digests _inflationBondSecurity = new Digests("Security");

  @Override
  public Object visitInflationBondSecurity(final InflationBondSecurity security) {
    return _inflationBondSecurity.get(security.getCurrency());
  }

  private final Digests _nonDeliverableFXDigitalOptionSecurity = new Digests("Security");

  @Override
  public Object visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
    return _nonDeliverableFXDigitalOptionSecurity.get(pair(security.getPutCurrency(), security.getCallCurrency()));
  }

  private final Digests _nonDeliverableFXForwardSecurity = new Digests("Security");

  @Override
  public Object visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    return _nonDeliverableFXForwardSecurity.get(pair(security.getPayCurrency(), security.getReceiveCurrency()));
  }

  private final Digests _nonDeliverableFXOptionSecurity = new Digests("Security");

  @Override
  public Object visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    return _nonDeliverableFXOptionSecurity.get(pair(security.getPutCurrency(), security.getCallCurrency()));
  }

  private final Digests _periodicZeroDepositSecurity = new Digests("Security");

  @Override
  public Object visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    return _periodicZeroDepositSecurity.get(security.getCurrency());
  }

  private final Digests _simpleZeroDepositSecurity = new Digests("Security");

  @Override
  public Object visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    return _simpleZeroDepositSecurity.get(security.getCurrency());
  }

  private final Digests _swapSecurity = new Digests("Security");

  @Override
  public Object visitSwapSecurity(final SwapSecurity security) {
    return _swapSecurity.get(pair(security.getPayLeg().getNotional().accept(this), security.getReceiveLeg().getNotional().accept(this)));
  }

  @Override
  public Object visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    return _swapSecurity.get(pair(security.getPayLeg().getNotional().accept(this), security.getReceiveLeg().getNotional().accept(this)));
  }

  private final Digests _swaptionSecurity = new Digests("Security");

  @Override
  public Object visitSwaptionSecurity(final SwaptionSecurity security) {
    return _swaptionSecurity.get(security.getCurrency());
  }

  private final Digests _fxVolatilitySwapSecurity = new Digests("Security");

  @Override
  public Object visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    return _fxVolatilitySwapSecurity.get(security.getCurrency());
  }

  private final Digests _floatingRateNoteSecurity = new Digests("Security");

  @Override
  public Object visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
    return _floatingRateNoteSecurity.get(security.getCurrency());
  }

  private final Digests _equityTRSSecurity = new Digests("Security");

  @Override
  public Object visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
    return _equityTRSSecurity.get(pair(security.getNotionalCurrency(), security.getFundingLeg().getNotional().accept(this)));
  }

  private final Digests _bondTRSSecurity = new Digests("Security");

  @Override
  public Object visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
    return _bondTRSSecurity.get(pair(security.getNotionalCurrency(), security.getFundingLeg().getNotional().accept(this)));
  }

  private final Digests _billTRSSecurity = new Digests("Security");

  @Override
  public Object visitBillTotalReturnSwapSecurity(BillTotalReturnSwapSecurity security) {
    return _billTRSSecurity.get(pair(security.getNotionalCurrency(), security.getFundingLeg().getNotional().accept(this)));
  }

  private final Digests _standardCDSSecurity = new Digests("Security");

  @Override
  public Object visitStandardCDSSecurity(StandardCDSSecurity security) {
    return _standardCDSSecurity.get(security.getNotional().accept(this));
  }
  private final Digests _legacyCDSSecurity = new Digests("Security");

  @Override
  public Object visitLegacyCDSSecurity(LegacyCDSSecurity security) {
    return _legacyCDSSecurity.get(security.getNotional().accept(this));
  }

  private final Digests _indexCDSSecurity = new Digests("Security");

  @Override
  public Object visitIndexCDSSecurity(IndexCDSSecurity security) {
    return _indexCDSSecurity.get(security.getNotional().accept(this));
  }

  private final Digests _indexCDSDefSecurity = new Digests("Security");

  @Override
  public Object visitIndexCDSDefinitionSecurity(IndexCDSDefinitionSecurity security) {
    return _indexCDSDefSecurity.get(security.getCurrency());
  }

  // NotionalVisitor

  @Override
  public Object visitCommodityNotional(final CommodityNotional notional) {
    return null;
  }

  @Override
  public Object visitInterestRateNotional(final InterestRateNotional notional) {
    return notional.getCurrency();
  }

  @Override
  public Object visitSecurityNotional(final SecurityNotional notional) {
    return null;
  }

  @Override
  public Object visitVarianceSwapNotional(final VarianceSwapNotional notional) {
    return notional.getCurrency();
  }

  @Override
  public Object visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    throw new UnsupportedOperationException("Cannot handle zero coupon inflation swap securities");
  }

  @Override
  public Object visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    throw new UnsupportedOperationException("Cannot handle year-on-year inflation swap securities");
  }

  @Override
  public Object visitExchangeTradedFundSecurity(final ExchangeTradedFundSecurity security) {
    throw new UnsupportedOperationException("Cannot handle exchange traded fund securities");
  }

  @Override
  public Object visitAmericanDepositaryReceiptSecurity(final AmericanDepositaryReceiptSecurity security) {
    throw new UnsupportedOperationException("Cannot handle ADR securities");
  }

  @Override
  public Object visitEquityWarrantSecurity(final EquityWarrantSecurity security) {
    throw new UnsupportedOperationException("Cannot handle equity warrant securities");
  }

}
