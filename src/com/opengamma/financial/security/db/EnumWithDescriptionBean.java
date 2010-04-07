package com.opengamma.financial.security.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class EnumWithDescriptionBean extends EnumBean {
  private String _description;

  public EnumWithDescriptionBean() {
    super();
  }


  public EnumWithDescriptionBean(String name, String description) {
    super(name);
    _description = description;
  }
  
  
  public String getDescription() {
    return _description;
  }
  
  public void setDescription(String description) {
    _description = description;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof EnumWithDescriptionBean)) {
      System.err.println("not EnumWithDescriptionBean");
      return false;
    }
    EnumWithDescriptionBean ewd = (EnumWithDescriptionBean) o;
    if (getId() != -1 && ewd.getId() != -1) {
      System.err.println("both have valid id's:"+getId()+" other:"+ewd.getId()+" value="+(getId() == ewd.getId()));
      return getId().longValue() == ewd.getId().longValue();
    }
    System.err.println("using equals builder");
    return new EqualsBuilder().append(getName(), ewd.getName()).append(getDescription(), ewd.getDescription()).isEquals();
  }
  
  public int hashCode() {
    return new HashCodeBuilder().append(getName()).append(getDescription()).toHashCode();
  }
  
  // should replace this with on-the-fly generated ones (can't remember class name!)
  public String toString() {
    return this.getClass().getName()+"[id="+getId()+", name="+getName()+", "+_description+"]";
  }
}
