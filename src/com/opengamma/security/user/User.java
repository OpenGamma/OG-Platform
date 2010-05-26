/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import org.mindrot.BCrypt;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;


/**
 * User of the OpenGamma system.
 */
public class User implements UserDetails {

  private Long _id;
  private String _username;
  private String _passwordHash;
  private Set<UserGroup> _userGroups = new HashSet<UserGroup>();
  private Date _lastLogin;

  public User(Long id, String username, String password, Set<UserGroup> userGroups, Date lastLogin) {
    _id = id;
    _username = username;
    setPassword(password);
    _userGroups = userGroups;
    _lastLogin = lastLogin;
  }

  protected User() {
  }

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

  @Override
  public String getPassword() {
    throw new UnsupportedOperationException("For security reasons, " + "we don't store the password itself");
  }

  public void setPassword(String password) {
    // we don't store the actual password
    _passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
  }

  /**
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
  public GrantedAuthority[] getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    for (Authority authority : getAuthoritySet()) {
      authorities.add(new GrantedAuthorityImpl(authority.getRegex()));
    }
    return authorities.toArray(new GrantedAuthority[0]);
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

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(_id).toHashCode();
  }

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
  public String toString() {
    return _username;
  }
}
