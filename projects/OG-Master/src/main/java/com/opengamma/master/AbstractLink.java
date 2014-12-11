/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.base.Objects;
import com.opengamma.core.Link;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
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
public abstract class AbstractLink<T extends UniqueIdentifiable> extends DirectBean
    implements Link<T>, Iterable<ExternalId>, Serializable, Cloneable {

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
  private ExternalIdBundle _externalId = ExternalIdBundle.EMPTY;
  /**
   * The resolved target.
   */
  @PropertyDefinition
  private T _target;

  /**
   * Creates an new instance.
   */
  public AbstractLink() {
  }

  /**
   * Creates a link from a strong object identifier.
   * 
   * @param objectId  the object identifier, not null
   */
  public AbstractLink(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    setObjectId(objectId);
  }

  /**
   * Creates a link from a strong unique identifier, only
   * storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   */
  public AbstractLink(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    setObjectId(uniqueId.getObjectId());
  }

  /**
   * Creates a link from an external identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  public AbstractLink(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    setExternalId(bundle);
  }

  /**
   * Creates a link from the target object.
   * The strong reference is set using {@link #setAndLockTarget}.
   * 
   * @param target  the resolved target, not null
   */
  public AbstractLink(final T target) {
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
      setExternalId(ExternalIdBundle.EMPTY);
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
    ExternalIdBundle bundle = getExternalId();
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
   * Iterates over the external reference identifiers.
   * 
   * @return the iterator, not null
   */
  @Override
  public Iterator<ExternalId> iterator() {
    return getExternalId().iterator();
  }

  /**
   * Gets the set of external reference identifiers.
   * This excludes the strong object identifier.
   * 
   * @return all the identifiers, not null
   */
  public Set<ExternalId> getExternalIds() {
    return getExternalId().getExternalIds();
  }

  /**
   * Gets a set of all identifiers, including the object identifier
   * expressed as an {@code ExternalId}.
   * 
   * @return all the identifiers, not null
   */
  public Set<ExternalId> getAllExternalIds() {
    Set<ExternalId> identifiers = getExternalId().getExternalIds();
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
   * The meta-bean for {@code AbstractLink}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static AbstractLink.Meta meta() {
    return AbstractLink.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code AbstractLink}.
   * @param <R>  the bean's generic type
   * @param cls  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R extends UniqueIdentifiable> AbstractLink.Meta<R> metaAbstractLink(Class<R> cls) {
    return AbstractLink.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AbstractLink.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractLink.Meta<T> metaBean() {
    return AbstractLink.Meta.INSTANCE;
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
  @Override
  public AbstractLink<T> clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractLink<?> other = (AbstractLink<?>) obj;
      return JodaBeanUtils.equal(getObjectId(), other.getObjectId()) &&
          JodaBeanUtils.equal(getExternalId(), other.getExternalId()) &&
          JodaBeanUtils.equal(getTarget(), other.getTarget());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getObjectId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTarget());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("AbstractLink{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("objectId").append('=').append(JodaBeanUtils.toString(getObjectId())).append(',').append(' ');
    buf.append("externalId").append('=').append(JodaBeanUtils.toString(getExternalId())).append(',').append(' ');
    buf.append("target").append('=').append(JodaBeanUtils.toString(getTarget())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractLink}.
   * @param <T>  the type
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
        this, "objectId", AbstractLink.class, ObjectId.class);
    /**
     * The meta-property for the {@code externalId} property.
     */
    private final MetaProperty<ExternalIdBundle> _externalId = DirectMetaProperty.ofReadWrite(
        this, "externalId", AbstractLink.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code target} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<T> _target = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
        this, "target", AbstractLink.class, Object.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
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
    public BeanBuilder<? extends AbstractLink<T>> builder() {
      throw new UnsupportedOperationException("AbstractLink is an abstract class");
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends AbstractLink<T>> beanType() {
      return (Class) AbstractLink.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
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
    public final MetaProperty<T> target() {
      return _target;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 90495162:  // objectId
          return ((AbstractLink<?>) bean).getObjectId();
        case -1699764666:  // externalId
          return ((AbstractLink<?>) bean).getExternalId();
        case -880905839:  // target
          return ((AbstractLink<?>) bean).getTarget();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 90495162:  // objectId
          ((AbstractLink<T>) bean).setObjectId((ObjectId) newValue);
          return;
        case -1699764666:  // externalId
          ((AbstractLink<T>) bean).setExternalId((ExternalIdBundle) newValue);
          return;
        case -880905839:  // target
          ((AbstractLink<T>) bean).setTarget((T) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((AbstractLink<?>) bean)._externalId, "externalId");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
