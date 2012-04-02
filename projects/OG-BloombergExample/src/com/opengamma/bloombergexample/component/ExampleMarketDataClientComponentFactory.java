/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.component;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

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
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.InMemoryNamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.live.LiveMarketDataProvider;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.client.RemoteLiveDataClientFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * Component factory for market data
 */
@BeanDefinition
public class ExampleMarketDataClientComponentFactory extends AbstractComponentFactory {
  
  private static final String BLOOMBERG_LIVE_SOURCE_NAME = "Live market data (Bloomberg)";
  
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
   * The Bloomberg subscription topic.
   */
  @PropertyDefinition(validate = "notNull")
  private String _bbgSubscriptionTopic;
  /**
   * The Bloomberg entitlement topic.
   */
  @PropertyDefinition(validate = "notNull")
  private String _bbgEntitlementTopic;
  /**
   * The Bloomberg heartbeat topic.
   */
  @PropertyDefinition(validate = "notNull")
  private String _bbgHeartbeatTopic;
  
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    initLiveMarketDataProviderFactory(repo);
    initNamedMarketDataSpecificationRepository(repo);
  }

  private MarketDataProviderFactory initLiveMarketDataProviderFactory(ComponentRepository repo) {
    LiveDataClient bbgLiveDataClient = createLiveDataClient(getBbgSubscriptionTopic(), getBbgEntitlementTopic(), getBbgHeartbeatTopic());
    MarketDataAvailabilityProvider bbgAvailabilityProvider = BloombergDataUtils.createAvailabilityProvider(getSecuritySource());
    MarketDataProvider bbgProvider = new LiveMarketDataProvider(bbgLiveDataClient, getSecuritySource(), bbgAvailabilityProvider, OpenGammaFudgeContext.getInstance(), 
        new PermissiveMarketDataPermissionProvider());
    
    Map<String, MarketDataProvider> sourceToProviderMap = ImmutableMap.of(BLOOMBERG_LIVE_SOURCE_NAME, bbgProvider);
    LiveMarketDataProviderFactory liveMarketDataProviderFactory = new LiveMarketDataProviderFactory(bbgProvider, sourceToProviderMap);
    ComponentInfo info = new ComponentInfo(MarketDataProviderFactory.class, getClassifier());
    repo.registerComponent(info, liveMarketDataProviderFactory);
    return liveMarketDataProviderFactory;
  }
  
  protected NamedMarketDataSpecificationRepository initNamedMarketDataSpecificationRepository(ComponentRepository repo) {
    InMemoryNamedMarketDataSpecificationRepository specRepository = new InMemoryNamedMarketDataSpecificationRepository();   
    
    LiveMarketDataSpecification bbgSpec = new LiveMarketDataSpecification(BLOOMBERG_LIVE_SOURCE_NAME);
    specRepository.addSpecification(BLOOMBERG_LIVE_SOURCE_NAME, bbgSpec);
    
    ComponentInfo info = new ComponentInfo(NamedMarketDataSpecificationRepository.class, getClassifier());
    repo.registerComponent(info, specRepository);
    return specRepository;
  }
  
  protected LiveDataClient createLiveDataClient(String subscriptionTopic, String entitlementTopic, String heartbeatTopic) {
    RemoteLiveDataClientFactoryBean ldcFb = new RemoteLiveDataClientFactoryBean();
    ldcFb.setJmsConnector(getJmsConnector());
    ldcFb.setSubscriptionTopic(subscriptionTopic);
    ldcFb.setEntitlementTopic(entitlementTopic);
    ldcFb.setHeartbeatTopic(heartbeatTopic);
    return ldcFb.getObjectCreating();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExampleMarketDataClientComponentFactory}.
   * @return the meta-bean, not null
   */
  public static ExampleMarketDataClientComponentFactory.Meta meta() {
    return ExampleMarketDataClientComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(ExampleMarketDataClientComponentFactory.Meta.INSTANCE);
  }

  @Override
  public ExampleMarketDataClientComponentFactory.Meta metaBean() {
    return ExampleMarketDataClientComponentFactory.Meta.INSTANCE;
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
      case -1347590997:  // bbgSubscriptionTopic
        return getBbgSubscriptionTopic();
      case 1424045641:  // bbgEntitlementTopic
        return getBbgEntitlementTopic();
      case 2142380442:  // bbgHeartbeatTopic
        return getBbgHeartbeatTopic();
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
      case -1347590997:  // bbgSubscriptionTopic
        setBbgSubscriptionTopic((String) newValue);
        return;
      case 1424045641:  // bbgEntitlementTopic
        setBbgEntitlementTopic((String) newValue);
        return;
      case 2142380442:  // bbgHeartbeatTopic
        setBbgHeartbeatTopic((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_securitySource, "securitySource");
    JodaBeanUtils.notNull(_jmsConnector, "jmsConnector");
    JodaBeanUtils.notNull(_bbgSubscriptionTopic, "bbgSubscriptionTopic");
    JodaBeanUtils.notNull(_bbgEntitlementTopic, "bbgEntitlementTopic");
    JodaBeanUtils.notNull(_bbgHeartbeatTopic, "bbgHeartbeatTopic");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExampleMarketDataClientComponentFactory other = (ExampleMarketDataClientComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getBbgSubscriptionTopic(), other.getBbgSubscriptionTopic()) &&
          JodaBeanUtils.equal(getBbgEntitlementTopic(), other.getBbgEntitlementTopic()) &&
          JodaBeanUtils.equal(getBbgHeartbeatTopic(), other.getBbgHeartbeatTopic()) &&
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
    hash += hash * 31 + JodaBeanUtils.hashCode(getBbgSubscriptionTopic());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBbgEntitlementTopic());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBbgHeartbeatTopic());
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
   * Gets the Bloomberg subscription topic.
   * @return the value of the property, not null
   */
  public String getBbgSubscriptionTopic() {
    return _bbgSubscriptionTopic;
  }

  /**
   * Sets the Bloomberg subscription topic.
   * @param bbgSubscriptionTopic  the new value of the property, not null
   */
  public void setBbgSubscriptionTopic(String bbgSubscriptionTopic) {
    JodaBeanUtils.notNull(bbgSubscriptionTopic, "bbgSubscriptionTopic");
    this._bbgSubscriptionTopic = bbgSubscriptionTopic;
  }

  /**
   * Gets the the {@code bbgSubscriptionTopic} property.
   * @return the property, not null
   */
  public final Property<String> bbgSubscriptionTopic() {
    return metaBean().bbgSubscriptionTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Bloomberg entitlement topic.
   * @return the value of the property, not null
   */
  public String getBbgEntitlementTopic() {
    return _bbgEntitlementTopic;
  }

  /**
   * Sets the Bloomberg entitlement topic.
   * @param bbgEntitlementTopic  the new value of the property, not null
   */
  public void setBbgEntitlementTopic(String bbgEntitlementTopic) {
    JodaBeanUtils.notNull(bbgEntitlementTopic, "bbgEntitlementTopic");
    this._bbgEntitlementTopic = bbgEntitlementTopic;
  }

  /**
   * Gets the the {@code bbgEntitlementTopic} property.
   * @return the property, not null
   */
  public final Property<String> bbgEntitlementTopic() {
    return metaBean().bbgEntitlementTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Bloomberg heartbeat topic.
   * @return the value of the property, not null
   */
  public String getBbgHeartbeatTopic() {
    return _bbgHeartbeatTopic;
  }

  /**
   * Sets the Bloomberg heartbeat topic.
   * @param bbgHeartbeatTopic  the new value of the property, not null
   */
  public void setBbgHeartbeatTopic(String bbgHeartbeatTopic) {
    JodaBeanUtils.notNull(bbgHeartbeatTopic, "bbgHeartbeatTopic");
    this._bbgHeartbeatTopic = bbgHeartbeatTopic;
  }

  /**
   * Gets the the {@code bbgHeartbeatTopic} property.
   * @return the property, not null
   */
  public final Property<String> bbgHeartbeatTopic() {
    return metaBean().bbgHeartbeatTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExampleMarketDataClientComponentFactory}.
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
        this, "classifier", ExampleMarketDataClientComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", ExampleMarketDataClientComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", ExampleMarketDataClientComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code bbgSubscriptionTopic} property.
     */
    private final MetaProperty<String> _bbgSubscriptionTopic = DirectMetaProperty.ofReadWrite(
        this, "bbgSubscriptionTopic", ExampleMarketDataClientComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code bbgEntitlementTopic} property.
     */
    private final MetaProperty<String> _bbgEntitlementTopic = DirectMetaProperty.ofReadWrite(
        this, "bbgEntitlementTopic", ExampleMarketDataClientComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code bbgHeartbeatTopic} property.
     */
    private final MetaProperty<String> _bbgHeartbeatTopic = DirectMetaProperty.ofReadWrite(
        this, "bbgHeartbeatTopic", ExampleMarketDataClientComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "securitySource",
        "jmsConnector",
        "bbgSubscriptionTopic",
        "bbgEntitlementTopic",
        "bbgHeartbeatTopic");

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
        case -1347590997:  // bbgSubscriptionTopic
          return _bbgSubscriptionTopic;
        case 1424045641:  // bbgEntitlementTopic
          return _bbgEntitlementTopic;
        case 2142380442:  // bbgHeartbeatTopic
          return _bbgHeartbeatTopic;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExampleMarketDataClientComponentFactory> builder() {
      return new DirectBeanBuilder<ExampleMarketDataClientComponentFactory>(new ExampleMarketDataClientComponentFactory());
    }

    @Override
    public Class<? extends ExampleMarketDataClientComponentFactory> beanType() {
      return ExampleMarketDataClientComponentFactory.class;
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
     * The meta-property for the {@code bbgSubscriptionTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> bbgSubscriptionTopic() {
      return _bbgSubscriptionTopic;
    }

    /**
     * The meta-property for the {@code bbgEntitlementTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> bbgEntitlementTopic() {
      return _bbgEntitlementTopic;
    }

    /**
     * The meta-property for the {@code bbgHeartbeatTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> bbgHeartbeatTopic() {
      return _bbgHeartbeatTopic;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
