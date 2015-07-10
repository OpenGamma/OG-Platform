/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.component;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.BuilderUtil;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergHistoricalTimeSeriesSource;
import com.opengamma.bbg.model.ReferenceDataRequestMessage;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceResource;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProviderFactoryBean;
import com.opengamma.transport.jms.JmsByteArrayRequestDispatcher;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsTopicContainer;
import com.opengamma.util.rest.DataConfigurationResource;

/**
 * Component factory to re-create the old means of accessing the reference data and time-series.
 */
@BeanDefinition
public class OldBloombergRemoteAccessComponentFactory extends AbstractComponentFactory {

  /**
   * The new reference data provider.
   */
  @PropertyDefinition(validate = "notNull")
  private ReferenceDataProvider _referenceDataProvider;
  /**
   * The new time-series provider.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesProvider _historicalTimeSeriesProvider;
  /**
   * The Fudge context.
   */
  @PropertyDefinition(validate = "notNull")
  private FudgeContext _fudgeContext;
  /**
   * The Jetty port.
   */
  @PropertyDefinition
  private int _jettyPort;
  /**
   * The JMS connector.
   */
  @PropertyDefinition(validate = "notNull")
  private JmsConnector _jmsConnector;
  /**
   * The name of the reference data topic.
   */
  @PropertyDefinition(validate = "notNull")
  private String _jmsReferenceDataTopic;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    initReferenceData(repo);
    initHistoricalTimeSeries(repo);
    initConfiguration(repo);
  }

  private void initReferenceData(ComponentRepository repo) {
    ReferenceDataProviderRequestReceiver receiver = new ReferenceDataProviderRequestReceiver(getReferenceDataProvider());
    FudgeRequestDispatcher dispatcher = new FudgeRequestDispatcher(receiver);
    JmsByteArrayRequestDispatcher jmsDispatcher = new JmsByteArrayRequestDispatcher(dispatcher);
    JmsTopicContainer jmsContainer = getJmsConnector().getTopicContainerFactory().create(getJmsReferenceDataTopic(), jmsDispatcher);
    repo.registerLifecycle(jmsContainer);
  }

  private void initHistoricalTimeSeries(ComponentRepository repo) {
    HistoricalTimeSeriesSource source = new BloombergHistoricalTimeSeriesSource(getHistoricalTimeSeriesProvider());
    DataHistoricalTimeSeriesSourceResource resource = new DataHistoricalTimeSeriesSourceResource(source);
    repo.getRestComponents().publishResource(resource);
  }

  private void initConfiguration(ComponentRepository repo) {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    
    UriEndPointDescriptionProviderFactoryBean factory = new UriEndPointDescriptionProviderFactoryBean();
    factory.setLocal("/jax/htsSource/");
    factory.setPort(getJettyPort());
    map.put("historicalTimeSeriesSource", factory.getObjectCreating());
    
    Map<String, Object> outer = new LinkedHashMap<String, Object>();
    outer.put("0", map);
    
    DataConfigurationResource resource = new DataConfigurationResource(getFudgeContext(), outer);
    repo.getRestComponents().publishResource(resource);
  }

  //-------------------------------------------------------------------------
  /**
  * Reference data request receiver.
  */
  static class ReferenceDataProviderRequestReceiver implements FudgeRequestReceiver {
    private final ReferenceDataProvider _underlying;
    
    ReferenceDataProviderRequestReceiver(ReferenceDataProvider underlying) {
      ArgumentChecker.notNull(underlying, "Reference Data Provider");
      _underlying = underlying;
    }

    @Override
    public FudgeMsg requestReceived(final FudgeDeserializer deserializer, final FudgeMsgEnvelope requestEnvelope) {
      ArgumentChecker.notNull(deserializer, "FudgeContext");
      ArgumentChecker.notNull(requestEnvelope, "FudgeMessageEnvelope");
      FudgeMsg requestFudgeMsg = requestEnvelope.getMessage();
      if (requestFudgeMsg == null) {
        throw new OpenGammaRuntimeException("Request fudgeMsg cannot be null");
      }
      ReferenceDataRequestMessage refDataRequest = ReferenceDataRequestMessage.fromFudgeMsg(deserializer, requestFudgeMsg);
      if (refDataRequest == null) {
        throw new OpenGammaRuntimeException("reference data request message from fudgeMsg cannot be null");
      }
      Set<String> securities = refDataRequest.getSecurities();
      Set<String> fields = refDataRequest.getFields();
      ReferenceDataProviderGetRequest underlyingRequest = ReferenceDataProviderGetRequest.createGet(securities, fields, true);
      ReferenceDataProviderGetResult underlyingResult = _underlying.getReferenceData(underlyingRequest);
      ReferenceDataResult refDataResult = new ReferenceDataResult();
      for (ReferenceData refData : underlyingResult.getReferenceData()) {
        refDataResult.addResult(new PerSecurityReferenceDataResult(refData));
      }
      return refDataResult.toFudgeMsg(deserializer.getFudgeContext());
    }
  }

  static class ReferenceDataResult {
    // aim is to replicate old message
    private static final String PER_SECURITY_FIELD_NAME = "PerSecurityReferenceDataResult";
    private static final String SECURITY_FIELD_NAME = "security";
    private static final String FIELD_DATA_FIELD_NAME = "fields";
    private static final String FIELD_EXCEPTIONS_FIELD_NAME = "fieldExceptions";
    private static final String EXCEPTIONS_FIELD_NAME = "exceptions";

    private final Map<String, PerSecurityReferenceDataResult> _resultsBySecurity = Maps.newTreeMap();

    ReferenceDataResult() {
    }

    public void addResult(PerSecurityReferenceDataResult result) {
      ArgumentChecker.notNull(result, "Per Security Reference Data Result");
      _resultsBySecurity.put(result.getSecurity(), result);
    }

    public FudgeMsg toFudgeMsg(FudgeContext fudgeContext) {
      ArgumentChecker.notNull(fudgeContext, "FudgeContext");
      FudgeSerializer serializer = new FudgeSerializer(fudgeContext);
      MutableFudgeMsg msg = serializer.newMessage();
      for (String sec : _resultsBySecurity.keySet()) {
        PerSecurityReferenceDataResult result = _resultsBySecurity.get(sec);
        if (result != null) {
          MutableFudgeMsg subMsg = serializer.newMessage();
          String security = result.getSecurity();
          subMsg.add(SECURITY_FIELD_NAME, security);
          FudgeMsg fieldData = result.getFieldData();
          subMsg.add(FIELD_DATA_FIELD_NAME, fieldData);
          List<String> exceptions = result.getExceptions();
          for (String exception : exceptions) {
            subMsg.add(EXCEPTIONS_FIELD_NAME, exception);
          }
          FudgeMsg exceptionMsg = buildExceptions(serializer, result.getFieldExceptions());
          subMsg.add(FIELD_EXCEPTIONS_FIELD_NAME, exceptionMsg);
          msg.add(PER_SECURITY_FIELD_NAME, subMsg);
        }
      }
      return msg;
    }

    private FudgeMsg buildExceptions(FudgeSerializer serializer, Map<String, ReferenceDataError> map) {
      final MutableFudgeMsg msg = serializer.newMessage();
      msg.add(null, BuilderUtil.KEY_TYPE_HINT_ORDINAL, FudgeWireType.STRING, "java.lang.String");
      msg.add(null, BuilderUtil.VALUE_TYPE_HINT_ORDINAL, FudgeWireType.STRING, "com.opengamma.bbg.ErrorInfo");
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        serializer.addToMessage(msg, null, BuilderUtil.KEY_ORDINAL, entry.getKey());
        serializer.addToMessage(msg, null, BuilderUtil.VALUE_ORDINAL, entry.getValue());
      }
      return msg;
    }

    public static ReferenceDataResult fromFudgeMsg(FudgeMsg msg, FudgeContext fudgeContext) {
      throw new UnsupportedOperationException();
    }
  }

  //-------------------------------------------------------------------------
  static class PerSecurityReferenceDataResult {
    private final String _security;
    private FudgeMsg _fieldData;
    private Set<Integer> _eidData;
    private final List<String> _exceptions = Lists.newArrayList();
    private final Map<String, ReferenceDataError> _fieldExceptions = Maps.newLinkedHashMap();

    PerSecurityReferenceDataResult(ReferenceData refData) {
      _security = refData.getIdentifier();
      _fieldData = refData.getFieldValues();
      _eidData = refData.getEidValues();
      for (ReferenceDataError error : refData.getErrors()) {
        if (error.isFieldBased()) {
          _fieldExceptions.put(error.getField(), error);
          error.setField(null);
        } else {
          String errorMessage = MessageFormat.format("{0}:{1}/{2} - {3}", error.getCode(),
              error.getCategory(), error.getSubcategory(), error.getMessage());
          _exceptions.add(errorMessage);
        }
      }
    }

    public String getSecurity() {
      return _security;
    }
    public FudgeMsg getFieldData() {
      return _fieldData;
    }

    public Set<Integer> getEidData() {
      return _eidData;
    }
    public List<String> getExceptions() {
      return _exceptions;
    }
    public Map<String, ReferenceDataError> getFieldExceptions() {
      return _fieldExceptions;
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code OldBloombergRemoteAccessComponentFactory}.
   * @return the meta-bean, not null
   */
  public static OldBloombergRemoteAccessComponentFactory.Meta meta() {
    return OldBloombergRemoteAccessComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(OldBloombergRemoteAccessComponentFactory.Meta.INSTANCE);
  }

  @Override
  public OldBloombergRemoteAccessComponentFactory.Meta metaBean() {
    return OldBloombergRemoteAccessComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the new reference data provider.
   * @return the value of the property, not null
   */
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

  /**
   * Sets the new reference data provider.
   * @param referenceDataProvider  the new value of the property, not null
   */
  public void setReferenceDataProvider(ReferenceDataProvider referenceDataProvider) {
    JodaBeanUtils.notNull(referenceDataProvider, "referenceDataProvider");
    this._referenceDataProvider = referenceDataProvider;
  }

  /**
   * Gets the the {@code referenceDataProvider} property.
   * @return the property, not null
   */
  public final Property<ReferenceDataProvider> referenceDataProvider() {
    return metaBean().referenceDataProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the new time-series provider.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesProvider getHistoricalTimeSeriesProvider() {
    return _historicalTimeSeriesProvider;
  }

  /**
   * Sets the new time-series provider.
   * @param historicalTimeSeriesProvider  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesProvider(HistoricalTimeSeriesProvider historicalTimeSeriesProvider) {
    JodaBeanUtils.notNull(historicalTimeSeriesProvider, "historicalTimeSeriesProvider");
    this._historicalTimeSeriesProvider = historicalTimeSeriesProvider;
  }

  /**
   * Gets the the {@code historicalTimeSeriesProvider} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesProvider> historicalTimeSeriesProvider() {
    return metaBean().historicalTimeSeriesProvider().createProperty(this);
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
   * Gets the Jetty port.
   * @return the value of the property
   */
  public int getJettyPort() {
    return _jettyPort;
  }

  /**
   * Sets the Jetty port.
   * @param jettyPort  the new value of the property
   */
  public void setJettyPort(int jettyPort) {
    this._jettyPort = jettyPort;
  }

  /**
   * Gets the the {@code jettyPort} property.
   * @return the property, not null
   */
  public final Property<Integer> jettyPort() {
    return metaBean().jettyPort().createProperty(this);
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
   * Gets the name of the reference data topic.
   * @return the value of the property, not null
   */
  public String getJmsReferenceDataTopic() {
    return _jmsReferenceDataTopic;
  }

  /**
   * Sets the name of the reference data topic.
   * @param jmsReferenceDataTopic  the new value of the property, not null
   */
  public void setJmsReferenceDataTopic(String jmsReferenceDataTopic) {
    JodaBeanUtils.notNull(jmsReferenceDataTopic, "jmsReferenceDataTopic");
    this._jmsReferenceDataTopic = jmsReferenceDataTopic;
  }

  /**
   * Gets the the {@code jmsReferenceDataTopic} property.
   * @return the property, not null
   */
  public final Property<String> jmsReferenceDataTopic() {
    return metaBean().jmsReferenceDataTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public OldBloombergRemoteAccessComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      OldBloombergRemoteAccessComponentFactory other = (OldBloombergRemoteAccessComponentFactory) obj;
      return JodaBeanUtils.equal(getReferenceDataProvider(), other.getReferenceDataProvider()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesProvider(), other.getHistoricalTimeSeriesProvider()) &&
          JodaBeanUtils.equal(getFudgeContext(), other.getFudgeContext()) &&
          (getJettyPort() == other.getJettyPort()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getJmsReferenceDataTopic(), other.getJmsReferenceDataTopic()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getReferenceDataProvider());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesProvider());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFudgeContext());
    hash = hash * 31 + JodaBeanUtils.hashCode(getJettyPort());
    hash = hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash = hash * 31 + JodaBeanUtils.hashCode(getJmsReferenceDataTopic());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("OldBloombergRemoteAccessComponentFactory{");
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
    buf.append("referenceDataProvider").append('=').append(JodaBeanUtils.toString(getReferenceDataProvider())).append(',').append(' ');
    buf.append("historicalTimeSeriesProvider").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesProvider())).append(',').append(' ');
    buf.append("fudgeContext").append('=').append(JodaBeanUtils.toString(getFudgeContext())).append(',').append(' ');
    buf.append("jettyPort").append('=').append(JodaBeanUtils.toString(getJettyPort())).append(',').append(' ');
    buf.append("jmsConnector").append('=').append(JodaBeanUtils.toString(getJmsConnector())).append(',').append(' ');
    buf.append("jmsReferenceDataTopic").append('=').append(JodaBeanUtils.toString(getJmsReferenceDataTopic())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code OldBloombergRemoteAccessComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code referenceDataProvider} property.
     */
    private final MetaProperty<ReferenceDataProvider> _referenceDataProvider = DirectMetaProperty.ofReadWrite(
        this, "referenceDataProvider", OldBloombergRemoteAccessComponentFactory.class, ReferenceDataProvider.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesProvider} property.
     */
    private final MetaProperty<HistoricalTimeSeriesProvider> _historicalTimeSeriesProvider = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesProvider", OldBloombergRemoteAccessComponentFactory.class, HistoricalTimeSeriesProvider.class);
    /**
     * The meta-property for the {@code fudgeContext} property.
     */
    private final MetaProperty<FudgeContext> _fudgeContext = DirectMetaProperty.ofReadWrite(
        this, "fudgeContext", OldBloombergRemoteAccessComponentFactory.class, FudgeContext.class);
    /**
     * The meta-property for the {@code jettyPort} property.
     */
    private final MetaProperty<Integer> _jettyPort = DirectMetaProperty.ofReadWrite(
        this, "jettyPort", OldBloombergRemoteAccessComponentFactory.class, Integer.TYPE);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", OldBloombergRemoteAccessComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code jmsReferenceDataTopic} property.
     */
    private final MetaProperty<String> _jmsReferenceDataTopic = DirectMetaProperty.ofReadWrite(
        this, "jmsReferenceDataTopic", OldBloombergRemoteAccessComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "referenceDataProvider",
        "historicalTimeSeriesProvider",
        "fudgeContext",
        "jettyPort",
        "jmsConnector",
        "jmsReferenceDataTopic");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1788671322:  // referenceDataProvider
          return _referenceDataProvider;
        case -1592479713:  // historicalTimeSeriesProvider
          return _historicalTimeSeriesProvider;
        case -917704420:  // fudgeContext
          return _fudgeContext;
        case 625005695:  // jettyPort
          return _jettyPort;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
        case -1416126390:  // jmsReferenceDataTopic
          return _jmsReferenceDataTopic;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends OldBloombergRemoteAccessComponentFactory> builder() {
      return new DirectBeanBuilder<OldBloombergRemoteAccessComponentFactory>(new OldBloombergRemoteAccessComponentFactory());
    }

    @Override
    public Class<? extends OldBloombergRemoteAccessComponentFactory> beanType() {
      return OldBloombergRemoteAccessComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code referenceDataProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ReferenceDataProvider> referenceDataProvider() {
      return _referenceDataProvider;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesProvider> historicalTimeSeriesProvider() {
      return _historicalTimeSeriesProvider;
    }

    /**
     * The meta-property for the {@code fudgeContext} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FudgeContext> fudgeContext() {
      return _fudgeContext;
    }

    /**
     * The meta-property for the {@code jettyPort} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> jettyPort() {
      return _jettyPort;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    /**
     * The meta-property for the {@code jmsReferenceDataTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jmsReferenceDataTopic() {
      return _jmsReferenceDataTopic;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1788671322:  // referenceDataProvider
          return ((OldBloombergRemoteAccessComponentFactory) bean).getReferenceDataProvider();
        case -1592479713:  // historicalTimeSeriesProvider
          return ((OldBloombergRemoteAccessComponentFactory) bean).getHistoricalTimeSeriesProvider();
        case -917704420:  // fudgeContext
          return ((OldBloombergRemoteAccessComponentFactory) bean).getFudgeContext();
        case 625005695:  // jettyPort
          return ((OldBloombergRemoteAccessComponentFactory) bean).getJettyPort();
        case -1495762275:  // jmsConnector
          return ((OldBloombergRemoteAccessComponentFactory) bean).getJmsConnector();
        case -1416126390:  // jmsReferenceDataTopic
          return ((OldBloombergRemoteAccessComponentFactory) bean).getJmsReferenceDataTopic();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1788671322:  // referenceDataProvider
          ((OldBloombergRemoteAccessComponentFactory) bean).setReferenceDataProvider((ReferenceDataProvider) newValue);
          return;
        case -1592479713:  // historicalTimeSeriesProvider
          ((OldBloombergRemoteAccessComponentFactory) bean).setHistoricalTimeSeriesProvider((HistoricalTimeSeriesProvider) newValue);
          return;
        case -917704420:  // fudgeContext
          ((OldBloombergRemoteAccessComponentFactory) bean).setFudgeContext((FudgeContext) newValue);
          return;
        case 625005695:  // jettyPort
          ((OldBloombergRemoteAccessComponentFactory) bean).setJettyPort((Integer) newValue);
          return;
        case -1495762275:  // jmsConnector
          ((OldBloombergRemoteAccessComponentFactory) bean).setJmsConnector((JmsConnector) newValue);
          return;
        case -1416126390:  // jmsReferenceDataTopic
          ((OldBloombergRemoteAccessComponentFactory) bean).setJmsReferenceDataTopic((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((OldBloombergRemoteAccessComponentFactory) bean)._referenceDataProvider, "referenceDataProvider");
      JodaBeanUtils.notNull(((OldBloombergRemoteAccessComponentFactory) bean)._historicalTimeSeriesProvider, "historicalTimeSeriesProvider");
      JodaBeanUtils.notNull(((OldBloombergRemoteAccessComponentFactory) bean)._fudgeContext, "fudgeContext");
      JodaBeanUtils.notNull(((OldBloombergRemoteAccessComponentFactory) bean)._jmsConnector, "jmsConnector");
      JodaBeanUtils.notNull(((OldBloombergRemoteAccessComponentFactory) bean)._jmsReferenceDataTopic, "jmsReferenceDataTopic");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
