/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fx;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierBeanToIdentifier;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierToIdentifierBean;

import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Bean/security conversion operations.
 */
public final class FXSecurityBeanOperation extends AbstractSecurityBeanOperation<FXSecurity, FXSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final FXSecurityBeanOperation INSTANCE = new FXSecurityBeanOperation();

  private FXSecurityBeanOperation() {
    super(FXSecurity.SECURITY_TYPE, FXSecurity.class, FXSecurityBean.class);
  }

  @Override
  public FXSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, FXSecurity security) {
    final FXSecurityBean bean = new FXSecurityBean();
    bean.setPayAmount(security.getPayAmount());
    bean.setPayCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPayCurrency().getCode()));
    bean.setReceiveAmount(security.getReceiveAmount());
    bean.setReceiveCurrency(secMasterSession.getOrCreateCurrencyBean(security.getReceiveCurrency().getCode()));
    bean.setRegion(identifierToIdentifierBean(security.getRegion()));
    return bean;
  }

  @Override
  public FXSecurity createSecurity(final OperationContext context, FXSecurityBean bean) {
    return new FXSecurity(currencyBeanToCurrency(bean.getPayCurrency()), 
        currencyBeanToCurrency(bean.getReceiveCurrency()), 
        bean.getPayAmount(), 
        bean.getReceiveAmount(), 
        identifierBeanToIdentifier(bean.getRegion()));
  }

}
