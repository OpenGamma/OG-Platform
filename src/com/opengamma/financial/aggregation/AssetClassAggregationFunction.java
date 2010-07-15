/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.AmericanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.PoweredEquityOptionSecurity;

/**
 * Function to classify positions by asset class.  Note that this bins all types of options together.
 * For more detailed subdivision, see DetailedAssetClassAggregationFunction.
 * @author jim
 */
public class AssetClassAggregationFunction implements AggregationFunction<String> {
  /* package */ static final String BONDS = "Bonds";
  /* package */ static final String FUTURES = "Futures";
  /*package*/ static final String EQUITIES = "Equities";
  /*package*/ static final String OPTIONS = "Options";
  /*package*/ static final String UNKNOWN = "Unknown Security Type";
  /*package*/ static final String NAME = "Asset Class";
  
  @Override
  public String classifyPosition(Position position) {
    Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new FinancialSecurityVisitor<String>() {
        
        private String visitOption(OptionSecurity security) {
          return OPTIONS;
        }
        
        private String visitBond(BondSecurity security) {
          return BONDS;
        }
        
        private String visitFuture(FutureSecurity security) {
          return FUTURES;
        }
        
        @Override
        public String visitAmericanVanillaEquityOptionSecurity(
            AmericanVanillaEquityOptionSecurity security) {
          return visitOption(security);
        }

        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          return EQUITIES;
        }

        @Override
        public String visitEuropeanVanillaEquityOptionSecurity(
            EuropeanVanillaEquityOptionSecurity security) {
          return visitOption(security);
        }

        @Override
        public String visitPoweredEquityOptionSecurity(
            PoweredEquityOptionSecurity security) {
          return visitOption(security);
        }
        @Override
        public String visitBondFutureSecurity(BondFutureSecurity security) {
          return visitFuture(security);
        }
        @Override
        public String visitCorporateBondSecurity(CorporateBondSecurity security) {
          return visitBond(security);
        }
        @Override
        public String visitFXFutureSecurity(FXFutureSecurity security) {
          return visitFuture(security);
        }
        @Override
        public String visitGovernmentBondSecurity(GovernmentBondSecurity security) {
          return visitBond(security);
        }
        @Override
        public String visitMunicipalBondSecurity(MunicipalBondSecurity security) {
          return visitBond(security);
        }

        @Override
        public String visitInterestRateFutureSecurity(
            InterestRateFutureSecurity security) {
          return visitFuture(security);
        }

        @Override
        public String visitAgricultureFutureSecurity(
            AgricultureFutureSecurity security) {
          return visitFuture(security);
        }

        @Override
        public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return visitFuture(security);
        }

        @Override
        public String visitMetalFutureSecurity(MetalFutureSecurity security) {
          return visitFuture(security);
        }

        @Override
        public String visitIndexFutureSecurity(IndexFutureSecurity security) {
          return visitFuture(security);
        }

        @Override
        public String visitStockFutureSecurity(StockFutureSecurity security) {
          return visitFuture(security);
        }

        @Override
        public String visitAmericanVanillaFutureOptionSecurity(
            AmericanVanillaFutureOptionSecurity security) {
          return visitOption(security);
        }

        @Override
        public String visitEuropeanVanillaFutureOptionSecurity(
            EuropeanVanillaFutureOptionSecurity security) {
          return visitOption(security);
        }

        @Override
        public String visitFXOptionSecurity(FXOptionSecurity security) {
          return visitOption(security);
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
