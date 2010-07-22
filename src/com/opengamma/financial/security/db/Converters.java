/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Calendar;
import java.util.Date;

import javax.time.calendar.DateProvider;
import javax.time.calendar.LocalDate;
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
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

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
  
  public static Expiry dateToExpiry(Date date) {
    if (date == null) {
      return null;
    }
    final Calendar c = Calendar.getInstance();
    c.setTime(date);
    return new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), ZoneOffset.UTC), TimeZone.UTC),
        ExpiryAccuracy.DAY_MONTH_YEAR);
  }
  
  public static Date expiryToDate(Expiry expiry) {
    if (expiry == null) {
      return null;
    }
    // we're storing just as a date, so assert that the value we're storing isn't a vague month or year
    if (expiry.getAccuracy() != null) {
      if (expiry.getAccuracy() != ExpiryAccuracy.DAY_MONTH_YEAR) {
        throw new OpenGammaRuntimeException("Expiry is not to DAY_MONTH_YEAR precision");
      }
    }
    return new Date(expiry.toInstant().toEpochMillisLong());
  }
  
  public static LocalDate dateToLocalDate(Date date) {
    if (date == null) {
      return null;
    }
    final Calendar c = Calendar.getInstance();
    c.setTime(date);
    return LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
  }
  
  public static Date localDateToDate(DateProvider date) {
    if (date == null) {
      return null;
    }
    return new Date(date.toLocalDate().atMidnight().atOffset(ZoneOffset.UTC).toInstant().toEpochMillisLong());
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
