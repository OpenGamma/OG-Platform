/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;

/**
 * Simple market data bundle containing market data items in a map.
 */
@BeanDefinition(builderScope = "private")
public final class MapMarketDataBundle implements MarketDataBundle, ImmutableBean {

  /** The time for which this market data is valid. */
  @PropertyDefinition(validate = "notNull", get = "private")
  private final MarketDataTime _time;

  /** The market data environment that provides data for this bundle. */
  @PropertyDefinition(validate = "notNull", get = "private")
  private final MarketDataEnvironment _env;

  /**
   * Creates a new bundle using the data in the environment.
   *
   * @param env the source of market data for this bundle
   */
  public MapMarketDataBundle(MarketDataEnvironment env) {
    this(MarketDataTime.VALUATION_TIME, env);
  }

  @Override
  public <T, I extends MarketDataId<T>> Result<T> get(I id, Class<T> dataType) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(dataType, "dataType");

    SingleValueRequirement requirement = SingleValueRequirement.of(id, _time);
    Object item = _env.getData().get(requirement);

    if (item != null) {
      if (!dataType.isInstance(item)) {
        return Result.failure(FailureStatus.ERROR,
                              "Market data is not of the expected type for {}/{}. type: {}, expected type: {}",
                              id, _time, item.getClass().getName(), dataType.getName());
      }
      return Result.success(dataType.cast(item));
    } else {
      return Result.failure(FailureStatus.MISSING_DATA, "No market data available for {}/{}", id, requirement.getMarketDataTime());
    }
  }

  @Override
  public <T, I extends MarketDataId<T>> Result<DateTimeSeries<LocalDate, T>> get(
      I id,
      Class<T> dataType,
      LocalDateRange dateRange) {

    DateTimeSeries<LocalDate, ?> timeSeries = _env.getTimeSeries().get(id);

    if (timeSeries != null && !timeSeries.isEmpty()) {
      @SuppressWarnings("unchecked")
      DateTimeSeries<LocalDate, T> castTimeSeries = (DateTimeSeries<LocalDate, T>) timeSeries;
      LocalDate start = dateRange.getStartDateInclusive();
      LocalDate end = dateRange.getEndDateInclusive();
      return Result.success(castTimeSeries.subSeries(start, true, end, true));

      // This code checks the time series has enough data available to satisfy the request.
      // Unfortunately it breaks a lot of existing tests where the test provides a small amount of data,
      // the function asks for a much greater range of data and then only uses the small amount provided by
      // the test. If you believe the range in the request then there isn't enough data in the environment, but
      // when the function runs there actually is. This code should be reinstated when PLT-633 is fixed
      /*
      DateTimeSeries<LocalDate, T> castTimeSeries = (DateTimeSeries<LocalDate, T>) timeSeries;
      LocalDate start = timeSeries.getEarliestTime();
      LocalDate end = timeSeries.getLatestTime();
      LocalDate requestedStart = dateRange.getStartDateInclusive();
      LocalDate requestedEnd = dateRange.getEndDateInclusive();

      if (!start.isAfter(dateRange.getStartDateInclusive()) && !end.isBefore(dateRange.getEndDateInclusive())) {
        return Result.success(castTimeSeries.subSeries(requestedStart, true, requestedEnd, true));
      } else {
        return Result.failure(
            FailureStatus.MISSING_DATA,
            "Time series {}/[{},{}] doesn't cover the requested range {}",
            id, start, end, dateRange);
      }
      */
    } else {
      return Result.failure(FailureStatus.MISSING_DATA, "No time series data available for {}/{}", id, dateRange);
    }
  }

  @Override
  public MarketDataBundle withTime(ZonedDateTime time) {
    ArgumentChecker.notNull(time, "time");
    return new MapMarketDataBundle(MarketDataTime.of(time), _env);
  }

  @Override
  public MarketDataBundle withDate(LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return new MapMarketDataBundle(MarketDataTime.of(date), _env);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MapMarketDataBundle}.
   * @return the meta-bean, not null
   */
  public static MapMarketDataBundle.Meta meta() {
    return MapMarketDataBundle.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MapMarketDataBundle.Meta.INSTANCE);
  }

  private MapMarketDataBundle(
      MarketDataTime time,
      MarketDataEnvironment env) {
    JodaBeanUtils.notNull(time, "time");
    JodaBeanUtils.notNull(env, "env");
    this._time = time;
    this._env = env;
  }

  @Override
  public MapMarketDataBundle.Meta metaBean() {
    return MapMarketDataBundle.Meta.INSTANCE;
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
   * Gets the time for which this market data is valid.
   * @return the value of the property, not null
   */
  private MarketDataTime getTime() {
    return _time;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data environment that provides data for this bundle.
   * @return the value of the property, not null
   */
  private MarketDataEnvironment getEnv() {
    return _env;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MapMarketDataBundle other = (MapMarketDataBundle) obj;
      return JodaBeanUtils.equal(getTime(), other.getTime()) &&
          JodaBeanUtils.equal(getEnv(), other.getEnv());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getTime());
    hash = hash * 31 + JodaBeanUtils.hashCode(getEnv());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("MapMarketDataBundle{");
    buf.append("time").append('=').append(getTime()).append(',').append(' ');
    buf.append("env").append('=').append(JodaBeanUtils.toString(getEnv()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MapMarketDataBundle}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code time} property.
     */
    private final MetaProperty<MarketDataTime> _time = DirectMetaProperty.ofImmutable(
        this, "time", MapMarketDataBundle.class, MarketDataTime.class);
    /**
     * The meta-property for the {@code env} property.
     */
    private final MetaProperty<MarketDataEnvironment> _env = DirectMetaProperty.ofImmutable(
        this, "env", MapMarketDataBundle.class, MarketDataEnvironment.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "time",
        "env");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3560141:  // time
          return _time;
        case 100589:  // env
          return _env;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MapMarketDataBundle> builder() {
      return new MapMarketDataBundle.Builder();
    }

    @Override
    public Class<? extends MapMarketDataBundle> beanType() {
      return MapMarketDataBundle.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code time} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataTime> time() {
      return _time;
    }

    /**
     * The meta-property for the {@code env} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataEnvironment> env() {
      return _env;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3560141:  // time
          return ((MapMarketDataBundle) bean).getTime();
        case 100589:  // env
          return ((MapMarketDataBundle) bean).getEnv();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code MapMarketDataBundle}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<MapMarketDataBundle> {

    private MarketDataTime _time;
    private MarketDataEnvironment _env;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3560141:  // time
          return _time;
        case 100589:  // env
          return _env;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3560141:  // time
          this._time = (MarketDataTime) newValue;
          break;
        case 100589:  // env
          this._env = (MarketDataEnvironment) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public MapMarketDataBundle build() {
      return new MapMarketDataBundle(
          _time,
          _env);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("MapMarketDataBundle.Builder{");
      buf.append("time").append('=').append(JodaBeanUtils.toString(_time)).append(',').append(' ');
      buf.append("env").append('=').append(JodaBeanUtils.toString(_env));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
