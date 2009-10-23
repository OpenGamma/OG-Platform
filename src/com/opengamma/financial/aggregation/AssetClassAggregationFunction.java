/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.view.FullyPopulatedPosition;
import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.PoweredEquityOptionSecurity;

/**
 * 
 * Function to classify positions by asset class.  Note that this bins all types of options together.
 * For more detailed subdivision, see DetailedAssetClassAggregationFunction.
 * @author jim
 */
public class AssetClassAggregationFunction implements AggregationFunction<String> {
  /*package*/ static final String EQUITIES = "Equities";
  /*package*/ static final String EQUITY_OPTIONS = "Equity Options";
  /*package*/ static final String UNKNOWN = "Unknown Security Type";
  /*package*/ static final String NAME = "Asset Class";
  
  @Override
  public String classifyPosition(FullyPopulatedPosition position) {
    Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity)security;
      return finSec.accept(new FinancialSecurityVisitor<String>() {
        
        public String visitEquityOption(EquityOptionSecurity security) {
          return EQUITY_OPTIONS;
        }
        @Override
        public String visitAmericanVanillaEquityOptionSecurity(
            AmericanVanillaEquityOptionSecurity security) {
          return visitEquityOption(security);
        }

        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          return EQUITIES;
        }

        @Override
        public String visitEuropeanVanillaEquityOptionSecurity(
            EuropeanVanillaEquityOptionSecurity security) {
          return visitEquityOption(security);
        }

        @Override
        public String visitPoweredEquityOptionSecurity(
            PoweredEquityOptionSecurity security) {
          return visitEquityOption(security);
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
