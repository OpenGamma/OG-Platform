/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.component;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.BloombergSecuritySource;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.RemoteReferenceDataProviderFactoryBean;
import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.bbg.loader.BloombergSecurityLoader;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.master.historicaltimeseries.ExternalIdResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.transport.jaxrs.UriEndPointUriFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * Component factory for the Integration project.
 */
@BeanDefinition
public class IntegrationDataComponentFactory extends AbstractComponentFactory {

  private static final String BBG_CLASSIFIER = "bbg";
  private static final String STANDARD_CLASSIFIER = "standard";

  /**
   * The configuration URI.
   */
  @PropertyDefinition(validate = "notNull")
  private URI _bbgServerConfigurationUri;
  /**
   * The JMS connector.
   */
  @PropertyDefinition(validate = "notNull")
  private String _referenceDataJmsTopic;

  /**
   * The security master.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityMaster _securityMaster;
  /**
   * The time-series master.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  /**
   * The Fudge context.
   */
  @PropertyDefinition(validate = "notNull")
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  /**
   * The JMS connector.
   */
  @PropertyDefinition(validate = "notNull")
  private JmsConnector _jmsConnector;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    ReferenceDataProvider refData = initReferenceDataProvider(repo);
    HistoricalTimeSeriesSource bbgHtsSource = initTimeSeriesSource(repo);
    initSecurityLoader(repo, refData, bbgHtsSource);
    initHistoricalTimeSeriesLoader(repo, refData, bbgHtsSource);
    initBloombergSecuritySource(repo, refData);
  }

  private void initBloombergSecuritySource(ComponentRepository repo, ReferenceDataProvider refData) {
    BloombergSecuritySource bloombergSecuritySource = new BloombergSecuritySource(refData, new DefaultExchangeDataProvider());
    ComponentInfo info = new ComponentInfo(BloombergSecuritySource.class, BBG_CLASSIFIER);
    repo.registerComponent(info, bloombergSecuritySource);
  }

  protected ReferenceDataProvider initReferenceDataProvider(ComponentRepository repo) {
    RemoteReferenceDataProviderFactoryBean factory = new RemoteReferenceDataProviderFactoryBean();
    factory.setJmsConnector(getJmsConnector());
    factory.setRequestTopic(_referenceDataJmsTopic);
    factory.setFudgeContext(getFudgeContext());
    
    ReferenceDataProvider refData = factory.getObjectCreating();
    ComponentInfo info = new ComponentInfo(ReferenceDataProvider.class, BBG_CLASSIFIER);
    repo.registerComponent(info, refData);
    return refData;
  }

  protected HistoricalTimeSeriesSource initTimeSeriesSource(ComponentRepository repo) {
    UriEndPointUriFactoryBean factory = new UriEndPointUriFactoryBean();
    factory.setUri(getBbgServerConfigurationUri());
    URI uri = factory.getObjectCreating();
    
    RemoteHistoricalTimeSeriesSource bbgHtsSource = new RemoteHistoricalTimeSeriesSource(uri);
    ComponentInfo info = new ComponentInfo(HistoricalTimeSeriesSource.class, BBG_CLASSIFIER);
    repo.registerComponent(info, bbgHtsSource);
    return bbgHtsSource;
  }

  protected SecurityLoader initSecurityLoader(ComponentRepository repo, ReferenceDataProvider refData, HistoricalTimeSeriesSource bbgHtsSource) {

    SecurityLoader secLoader = createSecurityLoader(refData, bbgHtsSource);

    ComponentInfo info = new ComponentInfo(SecurityLoader.class, STANDARD_CLASSIFIER);
    repo.registerComponent(info, secLoader);
    return secLoader;
  }

  protected SecurityLoader createSecurityLoader(ReferenceDataProvider refData, HistoricalTimeSeriesSource bbgHtsSource) {
    // bulk loader
    ExchangeDataProvider exchangeDataProvider = initExchangeDataProvider();
    BloombergBulkSecurityLoader bbgBulkSecLoader = new BloombergBulkSecurityLoader(refData, exchangeDataProvider);

    // security master loader
    SecurityLoader bbgSecLoader = new BloombergSecurityLoader(getSecurityMaster(), bbgBulkSecLoader);

    return bbgSecLoader;
  }
  
  protected HistoricalTimeSeriesLoader initHistoricalTimeSeriesLoader(ComponentRepository repo, ReferenceDataProvider refData, HistoricalTimeSeriesSource bbgHtsSource) {
    ExternalIdResolver idProvider = new BloombergIdentifierProvider(refData);
    HistoricalTimeSeriesLoader htsLoader = new BloombergHistoricalLoader(getHistoricalTimeSeriesMaster(), bbgHtsSource, idProvider);
    ComponentInfo info = new ComponentInfo(HistoricalTimeSeriesLoader.class, STANDARD_CLASSIFIER);
    repo.registerComponent(info, htsLoader);
    return htsLoader;
  }

  protected ExchangeDataProvider initExchangeDataProvider() {
    return new DefaultExchangeDataProvider();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code IntegrationDataComponentFactory}.
   * @return the meta-bean, not null
   */
  public static IntegrationDataComponentFactory.Meta meta() {
    return IntegrationDataComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(IntegrationDataComponentFactory.Meta.INSTANCE);
  }

  @Override
  public IntegrationDataComponentFactory.Meta metaBean() {
    return IntegrationDataComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -848676096:  // bbgServerConfigurationUri
        return getBbgServerConfigurationUri();
      case 580124724:  // referenceDataJmsTopic
        return getReferenceDataJmsTopic();
      case -887218750:  // securityMaster
        return getSecurityMaster();
      case 173967376:  // historicalTimeSeriesMaster
        return getHistoricalTimeSeriesMaster();
      case -917704420:  // fudgeContext
        return getFudgeContext();
      case -1495762275:  // jmsConnector
        return getJmsConnector();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -848676096:  // bbgServerConfigurationUri
        setBbgServerConfigurationUri((URI) newValue);
        return;
      case 580124724:  // referenceDataJmsTopic
        setReferenceDataJmsTopic((String) newValue);
        return;
      case -887218750:  // securityMaster
        setSecurityMaster((SecurityMaster) newValue);
        return;
      case 173967376:  // historicalTimeSeriesMaster
        setHistoricalTimeSeriesMaster((HistoricalTimeSeriesMaster) newValue);
        return;
      case -917704420:  // fudgeContext
        setFudgeContext((FudgeContext) newValue);
        return;
      case -1495762275:  // jmsConnector
        setJmsConnector((JmsConnector) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_bbgServerConfigurationUri, "bbgServerConfigurationUri");
    JodaBeanUtils.notNull(_referenceDataJmsTopic, "referenceDataJmsTopic");
    JodaBeanUtils.notNull(_securityMaster, "securityMaster");
    JodaBeanUtils.notNull(_historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    JodaBeanUtils.notNull(_fudgeContext, "fudgeContext");
    JodaBeanUtils.notNull(_jmsConnector, "jmsConnector");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      IntegrationDataComponentFactory other = (IntegrationDataComponentFactory) obj;
      return JodaBeanUtils.equal(getBbgServerConfigurationUri(), other.getBbgServerConfigurationUri()) &&
          JodaBeanUtils.equal(getReferenceDataJmsTopic(), other.getReferenceDataJmsTopic()) &&
          JodaBeanUtils.equal(getSecurityMaster(), other.getSecurityMaster()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesMaster(), other.getHistoricalTimeSeriesMaster()) &&
          JodaBeanUtils.equal(getFudgeContext(), other.getFudgeContext()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getBbgServerConfigurationUri());
    hash += hash * 31 + JodaBeanUtils.hashCode(getReferenceDataJmsTopic());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFudgeContext());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the configuration URI.
   * @return the value of the property, not null
   */
  public URI getBbgServerConfigurationUri() {
    return _bbgServerConfigurationUri;
  }

  /**
   * Sets the configuration URI.
   * @param bbgServerConfigurationUri  the new value of the property, not null
   */
  public void setBbgServerConfigurationUri(URI bbgServerConfigurationUri) {
    JodaBeanUtils.notNull(bbgServerConfigurationUri, "bbgServerConfigurationUri");
    this._bbgServerConfigurationUri = bbgServerConfigurationUri;
  }

  /**
   * Gets the the {@code bbgServerConfigurationUri} property.
   * @return the property, not null
   */
  public final Property<URI> bbgServerConfigurationUri() {
    return metaBean().bbgServerConfigurationUri().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS connector.
   * @return the value of the property, not null
   */
  public String getReferenceDataJmsTopic() {
    return _referenceDataJmsTopic;
  }

  /**
   * Sets the JMS connector.
   * @param referenceDataJmsTopic  the new value of the property, not null
   */
  public void setReferenceDataJmsTopic(String referenceDataJmsTopic) {
    JodaBeanUtils.notNull(referenceDataJmsTopic, "referenceDataJmsTopic");
    this._referenceDataJmsTopic = referenceDataJmsTopic;
  }

  /**
   * Gets the the {@code referenceDataJmsTopic} property.
   * @return the property, not null
   */
  public final Property<String> referenceDataJmsTopic() {
    return metaBean().referenceDataJmsTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security master.
   * @return the value of the property, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Sets the security master.
   * @param securityMaster  the new value of the property, not null
   */
  public void setSecurityMaster(SecurityMaster securityMaster) {
    JodaBeanUtils.notNull(securityMaster, "securityMaster");
    this._securityMaster = securityMaster;
  }

  /**
   * Gets the the {@code securityMaster} property.
   * @return the property, not null
   */
  public final Property<SecurityMaster> securityMaster() {
    return metaBean().securityMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series master.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  /**
   * Sets the time-series master.
   * @param historicalTimeSeriesMaster  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster historicalTimeSeriesMaster) {
    JodaBeanUtils.notNull(historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    this._historicalTimeSeriesMaster = historicalTimeSeriesMaster;
  }

  /**
   * Gets the the {@code historicalTimeSeriesMaster} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
    return metaBean().historicalTimeSeriesMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * @return the value of the property, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the Fudge context.
   * @param fudgeContext  the new value of the property, not null
   */
  public void setFudgeContext(FudgeContext fudgeContext) {
    JodaBeanUtils.notNull(fudgeContext, "fudgeContext");
    this._fudgeContext = fudgeContext;
  }

  /**
   * Gets the the {@code fudgeContext} property.
   * @return the property, not null
   */
  public final Property<FudgeContext> fudgeContext() {
    return metaBean().fudgeContext().createProperty(this);
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
   * The meta-bean for {@code IntegrationDataComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code bbgServerConfigurationUri} property.
     */
    private final MetaProperty<URI> _bbgServerConfigurationUri = DirectMetaProperty.ofReadWrite(
        this, "bbgServerConfigurationUri", IntegrationDataComponentFactory.class, URI.class);
    /**
     * The meta-property for the {@code referenceDataJmsTopic} property.
     */
    private final MetaProperty<String> _referenceDataJmsTopic = DirectMetaProperty.ofReadWrite(
        this, "referenceDataJmsTopic", IntegrationDataComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code securityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _securityMaster = DirectMetaProperty.ofReadWrite(
        this, "securityMaster", IntegrationDataComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     */
    private final MetaProperty<HistoricalTimeSeriesMaster> _historicalTimeSeriesMaster = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesMaster", IntegrationDataComponentFactory.class, HistoricalTimeSeriesMaster.class);
    /**
     * The meta-property for the {@code fudgeContext} property.
     */
    private final MetaProperty<FudgeContext> _fudgeContext = DirectMetaProperty.ofReadWrite(
        this, "fudgeContext", IntegrationDataComponentFactory.class, FudgeContext.class);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", IntegrationDataComponentFactory.class, JmsConnector.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "bbgServerConfigurationUri",
        "referenceDataJmsTopic",
        "securityMaster",
        "historicalTimeSeriesMaster",
        "fudgeContext",
        "jmsConnector");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -848676096:  // bbgServerConfigurationUri
          return _bbgServerConfigurationUri;
        case 580124724:  // referenceDataJmsTopic
          return _referenceDataJmsTopic;
        case -887218750:  // securityMaster
          return _securityMaster;
        case 173967376:  // historicalTimeSeriesMaster
          return _historicalTimeSeriesMaster;
        case -917704420:  // fudgeContext
          return _fudgeContext;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends IntegrationDataComponentFactory> builder() {
      return new DirectBeanBuilder<IntegrationDataComponentFactory>(new IntegrationDataComponentFactory());
    }

    @Override
    public Class<? extends IntegrationDataComponentFactory> beanType() {
      return IntegrationDataComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code bbgServerConfigurationUri} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<URI> bbgServerConfigurationUri() {
      return _bbgServerConfigurationUri;
    }

    /**
     * The meta-property for the {@code referenceDataJmsTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> referenceDataJmsTopic() {
      return _referenceDataJmsTopic;
    }

    /**
     * The meta-property for the {@code securityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityMaster> securityMaster() {
      return _securityMaster;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
      return _historicalTimeSeriesMaster;
    }

    /**
     * The meta-property for the {@code fudgeContext} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FudgeContext> fudgeContext() {
      return _fudgeContext;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
