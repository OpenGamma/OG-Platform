/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
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
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.master.security.RawSecurity;

/**
 * Function to classify positions by asset class.  Note that this bins all types of options together.
 * For more detailed subdivision, see DetailedAssetClassAggregationFunction.
 * @author jim
 */
public class AssetClassAggregationFunction implements AggregationFunction<String> {

  /* package */static final String FX_OPTIONS = "FX Options";
  /* package */static final String NONDELIVERABLE_FX_OPTIONS = "Non-deliverable FX Options";
  /* package */static final String FX_BARRIER_OPTIONS = "FX Barrier Options";
  /* package */static final String BONDS = "Bonds";
  /* package */static final String CASH = "Cash";
  /* package */static final String CASHFLOW = "CashFlow";
  /* package */static final String EQUITIES = "Equities";
  /* package */static final String FRAS = "FRAs";
  /* package */static final String FUTURES = "Futures";
  /* package */static final String FORWARDS = "Forwards";
  /* package */static final String EQUITY_INDEX_OPTIONS = "Equity Index Options";
  /* package */static final String EQUITY_OPTIONS = "Equity Options";
  /* package */static final String EQUITY_BARRIER_OPTIONS = "Equity Barrier Options";
  /* package */static final String EQUITY_VARIANCE_SWAPS = "Equity Variance Swaps";
  /* package */static final String SWAPTIONS = "Swaptions";
  /* package */static final String IRFUTURE_OPTIONS = "IR Future Options";
  /* package */static final String COMMODITY_FUTURE_OPTIONS = "Commodity Future Options";
  /* package */static final String FX_FUTURE_OPTIONS = "FX Future Options";
  /* package */static final String BOND_FUTURE_OPTIONS = "Bond Future Options";
  /* package */static final String EQUITY_INDEX_DIVIDEND_FUTURE_OPTIONS = "Equity Index Dividend Future Options";
  /* package */static final String SWAPS = "Swaps";
  /* package */static final String FX_FORWARDS = "FX Forwards";
  /* package */static final String FX_DIGITAL_OPTIONS = "FX Digital Options";
  /* package */static final String NONDELIVERABLE_FX_DIGITAL_OPTIONS = "Non-deliverable FX Digital Options";
  /* package */static final String NONDELIVERABLE_FX_FORWARDS = "Non-deliverable FX Forwards";
  /* package */static final String CAP_FLOOR = "Cap/Floor";
  /* package */static final String CAP_FLOOR_CMS_SPREAD = "Cap/Floor CMS Spread";
  /* package */static final String UNKNOWN = "Unknown Security Type";
  /* package */static final String NAME = "Asset Class";
  /* package */static final String CDS = "CDS"; // TODO: is this the correct abbreviation?
  /* package */static final String EQUITY_INDEX_FUTURE_OPTIONS = "Equity Index Future Options";
  /* package */static final String DELIVERABLE_SWAP_FUTURES = "Deliverable Swap Futures";
  /* package */static final String CDX = "CDS Indices";
  /* package */static final String CREDIT_DEFAULT_SWAP_OPTIONS = "CDS Options";
  /* package */static final String INFLATION_SWAPS = "Inflation Swaps";
  /* package */static final String FX_VOLATILITY_SWAPS = "FX Volatility Swaps";
  /* package */static final String EXCHANGE_TRADED_FUNDS = "Exchange-Traded Funds";
  /* package */static final String ADRS = "American Depositary Receipts";
  /* package */static final String EQUITY_WARRANTS = "Equity Warrants";
  /* package */static final String BILLS = "Bills";
  /* package */static final String FLOATING_RATE_NOTES = "Floating Rate Notes";
  /* package */static final String CASH_BALANCE = "Cash Balance";
  /* package */static final String EQUITY_TRS = "Equity Total Return Swap";
  /* package */static final String BOND_TRS = "Bond Total Return Swap";

  private final Comparator<Position> _comparator = new SimplePositionComparator();

  /* package */static final List<String> ALL_CATEGORIES = Arrays.asList(FX_OPTIONS, NONDELIVERABLE_FX_FORWARDS, FX_BARRIER_OPTIONS, FX_DIGITAL_OPTIONS,
      NONDELIVERABLE_FX_DIGITAL_OPTIONS, FX_FORWARDS, NONDELIVERABLE_FX_FORWARDS, BONDS, CASH, EQUITIES,
      FRAS, FUTURES, EQUITY_INDEX_OPTIONS, EQUITY_OPTIONS, EQUITY_BARRIER_OPTIONS,
      EQUITY_VARIANCE_SWAPS, SWAPTIONS, IRFUTURE_OPTIONS, EQUITY_INDEX_DIVIDEND_FUTURE_OPTIONS,
      SWAPS, CAP_FLOOR, CAP_FLOOR_CMS_SPREAD, EQUITY_INDEX_FUTURE_OPTIONS, INFLATION_SWAPS, FX_VOLATILITY_SWAPS,
      EXCHANGE_TRADED_FUNDS, ADRS, EQUITY_WARRANTS, FLOATING_RATE_NOTES, EQUITY_TRS, BOND_TRS, UNKNOWN);

  private final boolean _includeEmptyCategories;

  public AssetClassAggregationFunction() {
    this(false);
  }

  public AssetClassAggregationFunction(final boolean includeEmptyCategories) {
    _includeEmptyCategories = includeEmptyCategories;
  }

  /**
   * Gets all of the asset class categories in this aggregation function.
   * @return A list of the categories
   */
  protected static List<String> getAllCategories() {
    return ALL_CATEGORIES;
  }

  /**
   * Gets the include empty categories field.
   * @return The include empty categories field
   */
  protected boolean includeEmptyCategories() {
    return _includeEmptyCategories;
  }

  @Override
  public String classifyPosition(final Position position) {
    final Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new FinancialSecurityVisitor<String>() {

        @Override
        public String visitBillSecurity(final BillSecurity security) {
          return BILLS;
        }

        @Override
        public String visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          return BONDS;
        }

        @Override
        public String visitCorporateBondSecurity(final CorporateBondSecurity security) {
          return BONDS;
        }

        @Override
        public String visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          return BONDS;
        }

        @Override
        public String visitInflationBondSecurity(final InflationBondSecurity security) {
          return BONDS;
        }

        @Override
        public String visitCashBalanceSecurity(final CashBalanceSecurity security) {
          return CASH_BALANCE;
        }

        @Override
        public String visitCashSecurity(final CashSecurity security) {
          return CASH;
        }

        @Override
        public String visitCashFlowSecurity(final CashFlowSecurity security) {
          return CASHFLOW;
        }

        @Override
        public String visitEquitySecurity(final EquitySecurity security) {
          return EQUITIES;
        }

        @Override
        public String visitFRASecurity(final FRASecurity security) {
          return FRAS;
        }

        @Override
        public String visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
          return FRAS;
        }

        @Override
        public String visitSwapSecurity(final SwapSecurity security) {
          return SWAPS;
        }

        @Override
        public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return EQUITY_INDEX_OPTIONS;
        }

        @Override
        public String visitEquityOptionSecurity(final EquityOptionSecurity equityOptionSecurity) {
          return EQUITY_OPTIONS;
        }

        @Override
        public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return EQUITY_BARRIER_OPTIONS;
        }

        @Override
        public String visitFXOptionSecurity(final FXOptionSecurity fxOptionSecurity) {
          return FX_OPTIONS;
        }

        @Override
        public String visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          return NONDELIVERABLE_FX_OPTIONS;
        }

        @Override
        public String visitSwaptionSecurity(final SwaptionSecurity security) {
          return SWAPTIONS;
        }

        @Override
        public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return IRFUTURE_OPTIONS;
        }

        @Override
        public String visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity commodityFutureOptionSecurity) {
          return COMMODITY_FUTURE_OPTIONS;
        }

        @Override
        public String visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
          return FX_FUTURE_OPTIONS;
        }

        @Override
        public String visitBondFutureOptionSecurity(final BondFutureOptionSecurity bondFutureOptionSecurity) {
          return BOND_FUTURE_OPTIONS;
        }

        @Override
        public String visitEquityIndexDividendFutureOptionSecurity(
            final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
          return EQUITY_INDEX_DIVIDEND_FUTURE_OPTIONS;
        }

        @Override
        public String visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity equityIndexFutureOptionSecurity) {
          return EQUITY_INDEX_FUTURE_OPTIONS;
        }

        @Override
        public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return FX_BARRIER_OPTIONS;
        }

        @Override
        public String visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          return FX_DIGITAL_OPTIONS;
        }

        @Override
        public String visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          return NONDELIVERABLE_FX_DIGITAL_OPTIONS;
        }

        @Override
        public String visitFXForwardSecurity(final FXForwardSecurity security) {
          return FX_FORWARDS;
        }

        @Override
        public String visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return NONDELIVERABLE_FX_FORWARDS;
        }

        @Override
        public String visitCapFloorSecurity(final CapFloorSecurity security) {
          return CAP_FLOOR;
        }

        @Override
        public String visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return CAP_FLOOR_CMS_SPREAD;
        }

        @Override
        public String visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return EQUITY_VARIANCE_SWAPS;
        }

        @Override
        public String visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          throw new UnsupportedOperationException("SimpleZeroDepositSecurity should not be used in a position");
        }

        @Override
        public String visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          throw new UnsupportedOperationException("PeriodicZeroDepositSecurity should not be used in a position");
        }

        @Override
        public String visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          throw new UnsupportedOperationException("ContinuousZeroDepositSecurity should not be used in a position");
        }

        @Override
        public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitBondFutureSecurity(final BondFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitFXFutureSecurity(final FXFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitForwardSwapSecurity(final ForwardSwapSecurity security) {
          return SWAPS;
        }

        @Override
        public String visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitStockFutureSecurity(final StockFutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
          return FORWARDS;
        }

        @Override
        public String visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
          return FORWARDS;
        }

        @Override
        public String visitMetalForwardSecurity(final MetalForwardSecurity security) {
          return FORWARDS;
        }

        @Override
        public String visitCDSSecurity(final CDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
          return DELIVERABLE_SWAP_FUTURES;
        }

        @Override
        public String visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
          return CDX;
        }

        @Override
        public String visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
          return CDX;
        }

        @Override
        public String visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
          return CREDIT_DEFAULT_SWAP_OPTIONS;
        }

        @Override
        public String visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
          return INFLATION_SWAPS;
        }

        @Override
        public String visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
          return INFLATION_SWAPS;
        }

        @Override
        public String visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
          return SWAPS;
        }

        @Override
        public String visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
          return FX_VOLATILITY_SWAPS;
        }

        @Override
        public String visitExchangeTradedFundSecurity(final ExchangeTradedFundSecurity security) {
          return EXCHANGE_TRADED_FUNDS;
        }

        @Override
        public String visitAmericanDepositaryReceiptSecurity(final AmericanDepositaryReceiptSecurity security) {
          return ADRS;
        }

        @Override
        public String visitEquityWarrantSecurity(final EquityWarrantSecurity security) {
          return EQUITY_WARRANTS;
        }

        @Override
        public String visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
          return FLOATING_RATE_NOTES;
        }

        @Override
        public String visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
          return EQUITY_TRS;
        }

        @Override
        public String visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
          return BOND_TRS;
        }

        @Override
        public String visitStandardCDSSecurity(final StandardCDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitLegacyCDSSecurity(final LegacyCDSSecurity security) {
          return CDS;
        }

        @Override
        public String visitIndexCDSSecurity(final IndexCDSSecurity security) {
          return CDX;
        }

        @Override
        public String visitIndexCDSDefinitionSecurity(final IndexCDSDefinitionSecurity security) {
          return CDX;
        }
      });
    } else {
      if (security instanceof RawSecurity && security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
        if (security.getAttributes().containsKey("Security Type")) {
          return security.getAttributes().get("Security Type");
        }
      }
      return UNKNOWN;
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    if (_includeEmptyCategories) {
      return ALL_CATEGORIES;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public int compare(final String assetClass1, final String assetClass2) {
    if (!ALL_CATEGORIES.contains(assetClass1)) {
      if (!ALL_CATEGORIES.contains(assetClass2)) {
        return assetClass1.compareTo(assetClass2);
      } else {
        return -1;
      }
    } else if (!ALL_CATEGORIES.contains(assetClass2)) {
      return 1;
    } else {
      return ALL_CATEGORIES.indexOf(assetClass2) - ALL_CATEGORIES.indexOf(assetClass1);
    }
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }
}
