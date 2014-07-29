/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.util.Date;

import org.threeten.bp.Instant;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.financial.security.index.IndexWeightingType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.masterdb.security.hibernate.bond.YieldConventionBean;
import com.opengamma.masterdb.security.hibernate.cds.CDSIndexComponentBean;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Utility methods for simple conversions.
 */
public final class Converters {

  private Converters() {
  }

  public static Currency currencyBeanToCurrency(final CurrencyBean currencyBean) {
    if (currencyBean == null) {
      return null;
    }
    return Currency.of(currencyBean.getName());
  }

  //-------------------------------------------------------------------------
  public static ExternalId externalIdBeanToExternalId(final ExternalIdBean hibernateBean) {
    if (hibernateBean == null) {
      return null;
    }
    return ExternalId.of(hibernateBean.getScheme(), hibernateBean.getIdentifier());
  }

  public static ExternalIdBean externalIdToExternalIdBean(final ExternalId identifier) {
    if (identifier == null) {
      return null;
    }
    return new ExternalIdBean(identifier.getScheme().getName(), identifier.getValue());
  }
  
  public static CreditDefaultSwapIndexComponent cdsIndexComponentBeanToCDSIndexComponent(final CDSIndexComponentBean componentBean) {
    final ExternalId obligor = externalIdBeanToExternalId(componentBean.getObligor());
    final ExternalId bondId = externalIdBeanToExternalId(componentBean.getBondId());
    return new CreditDefaultSwapIndexComponent(componentBean.getName(), obligor, componentBean.getWeight(), bondId);
  }

  //-------------------------------------------------------------------------
  public static UniqueId uniqueIdBeanToUniqueId(final UniqueIdBean hibernateBean) {
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
    final ZonedDateTimeBean zonedDateTimeBean = bean.getExpiry();

    final long epochSeconds = zonedDateTimeBean.getDate().getTime() / 1000;
    ZonedDateTime zdt = null;
    if (zonedDateTimeBean.getZone() == null) {
      zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    } else {
      zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.of(zonedDateTimeBean.getZone()));
    }

    return new Expiry(zdt, bean.getAccuracy());
  }

  public static ExpiryBean expiryToExpiryBean(final Expiry expiry) {
    if (expiry == null) {
      return null;
    }
    final ExpiryBean bean = new ExpiryBean();

    final ZonedDateTimeBean zonedDateTimeBean = new ZonedDateTimeBean();
    zonedDateTimeBean.setDate(new Date(expiry.getExpiry().toInstant().toEpochMilli()));
    zonedDateTimeBean.setZone(expiry.getExpiry().getZone().getId());
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
      return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    } else {
      return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.of(date.getZone()));
    }
  }

  public static ZonedDateTimeBean dateTimeWithZoneToZonedDateTimeBean(final ZonedDateTime zdt) {
    if (zdt == null) {
      return null;
    }
    final ZonedDateTimeBean bean = new ZonedDateTimeBean();
    bean.setDate(new Date(zdt.toInstant().toEpochMilli()));
    bean.setZone(zdt.getZone().getId());
    return bean;
  }

  //-------------------------------------------------------------------------
  public static Tenor tenorBeanToTenor(final TenorBean tenorBean) {
    if (tenorBean == null) {
      return null;
    }
    return Tenor.of(Period.parse(tenorBean.getName()));
  }

  public static IndexWeightingType indexWeightingTypeBeanToIndexWeightingType(final IndexWeightingTypeBean weightingTypeBean) {
    if (weightingTypeBean == null) {
      return null;
    }
    return IndexWeightingType.valueOf(weightingTypeBean.getName());
  }
  //-------------------------------------------------------------------------
  public static Frequency frequencyBeanToFrequency(final FrequencyBean frequencyBean) {
    if (frequencyBean == null) {
      return null;
    }
    return SimpleFrequencyFactory.of(frequencyBean.getName());
  }
  
  public static void validateFrequency(final String name) {
    SimpleFrequencyFactory.of(name);
  }

  //-------------------------------------------------------------------------
  public static DayCount dayCountBeanToDayCount(final DayCountBean dayCountBean) {
    if (dayCountBean == null) {
      return null;
    }
    return DayCountFactory.of(dayCountBean.getName());
  }

  public static void validateDayCount(final String name) {
    DayCountFactory.of(name);
  }

  //-------------------------------------------------------------------------
  public static BusinessDayConvention businessDayConventionBeanToBusinessDayConvention(final BusinessDayConventionBean businessDayConventionBean) {
    if (businessDayConventionBean == null) {
      return null;
    }
    return BusinessDayConventionFactory.of(businessDayConventionBean.getName());
  }

  public static void validateBusinessDayConvention(final String name) {
    BusinessDayConventionFactory.of(name);
  }

  //-------------------------------------------------------------------------
  public static YieldConvention yieldConventionBeanToYieldConvention(final YieldConventionBean yieldConventionBean) {
    if (yieldConventionBean == null) {
      return null;
    }
    return YieldConventionFactory.of(yieldConventionBean.getName());
  }

  public static void validateYieldConvention(final String name) {
    YieldConventionFactory.of(name);
  }

  //-------------------------------------------------------------------------
  public static StubType stubTypeBeanToStubType(final StubTypeBean stubTypeBean) {
    if (stubTypeBean == null) {
      return null;
    }
    validateStubType(stubTypeBean.getName());
    return StubType.valueOf(stubTypeBean.getName());
  }

  private static void validateStubType(final String name) {
    try {
      StubType.valueOf(name);
    } catch (final IllegalArgumentException e) {
      throw new OpenGammaRuntimeException("Bad value for stub type (" + name + ")");
    }
  }

  //-------------------------------------------------------------------------
  public static DebtSeniority debtSeniorityBeanToDebtSeniority(final DebtSeniorityBean bean) {
    if (bean == null) {
      return null;
    }
    try {
      return DebtSeniority.valueOf(bean.getName());
    } catch (final IllegalArgumentException e) {
      throw new OpenGammaRuntimeException("Bad value for DebtSeniority type (" + bean.getName() + ")");
    }
  }

  //-------------------------------------------------------------------------
  public static RestructuringClause restructuringClauseBeanToRestructuringClause(final RestructuringClauseBean bean) {
    if (bean == null) {
      return null;
    }
    try {
      return RestructuringClause.valueOf(bean.getName());
    } catch (final IllegalArgumentException e) {
      throw new OpenGammaRuntimeException("Bad value for RestructuringClause type (" + bean.getName() + ")");
    }
  }

}
