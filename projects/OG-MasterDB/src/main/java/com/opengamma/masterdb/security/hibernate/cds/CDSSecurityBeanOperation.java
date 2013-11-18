/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

import static com.opengamma.masterdb.security.hibernate.Converters.businessDayConventionBeanToBusinessDayConvention;
import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.stubTypeBeanToStubType;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Operation for converting between {@link CDSSecurity} and {@link CDSSecurityBean}
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public final class CDSSecurityBeanOperation extends AbstractSecurityBeanOperation<CDSSecurity, CDSSecurityBean> {

  /** Singleton instance */
  public static final CDSSecurityBeanOperation INSTANCE = new CDSSecurityBeanOperation();

  private CDSSecurityBeanOperation() {
    super(CDSSecurity.SECURITY_TYPE, CDSSecurity.class, CDSSecurityBean.class);
  }

  @Override
  public CDSSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, CDSSecurity security) {
    final CDSSecurityBean bean = new CDSSecurityBean();
    bean.setNotional(security.getNotional());
    bean.setRecoveryRate(security.getRecoveryRate());
    bean.setSpread(security.getSpread());
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setMaturity(dateTimeWithZoneToZonedDateTimeBean(security.getMaturity()));
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setPremiumFrequency(secMasterSession.getOrCreateFrequencyBean(security.getPremiumFrequency().getName()));
    bean.setDayCount(secMasterSession.getOrCreateDayCountBean(security.getDayCount().getName()));
    bean.setBusinessDayConvention(secMasterSession.getOrCreateBusinessDayConventionBean(security.getBusinessDayConvention().getName()));
    bean.setStubType(secMasterSession.getOrCreateStubTypeBean(security.getStubType().name()));
    bean.setSettlementDays(security.getSettlementDays());
    bean.setUnderlyingIssuer(security.getUnderlyingIssuer());
    bean.setUnderlyingCurrency(secMasterSession.getOrCreateCurrencyBean(security.getUnderlyingCurrency().getCode()));
    bean.setUnderlyingSeniority(security.getUnderlyingSeniority());
    bean.setRestructuringClause(security.getRestructuringClause());
    return bean;
  }

  @Override
  public CDSSecurity createSecurity(OperationContext context, CDSSecurityBean bean) {
    return new CDSSecurity(
      bean.getNotional(),
      bean.getRecoveryRate(),
      bean.getSpread(),
      currencyBeanToCurrency(bean.getCurrency()),
      zonedDateTimeBeanToDateTimeWithZone(bean.getMaturity()),
      zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate()),
      frequencyBeanToFrequency(bean.getPremiumFrequency()),
      dayCountBeanToDayCount(bean.getDayCount()),
      businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
      stubTypeBeanToStubType(bean.getStubType()),
      bean.getSettlementDays(),
      bean.getUnderlyingIssuer(),
      currencyBeanToCurrency(bean.getUnderlyingCurrency()),
      bean.getUnderlyingSeniority(),
      bean.getRestructuringClause());
  }
}
