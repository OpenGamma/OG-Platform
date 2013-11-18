/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import static com.opengamma.masterdb.security.hibernate.Converters.businessDayConventionBeanToBusinessDayConvention;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.masterdb.security.hibernate.Converters;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;

/**
 * Bean conversion operations.
 */
public final class SwapLegBeanOperation {

  private SwapLegBeanOperation() {
  }

  public static SwapLegBean createBean(final HibernateSecurityMasterDao secMasterSession, final SwapLeg swapLeg) {
    Converters.validateBusinessDayConvention(swapLeg.getBusinessDayConvention().getName());
    Converters.validateFrequency(swapLeg.getFrequency().getName());
    Converters.validateDayCount(swapLeg.getDayCount().getName());
    
    return swapLeg.accept(new SwapLegVisitor<SwapLegBean>() {

      private SwapLegBean createSwapLegBean(SwapLeg swapLeg) {
        final SwapLegBean bean = new SwapLegBean();
        bean.setSwapLegType(SwapLegType.identify(swapLeg));
        bean.setBusinessDayConvention(secMasterSession.getOrCreateBusinessDayConventionBean(swapLeg.getBusinessDayConvention().getName()));
        bean.setDayCount(secMasterSession.getOrCreateDayCountBean(swapLeg.getDayCount().getName()));
        bean.setFrequency(secMasterSession.getOrCreateFrequencyBean(swapLeg.getFrequency().getName()));
        bean.setNotional(NotionalBeanOperation.createBean(secMasterSession, swapLeg.getNotional()));
        bean.setRegion(externalIdToExternalIdBean(swapLeg.getRegionId()));
        bean.setEom(swapLeg.isEom());
        return bean;
      }

      private SwapLegBean createInterestRateLegBean(InterestRateLeg swapLeg) {
        return createSwapLegBean(swapLeg);
      }
      
      private void setFloatingInterestRateProperties(FloatingInterestRateLeg swapLeg, final SwapLegBean bean) {
        if (swapLeg.getInitialFloatingRate() != null) {
          bean.setRate(swapLeg.getInitialFloatingRate());
        }
        bean.setRateIdentifier(externalIdToExternalIdBean(swapLeg.getFloatingReferenceRateId()));
        bean.setFloatingRateType(swapLeg.getFloatingRateType());
        if (swapLeg.getSettlementDays() != null) {
          bean.setSettlementDays(swapLeg.getSettlementDays());
        }
        if (swapLeg.getOffsetFixing() != null) {
          Converters.validateFrequency(swapLeg.getOffsetFixing().getName());
          bean.setOffsetFixing(secMasterSession.getOrCreateFrequencyBean(swapLeg.getOffsetFixing().getName()));
        }
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
        setFloatingInterestRateProperties(swapLeg, bean);
        return bean;
      }
      
      @Override
      public SwapLegBean visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
        final SwapLegBean bean = createInterestRateLegBean(swapLeg);
        setFloatingInterestRateProperties(swapLeg, bean);
        bean.setSpread(swapLeg.getSpread());
        return bean;
      }

      @Override
      public SwapLegBean visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
        final SwapLegBean bean = createInterestRateLegBean(swapLeg);
        setFloatingInterestRateProperties(swapLeg, bean);
        bean.setGearing(swapLeg.getGearing());
        return bean;
      }

      @Override
      public SwapLegBean visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
        SwapLegBean bean = createSwapLegBean(swapLeg);
        bean.setStrike(swapLeg.getStrike());
        bean.setVarianceSwapType(swapLeg.getType());
        return bean;
      }

      @Override
      public SwapLegBean visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
        SwapLegBean bean = createSwapLegBean(swapLeg);
        bean.setUnderlyingId(externalIdToExternalIdBean(swapLeg.getUnderlyingId()));
        Frequency monitoringFrequency = swapLeg.getMonitoringFrequency();
        if (monitoringFrequency != null) {
          Converters.validateFrequency(monitoringFrequency.getName());
          bean.setMonitoringFrequency(secMasterSession.getOrCreateFrequencyBean(monitoringFrequency.getName()));
        }
        bean.setAnnualizationFactor(swapLeg.getAnnualizationFactor());
        return bean;
      }

      @Override
      public SwapLegBean visitFixedInflationSwapLeg(FixedInflationSwapLeg swapLeg) {
        SwapLegBean bean = createSwapLegBean(swapLeg);
        bean.setRate(swapLeg.getRate());
        return bean;
      }

      @Override
      public SwapLegBean visitInflationIndexSwapLeg(InflationIndexSwapLeg swapLeg) {
        SwapLegBean bean = createSwapLegBean(swapLeg);
        bean.setRateIdentifier(externalIdToExternalIdBean(swapLeg.getIndexId()));
        bean.setActualIndexationLag(swapLeg.getQuotationIndexationLag());
        bean.setConventionalIndexationLag(swapLeg.getConventionalIndexationLag());
        bean.setIndexInterpolationMethod(swapLeg.getInterpolationMethod());
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
            bean.isEom(), bean.getRate());
      }

      @Override
      public SwapLeg visitFloatingInterestRateLeg(FloatingInterestRateLeg ignore) {
        FloatingInterestRateLeg floatingInterestRateLeg = new FloatingInterestRateLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEom(), 
            externalIdBeanToExternalId(bean.getRateIdentifier()),
            bean.getFloatingRateType());
        floatingInterestRateLeg.setInitialFloatingRate(bean.getRate());
        floatingInterestRateLeg.setSettlementDays(bean.getSettlementDays());
        floatingInterestRateLeg.setOffsetFixing(frequencyBeanToFrequency(bean.getOffsetFixing()));
        return floatingInterestRateLeg;
      }

      @Override
      public SwapLeg visitFloatingSpreadIRLeg(FloatingSpreadIRLeg ignore) {
        FloatingSpreadIRLeg floatingSpreadIRLeg = new FloatingSpreadIRLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEom(), 
            externalIdBeanToExternalId(bean.getRateIdentifier()),
            bean.getFloatingRateType(), bean.getSpread());
        floatingSpreadIRLeg.setInitialFloatingRate(bean.getRate());
        floatingSpreadIRLeg.setSettlementDays(bean.getSettlementDays());
        floatingSpreadIRLeg.setOffsetFixing(frequencyBeanToFrequency(bean.getOffsetFixing()));
        return floatingSpreadIRLeg;
      }

      @Override
      public SwapLeg visitFloatingGearingIRLeg(FloatingGearingIRLeg ignore) {
        FloatingGearingIRLeg floatingGearingIRLeg = new FloatingGearingIRLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEom(), 
            externalIdBeanToExternalId(bean.getRateIdentifier()),
            bean.getFloatingRateType(), bean.getGearing());
        floatingGearingIRLeg.setInitialFloatingRate(bean.getRate());
        floatingGearingIRLeg.setSettlementDays(bean.getSettlementDays());
        floatingGearingIRLeg.setOffsetFixing(frequencyBeanToFrequency(bean.getOffsetFixing()));
        return floatingGearingIRLeg;
      }

      @Override
      public SwapLeg visitFixedVarianceSwapLeg(FixedVarianceSwapLeg ignore) {
        return new FixedVarianceSwapLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEom(),
            bean.getStrike(),
            bean.getVarianceSwapType());
      }

      @Override
      public SwapLeg visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg ignore) {
        return new FloatingVarianceSwapLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEom(),
            externalIdBeanToExternalId(bean.getUnderlyingId()),
            frequencyBeanToFrequency(bean.getMonitoringFrequency()),
            bean.getAnnualizationFactor());
      }

      @Override
      public SwapLeg visitFixedInflationSwapLeg(FixedInflationSwapLeg swapLeg) {
        return new FixedInflationSwapLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEom(),
            bean.getRate());
      }

      @Override
      public SwapLeg visitInflationIndexSwapLeg(InflationIndexSwapLeg swapLeg) {
        return new InflationIndexSwapLeg(
            dayCountBeanToDayCount(bean.getDayCount()),
            frequencyBeanToFrequency(bean.getFrequency()),
            externalIdBeanToExternalId(bean.getRegion()),
            businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()),
            NotionalBeanOperation.createNotional(bean.getNotional()),
            bean.isEom(),
            externalIdBeanToExternalId(bean.getRateIdentifier()),
            bean.getActualIndexationLag(),
            bean.getConventionalIndexationLag(),
            bean.getIndexInterpolationMethod());
      }
    });
  }

}
