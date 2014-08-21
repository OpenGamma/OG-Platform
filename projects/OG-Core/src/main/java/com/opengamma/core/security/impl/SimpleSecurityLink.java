/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.AbstractLink;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
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
public class SimpleSecurityLink extends AbstractLink<Security>
    implements SecurityLink {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SimpleSecurityLink.class);

  /**
   * The target security.
   */
  @PropertyDefinition(overrideGet = true)
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
      link.setExternalId(security.getExternalIdBundle());
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
    SimpleSecurityLink link = new SimpleSecurityLink(security.getExternalIdBundle());
    link.setExternalId(security.getExternalIdBundle());
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
   * @deprecated using object ids will limit ability to export links, use ExternalIds or ExternalIdBundles, even if they need to be generated GUIDs
   */
  @Deprecated
  public SimpleSecurityLink(final ObjectId objectId) {
    super(objectId);
  }

  /**
   * Creates a link from a unique identifier, only storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @deprecated using object ids will limit ability to export links, use ExternalIds or ExternalIdBundles, even if they need to be generated GUIDs
   */
  @Deprecated
  public SimpleSecurityLink(final UniqueId uniqueId) {
    super(uniqueId);
  }

  /**
   * Creates a link from an external identifier.
   * 
   * @param externalId  the external identifier, not null
   */
  public SimpleSecurityLink(final ExternalId externalId) {
    super(externalId);
  }

  /**
   * Creates a link from an external identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  public SimpleSecurityLink(final ExternalIdBundle bundle) {
    super(bundle);
  }

  /**
   * Clones the specified link, sharing the target security.
   * 
   * @param linkToClone  the link to clone, not null
   */
  public SimpleSecurityLink(SecurityLink linkToClone) {
    super();
    setObjectId(linkToClone.getObjectId());
    setExternalId(linkToClone.getExternalId());
    setTarget(linkToClone.getTarget());
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the security for the latest version-correction using a security source.
   * 
   * @param source  the source to use to resolve, not null
   * @return the resolved security, not null
   * @throws DataNotFoundException if the security could not be resolved
   * @throws RuntimeException if an error occurs while resolving
   */
  @Override
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
  @Override
  public Security resolve(SecuritySource source, VersionCorrection versionCorrection) {
    ObjectId objectId = getObjectId();
    if (objectId != null) {
      Security target = (Security) source.get(objectId, versionCorrection);
      setTarget(target);
      return target;
    }
    ExternalIdBundle bundle = getExternalId();
    if (bundle.size() > 0) {
      Security target = source.getSingle(bundle, versionCorrection);
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
  @Override
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

  //-----------------------------------------------------------------------
  /**
   * Gets the target security.
   * @return the value of the property
   */
  @Override
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
  @Override
  public SimpleSecurityLink clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimpleSecurityLink other = (SimpleSecurityLink) obj;
      return JodaBeanUtils.equal(getTarget(), other.getTarget()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getTarget());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("SimpleSecurityLink{");
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
    buf.append("target").append('=').append(JodaBeanUtils.toString(getTarget())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SimpleSecurityLink}.
   */
  public static class Meta extends AbstractLink.Meta<Security> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code target} property.
     */
    private final MetaProperty<Security> _target = DirectMetaProperty.ofReadWrite(
        this, "target", SimpleSecurityLink.class, Security.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "target");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
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
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code target} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Security> target() {
      return _target;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -880905839:  // target
          return ((SimpleSecurityLink) bean).getTarget();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -880905839:  // target
          ((SimpleSecurityLink) bean).setTarget((Security) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
