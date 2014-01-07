/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.LinkResolver;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractLink;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A flexible link between an object and a security.
 * <p>
 * The security link represents a connection from an entity to a security.
 * The connection can be held by an {@code ObjectId} or an {@code ExternalIdBundle}.
 * The link also holds a resolved reference to the security itself.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class ManageableSecurityLink extends AbstractLink<Security> implements SecurityLink {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ManageableSecurityLink.class);

  /**
   * Obtains an instance from a security, locking by strong object identifier
   * if possible and the external identifier bundle if not.
   * The result will contain the resolved target and one type of reference.
   * 
   * @param security  the security to store, not null
   * @return the link with target and object identifier set, not null
   */
  public static ManageableSecurityLink of(Security security) {
    ArgumentChecker.notNull(security, "security");
    ManageableSecurityLink link = new ManageableSecurityLink();
    link.setAndLockTarget(security);
    if (link.getObjectId() == null) {
      link.setExternalId(security.getExternalIdBundle());
    }
    return link;
  }

  /**
   * Obtains an instance from a security, locking by external identifier bundle.
   * The result will contain the external identifier bundle and the resolved target.
   * 
   * @param security  the security to store, not null
   * @return the link with target and identifier bundle set, not null
   */
  public static ManageableSecurityLink ofBundleId(Security security) {
    ArgumentChecker.notNull(security, "security");
    ManageableSecurityLink link = new ManageableSecurityLink(security.getExternalIdBundle());
    link.setTarget(security);
    return link;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an new instance.
   */
  public ManageableSecurityLink() {
    super();
  }

  /**
   * Creates a link from an object identifier.
   * 
   * @param objectId  the object identifier, not null
   * @deprecated using object ids will limit ability to export links, use ExternalIds or ExternalIdBundles, even if they need to be generated GUIDs
   */
  @Deprecated
  public ManageableSecurityLink(final ObjectId objectId) {
    super(objectId);
  }

  /**
   * Creates a link from a unique identifier, only storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @deprecated using object ids will limit ability to export links, use ExternalIds or ExternalIdBundles, even if they need to be generated GUIDs
   */
  @Deprecated
  public ManageableSecurityLink(final UniqueId uniqueId) {
    super(uniqueId);
  }

  /**
   * Creates a link from an external identifier.
   * 
   * @param identifier  the identifier, not null
   */
  public ManageableSecurityLink(final ExternalId identifier) {
    super(ExternalIdBundle.of(identifier));
  }

  /**
   * Creates a link from an external identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  public ManageableSecurityLink(final ExternalIdBundle bundle) {
    super(bundle);
  }

  /**
   * Clones the specified link, sharing the target security.
   * 
   * @param linkToClone  the link to clone, not null
   */
  public ManageableSecurityLink(SecurityLink linkToClone) {
    setObjectId(linkToClone.getObjectId());
    setExternalId(linkToClone.getExternalId());
    setTarget(linkToClone.getTarget());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the best descriptive name.
   * 
   * @return the best descriptive name, not null
   */
  public String getBestName() {
    Security security = getTarget();
    ObjectId objectId = getObjectId();
    ExternalIdBundle bundle = getExternalId();
    if (security != null) {
      // Try to retrieve the security's assigned name
      String name = security.getName();
      if (StringUtils.isNotBlank(name)) {
        return name;
      }      
      bundle = security.getExternalIdBundle();
    }
    if (bundle != null && bundle.size() > 0) {
      if (bundle.getValue(ExternalSchemes.BLOOMBERG_TICKER) != null) {
        return bundle.getValue(ExternalSchemes.BLOOMBERG_TICKER);
      } else if (bundle.getValue(ExternalSchemes.RIC) != null) {
        return bundle.getValue(ExternalSchemes.RIC);
      } else if (bundle.getValue(ExternalSchemes.ACTIVFEED_TICKER) != null) {
        return bundle.getValue(ExternalSchemes.ACTIVFEED_TICKER);
      } else {
        return bundle.getExternalIds().iterator().next().getValue();
      }
    }
    if (objectId != null) {
      return objectId.toString();
    }
    return "";
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
      Security target = source.get(objectId, versionCorrection);
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

  //-------------------------------------------------------------------------
  /**
   * Clones this link, sharing the target security.
   * 
   * @return the clone, not null
   */
  @Override
  public ManageableSecurityLink clone() {
    return (ManageableSecurityLink) super.clone();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableSecurityLink}.
   * @return the meta-bean, not null
   */
  public static ManageableSecurityLink.Meta meta() {
    return ManageableSecurityLink.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ManageableSecurityLink.Meta.INSTANCE);
  }

  @Override
  public ManageableSecurityLink.Meta metaBean() {
    return ManageableSecurityLink.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(32);
    buf.append("ManageableSecurityLink{");
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
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableSecurityLink}.
   */
  public static class Meta extends AbstractLink.Meta<Security> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends ManageableSecurityLink> builder() {
      return new DirectBeanBuilder<ManageableSecurityLink>(new ManageableSecurityLink());
    }

    @Override
    public Class<? extends ManageableSecurityLink> beanType() {
      return ManageableSecurityLink.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
