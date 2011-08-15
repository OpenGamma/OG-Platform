/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.LinkResolver;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecurityLinkUtils;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A flexible link between an object and a security.
 * <p>
 * A security link represents a connection from an entity to a security.
 * The connection can be held by an {@code ObjectId} or an {@code ExternalIdBundle}.
 * <p>
 * This class is mutable and not thread-safe.
 * It is intended to be used in the engine via the read-only {@code SecurityLink} interface.
 */
@BeanDefinition
public class SimpleSecurityLink extends DirectBean implements SecurityLink {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SimpleSecurityLink.class);

  /**
   * The object identifier that strongly references the target.
   */
  @PropertyDefinition
  private ObjectId _objectId;
  /**
   * The external identifier bundle that references the target.
   * An empty bundle is used if not referencing a target by external bundle.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalIdBundle _externalId = ExternalIdBundle.EMPTY;
  /**
   * The target security.
   */
  @PropertyDefinition
  private Security _target;
  // TODO: remove

  /**
   * Obtains an instance from a security, storing the object identifier
   * if possible and the external identifier bundle if not.
   * 
   * @param security  the security to store, not null
   * @return the link with one identifier set, not null
   */
  public static SimpleSecurityLink of(Security security) {
    ArgumentChecker.notNull(security, "security");
    SimpleSecurityLink link = new SimpleSecurityLink();
    if (security.getUniqueId() != null) {
      link.setObjectId(security.getUniqueId().getObjectId());
    } else {
      link.setExternalId(security.getIdentifiers());
    }
    link.setTarget(security);
    return link;
  }

  /**
   * Obtains an instance from a security, storing the external identifier bundle.
   * 
   * @param security  the security to store, not null
   * @return the link with identifier bundle set, not null
   */
  public static SimpleSecurityLink ofBundleId(Security security) {
    ArgumentChecker.notNull(security, "security");
    SimpleSecurityLink link = new SimpleSecurityLink(security.getIdentifiers());
    link.setExternalId(security.getIdentifiers());
    link.setTarget(security);
    return link;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an new instance.
   */
  public SimpleSecurityLink() {
    super();
  }

  /**
   * Creates a link from an object identifier.
   * 
   * @param objectId  the object identifier, not null
   */
  public SimpleSecurityLink(final ObjectId objectId) {
    setObjectId(objectId);
  }

  /**
   * Creates a link from a unique identifier, only storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   */
  public SimpleSecurityLink(final UniqueId uniqueId) {
    setObjectId(uniqueId.getObjectId());
  }

  /**
   * Creates a link from an external identifier.
   * 
   * @param identifier  the identifier, not null
   */
  public SimpleSecurityLink(final ExternalId identifier) {
    setExternalId(ExternalIdBundle.of(identifier));
  }

  /**
   * Creates a link from an external identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  public SimpleSecurityLink(final ExternalIdBundle bundle) {
    setExternalId(bundle);
  }

  /**
   * Clones the specified link, sharing the target security.
   * 
   * @param linkToClone  the link to clone, not null
   */
  public SimpleSecurityLink(SecurityLink linkToClone) {
    _objectId = linkToClone.getObjectId();
    _externalId = linkToClone.getExternalId();
    _target = linkToClone.getTarget();
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an external identifier to the bundle.
   * 
   * @param externalId  the identifier to add, not null
   */
  public void addExternalId(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    setExternalId(getExternalId().withExternalId(externalId));
  }

  /**
   * Adds external identifiers to the bundle.
   * 
   * @param externalIds  the identifiers to add, not null
   */
  public void addExternalIds(final Iterable<ExternalId> externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    setExternalId(getExternalId().withExternalIds(externalIds));
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the best available representation.
   * 
   * @return the best available representation, not null
   */
  public Object getBest() {
    return SecurityLinkUtils.best(this);
  }

  /**
   * Gets the best descriptive name.
   * 
   * @return the best descriptive name, not null
   */
  public String getBestName() {
    return SecurityLinkUtils.bestName(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the link to the target security.
   * 
   * @param resolver  the resolver capable of finding the target, not null
   * @return the resolved target, null if unable to resolve
   * @throws DataNotFoundException if the target could not be resolved
   * @throws RuntimeException if an error occurs
   */
  public Security resolve(LinkResolver<Security> resolver) {
    return resolver.resolve(this);
  }

  /**
   * Resolves the security for the latest version-correction using a security source.
   * 
   * @param source  the source to use to resolve, not null
   * @return the resolved security, not null
   * @throws DataNotFoundException if the security could not be resolved
   * @throws RuntimeException if an error occurs while resolving
   */
  public Security resolve(SecuritySource source) {
    return resolve(source, VersionCorrection.LATEST);
  }

  /**
   * Resolves the security using a security source.
   * 
   * @param source  the source to use to resolve, not null
   * @param versionCorrection  the version-correction, not null
   * @return the resolved security, not null
   * @throws DataNotFoundException if the security could not be resolved
   * @throws RuntimeException if an error occurs while resolving
   */
  public Security resolve(SecuritySource source, VersionCorrection versionCorrection) {
    ObjectId objectId = getObjectId();
    if (objectId != null) {
      Security target = source.getSecurity(objectId, versionCorrection);
      if (target != null) {
        setTarget(target);
        return target;
      }
    }
    ExternalIdBundle bundle = getExternalId();
    if (bundle.size() > 0) {
      Security target = source.getSecurity(bundle, versionCorrection);
      if (target != null) {
        setTarget(target);
        return target;
      }
    }
    throw new DataNotFoundException("Unable to resolve security: " + getBestName());
  }

  /**
   * Resolves the security using a security source,
   * logging any exception and returning null.
   * 
   * @param source  the source to use to resolve, not null
   * @return the resolved security, null if unable to resolve
   */
  public Security resolveQuiet(SecuritySource source) {
    try {
      return resolve(source);
    } catch (DataNotFoundException ex) {
      s_logger.warn("Unable to resolve security {}", this);
      return null;
    } catch (RuntimeException ex) {
      s_logger.warn("Unable to resolve security {}: {}", this, ex);
      return null;
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimpleSecurityLink}.
   * @return the meta-bean, not null
   */
  public static SimpleSecurityLink.Meta meta() {
    return SimpleSecurityLink.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(SimpleSecurityLink.Meta.INSTANCE);
  }

  @Override
  public SimpleSecurityLink.Meta metaBean() {
    return SimpleSecurityLink.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 90495162:  // objectId
        return getObjectId();
      case -1699764666:  // externalId
        return getExternalId();
      case -880905839:  // target
        return getTarget();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 90495162:  // objectId
        setObjectId((ObjectId) newValue);
        return;
      case -1699764666:  // externalId
        setExternalId((ExternalIdBundle) newValue);
        return;
      case -880905839:  // target
        setTarget((Security) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_externalId, "externalId");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimpleSecurityLink other = (SimpleSecurityLink) obj;
      return JodaBeanUtils.equal(getObjectId(), other.getObjectId()) &&
          JodaBeanUtils.equal(getExternalId(), other.getExternalId()) &&
          JodaBeanUtils.equal(getTarget(), other.getTarget());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTarget());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the object identifier that strongly references the target.
   * @return the value of the property
   */
  public ObjectId getObjectId() {
    return _objectId;
  }

  /**
   * Sets the object identifier that strongly references the target.
   * @param objectId  the new value of the property
   */
  public void setObjectId(ObjectId objectId) {
    this._objectId = objectId;
  }

  /**
   * Gets the the {@code objectId} property.
   * @return the property, not null
   */
  public final Property<ObjectId> objectId() {
    return metaBean().objectId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier bundle that references the target.
   * An empty bundle is used if not referencing a target by external bundle.
   * @return the value of the property, not null
   */
  public ExternalIdBundle getExternalId() {
    return _externalId;
  }

  /**
   * Sets the external identifier bundle that references the target.
   * An empty bundle is used if not referencing a target by external bundle.
   * @param externalId  the new value of the property, not null
   */
  public void setExternalId(ExternalIdBundle externalId) {
    JodaBeanUtils.notNull(externalId, "externalId");
    this._externalId = externalId;
  }

  /**
   * Gets the the {@code externalId} property.
   * An empty bundle is used if not referencing a target by external bundle.
   * @return the property, not null
   */
  public final Property<ExternalIdBundle> externalId() {
    return metaBean().externalId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the target security.
   * @return the value of the property
   */
  public Security getTarget() {
    return _target;
  }

  /**
   * Sets the target security.
   * @param target  the new value of the property
   */
  public void setTarget(Security target) {
    this._target = target;
  }

  /**
   * Gets the the {@code target} property.
   * @return the property, not null
   */
  public final Property<Security> target() {
    return metaBean().target().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SimpleSecurityLink}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code objectId} property.
     */
    private final MetaProperty<ObjectId> _objectId = DirectMetaProperty.ofReadWrite(
        this, "objectId", SimpleSecurityLink.class, ObjectId.class);
    /**
     * The meta-property for the {@code externalId} property.
     */
    private final MetaProperty<ExternalIdBundle> _externalId = DirectMetaProperty.ofReadWrite(
        this, "externalId", SimpleSecurityLink.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code target} property.
     */
    private final MetaProperty<Security> _target = DirectMetaProperty.ofReadWrite(
        this, "target", SimpleSecurityLink.class, Security.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "objectId",
        "externalId",
        "target");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 90495162:  // objectId
          return _objectId;
        case -1699764666:  // externalId
          return _externalId;
        case -880905839:  // target
          return _target;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimpleSecurityLink> builder() {
      return new DirectBeanBuilder<SimpleSecurityLink>(new SimpleSecurityLink());
    }

    @Override
    public Class<? extends SimpleSecurityLink> beanType() {
      return SimpleSecurityLink.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code objectId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ObjectId> objectId() {
      return _objectId;
    }

    /**
     * The meta-property for the {@code externalId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBundle> externalId() {
      return _externalId;
    }

    /**
     * The meta-property for the {@code target} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Security> target() {
      return _target;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
