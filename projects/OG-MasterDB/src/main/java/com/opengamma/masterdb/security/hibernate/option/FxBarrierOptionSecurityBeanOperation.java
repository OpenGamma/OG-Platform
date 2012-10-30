/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.Converters;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * FXBarrierOptionSecurityBeanOperation
 */
public final class FxBarrierOptionSecurityBeanOperation extends AbstractSecurityBeanOperation<FXBarrierOptionSecurity, FXBarrierOptionSecurityBean> {
  
  /**
   * Singleton
   */
  public static final FxBarrierOptionSecurityBeanOperation INSTANCE = new FxBarrierOptionSecurityBeanOperation();

  private FxBarrierOptionSecurityBeanOperation() {
    super(FXBarrierOptionSecurity.SECURITY_TYPE, FXBarrierOptionSecurity.class, FXBarrierOptionSecurityBean.class);
  }

  @Override
  public FXBarrierOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final FXBarrierOptionSecurity security) {
    final FXBarrierOptionSecurityBean bean = new FXBarrierOptionSecurityBean();
    bean.setCallAmount(security.getCallAmount());
    bean.setPutAmount(security.getPutAmount());
    bean.setCallCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCallCurrency().getCode()));
    bean.setPutCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPutCurrency().getCode()));
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setSettlementDate(Converters.dateTimeWithZoneToZonedDateTimeBean(security.getSettlementDate()));
    bean.setBarrierType(security.getBarrierType());
    bean.setBarrierDirection(security.getBarrierDirection());
    bean.setMonitoringType(security.getMonitoringType());
    bean.setSamplingFrequency(security.getSamplingFrequency());
    bean.setBarrierLevel(security.getBarrierLevel());
    bean.setLongShort(security.isLong());
    return bean;
  }

  @Override
  public FXBarrierOptionSecurity createSecurity(OperationContext context, FXBarrierOptionSecurityBean bean) {
    Currency putCurrency = currencyBeanToCurrency(bean.getPutCurrency());
    Currency callCurrency = currencyBeanToCurrency(bean.getCallCurrency());
    Expiry expiry = expiryBeanToExpiry(bean.getExpiry());
    ZonedDateTime settlementDate = Converters.zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate());
    FXBarrierOptionSecurity sec = new FXBarrierOptionSecurity(putCurrency, 
        callCurrency, 
        bean.getPutAmount(), 
        bean.getCallAmount(), 
        expiry, 
        settlementDate, 
        bean.getBarrierType(), 
        bean.getBarrierDirection(), 
        bean.getMonitoringType(), 
        bean.getSamplingFrequency(),
        bean.getBarrierLevel(),
        bean.isLongShort());
    return sec;
  }

}
