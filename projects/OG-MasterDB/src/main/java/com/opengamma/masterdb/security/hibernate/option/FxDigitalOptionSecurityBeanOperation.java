/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.Converters;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * FXOptionSecurityBeanOperation
 */
public final class FxDigitalOptionSecurityBeanOperation extends AbstractSecurityBeanOperation<FXDigitalOptionSecurity, FXDigitalOptionSecurityBean> {
  
  /**
   * Singleton
   */
  public static final FxDigitalOptionSecurityBeanOperation INSTANCE = new FxDigitalOptionSecurityBeanOperation();

  private FxDigitalOptionSecurityBeanOperation() {
    super(FXDigitalOptionSecurity.SECURITY_TYPE, FXDigitalOptionSecurity.class, FXDigitalOptionSecurityBean.class);
  }

  @Override
  public FXDigitalOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final FXDigitalOptionSecurity security) {
    final FXDigitalOptionSecurityBean bean = new FXDigitalOptionSecurityBean();
    bean.setCallAmount(security.getCallAmount());
    bean.setPutAmount(security.getPutAmount());
    bean.setCallCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCallCurrency().getCode()));
    bean.setPutCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPutCurrency().getCode()));
    bean.setPaymentCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPaymentCurrency().getCode()));
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setSettlementDate(Converters.dateTimeWithZoneToZonedDateTimeBean(security.getSettlementDate()));
    bean.setIsLong(security.isLong());
    return bean;
  }

  @Override
  public FXDigitalOptionSecurity createSecurity(OperationContext context, FXDigitalOptionSecurityBean bean) {
    Currency putCurrency = currencyBeanToCurrency(bean.getPutCurrency());
    Currency callCurrency = currencyBeanToCurrency(bean.getCallCurrency());
    Currency paymentCurrency = currencyBeanToCurrency(bean.getPaymentCurrency());
    Expiry expiry = expiryBeanToExpiry(bean.getExpiry());
    ZonedDateTime settlementDate = Converters.zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate());
    FXDigitalOptionSecurity sec = new FXDigitalOptionSecurity(putCurrency, callCurrency, bean.getPutAmount(), bean.getCallAmount(), paymentCurrency, expiry, settlementDate, bean.getIsLong());
    return sec;
  }

}
