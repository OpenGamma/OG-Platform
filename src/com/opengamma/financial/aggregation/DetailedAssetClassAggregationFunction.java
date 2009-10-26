/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.PoweredEquityOptionSecurity;

/**
 * 
 *
 * @author jim
 */
public class DetailedAssetClassAggregationFunction implements AggregationFunction<String> {
  /*package*/ static final String POWERED_EQUITY_OPTION_SECURITY = "Powered Equity Option";
  /*package*/ static final String EUROPEAN_VANILLA_EQUITY_OPTIONS = "European Vanilla Equity Options";
  /*package*/ static final String EQUITIES = "Equities";
  /*package*/ static final String AMERICAN_VANILLA_EQUITY_OPTIONS = "American Vanilla Equity Options";
  /*package*/ static final String UNKNOWN = "Unknown Security Type";
  /*package*/ static final String NAME = "Detailed Asset Class";
  
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
      });
    } else {
      return UNKNOWN;
    }
  }
  
  public String getName() {
    return NAME;
  }

}
