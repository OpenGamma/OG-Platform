package com.opengamma.financial.securities.keys;

import com.opengamma.engine.security.SecurityKey;
import com.opengamma.financial.model.option.definition.OptionDefinition;

public class OptionKey {
	private SecurityKey _security;
  private OptionDefinition _optionDefinition;

  public OptionKey(SecurityKey security, OptionDefinition optionDefinition) {
	  _security = security;
	  _optionDefinition = optionDefinition;
	}
	
  public SecurityKey getSecurity() {
    return _security;
  }

  public OptionDefinition getOptionDefinition() {
    return _optionDefinition;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_optionDefinition == null) ? 0 : _optionDefinition.hashCode());
    result = prime * result + ((_security == null) ? 0 : _security.hashCode());
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
    OptionKey other = (OptionKey) obj;
    if (_optionDefinition == null) {
      if (other._optionDefinition != null)
        return false;
    } else if (!_optionDefinition.equals(other._optionDefinition))
      return false;
    if (_security == null) {
      if (other._security != null)
        return false;
    } else if (!_security.equals(other._security))
      return false;
    return true;
  }
  
  
}
