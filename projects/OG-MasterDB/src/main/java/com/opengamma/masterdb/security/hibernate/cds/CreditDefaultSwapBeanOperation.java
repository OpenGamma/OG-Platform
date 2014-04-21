/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;

import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.swap.NotionalBeanOperation;

/**
 * 
 */
public final class CreditDefaultSwapBeanOperation {

  private CreditDefaultSwapBeanOperation() {
  }

  public static CreditDefaultSwapSecurityBean createBean(final HibernateSecurityMasterDao secMasterSession, final CreditDefaultSwapSecurityBean bean, final CreditDefaultSwapSecurity security) {

    bean.setAdjustEffectiveDate(security.isAdjustEffectiveDate());
    bean.setAdjustMaturityDate(security.isAdjustMaturityDate());
    bean.setBusinessDayConvention(
        secMasterSession.getOrCreateBusinessDayConventionBean(security.getBusinessDayConvention().getName()));
    bean.setBuy(security.isBuy());
    bean.setCouponFrequency(
        secMasterSession.getOrCreateFrequencyBean(security.getCouponFrequency().getName()));
    bean.setDayCount(secMasterSession.getOrCreateDayCountBean(security.getDayCount().getName()));
    bean.setDebtSeniority(secMasterSession.getOrCreateDebtSeniorityBean(security.getDebtSeniority().name()));
    bean.setEffectiveDate(dateTimeWithZoneToZonedDateTimeBean(security.getEffectiveDate()));
    bean.setImmAdjustMaturityDate(security.isImmAdjustMaturityDate());
    bean.setIncludeAccruedPremium(security.isIncludeAccruedPremium());
    bean.setMaturityDate(dateTimeWithZoneToZonedDateTimeBean(security.getMaturityDate()));
    bean.setNotional(NotionalBeanOperation.createBean(secMasterSession, security.getNotional()));
    bean.setProtectionBuyer(externalIdToExternalIdBean(security.getProtectionBuyer()));
    bean.setProtectionSeller(externalIdToExternalIdBean(security.getProtectionSeller()));
    bean.setProtectionStart(security.isProtectionStart());
    bean.setReferenceEntity(externalIdToExternalIdBean(security.getReferenceEntity()));
    bean.setRegionId(externalIdToExternalIdBean(security.getRegionId()));
    bean.setRestructuringClause(
        secMasterSession.getOrCreateRestructuringCleanBean(security.getRestructuringClause().name()));
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setStubType(secMasterSession.getOrCreateStubTypeBean(security.getStubType().name()));
    return bean;
  }
  
  public static CreditDefaultSwapSecurity createSecurity(final CreditDefaultSwapSecurity security, final CreditDefaultSwapSecurityBean bean) {
    return security;
  }

}
