/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.equity;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;

import java.util.Date;

import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExchangeBean;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * 
 */
public final class EquitySecurityBeanOperation extends AbstractSecurityBeanOperation<EquitySecurity, EquitySecurityBean> {

  /**
   * Singleton.
   */
  public static final EquitySecurityBeanOperation INSTANCE = new EquitySecurityBeanOperation();

  private EquitySecurityBeanOperation() {
    super(EquitySecurity.SECURITY_TYPE, EquitySecurity.class, EquitySecurityBean.class);
  }

  public static GICSCode gicsCodeBeanToGICSCode(final GICSCodeBean gicsCodeBean) {
    if (gicsCodeBean == null) {
      return null;
    }
    return GICSCode.of(gicsCodeBean.getName());
  }

  @Override
  public EquitySecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final EquitySecurity security) {
    GICSCodeBean gicsCodeBean = null;
    if (security.getGicsCode() != null) {
      gicsCodeBean = secMasterSession.getOrCreateGICSCodeBean(security.getGicsCode().getCode(), "");
    }
    final EquitySecurityBean bean = createBean(secMasterSession.getOrCreateExchangeBean(security.getExchangeCode(), security.getExchange()), security.getCompanyName(), secMasterSession
        .getOrCreateCurrencyBean(security
        .getCurrency().getCode()), gicsCodeBean);
    bean.setShortName(security.getShortName());
    return bean;
  }

  public EquitySecurityBean createBean(final ExchangeBean exchange, final String companyName, final CurrencyBean currency, final GICSCodeBean gicsCode) {
    final EquitySecurityBean equity = new EquitySecurityBean();
    equity.setExchange(exchange);
    equity.setCompanyName(companyName);
    equity.setCurrency(currency);
    equity.setGicsCode(gicsCode);
    return equity;
  }

  public EquitySecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final Date effectiveDateTime, final boolean deleted, final Date lastModified,
      final String modifiedBy, final EquitySecurityBean firstVersion, final String displayName, final ExchangeBean exchange, final String companyName, final CurrencyBean currency,
      final GICSCodeBean gicsCode) {
    final EquitySecurityBean equity = createBean(exchange, companyName, currency, gicsCode);
    secMasterSession.persistSecurityBean(context, equity);
    return equity;
  }

  @Override
  public EquitySecurity createSecurity(final OperationContext context, final EquitySecurityBean bean) {
    final EquitySecurity security = new EquitySecurity(bean.getExchange().getDescription(), bean.getExchange().getName(), bean.getCompanyName(), currencyBeanToCurrency(bean.getCurrency()));
    security.setShortName(bean.getShortName());
    security.setGicsCode(gicsCodeBeanToGICSCode(bean.getGicsCode()));
    return security;
  }

}
