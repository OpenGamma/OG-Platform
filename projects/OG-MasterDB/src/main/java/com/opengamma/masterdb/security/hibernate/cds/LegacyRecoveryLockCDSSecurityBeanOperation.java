/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

import static com.opengamma.masterdb.security.hibernate.Converters.businessDayConventionBeanToBusinessDayConvention;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.debtSeniorityBeanToDebtSeniority;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.restructuringClauseBeanToRestructuringClause;
import static com.opengamma.masterdb.security.hibernate.Converters.stubTypeBeanToStubType;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;
import static com.opengamma.masterdb.security.hibernate.swap.NotionalBeanOperation.createNotional;

import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * 
 */
public final class LegacyRecoveryLockCDSSecurityBeanOperation extends AbstractSecurityBeanOperation<LegacyRecoveryLockCDSSecurity, LegacyRecoveryLockCDSSecurityBean> {
  
  /**
   * Singleton.
   * */
  public static final LegacyRecoveryLockCDSSecurityBeanOperation INSTANCE = new LegacyRecoveryLockCDSSecurityBeanOperation();

  private LegacyRecoveryLockCDSSecurityBeanOperation() {
    super(LegacyRecoveryLockCDSSecurity.SECURITY_TYPE, LegacyRecoveryLockCDSSecurity.class, LegacyRecoveryLockCDSSecurityBean.class);
  }
  
  @Override
  public LegacyRecoveryLockCDSSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, LegacyRecoveryLockCDSSecurity security) {
    final LegacyRecoveryLockCDSSecurityBean bean = new LegacyRecoveryLockCDSSecurityBean();
    CreditDefaultSwapBeanOperation.createBean(secMasterSession, bean, security);
    bean.setParSpread(security.getParSpread());
    bean.setRecoveryRate(security.getRecoveryRate());
    return bean;
  }

  @Override
  public LegacyRecoveryLockCDSSecurity createSecurity(OperationContext context, LegacyRecoveryLockCDSSecurityBean bean) {
    LegacyRecoveryLockCDSSecurity security = new LegacyRecoveryLockCDSSecurity(
        bean.getBuy(), 
        externalIdBeanToExternalId(bean.getProtectionSeller()), 
        externalIdBeanToExternalId(bean.getProtectionBuyer()), 
        externalIdBeanToExternalId(bean.getReferenceEntity()), 
        debtSeniorityBeanToDebtSeniority(bean.getDebtSeniority()), 
        restructuringClauseBeanToRestructuringClause(bean.getRestructuringClause()), 
        externalIdBeanToExternalId(bean.getRegionId()), 
        zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate()), 
        zonedDateTimeBeanToDateTimeWithZone(bean.getEffectiveDate()), 
        zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate()), 
        stubTypeBeanToStubType(bean.getStubType()), 
        frequencyBeanToFrequency(bean.getCouponFrequency()), 
        dayCountBeanToDayCount(bean.getDayCount()), 
        businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()), 
        bean.getImmAdjustMaturityDate(), 
        bean.getAdjustEffectiveDate(), 
        bean.getAdjustMaturityDate(), 
        (InterestRateNotional) createNotional(bean.getNotional()), 
        bean.getRecoveryRate(), 
        bean.getIncludeAccruedPremium(), 
        bean.getProtectionStart(), 
        bean.getParSpread());
    return security;
  }

}
