/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorSameMethodAdapter;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Currency exposure function that returns the currency for a given trade.
 */
public class CurrencyExposureFunction implements ExposureFunction {
  
  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Currency";
  
  private final CurrencyVisitor _currencyVisitor;

  /**
   * Constructor that uses a security source to lookup the underlying currency, if necessary.
   * @param securitySource the source containing security definitions.
   */
  public CurrencyExposureFunction(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _currencyVisitor = new CurrencyVisitor(securitySource);
  }

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public List<ExternalId> getIds(Trade trade) {
    Security security = trade.getSecurity();
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(_currencyVisitor);
    }
    return null;
  }
  
  private static final class DefaultCurrencyVisitor implements FinancialSecurityVisitorSameMethodAdapter.Visitor<List<ExternalId>> {
    
    private final SecuritySource _securitySource;
    
    public DefaultCurrencyVisitor(SecuritySource securitySource) {
      _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    }
    
    @Override
    public List<ExternalId> visit(FinancialSecurity security) {
      final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(security, _securitySource);
      if (currencies.isEmpty()) {
        return null;
      }
      final List<ExternalId> result = new ArrayList<>();
      for (final Currency currency : currencies) {
        result.add(ExternalId.of(Currency.OBJECT_SCHEME, currency.getCode()));
      }
      return result;
    }
  }
  
  private static final class CurrencyVisitor extends FinancialSecurityVisitorSameMethodAdapter<List<ExternalId>> {
    
    private final SecuritySource _securitySource;
    
    public CurrencyVisitor(SecuritySource securitySource) {
      super(new DefaultCurrencyVisitor(securitySource));
      _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    }
  
    @Override
    public List<ExternalId> visitFXFutureSecurity(final FXFutureSecurity security) {
      return Arrays.asList(ExternalId.of(Currency.OBJECT_SCHEME, security.getDenominator().getCode()), ExternalId.of(Currency.OBJECT_SCHEME, security.getNumerator().getCode()));
    }
  
    @Override
    public List<ExternalId> visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
      final FXFutureSecurity fxFuture = (FXFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      return Arrays.asList(ExternalId.of(Currency.OBJECT_SCHEME, fxFuture.getDenominator().getCode()), ExternalId.of(Currency.OBJECT_SCHEME, fxFuture.getNumerator().getCode()));
    }
    
  }
}
