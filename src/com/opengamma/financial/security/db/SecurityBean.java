/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

/**
 * Hibernate database bean for a security.
 */
public abstract class SecurityBean {
  private Long _id;
  private Date _effectiveDateTime;
  private boolean _deleted;
  private Date _lastModifiedDateTime;
  private String _modifiedBy;
  private SecurityBean _firstVersion;
  private String _displayName;
  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    _id = id;
  }
  /**
   * @return the id
   */
  public Long getId() {
    return _id;
  }
  
  /**
   * @param dateTime the date/time this security is effective from
   */
  public void setEffectiveDateTime(Date dateTime) {
    _effectiveDateTime = dateTime;
  }
  
  /**
   * @return the date/time this security is effective from
   */
  public Date getEffectiveDateTime() {
    return _effectiveDateTime;
  }
  
  /**
   * @param deleted indicated if the security has been deleted
   */
  public void setDeleted(boolean deleted) {
    _deleted = deleted;
  }
  
  /**
   * @return whether or not the security has been deleted
   */
  public boolean isDeleted() {
    return _deleted;
  }
  
  /**
   * @param dateTime the last date/time this security was modified
   */
  public void setLastModifiedDateTime(Date dateTime) {
    _lastModifiedDateTime = dateTime;
  }
  
  /**
   * @return the last date/time this security was modified
   */
  public Date getLastModifiedDateTime() {
    return _lastModifiedDateTime;
  }
  
  /**
   * @param modifiedBy an indication of who or what this security was last modified by
   */
  public void setLastModifiedBy(String modifiedBy) {
    _modifiedBy = modifiedBy;
  }
  
  /**
   * @return an indication of what or who last modified this security
   */
  public String getLastModifiedBy() {
    return _modifiedBy;
  }
  
  /**
   * this property is actually used to group all versions together.
   * @return the first version of this equity (by order of creation, i.e. lowest id)
   */
  public SecurityBean getFirstVersion() {
    return _firstVersion;
  }
  
  /**
   * this property is actually used to group all versions together.
   * @param firstVersion the first version of this object (by order of creation, i.e. lowest id)
   */
  public void setFirstVersion(SecurityBean firstVersion) {
    _firstVersion = firstVersion; 
  }
  
  public void setDisplayName (final String displayName) {
    _displayName = displayName;
  }
  
  public String getDisplayName () {
    return _displayName;
  }
  
}
