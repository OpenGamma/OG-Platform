/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.*;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.*;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 *
 */
public class DetailedAssetClassAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Asset Class - Detailed";

  private final Comparator<Position> _comparator = new SimplePositionComparator();
  private static final String EQUITIES = "Equities";
  private static final String GOVERNMENT_BONDS = "Government Bonds";
  private static final String MUNICIPAL_BONDS = "Municipal Bonds";
  private static final String CORPORATE_BONDS = "Corporate Bonds";
  private static final String BOND_FUTURES = "Bond Futures";
  private static final String CURRENCY_FUTURES = "Currency Futures";
  private static final String INTEREST_RATE_FUTURES = "Interest Rate Futures";
  private static final String UNKNOWN = "Unknown Security Type";
  private static final String AGRICULTURAL_FUTURES = "Agriculture Futures";
  private static final String METAL_FUTURES = "Metal Futures";
  private static final String ENERGY_FUTURES = "Energy Futures";
  private static final String INDEX_FUTURES = "Index Futures";
  private static final String STOCK_FUTURES = "Stock Futures";
  private static final String EQUITY_OPTIONS = "Equity Options";
  private static final String EQUITY_BARRIER_OPTIONS = "Equity Barrier Options";
  private static final String EQUITY_FUTURES = "Equity Futures";
  private static final String EQUITY_INDEX_DIVIDEND_FUTURES = "Equity Index Dividend Futures";
  private static final String EQUITY_VARIANCE_SWAPS = "Equity Variance Swaps";
  private static final String IRFUTURE_OPTIONS = "IRFuture Options";
  private static final String COMMODITY_FUTURE_OPTIONS = "Commodity Future Options";
  private static final String FX_OPTIONS = "FX Options";
  private static final String NONDELIVERABLE_FX_OPTIONS = "Non-deliverable FX Options";
  private static final String FX_BARRIER_OPTIONS = "FX Barrier Options";
  private static final String SWAPTIONS = "Swaptions";
  private static final String CASH = "Cash";
  private static final String FRAS = "FRAs";
  private static final String SWAPS = "Swaps";
  private static final String FORWARD_SWAPS = "Forward Swaps";
  private static final String EQUITY_INDEX_OPTIONS = "Equity Index Options";
  private static final String FX_DIGITAL_OPTIONS = "FX Digital Options";
  private static final String NONDELIVERABLE_FX_DIGITAL_OPTIONS = "Non-deliverable FX Digital Options";
  private static final String FX_FORWARDS = "FX forwards";
  private static final String NONDELIVERABLE_FX_FORWARDS = "Non-deliverable FX forwards";
  private static final String CAP_FLOOR = "Cap/Floor";
  private static final String CAP_FLOOR_CMS_SPREAD = "Cap/Floor CMS Spread";
  private static final String EQUITY_INDEX_DIVIDEND_FUTURE_OPTIONS = "Equity Index Dividend Future Options";


  @Override
  public String classifyPosition(final Position position) {
    final Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;

      return finSec.accept(new FinancialSecurityVisitorAdapter<String>() {
        @Override
        public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
          return AGRICULTURAL_FUTURES;
        }

        @Override
        public String visitCorporateBondSecurity(CorporateBondSecurity security) {
          return CORPORATE_BONDS;
        }

        @Override
        public String visitGovernmentBondSecurity(GovernmentBondSecurity security) {
          return GOVERNMENT_BONDS;
        }

        @Override
        public String visitMunicipalBondSecurity(MunicipalBondSecurity security) {
          return MUNICIPAL_BONDS;
        }

        @Override
        public String visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
          return CAP_FLOOR_CMS_SPREAD;
        }

        @Override
        public String visitCapFloorSecurity(CapFloorSecurity security) {
          return CAP_FLOOR;
        }

        @Override
        public String visitCashSecurity(CashSecurity security) {
          return CASH;
        }

        @Override
        public String visitContinuousZeroDepositSecurity(ContinuousZeroDepositSecurity security) {
          throw new UnsupportedOperationException("Should not form a position containing a ContinuousZeroDepositSecurity");
        }

        @Override
        public String visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
          return EQUITY_BARRIER_OPTIONS;
        }

        @Override
        public String visitEquityIndexDividendFutureOptionSecurity(EquityIndexDividendFutureOptionSecurity security) {
          return EQUITY_INDEX_DIVIDEND_FUTURE_OPTIONS;
        }

        @Override
        public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
          return EQUITY_INDEX_OPTIONS;
        }

        @Override
        public String visitEquityOptionSecurity(EquityOptionSecurity security) {
          return EQUITY_OPTIONS;
        }

        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          return EQUITIES;
        }

        @Override
        public String visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
          return EQUITY_VARIANCE_SWAPS;
        }

        @Override
        public String visitFRASecurity(FRASecurity security) {
          return FRAS;
        }

        @Override
        public String visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
          return FX_BARRIER_OPTIONS;
        }

        @Override
        public String visitFXDigitalOptionSecurity(FXDigitalOptionSecurity security) {
          return FX_DIGITAL_OPTIONS;
        }

        @Override
        public String visitFXForwardSecurity(FXForwardSecurity security) {
          return FX_FORWARDS;
        }

        @Override
        public String visitFXOptionSecurity(FXOptionSecurity security) {
          return FX_OPTIONS;
        }

        @Override
        public String visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
          return IRFUTURE_OPTIONS;
        }

        @Override
        public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
          return INTEREST_RATE_FUTURES;
        }

        @Override
        public String visitNonDeliverableFXDigitalOptionSecurity(NonDeliverableFXDigitalOptionSecurity security) {
          return NONDELIVERABLE_FX_DIGITAL_OPTIONS;
        }

        @Override
        public String visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
          return NONDELIVERABLE_FX_FORWARDS;
        }

        @Override
        public String visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
          return NONDELIVERABLE_FX_OPTIONS;
        }

        @Override
        public String visitPeriodicZeroDepositSecurity(PeriodicZeroDepositSecurity security) {
          throw new UnsupportedOperationException("Should not form a position containing a PeriodicZeroDepositSecurity");
        }

        @Override
        public String visitSimpleZeroDepositSecurity(SimpleZeroDepositSecurity security) {
          throw new UnsupportedOperationException("Should not form a position containing a SimpleZeroDepositSecurity");
        }

        @Override
        public String visitForwardSwapSecurity(ForwardSwapSecurity security) {
          return FORWARD_SWAPS;
        }

        @Override
        public String visitSwapSecurity(SwapSecurity security) {
          return SWAPS;
        }

        @Override
        public String visitSwaptionSecurity(SwaptionSecurity security) {
          return SWAPTIONS;
        }

        @Override
        public String visitBondFutureSecurity(BondFutureSecurity security) {
          return BOND_FUTURES;
        }

        @Override
        public String visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security) {
          return COMMODITY_FUTURE_OPTIONS;
        }

        @Override
        public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return ENERGY_FUTURES;
        }

        @Override
        public String visitEquityFutureSecurity(EquityFutureSecurity security) {
          return EQUITY_FUTURES;
        }

        @Override
        public String visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
          return EQUITY_INDEX_DIVIDEND_FUTURES;
        }

        @Override
        public String visitFXFutureSecurity(FXFutureSecurity security) {
          return CURRENCY_FUTURES;
        }

        @Override
        public String visitIndexFutureSecurity(IndexFutureSecurity security) {
          return INDEX_FUTURES;
        }

        @Override
        public String visitMetalFutureSecurity(MetalFutureSecurity security) {
          return METAL_FUTURES;
        }

        @Override
        public String visitStockFutureSecurity(StockFutureSecurity security) {
          return STOCK_FUTURES;
        }
      });
    } else {
      return UNKNOWN;
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptyList();
  }

  @Override
  public int compare(final String detailedAssetClass1, final String detailedAssetClass2) {
    return detailedAssetClass1.compareTo(detailedAssetClass2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }

}
