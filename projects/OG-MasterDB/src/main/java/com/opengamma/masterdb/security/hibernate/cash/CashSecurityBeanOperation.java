/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.cash;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.DayCountBean;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * Bean/security conversion operations.
 */
public final class CashSecurityBeanOperation extends AbstractSecurityBeanOperation<CashSecurity, CashSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final CashSecurityBeanOperation INSTANCE = new CashSecurityBeanOperation();

  private CashSecurityBeanOperation() {
    super(CashSecurity.SECURITY_TYPE, CashSecurity.class, CashSecurityBean.class);
  }

  @Override
  public CashSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, CashSecurity security) {
    CurrencyBean currencyBean = secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode());
    ExternalIdBean regionIdentifier = externalIdToExternalIdBean(security.getRegionId());
    ZonedDateTimeBean startBean = dateTimeWithZoneToZonedDateTimeBean(security.getStart());
    ZonedDateTimeBean maturityBean = dateTimeWithZoneToZonedDateTimeBean(security.getMaturity());
    DayCountBean dayCountBean = secMasterSession.getOrCreateDayCountBean(security.getDayCount().getName());
    final CashSecurityBean bean = new CashSecurityBean();
    bean.setCurrency(currencyBean);
    bean.setRegion(regionIdentifier);
    bean.setStart(startBean);
    bean.setMaturity(maturityBean);
    bean.setDayCount(dayCountBean);
    bean.setRate(security.getRate());
    bean.setAmount(security.getAmount());
    return bean;
  }

  @Override
  public CashSecurity createSecurity(final OperationContext context, CashSecurityBean bean) {
    final CashSecurity security = new CashSecurity(currencyBeanToCurrency(bean.getCurrency()), externalIdBeanToExternalId(bean.getRegion()), 
                                                   zonedDateTimeBeanToDateTimeWithZone(bean.getStart()), zonedDateTimeBeanToDateTimeWithZone(bean.getMaturity()),
                                                   dayCountBeanToDayCount(bean.getDayCount()), bean.getRate(), bean.getAmount());
    return security;
  }

}
