/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;
import static com.opengamma.masterdb.security.hibernate.Converters.tenorBeanToTenor;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Bean/security conversion operations.
 */
public final class SwapSecurityBeanOperation extends AbstractSecurityBeanOperation<SwapSecurity, SwapSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final SwapSecurityBeanOperation INSTANCE = new SwapSecurityBeanOperation();

  private SwapSecurityBeanOperation() {
    super("SWAP", SwapSecurity.class, SwapSecurityBean.class);
  }

  @Override
  public SwapSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final SwapSecurity security) {
    return security.accept(new FinancialSecurityVisitorAdapter<SwapSecurityBean>() {

      private SwapSecurityBean createSwapSecurityBean(final SwapSecurity security) {
        final SwapSecurityBean bean = new SwapSecurityBean();
        bean.setSwapType(SwapType.identify(security));
        bean.setTradeDate(dateTimeWithZoneToZonedDateTimeBean(security.getTradeDate()));
        bean.setEffectiveDate(dateTimeWithZoneToZonedDateTimeBean(security.getEffectiveDate()));
        bean.setMaturityDate(dateTimeWithZoneToZonedDateTimeBean(security.getMaturityDate()));
        bean.setCounterparty(security.getCounterparty());
        bean.setPayLeg(SwapLegBeanOperation.createBean(secMasterSession, security.getPayLeg()));
        bean.setReceiveLeg(SwapLegBeanOperation.createBean(secMasterSession, security.getReceiveLeg()));
        bean.setExchangeInitialNotional(security.isExchangeInitialNotional());
        bean.setExchangeFinalNotional(security.isExchangeFinalNotional());
        return bean;
      }

      @Override
      public SwapSecurityBean visitForwardSwapSecurity(ForwardSwapSecurity security) {
        final SwapSecurityBean bean = createSwapSecurityBean(security);
        bean.setForwardStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getForwardStartDate()));
        return bean;
      }

      @Override
      public SwapSecurityBean visitSwapSecurity(SwapSecurity security) {
        return createSwapSecurityBean(security);
      }

      @Override
      public SwapSecurityBean visitYearOnYearInflationSwapSecurity(YearOnYearInflationSwapSecurity security) {
        final SwapSecurityBean bean = createSwapSecurityBean(security);
        bean.setMaturityTenor(secMasterSession.getOrCreateTenorBean(security.getMaturityTenor().getPeriod().toString()));
        return bean;
      }
      
      @Override
      public SwapSecurityBean visitZeroCouponInflationSwapSecurity(ZeroCouponInflationSwapSecurity security) {
        final SwapSecurityBean bean = createSwapSecurityBean(security);
        return bean;
      }
    });
  }

  @Override
  public SwapSecurity createSecurity(final OperationContext context, final SwapSecurityBean bean) {
    return bean.getSwapType().accept(new FinancialSecurityVisitorAdapter<SwapSecurity>() {

      @Override
      public SwapSecurity visitForwardSwapSecurity(ForwardSwapSecurity ignore) {
        ForwardSwapSecurity sec = new ForwardSwapSecurity(
            zonedDateTimeBeanToDateTimeWithZone(bean.getTradeDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getEffectiveDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate()),
            bean.getCounterparty(),
            SwapLegBeanOperation.createSwapLeg(bean.getPayLeg()),
            SwapLegBeanOperation.createSwapLeg(bean.getReceiveLeg()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getForwardStartDate()));
        sec.setExchangeInitialNotional(bean.isExchangeInitialNotional());
        sec.setExchangeFinalNotional(bean.isExchangeFinalNotional());
        return sec;
      }

      @Override
      public SwapSecurity visitSwapSecurity(SwapSecurity ignore) {
        SwapSecurity sec = new SwapSecurity(
            zonedDateTimeBeanToDateTimeWithZone(bean.getTradeDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getEffectiveDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate()),
            bean.getCounterparty(),
            SwapLegBeanOperation.createSwapLeg(bean.getPayLeg()),
            SwapLegBeanOperation.createSwapLeg(bean.getReceiveLeg()));
        sec.setExchangeInitialNotional(bean.isExchangeInitialNotional());
        sec.setExchangeFinalNotional(bean.isExchangeFinalNotional());
        return sec;
      }

      @Override
      public SwapSecurity visitYearOnYearInflationSwapSecurity(YearOnYearInflationSwapSecurity security) {
        return new YearOnYearInflationSwapSecurity(zonedDateTimeBeanToDateTimeWithZone(bean.getTradeDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getEffectiveDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate()),
            bean.getCounterparty(),
            SwapLegBeanOperation.createSwapLeg(bean.getPayLeg()),
            SwapLegBeanOperation.createSwapLeg(bean.getReceiveLeg()),
            bean.isExchangeInitialNotional(),
            bean.isExchangeFinalNotional(),
            tenorBeanToTenor(bean.getMaturityTenor()));
      }
      
      @Override
      public SwapSecurity visitZeroCouponInflationSwapSecurity(ZeroCouponInflationSwapSecurity security) {
        return new ZeroCouponInflationSwapSecurity(zonedDateTimeBeanToDateTimeWithZone(bean.getTradeDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getEffectiveDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate()),
            bean.getCounterparty(),
            SwapLegBeanOperation.createSwapLeg(bean.getPayLeg()),
            SwapLegBeanOperation.createSwapLeg(bean.getReceiveLeg()));
      }
    });
  }

}
