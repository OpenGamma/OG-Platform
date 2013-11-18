/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.master;

import java.util.Collection;
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
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.change.JmsChangeManager;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.ParallelQuerySplittingPositionMaster;
import com.opengamma.master.position.impl.QuerySplittingPositionMaster;
import com.opengamma.master.position.impl.RemotePositionMaster;
import com.opengamma.masterdb.position.DataDbPositionMasterResource;
import com.opengamma.masterdb.position.DbPositionMaster;
import com.opengamma.util.jms.JmsConnector;

/**
 * Component factory for the database position master.
 */
@BeanDefinition
public class DbPositionMasterComponentFactory extends AbstractDbMasterComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The JMS connector.
   */
  @PropertyDefinition
  private JmsConnector _jmsConnector;
  /**
   * The JMS change manager topic.
   */
  @PropertyDefinition
  private String _jmsChangeManagerTopic;
  /**
   * The scheme used by the {@code UniqueId}.
   */
  @PropertyDefinition
  private String _uniqueIdScheme;
  /**
   * The maximum number of retries when updating.
   */
  @PropertyDefinition
  private Integer _maxRetries;
  /**
   * The maximum number of get requests to pass in one hit - see {@link QuerySplittingPositionMaster#get(Collection)}
   */
  @PropertyDefinition
  private Integer _maxGetRequestSize;
  /**
   * The maximum size of search request to pass in one hit - see {@link QuerySplittingPositionMaster#search}
   */
  @PropertyDefinition
  private Integer _maxSearchRequestSize;
  /**
   * Whether to use parallel search queries - see {@link ParallelQuerySplittingPositionMaster}
   */
  @PropertyDefinition
  private boolean _parallelSearchQueries;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    ComponentInfo info = new ComponentInfo(PositionMaster.class, getClassifier());

    // create
    DbPositionMaster master = new DbPositionMaster(getDbConnector());
    if (getUniqueIdScheme() != null) {
      master.setUniqueIdScheme(getUniqueIdScheme());
    }
    if (getMaxRetries() != null) {
      master.setMaxRetries(getMaxRetries());
    }
    if (getJmsChangeManagerTopic() != null) {
      JmsChangeManager cm = new JmsChangeManager(getJmsConnector(), getJmsChangeManagerTopic());
      master.setChangeManager(cm);
      repo.registerLifecycle(cm);
      if (getJmsConnector().getClientBrokerUri() != null) {
        info.addAttribute(ComponentInfoAttributes.JMS_BROKER_URI, getJmsConnector().getClientBrokerUri().toString());
      }
      info.addAttribute(ComponentInfoAttributes.JMS_CHANGE_MANAGER_TOPIC, getJmsChangeManagerTopic());
    }
    checkSchema(master.getSchemaVersion(), "pos");

    // register
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemotePositionMaster.class);
    info.addAttribute(ComponentInfoAttributes.UNIQUE_ID_SCHEME, master.getUniqueIdScheme());
    repo.registerComponent(info, splitQueries(master));

    // publish
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataDbPositionMasterResource(master));
    }
  }

  /**
   * If query splitting is enabled, wraps the position master with a query splitter.
   * 
   * @param master the underlying master, not null
   * @return the original master if splitting is disabled, otherwise the splitting form
   */
  protected PositionMaster splitQueries(final PositionMaster master) {
    final QuerySplittingPositionMaster splitting = isParallelSearchQueries() ? new ParallelQuerySplittingPositionMaster(master) : new QuerySplittingPositionMaster(master);
    boolean wrapped = false;
    if (getMaxGetRequestSize() != null) {
      splitting.setMaxGetRequest(getMaxGetRequestSize());
      wrapped = true;
    }
    if (getMaxSearchRequestSize() != null) {
      splitting.setMaxSearchRequest(getMaxSearchRequestSize());
      wrapped = true;
    }
    if (wrapped) {
      return splitting;
    } else {
      return master;
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbPositionMasterComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DbPositionMasterComponentFactory.Meta meta() {
    return DbPositionMasterComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DbPositionMasterComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DbPositionMasterComponentFactory.Meta metaBean() {
    return DbPositionMasterComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property
   */
  public void setClassifier(String classifier) {
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
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS connector.
   * @return the value of the property
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector.
   * @param jmsConnector  the new value of the property
   */
  public void setJmsConnector(JmsConnector jmsConnector) {
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
   * Gets the JMS change manager topic.
   * @return the value of the property
   */
  public String getJmsChangeManagerTopic() {
    return _jmsChangeManagerTopic;
  }

  /**
   * Sets the JMS change manager topic.
   * @param jmsChangeManagerTopic  the new value of the property
   */
  public void setJmsChangeManagerTopic(String jmsChangeManagerTopic) {
    this._jmsChangeManagerTopic = jmsChangeManagerTopic;
  }

  /**
   * Gets the the {@code jmsChangeManagerTopic} property.
   * @return the property, not null
   */
  public final Property<String> jmsChangeManagerTopic() {
    return metaBean().jmsChangeManagerTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the scheme used by the {@code UniqueId}.
   * @return the value of the property
   */
  public String getUniqueIdScheme() {
    return _uniqueIdScheme;
  }

  /**
   * Sets the scheme used by the {@code UniqueId}.
   * @param uniqueIdScheme  the new value of the property
   */
  public void setUniqueIdScheme(String uniqueIdScheme) {
    this._uniqueIdScheme = uniqueIdScheme;
  }

  /**
   * Gets the the {@code uniqueIdScheme} property.
   * @return the property, not null
   */
  public final Property<String> uniqueIdScheme() {
    return metaBean().uniqueIdScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum number of retries when updating.
   * @return the value of the property
   */
  public Integer getMaxRetries() {
    return _maxRetries;
  }

  /**
   * Sets the maximum number of retries when updating.
   * @param maxRetries  the new value of the property
   */
  public void setMaxRetries(Integer maxRetries) {
    this._maxRetries = maxRetries;
  }

  /**
   * Gets the the {@code maxRetries} property.
   * @return the property, not null
   */
  public final Property<Integer> maxRetries() {
    return metaBean().maxRetries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum number of get requests to pass in one hit - see {@link QuerySplittingPositionMaster#get(Collection)}
   * @return the value of the property
   */
  public Integer getMaxGetRequestSize() {
    return _maxGetRequestSize;
  }

  /**
   * Sets the maximum number of get requests to pass in one hit - see {@link QuerySplittingPositionMaster#get(Collection)}
   * @param maxGetRequestSize  the new value of the property
   */
  public void setMaxGetRequestSize(Integer maxGetRequestSize) {
    this._maxGetRequestSize = maxGetRequestSize;
  }

  /**
   * Gets the the {@code maxGetRequestSize} property.
   * @return the property, not null
   */
  public final Property<Integer> maxGetRequestSize() {
    return metaBean().maxGetRequestSize().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum size of search request to pass in one hit - see {@link QuerySplittingPositionMaster#search}
   * @return the value of the property
   */
  public Integer getMaxSearchRequestSize() {
    return _maxSearchRequestSize;
  }

  /**
   * Sets the maximum size of search request to pass in one hit - see {@link QuerySplittingPositionMaster#search}
   * @param maxSearchRequestSize  the new value of the property
   */
  public void setMaxSearchRequestSize(Integer maxSearchRequestSize) {
    this._maxSearchRequestSize = maxSearchRequestSize;
  }

  /**
   * Gets the the {@code maxSearchRequestSize} property.
   * @return the property, not null
   */
  public final Property<Integer> maxSearchRequestSize() {
    return metaBean().maxSearchRequestSize().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to use parallel search queries - see {@link ParallelQuerySplittingPositionMaster}
   * @return the value of the property
   */
  public boolean isParallelSearchQueries() {
    return _parallelSearchQueries;
  }

  /**
   * Sets whether to use parallel search queries - see {@link ParallelQuerySplittingPositionMaster}
   * @param parallelSearchQueries  the new value of the property
   */
  public void setParallelSearchQueries(boolean parallelSearchQueries) {
    this._parallelSearchQueries = parallelSearchQueries;
  }

  /**
   * Gets the the {@code parallelSearchQueries} property.
   * @return the property, not null
   */
  public final Property<Boolean> parallelSearchQueries() {
    return metaBean().parallelSearchQueries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DbPositionMasterComponentFactory clone() {
    return (DbPositionMasterComponentFactory) super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DbPositionMasterComponentFactory other = (DbPositionMasterComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getJmsChangeManagerTopic(), other.getJmsChangeManagerTopic()) &&
          JodaBeanUtils.equal(getUniqueIdScheme(), other.getUniqueIdScheme()) &&
          JodaBeanUtils.equal(getMaxRetries(), other.getMaxRetries()) &&
          JodaBeanUtils.equal(getMaxGetRequestSize(), other.getMaxGetRequestSize()) &&
          JodaBeanUtils.equal(getMaxSearchRequestSize(), other.getMaxSearchRequestSize()) &&
          (isParallelSearchQueries() == other.isParallelSearchQueries()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsChangeManagerTopic());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueIdScheme());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaxRetries());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaxGetRequestSize());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaxSearchRequestSize());
    hash += hash * 31 + JodaBeanUtils.hashCode(isParallelSearchQueries());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("DbPositionMasterComponentFactory{");
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
    buf.append("publishRest").append('=').append(JodaBeanUtils.toString(isPublishRest())).append(',').append(' ');
    buf.append("jmsConnector").append('=').append(JodaBeanUtils.toString(getJmsConnector())).append(',').append(' ');
    buf.append("jmsChangeManagerTopic").append('=').append(JodaBeanUtils.toString(getJmsChangeManagerTopic())).append(',').append(' ');
    buf.append("uniqueIdScheme").append('=').append(JodaBeanUtils.toString(getUniqueIdScheme())).append(',').append(' ');
    buf.append("maxRetries").append('=').append(JodaBeanUtils.toString(getMaxRetries())).append(',').append(' ');
    buf.append("maxGetRequestSize").append('=').append(JodaBeanUtils.toString(getMaxGetRequestSize())).append(',').append(' ');
    buf.append("maxSearchRequestSize").append('=').append(JodaBeanUtils.toString(getMaxSearchRequestSize())).append(',').append(' ');
    buf.append("parallelSearchQueries").append('=').append(JodaBeanUtils.toString(isParallelSearchQueries())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbPositionMasterComponentFactory}.
   */
  public static class Meta extends AbstractDbMasterComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", DbPositionMasterComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", DbPositionMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", DbPositionMasterComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code jmsChangeManagerTopic} property.
     */
    private final MetaProperty<String> _jmsChangeManagerTopic = DirectMetaProperty.ofReadWrite(
        this, "jmsChangeManagerTopic", DbPositionMasterComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     */
    private final MetaProperty<String> _uniqueIdScheme = DirectMetaProperty.ofReadWrite(
        this, "uniqueIdScheme", DbPositionMasterComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code maxRetries} property.
     */
    private final MetaProperty<Integer> _maxRetries = DirectMetaProperty.ofReadWrite(
        this, "maxRetries", DbPositionMasterComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code maxGetRequestSize} property.
     */
    private final MetaProperty<Integer> _maxGetRequestSize = DirectMetaProperty.ofReadWrite(
        this, "maxGetRequestSize", DbPositionMasterComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code maxSearchRequestSize} property.
     */
    private final MetaProperty<Integer> _maxSearchRequestSize = DirectMetaProperty.ofReadWrite(
        this, "maxSearchRequestSize", DbPositionMasterComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code parallelSearchQueries} property.
     */
    private final MetaProperty<Boolean> _parallelSearchQueries = DirectMetaProperty.ofReadWrite(
        this, "parallelSearchQueries", DbPositionMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "jmsConnector",
        "jmsChangeManagerTopic",
        "uniqueIdScheme",
        "maxRetries",
        "maxGetRequestSize",
        "maxSearchRequestSize",
        "parallelSearchQueries");

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
        case -614707837:  // publishRest
          return _publishRest;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
        case -758086398:  // jmsChangeManagerTopic
          return _jmsChangeManagerTopic;
        case -1737146991:  // uniqueIdScheme
          return _uniqueIdScheme;
        case -2022653118:  // maxRetries
          return _maxRetries;
        case -769924994:  // maxGetRequestSize
          return _maxGetRequestSize;
        case 2100076388:  // maxSearchRequestSize
          return _maxSearchRequestSize;
        case -337894953:  // parallelSearchQueries
          return _parallelSearchQueries;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DbPositionMasterComponentFactory> builder() {
      return new DirectBeanBuilder<DbPositionMasterComponentFactory>(new DbPositionMasterComponentFactory());
    }

    @Override
    public Class<? extends DbPositionMasterComponentFactory> beanType() {
      return DbPositionMasterComponentFactory.class;
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
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    /**
     * The meta-property for the {@code jmsChangeManagerTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jmsChangeManagerTopic() {
      return _jmsChangeManagerTopic;
    }

    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uniqueIdScheme() {
      return _uniqueIdScheme;
    }

    /**
     * The meta-property for the {@code maxRetries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxRetries() {
      return _maxRetries;
    }

    /**
     * The meta-property for the {@code maxGetRequestSize} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxGetRequestSize() {
      return _maxGetRequestSize;
    }

    /**
     * The meta-property for the {@code maxSearchRequestSize} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxSearchRequestSize() {
      return _maxSearchRequestSize;
    }

    /**
     * The meta-property for the {@code parallelSearchQueries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> parallelSearchQueries() {
      return _parallelSearchQueries;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((DbPositionMasterComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((DbPositionMasterComponentFactory) bean).isPublishRest();
        case -1495762275:  // jmsConnector
          return ((DbPositionMasterComponentFactory) bean).getJmsConnector();
        case -758086398:  // jmsChangeManagerTopic
          return ((DbPositionMasterComponentFactory) bean).getJmsChangeManagerTopic();
        case -1737146991:  // uniqueIdScheme
          return ((DbPositionMasterComponentFactory) bean).getUniqueIdScheme();
        case -2022653118:  // maxRetries
          return ((DbPositionMasterComponentFactory) bean).getMaxRetries();
        case -769924994:  // maxGetRequestSize
          return ((DbPositionMasterComponentFactory) bean).getMaxGetRequestSize();
        case 2100076388:  // maxSearchRequestSize
          return ((DbPositionMasterComponentFactory) bean).getMaxSearchRequestSize();
        case -337894953:  // parallelSearchQueries
          return ((DbPositionMasterComponentFactory) bean).isParallelSearchQueries();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((DbPositionMasterComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((DbPositionMasterComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -1495762275:  // jmsConnector
          ((DbPositionMasterComponentFactory) bean).setJmsConnector((JmsConnector) newValue);
          return;
        case -758086398:  // jmsChangeManagerTopic
          ((DbPositionMasterComponentFactory) bean).setJmsChangeManagerTopic((String) newValue);
          return;
        case -1737146991:  // uniqueIdScheme
          ((DbPositionMasterComponentFactory) bean).setUniqueIdScheme((String) newValue);
          return;
        case -2022653118:  // maxRetries
          ((DbPositionMasterComponentFactory) bean).setMaxRetries((Integer) newValue);
          return;
        case -769924994:  // maxGetRequestSize
          ((DbPositionMasterComponentFactory) bean).setMaxGetRequestSize((Integer) newValue);
          return;
        case 2100076388:  // maxSearchRequestSize
          ((DbPositionMasterComponentFactory) bean).setMaxSearchRequestSize((Integer) newValue);
          return;
        case -337894953:  // parallelSearchQueries
          ((DbPositionMasterComponentFactory) bean).setParallelSearchQueries((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
