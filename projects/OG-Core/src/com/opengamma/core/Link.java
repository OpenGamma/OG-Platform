/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

import com.google.common.base.Objects;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A flexible link between two parts of the system.
 * <p>
 * A link represents a connection from one entity to another in the object model.
 * The connection can be held by an {@code ObjectId} or an {@code ExternalIdBundle}.
 * The link also stores a resolved reference to the object itself.
 * <p>
 * This class is mutable and not thread-safe.
 * 
 * @param <T> the type being linked to 
 */
@PublicAPI
@BeanDefinition
public class Link<T extends UniqueIdentifiable> extends DirectBean
    implements ObjectIdentifiable, Iterable<ExternalId>, Serializable, Cloneable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

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
  private ExternalIdBundle _bundleId = ExternalIdBundle.EMPTY;
  /**
   * The resolved target.
   */
  @PropertyDefinition
  private T _target;

  /**
   * Creates an new instance.
   */
  public Link() {
  }

  /**
   * Creates a link from a strong object identifier.
   * 
   * @param objectId  the object identifier, not null
   */
  public Link(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    setObjectId(objectId);
  }

  /**
   * Creates a link from a strong unique identifier, only
   * storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   */
  public Link(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    setObjectId(uniqueId.getObjectId());
  }

  /**
   * Creates a link from an external identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  public Link(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    setBundleId(bundle);
  }

  /**
   * Creates a link from the target object.
   * The strong reference is set using {@link #setAndLockTarget}.
   * 
   * @param target  the resolved target, not null
   */
  public Link(final T target) {
    ArgumentChecker.notNull(target, "target");
    setAndLockTarget(target);
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the resolved target, locking it for future resolution by setting the
   * object identifier and clearing the external identifier bundle.
   * If the target has no unique identifier, then no change is made to the references.
   * 
   * @param target  the target, not null
   */
  public void setAndLockTarget(T target) {
    ArgumentChecker.notNull(target, "target");
    setTarget(target);
    lockTarget();
  }

  /**
   * Locks the resolved target for future resolution by setting the
   * object identifier and clearing the external identifier bundle.
   * If the target has no unique identifier, then no change is made to the references.
   * <p>
   * The target of a link is effectively treated as transient state.
   * This method updates the object identifier based on the resolved target so
   * that true state of the link is up to date.
   */
  public void lockTarget() {
    T target = getTarget();
    if (target == null) {
      throw new IllegalStateException("Cannot lock a null target");
    }
    UniqueId uniqueId = target.getUniqueId();
    if (uniqueId != null) {
      setObjectId(uniqueId.getObjectId());
      setBundleId(ExternalIdBundle.EMPTY);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Clones the link, assigning the target.
   * 
   * @return the cloned link with assigned target, not null
   */
  @SuppressWarnings("unchecked")
  @Override
  public Link<T> clone() {
    try {
      return (Link<T>) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new OpenGammaRuntimeException("Clone failed", ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the best available representation.
   * 
   * @return the best available representation, not null
   */
  public Object getBest() {
    T target = getTarget();
    ObjectId objectId = getObjectId();
    ExternalIdBundle bundle = getBundleId();
    return Objects.firstNonNull(target, Objects.firstNonNull(objectId, bundle));
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an external identifier to the bundle.
   * 
   * @param externalId  the identifier to add, not null
   */
  public void addExternalId(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    setBundleId(getBundleId().withExternalId(externalId));
  }

  /**
   * Adds external identifiers to the bundle.
   * 
   * @param externalIds  the identifiers to add, not null
   */
  public void addExternalIds(final Iterable<ExternalId> externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    setBundleId(getBundleId().withExternalIds(externalIds));
  }

  //-------------------------------------------------------------------------
  /**
   * Iterates over the external reference identifiers.
   * 
   * @return the iterator, not null
   */
  @Override
  public Iterator<ExternalId> iterator() {
    return getBundleId().iterator();
  }

  /**
   * Gets the set of external reference identifiers.
   * This excludes the strong object identifier.
   * 
   * @return all the identifiers, not null
   */
  public Set<ExternalId> getExternalIds() {
    return getBundleId().getExternalIds();
  }

  /**
   * Gets a set of all identifiers, including the object identifier
   * expressed as an {@code ExternalId}.
   * 
   * @return all the identifiers, not null
   */
  public Set<ExternalId> getAllExternalIds() {
    Set<ExternalId> identifiers = getBundleId().getExternalIds();
    ObjectId objectId = getObjectId();
    if (objectId != null) {
      Set<ExternalId> set = new HashSet<ExternalId>(identifiers);
      set.add(ExternalId.of(ObjectId.EXTERNAL_SCHEME, objectId.toString()));
      return set;
    }
    return identifiers;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Link}.
   * @param <R>  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R extends UniqueIdentifiable> Link.Meta<R> meta() {
    return Link.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(Link.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Link.Meta<T> metaBean() {
    return Link.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 90495162:  // objectId
        return getObjectId();
      case -1294655171:  // bundleId
        return getBundleId();
      case -880905839:  // target
        return getTarget();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 90495162:  // objectId
        setObjectId((ObjectId) newValue);
        return;
      case -1294655171:  // bundleId
        setBundleId((ExternalIdBundle) newValue);
        return;
      case -880905839:  // target
        setTarget((T) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_bundleId, "bundleId");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      Link<?> other = (Link<?>) obj;
      return JodaBeanUtils.equal(getObjectId(), other.getObjectId()) &&
          JodaBeanUtils.equal(getBundleId(), other.getBundleId()) &&
          JodaBeanUtils.equal(getTarget(), other.getTarget());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBundleId());
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
  public ExternalIdBundle getBundleId() {
    return _bundleId;
  }

  /**
   * Sets the external identifier bundle that references the target.
   * An empty bundle is used if not referencing a target by external bundle.
   * @param bundleId  the new value of the property, not null
   */
  public void setBundleId(ExternalIdBundle bundleId) {
    JodaBeanUtils.notNull(bundleId, "bundleId");
    this._bundleId = bundleId;
  }

  /**
   * Gets the the {@code bundleId} property.
   * An empty bundle is used if not referencing a target by external bundle.
   * @return the property, not null
   */
  public final Property<ExternalIdBundle> bundleId() {
    return metaBean().bundleId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the resolved target.
   * @return the value of the property
   */
  public T getTarget() {
    return _target;
  }

  /**
   * Sets the resolved target.
   * @param target  the new value of the property
   */
  public void setTarget(T target) {
    this._target = target;
  }

  /**
   * Gets the the {@code target} property.
   * @return the property, not null
   */
  public final Property<T> target() {
    return metaBean().target().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Link}.
   */
  public static class Meta<T extends UniqueIdentifiable> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code objectId} property.
     */
    private final MetaProperty<ObjectId> _objectId = DirectMetaProperty.ofReadWrite(
        this, "objectId", Link.class, ObjectId.class);
    /**
     * The meta-property for the {@code bundleId} property.
     */
    private final MetaProperty<ExternalIdBundle> _bundleId = DirectMetaProperty.ofReadWrite(
        this, "bundleId", Link.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code target} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<T> _target = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
        this, "target", Link.class, Object.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "objectId",
        "bundleId",
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
        case -1294655171:  // bundleId
          return _bundleId;
        case -880905839:  // target
          return _target;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends Link<T>> builder() {
      return new DirectBeanBuilder<Link<T>>(new Link<T>());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends Link<T>> beanType() {
      return (Class) Link.class;
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
     * The meta-property for the {@code bundleId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBundle> bundleId() {
      return _bundleId;
    }

    /**
     * The meta-property for the {@code target} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<T> target() {
      return _target;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
