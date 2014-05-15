/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
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

import com.opengamma.util.PublicSPI;

/**
 * Result of permission checks for a set of permissions for a user.
 * <p>
 * The result contains a map of the permissions that were checked and the true-false result.
 * It also contains error fields that provide more detail.
 * The map of checked permissions will contain no true values if there are errors
 * and will typically be empty.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class PermissionCheckProviderResult implements Bean {

  /**
   * The permission check result.
   */
  @PropertyDefinition(validate = "notNull")
  private final Map<String, Boolean> _checkedPermissions = new HashMap<>();
  /**
   * The authentication error, null if no error in authentication.
   * The map of checked permissions contains no true values.
   */
  @PropertyDefinition
  private String _authenticationError;
  /**
   * The authorization error, null if no error in authorization.
   * The map of checked permissions contains no true values.
   */
  @PropertyDefinition
  private String _authorizationError;

  //-------------------------------------------------------------------------
  /**
   * Creates an authentication error result.
   * 
   * @param errorMessage  the message, not null
   * @return the result, not null
   */
  public static PermissionCheckProviderResult ofAuthenticationError(String errorMessage) {
    PermissionCheckProviderResult result = new PermissionCheckProviderResult();
    result.setAuthenticationError(errorMessage);
    return result;
  }

  /**
   * Creates an authorization error result.
   * 
   * @param errorMessage  the message, not null
   * @return the result, not null
   */
  public static PermissionCheckProviderResult ofAuthorizationError(String errorMessage) {
    PermissionCheckProviderResult result = new PermissionCheckProviderResult();
    result.setAuthorizationError(errorMessage);
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  public PermissionCheckProviderResult() {
  }

  /**
   * Creates an instance.
   * 
   * @param checkedPermissions  the map of checked permissions, not null
   */
  public PermissionCheckProviderResult(Map<String, Boolean> checkedPermissions) {
    setCheckedPermissions(checkedPermissions);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the specified permission is true or false.
   * <p>
   * This method returns false rather than throwing an exception.
   * 
   * @param requestedPermission  the requested permission, not null
   * @return true if permitted, false if not
   */
  public boolean isPermitted(String requestedPermission) {
    return BooleanUtils.isTrue(getCheckedPermissions().get(requestedPermission));
  }

  /**
   * Checks the specified permission, throwing an exception if the user does not
   * have the permission.
   * <p>
   * This method throws an exception unless the user has the specified permission.
   * Information stored about different kinds of error is used to refine the exception thrown.
   * 
   * @param requestedPermission  the requested permission, not null
   * @throws UnauthenticatedException if permission was denied due to invalid user authentication
   * @throws AuthorizationException if permission was denied due to issues checking authorization
   * @throws UnauthorizedException if the user does not have the requested permission
   */
  public void checkPermitted(String requestedPermission) {
    checkErrors();
    Boolean permitted = getCheckedPermissions().get(requestedPermission);
    if (permitted == null) {
      throw new AuthorizationException("Permission denied: Specified permission was not checked: " + requestedPermission);
    } else if (permitted.booleanValue() == false) {
      throw new UnauthorizedException("Permission denied: " + requestedPermission);
    }
  }

  /**
   * Checks if any errors occurred, throwing an exception if there were errors.
   * <p>
   * Information stored about different kinds of error is used to refine the exception thrown.
   * 
   * @throws UnauthenticatedException if permission was denied due to invalid user authentication
   * @throws AuthorizationException if permission was denied due to issues checking authorization
   */
  public void checkErrors() {
    if (getAuthenticationError() != null) {
      throw new UnauthenticatedException("Permission denied: " + getAuthenticationError());
    }
    if (getAuthorizationError() != null) {
      throw new AuthorizationException("Permission denied: " + getAuthorizationError());
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code PermissionCheckProviderResult}.
   * @return the meta-bean, not null
   */
  public static PermissionCheckProviderResult.Meta meta() {
    return PermissionCheckProviderResult.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(PermissionCheckProviderResult.Meta.INSTANCE);
  }

  @Override
  public PermissionCheckProviderResult.Meta metaBean() {
    return PermissionCheckProviderResult.Meta.INSTANCE;
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
   * Gets the permission check result.
   * @return the value of the property, not null
   */
  public Map<String, Boolean> getCheckedPermissions() {
    return _checkedPermissions;
  }

  /**
   * Sets the permission check result.
   * @param checkedPermissions  the new value of the property, not null
   */
  public void setCheckedPermissions(Map<String, Boolean> checkedPermissions) {
    JodaBeanUtils.notNull(checkedPermissions, "checkedPermissions");
    this._checkedPermissions.clear();
    this._checkedPermissions.putAll(checkedPermissions);
  }

  /**
   * Gets the the {@code checkedPermissions} property.
   * @return the property, not null
   */
  public final Property<Map<String, Boolean>> checkedPermissions() {
    return metaBean().checkedPermissions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the authentication error, null if no error in authentication.
   * The map of checked permissions contains no true values.
   * @return the value of the property
   */
  public String getAuthenticationError() {
    return _authenticationError;
  }

  /**
   * Sets the authentication error, null if no error in authentication.
   * The map of checked permissions contains no true values.
   * @param authenticationError  the new value of the property
   */
  public void setAuthenticationError(String authenticationError) {
    this._authenticationError = authenticationError;
  }

  /**
   * Gets the the {@code authenticationError} property.
   * The map of checked permissions contains no true values.
   * @return the property, not null
   */
  public final Property<String> authenticationError() {
    return metaBean().authenticationError().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the authorization error, null if no error in authorization.
   * The map of checked permissions contains no true values.
   * @return the value of the property
   */
  public String getAuthorizationError() {
    return _authorizationError;
  }

  /**
   * Sets the authorization error, null if no error in authorization.
   * The map of checked permissions contains no true values.
   * @param authorizationError  the new value of the property
   */
  public void setAuthorizationError(String authorizationError) {
    this._authorizationError = authorizationError;
  }

  /**
   * Gets the the {@code authorizationError} property.
   * The map of checked permissions contains no true values.
   * @return the property, not null
   */
  public final Property<String> authorizationError() {
    return metaBean().authorizationError().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public PermissionCheckProviderResult clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PermissionCheckProviderResult other = (PermissionCheckProviderResult) obj;
      return JodaBeanUtils.equal(getCheckedPermissions(), other.getCheckedPermissions()) &&
          JodaBeanUtils.equal(getAuthenticationError(), other.getAuthenticationError()) &&
          JodaBeanUtils.equal(getAuthorizationError(), other.getAuthorizationError());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getCheckedPermissions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAuthenticationError());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAuthorizationError());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("PermissionCheckProviderResult{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("checkedPermissions").append('=').append(JodaBeanUtils.toString(getCheckedPermissions())).append(',').append(' ');
    buf.append("authenticationError").append('=').append(JodaBeanUtils.toString(getAuthenticationError())).append(',').append(' ');
    buf.append("authorizationError").append('=').append(JodaBeanUtils.toString(getAuthorizationError())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PermissionCheckProviderResult}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code checkedPermissions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, Boolean>> _checkedPermissions = DirectMetaProperty.ofReadWrite(
        this, "checkedPermissions", PermissionCheckProviderResult.class, (Class) Map.class);
    /**
     * The meta-property for the {@code authenticationError} property.
     */
    private final MetaProperty<String> _authenticationError = DirectMetaProperty.ofReadWrite(
        this, "authenticationError", PermissionCheckProviderResult.class, String.class);
    /**
     * The meta-property for the {@code authorizationError} property.
     */
    private final MetaProperty<String> _authorizationError = DirectMetaProperty.ofReadWrite(
        this, "authorizationError", PermissionCheckProviderResult.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "checkedPermissions",
        "authenticationError",
        "authorizationError");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1315352995:  // checkedPermissions
          return _checkedPermissions;
        case 1320995440:  // authenticationError
          return _authenticationError;
        case 1547592975:  // authorizationError
          return _authorizationError;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends PermissionCheckProviderResult> builder() {
      return new DirectBeanBuilder<PermissionCheckProviderResult>(new PermissionCheckProviderResult());
    }

    @Override
    public Class<? extends PermissionCheckProviderResult> beanType() {
      return PermissionCheckProviderResult.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code checkedPermissions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, Boolean>> checkedPermissions() {
      return _checkedPermissions;
    }

    /**
     * The meta-property for the {@code authenticationError} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> authenticationError() {
      return _authenticationError;
    }

    /**
     * The meta-property for the {@code authorizationError} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> authorizationError() {
      return _authorizationError;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1315352995:  // checkedPermissions
          return ((PermissionCheckProviderResult) bean).getCheckedPermissions();
        case 1320995440:  // authenticationError
          return ((PermissionCheckProviderResult) bean).getAuthenticationError();
        case 1547592975:  // authorizationError
          return ((PermissionCheckProviderResult) bean).getAuthorizationError();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1315352995:  // checkedPermissions
          ((PermissionCheckProviderResult) bean).setCheckedPermissions((Map<String, Boolean>) newValue);
          return;
        case 1320995440:  // authenticationError
          ((PermissionCheckProviderResult) bean).setAuthenticationError((String) newValue);
          return;
        case 1547592975:  // authorizationError
          ((PermissionCheckProviderResult) bean).setAuthorizationError((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((PermissionCheckProviderResult) bean)._checkedPermissions, "checkedPermissions");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
