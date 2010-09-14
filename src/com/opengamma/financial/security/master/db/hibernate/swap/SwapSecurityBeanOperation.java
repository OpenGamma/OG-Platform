/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.master.db.hibernate.swap;

import static com.opengamma.financial.security.master.db.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.financial.security.master.db.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import com.opengamma.financial.security.master.db.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.financial.security.master.db.hibernate.HibernateSecurityMasterDao;
import com.opengamma.financial.security.master.db.hibernate.OperationContext;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;

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
  public boolean beanEquals(final OperationContext context, SwapSecurityBean bean, SwapSecurity security) {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public SwapSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final SwapSecurity security) {
    return security.accept(new SwapSecurityVisitor<SwapSecurityBean>() {

      private SwapSecurityBean createSwapSecurityBean(final SwapSecurity security) {
        final SwapSecurityBean bean = new SwapSecurityBean();
        bean.setSwapType(SwapType.identify(security));
        bean.setTradeDate(dateTimeWithZoneToZonedDateTimeBean(security.getTradeDate()));
        bean.setEffectiveDate(dateTimeWithZoneToZonedDateTimeBean(security.getEffectiveDate()));
        bean.setMaturityDate(dateTimeWithZoneToZonedDateTimeBean(security.getMaturityDate()));
        bean.setCounterparty(security.getCounterparty());
        bean.setPayLeg(SwapLegBeanOperation.createBean(secMasterSession, security.getPayLeg()));
        bean.setReceiveLeg(SwapLegBeanOperation.createBean(secMasterSession, security.getReceiveLeg()));
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

    });
  }

  @Override
  public SwapSecurity createSecurity(final OperationContext context, final SwapSecurityBean bean) {
    return bean.getSwapType().accept(new SwapSecurityVisitor<SwapSecurity>() {
      
      @Override
      public SwapSecurity visitForwardSwapSecurity(ForwardSwapSecurity ignore) {
        return new ForwardSwapSecurity(zonedDateTimeBeanToDateTimeWithZone(bean.getTradeDate()), zonedDateTimeBeanToDateTimeWithZone(bean.getEffectiveDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate()), bean.getCounterparty(), SwapLegBeanOperation.createSwapLeg(bean.getPayLeg()),
            SwapLegBeanOperation.createSwapLeg(bean.getReceiveLeg()), zonedDateTimeBeanToDateTimeWithZone(bean.getForwardStartDate()));
      }

      @Override
      public SwapSecurity visitSwapSecurity(SwapSecurity ignore) {
        return new SwapSecurity(zonedDateTimeBeanToDateTimeWithZone(bean.getTradeDate()), zonedDateTimeBeanToDateTimeWithZone(bean.getEffectiveDate()), zonedDateTimeBeanToDateTimeWithZone(bean
            .getMaturityDate()), bean.getCounterparty(), SwapLegBeanOperation.createSwapLeg(bean.getPayLeg()), SwapLegBeanOperation.createSwapLeg(bean.getReceiveLeg()));
      }

    });
  }

}
