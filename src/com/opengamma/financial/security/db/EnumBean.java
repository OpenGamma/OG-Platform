package com.opengamma.financial.security.db;

import org.apache.commons.lang.ObjectUtils;

public abstract class EnumBean {
  private Long _id;
  private String _name;
  
  public EnumBean() {
  }

  public EnumBean(String name) {
    _name = name;
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
    _name = name;
  }
  
  /* subclasses will need to check class equivalence */
  public boolean equals(Object o) {
    if (!(o instanceof EnumBean)) {
      return false;
    }
    EnumBean other = (EnumBean) o;
    if (getId() != -1 && other.getId() != -1) {
      return getId().longValue() == other.getId().longValue();
    }
    return ObjectUtils.equals(other.getName(), getName());
  }
  
  public int hashCode() {
    if (_id != null) {
      return _name.hashCode();
    } else {
      return _id.intValue();
    }
  }
  
  // should replace this with on-the-fly generated ones (can't remember class name!)
  public String toString() {
    return this.getClass().getName()+"[id="+_id+", name="+_name+"]";
  }
}
