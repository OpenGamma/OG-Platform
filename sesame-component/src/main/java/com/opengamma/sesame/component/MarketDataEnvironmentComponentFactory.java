/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.component;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;

import java.util.LinkedHashMap;
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
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.CompositeMarketDataFactory;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.FxMatrixMarketDataBuilder;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFactory;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.IssuerMulticurveMarketDataBuilder;
import com.opengamma.sesame.marketdata.MulticurveMarketDataBuilder;
import com.opengamma.sesame.marketdata.SnapshotMarketDataFactory;
import com.opengamma.sesame.marketdata.builders.FxRateMarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.marketdata.builders.RawMarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.SecurityMarketDataBuilder;

/**
 * Component factory to build a {@link MarketDataEnvironmentFactory}.
 */
@BeanDefinition
public class MarketDataEnvironmentComponentFactory extends AbstractComponentFactory {

  /** Name under which the components will be published. */
  @PropertyDefinition(validate = "notEmpty")
  private String _classifier;

  /** Singleton components used when constructing the market data builders. */
  @PropertyDefinition(validate = "notNull")
  private ComponentMap _componentMap;

  /** For looking up time series. */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesSource _timeSeriesSource;

  /** The data source name used when looking up time series. */
  @PropertyDefinition(validate = "notEmpty")
  private String _timeSeriesDataSource;

  /** For looking up market data snapshots. */
  @PropertyDefinition(validate = "notNull")
  private MarketDataSnapshotSource _snapshotSource;

  /** Name of the currency matrix for creating FX rates. */
  @PropertyDefinition(validate = "notEmpty")
  private String _currencyMatrixName;

  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    CompositeMarketDataFactory marketDataFactory =
        new CompositeMarketDataFactory(
            new HistoricalMarketDataFactory(_timeSeriesSource, _timeSeriesDataSource, null),
            new SnapshotMarketDataFactory(_snapshotSource));

    MarketDataEnvironmentFactory environmentFactory =
        new MarketDataEnvironmentFactory(marketDataFactory,
                                         rawBuilder(),
                                         multicurveBuilder(),
                                         issuerCurveBuilder(),
                                         securityBuilder(),
                                         fxMatrixBuilder(),
                                         fxRateBuilder());

    repo.registerComponent(MarketDataEnvironmentFactory.class, _classifier, environmentFactory);
  }

  /**
   * Creates a builder that provides raw market data (tickers and values).
   *
   * @return a builder that provides raw market data (tickers and values).
   */
  protected RawMarketDataBuilder rawBuilder() {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    RawMarketDataBuilder.class,
                    argument("dataSource", _timeSeriesDataSource))));

    return FunctionModel.build(RawMarketDataBuilder.class, config, _componentMap);
  }

  /**
   * Creates a builder that provides multicurve bundles.
   *
   * @return a builder that provides multicurve bundles.
   */
  protected MulticurveMarketDataBuilder multicurveBuilder() {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                function(
                    MulticurveDiscountBuildingRepository.class,
                    argument("toleranceAbs", 1e-9),
                    argument("toleranceRel", 1e-9),
                    argument("stepMaximum", 1000)),
                function(
                    DefaultHistoricalMarketDataFn.class,
                    argument("currencyMatrix", ConfigLink.resolvable(_currencyMatrixName, CurrencyMatrix.class))),
                function(
                    ConfigDBCurveSpecificationBuilder.class,
                    argument("versionCorrection", VersionCorrection.LATEST))),
            implementations(
                CurveSpecificationBuilder.class, ConfigDBCurveSpecificationBuilder.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class));

    return FunctionModel.build(MulticurveMarketDataBuilder.class, config, _componentMap);
  }

  /**
   * Creates a builder that provides FX matrices.
   *
   * @return a builder that provides FX matrices.
   */
  protected FxMatrixMarketDataBuilder fxMatrixBuilder() {
    return new FxMatrixMarketDataBuilder();
  }

  /**
   * Creates a builder that provides FX rates.
   *
   * @return a builder that provides FX rates.
   */
  protected FxRateMarketDataBuilder fxRateBuilder() {
    return new FxRateMarketDataBuilder(ConfigLink.resolvable(_currencyMatrixName, CurrencyMatrix.class).resolve());
  }

  /**
   * Creates a builder that provides issuer curves.
   *
   * @return a builder that provides issuer curves.
   */
  protected IssuerMulticurveMarketDataBuilder issuerCurveBuilder() {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                function(
                    DefaultHistoricalMarketDataFn.class,
                    argument("currencyMatrix", ConfigLink.resolvable(_currencyMatrixName, CurrencyMatrix.class))),
                function(
                    IssuerDiscountBuildingRepository.class,
                    argument("toleranceAbs", 1e-9),
                    argument("toleranceRel", 1e-9),
                    argument("stepMaximum", 1000)),
                function(
                    ConfigDBCurveSpecificationBuilder.class,
                    argument("versionCorrection", VersionCorrection.LATEST))),
            implementations(
                CurveSpecificationBuilder.class, ConfigDBCurveSpecificationBuilder.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class));

    return FunctionModel.build(IssuerMulticurveMarketDataBuilder.class, config, _componentMap);
  }

  /**
   * Creates a builder that provides raw data for securities.
   *
   * @return a builder that provides raw data for securities.
   */
  protected SecurityMarketDataBuilder securityBuilder() {
    return new SecurityMarketDataBuilder();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MarketDataEnvironmentComponentFactory}.
   * @return the meta-bean, not null
   */
  public static MarketDataEnvironmentComponentFactory.Meta meta() {
    return MarketDataEnvironmentComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MarketDataEnvironmentComponentFactory.Meta.INSTANCE);
  }

  @Override
  public MarketDataEnvironmentComponentFactory.Meta metaBean() {
    return MarketDataEnvironmentComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets name under which the components will be published.
   * @return the value of the property, not empty
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets name under which the components will be published.
   * @param classifier  the new value of the property, not empty
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notEmpty(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets singleton components used when constructing the market data builders.
   * @return the value of the property, not null
   */
  public ComponentMap getComponentMap() {
    return _componentMap;
  }

  /**
   * Sets singleton components used when constructing the market data builders.
   * @param componentMap  the new value of the property, not null
   */
  public void setComponentMap(ComponentMap componentMap) {
    JodaBeanUtils.notNull(componentMap, "componentMap");
    this._componentMap = componentMap;
  }

  /**
   * Gets the the {@code componentMap} property.
   * @return the property, not null
   */
  public final Property<ComponentMap> componentMap() {
    return metaBean().componentMap().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets for looking up time series.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

  /**
   * Sets for looking up time series.
   * @param timeSeriesSource  the new value of the property, not null
   */
  public void setTimeSeriesSource(HistoricalTimeSeriesSource timeSeriesSource) {
    JodaBeanUtils.notNull(timeSeriesSource, "timeSeriesSource");
    this._timeSeriesSource = timeSeriesSource;
  }

  /**
   * Gets the the {@code timeSeriesSource} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesSource> timeSeriesSource() {
    return metaBean().timeSeriesSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data source name used when looking up time series.
   * @return the value of the property, not empty
   */
  public String getTimeSeriesDataSource() {
    return _timeSeriesDataSource;
  }

  /**
   * Sets the data source name used when looking up time series.
   * @param timeSeriesDataSource  the new value of the property, not empty
   */
  public void setTimeSeriesDataSource(String timeSeriesDataSource) {
    JodaBeanUtils.notEmpty(timeSeriesDataSource, "timeSeriesDataSource");
    this._timeSeriesDataSource = timeSeriesDataSource;
  }

  /**
   * Gets the the {@code timeSeriesDataSource} property.
   * @return the property, not null
   */
  public final Property<String> timeSeriesDataSource() {
    return metaBean().timeSeriesDataSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets for looking up market data snapshots.
   * @return the value of the property, not null
   */
  public MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

  /**
   * Sets for looking up market data snapshots.
   * @param snapshotSource  the new value of the property, not null
   */
  public void setSnapshotSource(MarketDataSnapshotSource snapshotSource) {
    JodaBeanUtils.notNull(snapshotSource, "snapshotSource");
    this._snapshotSource = snapshotSource;
  }

  /**
   * Gets the the {@code snapshotSource} property.
   * @return the property, not null
   */
  public final Property<MarketDataSnapshotSource> snapshotSource() {
    return metaBean().snapshotSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets name of the currency matrix for creating FX rates.
   * @return the value of the property, not empty
   */
  public String getCurrencyMatrixName() {
    return _currencyMatrixName;
  }

  /**
   * Sets name of the currency matrix for creating FX rates.
   * @param currencyMatrixName  the new value of the property, not empty
   */
  public void setCurrencyMatrixName(String currencyMatrixName) {
    JodaBeanUtils.notEmpty(currencyMatrixName, "currencyMatrixName");
    this._currencyMatrixName = currencyMatrixName;
  }

  /**
   * Gets the the {@code currencyMatrixName} property.
   * @return the property, not null
   */
  public final Property<String> currencyMatrixName() {
    return metaBean().currencyMatrixName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MarketDataEnvironmentComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MarketDataEnvironmentComponentFactory other = (MarketDataEnvironmentComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getComponentMap(), other.getComponentMap()) &&
          JodaBeanUtils.equal(getTimeSeriesSource(), other.getTimeSeriesSource()) &&
          JodaBeanUtils.equal(getTimeSeriesDataSource(), other.getTimeSeriesDataSource()) &&
          JodaBeanUtils.equal(getSnapshotSource(), other.getSnapshotSource()) &&
          JodaBeanUtils.equal(getCurrencyMatrixName(), other.getCurrencyMatrixName()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getComponentMap());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTimeSeriesSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTimeSeriesDataSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSnapshotSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrencyMatrixName());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("MarketDataEnvironmentComponentFactory{");
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
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("componentMap").append('=').append(JodaBeanUtils.toString(getComponentMap())).append(',').append(' ');
    buf.append("timeSeriesSource").append('=').append(JodaBeanUtils.toString(getTimeSeriesSource())).append(',').append(' ');
    buf.append("timeSeriesDataSource").append('=').append(JodaBeanUtils.toString(getTimeSeriesDataSource())).append(',').append(' ');
    buf.append("snapshotSource").append('=').append(JodaBeanUtils.toString(getSnapshotSource())).append(',').append(' ');
    buf.append("currencyMatrixName").append('=').append(JodaBeanUtils.toString(getCurrencyMatrixName())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MarketDataEnvironmentComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", MarketDataEnvironmentComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code componentMap} property.
     */
    private final MetaProperty<ComponentMap> _componentMap = DirectMetaProperty.ofReadWrite(
        this, "componentMap", MarketDataEnvironmentComponentFactory.class, ComponentMap.class);
    /**
     * The meta-property for the {@code timeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _timeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "timeSeriesSource", MarketDataEnvironmentComponentFactory.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code timeSeriesDataSource} property.
     */
    private final MetaProperty<String> _timeSeriesDataSource = DirectMetaProperty.ofReadWrite(
        this, "timeSeriesDataSource", MarketDataEnvironmentComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code snapshotSource} property.
     */
    private final MetaProperty<MarketDataSnapshotSource> _snapshotSource = DirectMetaProperty.ofReadWrite(
        this, "snapshotSource", MarketDataEnvironmentComponentFactory.class, MarketDataSnapshotSource.class);
    /**
     * The meta-property for the {@code currencyMatrixName} property.
     */
    private final MetaProperty<String> _currencyMatrixName = DirectMetaProperty.ofReadWrite(
        this, "currencyMatrixName", MarketDataEnvironmentComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "componentMap",
        "timeSeriesSource",
        "timeSeriesDataSource",
        "snapshotSource",
        "currencyMatrixName");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -499150049:  // componentMap
          return _componentMap;
        case 2131310815:  // timeSeriesSource
          return _timeSeriesSource;
        case -2110575831:  // timeSeriesDataSource
          return _timeSeriesDataSource;
        case -1862154497:  // snapshotSource
          return _snapshotSource;
        case 1305503965:  // currencyMatrixName
          return _currencyMatrixName;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MarketDataEnvironmentComponentFactory> builder() {
      return new DirectBeanBuilder<MarketDataEnvironmentComponentFactory>(new MarketDataEnvironmentComponentFactory());
    }

    @Override
    public Class<? extends MarketDataEnvironmentComponentFactory> beanType() {
      return MarketDataEnvironmentComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code componentMap} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComponentMap> componentMap() {
      return _componentMap;
    }

    /**
     * The meta-property for the {@code timeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> timeSeriesSource() {
      return _timeSeriesSource;
    }

    /**
     * The meta-property for the {@code timeSeriesDataSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> timeSeriesDataSource() {
      return _timeSeriesDataSource;
    }

    /**
     * The meta-property for the {@code snapshotSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotSource> snapshotSource() {
      return _snapshotSource;
    }

    /**
     * The meta-property for the {@code currencyMatrixName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> currencyMatrixName() {
      return _currencyMatrixName;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((MarketDataEnvironmentComponentFactory) bean).getClassifier();
        case -499150049:  // componentMap
          return ((MarketDataEnvironmentComponentFactory) bean).getComponentMap();
        case 2131310815:  // timeSeriesSource
          return ((MarketDataEnvironmentComponentFactory) bean).getTimeSeriesSource();
        case -2110575831:  // timeSeriesDataSource
          return ((MarketDataEnvironmentComponentFactory) bean).getTimeSeriesDataSource();
        case -1862154497:  // snapshotSource
          return ((MarketDataEnvironmentComponentFactory) bean).getSnapshotSource();
        case 1305503965:  // currencyMatrixName
          return ((MarketDataEnvironmentComponentFactory) bean).getCurrencyMatrixName();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((MarketDataEnvironmentComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -499150049:  // componentMap
          ((MarketDataEnvironmentComponentFactory) bean).setComponentMap((ComponentMap) newValue);
          return;
        case 2131310815:  // timeSeriesSource
          ((MarketDataEnvironmentComponentFactory) bean).setTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
          return;
        case -2110575831:  // timeSeriesDataSource
          ((MarketDataEnvironmentComponentFactory) bean).setTimeSeriesDataSource((String) newValue);
          return;
        case -1862154497:  // snapshotSource
          ((MarketDataEnvironmentComponentFactory) bean).setSnapshotSource((MarketDataSnapshotSource) newValue);
          return;
        case 1305503965:  // currencyMatrixName
          ((MarketDataEnvironmentComponentFactory) bean).setCurrencyMatrixName((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notEmpty(((MarketDataEnvironmentComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((MarketDataEnvironmentComponentFactory) bean)._componentMap, "componentMap");
      JodaBeanUtils.notNull(((MarketDataEnvironmentComponentFactory) bean)._timeSeriesSource, "timeSeriesSource");
      JodaBeanUtils.notEmpty(((MarketDataEnvironmentComponentFactory) bean)._timeSeriesDataSource, "timeSeriesDataSource");
      JodaBeanUtils.notNull(((MarketDataEnvironmentComponentFactory) bean)._snapshotSource, "snapshotSource");
      JodaBeanUtils.notEmpty(((MarketDataEnvironmentComponentFactory) bean)._currencyMatrixName, "currencyMatrixName");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
