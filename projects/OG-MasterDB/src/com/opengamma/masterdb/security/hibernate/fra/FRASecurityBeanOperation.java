/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fra;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Bean/security conversion operations.
 */
public final class FRASecurityBeanOperation extends AbstractSecurityBeanOperation<FRASecurity, FRASecurityBean> {

  /**
   * Singleton instance.
   */
  public static final FRASecurityBeanOperation INSTANCE = new FRASecurityBeanOperation();

  private FRASecurityBeanOperation() {
    super("FRA", FRASecurity.class, FRASecurityBean.class);
  }

  @Override
  public FRASecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, FRASecurity security) {
    final FRASecurityBean bean = new FRASecurityBean();
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setRegion(externalIdToExternalIdBean(security.getRegion()));
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setEndDate(dateTimeWithZoneToZonedDateTimeBean(security.getEndDate()));
    bean.setRate(security.getRate());
    bean.setAmount(security.getAmount());
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingIdentifier()));
    return bean;
  }

  @Override
  public FRASecurity createSecurity(final OperationContext context, FRASecurityBean bean) {
    return new FRASecurity(currencyBeanToCurrency(bean.getCurrency()), externalIdBeanToExternalId(bean.getRegion()), 
                           zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate()), zonedDateTimeBeanToDateTimeWithZone(bean.getEndDate()), 
                           bean.getRate(), bean.getAmount(), externalIdBeanToExternalId(bean.getUnderlying()));
  }

}
