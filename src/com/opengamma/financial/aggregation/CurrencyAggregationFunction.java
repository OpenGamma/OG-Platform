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
import com.opengamma.financial.security.option.AmericanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.PoweredEquityOptionSecurity;

/**
 * Function to classify positions by Currency.
 *
 */
public class CurrencyAggregationFunction implements AggregationFunction<Currency> {
  private static final String NAME = "Currency";
  
  @Override
  public Currency classifyPosition(Position position) {
    Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity) security;
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
          return security.getCurrency();
        }

        @Override
        public Currency visitCorporateBondSecurity(
            CorporateBondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXFutureSecurity(
            FXFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitGovernmentBondSecurity(
            GovernmentBondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitMunicipalBondSecurity(
            MunicipalBondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitInterestRateFutureSecurity(
            InterestRateFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitAgricultureFutureSecurity(
            AgricultureFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEnergyFutureSecurity(EnergyFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitMetalFutureSecurity(MetalFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitIndexFutureSecurity(IndexFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitStockFutureSecurity(StockFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitAmericanVanillaFutureOptionSecurity(
            AmericanVanillaFutureOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEuropeanVanillaFutureOptionSecurity(
            EuropeanVanillaFutureOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXOptionSecurity(FXOptionSecurity security) {
          return security.getCurrency();
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
