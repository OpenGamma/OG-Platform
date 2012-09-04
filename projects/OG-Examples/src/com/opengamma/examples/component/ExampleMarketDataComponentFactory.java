/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.ImmutableSet;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNamesHelper;
import com.opengamma.engine.marketdata.InMemoryNamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.live.LiveDataFactory;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.client.RemoteLiveDataClientFactoryBean;
import com.opengamma.util.jms.JmsConnector;

/**
 * Component factory for market data
 */
@BeanDefinition
public class ExampleMarketDataComponentFactory extends AbstractComponentFactory {
  
  private static final String SIMULATED_LIVE_SOURCE_NAME = "Simulated live market data";
  
  /**
   * The classifier under which to publish.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The security source.
   */
  @PropertyDefinition(validate = "notNull")
  private SecuritySource _securitySource;
  /**
   * The JMS connector.
   */
  @PropertyDefinition(validate = "notNull")
  private JmsConnector _jmsConnector;
  /**
   * The subscription topic.
   */
  @PropertyDefinition(validate = "notNull")
  private String _subscriptionTopic;
  /**
   * The entitlement topic.
   */
  @PropertyDefinition(validate = "notNull")
  private String _entitlementTopic;
  /**
   * The heartbeat topic.
   */
  @PropertyDefinition(validate = "notNull")
  private String _heartbeatTopic;

  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    initLiveMarketDataProviderFactory(repo);
    initNamedMarketDataSpecificationRepository(repo);
  }

  private NamedMarketDataSpecificationRepository initNamedMarketDataSpecificationRepository(ComponentRepository repo) {
    InMemoryNamedMarketDataSpecificationRepository specRepository = new InMemoryNamedMarketDataSpecificationRepository();

    specRepository.addSpecification(SIMULATED_LIVE_SOURCE_NAME, new LiveMarketDataSpecification(SIMULATED_LIVE_SOURCE_NAME)); 
    ComponentInfo info = new ComponentInfo(NamedMarketDataSpecificationRepository.class, getClassifier());
    repo.registerComponent(info, specRepository);
    return specRepository;
  }

  private MarketDataProviderFactory initLiveMarketDataProviderFactory(ComponentRepository repo) {
    LiveDataClient liveDataClient = createLiveDataClient(getSubscriptionTopic(),
                                                         getEntitlementTopic(),
                                                         getHeartbeatTopic());
    MarketDataAvailabilityProvider availabilityProvider = createAvailabilityProvider();
    LiveDataFactory defaultFactory = new LiveDataFactory(liveDataClient, availabilityProvider, getSecuritySource());
    Map<String, LiveDataFactory> factoryMap = ImmutableMap.of(SIMULATED_LIVE_SOURCE_NAME, defaultFactory);
    LiveMarketDataProviderFactory marketDataProviderFactory = new LiveMarketDataProviderFactory(defaultFactory, factoryMap);
    ComponentInfo info = new ComponentInfo(MarketDataProviderFactory.class, getClassifier());
    repo.registerComponent(info, marketDataProviderFactory);
    return marketDataProviderFactory;
  }
  
  protected LiveDataClient createLiveDataClient(String subscriptionTopic, String entitlementTopic, String heartbeatTopic) {
    RemoteLiveDataClientFactoryBean ldcFb = new RemoteLiveDataClientFactoryBean();
    ldcFb.setJmsConnector(getJmsConnector());
    ldcFb.setSubscriptionTopic(subscriptionTopic);
    ldcFb.setEntitlementTopic(entitlementTopic);
    ldcFb.setHeartbeatTopic(heartbeatTopic);
    return ldcFb.getObjectCreating();
  }
  
  private MarketDataAvailabilityProvider createAvailabilityProvider() {
    Set<ExternalScheme> acceptableSchemes = ImmutableSet.of(ExternalSchemes.OG_SYNTHETIC_TICKER);
    Collection<String> validMarketDataRequirementNames = MarketDataRequirementNamesHelper.constructValidRequirementNames();
    return new DomainMarketDataAvailabilityProvider(getSecuritySource(), acceptableSchemes, validMarketDataRequirementNames);
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

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -702456965:  // securitySource
        return getSecuritySource();
      case -1495762275:  // jmsConnector
        return getJmsConnector();
      case 1191816722:  // subscriptionTopic
        return getSubscriptionTopic();
      case 397583362:  // entitlementTopic
        return getEntitlementTopic();
      case 1497737619:  // heartbeatTopic
        return getHeartbeatTopic();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -702456965:  // securitySource
        setSecuritySource((SecuritySource) newValue);
        return;
      case -1495762275:  // jmsConnector
        setJmsConnector((JmsConnector) newValue);
        return;
      case 1191816722:  // subscriptionTopic
        setSubscriptionTopic((String) newValue);
        return;
      case 397583362:  // entitlementTopic
        setEntitlementTopic((String) newValue);
        return;
      case 1497737619:  // heartbeatTopic
        setHeartbeatTopic((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_securitySource, "securitySource");
    JodaBeanUtils.notNull(_jmsConnector, "jmsConnector");
    JodaBeanUtils.notNull(_subscriptionTopic, "subscriptionTopic");
    JodaBeanUtils.notNull(_entitlementTopic, "entitlementTopic");
    JodaBeanUtils.notNull(_heartbeatTopic, "heartbeatTopic");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExampleMarketDataComponentFactory other = (ExampleMarketDataComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getSubscriptionTopic(), other.getSubscriptionTopic()) &&
          JodaBeanUtils.equal(getEntitlementTopic(), other.getEntitlementTopic()) &&
          JodaBeanUtils.equal(getHeartbeatTopic(), other.getHeartbeatTopic()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSubscriptionTopic());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEntitlementTopic());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHeartbeatTopic());
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
   * Gets the security source.
   * @return the value of the property, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Sets the security source.
   * @param securitySource  the new value of the property, not null
   */
  public void setSecuritySource(SecuritySource securitySource) {
    JodaBeanUtils.notNull(securitySource, "securitySource");
    this._securitySource = securitySource;
  }

  /**
   * Gets the the {@code securitySource} property.
   * @return the property, not null
   */
  public final Property<SecuritySource> securitySource() {
    return metaBean().securitySource().createProperty(this);
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
   * Gets the subscription topic.
   * @return the value of the property, not null
   */
  public String getSubscriptionTopic() {
    return _subscriptionTopic;
  }

  /**
   * Sets the subscription topic.
   * @param subscriptionTopic  the new value of the property, not null
   */
  public void setSubscriptionTopic(String subscriptionTopic) {
    JodaBeanUtils.notNull(subscriptionTopic, "subscriptionTopic");
    this._subscriptionTopic = subscriptionTopic;
  }

  /**
   * Gets the the {@code subscriptionTopic} property.
   * @return the property, not null
   */
  public final Property<String> subscriptionTopic() {
    return metaBean().subscriptionTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the entitlement topic.
   * @return the value of the property, not null
   */
  public String getEntitlementTopic() {
    return _entitlementTopic;
  }

  /**
   * Sets the entitlement topic.
   * @param entitlementTopic  the new value of the property, not null
   */
  public void setEntitlementTopic(String entitlementTopic) {
    JodaBeanUtils.notNull(entitlementTopic, "entitlementTopic");
    this._entitlementTopic = entitlementTopic;
  }

  /**
   * Gets the the {@code entitlementTopic} property.
   * @return the property, not null
   */
  public final Property<String> entitlementTopic() {
    return metaBean().entitlementTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the heartbeat topic.
   * @return the value of the property, not null
   */
  public String getHeartbeatTopic() {
    return _heartbeatTopic;
  }

  /**
   * Sets the heartbeat topic.
   * @param heartbeatTopic  the new value of the property, not null
   */
  public void setHeartbeatTopic(String heartbeatTopic) {
    JodaBeanUtils.notNull(heartbeatTopic, "heartbeatTopic");
    this._heartbeatTopic = heartbeatTopic;
  }

  /**
   * Gets the the {@code heartbeatTopic} property.
   * @return the property, not null
   */
  public final Property<String> heartbeatTopic() {
    return metaBean().heartbeatTopic().createProperty(this);
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
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", ExampleMarketDataComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", ExampleMarketDataComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code subscriptionTopic} property.
     */
    private final MetaProperty<String> _subscriptionTopic = DirectMetaProperty.ofReadWrite(
        this, "subscriptionTopic", ExampleMarketDataComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code entitlementTopic} property.
     */
    private final MetaProperty<String> _entitlementTopic = DirectMetaProperty.ofReadWrite(
        this, "entitlementTopic", ExampleMarketDataComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code heartbeatTopic} property.
     */
    private final MetaProperty<String> _heartbeatTopic = DirectMetaProperty.ofReadWrite(
        this, "heartbeatTopic", ExampleMarketDataComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "securitySource",
        "jmsConnector",
        "subscriptionTopic",
        "entitlementTopic",
        "heartbeatTopic");

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
        case -702456965:  // securitySource
          return _securitySource;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
        case 1191816722:  // subscriptionTopic
          return _subscriptionTopic;
        case 397583362:  // entitlementTopic
          return _entitlementTopic;
        case 1497737619:  // heartbeatTopic
          return _heartbeatTopic;
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
     * The meta-property for the {@code securitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecuritySource> securitySource() {
      return _securitySource;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    /**
     * The meta-property for the {@code subscriptionTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> subscriptionTopic() {
      return _subscriptionTopic;
    }

    /**
     * The meta-property for the {@code entitlementTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> entitlementTopic() {
      return _entitlementTopic;
    }

    /**
     * The meta-property for the {@code heartbeatTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> heartbeatTopic() {
      return _heartbeatTopic;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
