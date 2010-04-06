/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.AgricultureFutureSecurity;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.CorporateBondSecurity;
import com.opengamma.financial.security.EnergyFutureSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.IndexFutureSecurity;
import com.opengamma.financial.security.InterestRateFutureSecurity;
import com.opengamma.financial.security.MetalFutureSecurity;
import com.opengamma.financial.security.MunicipalBondSecurity;
import com.opengamma.financial.security.StockFutureSecurity;
import com.opengamma.financial.security.option.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.AmericanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.PoweredEquityOptionSecurity;

/**
 * 
 *
 * @author jim
 */
public class DetailedAssetClassAggregationFunction implements AggregationFunction<String> {
  /*package*/ static final String POWERED_EQUITY_OPTION_SECURITY = "Powered Equity Options";
  /*package*/ static final String EUROPEAN_VANILLA_EQUITY_OPTIONS = "European Vanilla Equity Options";
  /*package*/ static final String EQUITIES = "Equities";
  /*package*/ static final String AMERICAN_VANILLA_EQUITY_OPTIONS = "American Vanilla Equity Options";
  /*package*/ static final String GOVERNMENT_BONDS = "Government Bonds";
  /*package*/ static final String MUNICIPAL_BONDS = "Municipal Bonds";
  /*package*/ static final String CORPORATE_BONDS = "Corporate Bonds";
  /*package*/ static final String BOND_FUTURES = "Bond Futures";
  /*package*/ static final String CURRENCY_FUTURES = "Currency Futures";
  /*package*/ static final String INTEREST_RATE_FUTURES = "Interest Rate Futures";
  /*package*/ static final String UNKNOWN = "Unknown Security Type";
  /*package*/ static final String NAME = "Detailed Asset Class";
  /*package*/ static final String AGRICULTURAL_FUTURES = "Agriculture Futures";
  /*package*/ static final String METAL_FUTURES = "Metal Futures";
  /*package*/ static final String ENERGY_FUTURES = "Energy Futures";
  /*package*/ static final String INDEX_FUTURES = "Index Futures";
  /*package*/ static final String STOCK_FUTURES = "Stock Futures";
  /*package*/ static final String AMERICAN_VANILLA_FUTURE_OPTIONS = "American Vanilla Future Options";
  /*package*/ static final String EUROPEAN_VANILLA_FUTURE_OPTIONS = "European Vanilla Future Options";
  /*package*/ static final String FX_OPTIONS = "FX Options";
  
  @Override
  public String classifyPosition(Position position) {
    Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity)security;
      return finSec.accept(new FinancialSecurityVisitor<String>() {
        @Override
        public String visitAmericanVanillaEquityOptionSecurity(
            AmericanVanillaEquityOptionSecurity security) {
          return AMERICAN_VANILLA_EQUITY_OPTIONS;
        }

        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          return EQUITIES;
        }

        @Override
        public String visitEuropeanVanillaEquityOptionSecurity(
            EuropeanVanillaEquityOptionSecurity security) {
          return EUROPEAN_VANILLA_EQUITY_OPTIONS;
        }

        @Override
        public String visitPoweredEquityOptionSecurity(
            PoweredEquityOptionSecurity security) {
          return POWERED_EQUITY_OPTION_SECURITY;
        }

        @Override
        public String visitBondFutureSecurity(BondFutureSecurity security) {
          return BOND_FUTURES;
        }

        @Override
        public String visitCorporateBondSecurity(CorporateBondSecurity security) {
          return CORPORATE_BONDS;
        }

        @Override
        public String visitFXFutureSecurity(FXFutureSecurity security) {
          return CURRENCY_FUTURES;
        }

        @Override
        public String visitGovernmentBondSecurity(
            GovernmentBondSecurity security) {
          return GOVERNMENT_BONDS;
        }

        @Override
        public String visitMunicipalBondSecurity(MunicipalBondSecurity security) {
          return MUNICIPAL_BONDS;
        }

        @Override
        public String visitInterestRateFutureSecurity(
            InterestRateFutureSecurity security) {
          return INTEREST_RATE_FUTURES;
        }

        @Override
        public String visitAgricultureFutureSecurity(
            AgricultureFutureSecurity security) {
          return AGRICULTURAL_FUTURES;
        }

        @Override
        public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return ENERGY_FUTURES;
        }

        @Override
        public String visitMetalFutureSecurity(MetalFutureSecurity security) {
          return METAL_FUTURES;
        }

        @Override
        public String visitIndexFutureSecurity(IndexFutureSecurity security) {
          return INDEX_FUTURES;
        }

        @Override
        public String visitStockFutureSecurity(StockFutureSecurity security) {
          return STOCK_FUTURES;
        }

        @Override
        public String visitAmericanVanillaFutureOptionSecurity(
            AmericanVanillaFutureOptionSecurity security) {
          return AMERICAN_VANILLA_FUTURE_OPTIONS;
        }

        @Override
        public String visitEuropeanVanillaFutureOptionSecurity(
            EuropeanVanillaFutureOptionSecurity security) {
          return EUROPEAN_VANILLA_FUTURE_OPTIONS;
        }

        @Override
        public String visitFXOptionSecurity(FXOptionSecurity security) {
          return FX_OPTIONS;
        }
      });
    } else {
      return UNKNOWN;
    }
  }
  
  public String getName() {
    return NAME;
  }

}
