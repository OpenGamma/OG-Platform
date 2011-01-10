/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Function to classify positions by asset class.  Note that this bins all types of options together.
 * For more detailed subdivision, see DetailedAssetClassAggregationFunction.
 * @author jim
 */
public class AssetClassAggregationFunction implements AggregationFunction<String> {
  /* package */ static final String BONDS = "Bonds";
  /* package */ static final String CASH = "Cash";
  /* package */ static final String EQUITIES = "Equities";
  /* package */static final String FRAS = "FRAs";
  /* package */ static final String FUTURES = "Futures";
  /* package */ static final String OPTIONS = "Options";
  /* package */ static final String SWAPS = "Swaps";
  /* package */ static final String UNKNOWN = "Unknown Security Type";
  /* package */ static final String NAME = "Asset Class";

  @Override
  public String classifyPosition(Position position) {
    Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new FinancialSecurityVisitor<String>() {

        @Override
        public String visitBondSecurity(BondSecurity security) {
          return BONDS;
        }

        @Override
        public String visitCashSecurity(CashSecurity security) {
          return CASH;
        }

        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          return EQUITIES;
        }

        @Override
        public String visitFRASecurity(FRASecurity security) {
          return FRAS;
        }

        @Override
        public String visitFutureSecurity(FutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitOptionSecurity(OptionSecurity security) {
          return OPTIONS;
        }

        @Override
        public String visitSwapSecurity(SwapSecurity security) {
          return SWAPS;
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
