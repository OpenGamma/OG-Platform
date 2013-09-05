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

import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.swap.NotionalBeanOperation;

/**
 * 
 */
public final class StdFixedRecoveryCDSSecurityBeanOperation extends AbstractSecurityBeanOperation<StandardFixedRecoveryCDSSecurity, StandardFixedRecoveryCDSSecurityBean> {

  /**
   * Singleton
   */
  public static final StdFixedRecoveryCDSSecurityBeanOperation INSTANCE = new StdFixedRecoveryCDSSecurityBeanOperation();
  
  private StdFixedRecoveryCDSSecurityBeanOperation() {
    super(StandardFixedRecoveryCDSSecurity.SECURITY_TYPE, StandardFixedRecoveryCDSSecurity.class, StandardFixedRecoveryCDSSecurityBean.class);
  }


  @Override
  public StandardFixedRecoveryCDSSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, StandardFixedRecoveryCDSSecurity security) {
    StandardFixedRecoveryCDSSecurityBean bean = new StandardFixedRecoveryCDSSecurityBean();
    CreditDefaultSwapBeanOperation.createBean(secMasterSession, bean, security);
    bean.setQuotedSpread(security.getQuotedSpread());
    bean.setUpfrontAmount(NotionalBeanOperation.createBean(secMasterSession, security.getUpfrontAmount()));
    bean.setRecoveryRate(security.getRecoveryRate());
    return bean;
  }

  @Override
  public StandardFixedRecoveryCDSSecurity createSecurity(OperationContext context, StandardFixedRecoveryCDSSecurityBean bean) {
    StandardFixedRecoveryCDSSecurity security = new StandardFixedRecoveryCDSSecurity(
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
