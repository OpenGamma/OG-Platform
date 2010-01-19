/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * 
 * @author pietari
 */
public class UserGroup {

  private Long _id;
  private String _name;
  private Set<User> _users = new HashSet<User>();
  private Set<Authority> _authorities = new HashSet<Authority>();

  protected UserGroup() {
  }
  
  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    this._name = name;
  }

  public Set<User> getUsers() {
    return _users;
  }

  public void setUsers(Set<User> users) {
    this._users = users;
  }

  public Set<Authority> getAuthorities() {
    return _authorities;
  }

  public void setAuthorities(Set<Authority> authorities) {
    this._authorities = authorities;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
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
    UserGroup other = (UserGroup) obj;
    if (_name == null) {
      if (other._name != null)
        return false;
    } else if (!_name.equals(other._name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return _name;
  }

}
