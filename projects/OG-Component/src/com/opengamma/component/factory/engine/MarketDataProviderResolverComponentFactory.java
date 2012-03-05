/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.CombinedMarketDataProviderFactory;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.historical.HistoricalMarketDataProviderFactory;
import com.opengamma.engine.marketdata.historical.LatestHistoricalMarketDataProviderFactory;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.resolver.TypeBasedMarketDataProviderResolver;
import com.opengamma.engine.marketdata.snapshot.UserMarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.test.OptimisticMarketDataAvailabilityProvider;

/**
 * Component factory for the market data provider resolver.
 */
@BeanDefinition
public class MarketDataProviderResolverComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier under which to publish.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The live market data provider factory.
   */
  @PropertyDefinition(validate = "notNull")
  private MarketDataProviderFactory _liveMarketDataProviderFactory;
  /**
   * The historical time-series source.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * The market data snapshot source.
   */
  @PropertyDefinition(validate = "notNull")  
  private MarketDataSnapshotSource _marketDataSnapshotSource;
  
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    initMarketDataProviderResolver(repo);
  }

  private MarketDataProviderResolver initMarketDataProviderResolver(ComponentRepository repo) {
    TypeBasedMarketDataProviderResolver providerResolver = new TypeBasedMarketDataProviderResolver();
    
    providerResolver.addProvider(LiveMarketDataSpecification.class, getLiveMarketDataProviderFactory());
    
    MarketDataProviderFactory fixedHistoricalMarketDataProviderFactory = initFixedHistoricalMarketDataProviderFactory();
    providerResolver.addProvider(FixedHistoricalMarketDataSpecification.class, fixedHistoricalMarketDataProviderFactory);
    
    MarketDataProviderFactory latestHistoricalMarketDataProviderFactory = initLatestHistoricalMarketDataProviderFactory();
    providerResolver.addProvider(LatestHistoricalMarketDataSpecification.class, latestHistoricalMarketDataProviderFactory);
    
    MarketDataProviderFactory userMarketDataProviderFactory = initUserMarketDataProviderFactory();
    providerResolver.addProvider(UserMarketDataSpecification.class, userMarketDataProviderFactory);
    
    MarketDataProviderFactory combinedMarketDataProviderFactory = initCombinedMarketDataProviderFactory(providerResolver);
    providerResolver.addProvider(CombinedMarketDataSpecification.class, combinedMarketDataProviderFactory);
    
    ComponentInfo info = new ComponentInfo(MarketDataProviderResolver.class, getClassifier());
    repo.registerComponent(info, providerResolver);
    return providerResolver;
  }
  
  protected MarketDataProviderFactory initFixedHistoricalMarketDataProviderFactory() {
    return new HistoricalMarketDataProviderFactory(getHistoricalTimeSeriesSource());
  }
  
  protected MarketDataProviderFactory initLatestHistoricalMarketDataProviderFactory() {
    return new LatestHistoricalMarketDataProviderFactory(getHistoricalTimeSeriesSource());
  }
  
  protected MarketDataProviderFactory initUserMarketDataProviderFactory() {
    return new UserMarketDataProviderFactory(getMarketDataSnapshotSource(), new OptimisticMarketDataAvailabilityProvider());
  }
  
  protected MarketDataProviderFactory initCombinedMarketDataProviderFactory(MarketDataProviderResolver underlyingResolver) {
    return new CombinedMarketDataProviderFactory(underlyingResolver);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MarketDataProviderResolverComponentFactory}.
   * @return the meta-bean, not null
   */
  public static MarketDataProviderResolverComponentFactory.Meta meta() {
    return MarketDataProviderResolverComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(MarketDataProviderResolverComponentFactory.Meta.INSTANCE);
  }

  @Override
  public MarketDataProviderResolverComponentFactory.Meta metaBean() {
    return MarketDataProviderResolverComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -301472921:  // liveMarketDataProviderFactory
        return getLiveMarketDataProviderFactory();
      case 358729161:  // historicalTimeSeriesSource
        return getHistoricalTimeSeriesSource();
      case -2019554651:  // marketDataSnapshotSource
        return getMarketDataSnapshotSource();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -301472921:  // liveMarketDataProviderFactory
        setLiveMarketDataProviderFactory((MarketDataProviderFactory) newValue);
        return;
      case 358729161:  // historicalTimeSeriesSource
        setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
        return;
      case -2019554651:  // marketDataSnapshotSource
        setMarketDataSnapshotSource((MarketDataSnapshotSource) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_liveMarketDataProviderFactory, "liveMarketDataProviderFactory");
    JodaBeanUtils.notNull(_historicalTimeSeriesSource, "historicalTimeSeriesSource");
    JodaBeanUtils.notNull(_marketDataSnapshotSource, "marketDataSnapshotSource");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MarketDataProviderResolverComponentFactory other = (MarketDataProviderResolverComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getLiveMarketDataProviderFactory(), other.getLiveMarketDataProviderFactory()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotSource(), other.getMarketDataSnapshotSource()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLiveMarketDataProviderFactory());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotSource());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier under which to publish.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier under which to publish.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
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
   * Gets the live market data provider factory.
   * @return the value of the property, not null
   */
  public MarketDataProviderFactory getLiveMarketDataProviderFactory() {
    return _liveMarketDataProviderFactory;
  }

  /**
   * Sets the live market data provider factory.
   * @param liveMarketDataProviderFactory  the new value of the property, not null
   */
  public void setLiveMarketDataProviderFactory(MarketDataProviderFactory liveMarketDataProviderFactory) {
    JodaBeanUtils.notNull(liveMarketDataProviderFactory, "liveMarketDataProviderFactory");
    this._liveMarketDataProviderFactory = liveMarketDataProviderFactory;
  }

  /**
   * Gets the the {@code liveMarketDataProviderFactory} property.
   * @return the property, not null
   */
  public final Property<MarketDataProviderFactory> liveMarketDataProviderFactory() {
    return metaBean().liveMarketDataProviderFactory().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the historical time-series source.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  /**
   * Sets the historical time-series source.
   * @param historicalTimeSeriesSource  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    JodaBeanUtils.notNull(historicalTimeSeriesSource, "historicalTimeSeriesSource");
    this._historicalTimeSeriesSource = historicalTimeSeriesSource;
  }

  /**
   * Gets the the {@code historicalTimeSeriesSource} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
    return metaBean().historicalTimeSeriesSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data snapshot source.
   * @return the value of the property, not null
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource() {
    return _marketDataSnapshotSource;
  }

  /**
   * Sets the market data snapshot source.
   * @param marketDataSnapshotSource  the new value of the property, not null
   */
  public void setMarketDataSnapshotSource(MarketDataSnapshotSource marketDataSnapshotSource) {
    JodaBeanUtils.notNull(marketDataSnapshotSource, "marketDataSnapshotSource");
    this._marketDataSnapshotSource = marketDataSnapshotSource;
  }

  /**
   * Gets the the {@code marketDataSnapshotSource} property.
   * @return the property, not null
   */
  public final Property<MarketDataSnapshotSource> marketDataSnapshotSource() {
    return metaBean().marketDataSnapshotSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MarketDataProviderResolverComponentFactory}.
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
        this, "classifier", MarketDataProviderResolverComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code liveMarketDataProviderFactory} property.
     */
    private final MetaProperty<MarketDataProviderFactory> _liveMarketDataProviderFactory = DirectMetaProperty.ofReadWrite(
        this, "liveMarketDataProviderFactory", MarketDataProviderResolverComponentFactory.class, MarketDataProviderFactory.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _historicalTimeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesSource", MarketDataProviderResolverComponentFactory.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code marketDataSnapshotSource} property.
     */
    private final MetaProperty<MarketDataSnapshotSource> _marketDataSnapshotSource = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotSource", MarketDataProviderResolverComponentFactory.class, MarketDataSnapshotSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "liveMarketDataProviderFactory",
        "historicalTimeSeriesSource",
        "marketDataSnapshotSource");

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
        case -301472921:  // liveMarketDataProviderFactory
          return _liveMarketDataProviderFactory;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
        case -2019554651:  // marketDataSnapshotSource
          return _marketDataSnapshotSource;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MarketDataProviderResolverComponentFactory> builder() {
      return new DirectBeanBuilder<MarketDataProviderResolverComponentFactory>(new MarketDataProviderResolverComponentFactory());
    }

    @Override
    public Class<? extends MarketDataProviderResolverComponentFactory> beanType() {
      return MarketDataProviderResolverComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
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
     * The meta-property for the {@code liveMarketDataProviderFactory} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataProviderFactory> liveMarketDataProviderFactory() {
      return _liveMarketDataProviderFactory;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
      return _historicalTimeSeriesSource;
    }

    /**
     * The meta-property for the {@code marketDataSnapshotSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotSource> marketDataSnapshotSource() {
      return _marketDataSnapshotSource;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
