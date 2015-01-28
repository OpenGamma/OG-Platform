/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

import org.joda.beans.Bean;
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
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.config.FunctionArguments;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.util.ArgumentChecker;

/**
 * Arguments used to drive the calculations in a single calculation cycle.
 */
@BeanDefinition
public final class CalculationArguments implements ImmutableBean {

  /** The valuation time for the calculations. If this isn't set, the time from the market data environment is used */
  @Nullable
  @PropertyDefinition
  private final ZonedDateTime _valuationTime;

  /** The version correction used when loading configuration, defaults to the latest version. */
  @Nullable
  @PropertyDefinition
  private final VersionCorrection _configVersionCorrection;

  /** Specification of the source of market data that should be used for data that isn't supplied by the user. */
  @Nullable
  @PropertyDefinition(get = "manual")
  private final MarketDataSpecification _marketDataSpecification;

  /** Specifies if tracing should be enabled for calculations for specific cells in the results. */
  @PropertyDefinition(validate = "notNull")
  private final Map<Cell, TraceType> _traceCells;

  /** Specifies if tracing should be enabled for calculations for specific non-portfolio outputs. */
  @PropertyDefinition(validate = "notNull")
  private final Map<String, TraceType> _traceOutputs;

  /** Arguments passed into top-level functions by the engine. */
  @PropertyDefinition(validate = "notNull")
  private final Map<Class<?>, FunctionArguments> _functionArguments;

  /**
   * Whether all input data should be captured for the run, including trades, configuration and market data.
   * <p>
   * This allows a calculation to be completely recreated at a later time, for example in a regression test.
   * It causes a large memory overhead and reduction in performance.
   */
  @PropertyDefinition
  private final boolean _captureInputs;

  /**
   * @param output the name of a non-portfolio output
   * @return the type of tracing that should be performed for the output
   */
  TraceType traceType(String output) {
    return _traceOutputs.containsKey(output) ? _traceOutputs.get(output) : TraceType.NONE;
  }

  /**
   * @param rowIndex the row index of a cell
   * @param colIndex the column index of a cell
   * @return the type of tracing that should be performed for the cell
   */
  TraceType traceType(int rowIndex, int colIndex) {
    Cell cell = Cell.of(rowIndex, colIndex);
    return _traceCells.containsKey(cell) ? _traceCells.get(cell) : TraceType.NONE;
  }

  /**
   * @return the specification of the source of market data that should be used for data that isn't supplied by the user
   */
  public MarketDataSpecification getMarketDataSpecification() {
    // TODO remove this when we upgrade to the version of Joda Beans that supports default values for immutable beans
    return _marketDataSpecification != null ? _marketDataSpecification : EmptyMarketDataSpec.INSTANCE;
  }

  @ImmutableConstructor
  private CalculationArguments(
      ZonedDateTime valuationTime,
      VersionCorrection configVersionCorrection,
      MarketDataSpecification marketDataSpecification,
      Map<Cell, TraceType> traceCells,
      Map<String, TraceType> traceOutputs,
      Map<Class<?>, FunctionArguments> functionArguments,
      boolean captureInputs) {
    ArgumentChecker.notNull(traceCells, "traceCells");
    ArgumentChecker.notNull(traceOutputs, "traceOutputs");
    ArgumentChecker.notNull(functionArguments, "functionArguments");
    // Convert valuation time to be in UTC as all other ZonedDateTimes are
    this._valuationTime = valuationTime == null ? null : valuationTime.withZoneSameInstant(ZoneOffset.UTC);
    this._configVersionCorrection = configVersionCorrection;
    this._marketDataSpecification = marketDataSpecification;
    this._traceCells = ImmutableMap.copyOf(traceCells);
    this._traceOutputs = ImmutableMap.copyOf(traceOutputs);
    this._functionArguments = ImmutableMap.copyOf(functionArguments);
    this._captureInputs = captureInputs;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CalculationArguments}.
   * @return the meta-bean, not null
   */
  public static CalculationArguments.Meta meta() {
    return CalculationArguments.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CalculationArguments.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CalculationArguments.Builder builder() {
    return new CalculationArguments.Builder();
  }

  @Override
  public CalculationArguments.Meta metaBean() {
    return CalculationArguments.Meta.INSTANCE;
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
   * Gets the valuation time for the calculations. If this isn't set, the time from the market data environment is used
   * @return the value of the property
   */
  public ZonedDateTime getValuationTime() {
    return _valuationTime;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the version correction used when loading configuration, defaults to the latest version.
   * @return the value of the property
   */
  public VersionCorrection getConfigVersionCorrection() {
    return _configVersionCorrection;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets specifies if tracing should be enabled for calculations for specific cells in the results.
   * @return the value of the property, not null
   */
  public Map<Cell, TraceType> getTraceCells() {
    return _traceCells;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets specifies if tracing should be enabled for calculations for specific non-portfolio outputs.
   * @return the value of the property, not null
   */
  public Map<String, TraceType> getTraceOutputs() {
    return _traceOutputs;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets arguments passed into top-level functions by the engine.
   * @return the value of the property, not null
   */
  public Map<Class<?>, FunctionArguments> getFunctionArguments() {
    return _functionArguments;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether all input data should be captured for the run, including trades, configuration and market data.
   * <p>
   * This allows a calculation to be completely recreated at a later time, for example in a regression test.
   * It causes a large memory overhead and reduction in performance.
   * @return the value of the property
   */
  public boolean isCaptureInputs() {
    return _captureInputs;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CalculationArguments other = (CalculationArguments) obj;
      return JodaBeanUtils.equal(getValuationTime(), other.getValuationTime()) &&
          JodaBeanUtils.equal(getConfigVersionCorrection(), other.getConfigVersionCorrection()) &&
          JodaBeanUtils.equal(getMarketDataSpecification(), other.getMarketDataSpecification()) &&
          JodaBeanUtils.equal(getTraceCells(), other.getTraceCells()) &&
          JodaBeanUtils.equal(getTraceOutputs(), other.getTraceOutputs()) &&
          JodaBeanUtils.equal(getFunctionArguments(), other.getFunctionArguments()) &&
          (isCaptureInputs() == other.isCaptureInputs());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigVersionCorrection());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMarketDataSpecification());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTraceCells());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTraceOutputs());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFunctionArguments());
    hash = hash * 31 + JodaBeanUtils.hashCode(isCaptureInputs());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("CalculationArguments{");
    buf.append("valuationTime").append('=').append(getValuationTime()).append(',').append(' ');
    buf.append("configVersionCorrection").append('=').append(getConfigVersionCorrection()).append(',').append(' ');
    buf.append("marketDataSpecification").append('=').append(getMarketDataSpecification()).append(',').append(' ');
    buf.append("traceCells").append('=').append(getTraceCells()).append(',').append(' ');
    buf.append("traceOutputs").append('=').append(getTraceOutputs()).append(',').append(' ');
    buf.append("functionArguments").append('=').append(getFunctionArguments()).append(',').append(' ');
    buf.append("captureInputs").append('=').append(JodaBeanUtils.toString(isCaptureInputs()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CalculationArguments}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<ZonedDateTime> _valuationTime = DirectMetaProperty.ofImmutable(
        this, "valuationTime", CalculationArguments.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code configVersionCorrection} property.
     */
    private final MetaProperty<VersionCorrection> _configVersionCorrection = DirectMetaProperty.ofImmutable(
        this, "configVersionCorrection", CalculationArguments.class, VersionCorrection.class);
    /**
     * The meta-property for the {@code marketDataSpecification} property.
     */
    private final MetaProperty<MarketDataSpecification> _marketDataSpecification = DirectMetaProperty.ofImmutable(
        this, "marketDataSpecification", CalculationArguments.class, MarketDataSpecification.class);
    /**
     * The meta-property for the {@code traceCells} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Cell, TraceType>> _traceCells = DirectMetaProperty.ofImmutable(
        this, "traceCells", CalculationArguments.class, (Class) Map.class);
    /**
     * The meta-property for the {@code traceOutputs} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, TraceType>> _traceOutputs = DirectMetaProperty.ofImmutable(
        this, "traceOutputs", CalculationArguments.class, (Class) Map.class);
    /**
     * The meta-property for the {@code functionArguments} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Class<?>, FunctionArguments>> _functionArguments = DirectMetaProperty.ofImmutable(
        this, "functionArguments", CalculationArguments.class, (Class) Map.class);
    /**
     * The meta-property for the {@code captureInputs} property.
     */
    private final MetaProperty<Boolean> _captureInputs = DirectMetaProperty.ofImmutable(
        this, "captureInputs", CalculationArguments.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "valuationTime",
        "configVersionCorrection",
        "marketDataSpecification",
        "traceCells",
        "traceOutputs",
        "functionArguments",
        "captureInputs");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 113591406:  // valuationTime
          return _valuationTime;
        case -1666952396:  // configVersionCorrection
          return _configVersionCorrection;
        case -498995043:  // marketDataSpecification
          return _marketDataSpecification;
        case 961856780:  // traceCells
          return _traceCells;
        case -842881395:  // traceOutputs
          return _traceOutputs;
        case -260573090:  // functionArguments
          return _functionArguments;
        case 1669810383:  // captureInputs
          return _captureInputs;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CalculationArguments.Builder builder() {
      return new CalculationArguments.Builder();
    }

    @Override
    public Class<? extends CalculationArguments> beanType() {
      return CalculationArguments.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code valuationTime} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ZonedDateTime> valuationTime() {
      return _valuationTime;
    }

    /**
     * The meta-property for the {@code configVersionCorrection} property.
     * @return the meta-property, not null
     */
    public MetaProperty<VersionCorrection> configVersionCorrection() {
      return _configVersionCorrection;
    }

    /**
     * The meta-property for the {@code marketDataSpecification} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataSpecification> marketDataSpecification() {
      return _marketDataSpecification;
    }

    /**
     * The meta-property for the {@code traceCells} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<Cell, TraceType>> traceCells() {
      return _traceCells;
    }

    /**
     * The meta-property for the {@code traceOutputs} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<String, TraceType>> traceOutputs() {
      return _traceOutputs;
    }

    /**
     * The meta-property for the {@code functionArguments} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<Class<?>, FunctionArguments>> functionArguments() {
      return _functionArguments;
    }

    /**
     * The meta-property for the {@code captureInputs} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Boolean> captureInputs() {
      return _captureInputs;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 113591406:  // valuationTime
          return ((CalculationArguments) bean).getValuationTime();
        case -1666952396:  // configVersionCorrection
          return ((CalculationArguments) bean).getConfigVersionCorrection();
        case -498995043:  // marketDataSpecification
          return ((CalculationArguments) bean).getMarketDataSpecification();
        case 961856780:  // traceCells
          return ((CalculationArguments) bean).getTraceCells();
        case -842881395:  // traceOutputs
          return ((CalculationArguments) bean).getTraceOutputs();
        case -260573090:  // functionArguments
          return ((CalculationArguments) bean).getFunctionArguments();
        case 1669810383:  // captureInputs
          return ((CalculationArguments) bean).isCaptureInputs();
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
   * The bean-builder for {@code CalculationArguments}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CalculationArguments> {

    private ZonedDateTime _valuationTime;
    private VersionCorrection _configVersionCorrection;
    private MarketDataSpecification _marketDataSpecification;
    private Map<Cell, TraceType> _traceCells = new HashMap<Cell, TraceType>();
    private Map<String, TraceType> _traceOutputs = new HashMap<String, TraceType>();
    private Map<Class<?>, FunctionArguments> _functionArguments = new HashMap<Class<?>, FunctionArguments>();
    private boolean _captureInputs;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CalculationArguments beanToCopy) {
      this._valuationTime = beanToCopy.getValuationTime();
      this._configVersionCorrection = beanToCopy.getConfigVersionCorrection();
      this._marketDataSpecification = beanToCopy.getMarketDataSpecification();
      this._traceCells = new HashMap<Cell, TraceType>(beanToCopy.getTraceCells());
      this._traceOutputs = new HashMap<String, TraceType>(beanToCopy.getTraceOutputs());
      this._functionArguments = new HashMap<Class<?>, FunctionArguments>(beanToCopy.getFunctionArguments());
      this._captureInputs = beanToCopy.isCaptureInputs();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 113591406:  // valuationTime
          return _valuationTime;
        case -1666952396:  // configVersionCorrection
          return _configVersionCorrection;
        case -498995043:  // marketDataSpecification
          return _marketDataSpecification;
        case 961856780:  // traceCells
          return _traceCells;
        case -842881395:  // traceOutputs
          return _traceOutputs;
        case -260573090:  // functionArguments
          return _functionArguments;
        case 1669810383:  // captureInputs
          return _captureInputs;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 113591406:  // valuationTime
          this._valuationTime = (ZonedDateTime) newValue;
          break;
        case -1666952396:  // configVersionCorrection
          this._configVersionCorrection = (VersionCorrection) newValue;
          break;
        case -498995043:  // marketDataSpecification
          this._marketDataSpecification = (MarketDataSpecification) newValue;
          break;
        case 961856780:  // traceCells
          this._traceCells = (Map<Cell, TraceType>) newValue;
          break;
        case -842881395:  // traceOutputs
          this._traceOutputs = (Map<String, TraceType>) newValue;
          break;
        case -260573090:  // functionArguments
          this._functionArguments = (Map<Class<?>, FunctionArguments>) newValue;
          break;
        case 1669810383:  // captureInputs
          this._captureInputs = (Boolean) newValue;
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
    public CalculationArguments build() {
      return new CalculationArguments(
          _valuationTime,
          _configVersionCorrection,
          _marketDataSpecification,
          _traceCells,
          _traceOutputs,
          _functionArguments,
          _captureInputs);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code valuationTime} property in the builder.
     * @param valuationTime  the new value
     * @return this, for chaining, not null
     */
    public Builder valuationTime(ZonedDateTime valuationTime) {
      this._valuationTime = valuationTime;
      return this;
    }

    /**
     * Sets the {@code configVersionCorrection} property in the builder.
     * @param configVersionCorrection  the new value
     * @return this, for chaining, not null
     */
    public Builder configVersionCorrection(VersionCorrection configVersionCorrection) {
      this._configVersionCorrection = configVersionCorrection;
      return this;
    }

    /**
     * Sets the {@code marketDataSpecification} property in the builder.
     * @param marketDataSpecification  the new value
     * @return this, for chaining, not null
     */
    public Builder marketDataSpecification(MarketDataSpecification marketDataSpecification) {
      this._marketDataSpecification = marketDataSpecification;
      return this;
    }

    /**
     * Sets the {@code traceCells} property in the builder.
     * @param traceCells  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder traceCells(Map<Cell, TraceType> traceCells) {
      JodaBeanUtils.notNull(traceCells, "traceCells");
      this._traceCells = traceCells;
      return this;
    }

    /**
     * Sets the {@code traceOutputs} property in the builder.
     * @param traceOutputs  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder traceOutputs(Map<String, TraceType> traceOutputs) {
      JodaBeanUtils.notNull(traceOutputs, "traceOutputs");
      this._traceOutputs = traceOutputs;
      return this;
    }

    /**
     * Sets the {@code functionArguments} property in the builder.
     * @param functionArguments  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder functionArguments(Map<Class<?>, FunctionArguments> functionArguments) {
      JodaBeanUtils.notNull(functionArguments, "functionArguments");
      this._functionArguments = functionArguments;
      return this;
    }

    /**
     * Sets the {@code captureInputs} property in the builder.
     * @param captureInputs  the new value
     * @return this, for chaining, not null
     */
    public Builder captureInputs(boolean captureInputs) {
      this._captureInputs = captureInputs;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(256);
      buf.append("CalculationArguments.Builder{");
      buf.append("valuationTime").append('=').append(JodaBeanUtils.toString(_valuationTime)).append(',').append(' ');
      buf.append("configVersionCorrection").append('=').append(JodaBeanUtils.toString(_configVersionCorrection)).append(',').append(' ');
      buf.append("marketDataSpecification").append('=').append(JodaBeanUtils.toString(_marketDataSpecification)).append(',').append(' ');
      buf.append("traceCells").append('=').append(JodaBeanUtils.toString(_traceCells)).append(',').append(' ');
      buf.append("traceOutputs").append('=').append(JodaBeanUtils.toString(_traceOutputs)).append(',').append(' ');
      buf.append("functionArguments").append('=').append(JodaBeanUtils.toString(_functionArguments)).append(',').append(' ');
      buf.append("captureInputs").append('=').append(JodaBeanUtils.toString(_captureInputs));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
