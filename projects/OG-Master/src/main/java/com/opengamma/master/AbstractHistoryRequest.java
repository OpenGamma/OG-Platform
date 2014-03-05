/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

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
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.paging.PagingRequest;

/**
 * Request for the history of a document.
 * <p>
 * A full master implements historical storage of data.
 * History can be stored in two dimensions and this request provides searching.
 * <p>
 * The first historic dimension is the classic series of versions.
 * Each new version is stored in such a manor that previous versions can be accessed.
 * <p>
 * The second historic dimension is corrections.
 * A correction occurs when it is realized that the original data stored was incorrect.
 * A simple master might simply replace the original version with the corrected value.
 * A full implementation will store the correction in such a manner that it is still possible
 * to obtain the value before the correction was made.
 * <p>
 * For example, a document added on Monday and updated on Thursday has two versions.
 * If it is realized on Friday that the version stored on Monday was incorrect, then a
 * correction may be applied. There are now two versions, the first of which has one correction.
 * This may continue, with multiple corrections allowed for each version.
 * <p>
 * Versions and corrections are represented by instants in the search.
 */
@PublicSPI
@BeanDefinition
public abstract class AbstractHistoryRequest extends DirectBean implements PagedRequest {

  /**
   * The request for paging.
   * By default all matching items will be returned.
   */
  @PropertyDefinition
  private PagingRequest _pagingRequest = PagingRequest.ALL;
  /**
   * The object identifier to match.
   */
  @PropertyDefinition
  private ObjectId _objectId;
  /**
   * The instant to retrieve versions on or after (inclusive).
   * If this instant equals the {@code versionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version.
   */
  @PropertyDefinition
  private Instant _versionsFromInstant;
  /**
   * The instant to retrieve versions before (exclusive).
   * If this instant equals the {@code versionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest version.
   * This should be equal to or later than the {@code versionsFromInstant}.
   */
  @PropertyDefinition
  private Instant _versionsToInstant;
  /**
   * The instant to retrieve corrections on or after (inclusive).
   * If this instant equals the {@code correctionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version prior to corrections.
   * This should be equal to or later than the {@code versionsFromInstant}.
   */
  @PropertyDefinition
  private Instant _correctionsFromInstant;
  /**
   * The instant to retrieve corrections before (exclusive).
   * If this instant equals the {@code correctionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest correction.
   * This should be equal to or later than the {@code correctionsFromInstant}.
   */
  @PropertyDefinition
  private Instant _correctionsToInstant;

  /**
   * Creates an instance.
   * The object identifier must be added before searching.
   */
  public AbstractHistoryRequest() {
  }

  /**
   * Creates an instance with object identifier.
   * This will retrieve all versions and corrections unless the relevant fields are set.
   * 
   * @param objectId  the object identifier, not null
   */
  public AbstractHistoryRequest(final ObjectIdentifiable objectId) {
    this(objectId, null, null);
  }

  /**
   * Creates an instance with object identifier and optional version and correction.
   * 
   * @param objectId  the object identifier, not null
   * @param versionInstant  the version instant to retrieve, null for all versions
   * @param correctedToInstant  the instant that the data should be corrected to, null for all corrections
   */
  public AbstractHistoryRequest(final ObjectIdentifiable objectId, Instant versionInstant, Instant correctedToInstant) {
    ArgumentChecker.notNull(objectId, "objectId");
    setObjectId(objectId.getObjectId());
    setVersionsFromInstant(versionInstant);
    setVersionsToInstant(versionInstant);
    setCorrectionsFromInstant(correctedToInstant);
    setCorrectionsToInstant(correctedToInstant);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AbstractHistoryRequest}.
   * @return the meta-bean, not null
   */
  public static AbstractHistoryRequest.Meta meta() {
    return AbstractHistoryRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AbstractHistoryRequest.Meta.INSTANCE);
  }

  @Override
  public AbstractHistoryRequest.Meta metaBean() {
    return AbstractHistoryRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the request for paging.
   * By default all matching items will be returned.
   * @return the value of the property
   */
  public PagingRequest getPagingRequest() {
    return _pagingRequest;
  }

  /**
   * Sets the request for paging.
   * By default all matching items will be returned.
   * @param pagingRequest  the new value of the property
   */
  public void setPagingRequest(PagingRequest pagingRequest) {
    this._pagingRequest = pagingRequest;
  }

  /**
   * Gets the the {@code pagingRequest} property.
   * By default all matching items will be returned.
   * @return the property, not null
   */
  public final Property<PagingRequest> pagingRequest() {
    return metaBean().pagingRequest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the object identifier to match.
   * @return the value of the property
   */
  public ObjectId getObjectId() {
    return _objectId;
  }

  /**
   * Sets the object identifier to match.
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
   * Gets the instant to retrieve versions on or after (inclusive).
   * If this instant equals the {@code versionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version.
   * @return the value of the property
   */
  public Instant getVersionsFromInstant() {
    return _versionsFromInstant;
  }

  /**
   * Sets the instant to retrieve versions on or after (inclusive).
   * If this instant equals the {@code versionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version.
   * @param versionsFromInstant  the new value of the property
   */
  public void setVersionsFromInstant(Instant versionsFromInstant) {
    this._versionsFromInstant = versionsFromInstant;
  }

  /**
   * Gets the the {@code versionsFromInstant} property.
   * If this instant equals the {@code versionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version.
   * @return the property, not null
   */
  public final Property<Instant> versionsFromInstant() {
    return metaBean().versionsFromInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to retrieve versions before (exclusive).
   * If this instant equals the {@code versionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest version.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @return the value of the property
   */
  public Instant getVersionsToInstant() {
    return _versionsToInstant;
  }

  /**
   * Sets the instant to retrieve versions before (exclusive).
   * If this instant equals the {@code versionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest version.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @param versionsToInstant  the new value of the property
   */
  public void setVersionsToInstant(Instant versionsToInstant) {
    this._versionsToInstant = versionsToInstant;
  }

  /**
   * Gets the the {@code versionsToInstant} property.
   * If this instant equals the {@code versionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest version.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @return the property, not null
   */
  public final Property<Instant> versionsToInstant() {
    return metaBean().versionsToInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to retrieve corrections on or after (inclusive).
   * If this instant equals the {@code correctionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version prior to corrections.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @return the value of the property
   */
  public Instant getCorrectionsFromInstant() {
    return _correctionsFromInstant;
  }

  /**
   * Sets the instant to retrieve corrections on or after (inclusive).
   * If this instant equals the {@code correctionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version prior to corrections.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @param correctionsFromInstant  the new value of the property
   */
  public void setCorrectionsFromInstant(Instant correctionsFromInstant) {
    this._correctionsFromInstant = correctionsFromInstant;
  }

  /**
   * Gets the the {@code correctionsFromInstant} property.
   * If this instant equals the {@code correctionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version prior to corrections.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @return the property, not null
   */
  public final Property<Instant> correctionsFromInstant() {
    return metaBean().correctionsFromInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to retrieve corrections before (exclusive).
   * If this instant equals the {@code correctionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest correction.
   * This should be equal to or later than the {@code correctionsFromInstant}.
   * @return the value of the property
   */
  public Instant getCorrectionsToInstant() {
    return _correctionsToInstant;
  }

  /**
   * Sets the instant to retrieve corrections before (exclusive).
   * If this instant equals the {@code correctionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest correction.
   * This should be equal to or later than the {@code correctionsFromInstant}.
   * @param correctionsToInstant  the new value of the property
   */
  public void setCorrectionsToInstant(Instant correctionsToInstant) {
    this._correctionsToInstant = correctionsToInstant;
  }

  /**
   * Gets the the {@code correctionsToInstant} property.
   * If this instant equals the {@code correctionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest correction.
   * This should be equal to or later than the {@code correctionsFromInstant}.
   * @return the property, not null
   */
  public final Property<Instant> correctionsToInstant() {
    return metaBean().correctionsToInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public AbstractHistoryRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractHistoryRequest other = (AbstractHistoryRequest) obj;
      return JodaBeanUtils.equal(getPagingRequest(), other.getPagingRequest()) &&
          JodaBeanUtils.equal(getObjectId(), other.getObjectId()) &&
          JodaBeanUtils.equal(getVersionsFromInstant(), other.getVersionsFromInstant()) &&
          JodaBeanUtils.equal(getVersionsToInstant(), other.getVersionsToInstant()) &&
          JodaBeanUtils.equal(getCorrectionsFromInstant(), other.getCorrectionsFromInstant()) &&
          JodaBeanUtils.equal(getCorrectionsToInstant(), other.getCorrectionsToInstant());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getPagingRequest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVersionsFromInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVersionsToInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCorrectionsFromInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCorrectionsToInstant());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("AbstractHistoryRequest{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("pagingRequest").append('=').append(JodaBeanUtils.toString(getPagingRequest())).append(',').append(' ');
    buf.append("objectId").append('=').append(JodaBeanUtils.toString(getObjectId())).append(',').append(' ');
    buf.append("versionsFromInstant").append('=').append(JodaBeanUtils.toString(getVersionsFromInstant())).append(',').append(' ');
    buf.append("versionsToInstant").append('=').append(JodaBeanUtils.toString(getVersionsToInstant())).append(',').append(' ');
    buf.append("correctionsFromInstant").append('=').append(JodaBeanUtils.toString(getCorrectionsFromInstant())).append(',').append(' ');
    buf.append("correctionsToInstant").append('=').append(JodaBeanUtils.toString(getCorrectionsToInstant())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractHistoryRequest}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code pagingRequest} property.
     */
    private final MetaProperty<PagingRequest> _pagingRequest = DirectMetaProperty.ofReadWrite(
        this, "pagingRequest", AbstractHistoryRequest.class, PagingRequest.class);
    /**
     * The meta-property for the {@code objectId} property.
     */
    private final MetaProperty<ObjectId> _objectId = DirectMetaProperty.ofReadWrite(
        this, "objectId", AbstractHistoryRequest.class, ObjectId.class);
    /**
     * The meta-property for the {@code versionsFromInstant} property.
     */
    private final MetaProperty<Instant> _versionsFromInstant = DirectMetaProperty.ofReadWrite(
        this, "versionsFromInstant", AbstractHistoryRequest.class, Instant.class);
    /**
     * The meta-property for the {@code versionsToInstant} property.
     */
    private final MetaProperty<Instant> _versionsToInstant = DirectMetaProperty.ofReadWrite(
        this, "versionsToInstant", AbstractHistoryRequest.class, Instant.class);
    /**
     * The meta-property for the {@code correctionsFromInstant} property.
     */
    private final MetaProperty<Instant> _correctionsFromInstant = DirectMetaProperty.ofReadWrite(
        this, "correctionsFromInstant", AbstractHistoryRequest.class, Instant.class);
    /**
     * The meta-property for the {@code correctionsToInstant} property.
     */
    private final MetaProperty<Instant> _correctionsToInstant = DirectMetaProperty.ofReadWrite(
        this, "correctionsToInstant", AbstractHistoryRequest.class, Instant.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "pagingRequest",
        "objectId",
        "versionsFromInstant",
        "versionsToInstant",
        "correctionsFromInstant",
        "correctionsToInstant");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -2092032669:  // pagingRequest
          return _pagingRequest;
        case 90495162:  // objectId
          return _objectId;
        case 825630012:  // versionsFromInstant
          return _versionsFromInstant;
        case 288644747:  // versionsToInstant
          return _versionsToInstant;
        case -1002076478:  // correctionsFromInstant
          return _correctionsFromInstant;
        case -1241747055:  // correctionsToInstant
          return _correctionsToInstant;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AbstractHistoryRequest> builder() {
      throw new UnsupportedOperationException("AbstractHistoryRequest is an abstract class");
    }

    @Override
    public Class<? extends AbstractHistoryRequest> beanType() {
      return AbstractHistoryRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code pagingRequest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PagingRequest> pagingRequest() {
      return _pagingRequest;
    }

    /**
     * The meta-property for the {@code objectId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ObjectId> objectId() {
      return _objectId;
    }

    /**
     * The meta-property for the {@code versionsFromInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> versionsFromInstant() {
      return _versionsFromInstant;
    }

    /**
     * The meta-property for the {@code versionsToInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> versionsToInstant() {
      return _versionsToInstant;
    }

    /**
     * The meta-property for the {@code correctionsFromInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> correctionsFromInstant() {
      return _correctionsFromInstant;
    }

    /**
     * The meta-property for the {@code correctionsToInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> correctionsToInstant() {
      return _correctionsToInstant;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2092032669:  // pagingRequest
          return ((AbstractHistoryRequest) bean).getPagingRequest();
        case 90495162:  // objectId
          return ((AbstractHistoryRequest) bean).getObjectId();
        case 825630012:  // versionsFromInstant
          return ((AbstractHistoryRequest) bean).getVersionsFromInstant();
        case 288644747:  // versionsToInstant
          return ((AbstractHistoryRequest) bean).getVersionsToInstant();
        case -1002076478:  // correctionsFromInstant
          return ((AbstractHistoryRequest) bean).getCorrectionsFromInstant();
        case -1241747055:  // correctionsToInstant
          return ((AbstractHistoryRequest) bean).getCorrectionsToInstant();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2092032669:  // pagingRequest
          ((AbstractHistoryRequest) bean).setPagingRequest((PagingRequest) newValue);
          return;
        case 90495162:  // objectId
          ((AbstractHistoryRequest) bean).setObjectId((ObjectId) newValue);
          return;
        case 825630012:  // versionsFromInstant
          ((AbstractHistoryRequest) bean).setVersionsFromInstant((Instant) newValue);
          return;
        case 288644747:  // versionsToInstant
          ((AbstractHistoryRequest) bean).setVersionsToInstant((Instant) newValue);
          return;
        case -1002076478:  // correctionsFromInstant
          ((AbstractHistoryRequest) bean).setCorrectionsFromInstant((Instant) newValue);
          return;
        case -1241747055:  // correctionsToInstant
          ((AbstractHistoryRequest) bean).setCorrectionsToInstant((Instant) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
