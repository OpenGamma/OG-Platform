/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;


/**
 * This availability provider assumes that we can always get 
 * primitive and security market data. 
 *
 * @author kirk
 */
public class BasicLiveDataAvailabilityProvider
implements LiveDataAvailabilityProvider {
  
  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    if(!ObjectUtils.equals(ValueRequirementNames.MARKET_DATA_HEADER, requirement.getValueName())) {
      return false;
    }
    switch(requirement.getTargetSpecification().getType()) {
    case PRIMITIVE:
    case SECURITY:
      return true;
    default:
      // We can't extract enough information for anything other
      // than Primitive and Security types.
      return false;
    }
  }

}
