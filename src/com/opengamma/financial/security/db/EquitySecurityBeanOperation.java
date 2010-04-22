/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import static com.opengamma.financial.security.db.Converters.currencyBeanToCurrency;
import static com.opengamma.financial.security.db.Converters.gicsCodeBeanToGICSCode;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.id.Identifier;

/* package */ class EquitySecurityBeanOperation extends AbstractBeanOperation<EquitySecurity, EquitySecurityBean> {
  
  public static final EquitySecurityBeanOperation INSTANCE = new EquitySecurityBeanOperation ();
  
  private EquitySecurityBeanOperation () {
    super ("EQUITY", EquitySecurity.class, EquitySecurityBean.class);
  }

  @Override
  public EquitySecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final EquitySecurity security) {
    GICSCodeBean gicsCodeBean = null;
    if (security.getGICSCode () != null) {
      gicsCodeBean = secMasterSession.getOrCreateGICSCodeBean (security.getGICSCode ().toString (), "");
    }
    return createBean (secMasterSession.getOrCreateExchangeBean (security.getExchangeCode(), security.getExchange()), security.getCompanyName (), secMasterSession.getOrCreateCurrencyBean (security.getCurrency ().getISOCode ()), gicsCodeBean);
  }
  
  /* package */ EquitySecurityBean createBean (
      final ExchangeBean exchange,
      final String companyName,
      final CurrencyBean currency,
      final GICSCodeBean gicsCode) {
    final EquitySecurityBean equity = new EquitySecurityBean ();
    equity.setExchange (exchange);
    equity.setCompanyName (companyName);
    equity.setCurrency (currency);
    equity.setGICSCode (gicsCode);
    return equity;
  }
  
  /* package */ EquitySecurityBean createBean (
      final HibernateSecurityMasterSession secMasterSession,
      final Date effectiveDateTime,
      final boolean deleted,
      final Date lastModified,
      final String modifiedBy,
      final EquitySecurityBean firstVersion,
      final String displayName,
      final ExchangeBean exchange,
      final String companyName,
      final CurrencyBean currency,
      final GICSCodeBean gicsCode) {
    final EquitySecurityBean equity = createBean (exchange, companyName, currency, gicsCode);
    secMasterSession.persistSecurityBean (effectiveDateTime, deleted, lastModified, modifiedBy, firstVersion, displayName, equity);
    return equity;
  }

  @Override
  public EquitySecurity createSecurity(final Identifier identifier, final EquitySecurityBean bean) {
    EquitySecurity result = new EquitySecurity();
    result.setCompanyName(bean.getCompanyName());
    result.setCurrency(currencyBeanToCurrency(bean.getCurrency()));
    result.setExchange(bean.getExchange().getDescription());
    result.setExchangeCode(bean.getExchange().getName());
    result.setTicker(identifier.getValue());
    result.setIdentityKey(identifier.getValue());
    result.setGICSCode(gicsCodeBeanToGICSCode (bean.getGICSCode ()));
    return result;
  }

  @Override
  public boolean beanEquals(EquitySecurityBean bean, EquitySecurity security) {
    return ObjectUtils.equals(bean.getCompanyName(), security.getCompanyName()) &&
        ObjectUtils.equals(currencyBeanToCurrency(bean.getCurrency()), security.getCurrency()) &&
        ObjectUtils.equals(bean.getExchange().getName (), security.getExchange()) &&
        ObjectUtils.equals(gicsCodeBeanToGICSCode (bean.getGICSCode ()), security.getGICSCode ());
  }

}