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

import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.Converters;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * FXOptionSecurityBeanOperation
 */
public final class NonDeliverableFxDigitalOptionSecurityBeanOperation extends AbstractSecurityBeanOperation<NonDeliverableFXDigitalOptionSecurity, NonDeliverableFXDigitalOptionSecurityBean> {
  
  /**
   * Singleton
   */
  public static final NonDeliverableFxDigitalOptionSecurityBeanOperation INSTANCE = new NonDeliverableFxDigitalOptionSecurityBeanOperation();

  private NonDeliverableFxDigitalOptionSecurityBeanOperation() {
    super(NonDeliverableFXDigitalOptionSecurity.SECURITY_TYPE, NonDeliverableFXDigitalOptionSecurity.class, NonDeliverableFXDigitalOptionSecurityBean.class);
  }

  @Override
  public NonDeliverableFXDigitalOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final NonDeliverableFXDigitalOptionSecurity security) {
    final NonDeliverableFXDigitalOptionSecurityBean bean = new NonDeliverableFXDigitalOptionSecurityBean();
    bean.setCallAmount(security.getCallAmount());
    bean.setPutAmount(security.getPutAmount());
    bean.setCallCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCallCurrency().getCode()));
    bean.setPutCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPutCurrency().getCode()));
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setSettlementDate(Converters.dateTimeWithZoneToZonedDateTimeBean(security.getSettlementDate()));
    bean.setIsLong(security.isLong());
    bean.setDeliverInCallCurrency(security.isDeliverInCallCurrency());
    bean.setPaymentCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPaymentCurrency().getCode()));
    return bean;
  }

  @Override
  public NonDeliverableFXDigitalOptionSecurity createSecurity(OperationContext context, NonDeliverableFXDigitalOptionSecurityBean bean) {
    Currency putCurrency = currencyBeanToCurrency(bean.getPutCurrency());
    Currency callCurrency = currencyBeanToCurrency(bean.getCallCurrency());
    Currency paymentCurrency = currencyBeanToCurrency(bean.getPaymentCurrency());
    Expiry expiry = expiryBeanToExpiry(bean.getExpiry());
    ZonedDateTime settlementDate = Converters.zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate());
    boolean deliverInCallCurrency = bean.isDeliverInCallCurrency();
    NonDeliverableFXDigitalOptionSecurity sec = new NonDeliverableFXDigitalOptionSecurity(putCurrency, callCurrency, 
                                                                                          bean.getPutAmount(), bean.getCallAmount(), 
                                                                                          paymentCurrency, expiry, settlementDate, bean.getIsLong(),
                                                                                          deliverInCallCurrency);
    return sec;
  }

}
