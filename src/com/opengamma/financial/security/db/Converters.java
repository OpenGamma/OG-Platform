/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Calendar;
import java.util.Date;

import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.Currency;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.DateTimeWithZone;
import com.opengamma.id.Identifier;
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
    return Currency.getInstance(currencyBean.getName());
  }

  public static Identifier identifierBeanToIdentifier(IdentifierBean identifierBean) {
    if (identifierBean == null) {
      return null;
    }
    return Identifier.of(identifierBean.getScheme(), identifierBean.getIdentifier());
  }

  public static IdentifierBean identifierToIdentifierBean(final Identifier identifier) {
    return new IdentifierBean(identifier.getScheme().getName(), identifier.getValue());
  }

  public static Expiry expiryBeanToExpiry(final ExpiryBean bean) {
    if (bean == null) {
      return null;
    }
    final Calendar c = Calendar.getInstance();
    c.setTime(bean.getDate());
    // TODO: timezone ?
    return new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), ZoneOffset.UTC), TimeZone.UTC), bean
        .getAccuracy());
  }

  public static ExpiryBean expiryToExpiryBean(final Expiry expiry) {
    if (expiry == null) {
      return null;
    }
    final ExpiryBean bean = new ExpiryBean();
    bean.setDate(new Date(expiry.toInstant().toEpochMillisLong()));
    bean.setAccuracy(expiry.getAccuracy());
    return bean;
  }

  public static DateTimeWithZone zonedDateTimeBeanToDateTimeWithZone(final ZonedDateTimeBean date) {
    if ((date == null) || (date.getDate() == null)) {
      return null;
    }
    final long epochSeconds = date.getDate().getTime() / 1000;
    if (date.getZone() == null) {
      return new DateTimeWithZone(ZonedDateTime.ofEpochSeconds(epochSeconds, TimeZone.UTC));
    } else {
      final ZonedDateTime zdt = ZonedDateTime.ofEpochSeconds(epochSeconds, TimeZone.of(date.getZone()));
      return new DateTimeWithZone(zdt, zdt.getZone().getID());
    }
  }

  public static ZonedDateTimeBean dateTimeWithZoneToZonedDateTimeBean(final DateTimeWithZone date) {
    if (date == null) {
      return null;
    }
    final ZonedDateTimeBean bean = new ZonedDateTimeBean();
    final ZonedDateTime zdt = ZonedDateTime.of(date.getDate(), TimeZone.of(date.getZone()));
    bean.setDate(new Date(zdt.toInstant().toEpochMillisLong()));
    bean.setZone(zdt.getZone().getID());
    return bean;
  }

  public static Frequency frequencyBeanToFrequency(final FrequencyBean frequencyBean) {
    if (frequencyBean == null) {
      return null;
    }
    final Frequency f = SimpleFrequencyFactory.INSTANCE.getFrequency(frequencyBean.getName());
    if (f == null) {
      throw new OpenGammaRuntimeException("Bad value for frequencyBean (" + frequencyBean.getName() + ")");
    }
    return f;
  }

  public static DayCount dayCountBeanToDayCount(final DayCountBean dayCountBean) {
    if (dayCountBean == null) {
      return null;
    }
    final DayCount dc = DayCountFactory.INSTANCE.getDayCount(dayCountBean.getName());
    if (dc == null) {
      throw new OpenGammaRuntimeException("Bad value for dayCountBean (" + dayCountBean.getName() + ")");
    }
    return dc;
  }

  public static BusinessDayConvention businessDayConventionBeanToBusinessDayConvention(final BusinessDayConventionBean businessDayConventionBean) {
    if (businessDayConventionBean == null) {
      return null;
    }
    final BusinessDayConvention bdc = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(businessDayConventionBean.getName());
    if (bdc == null) {
      throw new OpenGammaRuntimeException("Bad value for businessDayConventionBean (" + businessDayConventionBean.getName() + ")");
    }
    return bdc;
  }

}
