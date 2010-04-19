/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.option.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.AmericanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaFutureOptionSecurity;
import com.opengamma.financial.security.option.ExchangeTradedOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FutureOptionSecurity;
import com.opengamma.financial.security.option.OTCOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionSecurityVisitor;
import com.opengamma.financial.security.option.PoweredEquityOptionSecurity;
import com.opengamma.id.Identifier;

/* package */ class OptionSecurityBeanOperation extends Converters implements BeanOperation<OptionSecurity,OptionSecurityBean> {
  
  public static final OptionSecurityBeanOperation INSTANCE = new OptionSecurityBeanOperation ();
  
  private OptionSecurityBeanOperation () {
  }
  
  @Override
  public OptionSecurity createSecurity (final Identifier identifier, final OptionSecurityBean bean) {
    return bean.getOptionSecurityType ().accept (new OptionSecurityType.Visitor<OptionSecurity> () {

      @Override
      public OptionSecurity visitAmericanEquityOptionType() {
        return new AmericanVanillaEquityOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, bean.getUnderlyingIdentityKey()),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getExchange ().getName ()
            );
      }

      @Override
      public OptionSecurity visitEuropeanEquityOptionType() {
        return new EuropeanVanillaEquityOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, bean.getUnderlyingIdentityKey()),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getExchange ().getName ()
            );
      }

      @Override
      public OptionSecurity visitPoweredEquityOptionType() {
        return new PoweredEquityOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            bean.getPower (),
            new Identifier(Security.SECURITY_IDENTITY_KEY_DOMAIN, bean.getUnderlyingIdentityKey()),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getExchange ().getName ()
            );
      }

      @Override
      public OptionSecurity visitAmericanFutureOptionType() {
        return new AmericanVanillaFutureOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            new Identifier (Security.SECURITY_IDENTITY_KEY_DOMAIN, bean.getUnderlyingIdentityKey ()),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getExchange ().getName (),
            bean.isMargined ());
      }

      @Override
      public OptionSecurity visitEuropeanFutureOptionType() {
        return new EuropeanVanillaFutureOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            new Identifier (Security.SECURITY_IDENTITY_KEY_DOMAIN, bean.getUnderlyingIdentityKey ()),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getExchange ().getName (),
            bean.isMargined ());
      }

      @Override
      public OptionSecurity visitFXOptionType() {
        return new FXOptionSecurity (
            bean.getOptionType (),
            bean.getStrike (),
            dateToExpiry (bean.getExpiry ()),
            new Identifier (Security.SECURITY_IDENTITY_KEY_DOMAIN, bean.getUnderlyingIdentityKey ()),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getCounterparty (),
            currencyBeanToCurrency (bean.getCurrency2 ()),
            currencyBeanToCurrency (bean.getCurrency3 ()));
      }
    });
  }

  @Override
  public boolean beanEquals(final OptionSecurityBean bean, final OptionSecurity security) {
    return security.accept (new OptionSecurityVisitor<Boolean> () {
      
      private Boolean beanEquals (final OptionSecurity security) {
        return
          ObjectUtils.equals(bean.getOptionSecurityType (), OptionSecurityType.identify (security)) &&
          ObjectUtils.equals(bean.getOptionType (), security.getOptionType ()) &&
          ObjectUtils.equals(bean.getStrike (), security.getStrike ()) &&
          ObjectUtils.equals(dateToExpiry (bean.getExpiry ()), security.getExpiry ()) &&
          ObjectUtils.equals(bean.getUnderlyingIdentityKey (), security.getUnderlyingIdentityKey ()) &&
          ObjectUtils.equals(currencyBeanToCurrency (bean.getCurrency1 ()), security.getCurrency ());
      }
      
      private Boolean beanEquals (final ExchangeTradedOptionSecurity security) {
        return beanEquals ((OptionSecurity)security) &&
          ObjectUtils.equals (bean.getExchange ().getName (), security.getExchange ());
      }
      
      private Boolean beanEquals (final FutureOptionSecurity security) {
        return beanEquals ((ExchangeTradedOptionSecurity)security) &&
          ObjectUtils.equals (bean.isMargined (), security.isMargined ());
      }
      
      private Boolean beanEquals (final OTCOptionSecurity security) {
        return beanEquals ((OptionSecurity)security) &&
          ObjectUtils.equals (bean.getCounterparty (), security.getCounterparty ());
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

      @Override
      public Boolean visitAmericanVanillaFutureOptionSecurity(
          AmericanVanillaFutureOptionSecurity security) {
        if (!beanEquals (security)) return false;
        return true;
      }

      @Override
      public Boolean visitEuropeanVanillaFutureOptionSecurity(
          EuropeanVanillaFutureOptionSecurity security) {
        if (!beanEquals (security)) return false;
        return true;
      }

      @Override
      public Boolean visitFXOptionSecurity(FXOptionSecurity security) {
        if (!beanEquals (security)) return false;
        return
          ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency2 ()), security.getPutCurrency ()) &&
          ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency3 ()), security.getCallCurrency ());
      }
    });
  }

  @Override
  public OptionSecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final OptionSecurity security) {
    return security.accept (new OptionSecurityVisitor<OptionSecurityBean> () {
      
      private OptionSecurityBean createSecurityBean (final OptionSecurity security) {
        final OptionSecurityBean bean = new OptionSecurityBean();
        bean.setOptionSecurityType(OptionSecurityType.identify (security));
        bean.setOptionType(security.getOptionType ());
        bean.setStrike(security.getStrike ());
        bean.setExpiry(new Date (security.getExpiry ().toInstant ().toEpochMillisLong ()));
        bean.setUnderlyingIdentityKey(security.getUnderlyingIdentityKey().getValue());
        bean.setCurrency1(secMasterSession.getOrCreateCurrencyBean (security.getCurrency ().getISOCode ()));
        return bean;
      }
      
      private OptionSecurityBean createSecurityBean (final ExchangeTradedOptionSecurity security) {
        final OptionSecurityBean bean = createSecurityBean ((OptionSecurity)security);
        bean.setExchange(secMasterSession.getOrCreateExchangeBean (security.getExchange (), ""));
        return bean;
      }
      
      private OptionSecurityBean createSecurityBean (final EquityOptionSecurity security) {
        final OptionSecurityBean bean = createSecurityBean ((ExchangeTradedOptionSecurity)security);
        return bean;
      }
      
      private OptionSecurityBean createSecurityBean (final FutureOptionSecurity security) {
        final OptionSecurityBean bean = createSecurityBean ((ExchangeTradedOptionSecurity)security);
        bean.setMargined (security.isMargined ());
        return bean;
      }
      
      private OptionSecurityBean createSecurityBean (final OTCOptionSecurity security) {
        final OptionSecurityBean bean = createSecurityBean ((OptionSecurity)security);
        bean.setCounterparty (security.getCounterparty ());
        return bean;
      }

      @Override
      public OptionSecurityBean visitAmericanVanillaEquityOptionSecurity(
          AmericanVanillaEquityOptionSecurity security) {
        return createSecurityBean (security);
      }

      @Override
      public OptionSecurityBean visitEuropeanVanillaEquityOptionSecurity(
          EuropeanVanillaEquityOptionSecurity security) {
        return createSecurityBean (security);
      }

      @Override
      public OptionSecurityBean visitPoweredEquityOptionSecurity(
          PoweredEquityOptionSecurity security) {
        final OptionSecurityBean bean = createSecurityBean (security);
        bean.setPower (security.getPower ());
        return bean;
      }

      @Override
      public OptionSecurityBean visitAmericanVanillaFutureOptionSecurity(
          AmericanVanillaFutureOptionSecurity security) {
        return createSecurityBean (security);
      }

      @Override
      public OptionSecurityBean visitEuropeanVanillaFutureOptionSecurity(
          EuropeanVanillaFutureOptionSecurity security) {
        return createSecurityBean (security);
      }

      @Override
      public OptionSecurityBean visitFXOptionSecurity(FXOptionSecurity security) {
        final OptionSecurityBean bean = createSecurityBean (security);
        bean.setCurrency2 (secMasterSession.getOrCreateCurrencyBean (security.getPutCurrency ().getISOCode ()));
        bean.setCurrency3 (secMasterSession.getOrCreateCurrencyBean (security.getCallCurrency ().getISOCode ()));
        return bean;
      }
      
    });
  }
  
  @Override
  public Class<? extends OptionSecurityBean> getBeanClass() {
    return OptionSecurityBean.class;
  }

  @Override
  public Class<? extends OptionSecurity> getSecurityClass() {
    return OptionSecurity.class;
  }

  @Override
  public String getSecurityType() {
    return "OPTION";
  }
  
}