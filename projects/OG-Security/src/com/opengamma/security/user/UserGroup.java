/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import java.util.HashSet;
import java.util.Set;

/**
 * A user group within OpenGamma.
 */
public class UserGroup {

  /**
   * The database id.
   */
  private Long _id;
  /**
   * The group name.
   */
  private String _name;
  /**
   * The users within the group.
   */
  private Set<User> _users = new HashSet<User>();
  /**
   * The authorities controlled by the group.
   */
  private Set<Authority> _authorities = new HashSet<Authority>();

  /**
   * Creates an instance of a user group.
   * @param id  the database id
   * @param name  the group name
   */
  public UserGroup(Long id, String name) {
    this(id, name, new HashSet<User>(), new HashSet<Authority>());    
  }

  /**
   * Creates an instance of a user group.
   * @param id  the database id
   * @param name  the group name
   * @param users  the users
   * @param authorities  the authorities
   */
  public UserGroup(Long id, String name, Set<User> users, Set<Authority> authorities) {
    _id = id;
    _name = name;
    _users = users;
    _authorities = authorities;
  }

  /**
   * Restricted constructor for tools.
   */
  protected UserGroup() {
  }

  //-------------------------------------------------------------------------
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

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    UserGroup other = (UserGroup) obj;
    if (_id == null) {
      if (other._id != null) {
        return false;
      }
    } else if (!_id.equals(other._id)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return _name;
  }

}
