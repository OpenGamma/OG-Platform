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

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;

/**
 * 
 * 
 * @author pietari
 */
public class User implements UserDetails {

  private Long _id;
  private String _username;
  private String _password;
  private Set<UserGroup> _userGroups = new HashSet<UserGroup>();
  private Date _lastLogin;
  
  public User(Long id,
      String username,
      String password,
      Set<UserGroup> userGroups,
      Date lastLogin) {
    _id = id;
    _username = username;
    _password = password;
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

  public String getPassword() {
    return _password;
  }

  public void setPassword(String password) {
    this._password = password;
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
        authorities.add(new GrantedAuthorityImpl(authority.getAuthority()));        
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
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    User other = (User) obj;
    if (_id == null) {
      if (other._id != null)
        return false;
    } else if (!_id.equals(other._id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return _username;
  }
}
