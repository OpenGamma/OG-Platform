/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
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
import com.opengamma.financial.security.option.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.PoweredEquityOptionSecurity;

/**
 * Function to classify positions by Currency.
 *
 * @author jim
 */
public class CurrencyAggregationFunction implements AggregationFunction<Currency> {
  public static final String NAME="Currency";
  @Override
  public Currency classifyPosition(Position position) {
    Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity)security;
      return finSec.accept(new FinancialSecurityVisitor<Currency>() {

        @Override
        public Currency visitAmericanVanillaEquityOptionSecurity(
            AmericanVanillaEquityOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquitySecurity(EquitySecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEuropeanVanillaEquityOptionSecurity(
            EuropeanVanillaEquityOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitPoweredEquityOptionSecurity(
            PoweredEquityOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitBondFutureSecurity(BondFutureSecurity security) {
          return null; // TODO this is probably wrong
        }

        @Override
        public Currency visitCorporateBondSecurity(
            CorporateBondSecurity security) {
          return security.getCurrency ();
        }

        @Override
        public Currency visitFXFutureSecurity(
            FXFutureSecurity security) {
          return security.getNumerator (); // TODO check this is correct
        }

        @Override
        public Currency visitGovernmentBondSecurity(
            GovernmentBondSecurity security) {
          return security.getCurrency ();
        }

        @Override
        public Currency visitMunicipalBondSecurity(
            MunicipalBondSecurity security) {
          return security.getCurrency ();
        }

        @Override
        public Currency visitInterestRateFutureSecurity(
            InterestRateFutureSecurity security) {
          return security.getCurrency ();
        }

        @Override
        public Currency visitAgricultureFutureSecurity(
            AgricultureFutureSecurity security) {
          return null; // TODO this is probably wrong
        }

        @Override
        public Currency visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return null; // TODO this is probably wrong
        }

        @Override
        public Currency visitMetalFutureSecurity(MetalFutureSecurity security) {
          return null; // TODO this is probably wrong
        }

        @Override
        public Currency visitIndexFutureSecurity(IndexFutureSecurity security) {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public Currency visitStockFutureSecurity(StockFutureSecurity security) {
          // TODO Auto-generated method stub
          return null;
        }  
      });
    } else {
      return null;
    }
  }

  public String getName() {
    return NAME;
  }
}
