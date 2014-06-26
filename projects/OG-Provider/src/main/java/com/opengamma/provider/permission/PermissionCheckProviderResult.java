/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.PublicSPI;

/**
 * Result of permission checks for a set of permissions for a user.
 * <p>
 * The result contains a map of the permissions that were checked and the true-false result.
 * It also contains error fields that provide more detail.
 * The map of checked permissions will contain no true values if there are errors
 * and will typically be empty.
 * As such, there is no need for an {@code isErrors()} method.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicSPI
@BeanDefinition(builderScope = "private")
public final class PermissionCheckProviderResult implements ImmutableBean {

  /**
   * The permission check result.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableMap<String, Boolean> _checkedPermissions;
  /**
   * The authentication error, null if no error in authentication.
   * The map of checked permissions contains no true values.
   */
  @PropertyDefinition
  private final String _authenticationError;
  /**
   * The authorization error, null if no error in authorization.
   * The map of checked permissions contains no true values.
   */
  @PropertyDefinition
  private final String _authorizationError;

  //-------------------------------------------------------------------------
  /**
   * Creates a result containing authorization information.
   * 
   * @param checkedPermissions  the map of checked permissions, not null
   * @return the result, not null
   */
  public static PermissionCheckProviderResult of(Map<String, Boolean> checkedPermissions) {
    return new PermissionCheckProviderResult(checkedPermissions, null, null);
  }

  /**
   * Creates an authentication error result.
   * 
   * @param errorMessage  the message, not null
   * @return the result, not null
   */
  public static PermissionCheckProviderResult ofAuthenticationError(String errorMessage) {
    return new PermissionCheckProviderResult(ImmutableMap.<String, Boolean>of(), errorMessage, null);
  }

  /**
   * Creates an authorization error result.
   * 
   * @param errorMessage  the message, not null
   * @return the result, not null
   */
  public static PermissionCheckProviderResult ofAuthorizationError(String errorMessage) {
    return new PermissionCheckProviderResult(ImmutableMap.<String, Boolean>of(), null, errorMessage);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the specified permission is true or false.
   * <p>
   * This method returns false rather than throwing an exception.
   * 
   * @param requestedPermissions  the requested permissions, not null
   * @return true if permitted, false if not
   */
  public boolean isPermittedAll(Collection<String> requestedPermissions) {
    for (String requestedPermission : requestedPermissions) {
      if (isPermitted(requestedPermission) == false) {
        return false;
      }
    }
    return true;
  }

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

  //-------------------------------------------------------------------------
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

  private PermissionCheckProviderResult(
      Map<String, Boolean> checkedPermissions,
      String authenticationError,
      String authorizationError) {
    JodaBeanUtils.notNull(checkedPermissions, "checkedPermissions");
    this._checkedPermissions = ImmutableMap.copyOf(checkedPermissions);
    this._authenticationError = authenticationError;
    this._authorizationError = authorizationError;
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
  public ImmutableMap<String, Boolean> getCheckedPermissions() {
    return _checkedPermissions;
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

  //-----------------------------------------------------------------------
  /**
   * Gets the authorization error, null if no error in authorization.
   * The map of checked permissions contains no true values.
   * @return the value of the property
   */
  public String getAuthorizationError() {
    return _authorizationError;
  }

  //-----------------------------------------------------------------------
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
    buf.append("checkedPermissions").append('=').append(getCheckedPermissions()).append(',').append(' ');
    buf.append("authenticationError").append('=').append(getAuthenticationError()).append(',').append(' ');
    buf.append("authorizationError").append('=').append(JodaBeanUtils.toString(getAuthorizationError()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PermissionCheckProviderResult}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code checkedPermissions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableMap<String, Boolean>> _checkedPermissions = DirectMetaProperty.ofImmutable(
        this, "checkedPermissions", PermissionCheckProviderResult.class, (Class) ImmutableMap.class);
    /**
     * The meta-property for the {@code authenticationError} property.
     */
    private final MetaProperty<String> _authenticationError = DirectMetaProperty.ofImmutable(
        this, "authenticationError", PermissionCheckProviderResult.class, String.class);
    /**
     * The meta-property for the {@code authorizationError} property.
     */
    private final MetaProperty<String> _authorizationError = DirectMetaProperty.ofImmutable(
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
    private Meta() {
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
    public PermissionCheckProviderResult.Builder builder() {
      return new PermissionCheckProviderResult.Builder();
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
    public MetaProperty<ImmutableMap<String, Boolean>> checkedPermissions() {
      return _checkedPermissions;
    }

    /**
     * The meta-property for the {@code authenticationError} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> authenticationError() {
      return _authenticationError;
    }

    /**
     * The meta-property for the {@code authorizationError} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> authorizationError() {
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

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code PermissionCheckProviderResult}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<PermissionCheckProviderResult> {

    private Map<String, Boolean> _checkedPermissions = new HashMap<String, Boolean>();
    private String _authenticationError;
    private String _authorizationError;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1315352995:  // checkedPermissions
          return _checkedPermissions;
        case 1320995440:  // authenticationError
          return _authenticationError;
        case 1547592975:  // authorizationError
          return _authorizationError;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1315352995:  // checkedPermissions
          this._checkedPermissions = (Map<String, Boolean>) newValue;
          break;
        case 1320995440:  // authenticationError
          this._authenticationError = (String) newValue;
          break;
        case 1547592975:  // authorizationError
          this._authorizationError = (String) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public PermissionCheckProviderResult build() {
      return new PermissionCheckProviderResult(
          _checkedPermissions,
          _authenticationError,
          _authorizationError);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("PermissionCheckProviderResult.Builder{");
      buf.append("checkedPermissions").append('=').append(JodaBeanUtils.toString(_checkedPermissions)).append(',').append(' ');
      buf.append("authenticationError").append('=').append(JodaBeanUtils.toString(_authenticationError)).append(',').append(' ');
      buf.append("authorizationError").append('=').append(JodaBeanUtils.toString(_authorizationError));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
