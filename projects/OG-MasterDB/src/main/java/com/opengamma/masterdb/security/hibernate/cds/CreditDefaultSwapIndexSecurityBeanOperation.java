/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

import static com.opengamma.masterdb.security.hibernate.Converters.businessDayConventionBeanToBusinessDayConvention;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.stubTypeBeanToStubType;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;
import static com.opengamma.masterdb.security.hibernate.swap.NotionalBeanOperation.createNotional;

import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.swap.NotionalBeanOperation;

/**
 * 
 */
public final class CreditDefaultSwapIndexSecurityBeanOperation extends AbstractSecurityBeanOperation<CreditDefaultSwapIndexSecurity, CreditDefaultSwapIndexSecurityBean> {

  /**
   * Singleton
   */
  public static final CreditDefaultSwapIndexSecurityBeanOperation INSTANCE = new CreditDefaultSwapIndexSecurityBeanOperation();

  private CreditDefaultSwapIndexSecurityBeanOperation() {
    super(CreditDefaultSwapIndexSecurity.SECURITY_TYPE, CreditDefaultSwapIndexSecurity.class, CreditDefaultSwapIndexSecurityBean.class);
  }


  @Override
  public CreditDefaultSwapIndexSecurityBean createBean(OperationContext context,
                                                       HibernateSecurityMasterDao secMasterSession,
                                                       CreditDefaultSwapIndexSecurity security) {

    CreditDefaultSwapIndexSecurityBean bean = new CreditDefaultSwapIndexSecurityBean();

    bean.setAdjustEffectiveDate(security.isAdjustEffectiveDate());
    bean.setAdjustMaturityDate(security.isAdjustMaturityDate());
    bean.setBusinessDayConvention(
        secMasterSession.getOrCreateBusinessDayConventionBean(security.getBusinessDayConvention().getName()));
    bean.setBuy(security.isBuy());
    bean.setCouponFrequency(
        secMasterSession.getOrCreateFrequencyBean(security.getCouponFrequency().getName()));
    bean.setDayCount(secMasterSession.getOrCreateDayCountBean(security.getDayCount().getName()));
    bean.setEffectiveDate(dateTimeWithZoneToZonedDateTimeBean(security.getEffectiveDate()));
    bean.setImmAdjustMaturityDate(security.isImmAdjustMaturityDate());
    bean.setIncludeAccruedPremium(security.isIncludeAccruedPremium());
    bean.setMaturityDate(dateTimeWithZoneToZonedDateTimeBean(security.getMaturityDate()));
    bean.setNotional(NotionalBeanOperation.createBean(secMasterSession, security.getNotional()));
    bean.setProtectionBuyer(externalIdToExternalIdBean(security.getProtectionBuyer()));
    bean.setProtectionSeller(externalIdToExternalIdBean(security.getProtectionSeller()));
    bean.setProtectionStart(security.isProtectionStart());
    bean.setIndex(externalIdToExternalIdBean(security.getReferenceEntity()));
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setStubType(secMasterSession.getOrCreateStubTypeBean(security.getStubType().name()));
    bean.setSettlementDate(dateTimeWithZoneToZonedDateTimeBean(security.getSettlementDate()));
    bean.setAdjustSettlementDate(security.isAdjustSettlementDate());
    bean.setUpfrontPayment(NotionalBeanOperation.createBean(secMasterSession, security.getUpfrontPayment()));
    bean.setIndexCoupon(security.getIndexCoupon());
    return bean;
  }

  @Override
  public CreditDefaultSwapIndexSecurity createSecurity(OperationContext context,
                                                       CreditDefaultSwapIndexSecurityBean bean) {

    return new CreditDefaultSwapIndexSecurity(
        bean.getBuy(), externalIdBeanToExternalId(bean.getProtectionBuyer()), externalIdBeanToExternalId(bean.getProtectionSeller()),
        externalIdBeanToExternalId(bean.getIndex()), zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate()),
        zonedDateTimeBeanToDateTimeWithZone(bean.getEffectiveDate()), zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate()),
        stubTypeBeanToStubType(bean.getStubType()), frequencyBeanToFrequency(bean.getCouponFrequency()),
        dayCountBeanToDayCount(bean.getDayCount()), businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
        bean.getImmAdjustMaturityDate(), bean.getAdjustEffectiveDate(), bean.getAdjustMaturityDate(),
        (InterestRateNotional) createNotional(bean.getNotional()),
        bean.getIncludeAccruedPremium(), bean.getProtectionStart(),
        zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate()),
        bean.getAdjustSettlementDate(), (InterestRateNotional) createNotional(bean.getUpfrontPayment()), bean.getIndexCoupon());
  }
}
