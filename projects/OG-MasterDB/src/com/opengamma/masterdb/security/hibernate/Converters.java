/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.util.Date;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.masterdb.security.hibernate.bond.YieldConventionBean;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Utility methods for simple conversions.
 */
public final class Converters {

  private Converters() {
  }

  public static Currency currencyBeanToCurrency(CurrencyBean currencyBean) {
    if (currencyBean == null) {
      return null;
    }
    return Currency.of(currencyBean.getName());
  }

  //-------------------------------------------------------------------------
  public static ExternalId externalIdBeanToExternalId(ExternalIdBean hibernateBean) {
    if (hibernateBean == null) {
      return null;
    }
    return ExternalId.of(hibernateBean.getScheme(), hibernateBean.getIdentifier());
  }

  public static ExternalIdBean externalIdToExternalIdBean(final ExternalId identifier) {
    return new ExternalIdBean(identifier.getScheme().getName(), identifier.getValue());
  }

  //-------------------------------------------------------------------------
  public static UniqueId uniqueIdBeanToUniqueId(UniqueIdBean hibernateBean) {
    if (hibernateBean == null) {
      return null;
    }
    return UniqueId.of(hibernateBean.getScheme(), hibernateBean.getIdentifier());
  }

  public static UniqueIdBean uniqueIdToUniqueIdBean(final UniqueId identifier) {
    return new UniqueIdBean(identifier.getScheme(), identifier.getValue());
  }

  //-------------------------------------------------------------------------
  public static Expiry expiryBeanToExpiry(final ExpiryBean bean) {
    if (bean == null) {
      return null;
    }
    ZonedDateTimeBean zonedDateTimeBean = bean.getExpiry();

    final long epochSeconds = zonedDateTimeBean.getDate().getTime() / 1000;
    ZonedDateTime zdt = null;
    if (zonedDateTimeBean.getZone() == null) {
      zdt = ZonedDateTime.ofEpochSeconds(epochSeconds, TimeZone.UTC);
    } else {
      zdt = ZonedDateTime.ofEpochSeconds(epochSeconds, TimeZone.of(zonedDateTimeBean.getZone()));
    }

    return new Expiry(zdt, bean.getAccuracy());
  }

  public static ExpiryBean expiryToExpiryBean(final Expiry expiry) {
    if (expiry == null) {
      return null;
    }
    final ExpiryBean bean = new ExpiryBean();
    
    final ZonedDateTimeBean zonedDateTimeBean = new ZonedDateTimeBean();
    zonedDateTimeBean.setDate(new Date(expiry.getExpiry().toInstant().toEpochMillisLong()));
    zonedDateTimeBean.setZone(expiry.getExpiry().getZone().getID());
    bean.setExpiry(zonedDateTimeBean);
    bean.setAccuracy(expiry.getAccuracy());
    return bean;
  }

  public static ZonedDateTime zonedDateTimeBeanToDateTimeWithZone(final ZonedDateTimeBean date) {
    if ((date == null) || (date.getDate() == null)) {
      return null;
    }
    final long epochSeconds = date.getDate().getTime() / 1000;
    if (date.getZone() == null) {
      return ZonedDateTime.ofEpochSeconds(epochSeconds, TimeZone.UTC);
    } else {
      return ZonedDateTime.ofEpochSeconds(epochSeconds, TimeZone.of(date.getZone()));
    }
  }

  public static ZonedDateTimeBean dateTimeWithZoneToZonedDateTimeBean(final ZonedDateTime zdt) {
    if (zdt == null) {
      return null;
    }
    final ZonedDateTimeBean bean = new ZonedDateTimeBean();
    bean.setDate(new Date(zdt.toInstant().toEpochMillisLong()));
    bean.setZone(zdt.getZone().getID());
    return bean;
  }

  public static Frequency frequencyBeanToFrequency(final FrequencyBean frequencyBean) {
    if (frequencyBean == null) {
      return null;
    }
    validateFrequency(frequencyBean.getName());
    return SimpleFrequencyFactory.INSTANCE.getFrequency(frequencyBean.getName());
  }

  public static void validateFrequency(final String name) {
    final Frequency f = SimpleFrequencyFactory.INSTANCE.getFrequency(name);
    if (f == null) {
      throw new OpenGammaRuntimeException("Bad value for frequency (" + name + ")");
    }
  }

  public static DayCount dayCountBeanToDayCount(final DayCountBean dayCountBean) {
    if (dayCountBean == null) {
      return null;
    }
    validateDayCount(dayCountBean.getName());
    return DayCountFactory.INSTANCE.getDayCount(dayCountBean.getName());
  }
  
  public static void validateDayCount(final String name) {
    final DayCount dc = DayCountFactory.INSTANCE.getDayCount(name);
    if (dc == null) {
      throw new OpenGammaRuntimeException("Bad value for dayCount (" + name + ")");
    }
  }

  public static BusinessDayConvention businessDayConventionBeanToBusinessDayConvention(final BusinessDayConventionBean businessDayConventionBean) {
    if (businessDayConventionBean == null) {
      return null;
    }
    validateBusinessDayConvention(businessDayConventionBean.getName());
    return BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(businessDayConventionBean.getName());
  }
  
  public static void validateBusinessDayConvention(final String name) {
    final BusinessDayConvention bdc = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(name);
    if (bdc == null) {
      throw new OpenGammaRuntimeException("Bad value for businessDayConvention (" + name + ")");
    }
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
  
  public static void validateYieldConvention(final String name) {
    final YieldConvention yc = YieldConventionFactory.INSTANCE.getYieldConvention(name);
    if (yc == null) {
      throw new OpenGammaRuntimeException("Bad value for yieldConvention (" + name + ")");
    }
  }

}
