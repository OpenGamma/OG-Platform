/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.view.FullyPopulatedPosition;
import com.opengamma.financial.Currency;
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.PoweredEquityOptionSecurity;

/**
 * Function to classify positions by Currency.
 *
 * @author jim
 */
public class CurrencyAggregationFunction implements AggregationFunction<Currency> {
  public static final String NAME="Currency";
  @Override
  public Currency classifyPosition(FullyPopulatedPosition position) {
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
      });
    } else {
      return null;
    }
  }

  public String getName() {
    return NAME;
  }
}
