/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.EquityOptionSecurityVisitor;
import com.opengamma.financial.security.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.PoweredEquityOptionSecurity;
import com.opengamma.id.DomainSpecificIdentifier;

/* package */ class EquityOptionSecurityBeanOperation extends Converters implements BeanOperation<EquityOptionSecurity,EquityOptionSecurityBean> {
  
  public static final EquityOptionSecurityBeanOperation INSTANCE = new EquityOptionSecurityBeanOperation ();
  
  private EquityOptionSecurityBeanOperation () {
  }
  
  @Override
  public EquityOptionSecurity createSecurity (final DomainSpecificIdentifier identifier, final EquityOptionSecurityBean bean) {
    return bean.getEquityOptionType ().accept (new EquityOptionType.Visitor<EquityOptionSecurity> () {

      @Override
      public EquityOptionSecurity visitAmericanEquityOptionType() {
        return new AmericanVanillaEquityOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            bean.getUnderlyingIdentityKey (),
            currencyBeanToCurrency (bean.getCurrency ()),
            bean.getExchange ().getName ()
            );
      }

      @Override
      public EquityOptionSecurity visitEuropeanEquityOptionType() {
        return new EuropeanVanillaEquityOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            bean.getUnderlyingIdentityKey (),
            currencyBeanToCurrency (bean.getCurrency ()),
            bean.getExchange ().getName ()
            );
      }

      @Override
      public EquityOptionSecurity visitPoweredEquityOptionType() {
        return new PoweredEquityOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            bean.getPower (),
            bean.getUnderlyingIdentityKey (),
            currencyBeanToCurrency (bean.getCurrency ()),
            bean.getExchange ().getName ()
            );
      }
    });
  }

  @Override
  public boolean beanEquals(final EquityOptionSecurityBean bean, final EquityOptionSecurity security) {
    return security.accept (new EquityOptionSecurityVisitor<Boolean> () {
      
      public Boolean beanEquals (final EquityOptionSecurity security) {
        return
        ObjectUtils.equals(bean.getEquityOptionType (), EquityOptionType.identify (security)) &&
        ObjectUtils.equals(bean.getOptionType (), security.getOptionType ()) &&
        ObjectUtils.equals(bean.getStrike (), security.getStrike ()) &&
        ObjectUtils.equals(dateToExpiry (bean.getExpiry ()), security.getExpiry ()) &&
        ObjectUtils.equals(bean.getUnderlyingIdentityKey (), security.getUnderlyingIdentityKey ()) &&
        ObjectUtils.equals(currencyBeanToCurrency (bean.getCurrency ()), security.getCurrency ()) &&
        ObjectUtils.equals(bean.getExchange ().getName (), security.getExchange ());
      }

      @Override
      public Boolean visitAmericanVanillaEquityOptionSecurity(final AmericanVanillaEquityOptionSecurity security) {
        if (!beanEquals (security)) return false;
        return true;
      }

      @Override
      public Boolean visitEuropeanVanillaEquityOptionSecurity(final EuropeanVanillaEquityOptionSecurity security) {
        if (!beanEquals (security)) return false;
        return true;
      }

      @Override
      public Boolean visitPoweredEquityOptionSecurity(final PoweredEquityOptionSecurity security) {
        if (!beanEquals (security)) return false;
        if (bean.getPower () != security.getPower ()) return false;
        return true;
      }
    });
  }

  @Override
  public EquityOptionSecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final EquityOptionSecurity security) {
    return security.accept (new EquityOptionSecurityVisitor<EquityOptionSecurityBean> () {
      
      private EquityOptionSecurityBean createBasicBean (final EquityOptionSecurity security) {
        final EquityOptionSecurityBean equityOption = new EquityOptionSecurityBean();
        equityOption.setEquityOptionType(EquityOptionType.identify (security));
        equityOption.setOptionType(security.getOptionType ());
        equityOption.setStrike(security.getStrike ());
        equityOption.setExpiry(new Date (security.getExpiry ().toInstant ().toEpochMillis ()));
        equityOption.setUnderlyingIdentityKey(security.getUnderlyingIdentityKey ());
        equityOption.setCurrency(secMasterSession.getOrCreateCurrencyBean (security.getCurrency ().getISOCode ()));
        equityOption.setExchange(secMasterSession.getOrCreateExchangeBean (security.getExchange (), ""));
        return equityOption;
      }

      @Override
      public EquityOptionSecurityBean visitAmericanVanillaEquityOptionSecurity(
          AmericanVanillaEquityOptionSecurity security) {
        return createBasicBean (security);
      }

      @Override
      public EquityOptionSecurityBean visitEuropeanVanillaEquityOptionSecurity(
          EuropeanVanillaEquityOptionSecurity security) {
        return createBasicBean (security);
      }

      @Override
      public EquityOptionSecurityBean visitPoweredEquityOptionSecurity(
          PoweredEquityOptionSecurity security) {
        final EquityOptionSecurityBean equityOption = createBasicBean (security);
        equityOption.setPower (security.getPower ());
        return equityOption;
      }
      
    });
  }
  
  @Override
  public Class<? extends EquityOptionSecurityBean> getBeanClass() {
    return EquityOptionSecurityBean.class;
  }

  @Override
  public Class<? extends EquityOptionSecurity> getSecurityClass() {
    return EquityOptionSecurity.class;
  }

  @Override
  public String getSecurityType() {
    return "EQUITYOPTION";
  }
  
}