/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.credit;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Class holding properties of a single name.
 */
public class SingleNameIdentifiable implements UniqueIdentifiable, ObjectIdentifiable {

  /**
   * Scheme used for a single name.
   */
  public static final String SCHEME = "SingleName";
  /**
   * The separator.
   */
  private static final String SEPERATOR = "-";

  private String name;
  private UniqueId id;
  private ExternalId referenceEntity;
  private BusinessDayConvention badDayConvention;
  private DayCount daycount;
  private Period couponFrequency;
  private StubType stubType;

  public SingleNameIdentifiable(String name,
                                ExternalId referenceEntity,
                                BusinessDayConvention badDayConvention,
                                DayCount daycount,
                                Period couponFrequency,
                                StubType stubType) {
    this.name = name;
    this.referenceEntity = referenceEntity;
    this.badDayConvention = badDayConvention;
    this.daycount = daycount;
    this.couponFrequency = couponFrequency;
    this.stubType = stubType;
    this.id = UniqueId.of(SCHEME, name + SEPERATOR + referenceEntity.getScheme() + SEPERATOR + badDayConvention.getName()
        + SEPERATOR + daycount.getName() + SEPERATOR + couponFrequency + SEPERATOR + stubType);
  }

  public static SingleNameIdentifiable of(final UniqueId id) {
    String[] tokens = id.getValue().split(SEPERATOR);
    ArgumentChecker.isTrue(tokens.length == 6, "Incorrect number of params for SingleNameIdentifiable");
    final String name = tokens[0];
    final ExternalId reference = ExternalId.of(tokens[1], name);
    final BusinessDayConvention badDayConvention = BusinessDayConventionFactory.of(tokens[2]);
    final DayCount dayCount = DayCountFactory.of(tokens[3]);
    final Period couponFrequency = Period.parse(tokens[4]);
    final StubType stubType = StubType.valueOf(tokens[5]);
    return new SingleNameIdentifiable(name, reference, badDayConvention, dayCount, couponFrequency, stubType);
  }

  @Override
  public UniqueId getUniqueId() {
    return id;
  }

  @Override
  public ObjectId getObjectId() {
    return getUniqueId().getObjectId();
  }

  public String getName() {
    return name;
  }

  public ExternalId getReferenceEntity() {
    return referenceEntity;
  }

  public BusinessDayConvention getBadDayConvention() {
    return badDayConvention;
  }

  public DayCount getDaycount() {
    return daycount;
  }

  public Period getCouponFrequency() {
    return couponFrequency;
  }

  public StubType getStubType() {
    return stubType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SingleNameIdentifiable that = (SingleNameIdentifiable) o;

    if (badDayConvention != null ? !badDayConvention.equals(that.badDayConvention) : that.badDayConvention != null) {
      return false;
    }
    if (couponFrequency != null ? !couponFrequency.equals(that.couponFrequency) : that.couponFrequency != null) {
      return false;
    }
    if (daycount != null ? !daycount.equals(that.daycount) : that.daycount != null) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (referenceEntity != null ? !referenceEntity.equals(that.referenceEntity) : that.referenceEntity != null) {
      return false;
    }
    if (stubType != that.stubType) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    result = name != null ? name.hashCode() : 0;
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (referenceEntity != null ? referenceEntity.hashCode() : 0);
    result = 31 * result + (badDayConvention != null ? badDayConvention.hashCode() : 0);
    result = 31 * result + (daycount != null ? daycount.hashCode() : 0);
    result = 31 * result + (couponFrequency != null ? couponFrequency.hashCode() : 0);
    result = 31 * result + (stubType != null ? stubType.hashCode() : 0);
    return result;
  }
}
