/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.component;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.engine.marketdata.InMemoryNamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.live.InMemoryLKVLiveMarketDataProviderFactory;
import com.opengamma.engine.marketdata.live.LiveDataAvailabilityNotificationListener;
import com.opengamma.engine.marketdata.live.LiveDataFactory;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.client.RemoteLiveDataClientFactoryBean;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataServerTypes;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Simple component factory for live Bloomberg market data.
 */
@BeanDefinition
public class BloombergLiveDataClientComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier under which to publish.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The JMS connector.
   */
  @PropertyDefinition(validate = "notNull")
  private JmsConnector _jmsConnector;
  /**
   * The Bloomberg live data meta-data.
   */
  @PropertyDefinition
  private LiveDataMetaDataProvider _bloombergMetaDataProvider;
  /**
   * JMS topic for notifications when market data providers become available
   */
  @PropertyDefinition
  private String _jmsMarketDataAvailabilityTopic;

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    final LiveDataMetaData metaData = getBloombergMetaDataProvider().metaData();
    if (metaData.getServerType() != LiveDataServerTypes.STANDARD) {
      throw new OpenGammaRuntimeException("Unexpected server type in metadata " + metaData);
    }
    String description = metaData.getDescription() != null ? metaData.getDescription() : "Bloomberg";
    description = "Live market data (" + description + ")";
    final URI jmsUri = metaData.getJmsBrokerUri();
    if (jmsUri == null) {
      throw new OpenGammaRuntimeException("JMS URI not set in metadata " + metaData);
    }

    JmsConnector jmsConnector = getJmsConnector();
    if (jmsConnector.getClientBrokerUri().equals(jmsUri) == false) {
      final JmsConnectorFactoryBean jmsFactory = new JmsConnectorFactoryBean(jmsConnector);
      jmsFactory.setClientBrokerUri(jmsUri);
      jmsConnector = jmsFactory.getObjectCreating();
    }

    final RemoteLiveDataClientFactoryBean liveDataClientFactory = new RemoteLiveDataClientFactoryBean();
    liveDataClientFactory.setJmsConnector(jmsConnector);
    liveDataClientFactory.setSubscriptionTopic(metaData.getJmsSubscriptionTopic());
    liveDataClientFactory.setEntitlementTopic(metaData.getJmsEntitlementTopic());
    liveDataClientFactory.setHeartbeatTopic(metaData.getJmsHeartbeatTopic());
    final LiveDataClient liveDataClient = liveDataClientFactory.getObjectCreating();

    final MarketDataAvailabilityFilter availability = BloombergDataUtils.createAvailabilityFilter();
    final LiveDataFactory liveDataFactory = new LiveDataFactory(liveDataClient, availability);
    final MarketDataProviderFactory liveMarketDataProviderFactory = new InMemoryLKVLiveMarketDataProviderFactory(liveDataFactory, ImmutableMap.of(description, liveDataFactory));
    final ComponentInfo providerFactoryInfo = new ComponentInfo(MarketDataProviderFactory.class, getClassifier());
    repo.registerComponent(providerFactoryInfo, liveMarketDataProviderFactory);

    // notifies LiveDataFactories when market data providers come up so they can retry failed subscriptions
    List<LiveDataFactory> factoryList = ImmutableList.of(liveDataFactory);
    LiveDataAvailabilityNotificationListener availabilityNotificationListener =
        new LiveDataAvailabilityNotificationListener(getJmsMarketDataAvailabilityTopic(), factoryList, getJmsConnector());
    repo.registerLifecycle(availabilityNotificationListener);

    final InMemoryNamedMarketDataSpecificationRepository specRepository = new InMemoryNamedMarketDataSpecificationRepository();
    specRepository.addSpecification(description, new LiveMarketDataSpecification(description));

    final ComponentInfo specRepositoryInfo = new ComponentInfo(NamedMarketDataSpecificationRepository.class, getClassifier());
    repo.registerComponent(specRepositoryInfo, specRepository);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BloombergLiveDataClientComponentFactory}.
   * @return the meta-bean, not null
   */
  public static BloombergLiveDataClientComponentFactory.Meta meta() {
    return BloombergLiveDataClientComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BloombergLiveDataClientComponentFactory.Meta.INSTANCE);
  }

  @Override
  public BloombergLiveDataClientComponentFactory.Meta metaBean() {
    return BloombergLiveDataClientComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -1495762275:  // jmsConnector
        return getJmsConnector();
      case -1210045765:  // bloombergMetaDataProvider
        return getBloombergMetaDataProvider();
      case 108776830:  // jmsMarketDataAvailabilityTopic
        return getJmsMarketDataAvailabilityTopic();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -1495762275:  // jmsConnector
        setJmsConnector((JmsConnector) newValue);
        return;
      case -1210045765:  // bloombergMetaDataProvider
        setBloombergMetaDataProvider((LiveDataMetaDataProvider) newValue);
        return;
      case 108776830:  // jmsMarketDataAvailabilityTopic
        setJmsMarketDataAvailabilityTopic((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_jmsConnector, "jmsConnector");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BloombergLiveDataClientComponentFactory other = (BloombergLiveDataClientComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getBloombergMetaDataProvider(), other.getBloombergMetaDataProvider()) &&
          JodaBeanUtils.equal(getJmsMarketDataAvailabilityTopic(), other.getJmsMarketDataAvailabilityTopic()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBloombergMetaDataProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsMarketDataAvailabilityTopic());
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
   * Gets the JMS connector.
   * @return the value of the property, not null
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector.
   * @param jmsConnector  the new value of the property, not null
   */
  public void setJmsConnector(JmsConnector jmsConnector) {
    JodaBeanUtils.notNull(jmsConnector, "jmsConnector");
    this._jmsConnector = jmsConnector;
  }

  /**
   * Gets the the {@code jmsConnector} property.
   * @return the property, not null
   */
  public final Property<JmsConnector> jmsConnector() {
    return metaBean().jmsConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Bloomberg live data meta-data.
   * @return the value of the property
   */
  public LiveDataMetaDataProvider getBloombergMetaDataProvider() {
    return _bloombergMetaDataProvider;
  }

  /**
   * Sets the Bloomberg live data meta-data.
   * @param bloombergMetaDataProvider  the new value of the property
   */
  public void setBloombergMetaDataProvider(LiveDataMetaDataProvider bloombergMetaDataProvider) {
    this._bloombergMetaDataProvider = bloombergMetaDataProvider;
  }

  /**
   * Gets the the {@code bloombergMetaDataProvider} property.
   * @return the property, not null
   */
  public final Property<LiveDataMetaDataProvider> bloombergMetaDataProvider() {
    return metaBean().bloombergMetaDataProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets jMS topic for notifications when market data providers become available
   * @return the value of the property
   */
  public String getJmsMarketDataAvailabilityTopic() {
    return _jmsMarketDataAvailabilityTopic;
  }

  /**
   * Sets jMS topic for notifications when market data providers become available
   * @param jmsMarketDataAvailabilityTopic  the new value of the property
   */
  public void setJmsMarketDataAvailabilityTopic(String jmsMarketDataAvailabilityTopic) {
    this._jmsMarketDataAvailabilityTopic = jmsMarketDataAvailabilityTopic;
  }

  /**
   * Gets the the {@code jmsMarketDataAvailabilityTopic} property.
   * @return the property, not null
   */
  public final Property<String> jmsMarketDataAvailabilityTopic() {
    return metaBean().jmsMarketDataAvailabilityTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BloombergLiveDataClientComponentFactory}.
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
        this, "classifier", BloombergLiveDataClientComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", BloombergLiveDataClientComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code bloombergMetaDataProvider} property.
     */
    private final MetaProperty<LiveDataMetaDataProvider> _bloombergMetaDataProvider = DirectMetaProperty.ofReadWrite(
        this, "bloombergMetaDataProvider", BloombergLiveDataClientComponentFactory.class, LiveDataMetaDataProvider.class);
    /**
     * The meta-property for the {@code jmsMarketDataAvailabilityTopic} property.
     */
    private final MetaProperty<String> _jmsMarketDataAvailabilityTopic = DirectMetaProperty.ofReadWrite(
        this, "jmsMarketDataAvailabilityTopic", BloombergLiveDataClientComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "jmsConnector",
        "bloombergMetaDataProvider",
        "jmsMarketDataAvailabilityTopic");

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
        case -1495762275:  // jmsConnector
          return _jmsConnector;
        case -1210045765:  // bloombergMetaDataProvider
          return _bloombergMetaDataProvider;
        case 108776830:  // jmsMarketDataAvailabilityTopic
          return _jmsMarketDataAvailabilityTopic;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BloombergLiveDataClientComponentFactory> builder() {
      return new DirectBeanBuilder<BloombergLiveDataClientComponentFactory>(new BloombergLiveDataClientComponentFactory());
    }

    @Override
    public Class<? extends BloombergLiveDataClientComponentFactory> beanType() {
      return BloombergLiveDataClientComponentFactory.class;
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
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    /**
     * The meta-property for the {@code bloombergMetaDataProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LiveDataMetaDataProvider> bloombergMetaDataProvider() {
      return _bloombergMetaDataProvider;
    }

    /**
     * The meta-property for the {@code jmsMarketDataAvailabilityTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jmsMarketDataAvailabilityTopic() {
      return _jmsMarketDataAvailabilityTopic;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
