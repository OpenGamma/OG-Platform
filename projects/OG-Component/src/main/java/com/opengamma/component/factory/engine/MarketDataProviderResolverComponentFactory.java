/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

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

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.CombinedMarketDataProviderFactory;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.historical.HistoricalMarketDataProviderFactory;
import com.opengamma.engine.marketdata.historical.HistoricalShockMarketDataProviderFactory;
import com.opengamma.engine.marketdata.historical.LatestHistoricalMarketDataProviderFactory;
import com.opengamma.engine.marketdata.random.RandomizingMarketDataProviderFactory;
import com.opengamma.engine.marketdata.resolver.CachingMarketDataProviderResolver;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.resolver.TypeBasedMarketDataProviderResolver;
import com.opengamma.engine.marketdata.snapshot.UserMarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.HistoricalShockMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.RandomizingMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;

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
   * The live market data provider factory. May be null if no live data required.
   */
  @PropertyDefinition()
  private MarketDataProviderFactory _liveMarketDataProviderFactory;
  /**
   * The historical time-series source.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * The historical time-series resolver.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesResolver _historicalTimeSeriesResolver;
  /**
   * The market data snapshot source.
   */
  @PropertyDefinition(validate = "notNull")
  private MarketDataSnapshotSource _marketDataSnapshotSource;

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    initMarketDataProviderResolver(repo);
  }

  protected MarketDataProviderResolver createMarketDataProviderResolver() {
    final TypeBasedMarketDataProviderResolver providerResolver = new TypeBasedMarketDataProviderResolver();
    if (getLiveMarketDataProviderFactory() != null) {
      providerResolver.addProvider(LiveMarketDataSpecification.class, getLiveMarketDataProviderFactory());
    }
    final MarketDataProviderFactory fixedHistoricalMarketDataProviderFactory = initFixedHistoricalMarketDataProviderFactory();
    providerResolver.addProvider(FixedHistoricalMarketDataSpecification.class, fixedHistoricalMarketDataProviderFactory);
    final MarketDataProviderFactory latestHistoricalMarketDataProviderFactory = initLatestHistoricalMarketDataProviderFactory();
    providerResolver.addProvider(LatestHistoricalMarketDataSpecification.class, latestHistoricalMarketDataProviderFactory);
    final MarketDataProviderFactory userMarketDataProviderFactory = initUserMarketDataProviderFactory();
    providerResolver.addProvider(UserMarketDataSpecification.class, userMarketDataProviderFactory);
    final MarketDataProviderFactory combinedMarketDataProviderFactory = initCombinedMarketDataProviderFactory(providerResolver);
    providerResolver.addProvider(CombinedMarketDataSpecification.class, combinedMarketDataProviderFactory);
    final MarketDataProviderFactory historicalShockMarketDataProviderFactory = initHistoricalShockMarketDataProviderFactory(providerResolver);
    providerResolver.addProvider(HistoricalShockMarketDataSpecification.class, historicalShockMarketDataProviderFactory);
    final MarketDataProviderFactory randomizingMarketDataProviderFactory = initRandomizingMarketDataProviderFactory(providerResolver);
    providerResolver.addProvider(RandomizingMarketDataSpecification.class, randomizingMarketDataProviderFactory);
    return providerResolver;
  }

  private void initMarketDataProviderResolver(final ComponentRepository repo) {
    final MarketDataProviderResolver resolver = new CachingMarketDataProviderResolver(createMarketDataProviderResolver());
    final ComponentInfo info = new ComponentInfo(MarketDataProviderResolver.class, getClassifier());
    repo.registerComponent(info, resolver);
  }

  private MarketDataProviderFactory initRandomizingMarketDataProviderFactory(MarketDataProviderResolver resolver) {
    return new RandomizingMarketDataProviderFactory(resolver);
  }

  protected MarketDataProviderFactory initFixedHistoricalMarketDataProviderFactory() {
    return new HistoricalMarketDataProviderFactory(getHistoricalTimeSeriesSource(), getHistoricalTimeSeriesResolver());
  }

  protected MarketDataProviderFactory initLatestHistoricalMarketDataProviderFactory() {
    return new LatestHistoricalMarketDataProviderFactory(getHistoricalTimeSeriesSource(), getHistoricalTimeSeriesResolver());
  }

  protected MarketDataProviderFactory initUserMarketDataProviderFactory() {
    return new UserMarketDataProviderFactory(getMarketDataSnapshotSource());
  }

  protected MarketDataProviderFactory initCombinedMarketDataProviderFactory(final MarketDataProviderResolver underlyingResolver) {
    return new CombinedMarketDataProviderFactory(underlyingResolver);
  }

  protected MarketDataProviderFactory initHistoricalShockMarketDataProviderFactory(MarketDataProviderResolver underlyingResolver) {
    return new HistoricalShockMarketDataProviderFactory(underlyingResolver);
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
   * Gets the live market data provider factory. May be null if no live data required.
   * @return the value of the property
   */
  public MarketDataProviderFactory getLiveMarketDataProviderFactory() {
    return _liveMarketDataProviderFactory;
  }

  /**
   * Sets the live market data provider factory. May be null if no live data required.
   * @param liveMarketDataProviderFactory  the new value of the property
   */
  public void setLiveMarketDataProviderFactory(MarketDataProviderFactory liveMarketDataProviderFactory) {
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
   * Gets the historical time-series resolver.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesResolver getHistoricalTimeSeriesResolver() {
    return _historicalTimeSeriesResolver;
  }

  /**
   * Sets the historical time-series resolver.
   * @param historicalTimeSeriesResolver  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesResolver(HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    JodaBeanUtils.notNull(historicalTimeSeriesResolver, "historicalTimeSeriesResolver");
    this._historicalTimeSeriesResolver = historicalTimeSeriesResolver;
  }

  /**
   * Gets the the {@code historicalTimeSeriesResolver} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesResolver> historicalTimeSeriesResolver() {
    return metaBean().historicalTimeSeriesResolver().createProperty(this);
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
  @Override
  public MarketDataProviderResolverComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
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
          JodaBeanUtils.equal(getHistoricalTimeSeriesResolver(), other.getHistoricalTimeSeriesResolver()) &&
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
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesResolver());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotSource());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("MarketDataProviderResolverComponentFactory{");
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
    buf.append("liveMarketDataProviderFactory").append('=').append(JodaBeanUtils.toString(getLiveMarketDataProviderFactory())).append(',').append(' ');
    buf.append("historicalTimeSeriesSource").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesSource())).append(',').append(' ');
    buf.append("historicalTimeSeriesResolver").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesResolver())).append(',').append(' ');
    buf.append("marketDataSnapshotSource").append('=').append(JodaBeanUtils.toString(getMarketDataSnapshotSource())).append(',').append(' ');
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
     * The meta-property for the {@code historicalTimeSeriesResolver} property.
     */
    private final MetaProperty<HistoricalTimeSeriesResolver> _historicalTimeSeriesResolver = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesResolver", MarketDataProviderResolverComponentFactory.class, HistoricalTimeSeriesResolver.class);
    /**
     * The meta-property for the {@code marketDataSnapshotSource} property.
     */
    private final MetaProperty<MarketDataSnapshotSource> _marketDataSnapshotSource = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotSource", MarketDataProviderResolverComponentFactory.class, MarketDataSnapshotSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "liveMarketDataProviderFactory",
        "historicalTimeSeriesSource",
        "historicalTimeSeriesResolver",
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
        case -946313676:  // historicalTimeSeriesResolver
          return _historicalTimeSeriesResolver;
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
     * The meta-property for the {@code historicalTimeSeriesResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesResolver> historicalTimeSeriesResolver() {
      return _historicalTimeSeriesResolver;
    }

    /**
     * The meta-property for the {@code marketDataSnapshotSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotSource> marketDataSnapshotSource() {
      return _marketDataSnapshotSource;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((MarketDataProviderResolverComponentFactory) bean).getClassifier();
        case -301472921:  // liveMarketDataProviderFactory
          return ((MarketDataProviderResolverComponentFactory) bean).getLiveMarketDataProviderFactory();
        case 358729161:  // historicalTimeSeriesSource
          return ((MarketDataProviderResolverComponentFactory) bean).getHistoricalTimeSeriesSource();
        case -946313676:  // historicalTimeSeriesResolver
          return ((MarketDataProviderResolverComponentFactory) bean).getHistoricalTimeSeriesResolver();
        case -2019554651:  // marketDataSnapshotSource
          return ((MarketDataProviderResolverComponentFactory) bean).getMarketDataSnapshotSource();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((MarketDataProviderResolverComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -301472921:  // liveMarketDataProviderFactory
          ((MarketDataProviderResolverComponentFactory) bean).setLiveMarketDataProviderFactory((MarketDataProviderFactory) newValue);
          return;
        case 358729161:  // historicalTimeSeriesSource
          ((MarketDataProviderResolverComponentFactory) bean).setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
          return;
        case -946313676:  // historicalTimeSeriesResolver
          ((MarketDataProviderResolverComponentFactory) bean).setHistoricalTimeSeriesResolver((HistoricalTimeSeriesResolver) newValue);
          return;
        case -2019554651:  // marketDataSnapshotSource
          ((MarketDataProviderResolverComponentFactory) bean).setMarketDataSnapshotSource((MarketDataSnapshotSource) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((MarketDataProviderResolverComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((MarketDataProviderResolverComponentFactory) bean)._historicalTimeSeriesSource, "historicalTimeSeriesSource");
      JodaBeanUtils.notNull(((MarketDataProviderResolverComponentFactory) bean)._historicalTimeSeriesResolver, "historicalTimeSeriesResolver");
      JodaBeanUtils.notNull(((MarketDataProviderResolverComponentFactory) bean)._marketDataSnapshotSource, "marketDataSnapshotSource");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
