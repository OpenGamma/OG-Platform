/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;

/**
 * Bean operation for {@link CreditDefaultSwapOptionSecurity}
 */
public final class CDSOptionSecurityBeanOperation extends AbstractSecurityBeanOperation<CreditDefaultSwapOptionSecurity, CreditDefaultSwapOptionSecurityBean> {
  
  /**
   * Singleton
   */
  public static final CDSOptionSecurityBeanOperation INSTANCE = new CDSOptionSecurityBeanOperation();

  private CDSOptionSecurityBeanOperation() {
    super(CreditDefaultSwapOptionSecurity.SECURITY_TYPE, CreditDefaultSwapOptionSecurity.class, CreditDefaultSwapOptionSecurityBean.class);
  }

  @Override
  public CreditDefaultSwapOptionSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, CreditDefaultSwapOptionSecurity security) {
    CreditDefaultSwapOptionSecurityBean bean = new CreditDefaultSwapOptionSecurityBean();
    
    bean.setBuy(security.isBuy());
    bean.setProtectionBuyer(externalIdToExternalIdBean(security.getProtectionBuyer()));
    bean.setProtectionSeller(externalIdToExternalIdBean(security.getProtectionSeller()));
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setMaturityDate(dateTimeWithZoneToZonedDateTimeBean(security.getMaturityDate()));
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setNotional(security.getNotional());
    bean.setStrike(security.getStrike());
    bean.setKnockOut(security.isKnockOut());
    bean.setPayer(security.isPayer());
    bean.setExerciseType(OptionExerciseType.identify(security.getExerciseType()));
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    return bean;
  }

  @Override
  public CreditDefaultSwapOptionSecurity createSecurity(OperationContext context, CreditDefaultSwapOptionSecurityBean bean) {
    
    final boolean buy = bean.getBuy();
    final ExternalId protectionBuyer = externalIdBeanToExternalId(bean.getProtectionBuyer());
    final ExternalId protectionSeller = externalIdBeanToExternalId(bean.getProtectionSeller());
    final ZonedDateTime startDate = zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate());
    final ZonedDateTime maturityDate = zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate());
    final Currency currency = currencyBeanToCurrency(bean.getCurrency());
    final ExerciseType exerciseType = bean.getExerciseType().accept(new ExerciseTypeVisitorImpl());
    final ExternalId underlying = externalIdBeanToExternalId(bean.getUnderlying());
    
    
    final CreditDefaultSwapOptionSecurity security = new CreditDefaultSwapOptionSecurity(buy, protectionBuyer, protectionSeller, 
        startDate, maturityDate, currency, bean.getNotional(), bean.getStrike(), bean.getKnockOut(), bean.getPayer(), exerciseType, underlying);
    return security;
  }

}
