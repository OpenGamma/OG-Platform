/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.money.Currency;

/**
 * General utility method applying to Financial Securities
 */
public class FinancialSecurityUtils {

  /**
   * 
   * @param target the computation target being examined.
   * @return ValueProperties containing a constraint of the CurrencyUnit or empty if not possible
   */
  public static ValueProperties getCurrencyConstraint(ComputationTarget target) {
    switch (target.getType()) {
      case PORTFOLIO_NODE:
        break;
      case POSITION: {
        Security security = target.getPosition().getSecurity();
        Currency ccy = getCurrency(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        }
      }
        break;
      case PRIMITIVE: {
        UniqueIdentifier uid = target.getUniqueId();
        if (uid.getScheme().equals(Currency.OBJECT_IDENTIFIER_SCHEME)) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, uid.getValue()).get();
        }
      }
        break;
      case SECURITY: {
        Security security = target.getSecurity();
        Currency ccy = getCurrency(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        }
      }
        break;
      case TRADE: {
        Security security = target.getTrade().getSecurity();
        Currency ccy = getCurrency(security);
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
   * @return a Currency, where it is possible to determine a single Currency association, null otherwise.
   */
  public static Currency getCurrency(Security security) {
    if (security instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity) security;
      Currency ccy = finSec.accept(new FinancialSecurityVisitor<Currency>() {
        @Override
        public Currency visitBondSecurity(BondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCashSecurity(CashSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquitySecurity(EquitySecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFRASecurity(FRASecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFutureSecurity(FutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitSwapSecurity(SwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return payLeg.getCurrency();
            }
          }
          return null;
        }

        @Override
        public Currency visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityOptionSecurity(EquityOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXOptionSecurity(FXOptionSecurity security) {
          return null;
        }

        @Override
        public Currency visitSwaptionSecurity(SwaptionSecurity security) {
          return null;
        }

        @Override
        public Currency visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
          return null;
        }
      });
      return ccy;
    }
    return null;
  }
}
