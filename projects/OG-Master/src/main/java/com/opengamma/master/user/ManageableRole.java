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

import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * A role within the user management system.
 * <p>
 * A role is used to manage groups of users and their permissions.
 * The model is simple and flexible.
 * A role associates a set of users with a set of permissions.
 * Roles can be built up into hierarchies by including other roles.
 * <p>
 * One approach is to have two types of role - "groups" and "permission-based roles".
 * Groups would contain a set of users, but no permissions.
 * Permission-based roles would contain a set of permissions, but no users.
 * Groups would specify the roles which they are associated to, effectively linking
 * the permissions to users.
 */
@BeanDefinition
public class ManageableRole
    implements Bean, UniqueIdentifiable, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of this role.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The name that uniquely identifies this role.
   */
  @PropertyDefinition(validate = "notNull")
  private String _roleName;
  /**
   * The description of this role.
   */
  @PropertyDefinition(validate = "notNull")
  private String _description = "";
  /**
   * The set of other roles that are associated directly with this role.
   * When fully resolved, the complete set of users and permissions for this
   * role will include those of these associated roles.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<String> _associatedRoles = new TreeSet<>();
  /**
   * The set of users that are associated directly with this role.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<String> _associatedUsers = new TreeSet<>();
  /**
   * The set of permissions associated directly with this role.
   * Permissions are used to define access control.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<String> _associatedPermissions = new TreeSet<>();

  /**
   * Creates a user.
   */
  protected ManageableRole() {
  }

  /**
   * Creates a user, setting the user name.
   * 
   * @param roleName  the role name, not null
   */
  public ManageableRole(String roleName) {
    setRoleName(roleName);
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

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableRole}.
   * @return the meta-bean, not null
   */
  public static ManageableRole.Meta meta() {
    return ManageableRole.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ManageableRole.Meta.INSTANCE);
  }

  @Override
  public ManageableRole.Meta metaBean() {
    return ManageableRole.Meta.INSTANCE;
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
   * Gets the unique identifier of this role.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of this role.
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
   * Gets the name that uniquely identifies this role.
   * @return the value of the property, not null
   */
  public String getRoleName() {
    return _roleName;
  }

  /**
   * Sets the name that uniquely identifies this role.
   * @param roleName  the new value of the property, not null
   */
  public void setRoleName(String roleName) {
    JodaBeanUtils.notNull(roleName, "roleName");
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
   * Gets the description of this role.
   * @return the value of the property, not null
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Sets the description of this role.
   * @param description  the new value of the property, not null
   */
  public void setDescription(String description) {
    JodaBeanUtils.notNull(description, "description");
    this._description = description;
  }

  /**
   * Gets the the {@code description} property.
   * @return the property, not null
   */
  public final Property<String> description() {
    return metaBean().description().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of other roles that are associated directly with this role.
   * When fully resolved, the complete set of users and permissions for this
   * role will include those of these associated roles.
   * @return the value of the property, not null
   */
  public Set<String> getAssociatedRoles() {
    return _associatedRoles;
  }

  /**
   * Sets the set of other roles that are associated directly with this role.
   * When fully resolved, the complete set of users and permissions for this
   * role will include those of these associated roles.
   * @param associatedRoles  the new value of the property, not null
   */
  public void setAssociatedRoles(Set<String> associatedRoles) {
    JodaBeanUtils.notNull(associatedRoles, "associatedRoles");
    this._associatedRoles.clear();
    this._associatedRoles.addAll(associatedRoles);
  }

  /**
   * Gets the the {@code associatedRoles} property.
   * When fully resolved, the complete set of users and permissions for this
   * role will include those of these associated roles.
   * @return the property, not null
   */
  public final Property<Set<String>> associatedRoles() {
    return metaBean().associatedRoles().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of users that are associated directly with this role.
   * @return the value of the property, not null
   */
  public Set<String> getAssociatedUsers() {
    return _associatedUsers;
  }

  /**
   * Sets the set of users that are associated directly with this role.
   * @param associatedUsers  the new value of the property, not null
   */
  public void setAssociatedUsers(Set<String> associatedUsers) {
    JodaBeanUtils.notNull(associatedUsers, "associatedUsers");
    this._associatedUsers.clear();
    this._associatedUsers.addAll(associatedUsers);
  }

  /**
   * Gets the the {@code associatedUsers} property.
   * @return the property, not null
   */
  public final Property<Set<String>> associatedUsers() {
    return metaBean().associatedUsers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of permissions associated directly with this role.
   * Permissions are used to define access control.
   * @return the value of the property, not null
   */
  public Set<String> getAssociatedPermissions() {
    return _associatedPermissions;
  }

  /**
   * Sets the set of permissions associated directly with this role.
   * Permissions are used to define access control.
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
   * @return the property, not null
   */
  public final Property<Set<String>> associatedPermissions() {
    return metaBean().associatedPermissions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ManageableRole clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableRole other = (ManageableRole) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getRoleName(), other.getRoleName()) &&
          JodaBeanUtils.equal(getDescription(), other.getDescription()) &&
          JodaBeanUtils.equal(getAssociatedRoles(), other.getAssociatedRoles()) &&
          JodaBeanUtils.equal(getAssociatedUsers(), other.getAssociatedUsers()) &&
          JodaBeanUtils.equal(getAssociatedPermissions(), other.getAssociatedPermissions());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRoleName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDescription());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAssociatedRoles());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAssociatedUsers());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAssociatedPermissions());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("ManageableRole{");
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
    buf.append("roleName").append('=').append(JodaBeanUtils.toString(getRoleName())).append(',').append(' ');
    buf.append("description").append('=').append(JodaBeanUtils.toString(getDescription())).append(',').append(' ');
    buf.append("associatedRoles").append('=').append(JodaBeanUtils.toString(getAssociatedRoles())).append(',').append(' ');
    buf.append("associatedUsers").append('=').append(JodaBeanUtils.toString(getAssociatedUsers())).append(',').append(' ');
    buf.append("associatedPermissions").append('=').append(JodaBeanUtils.toString(getAssociatedPermissions())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableRole}.
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
        this, "uniqueId", ManageableRole.class, UniqueId.class);
    /**
     * The meta-property for the {@code roleName} property.
     */
    private final MetaProperty<String> _roleName = DirectMetaProperty.ofReadWrite(
        this, "roleName", ManageableRole.class, String.class);
    /**
     * The meta-property for the {@code description} property.
     */
    private final MetaProperty<String> _description = DirectMetaProperty.ofReadWrite(
        this, "description", ManageableRole.class, String.class);
    /**
     * The meta-property for the {@code associatedRoles} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _associatedRoles = DirectMetaProperty.ofReadWrite(
        this, "associatedRoles", ManageableRole.class, (Class) Set.class);
    /**
     * The meta-property for the {@code associatedUsers} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _associatedUsers = DirectMetaProperty.ofReadWrite(
        this, "associatedUsers", ManageableRole.class, (Class) Set.class);
    /**
     * The meta-property for the {@code associatedPermissions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _associatedPermissions = DirectMetaProperty.ofReadWrite(
        this, "associatedPermissions", ManageableRole.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "roleName",
        "description",
        "associatedRoles",
        "associatedUsers",
        "associatedPermissions");

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
        case -266779615:  // roleName
          return _roleName;
        case -1724546052:  // description
          return _description;
        case -1839202985:  // associatedRoles
          return _associatedRoles;
        case -1836319582:  // associatedUsers
          return _associatedUsers;
        case 1336772510:  // associatedPermissions
          return _associatedPermissions;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableRole> builder() {
      return new DirectBeanBuilder<ManageableRole>(new ManageableRole());
    }

    @Override
    public Class<? extends ManageableRole> beanType() {
      return ManageableRole.class;
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
     * The meta-property for the {@code roleName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> roleName() {
      return _roleName;
    }

    /**
     * The meta-property for the {@code description} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> description() {
      return _description;
    }

    /**
     * The meta-property for the {@code associatedRoles} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> associatedRoles() {
      return _associatedRoles;
    }

    /**
     * The meta-property for the {@code associatedUsers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> associatedUsers() {
      return _associatedUsers;
    }

    /**
     * The meta-property for the {@code associatedPermissions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> associatedPermissions() {
      return _associatedPermissions;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ManageableRole) bean).getUniqueId();
        case -266779615:  // roleName
          return ((ManageableRole) bean).getRoleName();
        case -1724546052:  // description
          return ((ManageableRole) bean).getDescription();
        case -1839202985:  // associatedRoles
          return ((ManageableRole) bean).getAssociatedRoles();
        case -1836319582:  // associatedUsers
          return ((ManageableRole) bean).getAssociatedUsers();
        case 1336772510:  // associatedPermissions
          return ((ManageableRole) bean).getAssociatedPermissions();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ManageableRole) bean).setUniqueId((UniqueId) newValue);
          return;
        case -266779615:  // roleName
          ((ManageableRole) bean).setRoleName((String) newValue);
          return;
        case -1724546052:  // description
          ((ManageableRole) bean).setDescription((String) newValue);
          return;
        case -1839202985:  // associatedRoles
          ((ManageableRole) bean).setAssociatedRoles((Set<String>) newValue);
          return;
        case -1836319582:  // associatedUsers
          ((ManageableRole) bean).setAssociatedUsers((Set<String>) newValue);
          return;
        case 1336772510:  // associatedPermissions
          ((ManageableRole) bean).setAssociatedPermissions((Set<String>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ManageableRole) bean)._roleName, "roleName");
      JodaBeanUtils.notNull(((ManageableRole) bean)._description, "description");
      JodaBeanUtils.notNull(((ManageableRole) bean)._associatedRoles, "associatedRoles");
      JodaBeanUtils.notNull(((ManageableRole) bean)._associatedUsers, "associatedUsers");
      JodaBeanUtils.notNull(((ManageableRole) bean)._associatedPermissions, "associatedPermissions");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
