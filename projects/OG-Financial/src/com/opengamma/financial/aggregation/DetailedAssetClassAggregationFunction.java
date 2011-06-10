/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cash.CashSecurityVisitor;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquitySecurityVisitor;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.FRASecurityVisitor;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
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
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurityVisitor;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;

/**
 * 
 *
 * @author jim
 */
public class DetailedAssetClassAggregationFunction implements AggregationFunction<String> {
  /* package */static final String EQUITIES = "Equities";
  /* package */static final String GOVERNMENT_BONDS = "Government Bonds";
  /* package */static final String MUNICIPAL_BONDS = "Municipal Bonds";
  /* package */static final String CORPORATE_BONDS = "Corporate Bonds";
  /* package */static final String BOND_FUTURES = "Bond Futures";
  /* package */static final String CURRENCY_FUTURES = "Currency Futures";
  /* package */static final String INTEREST_RATE_FUTURES = "Interest Rate Futures";
  /* package */static final String UNKNOWN = "Unknown Security Type";
  /* package */static final String NAME = "Detailed Asset Class";
  /* package */static final String AGRICULTURAL_FUTURES = "Agriculture Futures";
  /* package */static final String METAL_FUTURES = "Metal Futures";
  /* package */static final String ENERGY_FUTURES = "Energy Futures";
  /* package */static final String INDEX_FUTURES = "Index Futures";
  /* package */static final String STOCK_FUTURES = "Stock Futures";
  /* package */static final String EQUITY_OPTIONS = "Equity Options";
  /* package */static final String IRFUTURE_OPTIONS = "IRFuture Options";
  /* package */static final String FX_OPTIONS = "FX Options";
  /* package */static final String FX_BARRIER_OPTIONS = "FX Barrier Options";
  /* package */static final String SWAPTIONS = "Swaptions";
  /* package */static final String CASH = "Cash";
  /* package */static final String FRAS = "FRAs";
  /* package */static final String SWAPS = "Swaps";
  /* package */static final String FORWARD_SWAPS = "Forward Swaps";
  /* package */static final String EQUITY_INDEX_OPTIONS = "Equity Index Options";

  @Override
  public String classifyPosition(Position position) {
    Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new FinancialSecurityVisitorAdapter<String>(new BondSecurityVisitor<String>() {
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
      }, new CashSecurityVisitor<String>() {

        @Override
        public String visitCashSecurity(CashSecurity security) {
          return CASH;
        }

      }, new EquitySecurityVisitor<String>() {

        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          return EQUITIES;
        }
      }, new FRASecurityVisitor<String>() {

        @Override
        public String visitFRASecurity(FRASecurity security) {
          return FRAS;
        }
      }, new FutureSecurityVisitor<String>() {

        @Override
        public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
          return AGRICULTURAL_FUTURES;
        }

        @Override
        public String visitBondFutureSecurity(BondFutureSecurity security) {
          return BOND_FUTURES;
        }

        @Override
        public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return ENERGY_FUTURES;
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
        public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
          return INTEREST_RATE_FUTURES;
        }

        @Override
        public String visitMetalFutureSecurity(MetalFutureSecurity security) {
          return METAL_FUTURES;
        }

        @Override
        public String visitStockFutureSecurity(StockFutureSecurity security) {
          return STOCK_FUTURES;
        }
      }, new SwapSecurityVisitor<String>() {

        @Override
        public String visitForwardSwapSecurity(ForwardSwapSecurity security) {
          return FORWARD_SWAPS;
        }

        @Override
        public String visitSwapSecurity(SwapSecurity security) {
          return SWAPS;
        }
      }, new EquityIndexOptionSecurityVisitor<String>() {

        @Override
        public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
          return EQUITY_INDEX_OPTIONS;
        }
      }, new EquityOptionSecurityVisitor<String>() {

        @Override
        public String visitEquityOptionSecurity(EquityOptionSecurity equityOptionSecurity) {
          return EQUITY_OPTIONS;
        }
      }, new FXOptionSecurityVisitor<String>() {

        @Override
        public String visitFXOptionSecurity(FXOptionSecurity fxOptionSecurity) {
          return FX_OPTIONS;
        }
      }, new SwaptionSecurityVisitor<String>() {

        @Override
        public String visitSwaptionSecurity(SwaptionSecurity swaptionSecurity) {
          return SWAPTIONS;
        }
      }, new IRFutureOptionSecurityVisitor<String>() {

        @Override
        public String visitIRFutureOptionSecurity(IRFutureOptionSecurity irFutureOptionSecurity) {
          return IRFUTURE_OPTIONS;
        }
      }, new FXBarrierOptionSecurityVisitor<String>() {

        @Override
        public String visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
          return FX_BARRIER_OPTIONS;
        }
      }));
    } else {
      return UNKNOWN;
    }
  }

  public String getName() {
    return NAME;
  }

}
