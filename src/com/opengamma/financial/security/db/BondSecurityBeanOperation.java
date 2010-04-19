/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.CorporateBondSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.MunicipalBondSecurity;
import com.opengamma.id.Identifier;

/* package */ class BondSecurityBeanOperation extends Converters implements BeanOperation<BondSecurity,BondSecurityBean> {
  
  public static final BondSecurityBeanOperation INSTANCE = new BondSecurityBeanOperation ();
  
  private BondSecurityBeanOperation () {
  }
  
  @Override
  public BondSecurity createSecurity (final Identifier identifier, final BondSecurityBean bean) {
    return bean.getBondType ().accept (new BondType.Visitor<BondSecurity> () {

      @Override
      public BondSecurity visitCorporateBondType() {
        return new CorporateBondSecurity (
            bean.getIssuerName (),
            bean.getIssuerType ().getName (),
            bean.getIssuerDomicile (),
            bean.getMarket ().getName (),
            currencyBeanToCurrency (bean.getCurrency ()),
            yieldConventionBeanToYieldConvention (bean.getYieldConvention ()),
            bean.getGuaranteeType ().getName (),
            dateToExpiry (bean.getMaturity ()),
            bean.getCouponType ().getName (),
            bean.getCouponRate (),
            frequencyBeanToFrequency (bean.getCouponFrequency ()),
            dayCountBeanToDayCount (bean.getDayCountConvention ()),
            businessDayConventionBeanToBusinessDayConvention (bean.getBusinessDayConvention ()),
            dateToLocalDate (bean.getAnnouncementDate ()),
            dateToLocalDate (bean.getInterestAccrualDate ()),
            dateToLocalDate (bean.getSettlementDate ()),
            dateToLocalDate (bean.getFirstCouponDate ()),
            bean.getIssuancePrice (),
            bean.getTotalAmountIssued (),
            bean.getMinimumAmount (),
            bean.getMinimumIncrement (),
            bean.getParAmount (),
            bean.getRedemptionValue ());
      }

      @Override
      public BondSecurity visitGovernmentBondType() {
        return new GovernmentBondSecurity (
            bean.getIssuerName (),
            bean.getIssuerType ().getName (),
            bean.getIssuerDomicile (),
            bean.getMarket ().getName (),
            currencyBeanToCurrency (bean.getCurrency ()),
            yieldConventionBeanToYieldConvention (bean.getYieldConvention ()),
            bean.getGuaranteeType ().getName (),
            dateToExpiry (bean.getMaturity ()),
            bean.getCouponType ().getName (),
            bean.getCouponRate (),
            frequencyBeanToFrequency (bean.getCouponFrequency ()),
            dayCountBeanToDayCount (bean.getDayCountConvention ()),
            businessDayConventionBeanToBusinessDayConvention (bean.getBusinessDayConvention ()),
            dateToLocalDate (bean.getAnnouncementDate ()),
            dateToLocalDate (bean.getInterestAccrualDate ()),
            dateToLocalDate (bean.getSettlementDate ()),
            dateToLocalDate (bean.getFirstCouponDate ()),
            bean.getIssuancePrice (),
            bean.getTotalAmountIssued (),
            bean.getMinimumAmount (),
            bean.getMinimumIncrement (),
            bean.getParAmount (),
            bean.getRedemptionValue ());
      }

      @Override
      public BondSecurity visitMunicipalBondType() {
        return new MunicipalBondSecurity (
            bean.getIssuerName (),
            bean.getIssuerType ().getName (),
            bean.getIssuerDomicile (),
            bean.getMarket ().getName (),
            currencyBeanToCurrency (bean.getCurrency ()),
            yieldConventionBeanToYieldConvention (bean.getYieldConvention ()),
            bean.getGuaranteeType ().getName (),
            dateToExpiry (bean.getMaturity ()),
            bean.getCouponType ().getName (),
            bean.getCouponRate (),
            frequencyBeanToFrequency (bean.getCouponFrequency ()),
            dayCountBeanToDayCount (bean.getDayCountConvention ()),
            businessDayConventionBeanToBusinessDayConvention (bean.getBusinessDayConvention ()),
            dateToLocalDate (bean.getAnnouncementDate ()),
            dateToLocalDate (bean.getInterestAccrualDate ()),
            dateToLocalDate (bean.getSettlementDate ()),
            dateToLocalDate (bean.getFirstCouponDate ()),
            bean.getIssuancePrice (),
            bean.getTotalAmountIssued (),
            bean.getMinimumAmount (),
            bean.getMinimumIncrement (),
            bean.getParAmount (),
            bean.getRedemptionValue ());
      }
      
    });
  }

  @Override
  public boolean beanEquals(BondSecurityBean bean, BondSecurity security) {
    return
        ObjectUtils.equals (bean.getBondType (), BondType.identify (security)) &&
        ObjectUtils.equals (bean.getIssuerName (), security.getIssuerName ()) &&
        ObjectUtils.equals (bean.getIssuerType ().getName (), security.getIssuerType ()) &&
        ObjectUtils.equals (bean.getIssuerDomicile (), security.getIssuerDomicile ()) &&
        ObjectUtils.equals (bean.getMarket ().getName (), security.getMarket ()) &&
        ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency ()), security.getCurrency ()) &&
        ObjectUtils.equals (yieldConventionBeanToYieldConvention (bean.getYieldConvention ()), security.getYieldConvention ()) &&
        ObjectUtils.equals (bean.getGuaranteeType ().getName (), security.getGuaranteeType ()) &&
        ObjectUtils.equals (dateToExpiry (bean.getMaturity ()), security.getMaturity ()) &&
        ObjectUtils.equals (bean.getCouponType ().getName (), security.getCouponType ()) &&
        ObjectUtils.equals (bean.getCouponRate (), security.getCouponRate ()) &&
        ObjectUtils.equals (frequencyBeanToFrequency (bean.getCouponFrequency ()), security.getCouponFrequency ()) &&
        ObjectUtils.equals (dayCountBeanToDayCount (bean.getDayCountConvention ()), security.getDayCountConvention ()) &&
        ObjectUtils.equals (businessDayConventionBeanToBusinessDayConvention (bean.getBusinessDayConvention ()), security.getBusinessDayConvention ()) &&
        ObjectUtils.equals (dateToLocalDate (bean.getAnnouncementDate ()), security.getAnnouncementDate ()) &&
        ObjectUtils.equals (dateToLocalDate (bean.getInterestAccrualDate ()), security.getInterestAccrualDate ()) &&
        ObjectUtils.equals (dateToLocalDate (bean.getSettlementDate ()), security.getSettlementDate ()) &&
        ObjectUtils.equals (dateToLocalDate (bean.getFirstCouponDate ()), security.getFirstCouponDate ()) &&
        ObjectUtils.equals (bean.getIssuancePrice (), security.getIssuancePrice ()) &&
        ObjectUtils.equals (bean.getTotalAmountIssued (), security.getTotalAmountIssued ()) &&
        ObjectUtils.equals (bean.getMinimumAmount (), security.getMinimumAmount ()) &&
        ObjectUtils.equals (bean.getMinimumIncrement (), security.getMinimumIncrement ()) &&
        ObjectUtils.equals (bean.getParAmount (), security.getParAmount ()) &&
        ObjectUtils.equals (bean.getRedemptionValue (), security.getRedemptionValue ());
  }

  @Override
  public BondSecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final BondSecurity security) {
    final BondSecurityBean bond = new BondSecurityBean();
    bond.setBondType (BondType.identify (security));
    bond.setIssuerName (security.getIssuerName ());
    bond.setIssuerType (secMasterSession.getOrCreateIssuerTypeBean (security.getIssuerType ()));
    bond.setIssuerDomicile (security.getIssuerDomicile ());
    bond.setMarket (secMasterSession.getOrCreateMarketBean (security.getMarket ()));
    bond.setCurrency (secMasterSession.getOrCreateCurrencyBean (security.getCurrency ().getISOCode ()));
    bond.setYieldConvention (secMasterSession.getOrCreateYieldConventionBean (security.getYieldConvention ().getConventionName ()));
    bond.setGuaranteeType (secMasterSession.getOrCreateGuaranteeTypeBean (security.getGuaranteeType ()));
    bond.setMaturity (expiryToDate (security.getMaturity ()));
    bond.setCouponType (secMasterSession.getOrCreateCouponTypeBean (security.getCouponType ()));
    bond.setCouponRate (security.getCouponRate ());
    bond.setCouponFrequency (secMasterSession.getOrCreateFrequencyBean (security.getCouponFrequency ().getConventionName ()));
    bond.setDayCountConvention (secMasterSession.getOrCreateDayCountBean (security.getDayCountConvention ().getConventionName ()));
    bond.setBusinessDayConvention (secMasterSession.getOrCreateBusinessDayConventionBean (security.getBusinessDayConvention ().getConventionName ()));
    bond.setAnnouncementDate (localDateToDate (security.getAnnouncementDate ()));
    bond.setInterestAccrualDate (localDateToDate (security.getInterestAccrualDate ()));
    bond.setSettlementDate (localDateToDate (security.getSettlementDate ()));
    bond.setFirstCouponDate (localDateToDate (security.getFirstCouponDate ()));
    bond.setIssuancePrice (security.getIssuancePrice ());
    bond.setTotalAmountIssued (security.getTotalAmountIssued ());
    bond.setMinimumAmount (security.getMinimumAmount ());
    bond.setMinimumIncrement (security.getMinimumIncrement ());
    bond.setParAmount (security.getParAmount ());
    bond.setRedemptionValue (security.getRedemptionValue ());
    return bond;
  }

  @Override
  public Class<? extends BondSecurityBean> getBeanClass() {
    return BondSecurityBean.class;
  }

  @Override
  public Class<? extends BondSecurity> getSecurityClass() {
    return BondSecurity.class;
  }

  @Override
  public String getSecurityType() {
    return "BOND";
  }
}