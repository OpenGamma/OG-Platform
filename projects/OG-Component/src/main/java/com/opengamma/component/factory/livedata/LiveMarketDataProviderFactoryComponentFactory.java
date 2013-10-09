/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.livedata;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.value.MarketDataRequirementNamesHelper;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.live.InMemoryLKVLiveMarketDataProviderFactory;
import com.opengamma.engine.marketdata.live.LiveDataFactory;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataServerTypes;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

/**
 * Component factory for a live market data provider factory.
 */
@BeanDefinition
public class LiveMarketDataProviderFactoryComponentFactory extends AbstractComponentFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveMarketDataProviderFactoryComponentFactory.class);
  
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
   * A comma-separated list of default providers, from highest priority to lowest, not null or empty.
   * <p>
   * The first available provider from this list will be used as the default.
   */
  @PropertyDefinition(validate = "notNull")
  private String _defaultProviders;
  
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    if (StringUtils.isBlank(getDefaultProviders())) {
      throw new OpenGammaRuntimeException(defaultProviders().name() + " cannot be empty");
    }
    List<String> defaultProviders = ImmutableList.copyOf(getDefaultProviders().split(","));
    initLiveMarketDataProviderFactory(repo.getInstances(LiveDataMetaDataProvider.class), defaultProviders, repo);
  }

  private LiveMarketDataProviderFactory initLiveMarketDataProviderFactory(Collection<LiveDataMetaDataProvider> metaDataProviders, List<String> defaultProviders, ComponentRepository repo) {
    Map<String, LiveDataFactory> factories = Maps.newLinkedHashMap();
    for (LiveDataMetaDataProvider metaDataProvider : metaDataProviders) {
      MarketDataAvailabilityFilter filter = createMarketDataAvailabilityFilter(metaDataProvider);
      if (filter == null) {
        continue;
      }
      LiveDataClient client = createLiveDataClient(metaDataProvider);
      if (client == null) {
        continue;
      }
      factories.put(metaDataProvider.metaData().getDescription(), new LiveDataFactory(client, filter));
    }
    
    LiveDataFactory defaultFactory = null;
    for (String defaultProvider : defaultProviders) {
      if (factories.containsKey(defaultProvider)) {
        defaultFactory = factories.get(defaultProvider);
        break;
      }
    }
    if (defaultFactory == null) {
      throw new OpenGammaRuntimeException("Unable to find a default provider matching one of [" + StringUtils.join(defaultProviders, ", ")
          + "] from available providers [" + StringUtils.join(factories.keySet(), ", ") + "]");
    }
    
    InMemoryLKVLiveMarketDataProviderFactory liveMarketDataProviderFactory = new InMemoryLKVLiveMarketDataProviderFactory(defaultFactory, ImmutableMap.copyOf(factories));
    ComponentInfo info = new ComponentInfo(LiveMarketDataProviderFactory.class, getClassifier());
    repo.registerComponent(info, liveMarketDataProviderFactory);
    
    // REVIEW jonathan 2013-08-23 -- Didn't want to break backwards compatibility, but shouldn't the repository take care of supertypes?
    info = new ComponentInfo(MarketDataProviderFactory.class, getClassifier());
    repo.registerComponent(info, liveMarketDataProviderFactory);

    return liveMarketDataProviderFactory;
  }
  
  /**
   * Creates a live data client based on the information in the remote metadata.
   * 
   * @param provider the metadata provider, null returns null
   * @return the client
   */
  @SuppressWarnings("deprecation")
  protected LiveDataClient createLiveDataClient(final LiveDataMetaDataProvider provider) {
    LiveDataMetaData metaData = provider.metaData();
    URI jmsUri = metaData.getJmsBrokerUri();
    if (metaData.getServerType() != LiveDataServerTypes.STANDARD || jmsUri == null) {
      s_logger.warn("Unsupported live data server type " + metaData.getServerType() + " for " + metaData.getDescription() + " live data provider. This provider will not be available.");
      return null;
    }
    JmsConnector jmsConnector = getJmsConnector();
    if (!jmsConnector.getClientBrokerUri().equals(jmsUri)) {
      JmsConnectorFactoryBean jmsFactory = new JmsConnectorFactoryBean(jmsConnector);
      jmsFactory.setClientBrokerUri(jmsUri);
      jmsConnector = jmsFactory.getObjectCreating();
    }
    
    JmsTemplate jmsTemplate = getJmsConnector().getJmsTemplateTopic();
    
    JmsByteArrayRequestSender jmsSubscriptionRequestSender;

    if (metaData.getJmsSubscriptionQueue() != null) {
      JmsTemplate subscriptionRequestTemplate = getJmsConnector().getJmsTemplateQueue();
      jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(metaData.getJmsSubscriptionQueue(), subscriptionRequestTemplate);
    } else {
      jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(metaData.getJmsSubscriptionTopic(), jmsTemplate);
    }
    ByteArrayFudgeRequestSender fudgeSubscriptionRequestSender = new ByteArrayFudgeRequestSender(jmsSubscriptionRequestSender);
    
    JmsByteArrayRequestSender jmsEntitlementRequestSender = new JmsByteArrayRequestSender(metaData.getJmsEntitlementTopic(), jmsTemplate);
    ByteArrayFudgeRequestSender fudgeEntitlementRequestSender = new ByteArrayFudgeRequestSender(jmsEntitlementRequestSender);
    
    final JmsLiveDataClient liveDataClient = new JmsLiveDataClient(fudgeSubscriptionRequestSender,
        fudgeEntitlementRequestSender, getJmsConnector(), OpenGammaFudgeContext.getInstance(), JmsLiveDataClient.DEFAULT_NUM_SESSIONS);
    liveDataClient.setFudgeContext(OpenGammaFudgeContext.getInstance());
    if (metaData.getJmsHeartbeatTopic() != null) {
      JmsByteArrayMessageSender jmsHeartbeatSender = new JmsByteArrayMessageSender(metaData.getJmsHeartbeatTopic(), jmsTemplate);
      liveDataClient.setHeartbeatMessageSender(jmsHeartbeatSender);
    }
    liveDataClient.start();
    liveDataClient.registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), "LiveDataClient - " + provider.metaData().getDescription());
    return liveDataClient;
  }
  
  /**
   * Creates a market data availability filter for a metadata provider.
   * 
   * @param metaDataProvider  the metadata provider, not null
   * @return the availability filter, null if none can be created
   */
  protected MarketDataAvailabilityFilter createMarketDataAvailabilityFilter(LiveDataMetaDataProvider metaDataProvider) {
    List<ExternalScheme> supportedSchemes = metaDataProvider.metaData().getSupportedSchemes();
    if (supportedSchemes == null || supportedSchemes.isEmpty()) {
      s_logger.warn("No supported external identifier schemes declared for " + metaDataProvider.metaData().getDescription() + " live data provider. This provider will not be available.");
      return null;
    }
    final Set<ExternalScheme> acceptableSchemes = ImmutableSet.copyOf(supportedSchemes);
    final Collection<String> validMarketDataRequirementNames = getMarketDataRequirementNames();
    return new DomainMarketDataAvailabilityFilter(acceptableSchemes, validMarketDataRequirementNames);
  }

  protected Set<String> getMarketDataRequirementNames() {
    return MarketDataRequirementNamesHelper.constructValidRequirementNames();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code LiveMarketDataProviderFactoryComponentFactory}.
   * @return the meta-bean, not null
   */
  public static LiveMarketDataProviderFactoryComponentFactory.Meta meta() {
    return LiveMarketDataProviderFactoryComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LiveMarketDataProviderFactoryComponentFactory.Meta.INSTANCE);
  }

  @Override
  public LiveMarketDataProviderFactoryComponentFactory.Meta metaBean() {
    return LiveMarketDataProviderFactoryComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -1495762275:  // jmsConnector
        return getJmsConnector();
      case 1263631201:  // defaultProviders
        return getDefaultProviders();
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
      case 1263631201:  // defaultProviders
        setDefaultProviders((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_jmsConnector, "jmsConnector");
    JodaBeanUtils.notNull(_defaultProviders, "defaultProviders");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      LiveMarketDataProviderFactoryComponentFactory other = (LiveMarketDataProviderFactoryComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getDefaultProviders(), other.getDefaultProviders()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDefaultProviders());
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
   * Gets a comma-separated list of default providers, from highest priority to lowest, not null or empty.
   * <p>
   * The first available provider from this list will be used as the default.
   * @return the value of the property, not null
   */
  public String getDefaultProviders() {
    return _defaultProviders;
  }

  /**
   * Sets a comma-separated list of default providers, from highest priority to lowest, not null or empty.
   * <p>
   * The first available provider from this list will be used as the default.
   * @param defaultProviders  the new value of the property, not null
   */
  public void setDefaultProviders(String defaultProviders) {
    JodaBeanUtils.notNull(defaultProviders, "defaultProviders");
    this._defaultProviders = defaultProviders;
  }

  /**
   * Gets the the {@code defaultProviders} property.
   * <p>
   * The first available provider from this list will be used as the default.
   * @return the property, not null
   */
  public final Property<String> defaultProviders() {
    return metaBean().defaultProviders().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LiveMarketDataProviderFactoryComponentFactory}.
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
        this, "classifier", LiveMarketDataProviderFactoryComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", LiveMarketDataProviderFactoryComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code defaultProviders} property.
     */
    private final MetaProperty<String> _defaultProviders = DirectMetaProperty.ofReadWrite(
        this, "defaultProviders", LiveMarketDataProviderFactoryComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "jmsConnector",
        "defaultProviders");

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
        case 1263631201:  // defaultProviders
          return _defaultProviders;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends LiveMarketDataProviderFactoryComponentFactory> builder() {
      return new DirectBeanBuilder<LiveMarketDataProviderFactoryComponentFactory>(new LiveMarketDataProviderFactoryComponentFactory());
    }

    @Override
    public Class<? extends LiveMarketDataProviderFactoryComponentFactory> beanType() {
      return LiveMarketDataProviderFactoryComponentFactory.class;
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
     * The meta-property for the {@code defaultProviders} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> defaultProviders() {
      return _defaultProviders;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
