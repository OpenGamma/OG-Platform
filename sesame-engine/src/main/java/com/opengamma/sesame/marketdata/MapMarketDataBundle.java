/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
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

import com.google.common.collect.ImmutableMap;
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

  // TODO the getter should be private
  //@PropertyDefinition(validate = "notNull", get = "private")
  /** Single items or market data, keyed by their ID. */
  @PropertyDefinition(validate = "notNull", get = "manual")
  private final Map<MarketDataRequirement, Object> _marketData;

  /** Time series of market data, keyed by the ID of the market data they contain. */
  @PropertyDefinition(validate = "notNull", get = "private")
  private final Map<MarketDataId, DateTimeSeries<LocalDate, ?>> _timeSeries;

  @ImmutableConstructor
  MapMarketDataBundle(MarketDataTime time,
                      Map<MarketDataRequirement, Object> marketData,
                      Map<MarketDataId, DateTimeSeries<LocalDate, ?>> timeSeries) {
    _time = ArgumentChecker.notNull(time, "time");
    _marketData = ImmutableMap.copyOf(marketData);
    _timeSeries = ImmutableMap.copyOf(timeSeries);
  }

  @Override
  public <T> Result<T> get(MarketDataId id, Class<T> dataType) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(dataType, "dataType");

    MarketDataRequirement requirement = SingleValueRequirement.of(id, _time);
    Object item = _marketData.get(requirement);

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

  @SuppressWarnings("unchecked")
  @Override
  public <T> Result<DateTimeSeries<LocalDate, T>> get(MarketDataId id,
                                                      Class<T> dataType,
                                                      LocalDateRange dateRange) {
    DateTimeSeries<LocalDate, ?> timeSeries = _timeSeries.get(id);

    // TODO should the size only be checked if the date range is non-empty?
    if (timeSeries != null && timeSeries.size() > 0) {
      // TODO type check
      DateTimeSeries<LocalDate, T> castTimeSeries = (DateTimeSeries<LocalDate, T>) timeSeries;
      LocalDate start = dateRange.getStartDateInclusive();
      LocalDate end = dateRange.getEndDateInclusive();
      return Result.success(castTimeSeries.subSeries(start, true, end, true));
    } else {
      return Result.failure(FailureStatus.MISSING_DATA, "No time series data available for {}/{}", id, dateRange);
    }
  }

  @Override
  public MarketDataBundle withTime(ZonedDateTime time) {
    ArgumentChecker.notNull(time, "time");
    return new MapMarketDataBundle(MarketDataTime.of(time), _marketData, _timeSeries);
  }

  @Override
  public MarketDataBundle withDate(LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return new MapMarketDataBundle(MarketDataTime.of(date), _marketData, _timeSeries);
  }

  /**
   * Creates a builder for market data that's valid indefinitely.
   *
   * @return a builder for market data that's valid indefinitely
   */
  public static MapMarketDataBundleBuilder builder() {
    return new MapMarketDataBundleBuilder();
  }

  /**
   * Creates a builder with market data valid for a single day.
   *
   * @param marketDataDate the day on which the market data is valid
   * @return a builder for building a bundle of market data
   */
  public static MapMarketDataBundleBuilder builder(LocalDate marketDataDate) {
    return new MapMarketDataBundleBuilder(marketDataDate);
  }

  /**
   * Creates a builder with market data valid at a single point in time.
   *
   * @param marketDataTime the time at which the market data is valid
   * @return a builder for building a bundle of market data
   */
  public static MapMarketDataBundleBuilder builder(ZonedDateTime marketDataTime) {
    return new MapMarketDataBundleBuilder(marketDataTime);
  }

  public MapMarketDataBundleBuilder toBuilder() {
    return new MapMarketDataBundleBuilder(_time, _marketData, _timeSeries);
  }

  /**
   * Returns this bundle's market data.
   *
   * @return the market data
   * @deprecated this method is temporary and will be removed when the engine has native support for
   *   {@link MarketDataBundle}
   */
  public Map<MarketDataRequirement, Object> getMarketData() {
    return _marketData;
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
   * Gets time series of market data, keyed by the ID of the market data they contain.
   * @return the value of the property, not null
   */
  private Map<MarketDataId, DateTimeSeries<LocalDate, ?>> getTimeSeries() {
    return _timeSeries;
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
          JodaBeanUtils.equal(getMarketData(), other.getMarketData()) &&
          JodaBeanUtils.equal(getTimeSeries(), other.getTimeSeries());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketData());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTimeSeries());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("MapMarketDataBundle{");
    buf.append("time").append('=').append(getTime()).append(',').append(' ');
    buf.append("marketData").append('=').append(getMarketData()).append(',').append(' ');
    buf.append("timeSeries").append('=').append(JodaBeanUtils.toString(getTimeSeries()));
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
     * The meta-property for the {@code marketData} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<MarketDataRequirement, Object>> _marketData = DirectMetaProperty.ofImmutable(
        this, "marketData", MapMarketDataBundle.class, (Class) Map.class);
    /**
     * The meta-property for the {@code timeSeries} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<MarketDataId, DateTimeSeries<LocalDate, ?>>> _timeSeries = DirectMetaProperty.ofImmutable(
        this, "timeSeries", MapMarketDataBundle.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "time",
        "marketData",
        "timeSeries");

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
        case 1116764678:  // marketData
          return _marketData;
        case 779431844:  // timeSeries
          return _timeSeries;
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
     * The meta-property for the {@code marketData} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<MarketDataRequirement, Object>> marketData() {
      return _marketData;
    }

    /**
     * The meta-property for the {@code timeSeries} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<MarketDataId, DateTimeSeries<LocalDate, ?>>> timeSeries() {
      return _timeSeries;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3560141:  // time
          return ((MapMarketDataBundle) bean).getTime();
        case 1116764678:  // marketData
          return ((MapMarketDataBundle) bean).getMarketData();
        case 779431844:  // timeSeries
          return ((MapMarketDataBundle) bean).getTimeSeries();
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
    private Map<MarketDataRequirement, Object> _marketData = new HashMap<MarketDataRequirement, Object>();
    private Map<MarketDataId, DateTimeSeries<LocalDate, ?>> _timeSeries = new HashMap<MarketDataId, DateTimeSeries<LocalDate, ?>>();

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
        case 1116764678:  // marketData
          return _marketData;
        case 779431844:  // timeSeries
          return _timeSeries;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3560141:  // time
          this._time = (MarketDataTime) newValue;
          break;
        case 1116764678:  // marketData
          this._marketData = (Map<MarketDataRequirement, Object>) newValue;
          break;
        case 779431844:  // timeSeries
          this._timeSeries = (Map<MarketDataId, DateTimeSeries<LocalDate, ?>>) newValue;
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
          _marketData,
          _timeSeries);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("MapMarketDataBundle.Builder{");
      buf.append("time").append('=').append(JodaBeanUtils.toString(_time)).append(',').append(' ');
      buf.append("marketData").append('=').append(JodaBeanUtils.toString(_marketData)).append(',').append(' ');
      buf.append("timeSeries").append('=').append(JodaBeanUtils.toString(_timeSeries));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
