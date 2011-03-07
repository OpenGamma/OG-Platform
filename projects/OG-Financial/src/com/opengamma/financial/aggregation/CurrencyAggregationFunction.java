/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.common.CurrencyUnit;
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
 * Function to classify positions by Currency.
 *
 */
public class CurrencyAggregationFunction implements AggregationFunction<CurrencyUnit> {
  private static final String NAME = "Currency";
  
  @Override
  public CurrencyUnit classifyPosition(Position position) {
    Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new FinancialSecurityVisitor<CurrencyUnit>() {

        @Override
        public CurrencyUnit visitBondSecurity(BondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public CurrencyUnit visitCashSecurity(CashSecurity security) {
          return null;
        }

        @Override
        public CurrencyUnit visitEquitySecurity(EquitySecurity security) {
          return security.getCurrency();
        }

        @Override
        public CurrencyUnit visitFRASecurity(FRASecurity security) {
          return null;
        }

        @Override
        public CurrencyUnit visitFutureSecurity(FutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public CurrencyUnit visitOptionSecurity(OptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public CurrencyUnit visitSwapSecurity(SwapSecurity security) {
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
