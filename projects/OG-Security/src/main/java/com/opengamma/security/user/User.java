/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A user of the OpenGamma system.
 */
public class User implements UserDetails {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The database id.
   */
  private Long _id;
  /**
   * The user name.
   */
  private String _username;
  /**
   * The hash of the password.
   */
  private String _passwordHash;
  /**
   * The groups the user belongs to.
   */
  private Set<UserGroup> _userGroups = new HashSet<UserGroup>();
  /**
   * The instant of last logon.
   */
  private Date _lastLogin;

  /**
   * Creates an instance of the user.
   * @param id  the database id
   * @param username  the user name
   * @param password  the password, hashed internally
   * @param userGroups  the set of groups
   * @param lastLogin  the last logon instant
   */
  public User(Long id, String username, String password, Set<UserGroup> userGroups, Date lastLogin) {
    _id = id;
    _username = username;
    setPassword(password);
    _userGroups = userGroups;
    _lastLogin = lastLogin;
  }

  /**
   * Restricted constructor for tools.
   */
  protected User() {
  }

  //-------------------------------------------------------------------------
  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

  public String getUsername() {
    return _username;
  }

  public void setUsername(String username) {
    this._username = username;
  }

  /**
   * Throws an exception, as the password is not directly stored for security reasons.
   * @return never
   */
  @Override
  public String getPassword() {
    throw new UnsupportedOperationException("For security reasons, the password is not stored directly");
  }

  /**
   * Sets the password, which hashes the password internally.
   * @param password  the password to set
   */
  public void setPassword(String password) {
    // we don't store the actual password
    _passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
  }

  /**
   * Checks if the password specified matches the stored password.
   * @param password  the password to check
   * @return true if the password is OK, false otherwise
   */
  public boolean checkPassword(String password) {
    return BCrypt.checkpw(password, _passwordHash);
  }

  public String getPasswordHash() {
    return _passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this._passwordHash = passwordHash;
  }

  public Set<UserGroup> getUserGroups() {
    return _userGroups;
  }

  public void setUserGroups(Set<UserGroup> userGroups) {
    this._userGroups = userGroups;
  }

  public Date getLastLogin() {
    return _lastLogin;
  }

  public void setLastLogin(Date lastLogin) {
    this._lastLogin = lastLogin;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    for (Authority authority : getAuthoritySet()) {
      authorities.add(new SimpleGrantedAuthority(authority.getRegex()));
    }
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public Set<Authority> getAuthoritySet() {
    Set<Authority> authorities = new HashSet<Authority>();
    for (UserGroup group : _userGroups) {
      authorities.addAll(group.getAuthorities());
    }
    return authorities;
  }

  /**
   * Returns whether this <code>User</code> has the given permission.
   * This will be the case if and only if the permission matches at least one of this user's <code>Authorities</code>.
   * 
   * @param permission Permission to check, for example /MarketData/Bloomberg/AAPL/View
   * @return true if this <code>User</code> has the given permission, false otherwise
   * @see Authority#matches
   */
  public boolean hasPermission(String permission) {
    for (Authority authority : getAuthoritySet()) {
      if (authority.matches(permission)) {
        return true;
      }
    }
    return false;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    User rhs = (User) obj;
    return new EqualsBuilder().append(_id, rhs._id).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(_id).toHashCode();
  }

  @Override
  public String toString() {
    return _username;
  }

}
