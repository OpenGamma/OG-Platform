/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.master.db.hibernate.cash;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.master.db.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.financial.security.master.db.hibernate.CurrencyBean;
import com.opengamma.financial.security.master.db.hibernate.HibernateSecurityMasterDao;
import com.opengamma.financial.security.master.db.hibernate.IdentifierBean;
import com.opengamma.financial.security.master.db.hibernate.OperationContext;

import static com.opengamma.financial.security.master.db.hibernate.Converters.*;

/**
 * Bean/security conversion operations.
 */
public final class CashSecurityBeanOperation extends AbstractSecurityBeanOperation<CashSecurity, CashSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final CashSecurityBeanOperation INSTANCE = new CashSecurityBeanOperation();

  private CashSecurityBeanOperation() {
    super("CASH", CashSecurity.class, CashSecurityBean.class);
  }

  @Override
  public boolean beanEquals(final OperationContext context, CashSecurityBean bean, CashSecurity security) {
    return true;
  }

  @Override
  public CashSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, CashSecurity security) {
    CurrencyBean currencyBean = secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getISOCode());
    IdentifierBean regionIdentifier = identifierToIdentifierBean(security.getRegion());
    final CashSecurityBean bean = new CashSecurityBean();
    bean.setCurrency(currencyBean);
    bean.setRegion(regionIdentifier);
    return bean;
  }

  @Override
  public CashSecurity createSecurity(final OperationContext context, CashSecurityBean bean) {
    final CashSecurity security = new CashSecurity(currencyBeanToCurrency(bean.getCurrency()), identifierBeanToIdentifier(bean.getRegion()));
    return security;
  }

}
