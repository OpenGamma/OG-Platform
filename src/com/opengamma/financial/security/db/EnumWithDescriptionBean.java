package com.opengamma.financial.security.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
    EnumWithDescriptionBean ewd = (EnumWithDescriptionBean) o;
    if (getId() != -1 && ewd.getId() != -1) {
      return getId().longValue() == ewd.getId().longValue();
    }
    return new EqualsBuilder().append(getName(), ewd.getName()).append(getDescription(), ewd.getDescription()).isEquals();
  }
  
  public int hashCode() {
    return new HashCodeBuilder().append(getName()).append(getDescription()).toHashCode();
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
}
