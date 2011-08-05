/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Link;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
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
public class SecurityLink extends Link<Security> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityLink.class);

  /**
   * Obtains an instance from a security, locking by strong object identifier
   * if possible and the external identifier bundle if not.
   * The result will contain the resolved target and one type of reference.
   * 
   * @param security  the security to store, not null
   * @return the link with target and object identifier set, not null
   */
  public static SecurityLink of(Security security) {
    ArgumentChecker.notNull(security, "security");
    SecurityLink link = new SecurityLink();
    link.setAndLockTarget(security);
    if (link.getObjectId() == null) {
      link.setBundleId(security.getIdentifiers());
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
  public static SecurityLink ofBundleId(Security security) {
    ArgumentChecker.notNull(security, "security");
    SecurityLink link = new SecurityLink(security.getIdentifiers());
    link.setTarget(security);
    return link;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an new instance.
   */
  public SecurityLink() {
    super();
  }

  /**
   * Creates a link from an object identifier.
   * 
   * @param objectId  the object identifier, not null
   */
  public SecurityLink(final ObjectId objectId) {
    super(objectId);
  }

  /**
   * Creates a link from a unique identifier, only storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   */
  public SecurityLink(final UniqueId uniqueId) {
    super(uniqueId);
  }

  /**
   * Creates a link from an external identifier.
   * 
   * @param identifier  the identifier, not null
   */
  public SecurityLink(final ExternalId identifier) {
    super(ExternalIdBundle.of(identifier));
  }

  /**
   * Creates a link from an external identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  public SecurityLink(final ExternalIdBundle bundle) {
    super(bundle);
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
    ExternalIdBundle bundle = getBundleId();
    if (security != null) {
      bundle = security.getIdentifiers();
    }
    if (bundle != null && bundle.size() > 0) {
      if (bundle.getValue(SecurityUtils.BLOOMBERG_TICKER) != null) {
        return bundle.getValue(SecurityUtils.BLOOMBERG_TICKER);
      } else if (bundle.getValue(SecurityUtils.RIC) != null) {
        return bundle.getValue(SecurityUtils.RIC);
      } else if (bundle.getValue(SecurityUtils.ACTIVFEED_TICKER) != null) {
        return bundle.getValue(SecurityUtils.ACTIVFEED_TICKER);
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
   * Resolves the security using a security source.
   * 
   * @param source  the source to use to resolve, not null
   * @return the resolved security, not null
   * @throws DataNotFoundException if the security could not be resolved
   * @throws RuntimeException if an error occurs while resolving
   */
  public Security resolve(SecuritySource source) {
    Security target = getTarget();
    if (target != null) {
      return target;
    }
    ObjectId objectId = getObjectId();
    if (objectId != null) {
      target = source.getSecurity(objectId.atLatestVersion());
      if (target != null) {
        setTarget(target);
        return target;
      }
    }
    ExternalIdBundle bundle = getBundleId();
    if (bundle.size() > 0) {
      target = source.getSecurity(bundle);
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
  public SecurityLink clone() {
    return (SecurityLink) super.clone();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SecurityLink}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static SecurityLink.Meta meta() {
    return SecurityLink.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(SecurityLink.Meta.INSTANCE);
  }

  @Override
  public SecurityLink.Meta metaBean() {
    return SecurityLink.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    super.propertySet(propertyName, newValue, quiet);
  }

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

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SecurityLink}.
   */
  public static class Meta extends Link.Meta<Security> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends SecurityLink> builder() {
      return new DirectBeanBuilder<SecurityLink>(new SecurityLink());
    }

    @Override
    public Class<? extends SecurityLink> beanType() {
      return SecurityLink.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
