/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;

/**
 * Request for searching for users. 
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link UserHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class UserSearchRequest extends AbstractSearchRequest {

  /**
   * The set of user object identifiers, null to not limit by user object identifiers.
   * Note that an empty list will return no users.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _objectIds;
  /**
   * The external user identifiers to match, null to not match on user identifiers.
   */
  @PropertyDefinition
  private ExternalIdSearch _externalIdSearch;
  /**
   * The external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   */
  @PropertyDefinition
  private String _externalIdValue;
  /**
   * The external identifier scheme, matching against the <b>scheme</b> of the identifiers,
   * null not to match by identifier scheme. Wildcards are allowed.
   */
  @PropertyDefinition
  private String _externalIdScheme;
  /**
   * The user id to search for, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _userId;
  /**
   * The display user name to search for, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The time zone to search for, wildcards allowed, null to not match on time-zone.
   */
  @PropertyDefinition
  private String _timeZone;
  /**
   * The primary email address to search for, wildcards allowed, null to not match on email.
   */
  @PropertyDefinition
  private String _emailAddress;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private UserSearchSortOrder _sortOrder = UserSearchSortOrder.OBJECT_ID_ASC;

  /**
   * Creates an instance.
   */
  public UserSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param userId  the external user identifier to search for, not null
   */
  public UserSearchRequest(ExternalId userId) {
    addExternalId(userId);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param userIdBundle  the external user identifiers to search for, not null
   */
  public UserSearchRequest(ExternalIdBundle userIdBundle) {
    addExternalIds(userIdBundle);
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
   * Adds a single external user identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param externalUserId  the external user identifier to add, not null
   */
  public void addExternalId(ExternalId externalUserId) {
    ArgumentChecker.notNull(externalUserId, "externalUserId");
    addExternalIds(Arrays.asList(externalUserId));
  }

  /**
   * Adds a collection of external user identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param externalUserIds  the external user identifiers to add, not null
   */
  public void addExternalIds(ExternalId... externalUserIds) {
    ArgumentChecker.notNull(externalUserIds, "externalUserIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(externalUserIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(externalUserIds));
    }
  }

  /**
   * Adds a collection of external user identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param externalUserIds  the user key identifiers to add, not null
   */
  public void addExternalIds(Iterable<ExternalId> externalUserIds) {
    ArgumentChecker.notNull(externalUserIds, "externalUserIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(externalUserIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(externalUserIds));
    }
  }

  /**
   * Sets the search type to use in {@code ExternalIdSearch}.
   * 
   * @param type  the type to set, not null
   */
  public void setExternalIdSearchType(ExternalIdSearchType type) {
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(type));
    } else {
      setExternalIdSearch(getExternalIdSearch().withSearchType(type));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(AbstractDocument obj) {
    if (obj instanceof UserDocument == false) {
      return false;
    }    
    UserDocument document = (UserDocument) obj;
    ManageableOGUser user = document.getUser();
    if (getObjectIds() != null && getObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getExternalIdSearch() != null && getExternalIdSearch().matches(user.getExternalIdBundle()) == false) {
      return false;
    }
    if (getUserId() != null && RegexUtils.wildcardMatch(getUserId(), user.getUserId()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), user.getName()) == false) {
      return false;
    }
    if (getTimeZone() != null && RegexUtils.wildcardMatch(getTimeZone(), (user.getTimeZone() != null ? user.getTimeZone().getId() : null)) == false) {
      return false;
    }
    if (getEmailAddress() != null && RegexUtils.wildcardMatch(getEmailAddress(), user.getEmailAddress()) == false) {
      return false;
    }
    if (getExternalIdValue() != null) {
      for (ExternalId identifier : user.getExternalIdBundle()) {
        if (RegexUtils.wildcardMatch(getExternalIdValue(), identifier.getValue()) == false) {
          return false;
        }
      }
    }
    if (getExternalIdScheme() != null) {
      for (ExternalId identifier : user.getExternalIdBundle()) {
        if (RegexUtils.wildcardMatch(getExternalIdScheme(), identifier.getScheme().getName()) == false) {
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
   * Gets the external user identifiers to match, null to not match on user identifiers.
   * @return the value of the property
   */
  public ExternalIdSearch getExternalIdSearch() {
    return _externalIdSearch;
  }

  /**
   * Sets the external user identifiers to match, null to not match on user identifiers.
   * @param externalIdSearch  the new value of the property
   */
  public void setExternalIdSearch(ExternalIdSearch externalIdSearch) {
    this._externalIdSearch = externalIdSearch;
  }

  /**
   * Gets the the {@code externalIdSearch} property.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> externalIdSearch() {
    return metaBean().externalIdSearch().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the value of the property
   */
  public String getExternalIdValue() {
    return _externalIdValue;
  }

  /**
   * Sets the external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @param externalIdValue  the new value of the property
   */
  public void setExternalIdValue(String externalIdValue) {
    this._externalIdValue = externalIdValue;
  }

  /**
   * Gets the the {@code externalIdValue} property.
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the property, not null
   */
  public final Property<String> externalIdValue() {
    return metaBean().externalIdValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier scheme, matching against the <b>scheme</b> of the identifiers,
   * null not to match by identifier scheme. Wildcards are allowed.
   * @return the value of the property
   */
  public String getExternalIdScheme() {
    return _externalIdScheme;
  }

  /**
   * Sets the external identifier scheme, matching against the <b>scheme</b> of the identifiers,
   * null not to match by identifier scheme. Wildcards are allowed.
   * @param externalIdScheme  the new value of the property
   */
  public void setExternalIdScheme(String externalIdScheme) {
    this._externalIdScheme = externalIdScheme;
  }

  /**
   * Gets the the {@code externalIdScheme} property.
   * null not to match by identifier scheme. Wildcards are allowed.
   * @return the property, not null
   */
  public final Property<String> externalIdScheme() {
    return metaBean().externalIdScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user id to search for, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getUserId() {
    return _userId;
  }

  /**
   * Sets the user id to search for, wildcards allowed, null to not match on name.
   * @param userId  the new value of the property
   */
  public void setUserId(String userId) {
    this._userId = userId;
  }

  /**
   * Gets the the {@code userId} property.
   * @return the property, not null
   */
  public final Property<String> userId() {
    return metaBean().userId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the display user name to search for, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the display user name to search for, wildcards allowed, null to not match on name.
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
   * Gets the time zone to search for, wildcards allowed, null to not match on time-zone.
   * @return the value of the property
   */
  public String getTimeZone() {
    return _timeZone;
  }

  /**
   * Sets the time zone to search for, wildcards allowed, null to not match on time-zone.
   * @param timeZone  the new value of the property
   */
  public void setTimeZone(String timeZone) {
    this._timeZone = timeZone;
  }

  /**
   * Gets the the {@code timeZone} property.
   * @return the property, not null
   */
  public final Property<String> timeZone() {
    return metaBean().timeZone().createProperty(this);
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
    return (UserSearchRequest) super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserSearchRequest other = (UserSearchRequest) obj;
      return JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getExternalIdSearch(), other.getExternalIdSearch()) &&
          JodaBeanUtils.equal(getExternalIdValue(), other.getExternalIdValue()) &&
          JodaBeanUtils.equal(getExternalIdScheme(), other.getExternalIdScheme()) &&
          JodaBeanUtils.equal(getUserId(), other.getUserId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getTimeZone(), other.getTimeZone()) &&
          JodaBeanUtils.equal(getEmailAddress(), other.getEmailAddress()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdSearch());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdValue());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdScheme());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTimeZone());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEmailAddress());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    return hash ^ super.hashCode();
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

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("objectIds").append('=').append(JodaBeanUtils.toString(getObjectIds())).append(',').append(' ');
    buf.append("externalIdSearch").append('=').append(JodaBeanUtils.toString(getExternalIdSearch())).append(',').append(' ');
    buf.append("externalIdValue").append('=').append(JodaBeanUtils.toString(getExternalIdValue())).append(',').append(' ');
    buf.append("externalIdScheme").append('=').append(JodaBeanUtils.toString(getExternalIdScheme())).append(',').append(' ');
    buf.append("userId").append('=').append(JodaBeanUtils.toString(getUserId())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("timeZone").append('=').append(JodaBeanUtils.toString(getTimeZone())).append(',').append(' ');
    buf.append("emailAddress").append('=').append(JodaBeanUtils.toString(getEmailAddress())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserSearchRequest}.
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
        this, "objectIds", UserSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code externalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _externalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "externalIdSearch", UserSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code externalIdValue} property.
     */
    private final MetaProperty<String> _externalIdValue = DirectMetaProperty.ofReadWrite(
        this, "externalIdValue", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code externalIdScheme} property.
     */
    private final MetaProperty<String> _externalIdScheme = DirectMetaProperty.ofReadWrite(
        this, "externalIdScheme", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code userId} property.
     */
    private final MetaProperty<String> _userId = DirectMetaProperty.ofReadWrite(
        this, "userId", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code timeZone} property.
     */
    private final MetaProperty<String> _timeZone = DirectMetaProperty.ofReadWrite(
        this, "timeZone", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code emailAddress} property.
     */
    private final MetaProperty<String> _emailAddress = DirectMetaProperty.ofReadWrite(
        this, "emailAddress", UserSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<UserSearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", UserSearchRequest.class, UserSearchSortOrder.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "objectIds",
        "externalIdSearch",
        "externalIdValue",
        "externalIdScheme",
        "userId",
        "name",
        "timeZone",
        "emailAddress",
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
        case -265376882:  // externalIdSearch
          return _externalIdSearch;
        case 2072311499:  // externalIdValue
          return _externalIdValue;
        case -267027573:  // externalIdScheme
          return _externalIdScheme;
        case -836030906:  // userId
          return _userId;
        case 3373707:  // name
          return _name;
        case -2077180903:  // timeZone
          return _timeZone;
        case -1070931784:  // emailAddress
          return _emailAddress;
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
     * The meta-property for the {@code objectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> objectIds() {
      return _objectIds;
    }

    /**
     * The meta-property for the {@code externalIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> externalIdSearch() {
      return _externalIdSearch;
    }

    /**
     * The meta-property for the {@code externalIdValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> externalIdValue() {
      return _externalIdValue;
    }

    /**
     * The meta-property for the {@code externalIdScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> externalIdScheme() {
      return _externalIdScheme;
    }

    /**
     * The meta-property for the {@code userId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userId() {
      return _userId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code timeZone} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> timeZone() {
      return _timeZone;
    }

    /**
     * The meta-property for the {@code emailAddress} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> emailAddress() {
      return _emailAddress;
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
        case -1489617159:  // objectIds
          return ((UserSearchRequest) bean).getObjectIds();
        case -265376882:  // externalIdSearch
          return ((UserSearchRequest) bean).getExternalIdSearch();
        case 2072311499:  // externalIdValue
          return ((UserSearchRequest) bean).getExternalIdValue();
        case -267027573:  // externalIdScheme
          return ((UserSearchRequest) bean).getExternalIdScheme();
        case -836030906:  // userId
          return ((UserSearchRequest) bean).getUserId();
        case 3373707:  // name
          return ((UserSearchRequest) bean).getName();
        case -2077180903:  // timeZone
          return ((UserSearchRequest) bean).getTimeZone();
        case -1070931784:  // emailAddress
          return ((UserSearchRequest) bean).getEmailAddress();
        case -26774448:  // sortOrder
          return ((UserSearchRequest) bean).getSortOrder();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          ((UserSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -265376882:  // externalIdSearch
          ((UserSearchRequest) bean).setExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 2072311499:  // externalIdValue
          ((UserSearchRequest) bean).setExternalIdValue((String) newValue);
          return;
        case -267027573:  // externalIdScheme
          ((UserSearchRequest) bean).setExternalIdScheme((String) newValue);
          return;
        case -836030906:  // userId
          ((UserSearchRequest) bean).setUserId((String) newValue);
          return;
        case 3373707:  // name
          ((UserSearchRequest) bean).setName((String) newValue);
          return;
        case -2077180903:  // timeZone
          ((UserSearchRequest) bean).setTimeZone((String) newValue);
          return;
        case -1070931784:  // emailAddress
          ((UserSearchRequest) bean).setEmailAddress((String) newValue);
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
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
