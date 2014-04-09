/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.paging.PagingRequest;

/**
 * Request for searching for roles. 
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link UserEventHistoryRequest} for more details on how history works.
 */
@BeanDefinition
public class RoleSearchRequest implements Bean {

  /**
   * The request for paging.
   * By default all matching items will be returned.
   */
  @PropertyDefinition
  private PagingRequest _pagingRequest = PagingRequest.ALL;
  /**
   * The set of role object identifiers, null to not limit by user object identifiers.
   * Note that an empty list will return no users.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _objectIds;
  /**
   * The role name to search for, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _roleName;
  /**
   * The associated role name to search for, no wildcards.
   * If used, only those roles which explicitly reference the role are returned.
   * Any roles implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getRoles()}.
   */
  @PropertyDefinition
  private String _associatedRole;
  /**
   * The associated user name to search for, no wildcards.
   * If used, only those roles which explicitly reference the user are returned.
   * Any users implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getUsers()}.
   */
  @PropertyDefinition
  private String _associatedUser;
  /**
   * The associated permission to search for, no wildcards.
   * If used, only those roles which explicitly reference the permission are returned.
   * Any permissions implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getPermissions()}.
   */
  @PropertyDefinition
  private String _associatedPermission;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private RoleSearchSortOrder _sortOrder = RoleSearchSortOrder.NAME_ASC;

  /**
   * Creates an instance.
   */
  public RoleSearchRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single user object identifier to the set.
   * 
   * @param userId  the user object identifier to add, not null
   */
  public void addObjectId(ObjectIdentifiable userId) {
    ArgumentChecker.notNull(userId, "userId");
    if (_objectIds == null) {
      _objectIds = new ArrayList<ObjectId>();
    }
    _objectIds.add(userId.getObjectId());
  }

  /**
   * Sets the set of user object identifiers, null to not limit by user object identifiers.
   * Note that an empty collection will return no securities.
   * 
   * @param userIds  the new user identifiers, null clears the user id search
   */
  public void setObjectIds(Iterable<? extends ObjectIdentifiable> userIds) {
    if (userIds == null) {
      _objectIds = null;
    } else {
      _objectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable userId : userIds) {
        _objectIds.add(userId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this search matches the specified role.
   *
   * @param role  the role to match, null returns false
   * @return true if matches
   */
  public boolean matches(ManageableRole role) {
    if (role == null) {
      return false;
    }
    if (getObjectIds() != null && getObjectIds().contains(role.getObjectId()) == false) {
      return false;
    }
    if (getRoleName() != null && RegexUtils.wildcardMatch(getRoleName(), role.getRoleName()) == false) {
      return false;
    }
    if (role.getAssociatedRoles().contains(getAssociatedPermission()) == false) {
      return false;
    }
    if (role.getAssociatedUsers().contains(getAssociatedPermission()) == false) {
      return false;
    }
    if (role.getAssociatedPermissions().contains(getAssociatedPermission()) == false) {
      return false;
    }
    return true;
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

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
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
   * Gets the set of role object identifiers, null to not limit by user object identifiers.
   * Note that an empty list will return no users.
   * @return the value of the property
   */
  public List<ObjectId> getObjectIds() {
    return _objectIds;
  }

  /**
   * Gets the the {@code objectIds} property.
   * Note that an empty list will return no users.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> objectIds() {
    return metaBean().objectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the role name to search for, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getRoleName() {
    return _roleName;
  }

  /**
   * Sets the role name to search for, wildcards allowed, null to not match on name.
   * @param roleName  the new value of the property
   */
  public void setRoleName(String roleName) {
    this._roleName = roleName;
  }

  /**
   * Gets the the {@code roleName} property.
   * @return the property, not null
   */
  public final Property<String> roleName() {
    return metaBean().roleName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the associated role name to search for, no wildcards.
   * If used, only those roles which explicitly reference the role are returned.
   * Any roles implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getRoles()}.
   * @return the value of the property
   */
  public String getAssociatedRole() {
    return _associatedRole;
  }

  /**
   * Sets the associated role name to search for, no wildcards.
   * If used, only those roles which explicitly reference the role are returned.
   * Any roles implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getRoles()}.
   * @param associatedRole  the new value of the property
   */
  public void setAssociatedRole(String associatedRole) {
    this._associatedRole = associatedRole;
  }

  /**
   * Gets the the {@code associatedRole} property.
   * If used, only those roles which explicitly reference the role are returned.
   * Any roles implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getRoles()}.
   * @return the property, not null
   */
  public final Property<String> associatedRole() {
    return metaBean().associatedRole().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the associated user name to search for, no wildcards.
   * If used, only those roles which explicitly reference the user are returned.
   * Any users implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getUsers()}.
   * @return the value of the property
   */
  public String getAssociatedUser() {
    return _associatedUser;
  }

  /**
   * Sets the associated user name to search for, no wildcards.
   * If used, only those roles which explicitly reference the user are returned.
   * Any users implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getUsers()}.
   * @param associatedUser  the new value of the property
   */
  public void setAssociatedUser(String associatedUser) {
    this._associatedUser = associatedUser;
  }

  /**
   * Gets the the {@code associatedUser} property.
   * If used, only those roles which explicitly reference the user are returned.
   * Any users implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getUsers()}.
   * @return the property, not null
   */
  public final Property<String> associatedUser() {
    return metaBean().associatedUser().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the associated permission to search for, no wildcards.
   * If used, only those roles which explicitly reference the permission are returned.
   * Any permissions implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getPermissions()}.
   * @return the value of the property
   */
  public String getAssociatedPermission() {
    return _associatedPermission;
  }

  /**
   * Sets the associated permission to search for, no wildcards.
   * If used, only those roles which explicitly reference the permission are returned.
   * Any permissions implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getPermissions()}.
   * @param associatedPermission  the new value of the property
   */
  public void setAssociatedPermission(String associatedPermission) {
    this._associatedPermission = associatedPermission;
  }

  /**
   * Gets the the {@code associatedPermission} property.
   * If used, only those roles which explicitly reference the permission are returned.
   * Any permissions implied by membership of other roles are not matched.
   * In other words, this searches {@link ManageableRole#getPermissions()}.
   * @return the property, not null
   */
  public final Property<String> associatedPermission() {
    return metaBean().associatedPermission().createProperty(this);
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
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RoleSearchRequest other = (RoleSearchRequest) obj;
      return JodaBeanUtils.equal(getPagingRequest(), other.getPagingRequest()) &&
          JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getRoleName(), other.getRoleName()) &&
          JodaBeanUtils.equal(getAssociatedRole(), other.getAssociatedRole()) &&
          JodaBeanUtils.equal(getAssociatedUser(), other.getAssociatedUser()) &&
          JodaBeanUtils.equal(getAssociatedPermission(), other.getAssociatedPermission()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getPagingRequest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRoleName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAssociatedRole());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAssociatedUser());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAssociatedPermission());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    return hash;
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

  protected void toString(StringBuilder buf) {
    buf.append("pagingRequest").append('=').append(JodaBeanUtils.toString(getPagingRequest())).append(',').append(' ');
    buf.append("objectIds").append('=').append(JodaBeanUtils.toString(getObjectIds())).append(',').append(' ');
    buf.append("roleName").append('=').append(JodaBeanUtils.toString(getRoleName())).append(',').append(' ');
    buf.append("associatedRole").append('=').append(JodaBeanUtils.toString(getAssociatedRole())).append(',').append(' ');
    buf.append("associatedUser").append('=').append(JodaBeanUtils.toString(getAssociatedUser())).append(',').append(' ');
    buf.append("associatedPermission").append('=').append(JodaBeanUtils.toString(getAssociatedPermission())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RoleSearchRequest}.
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
        this, "pagingRequest", RoleSearchRequest.class, PagingRequest.class);
    /**
     * The meta-property for the {@code objectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _objectIds = DirectMetaProperty.ofReadWrite(
        this, "objectIds", RoleSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code roleName} property.
     */
    private final MetaProperty<String> _roleName = DirectMetaProperty.ofReadWrite(
        this, "roleName", RoleSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code associatedRole} property.
     */
    private final MetaProperty<String> _associatedRole = DirectMetaProperty.ofReadWrite(
        this, "associatedRole", RoleSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code associatedUser} property.
     */
    private final MetaProperty<String> _associatedUser = DirectMetaProperty.ofReadWrite(
        this, "associatedUser", RoleSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code associatedPermission} property.
     */
    private final MetaProperty<String> _associatedPermission = DirectMetaProperty.ofReadWrite(
        this, "associatedPermission", RoleSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<RoleSearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", RoleSearchRequest.class, RoleSearchSortOrder.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "pagingRequest",
        "objectIds",
        "roleName",
        "associatedRole",
        "associatedUser",
        "associatedPermission",
        "sortOrder");

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
        case -1489617159:  // objectIds
          return _objectIds;
        case -266779615:  // roleName
          return _roleName;
        case 217765532:  // associatedRole
          return _associatedRole;
        case 217858545:  // associatedUser
          return _associatedUser;
        case -1203804299:  // associatedPermission
          return _associatedPermission;
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
     * The meta-property for the {@code pagingRequest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PagingRequest> pagingRequest() {
      return _pagingRequest;
    }

    /**
     * The meta-property for the {@code objectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> objectIds() {
      return _objectIds;
    }

    /**
     * The meta-property for the {@code roleName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> roleName() {
      return _roleName;
    }

    /**
     * The meta-property for the {@code associatedRole} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> associatedRole() {
      return _associatedRole;
    }

    /**
     * The meta-property for the {@code associatedUser} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> associatedUser() {
      return _associatedUser;
    }

    /**
     * The meta-property for the {@code associatedPermission} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> associatedPermission() {
      return _associatedPermission;
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
        case -2092032669:  // pagingRequest
          return ((RoleSearchRequest) bean).getPagingRequest();
        case -1489617159:  // objectIds
          return ((RoleSearchRequest) bean).getObjectIds();
        case -266779615:  // roleName
          return ((RoleSearchRequest) bean).getRoleName();
        case 217765532:  // associatedRole
          return ((RoleSearchRequest) bean).getAssociatedRole();
        case 217858545:  // associatedUser
          return ((RoleSearchRequest) bean).getAssociatedUser();
        case -1203804299:  // associatedPermission
          return ((RoleSearchRequest) bean).getAssociatedPermission();
        case -26774448:  // sortOrder
          return ((RoleSearchRequest) bean).getSortOrder();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2092032669:  // pagingRequest
          ((RoleSearchRequest) bean).setPagingRequest((PagingRequest) newValue);
          return;
        case -1489617159:  // objectIds
          ((RoleSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -266779615:  // roleName
          ((RoleSearchRequest) bean).setRoleName((String) newValue);
          return;
        case 217765532:  // associatedRole
          ((RoleSearchRequest) bean).setAssociatedRole((String) newValue);
          return;
        case 217858545:  // associatedUser
          ((RoleSearchRequest) bean).setAssociatedUser((String) newValue);
          return;
        case -1203804299:  // associatedPermission
          ((RoleSearchRequest) bean).setAssociatedPermission((String) newValue);
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
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
