/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

import static com.opengamma.masterdb.security.hibernate.Converters.businessDayConventionBeanToBusinessDayConvention;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.debtSeniorityBeanToDebtSeniority;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.restructuringClauseBeanToRestructuringClause;
import static com.opengamma.masterdb.security.hibernate.Converters.stubTypeBeanToStubType;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;
import static com.opengamma.masterdb.security.hibernate.swap.NotionalBeanOperation.createNotional;

import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.swap.NotionalBeanOperation;

/**
 * 
 */
public final class StdVanillaCDSSecurityBeanOperation extends AbstractSecurityBeanOperation<StandardVanillaCDSSecurity, StandardVanillaCDSSecurityBean> {

  /**
   * Singleton
   */
  public static final StdVanillaCDSSecurityBeanOperation INSTANCE = new StdVanillaCDSSecurityBeanOperation();
  
  private StdVanillaCDSSecurityBeanOperation() {
    super(StandardVanillaCDSSecurity.SECURITY_TYPE, StandardVanillaCDSSecurity.class, StandardVanillaCDSSecurityBean.class);
  }
  
  @Override
  public StandardVanillaCDSSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, StandardVanillaCDSSecurity security) {
    StandardVanillaCDSSecurityBean bean = new StandardVanillaCDSSecurityBean();
    CreditDefaultSwapBeanOperation.createBean(secMasterSession, bean, security);
    bean.setQuotedSpread(security.getQuotedSpread());
    bean.setUpfrontAmount(NotionalBeanOperation.createBean(secMasterSession, security.getUpfrontAmount()));
    bean.setCoupon(security.getCoupon());
    bean.setAdjustCashSettlementDate(security.isAdjustCashSettlementDate());
    bean.setCashSettlementDate(dateTimeWithZoneToZonedDateTimeBean(security.getCashSettlementDate()));
    return bean;
  }

  @Override
  public StandardVanillaCDSSecurity createSecurity(OperationContext context, StandardVanillaCDSSecurityBean bean) {
    StandardVanillaCDSSecurity security = new StandardVanillaCDSSecurity(
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
        bean.getQuotedSpread(), 
        (InterestRateNotional) createNotional(bean.getUpfrontAmount()), 
        bean.getCoupon(), 
        zonedDateTimeBeanToDateTimeWithZone(bean.getCashSettlementDate()), 
        bean.getAdjustCashSettlementDate());
    return security;
  }

}
