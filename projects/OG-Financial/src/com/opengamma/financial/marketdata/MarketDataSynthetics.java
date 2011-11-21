/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import com.google.common.base.Function;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

/**
 * Synthetic property functions for the market data expression parsers.
 */
public final class MarketDataSynthetics {

  private MarketDataSynthetics() {
  }

  public static Function<Security, Currency> securityCurrency() {
    return new Function<Security, Currency>() {

      @Override
      public Currency apply(final Security security) {
        return FinancialSecurityUtils.getCurrency(security);
      }

    };
  }

  public static Function<Security, String> securityType() {
    return new Function<Security, String>() {

      @Override
      public String apply(final Security security) {
        return security.getSecurityType();
      }

    };
  }

  public static Function<ManageableSecurity, Security> securityUnderlying(final SecuritySource source) {
    return new Function<ManageableSecurity, Security>() {

      @Override
      public Security apply(final ManageableSecurity security) {
        final Object underlying = security.property("underlyingId").get();
        if (underlying == null) {
          return null;
        }
        if (underlying instanceof ExternalId) {
          return source.getSecurity(ExternalIdBundle.of((ExternalId) underlying));
        } else if (underlying instanceof UniqueId) {
          return source.getSecurity((UniqueId) underlying);
        } else if (underlying instanceof ExternalIdBundle) {
          return source.getSecurity((ExternalIdBundle) underlying);
        } else {
          throw new IllegalArgumentException("Bad underlying id - " + underlying);
        }
      }

    };
  }

}
