/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.capfloor;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.validateDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.validateFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;

/**
 * Bean/security conversion operations.
 */
public final class CapFloorSecurityBeanOperation extends AbstractSecurityBeanOperation<CapFloorSecurity, CapFloorSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final CapFloorSecurityBeanOperation INSTANCE = new CapFloorSecurityBeanOperation();

  private CapFloorSecurityBeanOperation() {
    super(CapFloorSecurity.SECURITY_TYPE, CapFloorSecurity.class, CapFloorSecurityBean.class);
  }

  @Override
  public CapFloorSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, CapFloorSecurity security) {
    validateDayCount(security.getDayCount().getName());
    validateFrequency(security.getFrequency().getName());
    
    final CapFloorSecurityBean bean = new CapFloorSecurityBean();
    bean.setCap(security.isCap());
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setDayCount(secMasterSession.getOrCreateDayCountBean(security.getDayCount().getName()));
    bean.setFrequency(secMasterSession.getOrCreateFrequencyBean(security.getFrequency().getName()));
    bean.setIbor(security.isIbor());
    bean.setMaturityDate(dateTimeWithZoneToZonedDateTimeBean(security.getMaturityDate()));
    bean.setNotional(security.getNotional());
    bean.setPayer(security.isPayer());
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setStrike(security.getStrike());
    bean.setUnderlyingIdentifier(externalIdToExternalIdBean(security.getUnderlyingId()));
    return bean;
  }

  @Override
  public CapFloorSecurity createSecurity(final OperationContext context, CapFloorSecurityBean bean) {
    
    ZonedDateTime startDate = zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate());
    ZonedDateTime maturityDate = zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate());
    ExternalId underlyingIdentifier = externalIdBeanToExternalId(bean.getUnderlyingIdentifier());
    Frequency frequency = frequencyBeanToFrequency(bean.getFrequency());
    Currency currency = currencyBeanToCurrency(bean.getCurrency());
    DayCount dayCount = dayCountBeanToDayCount(bean.getDayCount());
    return new CapFloorSecurity(startDate, 
        maturityDate, 
        bean.getNotional(), 
        underlyingIdentifier, 
        bean.getStrike(), 
        frequency, 
        currency, 
        dayCount, 
        bean.isPayer(), 
        bean.isCap(), 
        bean.isIbor());
  }

}
