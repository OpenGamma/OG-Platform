/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.io.Serializable;
import java.util.Iterator;
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

import com.google.common.base.Objects;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A flexible link between two parts of the system.
 * <p>
 * A link represents a connection from one entity to another in the object model.
 * The connection can be held by {@code UniqueIdentifier}, {@code IdentifierBundle}
 * or by a resolved reference to the object itself.
 * <p>
 * This class is mutable and not thread-safe.
 * 
 * @param <T> the type being linked to 
 */
@PublicAPI
@BeanDefinition
public class Link<T extends UniqueIdentifiable> extends DirectBean
    implements ObjectIdentifiable, Iterable<Identifier>, Serializable, Cloneable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The object identifier that strongly references the entity.
   */
  @PropertyDefinition
  private ObjectIdentifier _objectId;
  /**
   * The identifier bundle that weakly references the entity.
   */
  @PropertyDefinition(validate = "notNull")
  private IdentifierBundle _idBundle = IdentifierBundle.EMPTY;
  /**
   * The resolved target.
   */
  @PropertyDefinition(set = "manual")
  private T _target;

  /**
   * Creates an new instance.
   */
  public Link() {
  }

  /**
   * Creates a link from an object identifier.
   * 
   * @param objectId  the object identifier, not null
   */
  public Link(final ObjectIdentifier objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    setObjectId(objectId);
  }

  /**
   * Creates a link from a unique identifier, only storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   */
  public Link(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    setObjectId(uniqueId.getObjectId());
  }

  /**
   * Creates a link from an identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  public Link(final IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    setIdBundle(bundle);
  }

  /**
   * Creates a link from the scheme and standalone identifier.
   * 
   * @param target  the resolved target, not null
   */
  public Link(final T target) {
    ArgumentChecker.notNull(target, "target");
    setTarget(target);
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the resolved target.
   * This also sets the object id.
   * 
   * @param target  the new value of the property, may be null
   */
  public void setTarget(T target) {
    if (target != null && target.getUniqueId() != null) {
      UniqueIdentifier uniqueId = target.getUniqueId();
      if (getObjectId() != null) {
        if (uniqueId.equalObjectIdentifier(getObjectId()) == false) {
          throw new IllegalArgumentException("Object id of target does not match link: " + uniqueId.getObjectId() + " vs " + getObjectId());
        }
      } else {
        setObjectId(uniqueId.getObjectId());
      }
    }
    this._target = target;
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
    ObjectIdentifier objectId = getObjectId();
    IdentifierBundle bundle = getIdBundle();
    return Objects.firstNonNull(target, Objects.firstNonNull(objectId, bundle));
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an identifier to the weak reference bundle.
   * 
   * @param idKey  the identifier to add, not null
   */
  public void addIdentifier(final Identifier idKey) {
    ArgumentChecker.notNull(idKey, "idKey");
    setIdBundle(getIdBundle().withIdentifier(idKey));
  }

  /**
   * Adds identifiers to the weak reference bundle.
   * 
   * @param idKeys  the identifiers to add, not null
   */
  public void addIdentifiers(final Iterable<Identifier> idKeys) {
    ArgumentChecker.notNull(idKeys, "idKeys");
    setIdBundle(getIdBundle().withIdentifiers(idKeys));
  }

  //-------------------------------------------------------------------------
  /**
   * Iterates over the weak reference identifiers.
   * 
   * @return the iterator, not null
   */
  @Override
  public Iterator<Identifier> iterator() {
    return getIdBundle().iterator();
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
      case -1131501187:  // idBundle
        return getIdBundle();
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
        setObjectId((ObjectIdentifier) newValue);
        return;
      case -1131501187:  // idBundle
        setIdBundle((IdentifierBundle) newValue);
        return;
      case -880905839:  // target
        setTarget((T) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_idBundle, "idBundle");
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
          JodaBeanUtils.equal(getIdBundle(), other.getIdBundle()) &&
          JodaBeanUtils.equal(getTarget(), other.getTarget());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdBundle());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTarget());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the object identifier that strongly references the entity.
   * @return the value of the property
   */
  public ObjectIdentifier getObjectId() {
    return _objectId;
  }

  /**
   * Sets the object identifier that strongly references the entity.
   * @param objectId  the new value of the property
   */
  public void setObjectId(ObjectIdentifier objectId) {
    this._objectId = objectId;
  }

  /**
   * Gets the the {@code objectId} property.
   * @return the property, not null
   */
  public final Property<ObjectIdentifier> objectId() {
    return metaBean().objectId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the identifier bundle that weakly references the entity.
   * @return the value of the property, not null
   */
  public IdentifierBundle getIdBundle() {
    return _idBundle;
  }

  /**
   * Sets the identifier bundle that weakly references the entity.
   * @param idBundle  the new value of the property, not null
   */
  public void setIdBundle(IdentifierBundle idBundle) {
    JodaBeanUtils.notNull(idBundle, "idBundle");
    this._idBundle = idBundle;
  }

  /**
   * Gets the the {@code idBundle} property.
   * @return the property, not null
   */
  public final Property<IdentifierBundle> idBundle() {
    return metaBean().idBundle().createProperty(this);
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
    private final MetaProperty<ObjectIdentifier> _objectId = DirectMetaProperty.ofReadWrite(
        this, "objectId", Link.class, ObjectIdentifier.class);
    /**
     * The meta-property for the {@code idBundle} property.
     */
    private final MetaProperty<IdentifierBundle> _idBundle = DirectMetaProperty.ofReadWrite(
        this, "idBundle", Link.class, IdentifierBundle.class);
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
        "idBundle",
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
        case -1131501187:  // idBundle
          return _idBundle;
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
    public final MetaProperty<ObjectIdentifier> objectId() {
      return _objectId;
    }

    /**
     * The meta-property for the {@code idBundle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierBundle> idBundle() {
      return _idBundle;
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
