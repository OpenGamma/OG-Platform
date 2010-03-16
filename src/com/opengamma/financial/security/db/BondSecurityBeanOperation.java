/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.CorporateBondSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.MunicipalBondSecurity;
import com.opengamma.id.DomainSpecificIdentifier;

/* package */ class BondSecurityBeanOperation extends Converters implements BeanOperation<BondSecurity,BondSecurityBean> {
  
  public static final BondSecurityBeanOperation INSTANCE = new BondSecurityBeanOperation ();
  
  private BondSecurityBeanOperation () {
  }
  
  @Override
  public BondSecurity createSecurity (final DomainSpecificIdentifier identifier, final BondSecurityBean bean) {
    return bean.getBondType ().accept (new BondType.Visitor<BondSecurity> () {

      @Override
      public BondSecurity visitCorporateBondType() {
        return new CorporateBondSecurity (
            dateToExpiry (bean.getMaturity ()),
            bean.getCoupon (),
            frequencyBeanToFrequency (bean.getFrequency ()),
            bean.getCountry (),
            bean.getCreditRating (),
            currencyBeanToCurrency (bean.getCurrency ()),
            bean.getIssuer (),
            dayCountBeanToDayCount (bean.getDayCountConvention ()),
            businessDayConventionBeanToBusinessDayConvention (bean.getBusinessDayConvention ())
          );
      }

      @Override
      public BondSecurity visitGovernmentBondType() {
        return new GovernmentBondSecurity (
            dateToExpiry (bean.getMaturity ()),
            bean.getCoupon (),
            frequencyBeanToFrequency (bean.getFrequency ()),
            bean.getCountry (),
            bean.getCreditRating (),
            currencyBeanToCurrency (bean.getCurrency ()),
            bean.getIssuer (),
            dayCountBeanToDayCount (bean.getDayCountConvention ()),
            businessDayConventionBeanToBusinessDayConvention (bean.getBusinessDayConvention ())
          );
      }

      @Override
      public BondSecurity visitMunicipalBondType() {
        return new MunicipalBondSecurity (
            dateToExpiry (bean.getMaturity ()),
            bean.getCoupon (),
            frequencyBeanToFrequency (bean.getFrequency ()),
            bean.getCountry (),
            bean.getCreditRating (),
            currencyBeanToCurrency (bean.getCurrency ()),
            bean.getIssuer (),
            dayCountBeanToDayCount (bean.getDayCountConvention ()),
            businessDayConventionBeanToBusinessDayConvention (bean.getBusinessDayConvention ())
          );
      }
      
    });
  }

  @Override
  public boolean beanEquals(BondSecurityBean bean, BondSecurity security) {
    return ObjectUtils.equals (bean.getBondType (), BondType.identify (security)) &&
        ObjectUtils.equals (dateToExpiry (bean.getMaturity ()), security.getMaturity ()) &&
        ObjectUtils.equals (bean.getCoupon (), security.getCoupon ()) &&
        ObjectUtils.equals (frequencyBeanToFrequency (bean.getFrequency ()), security.getFrequency ()) &&
        ObjectUtils.equals (bean.getCountry (), security.getCountry ()) &&
        ObjectUtils.equals (bean.getCreditRating (), security.getCreditRating ()) &&
        ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency ()), security.getCurrency ()) &&
        ObjectUtils.equals (bean.getIssuer (), security.getIssuer ()) &&
        ObjectUtils.equals (dayCountBeanToDayCount (bean.getDayCountConvention ()), security.getDayCountConvention ()) &&
        ObjectUtils.equals (businessDayConventionBeanToBusinessDayConvention (bean.getBusinessDayConvention ()), security.getBusinessDayConvention ());
  }

  @Override
  public BondSecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final BondSecurity security) {
    final BondSecurityBean bond = new BondSecurityBean();
    bond.setBondType (BondType.identify (security));
    bond.setMaturity (new Date (security.getMaturity ().toInstant ().toEpochMillisLong ()));
    bond.setCoupon (security.getCoupon ());
    bond.setFrequency (secMasterSession.getOrCreateFrequencyBean (security.getFrequency ().getConventionName ()));
    bond.setCountry (security.getCountry ());
    bond.setCreditRating (security.getCreditRating ());
    bond.setCurrency (secMasterSession.getOrCreateCurrencyBean (security.getCurrency ().getISOCode ()));
    bond.setIssuer (security.getIssuer ());
    bond.setDayCountConvention (secMasterSession.getOrCreateDayCountBean (security.getDayCountConvention ().getConventionName ()));
    bond.setBusinessDayConvention (secMasterSession.getOrCreateBusinessDayConventionBean (security.getBusinessDayConvention ().getConventionName ()));
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