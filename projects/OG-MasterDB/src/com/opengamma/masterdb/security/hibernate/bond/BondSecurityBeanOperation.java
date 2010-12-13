/**
 * Copyright(C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.bond;

import static com.opengamma.masterdb.security.hibernate.Converters.businessDayConventionBeanToBusinessDayConvention;
import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Operation for working on a bond security.
 */
public final class BondSecurityBeanOperation extends AbstractSecurityBeanOperation<BondSecurity, BondSecurityBean> {

  /**
   * Singleton.
   */
  public static final BondSecurityBeanOperation INSTANCE = new BondSecurityBeanOperation();

  private BondSecurityBeanOperation() {
    super("BOND", BondSecurity.class, BondSecurityBean.class);
  }

  public static YieldConvention yieldConventionBeanToYieldConvention(final YieldConventionBean yieldConventionBean) {
    if (yieldConventionBean == null) {
      return null;
    }
    final YieldConvention yc = YieldConventionFactory.INSTANCE.getYieldConvention(yieldConventionBean.getName());
    if (yc == null) {
      throw new OpenGammaRuntimeException("Bad value for yieldConventionBean (" + yieldConventionBean.getName() + ")");
    }
    return yc;
  }

  @Override
  public BondSecurity createSecurity(final OperationContext context, final BondSecurityBean bean) {
    return bean.getBondType().accept(new BondSecurityVisitor<BondSecurity>() {

      @Override
      public BondSecurity visitCorporateBondSecurity(CorporateBondSecurity bond) {
        BondSecurity bondSecurity = new CorporateBondSecurity(bean.getIssuerName(), bean.getIssuerType().getName(), bean.getIssuerDomicile(), bean.getMarket().getName(),
            currencyBeanToCurrency(bean.getCurrency()), yieldConventionBeanToYieldConvention(bean.getYieldConvention()),
            expiryBeanToExpiry(bean.getMaturity()), bean.getCouponType().getName(), bean.getCouponRate(),
            frequencyBeanToFrequency(bean.getCouponFrequency()), dayCountBeanToDayCount(bean.getDayCountConvention()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getInterestAccrualDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getFirstCouponDate()),
            bean.getIssuancePrice(), bean.getTotalAmountIssued(), bean.getMinimumAmount(), bean.getMinimumIncrement(),
            bean.getParAmount(), bean.getRedemptionValue());
        bondSecurity.setBusinessDayConvention(businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()));
        bondSecurity.setAnnouncementDate(zonedDateTimeBeanToDateTimeWithZone(bean.getAnnouncementDate()));
        bondSecurity.setGuaranteeType(bean.getGuaranteeType() != null ? bean.getGuaranteeType().getName() : null);
        return bondSecurity;
      }

      @Override
      public BondSecurity visitGovernmentBondSecurity(GovernmentBondSecurity bond) {
        BondSecurity bondSecurity = new GovernmentBondSecurity(bean.getIssuerName(), bean.getIssuerType().getName(), bean.getIssuerDomicile(),
            bean.getMarket().getName(), currencyBeanToCurrency(bean.getCurrency()),
            yieldConventionBeanToYieldConvention(bean.getYieldConvention()), expiryBeanToExpiry(bean.getMaturity()),
            bean.getCouponType().getName(), bean.getCouponRate(), frequencyBeanToFrequency(bean.getCouponFrequency()),
            dayCountBeanToDayCount(bean.getDayCountConvention()), zonedDateTimeBeanToDateTimeWithZone(bean.getInterestAccrualDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getFirstCouponDate()), bean.getIssuancePrice(), bean.getTotalAmountIssued(),
            bean.getMinimumAmount(), bean.getMinimumIncrement(), bean.getParAmount(), bean.getRedemptionValue());
        bondSecurity.setBusinessDayConvention(businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()));
        bondSecurity.setAnnouncementDate(zonedDateTimeBeanToDateTimeWithZone(bean.getAnnouncementDate()));
        bondSecurity.setGuaranteeType(bean.getGuaranteeType() != null ? bean.getGuaranteeType().getName() : null);
        return bondSecurity;
      }

      @Override
      public BondSecurity visitMunicipalBondSecurity(MunicipalBondSecurity bond) {
        BondSecurity bondSecurity = new MunicipalBondSecurity(bean.getIssuerName(), bean.getIssuerType().getName(), bean.getIssuerDomicile(),
            bean.getMarket().getName(), currencyBeanToCurrency(bean.getCurrency()),
            yieldConventionBeanToYieldConvention(bean.getYieldConvention()), expiryBeanToExpiry(bean.getMaturity()),
            bean.getCouponType().getName(), bean.getCouponRate(), frequencyBeanToFrequency(bean.getCouponFrequency()),
            dayCountBeanToDayCount(bean.getDayCountConvention()), zonedDateTimeBeanToDateTimeWithZone(bean.getInterestAccrualDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getFirstCouponDate()),
            bean.getIssuancePrice(), bean.getTotalAmountIssued(), bean.getMinimumAmount(), bean.getMinimumIncrement(),
            bean.getParAmount(), bean.getRedemptionValue());
        bondSecurity.setBusinessDayConvention(businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()));
        bondSecurity.setAnnouncementDate(zonedDateTimeBeanToDateTimeWithZone(bean.getAnnouncementDate()));
        bondSecurity.setGuaranteeType(bean.getGuaranteeType() != null ? bean.getGuaranteeType().getName() : null);
        return bondSecurity;
      }

    });
  }

  @Override
  public boolean beanEquals(final OperationContext context, BondSecurityBean bean, BondSecurity security) {
    return ObjectUtils.equals(bean.getBondType(), BondType.identify(security)) && ObjectUtils.equals(bean.getIssuerName(), security.getIssuerName())
        && ObjectUtils.equals(bean.getIssuerType().getName(), security.getIssuerType()) && ObjectUtils.equals(bean.getIssuerDomicile(), security.getIssuerDomicile())
        && ObjectUtils.equals(bean.getMarket().getName(), security.getMarket()) && ObjectUtils.equals(currencyBeanToCurrency(bean.getCurrency()), security.getCurrency())
        && ObjectUtils.equals(yieldConventionBeanToYieldConvention(bean.getYieldConvention()), security.getYieldConvention())
        && ObjectUtils.equals(bean.getGuaranteeType() != null ? bean.getGuaranteeType().getName() : null, security.getGuaranteeType())
        && ObjectUtils.equals(expiryBeanToExpiry(bean.getMaturity()), security.getMaturity())
        && ObjectUtils.equals(bean.getCouponType().getName(), security.getCouponType()) && ObjectUtils.equals(bean.getCouponRate(), security.getCouponRate())
        && ObjectUtils.equals(frequencyBeanToFrequency(bean.getCouponFrequency()), security.getCouponFrequency())
        && ObjectUtils.equals(dayCountBeanToDayCount(bean.getDayCountConvention()), security.getDayCountConvention())
        && ObjectUtils.equals(businessDayConventionBeanToBusinessDayConvention(bean.getBusinessDayConvention()), security.getBusinessDayConvention())
        && ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getAnnouncementDate()), security.getAnnouncementDate())
        && ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getInterestAccrualDate()), security.getInterestAccrualDate())
        && ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate()), security.getSettlementDate())
        && ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getFirstCouponDate()), security.getFirstCouponDate())
        && ObjectUtils.equals(bean.getIssuancePrice(), security.getIssuancePrice()) && ObjectUtils.equals(bean.getTotalAmountIssued(), security.getTotalAmountIssued())
        && ObjectUtils.equals(bean.getMinimumAmount(), security.getMinimumAmount()) && ObjectUtils.equals(bean.getMinimumIncrement(), security.getMinimumIncrement())
        && ObjectUtils.equals(bean.getParAmount(), security.getParAmount()) && ObjectUtils.equals(bean.getRedemptionValue(), security.getRedemptionValue());
  }

  @Override
  public BondSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final BondSecurity security) {
    final BondSecurityBean bond = new BondSecurityBean();
    bond.setBondType(BondType.identify(security));
    bond.setIssuerName(security.getIssuerName());
    bond.setIssuerType(secMasterSession.getOrCreateIssuerTypeBean(security.getIssuerType()));
    bond.setIssuerDomicile(security.getIssuerDomicile());
    bond.setMarket(secMasterSession.getOrCreateMarketBean(security.getMarket()));
    bond.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getISOCode()));
    bond.setYieldConvention(secMasterSession.getOrCreateYieldConventionBean(security.getYieldConvention().getConventionName()));
    bond.setGuaranteeType(security.getGuaranteeType() != null ? secMasterSession.getOrCreateGuaranteeTypeBean(security.getGuaranteeType()) : null);
    bond.setMaturity(expiryToExpiryBean(security.getMaturity()));
    bond.setCouponType(secMasterSession.getOrCreateCouponTypeBean(security.getCouponType()));
    bond.setCouponRate(security.getCouponRate());
    bond.setCouponFrequency(secMasterSession.getOrCreateFrequencyBean(security.getCouponFrequency().getConventionName()));
    bond.setDayCountConvention(secMasterSession.getOrCreateDayCountBean(security.getDayCountConvention().getConventionName()));
    bond.setBusinessDayConvention(security.getBusinessDayConvention() != null ? secMasterSession.getOrCreateBusinessDayConventionBean(security.getBusinessDayConvention().getConventionName()) : null);
    bond.setAnnouncementDate(security.getAnnouncementDate() != null ? dateTimeWithZoneToZonedDateTimeBean(security.getAnnouncementDate()) : null);
    bond.setInterestAccrualDate(dateTimeWithZoneToZonedDateTimeBean(security.getInterestAccrualDate()));
    bond.setSettlementDate(dateTimeWithZoneToZonedDateTimeBean(security.getSettlementDate()));
    bond.setFirstCouponDate(dateTimeWithZoneToZonedDateTimeBean(security.getFirstCouponDate()));
    bond.setIssuancePrice(security.getIssuancePrice());
    bond.setTotalAmountIssued(security.getTotalAmountIssued());
    bond.setMinimumAmount(security.getMinimumAmount());
    bond.setMinimumIncrement(security.getMinimumIncrement());
    bond.setParAmount(security.getParAmount());
    bond.setRedemptionValue(security.getRedemptionValue());
    return bond;
  }

}
