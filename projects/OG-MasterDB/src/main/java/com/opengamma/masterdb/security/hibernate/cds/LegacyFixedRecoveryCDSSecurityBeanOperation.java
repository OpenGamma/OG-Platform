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

import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * 
 */
public final class LegacyFixedRecoveryCDSSecurityBeanOperation extends AbstractSecurityBeanOperation<LegacyFixedRecoveryCDSSecurity, LegacyFixedRecoveryCDSSecurityBean> {
  
  /**
   * Singleton.
   * */
  public static final LegacyFixedRecoveryCDSSecurityBeanOperation INSTANCE = new LegacyFixedRecoveryCDSSecurityBeanOperation();

  private LegacyFixedRecoveryCDSSecurityBeanOperation() {
    super(LegacyFixedRecoveryCDSSecurity.SECURITY_TYPE, LegacyFixedRecoveryCDSSecurity.class, LegacyFixedRecoveryCDSSecurityBean.class);
  }

  @Override
  public LegacyFixedRecoveryCDSSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, LegacyFixedRecoveryCDSSecurity security) {
    final LegacyFixedRecoveryCDSSecurityBean bean = new LegacyFixedRecoveryCDSSecurityBean();
    CreditDefaultSwapBeanOperation.createBean(secMasterSession, bean, security);
    bean.setParSpread(security.getParSpread());
    bean.setRecoveryRate(security.getRecoveryRate());
    return bean;
  }

  @Override
  public LegacyFixedRecoveryCDSSecurity createSecurity(OperationContext context, LegacyFixedRecoveryCDSSecurityBean bean) {
    LegacyFixedRecoveryCDSSecurity security = new LegacyFixedRecoveryCDSSecurity(
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
