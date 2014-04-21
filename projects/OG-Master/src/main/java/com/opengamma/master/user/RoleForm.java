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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * Provides a form bean suitable for creating or amending a {@code ManageableRole}.
 */
@PublicSPI
@BeanDefinition
public class RoleForm implements Bean {

  /**
   * Valid name regex.
   */
  private static final Pattern VALID_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]*");

  /**
   * The role name that uniquely identifies the role.
   * This is used with the password to authenticate.
   */
  @PropertyDefinition
  private String _roleName;
  /**
   * The plain text version of the role password.
   */
  @PropertyDefinition
  private String _description;
  /**
   * The comma separated list of roles to add.
   */
  @PropertyDefinition
  private String _addRoles;
  /**
   * The comma separated list of roles to remove.
   */
  @PropertyDefinition
  private String _removeRoles;
  /**
   * The comma separated list of permissions to add.
   */
  @PropertyDefinition
  private String _addPermissions;
  /**
   * The comma separated list of permissions to remove.
   */
  @PropertyDefinition
  private String _removePermissions;
  /**
   * The comma separated list of users to add.
   */
  @PropertyDefinition
  private String _addUsers;
  /**
   * The comma separated list of users to remove.
   */
  @PropertyDefinition
  private String _removeUsers;

  /**
   * The base role, allowing other properties to be set.
   */
  @PropertyDefinition
  private ManageableRole _baseRole;

  /**
   * Creates a form object.
   */
  public RoleForm() {
  }

  /**
   * Creates a form object.
   * 
   * @param role  the role to copy from, not null
   */
  public RoleForm(ManageableRole role) {
    setRoleName(role.getRoleName());
    setDescription(role.getDescription());
    setBaseRole(role);
  }

  /**
   * Creates a form object, changing everything except the description.
   * 
   * @param role  the role to copy from, not null
   * @param description  the description, not null
   */
  public RoleForm(ManageableRole role, String description) {
    setRoleName(role.getRoleName());
    setDescription(description);
    setBaseRole(role);
  }

  /**
   * Creates a form object.
   * 
   * @param roleName  the role name, not null
   * @param description  the description, not null
   */
  public RoleForm(String roleName, String description) {
    setRoleName(roleName);
    setDescription(description);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates and adds the proposed role to the master.
   * 
   * @param userMaster  the user master, not null
   * @return the added role
   * @throws RoleFormException if the proposed role is invalid
   */
  public ManageableRole add(UserMaster userMaster) {
    try {
      ManageableRole role = validate(userMaster, true);
      UniqueId uid = userMaster.roleMaster().add(role);
      role.setUniqueId(uid);
      return role;
    } catch (RoleFormException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw new RoleFormException(ex);
    }
  }

  /**
   * Validates and updates the proposed role in the master.
   * 
   * @param userMaster  the user master, not null
   * @return the added role
   * @throws RoleFormException if the proposed role is invalid
   */
  public ManageableRole update(UserMaster userMaster) {
    try {
      ManageableRole role = validate(userMaster, false);
      UniqueId uid = userMaster.roleMaster().update(role);
      role.setUniqueId(uid);
      return role;
    } catch (RoleFormException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw new RoleFormException(ex);
    }
  }

  /**
   * Validates and adds the proposed role to the master.
   * 
   * @param userMaster  the user master, not null
   * @param add  true if adding, false if updating
   * @return the added role
   * @throws RoleFormException if the proposed role is invalid
   */
  protected ManageableRole validate(UserMaster userMaster, boolean add) {
    userMaster = ArgumentChecker.notNull(userMaster, "userMaster");
    String roleName = StringUtils.trimToNull(getRoleName());
    String description = StringUtils.trimToNull(getDescription());
    String addRolesStr = StringUtils.trimToEmpty(getAddRoles());
    String removeRolesStr = StringUtils.trimToEmpty(getRemoveRoles());
    String addPermsStr = StringUtils.trimToEmpty(getAddPermissions());
    String removePermsStr = StringUtils.trimToEmpty(getRemovePermissions());
    String addUsersStr = StringUtils.trimToEmpty(getAddUsers());
    String removeUsersStr = StringUtils.trimToEmpty(getRemoveUsers());
    List<RoleFormError> errors = new ArrayList<>();
    // role name
    if (roleName == null) {
      if (getBaseRole() != null) {
        roleName = getBaseRole().getRoleName();
      }
      if (roleName == null) {
        errors.add(RoleFormError.ROLENAME_MISSING);
      }
    } else if (isRoleNameTooShort(roleName)) {
      errors.add(RoleFormError.ROLENAME_TOO_SHORT);
    } else if (isRoleNameTooLong(roleName)) {
      errors.add(RoleFormError.ROLENAME_TOO_LONG);
    } else if (isRoleNameInvalid(roleName)) {
      errors.add(RoleFormError.ROLENAME_INVALID);
    } else {
      if (add && userMaster.roleMaster().nameExists(roleName)) {
        errors.add(RoleFormError.ROLENAME_ALREADY_IN_USE);
      }
    }
    // email
    if (description == null) {
      errors.add(RoleFormError.DESCRIPTION_MISSING);
    } else if (isDescriptionTooLong(description)) {
      errors.add(RoleFormError.DESCRIPTION_TOO_LONG);
    } else if (isDescriptionInvalid(description)) {
      errors.add(RoleFormError.DESCRIPTION_INVALID);
    }
    // errors
    if (errors.size() > 0) {
      throw new RoleFormException(errors);
    }
    // build role object
    ManageableRole role = getBaseRole();
    if (role == null) {
      role = new ManageableRole(roleName);
    } else {
      role.setRoleName(roleName);
    }
    role.setDescription(description);
    // roles
    for (String roleStr : StringUtils.split(addRolesStr, ',')) {
      roleStr = roleStr.trim();
      if (VALID_NAME.matcher(roleStr).matches() && userMaster.roleMaster().nameExists(roleStr)) {
        role.getAssociatedRoles().add(roleStr);
      }
    }
    for (String roleStr : StringUtils.split(removeRolesStr, ',')) {
      role.getAssociatedRoles().remove(roleStr.trim());
    }
    // permissions
    for (String permStr : StringUtils.split(addPermsStr, ',')) {
      permStr = permStr.trim();
      if (UserForm.VALID_PERMISSION.matcher(permStr).matches()) {
        role.getAssociatedPermissions().add(permStr);
      }
    }
    for (String perm : StringUtils.split(removePermsStr, ',')) {
      role.getAssociatedPermissions().remove(perm.trim());
    }
    // users
    for (String userStr : StringUtils.split(addUsersStr, ',')) {
      userStr = userStr.trim();
      if (UserForm.VALID_NAME.matcher(userStr).matches() && userMaster.nameExists(userStr)) {
        role.getAssociatedUsers().add(userStr);
      }
    }
    for (String userName : StringUtils.split(removeUsersStr, ',')) {
      role.getAssociatedUsers().remove(userName.trim());
    }
    return role;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the role name is too short.
   * 
   * @param roleName  the role name, not null
   * @return true if short
   */
  protected boolean isRoleNameTooShort(String roleName) {
    return roleName.length() < 5;
  }

  /**
   * Checks if the role name is too long.
   * 
   * @param roleName  the role name, not null
   * @return true if long
   */
  protected boolean isRoleNameTooLong(String roleName) {
    return roleName.length() > 20;
  }

  /**
   * Checks if the role name is invalid.
   * 
   * @param roleName  the role name, not null
   * @return true if invalid
   */
  protected boolean isRoleNameInvalid(String roleName) {
    return VALID_NAME.matcher(roleName).matches() == false;
  }

  /**
   * Checks if the email address is too long.
   * 
   * @param emailAddress  the email address, not null
   * @return true if long
   */
  protected boolean isDescriptionTooLong(String emailAddress) {
    return emailAddress.length() > 200;
  }

  /**
   * Checks if the email address is invalid.
   * 
   * @param email  the email address, not null
   * @return true if invalid
   */
  protected boolean isDescriptionInvalid(String email) {
    return false;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RoleForm}.
   * @return the meta-bean, not null
   */
  public static RoleForm.Meta meta() {
    return RoleForm.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RoleForm.Meta.INSTANCE);
  }

  @Override
  public RoleForm.Meta metaBean() {
    return RoleForm.Meta.INSTANCE;
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
   * Gets the role name that uniquely identifies the role.
   * This is used with the password to authenticate.
   * @return the value of the property
   */
  public String getRoleName() {
    return _roleName;
  }

  /**
   * Sets the role name that uniquely identifies the role.
   * This is used with the password to authenticate.
   * @param roleName  the new value of the property
   */
  public void setRoleName(String roleName) {
    this._roleName = roleName;
  }

  /**
   * Gets the the {@code roleName} property.
   * This is used with the password to authenticate.
   * @return the property, not null
   */
  public final Property<String> roleName() {
    return metaBean().roleName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the plain text version of the role password.
   * @return the value of the property
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Sets the plain text version of the role password.
   * @param description  the new value of the property
   */
  public void setDescription(String description) {
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
   * Gets the comma separated list of roles to add.
   * @return the value of the property
   */
  public String getAddRoles() {
    return _addRoles;
  }

  /**
   * Sets the comma separated list of roles to add.
   * @param addRoles  the new value of the property
   */
  public void setAddRoles(String addRoles) {
    this._addRoles = addRoles;
  }

  /**
   * Gets the the {@code addRoles} property.
   * @return the property, not null
   */
  public final Property<String> addRoles() {
    return metaBean().addRoles().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the comma separated list of roles to remove.
   * @return the value of the property
   */
  public String getRemoveRoles() {
    return _removeRoles;
  }

  /**
   * Sets the comma separated list of roles to remove.
   * @param removeRoles  the new value of the property
   */
  public void setRemoveRoles(String removeRoles) {
    this._removeRoles = removeRoles;
  }

  /**
   * Gets the the {@code removeRoles} property.
   * @return the property, not null
   */
  public final Property<String> removeRoles() {
    return metaBean().removeRoles().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the comma separated list of permissions to add.
   * @return the value of the property
   */
  public String getAddPermissions() {
    return _addPermissions;
  }

  /**
   * Sets the comma separated list of permissions to add.
   * @param addPermissions  the new value of the property
   */
  public void setAddPermissions(String addPermissions) {
    this._addPermissions = addPermissions;
  }

  /**
   * Gets the the {@code addPermissions} property.
   * @return the property, not null
   */
  public final Property<String> addPermissions() {
    return metaBean().addPermissions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the comma separated list of permissions to remove.
   * @return the value of the property
   */
  public String getRemovePermissions() {
    return _removePermissions;
  }

  /**
   * Sets the comma separated list of permissions to remove.
   * @param removePermissions  the new value of the property
   */
  public void setRemovePermissions(String removePermissions) {
    this._removePermissions = removePermissions;
  }

  /**
   * Gets the the {@code removePermissions} property.
   * @return the property, not null
   */
  public final Property<String> removePermissions() {
    return metaBean().removePermissions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the comma separated list of users to add.
   * @return the value of the property
   */
  public String getAddUsers() {
    return _addUsers;
  }

  /**
   * Sets the comma separated list of users to add.
   * @param addUsers  the new value of the property
   */
  public void setAddUsers(String addUsers) {
    this._addUsers = addUsers;
  }

  /**
   * Gets the the {@code addUsers} property.
   * @return the property, not null
   */
  public final Property<String> addUsers() {
    return metaBean().addUsers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the comma separated list of users to remove.
   * @return the value of the property
   */
  public String getRemoveUsers() {
    return _removeUsers;
  }

  /**
   * Sets the comma separated list of users to remove.
   * @param removeUsers  the new value of the property
   */
  public void setRemoveUsers(String removeUsers) {
    this._removeUsers = removeUsers;
  }

  /**
   * Gets the the {@code removeUsers} property.
   * @return the property, not null
   */
  public final Property<String> removeUsers() {
    return metaBean().removeUsers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the base role, allowing other properties to be set.
   * @return the value of the property
   */
  public ManageableRole getBaseRole() {
    return _baseRole;
  }

  /**
   * Sets the base role, allowing other properties to be set.
   * @param baseRole  the new value of the property
   */
  public void setBaseRole(ManageableRole baseRole) {
    this._baseRole = baseRole;
  }

  /**
   * Gets the the {@code baseRole} property.
   * @return the property, not null
   */
  public final Property<ManageableRole> baseRole() {
    return metaBean().baseRole().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public RoleForm clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RoleForm other = (RoleForm) obj;
      return JodaBeanUtils.equal(getRoleName(), other.getRoleName()) &&
          JodaBeanUtils.equal(getDescription(), other.getDescription()) &&
          JodaBeanUtils.equal(getAddRoles(), other.getAddRoles()) &&
          JodaBeanUtils.equal(getRemoveRoles(), other.getRemoveRoles()) &&
          JodaBeanUtils.equal(getAddPermissions(), other.getAddPermissions()) &&
          JodaBeanUtils.equal(getRemovePermissions(), other.getRemovePermissions()) &&
          JodaBeanUtils.equal(getAddUsers(), other.getAddUsers()) &&
          JodaBeanUtils.equal(getRemoveUsers(), other.getRemoveUsers()) &&
          JodaBeanUtils.equal(getBaseRole(), other.getBaseRole());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getRoleName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDescription());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAddRoles());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRemoveRoles());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAddPermissions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRemovePermissions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAddUsers());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRemoveUsers());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBaseRole());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("RoleForm{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("roleName").append('=').append(JodaBeanUtils.toString(getRoleName())).append(',').append(' ');
    buf.append("description").append('=').append(JodaBeanUtils.toString(getDescription())).append(',').append(' ');
    buf.append("addRoles").append('=').append(JodaBeanUtils.toString(getAddRoles())).append(',').append(' ');
    buf.append("removeRoles").append('=').append(JodaBeanUtils.toString(getRemoveRoles())).append(',').append(' ');
    buf.append("addPermissions").append('=').append(JodaBeanUtils.toString(getAddPermissions())).append(',').append(' ');
    buf.append("removePermissions").append('=').append(JodaBeanUtils.toString(getRemovePermissions())).append(',').append(' ');
    buf.append("addUsers").append('=').append(JodaBeanUtils.toString(getAddUsers())).append(',').append(' ');
    buf.append("removeUsers").append('=').append(JodaBeanUtils.toString(getRemoveUsers())).append(',').append(' ');
    buf.append("baseRole").append('=').append(JodaBeanUtils.toString(getBaseRole())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RoleForm}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code roleName} property.
     */
    private final MetaProperty<String> _roleName = DirectMetaProperty.ofReadWrite(
        this, "roleName", RoleForm.class, String.class);
    /**
     * The meta-property for the {@code description} property.
     */
    private final MetaProperty<String> _description = DirectMetaProperty.ofReadWrite(
        this, "description", RoleForm.class, String.class);
    /**
     * The meta-property for the {@code addRoles} property.
     */
    private final MetaProperty<String> _addRoles = DirectMetaProperty.ofReadWrite(
        this, "addRoles", RoleForm.class, String.class);
    /**
     * The meta-property for the {@code removeRoles} property.
     */
    private final MetaProperty<String> _removeRoles = DirectMetaProperty.ofReadWrite(
        this, "removeRoles", RoleForm.class, String.class);
    /**
     * The meta-property for the {@code addPermissions} property.
     */
    private final MetaProperty<String> _addPermissions = DirectMetaProperty.ofReadWrite(
        this, "addPermissions", RoleForm.class, String.class);
    /**
     * The meta-property for the {@code removePermissions} property.
     */
    private final MetaProperty<String> _removePermissions = DirectMetaProperty.ofReadWrite(
        this, "removePermissions", RoleForm.class, String.class);
    /**
     * The meta-property for the {@code addUsers} property.
     */
    private final MetaProperty<String> _addUsers = DirectMetaProperty.ofReadWrite(
        this, "addUsers", RoleForm.class, String.class);
    /**
     * The meta-property for the {@code removeUsers} property.
     */
    private final MetaProperty<String> _removeUsers = DirectMetaProperty.ofReadWrite(
        this, "removeUsers", RoleForm.class, String.class);
    /**
     * The meta-property for the {@code baseRole} property.
     */
    private final MetaProperty<ManageableRole> _baseRole = DirectMetaProperty.ofReadWrite(
        this, "baseRole", RoleForm.class, ManageableRole.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "roleName",
        "description",
        "addRoles",
        "removeRoles",
        "addPermissions",
        "removePermissions",
        "addUsers",
        "removeUsers",
        "baseRole");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -266779615:  // roleName
          return _roleName;
        case -1724546052:  // description
          return _description;
        case -1247976804:  // addRoles
          return _addRoles;
        case -305702759:  // removeRoles
          return _removeRoles;
        case 1754866979:  // addPermissions
          return _addPermissions;
        case -1136662176:  // removePermissions
          return _removePermissions;
        case -1245093401:  // addUsers
          return _addUsers;
        case -302819356:  // removeUsers
          return _removeUsers;
        case -1721554201:  // baseRole
          return _baseRole;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RoleForm> builder() {
      return new DirectBeanBuilder<RoleForm>(new RoleForm());
    }

    @Override
    public Class<? extends RoleForm> beanType() {
      return RoleForm.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
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
     * The meta-property for the {@code addRoles} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> addRoles() {
      return _addRoles;
    }

    /**
     * The meta-property for the {@code removeRoles} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> removeRoles() {
      return _removeRoles;
    }

    /**
     * The meta-property for the {@code addPermissions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> addPermissions() {
      return _addPermissions;
    }

    /**
     * The meta-property for the {@code removePermissions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> removePermissions() {
      return _removePermissions;
    }

    /**
     * The meta-property for the {@code addUsers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> addUsers() {
      return _addUsers;
    }

    /**
     * The meta-property for the {@code removeUsers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> removeUsers() {
      return _removeUsers;
    }

    /**
     * The meta-property for the {@code baseRole} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ManageableRole> baseRole() {
      return _baseRole;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -266779615:  // roleName
          return ((RoleForm) bean).getRoleName();
        case -1724546052:  // description
          return ((RoleForm) bean).getDescription();
        case -1247976804:  // addRoles
          return ((RoleForm) bean).getAddRoles();
        case -305702759:  // removeRoles
          return ((RoleForm) bean).getRemoveRoles();
        case 1754866979:  // addPermissions
          return ((RoleForm) bean).getAddPermissions();
        case -1136662176:  // removePermissions
          return ((RoleForm) bean).getRemovePermissions();
        case -1245093401:  // addUsers
          return ((RoleForm) bean).getAddUsers();
        case -302819356:  // removeUsers
          return ((RoleForm) bean).getRemoveUsers();
        case -1721554201:  // baseRole
          return ((RoleForm) bean).getBaseRole();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -266779615:  // roleName
          ((RoleForm) bean).setRoleName((String) newValue);
          return;
        case -1724546052:  // description
          ((RoleForm) bean).setDescription((String) newValue);
          return;
        case -1247976804:  // addRoles
          ((RoleForm) bean).setAddRoles((String) newValue);
          return;
        case -305702759:  // removeRoles
          ((RoleForm) bean).setRemoveRoles((String) newValue);
          return;
        case 1754866979:  // addPermissions
          ((RoleForm) bean).setAddPermissions((String) newValue);
          return;
        case -1136662176:  // removePermissions
          ((RoleForm) bean).setRemovePermissions((String) newValue);
          return;
        case -1245093401:  // addUsers
          ((RoleForm) bean).setAddUsers((String) newValue);
          return;
        case -302819356:  // removeUsers
          ((RoleForm) bean).setRemoveUsers((String) newValue);
          return;
        case -1721554201:  // baseRole
          ((RoleForm) bean).setBaseRole((ManageableRole) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
