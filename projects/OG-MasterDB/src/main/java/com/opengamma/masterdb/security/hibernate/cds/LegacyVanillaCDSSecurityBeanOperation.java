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

import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * 
 */
public final class LegacyVanillaCDSSecurityBeanOperation extends AbstractSecurityBeanOperation<LegacyVanillaCDSSecurity, LegacyVanillaCDSSecurityBean> {
  
  /**
   * Singleton.
   * */
  public static final LegacyVanillaCDSSecurityBeanOperation INSTANCE = new LegacyVanillaCDSSecurityBeanOperation();

  private LegacyVanillaCDSSecurityBeanOperation() {
    super(LegacyVanillaCDSSecurity.SECURITY_TYPE, LegacyVanillaCDSSecurity.class, LegacyVanillaCDSSecurityBean.class);
  }

  @Override
  public LegacyVanillaCDSSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, LegacyVanillaCDSSecurity security) {
    final LegacyVanillaCDSSecurityBean bean = new LegacyVanillaCDSSecurityBean();
    CreditDefaultSwapBeanOperation.createBean(secMasterSession, bean, security);
    bean.setParSpread(security.getParSpread());
    return bean;
  }

  @Override
  public LegacyVanillaCDSSecurity createSecurity(OperationContext context, LegacyVanillaCDSSecurityBean bean) {
    LegacyVanillaCDSSecurity security = new LegacyVanillaCDSSecurity(
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
        bean.getIncludeAccruedPremium(),
        bean.getProtectionStart(), 
        bean.getParSpread());
    return security;
  }

}
