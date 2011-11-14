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

import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.Converters;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * FXOptionSecurityBeanOperation
 */
public final class FxOptionSecurityBeanOperation extends AbstractSecurityBeanOperation<FXOptionSecurity, FXOptionSecurityBean> {
  
  /**
   * Singleton
   */
  public static final FxOptionSecurityBeanOperation INSTANCE = new FxOptionSecurityBeanOperation();

  private FxOptionSecurityBeanOperation() {
    super(FXOptionSecurity.SECURITY_TYPE, FXOptionSecurity.class, FXOptionSecurityBean.class);
  }

  @Override
  public FXOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final FXOptionSecurity security) {
    final FXOptionSecurityBean bean = new FXOptionSecurityBean();
    bean.setCallAmount(security.getCallAmount());
    bean.setPutAmount(security.getPutAmount());
    bean.setCallCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCallCurrency().getCode()));
    bean.setPutCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPutCurrency().getCode()));
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setSettlementDate(Converters.dateTimeWithZoneToZonedDateTimeBean(security.getSettlementDate()));
    bean.setIsLong(security.isLong());
    bean.setOptionExerciseType(OptionExerciseType.identify(security.getExerciseType()));
    return bean;
  }

  @Override
  public FXOptionSecurity createSecurity(OperationContext context, FXOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());
    Currency putCurrency = currencyBeanToCurrency(bean.getPutCurrency());
    Currency callCurrency = currencyBeanToCurrency(bean.getCallCurrency());
    Expiry expiry = expiryBeanToExpiry(bean.getExpiry());
    ZonedDateTime settlementDate = Converters.zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate());
    FXOptionSecurity sec = new FXOptionSecurity(putCurrency, callCurrency, bean.getPutAmount(), bean.getCallAmount(), expiry, settlementDate, bean.getIsLong(), exerciseType);
    return sec;
  }

}
