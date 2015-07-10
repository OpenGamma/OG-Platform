/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.io.Serializable;
import java.util.Map;

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

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * An abstract implementation of a link between two parts of the system.
 * <p>
 * A link represents a connection from one entity to another in the object model.
 * The connection can be held by an {@code ObjectId} or an {@code ExternalIdBundle}.
 * The link is resolved using a resolver.
 * <p>
 * This class makes no guarantees about the thread-safety of implementations.
 * However, it is strongly recommended that the methods in this interface are individually thread-safe.
 * 
 * @param <T> the target type of the link
 */
@BeanDefinition
public abstract class AbstractLink<T extends UniqueIdentifiable> extends DirectBean
    implements Link<T>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The object identifier that strongly references the target.
   */
  @PropertyDefinition(overrideGet = true)
  private ObjectId _objectId;
  /**
   * The external identifier bundle that references the target.
   * An empty bundle is used if not referencing a target by external bundle.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private ExternalIdBundle _externalId = ExternalIdBundle.EMPTY;

  /**
   * Creates an new instance.
   */
  protected AbstractLink() {
    super();
  }

  /**
   * Creates a link from an object identifier.
   * 
   * @param objectId  the object identifier, not null
   */
  protected AbstractLink(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    setObjectId(objectId);
  }

  /**
   * Creates a link from a unique identifier, only storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   */
  protected AbstractLink(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    setObjectId(uniqueId.getObjectId());
  }

  /**
   * Creates a link from an external identifier.
   * 
   * @param externalId  the external identifier, not null
   */
  protected AbstractLink(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    setExternalId(ExternalIdBundle.of(externalId));
  }

  /**
   * Creates a link from an external identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  protected AbstractLink(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    setExternalId(bundle);
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
    return LinkUtils.best(this);
  }

  /**
   * Gets the best descriptive name.
   * 
   * @return the best descriptive name, not null
   */
  public String getBestName() {
    return LinkUtils.bestName(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the link to the target.
   * <p>
   * This simply calls {@link LinkResolver#resolve(Link)}.
   * 
   * @param resolver  the resolver capable of finding the target, not null
   * @return the resolved target, null if unable to resolve
   * @throws DataNotFoundException if the target could not be resolved
   * @throws RuntimeException if an error occurs
   */
  @Override
  public T resolve(LinkResolver<T> resolver) {
    return resolver.resolve(this);
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
  @Override
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
  @Override
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
          JodaBeanUtils.equal(getExternalId(), other.getExternalId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getObjectId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalId());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
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
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "objectId",
        "externalId");

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

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 90495162:  // objectId
          return ((AbstractLink<?>) bean).getObjectId();
        case -1699764666:  // externalId
          return ((AbstractLink<?>) bean).getExternalId();
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
