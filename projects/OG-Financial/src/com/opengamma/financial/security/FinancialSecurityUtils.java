/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueIdentifier;

/**
 * General utility method applying to Financial Securities
 */
public class FinancialSecurityUtils {
  
  /**
   * 
   * @param target the computation target being examined.
   * @return ValueProperties containing a constraint of the CurrencyUnit or empty if not possible
   */
  public static ValueProperties getCurrencyUnitConstraint(ComputationTarget target) {
    switch (target.getType()) {
      case PORTFOLIO_NODE:
        break;
      case POSITION: 
      {
        Security security = target.getPosition().getSecurity();
        CurrencyUnit ccy = getCurrencyUnit(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        }
      }
        break;
      case PRIMITIVE:
      {
        UniqueIdentifier uid = target.getUniqueId();
        if (uid.getScheme().equals(CurrencyUnit.OBJECT_IDENTIFIER_SCHEME)) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, uid.getValue()).get();
        }
      }
        break;
      case SECURITY: 
      {
        Security security = target.getSecurity();
        CurrencyUnit ccy = getCurrencyUnit(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        }
      } 
        break;
      case TRADE:
      {
        Security security = target.getTrade().getSecurity();
        CurrencyUnit ccy = getCurrencyUnit(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        }
      } 
        break;
    }
    return ValueProperties.none();
  }
  
  /**
   * @param security the security to be examined.
   * @return a CurrencyUnit, where it is possible to determine a single CurrencyUnit association, null otherwise.
   */
  public static CurrencyUnit getCurrencyUnit(Security security) {
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity) security;
      CurrencyUnit ccy = finSec.accept(new FinancialSecurityVisitor<CurrencyUnit>() {
        @Override
        public CurrencyUnit visitBondSecurity(BondSecurity security) {
          return security.getCurrency();
        }
  
        @Override
        public CurrencyUnit visitCashSecurity(CashSecurity security) {
          return security.getCurrency();
        }
  
        @Override
        public CurrencyUnit visitEquitySecurity(EquitySecurity security) {
          return security.getCurrency();
        }
  
        @Override
        public CurrencyUnit visitFRASecurity(FRASecurity security) {
          return security.getCurrency();
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
      return ccy;
    }
    return null;
  }
}
