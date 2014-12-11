/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.component;

import java.net.URI;
import java.util.Collection;
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

import com.google.common.collect.ImmutableMap;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.value.MarketDataRequirementNamesHelper;
import com.opengamma.engine.marketdata.InMemoryNamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.live.LiveDataFactory;
import com.opengamma.engine.marketdata.live.InMemoryLKVLiveMarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.client.RemoteLiveDataClientFactoryBean;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataServerTypes;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import org.joda.beans.Bean;

/**
 * Component factory for consuming simulated live data.
 */
@BeanDefinition
public class ExampleMarketDataComponentFactory extends AbstractComponentFactory {

  /**
   * Name to use.
   */
  public static final String SIMULATED_LIVE_SOURCE_NAME = "Simulated live market data";

  /**
   * The classifier under which to publish.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The meta-data about the server.
   */
  @PropertyDefinition(validate = "notNull")
  private LiveDataMetaDataProvider _serverMetaDataProvider;
  /**
   * The JMS connector.
   */
  @PropertyDefinition(validate = "notNull")
  private JmsConnector _jmsConnector;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    initLiveMarketDataProviderFactory(repo);
    initNamedMarketDataSpecificationRepository(repo);
  }

  private MarketDataProviderFactory initLiveMarketDataProviderFactory(final ComponentRepository repo) {
    final LiveDataMetaDataProvider provider = getServerMetaDataProvider();
    final LiveDataClient liveDataClient = createLiveDataClient(provider);

    final MarketDataAvailabilityFilter availability = createAvailabilityFilter(provider);
    final LiveDataFactory defaultFactory = new LiveDataFactory(liveDataClient, availability);
    final Map<String, LiveDataFactory> factoryMap = ImmutableMap.of(SIMULATED_LIVE_SOURCE_NAME, defaultFactory);
    final MarketDataProviderFactory marketDataProviderFactory = new InMemoryLKVLiveMarketDataProviderFactory(defaultFactory, factoryMap);

    final ComponentInfo info = new ComponentInfo(MarketDataProviderFactory.class, getClassifier());
    repo.registerComponent(info, marketDataProviderFactory);
    return marketDataProviderFactory;
  }

  private LiveDataClient createLiveDataClient(final LiveDataMetaDataProvider provider) {
    final LiveDataMetaData metaData = provider.metaData();
    final URI jmsUri = metaData.getJmsBrokerUri();
    if (metaData.getServerType() != LiveDataServerTypes.STANDARD || jmsUri == null) {
      throw new IllegalStateException();
    }
    JmsConnector jmsConnector = getJmsConnector();
    if (jmsConnector.getClientBrokerUri().equals(jmsUri) == false) {
      final JmsConnectorFactoryBean jmsFactory = new JmsConnectorFactoryBean(jmsConnector);
      jmsFactory.setClientBrokerUri(jmsUri);
      jmsConnector = jmsFactory.getObjectCreating();
    }

    final RemoteLiveDataClientFactoryBean ldcFb = new RemoteLiveDataClientFactoryBean();
    ldcFb.setJmsConnector(jmsConnector);
    ldcFb.setSubscriptionTopic(metaData.getJmsSubscriptionTopic());
    ldcFb.setEntitlementTopic(metaData.getJmsEntitlementTopic());
    ldcFb.setHeartbeatTopic(metaData.getJmsHeartbeatTopic());
    final LiveDataClient ldcDistributed = ldcFb.getObjectCreating();
    return ldcDistributed;
  }

  private MarketDataAvailabilityFilter createAvailabilityFilter(final LiveDataMetaDataProvider provider) {
    final List<ExternalScheme> acceptableSchemes = provider.metaData().getSupportedSchemes();
    final Collection<String> validMarketDataRequirementNames = MarketDataRequirementNamesHelper.constructValidRequirementNames();
    return new DomainMarketDataAvailabilityFilter(acceptableSchemes, validMarketDataRequirementNames);
  }

  private NamedMarketDataSpecificationRepository initNamedMarketDataSpecificationRepository(final ComponentRepository repo) {
    final InMemoryNamedMarketDataSpecificationRepository specRepository = new InMemoryNamedMarketDataSpecificationRepository();

    specRepository.addSpecification(SIMULATED_LIVE_SOURCE_NAME, LiveMarketDataSpecification.of(SIMULATED_LIVE_SOURCE_NAME));
    final ComponentInfo info = new ComponentInfo(NamedMarketDataSpecificationRepository.class, getClassifier());
    repo.registerComponent(info, specRepository);
    return specRepository;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExampleMarketDataComponentFactory}.
   * @return the meta-bean, not null
   */
  public static ExampleMarketDataComponentFactory.Meta meta() {
    return ExampleMarketDataComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ExampleMarketDataComponentFactory.Meta.INSTANCE);
  }

  @Override
  public ExampleMarketDataComponentFactory.Meta metaBean() {
    return ExampleMarketDataComponentFactory.Meta.INSTANCE;
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
   * Gets the meta-data about the server.
   * @return the value of the property, not null
   */
  public LiveDataMetaDataProvider getServerMetaDataProvider() {
    return _serverMetaDataProvider;
  }

  /**
   * Sets the meta-data about the server.
   * @param serverMetaDataProvider  the new value of the property, not null
   */
  public void setServerMetaDataProvider(LiveDataMetaDataProvider serverMetaDataProvider) {
    JodaBeanUtils.notNull(serverMetaDataProvider, "serverMetaDataProvider");
    this._serverMetaDataProvider = serverMetaDataProvider;
  }

  /**
   * Gets the the {@code serverMetaDataProvider} property.
   * @return the property, not null
   */
  public final Property<LiveDataMetaDataProvider> serverMetaDataProvider() {
    return metaBean().serverMetaDataProvider().createProperty(this);
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
  @Override
  public ExampleMarketDataComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExampleMarketDataComponentFactory other = (ExampleMarketDataComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getServerMetaDataProvider(), other.getServerMetaDataProvider()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getServerMetaDataProvider());
    hash = hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("ExampleMarketDataComponentFactory{");
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
    buf.append("serverMetaDataProvider").append('=').append(JodaBeanUtils.toString(getServerMetaDataProvider())).append(',').append(' ');
    buf.append("jmsConnector").append('=').append(JodaBeanUtils.toString(getJmsConnector())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExampleMarketDataComponentFactory}.
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
        this, "classifier", ExampleMarketDataComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code serverMetaDataProvider} property.
     */
    private final MetaProperty<LiveDataMetaDataProvider> _serverMetaDataProvider = DirectMetaProperty.ofReadWrite(
        this, "serverMetaDataProvider", ExampleMarketDataComponentFactory.class, LiveDataMetaDataProvider.class);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", ExampleMarketDataComponentFactory.class, JmsConnector.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "serverMetaDataProvider",
        "jmsConnector");

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
        case -187029565:  // serverMetaDataProvider
          return _serverMetaDataProvider;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExampleMarketDataComponentFactory> builder() {
      return new DirectBeanBuilder<ExampleMarketDataComponentFactory>(new ExampleMarketDataComponentFactory());
    }

    @Override
    public Class<? extends ExampleMarketDataComponentFactory> beanType() {
      return ExampleMarketDataComponentFactory.class;
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
     * The meta-property for the {@code serverMetaDataProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LiveDataMetaDataProvider> serverMetaDataProvider() {
      return _serverMetaDataProvider;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((ExampleMarketDataComponentFactory) bean).getClassifier();
        case -187029565:  // serverMetaDataProvider
          return ((ExampleMarketDataComponentFactory) bean).getServerMetaDataProvider();
        case -1495762275:  // jmsConnector
          return ((ExampleMarketDataComponentFactory) bean).getJmsConnector();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((ExampleMarketDataComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -187029565:  // serverMetaDataProvider
          ((ExampleMarketDataComponentFactory) bean).setServerMetaDataProvider((LiveDataMetaDataProvider) newValue);
          return;
        case -1495762275:  // jmsConnector
          ((ExampleMarketDataComponentFactory) bean).setJmsConnector((JmsConnector) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ExampleMarketDataComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((ExampleMarketDataComponentFactory) bean)._serverMetaDataProvider, "serverMetaDataProvider");
      JodaBeanUtils.notNull(((ExampleMarketDataComponentFactory) bean)._jmsConnector, "jmsConnector");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
