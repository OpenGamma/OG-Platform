/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
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

import com.google.common.base.Supplier;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.engine.calcnode.CalcNodeSocketConfiguration;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.DataConfigurationResource;

/**
 * Component factory providing a managed sub set of the server capabilities.
 */
@BeanDefinition
public class EngineConfigurationComponentFactory extends AbstractComponentFactory {

  /**
   * The name of the configuration document published.
   * <p>
   * This is used to support servers which publish multiple configurations, for example
   * if they host multiple view processors, or that act as aggregators for a number of
   * other servers at the installation site.
   * <p>
   * This default name may be hard-coded in native code and installation scripts.
   * Changes may cause client tools such as Excel to stop working correctly.
   */
  private static final String DEFAULT_CONFIGURATION_DOCUMENT_ID = "0";

  /**
   * The field name under which the logical server unique identifier is published.
   * <p>
   * This property may be set explicitly by calling {@link #setLogicalServerId},
   * or if omitted will be generated randomly.
   * <p>
   * This default name is hard-coded in native code. Changes may cause client tools
   * such as Excel to stop working correctly.
   */
  private static final String LOGICAL_SERVER_UNIQUE_IDENTIFIER = "lsid";

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;

  /**
   * The Fudge context.
   */
  @PropertyDefinition(validate = "notNull")
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  /**
   * The logical server unique identifier. This is defined by the data environment.
   * Clustered servers (that is, they appear suitably identical to any connecting clients)
   * should have the same logical identifier to reflect this. Any server backed by a
   * unique data environment must have a correspondingly unique identifier.
   * If a server has a transient or temporary data environment it must
   * generate a new logical identifier whenever that environment is flushed.
   * <p>
   * The default behavior, if this is not specified in the configuration file,
   * is to generate a unique identifier at start up. This is suitable for most
   * standard installations which include temporary
   * (for example, in-memory) masters or other data stores.
   */
  @PropertyDefinition
  private String _logicalServerId;

  /**
   * Creates a random logical server unique identifier.
   * This is used if an explicit identifier is not set in the configuration file.
   * <p>
   * This is a 24 character string using base-64 characters, created using
   * the algorithm from {@link GUIDGenerator} for uniqueness.
   * 
   * @return the logical server unique identifier, not null
   */
  protected String createLogicalServerId() {
    final UUID uuid = GUIDGenerator.generate();
    final byte[] bytes = new byte[16];
    long x = uuid.getMostSignificantBits();
    bytes[0] = (byte) x;
    bytes[1] = (byte) (x >> 8);
    bytes[2] = (byte) (x >> 16);
    bytes[3] = (byte) (x >> 24);
    bytes[4] = (byte) (x >> 32);
    bytes[5] = (byte) (x >> 40);
    bytes[6] = (byte) (x >> 48);
    bytes[7] = (byte) (x >> 56);
    x = uuid.getLeastSignificantBits();
    bytes[8] = (byte) x;
    bytes[9] = (byte) (x >> 8);
    bytes[10] = (byte) (x >> 16);
    bytes[11] = (byte) (x >> 24);
    bytes[12] = (byte) (x >> 32);
    bytes[13] = (byte) (x >> 40);
    bytes[14] = (byte) (x >> 48);
    bytes[15] = (byte) (x >> 56);
    return Base64.encodeBase64String(bytes);
  }

  protected void afterPropertiesSet() {
    if (getLogicalServerId() == null) {
      setLogicalServerId(createLogicalServerId());
    }
  }

  protected void buildConfiguration(ComponentRepository repo, Map<String, String> configuration, Map<String, Object> map) {
    map.put(LOGICAL_SERVER_UNIQUE_IDENTIFIER, getLogicalServerId());
    for (String key : configuration.keySet()) {
      String valueStr = configuration.get(key);
      Object targetValue = valueStr;
      if (valueStr.contains("::")) {
        ComponentInfo info = repo.findInfo(valueStr);
        if (info == null) {
          throw new IllegalArgumentException("Component not found: " + valueStr);
        }
        Object instance = repo.getInstance(info);
        if ((instance instanceof CalcNodeSocketConfiguration) || (instance instanceof Supplier)) {
          targetValue = instance;
        } else {
          if (info.getUri() == null) {
            throw new OpenGammaRuntimeException("Unable to add component to configuration as it has not been published by REST: " + valueStr);
          }
          targetValue = new UriEndPointDescriptionProvider(info.getUri().toString());
        }
      }
      buildMap(map, key, targetValue);
    }
  }

  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    afterPropertiesSet();
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    buildConfiguration(repo, configuration, map);
    Map<String, Object> outer = new LinkedHashMap<String, Object>();
    outer.put(DEFAULT_CONFIGURATION_DOCUMENT_ID, map);
    DataConfigurationResource resource = new DataConfigurationResource(getFudgeContext(), outer);
    repo.getRestComponents().publishResource(resource);
    // indicate that all component configuration was used
    configuration.clear();
  }

  /**
   * Builds the map, handling dot separate keys.
   * 
   * @param map the map, not null
   * @param key the key, not null
   * @param targetValue the target value,not null
   */
  protected void buildMap(Map<String, Object> map, String key, Object targetValue) {
    if (key.contains(".")) {
      String key1 = StringUtils.substringBefore(key, ".");
      String key2 = StringUtils.substringAfter(key, ".");
      @SuppressWarnings("unchecked")
      Map<String, Object> subMap = (Map<String, Object>) map.get(key1);
      if (subMap == null) {
        subMap = new LinkedHashMap<String, Object>();
        map.put(key1, subMap);
      }
      buildMap(subMap, key2, targetValue);
    } else {
      map.put(key, targetValue);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code EngineConfigurationComponentFactory}.
   * @return the meta-bean, not null
   */
  public static EngineConfigurationComponentFactory.Meta meta() {
    return EngineConfigurationComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(EngineConfigurationComponentFactory.Meta.INSTANCE);
  }

  @Override
  public EngineConfigurationComponentFactory.Meta metaBean() {
    return EngineConfigurationComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
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
   * Gets the logical server unique identifier. This is defined by the data environment.
   * Clustered servers (that is, they appear suitably identical to any connecting clients)
   * should have the same logical identifier to reflect this. Any server backed by a
   * unique data environment must have a correspondingly unique identifier.
   * If a server has a transient or temporary data environment it must
   * generate a new logical identifier whenever that environment is flushed.
   * <p>
   * The default behavior, if this is not specified in the configuration file,
   * is to generate a unique identifier at start up. This is suitable for most
   * standard installations which include temporary
   * (for example, in-memory) masters or other data stores.
   * @return the value of the property
   */
  public String getLogicalServerId() {
    return _logicalServerId;
  }

  /**
   * Sets the logical server unique identifier. This is defined by the data environment.
   * Clustered servers (that is, they appear suitably identical to any connecting clients)
   * should have the same logical identifier to reflect this. Any server backed by a
   * unique data environment must have a correspondingly unique identifier.
   * If a server has a transient or temporary data environment it must
   * generate a new logical identifier whenever that environment is flushed.
   * <p>
   * The default behavior, if this is not specified in the configuration file,
   * is to generate a unique identifier at start up. This is suitable for most
   * standard installations which include temporary
   * (for example, in-memory) masters or other data stores.
   * @param logicalServerId  the new value of the property
   */
  public void setLogicalServerId(String logicalServerId) {
    this._logicalServerId = logicalServerId;
  }

  /**
   * Gets the the {@code logicalServerId} property.
   * Clustered servers (that is, they appear suitably identical to any connecting clients)
   * should have the same logical identifier to reflect this. Any server backed by a
   * unique data environment must have a correspondingly unique identifier.
   * If a server has a transient or temporary data environment it must
   * generate a new logical identifier whenever that environment is flushed.
   * <p>
   * The default behavior, if this is not specified in the configuration file,
   * is to generate a unique identifier at start up. This is suitable for most
   * standard installations which include temporary
   * (for example, in-memory) masters or other data stores.
   * @return the property, not null
   */
  public final Property<String> logicalServerId() {
    return metaBean().logicalServerId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public EngineConfigurationComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      EngineConfigurationComponentFactory other = (EngineConfigurationComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getFudgeContext(), other.getFudgeContext()) &&
          JodaBeanUtils.equal(getLogicalServerId(), other.getLogicalServerId()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFudgeContext());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLogicalServerId());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("EngineConfigurationComponentFactory{");
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
    buf.append("fudgeContext").append('=').append(JodaBeanUtils.toString(getFudgeContext())).append(',').append(' ');
    buf.append("logicalServerId").append('=').append(JodaBeanUtils.toString(getLogicalServerId())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code EngineConfigurationComponentFactory}.
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
        this, "classifier", EngineConfigurationComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code fudgeContext} property.
     */
    private final MetaProperty<FudgeContext> _fudgeContext = DirectMetaProperty.ofReadWrite(
        this, "fudgeContext", EngineConfigurationComponentFactory.class, FudgeContext.class);
    /**
     * The meta-property for the {@code logicalServerId} property.
     */
    private final MetaProperty<String> _logicalServerId = DirectMetaProperty.ofReadWrite(
        this, "logicalServerId", EngineConfigurationComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "fudgeContext",
        "logicalServerId");

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
        case -917704420:  // fudgeContext
          return _fudgeContext;
        case -41854233:  // logicalServerId
          return _logicalServerId;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends EngineConfigurationComponentFactory> builder() {
      return new DirectBeanBuilder<EngineConfigurationComponentFactory>(new EngineConfigurationComponentFactory());
    }

    @Override
    public Class<? extends EngineConfigurationComponentFactory> beanType() {
      return EngineConfigurationComponentFactory.class;
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
     * The meta-property for the {@code fudgeContext} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FudgeContext> fudgeContext() {
      return _fudgeContext;
    }

    /**
     * The meta-property for the {@code logicalServerId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> logicalServerId() {
      return _logicalServerId;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((EngineConfigurationComponentFactory) bean).getClassifier();
        case -917704420:  // fudgeContext
          return ((EngineConfigurationComponentFactory) bean).getFudgeContext();
        case -41854233:  // logicalServerId
          return ((EngineConfigurationComponentFactory) bean).getLogicalServerId();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((EngineConfigurationComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -917704420:  // fudgeContext
          ((EngineConfigurationComponentFactory) bean).setFudgeContext((FudgeContext) newValue);
          return;
        case -41854233:  // logicalServerId
          ((EngineConfigurationComponentFactory) bean).setLogicalServerId((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((EngineConfigurationComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((EngineConfigurationComponentFactory) bean)._fudgeContext, "fudgeContext");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
