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

import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.paging.PagingRequest;

/**
 * Request for searching for users. 
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link UserEventHistoryRequest} for more details on how history works.
 */
@BeanDefinition
public class UserSearchRequest implements Bean {

  /**
   * The request for paging.
   * By default all matching items will be returned.
   */
  @PropertyDefinition
  private PagingRequest _pagingRequest = PagingRequest.ALL;
  /**
   * The set of user object identifiers, null to not limit by user object identifiers.
   * Note that an empty list will return no users.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _objectIds;
  /**
   * The user name to search for, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _userName;
  /**
   * The alternate user identifier scheme, matching only against the <b>scheme</b> of the
   * identifiers, null not to match by identifier scheme.
   * This matches against the {@link ExternalId#getScheme() scheme} of the identifier
   * and does not match against the key. Wildcards are allowed.
   */
  @PropertyDefinition
  private String _alternateIdScheme;
  /**
   * The alternate user identifier value, matching only against the <b>value</b> of the
   * stored identifiers, null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   */
  @PropertyDefinition
  private String _alternateIdValue;
  /**
   * The associated permission to search for, no wildcards.
   * If used, only those roles which explicitly reference the permission are returned.
   * Any permissions implied by membership of roles are not matched.
   * In other words, this searches {@link ManageableUser#getAssociatedPermissions()}.
   */
  @PropertyDefinition
  private String _associatedPermission;
  /**
   * The primary email address to search for, wildcards allowed, null to not match on email.
   */
  @PropertyDefinition
  private String _emailAddress;
  /**
   * The display name to search for, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _displayName;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private UserSearchSortOrder _sortOrder = UserSearchSortOrder.NAME_ASC;

  /**
   * Creates an instance.
   */
  public UserSearchRequest() {
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
   * Checks if this search matches the specified user.
   *
   * @param user  the user to match, null returns false
   * @return true if matches
   */
  public boolean matches(ManageableUser user) {
    if (user == null) {
      return false;
    }
    if (getObjectIds() != null && getObjectIds().contains(user.getObjectId()) == false) {
      return false;
    }
    if (getUserName() != null && RegexUtils.wildcardMatch(getUserName(), user.getUserName()) == false) {
      return false;
    }
    if (getDisplayName() != null && RegexUtils.wildcardMatch(getDisplayName(), user.getProfile().getDisplayName()) == false) {
      return false;
    }
    if (getEmailAddress() != null && RegexUtils.wildcardMatch(getEmailAddress(), user.getEmailAddress()) == false) {
      return false;
    }
    if (user.getAssociatedPermissions().contains(getAssociatedPermission()) == false) {
      return false;
    }
    if (getAlternateIdValue() != null) {
      for (ExternalId identifier : user.getAlternateIds()) {
        if (RegexUtils.wildcardMatch(getAlternateIdValue(), identifier.getValue()) == false) {
          return false;
        }
      }
    }
    if (getAlternateIdScheme() != null) {
      for (ExternalId identifier : user.getAlternateIds()) {
        if (RegexUtils.wildcardMatch(getAlternateIdScheme(), identifier.getScheme().getName()) == false) {
          return false;
        }
      }
    }
    return true;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UserSearchRequest}.
   * @return the meta-bean, not null
   */
  public static UserSearchRequest.Meta meta() {
    return UserSearchRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(UserSearchRequest.Meta.INSTANCE);
  }

  @Override
  public UserSearchRequest.Meta metaBean() {
    return UserSearchRequest.Meta.INSTANCE;
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
   * Gets the set of user object identifiers, null to not limit by user object identifiers.
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
   * Gets the user name to search for, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getUserName() {
    return _userName;
  }

  /**
   * Sets the user name to search for, wildcards allowed, null to not match on name.
   * @param userName  the new value of the property
   */
  public void setUserName(String userName) {
    this._userName = userName;
  }

  /**
   * Gets the the {@code userName} property.
   * @return the property, not null
   */
  public final Property<String> userName() {
    return metaBean().userName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the alternate user identifier scheme, matching only against the <b>scheme</b> of the
   * identifiers, null not to match by identifier scheme.
   * This matches against the {@link ExternalId#getScheme() scheme} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * @return the value of the property
   */
  public String getAlternateIdScheme() {
    return _alternateIdScheme;
  }

  /**
   * Sets the alternate user identifier scheme, matching only against the <b>scheme</b> of the
   * identifiers, null not to match by identifier scheme.
   * This matches against the {@link ExternalId#getScheme() scheme} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * @param alternateIdScheme  the new value of the property
   */
  public void setAlternateIdScheme(String alternateIdScheme) {
    this._alternateIdScheme = alternateIdScheme;
  }

  /**
   * Gets the the {@code alternateIdScheme} property.
   * identifiers, null not to match by identifier scheme.
   * This matches against the {@link ExternalId#getScheme() scheme} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * @return the property, not null
   */
  public final Property<String> alternateIdScheme() {
    return metaBean().alternateIdScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the alternate user identifier value, matching only against the <b>value</b> of the
   * stored identifiers, null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * @return the value of the property
   */
  public String getAlternateIdValue() {
    return _alternateIdValue;
  }

  /**
   * Sets the alternate user identifier value, matching only against the <b>value</b> of the
   * stored identifiers, null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * @param alternateIdValue  the new value of the property
   */
  public void setAlternateIdValue(String alternateIdValue) {
    this._alternateIdValue = alternateIdValue;
  }

  /**
   * Gets the the {@code alternateIdValue} property.
   * stored identifiers, null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * @return the property, not null
   */
  public final Property<String> alternateIdValue() {
    return metaBean().alternateIdValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the associated permission to search for, no wildcards.
   * If used, only those roles which explicitly reference the permission are returned.
   * Any permissions implied by membership of roles are not matched.
   * In other words, this searches {@link ManageableUser#getAssociatedPermissions()}.
   * @return the value of the property
   */
  public String getAssociatedPermission() {
    return _associatedPermission;
  }

  /**
   * Sets the associated permission to search for, no wildcards.
   * If used, only those roles which explicitly reference the permission are returned.
   * Any permissions implied by membership of roles are not matched.
   * In other words, this searches {@link ManageableUser#getAssociatedPermissions()}.
   * @param associatedPermission  the new value of the property
   */
  public void setAssociatedPermission(String associatedPermission) {
    this._associatedPermission = associatedPermission;
  }

  /**
   * Gets the the {@code associatedPermission} property.
   * If used, only those roles which explicitly reference the permission are returned.
   * Any permissions implied by membership of roles are not matched.
   * In other words, this searches {@link ManageableUser#getAssociatedPermissions()}.
   * @return the property, not null
   */
  public final Property<String> associatedPermission() {
    return metaBean().associatedPermission().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the primary email address to search for, wildcards allowed, null to not match on email.
   * @return the value of the property
   */
  public String getEmailAddress() {
    return _emailAddress;
  }

  /**
   * Sets the primary email address to search for, wildcards allowed, null to not match on email.
   * @param emailAddress  the new value of the property
   */
  public void setEmailAddress(String emailAddress) {
    this._emailAddress = emailAddress;
  }

  /**
   * Gets the the {@code emailAddress} property.
   * @return the property, not null
   */
  public final Property<String> emailAddress() {
    return metaBean().emailAddress().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the display name to search for, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getDisplayName() {
    return _displayName;
  }

  /**
   * Sets the display name to search for, wildcards allowed, null to not match on name.
   * @param displayName  the new value of the property
   */
  public void setDisplayName(String displayName) {
    this._displayName = displayName;
  }

  /**
   * Gets the the {@code displayName} property.
   * @return the property, not null
   */
  public final Property<String> displayName() {
    return metaBean().displayName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sort order to use.
   * @return the value of the property, not null
   */
  public UserSearchSortOrder getSortOrder() {
    return _sortOrder;
  }

  /**
   * Sets the sort order to use.
   * @param sortOrder  the new value of the property, not null
   */
  public void setSortOrder(UserSearchSortOrder sortOrder) {
    JodaBeanUtils.notNull(sortOrder, "sortOrder");
    this._sortOrder = sortOrder;
  }

  /**
   * Gets the the {@code sortOrder} property.
   * @return the property, not null
   */
  public final Property<UserSearchSortOrder> sortOrder() {
    return metaBean().sortOrder().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public UserSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserSearchRequest other = (UserSearchRequest) obj;
      return JodaBeanUtils.equal(getPagingRequest(), other.getPagingRequest()) &&
          JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getUserName(), other.getUserName()) &&
          JodaBeanUtils.equal(getAlternateIdScheme(), other.getAlternateIdScheme()) &&
          JodaBeanUtils.equal(getAlternateIdValue(), other.getAlternateIdValue()) &&
          JodaBeanUtils.equal(getAssociatedPermission(), other.getAssociatedPermission()) &&
          JodaBeanUtils.equal(getEmailAddress(), other.getEmailAddress()) &&
          JodaBeanUtils.equal(getDisplayName(), other.getDisplayName()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getPagingRequest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAlternateIdScheme());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAlternateIdValue());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAssociatedPermission());
    hash = hash * 31 + JodaBeanUtils.hashCode(getEmailAddress());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDisplayName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("UserSearchRequest{");
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
    buf.append("userName").append('=').append(JodaBeanUtils.toString(getUserName())).append(',').append(' ');
    buf.append("alternateIdScheme").append('=').append(JodaBeanUtils.toString(getAlternateIdScheme())).append(',').append(' ');
    buf.append("alternateIdValue").append('=').append(JodaBeanUtils.toString(getAlternateIdValue())).append(',').append(' ');
    buf.append("associatedPermission").append('=').append(JodaBeanUtils.toString(getAssociatedPermission())).append(',').append(' ');
    buf.append("emailAddress").append('=').append(JodaBeanUtils.toString(getEmailAddress())).append(',').append(' ');
    buf.append("displayName").append('=').append(JodaBeanUtils.toString(getDisplayName())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserSearchRequest}.
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
        this, "pagingRequest", UserSearchRequest.class, PagingRequest.class);
    /**
     * The meta-property for the {@code objectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _objectIds = DirectMetaProperty.ofReadWrite(
        this, "objectIds", UserSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code userName} property.
     */
    private final MetaProperty<String> _userName = DirectMetaProperty.ofReadWrite(
        this, "userName", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code alternateIdScheme} property.
     */
    private final MetaProperty<String> _alternateIdScheme = DirectMetaProperty.ofReadWrite(
        this, "alternateIdScheme", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code alternateIdValue} property.
     */
    private final MetaProperty<String> _alternateIdValue = DirectMetaProperty.ofReadWrite(
        this, "alternateIdValue", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code associatedPermission} property.
     */
    private final MetaProperty<String> _associatedPermission = DirectMetaProperty.ofReadWrite(
        this, "associatedPermission", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code emailAddress} property.
     */
    private final MetaProperty<String> _emailAddress = DirectMetaProperty.ofReadWrite(
        this, "emailAddress", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code displayName} property.
     */
    private final MetaProperty<String> _displayName = DirectMetaProperty.ofReadWrite(
        this, "displayName", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<UserSearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", UserSearchRequest.class, UserSearchSortOrder.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "pagingRequest",
        "objectIds",
        "userName",
        "alternateIdScheme",
        "alternateIdValue",
        "associatedPermission",
        "emailAddress",
        "displayName",
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
        case -266666762:  // userName
          return _userName;
        case -1982121670:  // alternateIdScheme
          return _alternateIdScheme;
        case -1169602756:  // alternateIdValue
          return _alternateIdValue;
        case -1203804299:  // associatedPermission
          return _associatedPermission;
        case -1070931784:  // emailAddress
          return _emailAddress;
        case 1714148973:  // displayName
          return _displayName;
        case -26774448:  // sortOrder
          return _sortOrder;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends UserSearchRequest> builder() {
      return new DirectBeanBuilder<UserSearchRequest>(new UserSearchRequest());
    }

    @Override
    public Class<? extends UserSearchRequest> beanType() {
      return UserSearchRequest.class;
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
     * The meta-property for the {@code userName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userName() {
      return _userName;
    }

    /**
     * The meta-property for the {@code alternateIdScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> alternateIdScheme() {
      return _alternateIdScheme;
    }

    /**
     * The meta-property for the {@code alternateIdValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> alternateIdValue() {
      return _alternateIdValue;
    }

    /**
     * The meta-property for the {@code associatedPermission} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> associatedPermission() {
      return _associatedPermission;
    }

    /**
     * The meta-property for the {@code emailAddress} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> emailAddress() {
      return _emailAddress;
    }

    /**
     * The meta-property for the {@code displayName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> displayName() {
      return _displayName;
    }

    /**
     * The meta-property for the {@code sortOrder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UserSearchSortOrder> sortOrder() {
      return _sortOrder;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2092032669:  // pagingRequest
          return ((UserSearchRequest) bean).getPagingRequest();
        case -1489617159:  // objectIds
          return ((UserSearchRequest) bean).getObjectIds();
        case -266666762:  // userName
          return ((UserSearchRequest) bean).getUserName();
        case -1982121670:  // alternateIdScheme
          return ((UserSearchRequest) bean).getAlternateIdScheme();
        case -1169602756:  // alternateIdValue
          return ((UserSearchRequest) bean).getAlternateIdValue();
        case -1203804299:  // associatedPermission
          return ((UserSearchRequest) bean).getAssociatedPermission();
        case -1070931784:  // emailAddress
          return ((UserSearchRequest) bean).getEmailAddress();
        case 1714148973:  // displayName
          return ((UserSearchRequest) bean).getDisplayName();
        case -26774448:  // sortOrder
          return ((UserSearchRequest) bean).getSortOrder();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2092032669:  // pagingRequest
          ((UserSearchRequest) bean).setPagingRequest((PagingRequest) newValue);
          return;
        case -1489617159:  // objectIds
          ((UserSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -266666762:  // userName
          ((UserSearchRequest) bean).setUserName((String) newValue);
          return;
        case -1982121670:  // alternateIdScheme
          ((UserSearchRequest) bean).setAlternateIdScheme((String) newValue);
          return;
        case -1169602756:  // alternateIdValue
          ((UserSearchRequest) bean).setAlternateIdValue((String) newValue);
          return;
        case -1203804299:  // associatedPermission
          ((UserSearchRequest) bean).setAssociatedPermission((String) newValue);
          return;
        case -1070931784:  // emailAddress
          ((UserSearchRequest) bean).setEmailAddress((String) newValue);
          return;
        case 1714148973:  // displayName
          ((UserSearchRequest) bean).setDisplayName((String) newValue);
          return;
        case -26774448:  // sortOrder
          ((UserSearchRequest) bean).setSortOrder((UserSearchSortOrder) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((UserSearchRequest) bean)._sortOrder, "sortOrder");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
