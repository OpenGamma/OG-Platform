/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.UserAccountStatus;
import com.opengamma.core.user.impl.SimpleUserProfile;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * A user stored within a {@code UserMaster}.
 * <p>
 * This class represents the persisted form of a user.
 * It operates in association with {@link UserMaster}.
 * The related {@link ManageableRole} and {@link RoleMaster} store groups of users.
 * <p>
 * A {@link UserAccount} represents a flattened form of user and role information.
 * As such, this class does not implement {@code UserAccount}.
 */
@BeanDefinition
public class ManageableUser
    implements Bean, UniqueIdentifiable, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of this user.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The user name that uniquely identifies this user.
   * This is used with the password to authenticate.
   */
  @PropertyDefinition(validate = "notEmpty")
  private String _userName;
  /**
   * The hashed version of the user password.
   * May be null or empty, particularly if the user is disabled.
   */
  @PropertyDefinition
  private String _passwordHash;
  /**
   * The account status, determining if the user is allowed to login.
   */
  @PropertyDefinition
  private UserAccountStatus _status = UserAccountStatus.ENABLED;
  /**
   * The bundle of alternate user identifiers.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalIdBundle _alternateIds = ExternalIdBundle.EMPTY;
  /**
   * The set of permissions associated directly with this user.
   * Permissions are used to define access control.
   * In a typical environment, permissions are controlled using {@linkplain RoleMaster roles}.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<String> _associatedPermissions = new TreeSet<>();
  /**
   * The primary email address associated with this user.
   */
  @PropertyDefinition
  private String _emailAddress;
  /**
   * The user profile, containing user settings.
   */
  @PropertyDefinition(validate = "notNull")
  private SimpleUserProfile _profile = new SimpleUserProfile();

  //-------------------------------------------------------------------------
  /**
   * Creates a {@code ManageableUserAccount} from a {@code UserAccount}.
   * <p>
   * Permissions are not copied. This is because the permissions of a
   * {@code UserAccount} are the complete set, including any from roles, whereas
   * the permissions on a {@code ManageableUserAccount} are user-level only.
   * 
   * @param accountToCopy  the account to copy, not null
   * @return the new account, not null
   */
  public static ManageableUser from(UserAccount accountToCopy) {
    ArgumentChecker.notNull(accountToCopy, "accountToCopy");
    ManageableUser copy = new ManageableUser(accountToCopy.getUserName());
    if (accountToCopy instanceof UniqueIdentifiable) {
      copy.setUniqueId(((UniqueIdentifiable) accountToCopy).getUniqueId());
    }
    copy.setPasswordHash(accountToCopy.getPasswordHash());
    copy.setStatus(accountToCopy.getStatus());
    copy.setAlternateIds(accountToCopy.getAlternateIds());
    copy.setEmailAddress(accountToCopy.getEmailAddress());
    copy.setProfile(SimpleUserProfile.from(accountToCopy.getProfile()));
    return copy;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a user.
   */
  protected ManageableUser() {
  }

  /**
   * Creates a user, setting the user name.
   * 
   * @param userName  the user name, not null
   */
  public ManageableUser(String userName) {
    setUserName(userName);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the object identifier.
   * 
   * @return the object identifier, null if not set
   */
  public ObjectId getObjectId() {
    return (getUniqueId() != null ? getUniqueId().getObjectId() : null);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an alternate user identifier to the bundle representing this user.
   * 
   * @param alternateId  the identifier to add, not null
   */
  public void addAlternateId(ExternalId alternateId) {
    setAlternateIds(getAlternateIds().withExternalId(alternateId));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableUser}.
   * @return the meta-bean, not null
   */
  public static ManageableUser.Meta meta() {
    return ManageableUser.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ManageableUser.Meta.INSTANCE);
  }

  @Override
  public ManageableUser.Meta metaBean() {
    return ManageableUser.Meta.INSTANCE;
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
   * Gets the unique identifier of this user.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of this user.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user name that uniquely identifies this user.
   * This is used with the password to authenticate.
   * @return the value of the property, not empty
   */
  public String getUserName() {
    return _userName;
  }

  /**
   * Sets the user name that uniquely identifies this user.
   * This is used with the password to authenticate.
   * @param userName  the new value of the property, not empty
   */
  public void setUserName(String userName) {
    JodaBeanUtils.notEmpty(userName, "userName");
    this._userName = userName;
  }

  /**
   * Gets the the {@code userName} property.
   * This is used with the password to authenticate.
   * @return the property, not null
   */
  public final Property<String> userName() {
    return metaBean().userName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the hashed version of the user password.
   * May be null or empty, particularly if the user is disabled.
   * @return the value of the property
   */
  public String getPasswordHash() {
    return _passwordHash;
  }

  /**
   * Sets the hashed version of the user password.
   * May be null or empty, particularly if the user is disabled.
   * @param passwordHash  the new value of the property
   */
  public void setPasswordHash(String passwordHash) {
    this._passwordHash = passwordHash;
  }

  /**
   * Gets the the {@code passwordHash} property.
   * May be null or empty, particularly if the user is disabled.
   * @return the property, not null
   */
  public final Property<String> passwordHash() {
    return metaBean().passwordHash().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the account status, determining if the user is allowed to login.
   * @return the value of the property
   */
  public UserAccountStatus getStatus() {
    return _status;
  }

  /**
   * Sets the account status, determining if the user is allowed to login.
   * @param status  the new value of the property
   */
  public void setStatus(UserAccountStatus status) {
    this._status = status;
  }

  /**
   * Gets the the {@code status} property.
   * @return the property, not null
   */
  public final Property<UserAccountStatus> status() {
    return metaBean().status().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bundle of alternate user identifiers.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   * @return the value of the property, not null
   */
  public ExternalIdBundle getAlternateIds() {
    return _alternateIds;
  }

  /**
   * Sets the bundle of alternate user identifiers.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   * @param alternateIds  the new value of the property, not null
   */
  public void setAlternateIds(ExternalIdBundle alternateIds) {
    JodaBeanUtils.notNull(alternateIds, "alternateIds");
    this._alternateIds = alternateIds;
  }

  /**
   * Gets the the {@code alternateIds} property.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   * @return the property, not null
   */
  public final Property<ExternalIdBundle> alternateIds() {
    return metaBean().alternateIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of permissions associated directly with this user.
   * Permissions are used to define access control.
   * In a typical environment, permissions are controlled using {@linkplain RoleMaster roles}.
   * @return the value of the property, not null
   */
  public Set<String> getAssociatedPermissions() {
    return _associatedPermissions;
  }

  /**
   * Sets the set of permissions associated directly with this user.
   * Permissions are used to define access control.
   * In a typical environment, permissions are controlled using {@linkplain RoleMaster roles}.
   * @param associatedPermissions  the new value of the property, not null
   */
  public void setAssociatedPermissions(Set<String> associatedPermissions) {
    JodaBeanUtils.notNull(associatedPermissions, "associatedPermissions");
    this._associatedPermissions.clear();
    this._associatedPermissions.addAll(associatedPermissions);
  }

  /**
   * Gets the the {@code associatedPermissions} property.
   * Permissions are used to define access control.
   * In a typical environment, permissions are controlled using {@linkplain RoleMaster roles}.
   * @return the property, not null
   */
  public final Property<Set<String>> associatedPermissions() {
    return metaBean().associatedPermissions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the primary email address associated with this user.
   * @return the value of the property
   */
  public String getEmailAddress() {
    return _emailAddress;
  }

  /**
   * Sets the primary email address associated with this user.
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
   * Gets the user profile, containing user settings.
   * @return the value of the property, not null
   */
  public SimpleUserProfile getProfile() {
    return _profile;
  }

  /**
   * Sets the user profile, containing user settings.
   * @param profile  the new value of the property, not null
   */
  public void setProfile(SimpleUserProfile profile) {
    JodaBeanUtils.notNull(profile, "profile");
    this._profile = profile;
  }

  /**
   * Gets the the {@code profile} property.
   * @return the property, not null
   */
  public final Property<SimpleUserProfile> profile() {
    return metaBean().profile().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ManageableUser clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableUser other = (ManageableUser) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getUserName(), other.getUserName()) &&
          JodaBeanUtils.equal(getPasswordHash(), other.getPasswordHash()) &&
          JodaBeanUtils.equal(getStatus(), other.getStatus()) &&
          JodaBeanUtils.equal(getAlternateIds(), other.getAlternateIds()) &&
          JodaBeanUtils.equal(getAssociatedPermissions(), other.getAssociatedPermissions()) &&
          JodaBeanUtils.equal(getEmailAddress(), other.getEmailAddress()) &&
          JodaBeanUtils.equal(getProfile(), other.getProfile());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPasswordHash());
    hash = hash * 31 + JodaBeanUtils.hashCode(getStatus());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAlternateIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAssociatedPermissions());
    hash = hash * 31 + JodaBeanUtils.hashCode(getEmailAddress());
    hash = hash * 31 + JodaBeanUtils.hashCode(getProfile());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("ManageableUser{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("userName").append('=').append(JodaBeanUtils.toString(getUserName())).append(',').append(' ');
    buf.append("passwordHash").append('=').append(JodaBeanUtils.toString(getPasswordHash())).append(',').append(' ');
    buf.append("status").append('=').append(JodaBeanUtils.toString(getStatus())).append(',').append(' ');
    buf.append("alternateIds").append('=').append(JodaBeanUtils.toString(getAlternateIds())).append(',').append(' ');
    buf.append("associatedPermissions").append('=').append(JodaBeanUtils.toString(getAssociatedPermissions())).append(',').append(' ');
    buf.append("emailAddress").append('=').append(JodaBeanUtils.toString(getEmailAddress())).append(',').append(' ');
    buf.append("profile").append('=').append(JodaBeanUtils.toString(getProfile())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableUser}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ManageableUser.class, UniqueId.class);
    /**
     * The meta-property for the {@code userName} property.
     */
    private final MetaProperty<String> _userName = DirectMetaProperty.ofReadWrite(
        this, "userName", ManageableUser.class, String.class);
    /**
     * The meta-property for the {@code passwordHash} property.
     */
    private final MetaProperty<String> _passwordHash = DirectMetaProperty.ofReadWrite(
        this, "passwordHash", ManageableUser.class, String.class);
    /**
     * The meta-property for the {@code status} property.
     */
    private final MetaProperty<UserAccountStatus> _status = DirectMetaProperty.ofReadWrite(
        this, "status", ManageableUser.class, UserAccountStatus.class);
    /**
     * The meta-property for the {@code alternateIds} property.
     */
    private final MetaProperty<ExternalIdBundle> _alternateIds = DirectMetaProperty.ofReadWrite(
        this, "alternateIds", ManageableUser.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code associatedPermissions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _associatedPermissions = DirectMetaProperty.ofReadWrite(
        this, "associatedPermissions", ManageableUser.class, (Class) Set.class);
    /**
     * The meta-property for the {@code emailAddress} property.
     */
    private final MetaProperty<String> _emailAddress = DirectMetaProperty.ofReadWrite(
        this, "emailAddress", ManageableUser.class, String.class);
    /**
     * The meta-property for the {@code profile} property.
     */
    private final MetaProperty<SimpleUserProfile> _profile = DirectMetaProperty.ofReadWrite(
        this, "profile", ManageableUser.class, SimpleUserProfile.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "userName",
        "passwordHash",
        "status",
        "alternateIds",
        "associatedPermissions",
        "emailAddress",
        "profile");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case -266666762:  // userName
          return _userName;
        case 566700617:  // passwordHash
          return _passwordHash;
        case -892481550:  // status
          return _status;
        case -1805823010:  // alternateIds
          return _alternateIds;
        case 1336772510:  // associatedPermissions
          return _associatedPermissions;
        case -1070931784:  // emailAddress
          return _emailAddress;
        case -309425751:  // profile
          return _profile;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableUser> builder() {
      return new DirectBeanBuilder<ManageableUser>(new ManageableUser());
    }

    @Override
    public Class<? extends ManageableUser> beanType() {
      return ManageableUser.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code userName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userName() {
      return _userName;
    }

    /**
     * The meta-property for the {@code passwordHash} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> passwordHash() {
      return _passwordHash;
    }

    /**
     * The meta-property for the {@code status} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UserAccountStatus> status() {
      return _status;
    }

    /**
     * The meta-property for the {@code alternateIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBundle> alternateIds() {
      return _alternateIds;
    }

    /**
     * The meta-property for the {@code associatedPermissions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> associatedPermissions() {
      return _associatedPermissions;
    }

    /**
     * The meta-property for the {@code emailAddress} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> emailAddress() {
      return _emailAddress;
    }

    /**
     * The meta-property for the {@code profile} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SimpleUserProfile> profile() {
      return _profile;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ManageableUser) bean).getUniqueId();
        case -266666762:  // userName
          return ((ManageableUser) bean).getUserName();
        case 566700617:  // passwordHash
          return ((ManageableUser) bean).getPasswordHash();
        case -892481550:  // status
          return ((ManageableUser) bean).getStatus();
        case -1805823010:  // alternateIds
          return ((ManageableUser) bean).getAlternateIds();
        case 1336772510:  // associatedPermissions
          return ((ManageableUser) bean).getAssociatedPermissions();
        case -1070931784:  // emailAddress
          return ((ManageableUser) bean).getEmailAddress();
        case -309425751:  // profile
          return ((ManageableUser) bean).getProfile();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ManageableUser) bean).setUniqueId((UniqueId) newValue);
          return;
        case -266666762:  // userName
          ((ManageableUser) bean).setUserName((String) newValue);
          return;
        case 566700617:  // passwordHash
          ((ManageableUser) bean).setPasswordHash((String) newValue);
          return;
        case -892481550:  // status
          ((ManageableUser) bean).setStatus((UserAccountStatus) newValue);
          return;
        case -1805823010:  // alternateIds
          ((ManageableUser) bean).setAlternateIds((ExternalIdBundle) newValue);
          return;
        case 1336772510:  // associatedPermissions
          ((ManageableUser) bean).setAssociatedPermissions((Set<String>) newValue);
          return;
        case -1070931784:  // emailAddress
          ((ManageableUser) bean).setEmailAddress((String) newValue);
          return;
        case -309425751:  // profile
          ((ManageableUser) bean).setProfile((SimpleUserProfile) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notEmpty(((ManageableUser) bean)._userName, "userName");
      JodaBeanUtils.notNull(((ManageableUser) bean)._alternateIds, "alternateIds");
      JodaBeanUtils.notNull(((ManageableUser) bean)._associatedPermissions, "associatedPermissions");
      JodaBeanUtils.notNull(((ManageableUser) bean)._profile, "profile");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
