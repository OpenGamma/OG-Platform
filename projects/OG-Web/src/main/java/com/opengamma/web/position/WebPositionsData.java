/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.util.Map;

import javax.ws.rs.core.UriInfo;

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

import com.google.common.collect.Maps;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.web.WebPerRequestData;

/**
 * Data class for web-based positions.
 */
@BeanDefinition
public class WebPositionsData extends WebPerRequestData {
  /**
   * The position master.
   */
  @PropertyDefinition
  private PositionMaster _positionMaster;
  /**
   * The security loader.
   */
  @PropertyDefinition
  private SecurityLoader _securityLoader;
  /**
   * The security source
   */
  @PropertyDefinition
  private SecuritySource _securitySource;
  /**
   * The time-series source.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * The position id from the input URI.
   */
  @PropertyDefinition
  private String _uriPositionId;
  /**
   * The version id from the URI.
   */
  @PropertyDefinition
  private String _uriVersionId;
  /**
   * The position.
   */
  @PropertyDefinition
  private PositionDocument _position;
  /**
   * The versioned position.
   */
  @PropertyDefinition
  private PositionDocument _versioned;
  /**
   * The external schemes.
   */
  @PropertyDefinition
  private final Map<ExternalScheme, String> _externalSchemes = Maps.newHashMap();

  /**
   * Creates an instance.
   */
  public WebPositionsData() {
  }

  /**
   * Creates an instance.
   * @param uriInfo  the URI information
   */
  public WebPositionsData(final UriInfo uriInfo) {
    setUriInfo(uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the best available position id.
   * @param overrideId  the override id, null derives the result from the data
   * @return the id, may be null
   */
  public String getBestPositionUriId(final UniqueId overrideId) {
    if (overrideId != null) {
      return overrideId.toLatest().toString();
    }
    return getPosition() != null ? getPosition().getUniqueId().toLatest().toString() : getUriPositionId();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code WebPositionsData}.
   * @return the meta-bean, not null
   */
  public static WebPositionsData.Meta meta() {
    return WebPositionsData.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(WebPositionsData.Meta.INSTANCE);
  }

  @Override
  public WebPositionsData.Meta metaBean() {
    return WebPositionsData.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the value of the property
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the position master.
   * @param positionMaster  the new value of the property
   */
  public void setPositionMaster(PositionMaster positionMaster) {
    this._positionMaster = positionMaster;
  }

  /**
   * Gets the the {@code positionMaster} property.
   * @return the property, not null
   */
  public final Property<PositionMaster> positionMaster() {
    return metaBean().positionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security loader.
   * @return the value of the property
   */
  public SecurityLoader getSecurityLoader() {
    return _securityLoader;
  }

  /**
   * Sets the security loader.
   * @param securityLoader  the new value of the property
   */
  public void setSecurityLoader(SecurityLoader securityLoader) {
    this._securityLoader = securityLoader;
  }

  /**
   * Gets the the {@code securityLoader} property.
   * @return the property, not null
   */
  public final Property<SecurityLoader> securityLoader() {
    return metaBean().securityLoader().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security source
   * @return the value of the property
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Sets the security source
   * @param securitySource  the new value of the property
   */
  public void setSecuritySource(SecuritySource securitySource) {
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
   * Gets the time-series source.
   * @return the value of the property
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  /**
   * Sets the time-series source.
   * @param historicalTimeSeriesSource  the new value of the property
   */
  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
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
   * Gets the position id from the input URI.
   * @return the value of the property
   */
  public String getUriPositionId() {
    return _uriPositionId;
  }

  /**
   * Sets the position id from the input URI.
   * @param uriPositionId  the new value of the property
   */
  public void setUriPositionId(String uriPositionId) {
    this._uriPositionId = uriPositionId;
  }

  /**
   * Gets the the {@code uriPositionId} property.
   * @return the property, not null
   */
  public final Property<String> uriPositionId() {
    return metaBean().uriPositionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the version id from the URI.
   * @return the value of the property
   */
  public String getUriVersionId() {
    return _uriVersionId;
  }

  /**
   * Sets the version id from the URI.
   * @param uriVersionId  the new value of the property
   */
  public void setUriVersionId(String uriVersionId) {
    this._uriVersionId = uriVersionId;
  }

  /**
   * Gets the the {@code uriVersionId} property.
   * @return the property, not null
   */
  public final Property<String> uriVersionId() {
    return metaBean().uriVersionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position.
   * @return the value of the property
   */
  public PositionDocument getPosition() {
    return _position;
  }

  /**
   * Sets the position.
   * @param position  the new value of the property
   */
  public void setPosition(PositionDocument position) {
    this._position = position;
  }

  /**
   * Gets the the {@code position} property.
   * @return the property, not null
   */
  public final Property<PositionDocument> position() {
    return metaBean().position().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the versioned position.
   * @return the value of the property
   */
  public PositionDocument getVersioned() {
    return _versioned;
  }

  /**
   * Sets the versioned position.
   * @param versioned  the new value of the property
   */
  public void setVersioned(PositionDocument versioned) {
    this._versioned = versioned;
  }

  /**
   * Gets the the {@code versioned} property.
   * @return the property, not null
   */
  public final Property<PositionDocument> versioned() {
    return metaBean().versioned().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external schemes.
   * @return the value of the property, not null
   */
  public Map<ExternalScheme, String> getExternalSchemes() {
    return _externalSchemes;
  }

  /**
   * Sets the external schemes.
   * @param externalSchemes  the new value of the property, not null
   */
  public void setExternalSchemes(Map<ExternalScheme, String> externalSchemes) {
    JodaBeanUtils.notNull(externalSchemes, "externalSchemes");
    this._externalSchemes.clear();
    this._externalSchemes.putAll(externalSchemes);
  }

  /**
   * Gets the the {@code externalSchemes} property.
   * @return the property, not null
   */
  public final Property<Map<ExternalScheme, String>> externalSchemes() {
    return metaBean().externalSchemes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public WebPositionsData clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      WebPositionsData other = (WebPositionsData) obj;
      return JodaBeanUtils.equal(getPositionMaster(), other.getPositionMaster()) &&
          JodaBeanUtils.equal(getSecurityLoader(), other.getSecurityLoader()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          JodaBeanUtils.equal(getUriPositionId(), other.getUriPositionId()) &&
          JodaBeanUtils.equal(getUriVersionId(), other.getUriVersionId()) &&
          JodaBeanUtils.equal(getPosition(), other.getPosition()) &&
          JodaBeanUtils.equal(getVersioned(), other.getVersioned()) &&
          JodaBeanUtils.equal(getExternalSchemes(), other.getExternalSchemes()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityLoader());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUriPositionId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUriVersionId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPosition());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVersioned());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalSchemes());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("WebPositionsData{");
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
    buf.append("positionMaster").append('=').append(JodaBeanUtils.toString(getPositionMaster())).append(',').append(' ');
    buf.append("securityLoader").append('=').append(JodaBeanUtils.toString(getSecurityLoader())).append(',').append(' ');
    buf.append("securitySource").append('=').append(JodaBeanUtils.toString(getSecuritySource())).append(',').append(' ');
    buf.append("historicalTimeSeriesSource").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesSource())).append(',').append(' ');
    buf.append("uriPositionId").append('=').append(JodaBeanUtils.toString(getUriPositionId())).append(',').append(' ');
    buf.append("uriVersionId").append('=').append(JodaBeanUtils.toString(getUriVersionId())).append(',').append(' ');
    buf.append("position").append('=').append(JodaBeanUtils.toString(getPosition())).append(',').append(' ');
    buf.append("versioned").append('=').append(JodaBeanUtils.toString(getVersioned())).append(',').append(' ');
    buf.append("externalSchemes").append('=').append(JodaBeanUtils.toString(getExternalSchemes())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code WebPositionsData}.
   */
  public static class Meta extends WebPerRequestData.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code positionMaster} property.
     */
    private final MetaProperty<PositionMaster> _positionMaster = DirectMetaProperty.ofReadWrite(
        this, "positionMaster", WebPositionsData.class, PositionMaster.class);
    /**
     * The meta-property for the {@code securityLoader} property.
     */
    private final MetaProperty<SecurityLoader> _securityLoader = DirectMetaProperty.ofReadWrite(
        this, "securityLoader", WebPositionsData.class, SecurityLoader.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", WebPositionsData.class, SecuritySource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _historicalTimeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesSource", WebPositionsData.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code uriPositionId} property.
     */
    private final MetaProperty<String> _uriPositionId = DirectMetaProperty.ofReadWrite(
        this, "uriPositionId", WebPositionsData.class, String.class);
    /**
     * The meta-property for the {@code uriVersionId} property.
     */
    private final MetaProperty<String> _uriVersionId = DirectMetaProperty.ofReadWrite(
        this, "uriVersionId", WebPositionsData.class, String.class);
    /**
     * The meta-property for the {@code position} property.
     */
    private final MetaProperty<PositionDocument> _position = DirectMetaProperty.ofReadWrite(
        this, "position", WebPositionsData.class, PositionDocument.class);
    /**
     * The meta-property for the {@code versioned} property.
     */
    private final MetaProperty<PositionDocument> _versioned = DirectMetaProperty.ofReadWrite(
        this, "versioned", WebPositionsData.class, PositionDocument.class);
    /**
     * The meta-property for the {@code externalSchemes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<ExternalScheme, String>> _externalSchemes = DirectMetaProperty.ofReadWrite(
        this, "externalSchemes", WebPositionsData.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "positionMaster",
        "securityLoader",
        "securitySource",
        "historicalTimeSeriesSource",
        "uriPositionId",
        "uriVersionId",
        "position",
        "versioned",
        "externalSchemes");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1840419605:  // positionMaster
          return _positionMaster;
        case -903470221:  // securityLoader
          return _securityLoader;
        case -702456965:  // securitySource
          return _securitySource;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
        case 1240319664:  // uriPositionId
          return _uriPositionId;
        case 666567687:  // uriVersionId
          return _uriVersionId;
        case 747804969:  // position
          return _position;
        case -1407102089:  // versioned
          return _versioned;
        case -1949439709:  // externalSchemes
          return _externalSchemes;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends WebPositionsData> builder() {
      return new DirectBeanBuilder<WebPositionsData>(new WebPositionsData());
    }

    @Override
    public Class<? extends WebPositionsData> beanType() {
      return WebPositionsData.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code positionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionMaster> positionMaster() {
      return _positionMaster;
    }

    /**
     * The meta-property for the {@code securityLoader} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityLoader> securityLoader() {
      return _securityLoader;
    }

    /**
     * The meta-property for the {@code securitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecuritySource> securitySource() {
      return _securitySource;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
      return _historicalTimeSeriesSource;
    }

    /**
     * The meta-property for the {@code uriPositionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uriPositionId() {
      return _uriPositionId;
    }

    /**
     * The meta-property for the {@code uriVersionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uriVersionId() {
      return _uriVersionId;
    }

    /**
     * The meta-property for the {@code position} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionDocument> position() {
      return _position;
    }

    /**
     * The meta-property for the {@code versioned} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionDocument> versioned() {
      return _versioned;
    }

    /**
     * The meta-property for the {@code externalSchemes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<ExternalScheme, String>> externalSchemes() {
      return _externalSchemes;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1840419605:  // positionMaster
          return ((WebPositionsData) bean).getPositionMaster();
        case -903470221:  // securityLoader
          return ((WebPositionsData) bean).getSecurityLoader();
        case -702456965:  // securitySource
          return ((WebPositionsData) bean).getSecuritySource();
        case 358729161:  // historicalTimeSeriesSource
          return ((WebPositionsData) bean).getHistoricalTimeSeriesSource();
        case 1240319664:  // uriPositionId
          return ((WebPositionsData) bean).getUriPositionId();
        case 666567687:  // uriVersionId
          return ((WebPositionsData) bean).getUriVersionId();
        case 747804969:  // position
          return ((WebPositionsData) bean).getPosition();
        case -1407102089:  // versioned
          return ((WebPositionsData) bean).getVersioned();
        case -1949439709:  // externalSchemes
          return ((WebPositionsData) bean).getExternalSchemes();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1840419605:  // positionMaster
          ((WebPositionsData) bean).setPositionMaster((PositionMaster) newValue);
          return;
        case -903470221:  // securityLoader
          ((WebPositionsData) bean).setSecurityLoader((SecurityLoader) newValue);
          return;
        case -702456965:  // securitySource
          ((WebPositionsData) bean).setSecuritySource((SecuritySource) newValue);
          return;
        case 358729161:  // historicalTimeSeriesSource
          ((WebPositionsData) bean).setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
          return;
        case 1240319664:  // uriPositionId
          ((WebPositionsData) bean).setUriPositionId((String) newValue);
          return;
        case 666567687:  // uriVersionId
          ((WebPositionsData) bean).setUriVersionId((String) newValue);
          return;
        case 747804969:  // position
          ((WebPositionsData) bean).setPosition((PositionDocument) newValue);
          return;
        case -1407102089:  // versioned
          ((WebPositionsData) bean).setVersioned((PositionDocument) newValue);
          return;
        case -1949439709:  // externalSchemes
          ((WebPositionsData) bean).setExternalSchemes((Map<ExternalScheme, String>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((WebPositionsData) bean)._externalSchemes, "externalSchemes");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
