/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class ComputeNode {
  
  private int _id;
  private int _configOid;
  private int _configVersion;
  private ComputeHost _computeHost;
  private String _nodeName;
  
  public int getId() {
    return _id;
  }
  
  public void setId(int id) {
    _id = id;
  }
  
  public int getConfigOid() {
    return _configOid;
  }
  
  public void setConfigOid(int configOid) {
    _configOid = configOid;
  }
  
  public int getConfigVersion() {
    return _configVersion;
  }
  
  public void setConfigVersion(int configVersion) {
    _configVersion = configVersion;
  }
  
  public ComputeHost getComputeHost() {
    return _computeHost;
  }
  
  public void setComputeHost(ComputeHost computeHost) {
    _computeHost = computeHost;
  }
  
  public String getNodeName() {
    return _nodeName;
  }
  
  public void setNodeName(String nodeName) {
    _nodeName = nodeName;
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
    ComputeNode rhs = (ComputeNode) obj;
    return new EqualsBuilder().append(_id, rhs._id).isEquals();
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
