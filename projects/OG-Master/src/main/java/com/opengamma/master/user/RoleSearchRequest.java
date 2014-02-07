/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.util.ArrayList;
import java.util.List;
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

import com.opengamma.core.user.OGEntitlement;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;

/**
 * Request for searching for roles.
 * <p/>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link com.opengamma.master.user.RoleHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class RoleSearchRequest extends AbstractSearchRequest {

  /**
   * The set of role object identifiers, null to not limit by role object identifiers.
   * Note that an empty list will return no roles.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _objectIds;
  /**
   * The display role name to search for, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;

  /**
   * The external identifier of a resource to match, null to not match on resource identifier.
   */
  @PropertyDefinition
  private String _resourceId;

  /**
   * The access type for a resource, null to not match on resource access type.
   */
  @PropertyDefinition
  private ResourceAccess _resourceAccess;

  /**
   * The type for a resource, null to not match on resource access type.
   */
  @PropertyDefinition
  private String _resourceType;

  /**
   * The unique identifier of a user for which we search roles.
   */
  @PropertyDefinition
  private UniqueId _userUid;

  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private RoleSearchSortOrder _sortOrder = RoleSearchSortOrder.OBJECT_ID_ASC;

  /**
   * Creates an instance.
   */
  public RoleSearchRequest() {
  }

  /**
   * Creates an instance using a single role name.
   *
   * @param name the role name to search for, not null
   */
  public RoleSearchRequest(String name) {
    setName(name);
  }

  //-------------------------------------------------------------------------

  /**
   * Adds a single role object identifier to the set.
   *
   * @param roleId the role object identifier to add, not null
   */
  public void addObjectId(ObjectIdentifiable roleId) {
    ArgumentChecker.notNull(roleId, "roleId");
    if (_objectIds == null) {
      _objectIds = new ArrayList<ObjectId>();
    }
    _objectIds.add(roleId.getObjectId());
  }

  /**
   * Sets the set of role object identifiers, null to not limit by role object identifiers.
   * Note that an empty collection will return no securities.
   *
   * @param roleIds the new role identifiers, null clears the role id search
   */
  public void setObjectIds(Iterable<? extends ObjectIdentifiable> roleIds) {
    if (roleIds == null) {
      _objectIds = null;
    } else {
      _objectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable roleId : roleIds) {
        _objectIds.add(roleId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(AbstractDocument obj) {
    if (obj instanceof RoleDocument == false) {
      return false;
    }
    RoleDocument document = (RoleDocument) obj;
    ManageableOGRole role = document.getRole();
    if (getObjectIds() != null && getObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getResourceId() != null || getResourceAccess() != null || getResourceType() != null) {
      for (OGEntitlement ogEntitlement : role.getEntitlements()) {
        if (
            (getResourceId() == null || getResourceId().equals(ogEntitlement.getResourceId()))
                &&
                (getResourceType() == null || getResourceType().equals(ogEntitlement.getType()))
                &&
                (getResourceAccess() == null || getResourceAccess().equals(ogEntitlement.getAccess()))
            ) {
          return true;
        }
      }
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), role.getName()) == false) {
      return false;
    }
    return true;
  }

  public static RoleSearchRequest byUserUid(UniqueId uniqueId) {
    RoleSearchRequest rsr = new RoleSearchRequest();
    rsr.setUserUid(uniqueId);
    return rsr;
  }

  public static RoleSearchRequest byUserUidAndEntitlement(UniqueId uniqueId, String resourceId, ResourceAccess resourceAccess) {
    RoleSearchRequest rsr = new RoleSearchRequest();
    rsr.setUserUid(uniqueId);
    rsr.setResourceId(resourceId);
    rsr.setResourceAccess(resourceAccess);
    return rsr;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RoleSearchRequest}.
   * @return the meta-bean, not null
   */
  public static RoleSearchRequest.Meta meta() {
    return RoleSearchRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RoleSearchRequest.Meta.INSTANCE);
  }

  @Override
  public RoleSearchRequest.Meta metaBean() {
    return RoleSearchRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of role object identifiers, null to not limit by role object identifiers.
   * Note that an empty list will return no roles.
   * @return the value of the property
   */
  public List<ObjectId> getObjectIds() {
    return _objectIds;
  }

  /**
   * Gets the the {@code objectIds} property.
   * Note that an empty list will return no roles.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> objectIds() {
    return metaBean().objectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the display role name to search for, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the display role name to search for, wildcards allowed, null to not match on name.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier of a resource to match, null to not match on resource identifier.
   * @return the value of the property
   */
  public String getResourceId() {
    return _resourceId;
  }

  /**
   * Sets the external identifier of a resource to match, null to not match on resource identifier.
   * @param resourceId  the new value of the property
   */
  public void setResourceId(String resourceId) {
    this._resourceId = resourceId;
  }

  /**
   * Gets the the {@code resourceId} property.
   * @return the property, not null
   */
  public final Property<String> resourceId() {
    return metaBean().resourceId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the access type for a resource, null to not match on resource access type.
   * @return the value of the property
   */
  public ResourceAccess getResourceAccess() {
    return _resourceAccess;
  }

  /**
   * Sets the access type for a resource, null to not match on resource access type.
   * @param resourceAccess  the new value of the property
   */
  public void setResourceAccess(ResourceAccess resourceAccess) {
    this._resourceAccess = resourceAccess;
  }

  /**
   * Gets the the {@code resourceAccess} property.
   * @return the property, not null
   */
  public final Property<ResourceAccess> resourceAccess() {
    return metaBean().resourceAccess().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the type for a resource, null to not match on resource access type.
   * @return the value of the property
   */
  public String getResourceType() {
    return _resourceType;
  }

  /**
   * Sets the type for a resource, null to not match on resource access type.
   * @param resourceType  the new value of the property
   */
  public void setResourceType(String resourceType) {
    this._resourceType = resourceType;
  }

  /**
   * Gets the the {@code resourceType} property.
   * @return the property, not null
   */
  public final Property<String> resourceType() {
    return metaBean().resourceType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of a user for which we search roles.
   * @return the value of the property
   */
  public UniqueId getUserUid() {
    return _userUid;
  }

  /**
   * Sets the unique identifier of a user for which we search roles.
   * @param userUid  the new value of the property
   */
  public void setUserUid(UniqueId userUid) {
    this._userUid = userUid;
  }

  /**
   * Gets the the {@code userUid} property.
   * @return the property, not null
   */
  public final Property<UniqueId> userUid() {
    return metaBean().userUid().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sort order to use.
   * @return the value of the property, not null
   */
  public RoleSearchSortOrder getSortOrder() {
    return _sortOrder;
  }

  /**
   * Sets the sort order to use.
   * @param sortOrder  the new value of the property, not null
   */
  public void setSortOrder(RoleSearchSortOrder sortOrder) {
    JodaBeanUtils.notNull(sortOrder, "sortOrder");
    this._sortOrder = sortOrder;
  }

  /**
   * Gets the the {@code sortOrder} property.
   * @return the property, not null
   */
  public final Property<RoleSearchSortOrder> sortOrder() {
    return metaBean().sortOrder().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public RoleSearchRequest clone() {
    return (RoleSearchRequest) super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RoleSearchRequest other = (RoleSearchRequest) obj;
      return JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getResourceId(), other.getResourceId()) &&
          JodaBeanUtils.equal(getResourceAccess(), other.getResourceAccess()) &&
          JodaBeanUtils.equal(getResourceType(), other.getResourceType()) &&
          JodaBeanUtils.equal(getUserUid(), other.getUserUid()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getResourceId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getResourceAccess());
    hash += hash * 31 + JodaBeanUtils.hashCode(getResourceType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserUid());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("RoleSearchRequest{");
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
    buf.append("objectIds").append('=').append(JodaBeanUtils.toString(getObjectIds())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("resourceId").append('=').append(JodaBeanUtils.toString(getResourceId())).append(',').append(' ');
    buf.append("resourceAccess").append('=').append(JodaBeanUtils.toString(getResourceAccess())).append(',').append(' ');
    buf.append("resourceType").append('=').append(JodaBeanUtils.toString(getResourceType())).append(',').append(' ');
    buf.append("userUid").append('=').append(JodaBeanUtils.toString(getUserUid())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RoleSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code objectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _objectIds = DirectMetaProperty.ofReadWrite(
        this, "objectIds", RoleSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", RoleSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code resourceId} property.
     */
    private final MetaProperty<String> _resourceId = DirectMetaProperty.ofReadWrite(
        this, "resourceId", RoleSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code resourceAccess} property.
     */
    private final MetaProperty<ResourceAccess> _resourceAccess = DirectMetaProperty.ofReadWrite(
        this, "resourceAccess", RoleSearchRequest.class, ResourceAccess.class);
    /**
     * The meta-property for the {@code resourceType} property.
     */
    private final MetaProperty<String> _resourceType = DirectMetaProperty.ofReadWrite(
        this, "resourceType", RoleSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code userUid} property.
     */
    private final MetaProperty<UniqueId> _userUid = DirectMetaProperty.ofReadWrite(
        this, "userUid", RoleSearchRequest.class, UniqueId.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<RoleSearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", RoleSearchRequest.class, RoleSearchSortOrder.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "objectIds",
        "name",
        "resourceId",
        "resourceAccess",
        "resourceType",
        "userUid",
        "sortOrder");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return _objectIds;
        case 3373707:  // name
          return _name;
        case -1345650231:  // resourceId
          return _resourceId;
        case -571694318:  // resourceAccess
          return _resourceAccess;
        case -384364440:  // resourceType
          return _resourceType;
        case -147142523:  // userUid
          return _userUid;
        case -26774448:  // sortOrder
          return _sortOrder;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RoleSearchRequest> builder() {
      return new DirectBeanBuilder<RoleSearchRequest>(new RoleSearchRequest());
    }

    @Override
    public Class<? extends RoleSearchRequest> beanType() {
      return RoleSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code objectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> objectIds() {
      return _objectIds;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code resourceId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> resourceId() {
      return _resourceId;
    }

    /**
     * The meta-property for the {@code resourceAccess} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ResourceAccess> resourceAccess() {
      return _resourceAccess;
    }

    /**
     * The meta-property for the {@code resourceType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> resourceType() {
      return _resourceType;
    }

    /**
     * The meta-property for the {@code userUid} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> userUid() {
      return _userUid;
    }

    /**
     * The meta-property for the {@code sortOrder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RoleSearchSortOrder> sortOrder() {
      return _sortOrder;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return ((RoleSearchRequest) bean).getObjectIds();
        case 3373707:  // name
          return ((RoleSearchRequest) bean).getName();
        case -1345650231:  // resourceId
          return ((RoleSearchRequest) bean).getResourceId();
        case -571694318:  // resourceAccess
          return ((RoleSearchRequest) bean).getResourceAccess();
        case -384364440:  // resourceType
          return ((RoleSearchRequest) bean).getResourceType();
        case -147142523:  // userUid
          return ((RoleSearchRequest) bean).getUserUid();
        case -26774448:  // sortOrder
          return ((RoleSearchRequest) bean).getSortOrder();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          ((RoleSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case 3373707:  // name
          ((RoleSearchRequest) bean).setName((String) newValue);
          return;
        case -1345650231:  // resourceId
          ((RoleSearchRequest) bean).setResourceId((String) newValue);
          return;
        case -571694318:  // resourceAccess
          ((RoleSearchRequest) bean).setResourceAccess((ResourceAccess) newValue);
          return;
        case -384364440:  // resourceType
          ((RoleSearchRequest) bean).setResourceType((String) newValue);
          return;
        case -147142523:  // userUid
          ((RoleSearchRequest) bean).setUserUid((UniqueId) newValue);
          return;
        case -26774448:  // sortOrder
          ((RoleSearchRequest) bean).setSortOrder((RoleSearchSortOrder) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((RoleSearchRequest) bean)._sortOrder, "sortOrder");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
