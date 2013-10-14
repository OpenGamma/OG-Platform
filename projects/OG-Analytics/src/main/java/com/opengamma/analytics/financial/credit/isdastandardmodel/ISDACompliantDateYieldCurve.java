/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantDateCurve.checkAndGetTimes;

import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 *
 */
public class ISDACompliantDateYieldCurve extends ISDACompliantYieldCurve implements ISDACompliantCurveWithDates, Bean {

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  @PropertyDefinition(get = "manual")
  private final LocalDate _baseDate;

  @PropertyDefinition(get = "private")
  private final LocalDate[] _dates;

  @PropertyDefinition(get = "private")
  private final DayCount _dayCount;

  /**
   * Builds a yield curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using ACT/365
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero rates at given knot dates
   */
  public ISDACompliantDateYieldCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    this(baseDate, dates, rates, ACT_365);
  }

  /**
   * Builds a yield curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates. The times (year-fractions)
   * between the baseDate and the knot dates is calculated using the specified day-count-convention
   * @param baseDate The base date for the curve (i.e. this is time zero)
   * @param dates Knot dates on the curve. These must be ascending with the first date after the baseDate
   * @param rates Continually compounded zero rates at given knot dates
   * @param dayCount The day-count-convention
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

  @Override
  public LocalDate getBaseDate() {
    return _baseDate;
  }

  @Override
  public LocalDate getCurveDate(final int index) {
    return _dates[index];
  }

  @Override
  public LocalDate[] getCurveDates() {
    final LocalDate[] res = new LocalDate[getNumberOfKnots()];
    // TODO since this is only copying references anyway, do we need it
    System.arraycopy(_dates, 0, res, 0, getNumberOfKnots());
    return res;
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

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code baseDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> baseDate() {
    return metaBean().baseDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the dates.
   * @return the value of the property
   */
  private LocalDate[] getDates() {
    return _dates;
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
   * Gets the dayCount.
   * @return the value of the property
   */
  private DayCount getDayCount() {
    return _dayCount;
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
    BeanBuilder<? extends ISDACompliantDateYieldCurve> builder = metaBean().builder();
    for (MetaProperty<?> mp : metaBean().metaPropertyIterable()) {
      if (mp.style().isBuildable()) {
        Object value = mp.get(this);
        if (value instanceof Bean) {
          value = ((Bean) value).clone();
        }
        builder.set(mp.name(), value);
      }
    }
    return builder.build();
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
          JodaBeanUtils.equal(getDayCount(), other.getDayCount());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getBaseDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    return hash;
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

  protected void toString(StringBuilder buf) {
    buf.append("baseDate").append('=').append(getBaseDate()).append(',').append(' ');
    buf.append("dates").append('=').append(getDates()).append(',').append(' ');
    buf.append("dayCount").append('=').append(getDayCount()).append(',').append(' ');
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
    private final MetaProperty<LocalDate> _baseDate = DirectMetaProperty.ofReadOnly(
        this, "baseDate", ISDACompliantDateYieldCurve.class, LocalDate.class);
    /**
     * The meta-property for the {@code dates} property.
     */
    private final MetaProperty<LocalDate[]> _dates = DirectMetaProperty.ofReadOnly(
        this, "dates", ISDACompliantDateYieldCurve.class, LocalDate[].class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadOnly(
        this, "dayCount", ISDACompliantDateYieldCurve.class, DayCount.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
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
      throw new UnsupportedOperationException();
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
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: baseDate");
        case 95356549:  // dates
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: dates");
        case 1905311443:  // dayCount
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: dayCount");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
