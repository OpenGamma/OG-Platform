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

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;

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
    bean.setRegion(externalIdToExternalIdBean(security.getRegionId()));
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setEndDate(dateTimeWithZoneToZonedDateTimeBean(security.getEndDate()));
    bean.setRate(security.getRate());
    bean.setAmount(security.getAmount());
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    bean.setFixingDate(dateTimeWithZoneToZonedDateTimeBean(security.getFixingDate()));
    return bean;
  }

  @Override
  public FRASecurity createSecurity(final OperationContext context, FRASecurityBean bean) {
    Currency currency = currencyBeanToCurrency(bean.getCurrency());
    ExternalId regionId = externalIdBeanToExternalId(bean.getRegion());
    ZonedDateTime startDate = zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate());
    ZonedDateTime endDate = zonedDateTimeBeanToDateTimeWithZone(bean.getEndDate());
    double rate = bean.getRate();
    double amount = bean.getAmount();
    ExternalId underlyingId = externalIdBeanToExternalId(bean.getUnderlying());
    ZonedDateTime fixingDate = zonedDateTimeBeanToDateTimeWithZone(bean.getFixingDate());
    return new FRASecurity(currency, regionId, startDate, endDate, rate, amount, underlyingId, fixingDate);
  }

}
