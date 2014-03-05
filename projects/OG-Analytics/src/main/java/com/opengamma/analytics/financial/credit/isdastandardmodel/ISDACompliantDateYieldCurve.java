/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantDateCurve.checkAndGetTimes;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;

/**
 * An ISDA compliant date yield curve.
 */
@BeanDefinition
public class ISDACompliantDateYieldCurve
    extends ISDACompliantYieldCurve
    implements ISDACompliantCurveWithDates {

  /**
   * The standard ACT/365 day count.
   */
  private static final DayCount ACT_365 = DayCounts.ACT_365;

  /**
   * The base date.
   */
  @PropertyDefinition(set = "private")
  private LocalDate _baseDate;
  /**
   * The knot dates on the curve.
   */
  @PropertyDefinition(get = "private", set = "private")
  private LocalDate[] _dates;
  /**
   * The day count.
   */
  @PropertyDefinition(get = "private", set = "private")
  private DayCount _dayCount;

  /**
   * Constructor for Joda-Beans.
   */
  protected ISDACompliantDateYieldCurve() {
    super();
  }

  /**
   * Builds a yield curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates.
   * The times (year-fractions) between the baseDate and the knot dates is calculated using ACT/365.
   * 
   * @param baseDate  the base date for the curve (i.e. this is time zero), not null
   * @param dates  the knot dates on the curve. These must be ascending with the first date after the baseDate, not null
   * @param rates  the continually compounded zero rates at given knot dates, not null
   */
  public ISDACompliantDateYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    this(baseDate, dates, rates, ACT_365);
  }

  /**
   * Builds a yield curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates.
   * The times (year-fractions) between the baseDate and the knot dates is calculated using the specified day-count-convention.
   * 
   * @param baseDate  the base date for the curve (i.e. this is time zero), not null
   * @param dates  the knot dates on the curve. These must be ascending with the first date after the baseDate, not null
   * @param rates  the continually compounded zero rates at given knot dates, not null
   * @param dayCount  the day-count-convention, not null
   */
  public ISDACompliantDateYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    super(checkAndGetTimes(baseDate, dates, rates, dayCount), rates);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  private ISDACompliantDateYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final DayCount dayCount, final ISDACompliantYieldCurve baseCurve) {
    super(baseCurve);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  //  /**
  //   * Converter from the old ISDADateCurve to ISDACompliantYieldCurve. Not this only works if offset = 0.0 and and baseDate is set.
  //   * @param yieldCurve a ISDADateCurve yieldCurve
  //   * @return A ISDACompliantYieldCurve
  //   */
  //  public static ISDACompliantDateYieldCurve fromISDADateCurve(final ISDADateCurve yieldCurve) {
  //
  //    ArgumentChecker.isTrue(yieldCurve.getOffset() == 0, "offset not zero - cannot convert");
  //    final ZonedDateTime bDate = yieldCurve.getBaseDate();
  //    ArgumentChecker.notNull(bDate, "base date");
  //    final LocalDate baseDate = bDate.toLocalDate();
  //
  //    final ZonedDateTime[] curveDates = yieldCurve.getCurveDates();
  //    final Double[] temp = yieldCurve.getCurve().getYData();
  //    final int n = temp.length;
  //    final double[] r = new double[n];
  //    for (int i = 0; i < n; i++) {
  //      r[i] = temp[i];
  //    }
  //
  //    final LocalDate[] dates = ISDACompliantScheduleGenerator.toLocalDate(curveDates);
  //    return new ISDACompliantDateYieldCurve(baseDate, dates, r);
  //  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate getCurveDate(final int index) {
    return _dates[index];
  }

  @Override
  public LocalDate[] getCurveDates() {
    return _dates.clone();
  }

  @Override
  public ISDACompliantDateYieldCurve withRate(final double rate, final int index) {
    final ISDACompliantYieldCurve temp = super.withRate(rate, index);
    return new ISDACompliantDateYieldCurve(_baseDate, _dates, _dayCount, temp);
  }

  @Override
  public double getZeroRate(final LocalDate date) {
    final double t = _dayCount.getDayCountFraction(_baseDate, date);
    return getZeroRate(t);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ISDACompliantDateYieldCurve}.
   * @return the meta-bean, not null
   */
  public static ISDACompliantDateYieldCurve.Meta meta() {
    return ISDACompliantDateYieldCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ISDACompliantDateYieldCurve.Meta.INSTANCE);
  }

  @Override
  public ISDACompliantDateYieldCurve.Meta metaBean() {
    return ISDACompliantDateYieldCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the base date.
   * @return the value of the property
   */
  public LocalDate getBaseDate() {
    return _baseDate;
  }

  /**
   * Sets the base date.
   * @param baseDate  the new value of the property
   */
  private void setBaseDate(LocalDate baseDate) {
    this._baseDate = baseDate;
  }

  /**
   * Gets the the {@code baseDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> baseDate() {
    return metaBean().baseDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the knot dates on the curve.
   * @return the value of the property
   */
  private LocalDate[] getDates() {
    return _dates;
  }

  /**
   * Sets the knot dates on the curve.
   * @param dates  the new value of the property
   */
  private void setDates(LocalDate[] dates) {
    this._dates = dates;
  }

  /**
   * Gets the the {@code dates} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> dates() {
    return metaBean().dates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count.
   * @return the value of the property
   */
  private DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the day count.
   * @param dayCount  the new value of the property
   */
  private void setDayCount(DayCount dayCount) {
    this._dayCount = dayCount;
  }

  /**
   * Gets the the {@code dayCount} property.
   * @return the property, not null
   */
  public final Property<DayCount> dayCount() {
    return metaBean().dayCount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ISDACompliantDateYieldCurve clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ISDACompliantDateYieldCurve other = (ISDACompliantDateYieldCurve) obj;
      return JodaBeanUtils.equal(getBaseDate(), other.getBaseDate()) &&
          JodaBeanUtils.equal(getDates(), other.getDates()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getBaseDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("ISDACompliantDateYieldCurve{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("baseDate").append('=').append(JodaBeanUtils.toString(getBaseDate())).append(',').append(' ');
    buf.append("dates").append('=').append(JodaBeanUtils.toString(getDates())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ISDACompliantDateYieldCurve}.
   */
  public static class Meta extends ISDACompliantYieldCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code baseDate} property.
     */
    private final MetaProperty<LocalDate> _baseDate = DirectMetaProperty.ofReadWrite(
        this, "baseDate", ISDACompliantDateYieldCurve.class, LocalDate.class);
    /**
     * The meta-property for the {@code dates} property.
     */
    private final MetaProperty<LocalDate[]> _dates = DirectMetaProperty.ofReadWrite(
        this, "dates", ISDACompliantDateYieldCurve.class, LocalDate[].class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", ISDACompliantDateYieldCurve.class, DayCount.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "baseDate",
        "dates",
        "dayCount");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          return _baseDate;
        case 95356549:  // dates
          return _dates;
        case 1905311443:  // dayCount
          return _dayCount;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ISDACompliantDateYieldCurve> builder() {
      return new DirectBeanBuilder<ISDACompliantDateYieldCurve>(new ISDACompliantDateYieldCurve());
    }

    @Override
    public Class<? extends ISDACompliantDateYieldCurve> beanType() {
      return ISDACompliantDateYieldCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code baseDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> baseDate() {
      return _baseDate;
    }

    /**
     * The meta-property for the {@code dates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> dates() {
      return _dates;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return _dayCount;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          return ((ISDACompliantDateYieldCurve) bean).getBaseDate();
        case 95356549:  // dates
          return ((ISDACompliantDateYieldCurve) bean).getDates();
        case 1905311443:  // dayCount
          return ((ISDACompliantDateYieldCurve) bean).getDayCount();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          ((ISDACompliantDateYieldCurve) bean).setBaseDate((LocalDate) newValue);
          return;
        case 95356549:  // dates
          ((ISDACompliantDateYieldCurve) bean).setDates((LocalDate[]) newValue);
          return;
        case 1905311443:  // dayCount
          ((ISDACompliantDateYieldCurve) bean).setDayCount((DayCount) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
