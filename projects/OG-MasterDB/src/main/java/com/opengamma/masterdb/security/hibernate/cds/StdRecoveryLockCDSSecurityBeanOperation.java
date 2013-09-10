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

import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.swap.NotionalBeanOperation;

/**
 * 
 */
public final class StdRecoveryLockCDSSecurityBeanOperation extends AbstractSecurityBeanOperation<StandardRecoveryLockCDSSecurity, StandardRecoveryLockCDSSecurityBean> {

  /**
   * Singleton
   */
  public static final StdRecoveryLockCDSSecurityBeanOperation INSTANCE = new StdRecoveryLockCDSSecurityBeanOperation();
  
  private StdRecoveryLockCDSSecurityBeanOperation() {
    super(StandardRecoveryLockCDSSecurity.SECURITY_TYPE, StandardRecoveryLockCDSSecurity.class, StandardRecoveryLockCDSSecurityBean.class);
  }

  @Override
  public StandardRecoveryLockCDSSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, StandardRecoveryLockCDSSecurity security) {
    StandardRecoveryLockCDSSecurityBean bean = new StandardRecoveryLockCDSSecurityBean();
    CreditDefaultSwapBeanOperation.createBean(secMasterSession, bean, security);
    bean.setQuotedSpread(security.getQuotedSpread());
    bean.setUpfrontAmount(NotionalBeanOperation.createBean(secMasterSession, security.getUpfrontAmount()));
    bean.setRecoveryRate(security.getRecoveryRate());
    return bean;
  }

  @Override
  public StandardRecoveryLockCDSSecurity createSecurity(OperationContext context, StandardRecoveryLockCDSSecurityBean bean) {
    StandardRecoveryLockCDSSecurity security = new StandardRecoveryLockCDSSecurity(
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
        bean.getQuotedSpread(), 
        (InterestRateNotional) createNotional(bean.getUpfrontAmount()));
    return security;
  }

}
