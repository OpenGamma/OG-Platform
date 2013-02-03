/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.cashflow;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * Bean/security conversion operations.
 */
public final class CashFlowSecurityBeanOperation extends AbstractSecurityBeanOperation<CashFlowSecurity, CashFlowSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final CashFlowSecurityBeanOperation INSTANCE = new CashFlowSecurityBeanOperation();

  private CashFlowSecurityBeanOperation() {
    super(CashFlowSecurity.SECURITY_TYPE, CashFlowSecurity.class, CashFlowSecurityBean.class);
  }

  @Override
  public CashFlowSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, CashFlowSecurity security) {
    CurrencyBean currencyBean = secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode());
    ZonedDateTimeBean settlementBean = dateTimeWithZoneToZonedDateTimeBean(security.getSettlement());
    final CashFlowSecurityBean bean = new CashFlowSecurityBean();
    bean.setCurrency(currencyBean);
    bean.setSettlement(settlementBean);
    bean.setAmount(security.getAmount());
    return bean;
  }

  @Override
  public CashFlowSecurity createSecurity(final OperationContext context, CashFlowSecurityBean bean) {
    return new CashFlowSecurity(currencyBeanToCurrency(bean.getCurrency()),
                                 zonedDateTimeBeanToDateTimeWithZone(bean.getSettlement()),
                                 bean.getAmount());
  }
}
