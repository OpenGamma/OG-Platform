/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import static com.opengamma.masterdb.security.hibernate.Converters.businessDayConventionBeanToBusinessDayConvention;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;

/**
 * Bean conversion operations.
 */
public final class SwapLegBeanOperation {

  private SwapLegBeanOperation() {
  }

  public static SwapLegBean createBean(final HibernateSecurityMasterDao secMasterSession, final SwapLeg swapLeg) {
    return swapLeg.accept(new SwapLegVisitor<SwapLegBean>() {

      private SwapLegBean createSwapLegBean(SwapLeg swapLeg) {
        final SwapLegBean bean = new SwapLegBean();
        bean.setSwapLegType(SwapLegType.identify(swapLeg));
        bean.setBusinessDayConvention(secMasterSession.getOrCreateBusinessDayConventionBean(swapLeg.getBusinessDayConvention().getConventionName()));
        bean.setDayCount(secMasterSession.getOrCreateDayCountBean(swapLeg.getDayCount().getConventionName()));
        bean.setFrequency(secMasterSession.getOrCreateFrequencyBean(swapLeg.getFrequency().getConventionName()));
        bean.setNotional(NotionalBeanOperation.createBean(secMasterSession, swapLeg.getNotional()));
        bean.setRegion(externalIdToExternalIdBean(swapLeg.getRegionIdentifier()));
        bean.setEOM(swapLeg.getIsEOM());
        return bean;
      }

      private SwapLegBean createInterestRateLegBean(InterestRateLeg swapLeg) {
        final SwapLegBean bean = createSwapLegBean(swapLeg);
        return bean;
      }

      @Override
      public SwapLegBean visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
        final SwapLegBean bean = createInterestRateLegBean(swapLeg);
        bean.setRate(swapLeg.getRate());
        return bean;
      }

      @Override
      public SwapLegBean visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
        final SwapLegBean bean = createInterestRateLegBean(swapLeg);
        bean.setRate(swapLeg.getInitialFloatingRate());
        bean.setRateIdentifier(externalIdToExternalIdBean(swapLeg.getFloatingReferenceRateIdentifier()));
        bean.setSpread(swapLeg.getSpread());
        bean.setFloatingRateType(swapLeg.getFloatingRateType());
        return bean;
      }
    });
  }

  public static SwapLeg createSwapLeg(final SwapLegBean bean) {
    return bean.getSwapLegType().accept(new SwapLegVisitor<SwapLeg>() {

      @Override
      public SwapLeg visitFixedInterestRateLeg(FixedInterestRateLeg ignore) {
        return new FixedInterestRateLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEOM(), bean.getRate());
      }

      @Override
      public SwapLeg visitFloatingInterestRateLeg(FloatingInterestRateLeg ignore) {
        return new FloatingInterestRateLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEOM(), 
            externalIdBeanToExternalId(bean.getRateIdentifier()),
            bean.getRate(),
            bean.getSpread(),
            bean.getFloatingRateType());
      }
    });
  }

}
