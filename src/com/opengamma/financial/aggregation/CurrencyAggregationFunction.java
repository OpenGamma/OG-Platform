/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.CorporateBondSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.ForwardExchangeFutureSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.MunicipalBondSecurity;
import com.opengamma.financial.security.PoweredEquityOptionSecurity;
import com.opengamma.financial.security.VanillaFutureSecurity;

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
        public Currency visitForwardExchangeFutureSecurity(
            ForwardExchangeFutureSecurity security) {
          return null; // TODO this is probably wrong
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
        public Currency visitVanillaFutureSecurity(
            VanillaFutureSecurity security) {
          return null; // TODO this is probably wrong
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
