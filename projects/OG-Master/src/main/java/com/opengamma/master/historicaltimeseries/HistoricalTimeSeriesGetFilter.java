/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;


import java.io.Serializable;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.util.PublicSPI;

/**
 * Request for getting the data points of a time-series.
 * <p>
 * This allows a subset of the total time-series to be returned, effectively
 * acting as a filter.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class HistoricalTimeSeriesGetFilter extends DirectBean implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = -3240580993706420652L;

  /**
   * The earliest date to return, inclusive, null means far past.
   */
  @PropertyDefinition
  private LocalDate _earliestDate;
  /**
   * The latest date to return, inclusive, null means far future.
   */
  @PropertyDefinition
  private LocalDate _latestDate;
  /**
   * The maximum number of points, null to not limit the number of points.
   * This limits the number of returned points within the date range.
   * If the number is negative, the points are counted from the latest date backwards.
   * If the number is positive, the points are counted from the earliest date forwards.
   * Setting this to zero, returns an empty time-series.
   * The value one will therefore get the earliest point, and minus one gets the latest point.
   */
  @PropertyDefinition
  private Integer _maxPoints;

  //-------------------------------------------------------------------------
  /**
   * Creates an instance that retrieves all points in the series.
   * 
   * @return the mutable request, not null
   */
  public static HistoricalTimeSeriesGetFilter ofAll() {
    return new HistoricalTimeSeriesGetFilter();
  }

  /**
   * Creates an instance that retrieves the latest value for the series.
   * 
   * @return the mutable request, not null
   */
  public static HistoricalTimeSeriesGetFilter ofLatestPoint() {
    HistoricalTimeSeriesGetFilter request = new HistoricalTimeSeriesGetFilter();
    request.setMaxPoints(-1);
    return request;
  }

  /**
   * Creates and instance that retrieves the latest point within a specified period ending now.
   * 
   * @param period  the period, counting backwards from the current time, within which the point must fall
   * @return the mutable request, not null
   */
  public static HistoricalTimeSeriesGetFilter ofLatestPoint(Period period) {
    HistoricalTimeSeriesGetFilter request = new HistoricalTimeSeriesGetFilter();
    LocalDate now = LocalDate.now();
    request.setEarliestDate(now.minus(period));
    request.setLatestDate(now);
    request.setMaxPoints(-1);
    return request;
  }

  /**
   * Creates and instance that retrieves the latest point within a specified time interval.
   * 
   * @param earliestDate  the earliest date to retrieve, inclusive, null means far past
   * @param latestDate  the latest date to retrieve, inclusive, null means far future
   * @return the mutable request, not null
   */
  public static HistoricalTimeSeriesGetFilter ofLatestPoint(LocalDate earliestDate, LocalDate latestDate) {
    HistoricalTimeSeriesGetFilter request = HistoricalTimeSeriesGetFilter.ofRange(earliestDate, latestDate);
    request.setMaxPoints(-1);
    return request;
  }

  /**
   * Creates an instance that retrieves the earliest value for the series.
   * 
   * @return the mutable request, not null
   */
  public static HistoricalTimeSeriesGetFilter ofEarliestPoint() {
    HistoricalTimeSeriesGetFilter request = new HistoricalTimeSeriesGetFilter();
    request.setMaxPoints(1);
    return request;
  }

  /**
   * Creates an instance specifying a date range.
   * 
   * @param earliestDate  the earliest date to retrieve, inclusive, null means far past
   * @param latestDate  the latest date to retrieve, inclusive, null means far future
   * @return the mutable request, not null
   */
  public static HistoricalTimeSeriesGetFilter ofRange(LocalDate earliestDate, LocalDate latestDate) {
    HistoricalTimeSeriesGetFilter request = new HistoricalTimeSeriesGetFilter();
    request.setEarliestDate(earliestDate);
    request.setLatestDate(latestDate);
    return request;
  }
  
  /**
   * Creates an instance specifying a date range and an upperbound for the number of points returned.
   * 
   * @param earliestDate  the earliest date to retrieve, inclusive, null means far past
   * @param latestDate  the latest date to retrieve, inclusive, null means far future
   * @param maxPoints  the max number of points to retrieve, null means fetch max possible
   *  -ve fetches backwards commencing from the latest date, +ve fetches forward from the earliest date
   * @return the mutable request, not null
   */
  public static HistoricalTimeSeriesGetFilter ofRange(LocalDate earliestDate, LocalDate latestDate, Integer maxPoints) {
    HistoricalTimeSeriesGetFilter request = new HistoricalTimeSeriesGetFilter();
    request.setEarliestDate(earliestDate);
    request.setLatestDate(latestDate);
    request.setMaxPoints(maxPoints);
    return request;
  }

  /**
   * Creates an instance.
   */
  public HistoricalTimeSeriesGetFilter() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HistoricalTimeSeriesGetFilter}.
   * @return the meta-bean, not null
   */
  public static HistoricalTimeSeriesGetFilter.Meta meta() {
    return HistoricalTimeSeriesGetFilter.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(HistoricalTimeSeriesGetFilter.Meta.INSTANCE);
  }

  @Override
  public HistoricalTimeSeriesGetFilter.Meta metaBean() {
    return HistoricalTimeSeriesGetFilter.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the earliest date to return, inclusive, null means far past.
   * @return the value of the property
   */
  public LocalDate getEarliestDate() {
    return _earliestDate;
  }

  /**
   * Sets the earliest date to return, inclusive, null means far past.
   * @param earliestDate  the new value of the property
   */
  public void setEarliestDate(LocalDate earliestDate) {
    this._earliestDate = earliestDate;
  }

  /**
   * Gets the the {@code earliestDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> earliestDate() {
    return metaBean().earliestDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the latest date to return, inclusive, null means far future.
   * @return the value of the property
   */
  public LocalDate getLatestDate() {
    return _latestDate;
  }

  /**
   * Sets the latest date to return, inclusive, null means far future.
   * @param latestDate  the new value of the property
   */
  public void setLatestDate(LocalDate latestDate) {
    this._latestDate = latestDate;
  }

  /**
   * Gets the the {@code latestDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> latestDate() {
    return metaBean().latestDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum number of points, null to not limit the number of points.
   * This limits the number of returned points within the date range.
   * If the number is negative, the points are counted from the latest date backwards.
   * If the number is positive, the points are counted from the earliest date forwards.
   * Setting this to zero, returns an empty time-series.
   * The value one will therefore get the earliest point, and minus one gets the latest point.
   * @return the value of the property
   */
  public Integer getMaxPoints() {
    return _maxPoints;
  }

  /**
   * Sets the maximum number of points, null to not limit the number of points.
   * This limits the number of returned points within the date range.
   * If the number is negative, the points are counted from the latest date backwards.
   * If the number is positive, the points are counted from the earliest date forwards.
   * Setting this to zero, returns an empty time-series.
   * The value one will therefore get the earliest point, and minus one gets the latest point.
   * @param maxPoints  the new value of the property
   */
  public void setMaxPoints(Integer maxPoints) {
    this._maxPoints = maxPoints;
  }

  /**
   * Gets the the {@code maxPoints} property.
   * This limits the number of returned points within the date range.
   * If the number is negative, the points are counted from the latest date backwards.
   * If the number is positive, the points are counted from the earliest date forwards.
   * Setting this to zero, returns an empty time-series.
   * The value one will therefore get the earliest point, and minus one gets the latest point.
   * @return the property, not null
   */
  public final Property<Integer> maxPoints() {
    return metaBean().maxPoints().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesGetFilter clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HistoricalTimeSeriesGetFilter other = (HistoricalTimeSeriesGetFilter) obj;
      return JodaBeanUtils.equal(getEarliestDate(), other.getEarliestDate()) &&
          JodaBeanUtils.equal(getLatestDate(), other.getLatestDate()) &&
          JodaBeanUtils.equal(getMaxPoints(), other.getMaxPoints());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getEarliestDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLatestDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaxPoints());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("HistoricalTimeSeriesGetFilter{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("earliestDate").append('=').append(JodaBeanUtils.toString(getEarliestDate())).append(',').append(' ');
    buf.append("latestDate").append('=').append(JodaBeanUtils.toString(getLatestDate())).append(',').append(' ');
    buf.append("maxPoints").append('=').append(JodaBeanUtils.toString(getMaxPoints())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HistoricalTimeSeriesGetFilter}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code earliestDate} property.
     */
    private final MetaProperty<LocalDate> _earliestDate = DirectMetaProperty.ofReadWrite(
        this, "earliestDate", HistoricalTimeSeriesGetFilter.class, LocalDate.class);
    /**
     * The meta-property for the {@code latestDate} property.
     */
    private final MetaProperty<LocalDate> _latestDate = DirectMetaProperty.ofReadWrite(
        this, "latestDate", HistoricalTimeSeriesGetFilter.class, LocalDate.class);
    /**
     * The meta-property for the {@code maxPoints} property.
     */
    private final MetaProperty<Integer> _maxPoints = DirectMetaProperty.ofReadWrite(
        this, "maxPoints", HistoricalTimeSeriesGetFilter.class, Integer.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "earliestDate",
        "latestDate",
        "maxPoints");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 239226785:  // earliestDate
          return _earliestDate;
        case -125315115:  // latestDate
          return _latestDate;
        case -667790489:  // maxPoints
          return _maxPoints;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HistoricalTimeSeriesGetFilter> builder() {
      return new DirectBeanBuilder<HistoricalTimeSeriesGetFilter>(new HistoricalTimeSeriesGetFilter());
    }

    @Override
    public Class<? extends HistoricalTimeSeriesGetFilter> beanType() {
      return HistoricalTimeSeriesGetFilter.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code earliestDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> earliestDate() {
      return _earliestDate;
    }

    /**
     * The meta-property for the {@code latestDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> latestDate() {
      return _latestDate;
    }

    /**
     * The meta-property for the {@code maxPoints} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxPoints() {
      return _maxPoints;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 239226785:  // earliestDate
          return ((HistoricalTimeSeriesGetFilter) bean).getEarliestDate();
        case -125315115:  // latestDate
          return ((HistoricalTimeSeriesGetFilter) bean).getLatestDate();
        case -667790489:  // maxPoints
          return ((HistoricalTimeSeriesGetFilter) bean).getMaxPoints();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 239226785:  // earliestDate
          ((HistoricalTimeSeriesGetFilter) bean).setEarliestDate((LocalDate) newValue);
          return;
        case -125315115:  // latestDate
          ((HistoricalTimeSeriesGetFilter) bean).setLatestDate((LocalDate) newValue);
          return;
        case -667790489:  // maxPoints
          ((HistoricalTimeSeriesGetFilter) bean).setMaxPoints((Integer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
