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
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurityVisitor;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurityVisitor;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cash.CashSecurityVisitor;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquitySecurityVisitor;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurityVisitor;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.FRASecurityVisitor;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurityVisitor;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.fx.FXSecurityVisitor;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurityVisitor;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurityVisitor;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurityVisitor;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurityVisitor;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurityVisitor;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurityVisitor;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurityVisitor;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurityVisitor;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurityVisitor;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;

/**
 * 
 */
public class DetailedAssetClassAggregationFunction implements AggregationFunction<String> {
  
  private static final String NAME = "Asset Class - Detailed";
  
  private Comparator<Position> _comparator = new SimplePositionComparator();
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
  private static final String FX_OPTIONS = "FX Options";
  private static final String NONDELIVERABLE_FX_OPTIONS = "Non-deliverable FX Options";
  private static final String FX_BARRIER_OPTIONS = "FX Barrier Options";
  private static final String SWAPTIONS = "Swaptions";
  private static final String CASH = "Cash";
  private static final String FRAS = "FRAs";
  private static final String SWAPS = "Swaps";
  private static final String FORWARD_SWAPS = "Forward Swaps";
  private static final String EQUITY_INDEX_OPTIONS = "Equity Index Options";
  private static final String FX = "FX";
  private static final String FX_FORWARDS = "FX forwards";
  private static final String NONDELIVERABLE_FX_FORWARDS = "Non-deliverable FX forwards";
  private static final String CAP_FLOOR = "Cap/Floor";
  private static final String CAP_FLOOR_CMS_SPREAD = "Cap/Floor CMS Spread";

  @Override
  public String classifyPosition(final Position position) {
    final Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new FinancialSecurityVisitorAdapter<String>(new BondSecurityVisitor<String>() {
        @Override
        public String visitCorporateBondSecurity(final CorporateBondSecurity security) {
          return CORPORATE_BONDS;
        }

        @Override
        public String visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          return GOVERNMENT_BONDS;
        }

        @Override
        public String visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          return MUNICIPAL_BONDS;
        }
      }, new CashSecurityVisitor<String>() {

        @Override
        public String visitCashSecurity(final CashSecurity security) {
          return CASH;
        }

      }, new EquitySecurityVisitor<String>() {

        @Override
        public String visitEquitySecurity(final EquitySecurity security) {
          return EQUITIES;
        }
      }, new FRASecurityVisitor<String>() {

        @Override
        public String visitFRASecurity(final FRASecurity security) {
          return FRAS;
        }
      }, new FutureSecurityVisitor<String>() {

        @Override
        public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
          return AGRICULTURAL_FUTURES;
        }

        @Override
        public String visitBondFutureSecurity(final BondFutureSecurity security) {
          return BOND_FUTURES;
        }

        @Override
        public String visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
          return ENERGY_FUTURES;
        }

        @Override
        public String visitFXFutureSecurity(final FXFutureSecurity security) {
          return CURRENCY_FUTURES;
        }

        @Override
        public String visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return INDEX_FUTURES;
        }

        @Override
        public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return INTEREST_RATE_FUTURES;
        }

        @Override
        public String visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return METAL_FUTURES;
        }

        @Override
        public String visitStockFutureSecurity(final StockFutureSecurity security) {
          return STOCK_FUTURES;
        }

        @Override
        public String visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return EQUITY_FUTURES;
        }

        @Override
        public String visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return EQUITY_INDEX_DIVIDEND_FUTURES;
        }
      }, new SwapSecurityVisitor<String>() {

        @Override
        public String visitForwardSwapSecurity(final ForwardSwapSecurity security) {
          return FORWARD_SWAPS;
        }

        @Override
        public String visitSwapSecurity(final SwapSecurity security) {
          return SWAPS;
        }
      }, new EquityIndexOptionSecurityVisitor<String>() {

        @Override
        public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return EQUITY_INDEX_OPTIONS;
        }
      }, new EquityOptionSecurityVisitor<String>() {

        @Override
        public String visitEquityOptionSecurity(final EquityOptionSecurity equityOptionSecurity) {
          return EQUITY_OPTIONS;
        }
      }, new EquityBarrierOptionSecurityVisitor<String>() {

        @Override
        public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity equityOptionSecurity) {
          return EQUITY_BARRIER_OPTIONS;
        }
      }, new FXOptionSecurityVisitor<String>() {

        @Override
        public String visitFXOptionSecurity(final FXOptionSecurity fxOptionSecurity) {
          return FX_OPTIONS;
        }
      }, new NonDeliverableFXOptionSecurityVisitor<String>() {

        @Override
        public String visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity fxOptionSecurity) {
          return NONDELIVERABLE_FX_OPTIONS;
        }
      }, new SwaptionSecurityVisitor<String>() {

        @Override
        public String visitSwaptionSecurity(final SwaptionSecurity swaptionSecurity) {
          return SWAPTIONS;
        }
      }, new IRFutureOptionSecurityVisitor<String>() {

        @Override
        public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity irFutureOptionSecurity) {
          return IRFUTURE_OPTIONS;
        }
      }, new FXBarrierOptionSecurityVisitor<String>() {

        @Override
        public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return FX_BARRIER_OPTIONS;
        }
      }, new FXSecurityVisitor<String>() {

        @Override
        public String visitFXSecurity(final FXSecurity security) {
          return FX;
        }

      }, new FXForwardSecurityVisitor<String>() {

        @Override
        public String visitFXForwardSecurity(final FXForwardSecurity security) {
          return FX_FORWARDS;
        }

      }, new NonDeliverableFXForwardSecurityVisitor<String>() {

        @Override
        public String visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return NONDELIVERABLE_FX_FORWARDS;
        }

      }, new CapFloorSecurityVisitor<String>() {

        @Override
        public String visitCapFloorSecurity(final CapFloorSecurity security) {
          return CAP_FLOOR;
        }
      }, new CapFloorCMSSpreadSecurityVisitor<String>() {

        @Override
        public String visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return CAP_FLOOR_CMS_SPREAD;
        }

      }, new EquityVarianceSwapSecurityVisitor<String>() {

        @Override
        public String visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return EQUITY_VARIANCE_SWAPS;
        }

      }

      ));
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
  public int compare(String detailedAssetClass1, String detailedAssetClass2) {
    return detailedAssetClass1.compareTo(detailedAssetClass2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }

}
