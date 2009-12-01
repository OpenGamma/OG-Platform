package com.opengamma.financial.security.db;

import javax.persistence.Entity;

@Entity
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
      return false;
    }
    return super.equals(o);
  }
  
  // should replace this with on-the-fly generated ones (can't remember class name!)
  public String toString() {
    return this.getClass().getName()+"[id="+getId()+", name="+getName()+", "+_description+"]";
  }
}
