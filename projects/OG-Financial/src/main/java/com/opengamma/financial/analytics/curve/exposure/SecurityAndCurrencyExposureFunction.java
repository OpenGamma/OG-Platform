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
import com.opengamma.financial.security.CurrenciesVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorSameMethodAdapter;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Exposure function that allows the curve lookup by security type and currency of a given trade.
 */
public class SecurityAndCurrencyExposureFunction implements ExposureFunction {

  /**
   * The name of the exposure function.
   */
  public static final String NAME = "Security / Currency";
  
  private final SecurityAndCurrencyVisitor _visitor;
  
  /**
   * Constructor that uses a security source to look up the underlying contract for the security type and currency, if 
   * necessary.
   * @param securitySource the security source containing the security definitions.
   */
  public SecurityAndCurrencyExposureFunction(final SecuritySource securitySource) {
    _visitor = new SecurityAndCurrencyVisitor(ArgumentChecker.notNull(securitySource, "security source"));
  }

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public List<ExternalId> getIds(Trade trade) {
    Security security = trade.getSecurity();
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(_visitor);
    }
    return null;
  }
  
  private static final class DefaultSecurityAndCurrencyVisitor implements FinancialSecurityVisitorSameMethodAdapter.Visitor<List<ExternalId>> {
    
    private final SecuritySource _securitySource;
    
    public DefaultSecurityAndCurrencyVisitor(SecuritySource securitySource) {
      _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    }
    
    @Override
    public List<ExternalId> visit(FinancialSecurity security) {
      final Collection<Currency> currencies = CurrenciesVisitor.getCurrencies(security, _securitySource);
      if (currencies == null || currencies.isEmpty()) {
        return null;
      }
      final List<ExternalId> result = new ArrayList<>();
      final String securityType = security.getSecurityType();
      for (final Currency currency : currencies) {
        result.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + currency.getCode()));
      }
      return result;
    }
    
  }
  
  private static final class SecurityAndCurrencyVisitor extends FinancialSecurityVisitorSameMethodAdapter<List<ExternalId>> {
    
    private final SecuritySource _securitySource;
    
    public SecurityAndCurrencyVisitor(SecuritySource securitySource) {
      super(new DefaultSecurityAndCurrencyVisitor(ArgumentChecker.notNull(securitySource, "securitySource")));
      _securitySource = securitySource;
    }

    @Override
    public List<ExternalId> visitFXFutureSecurity(final FXFutureSecurity security) {
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + security.getDenominator().getCode()),
          ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + security.getNumerator().getCode()));
    }

    @Override
    public List<ExternalId> visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
      final FXFutureSecurity fxFuture = (FXFutureSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      final String securityType = security.getSecurityType();
      return Arrays.asList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + fxFuture.getDenominator().getCode()),
          ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + fxFuture.getNumerator().getCode()));
    }
  }
}
