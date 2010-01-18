/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

/**
 * 
 * 
 * @author pietari
 */
public class Authority {

  private Long _id;
  private String _authority;

  protected Authority(String authority) {
    _authority = authority;
  }
  
  protected Authority() {
  }

  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

  public String getAuthority() {
    return _authority;
  }

  public void setAuthority(String authority) {
    this._authority = authority;
  }
  
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_authority == null) ? 0 : _authority.hashCode());
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
    Authority other = (Authority) obj;
    if (_authority == null) {
      if (other._authority != null)
        return false;
    } else if (!_authority.equals(other._authority))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return _authority;
  }

}
