/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * Encodes full metadata about a particular {@link ComputedValue}.
 *
 * @author kirk
 */
public class ValueSpecification implements Serializable {
  private final ValueRequirement _requirementSpecification;
  // TODO kirk 2009-12-30 -- Add metadata.
  
  public ValueSpecification(ValueRequirement requirementSpecification) {
    ArgumentChecker.checkNotNull(requirementSpecification, "Value requirement specification");
    _requirementSpecification = requirementSpecification;
  }

  /**
   * @return the requirementSpecification
   */
  public ValueRequirement getRequirementSpecification() {
    return _requirementSpecification;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof ValueSpecification)) {
      return false;
    }
    ValueSpecification other = (ValueSpecification) obj;
    if(!ObjectUtils.equals(_requirementSpecification, other._requirementSpecification)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int prime = 37;
    int result = 1;
    result = (result * prime) + getRequirementSpecification().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    MutableFudgeFieldContainer msg = fudgeMessageFactory.newMessage();
    _requirementSpecification.writeFields(msg);
    return msg;
  }
  
  public static ValueSpecification fromFudgeMsg(FudgeFieldContainer msg) {
    return new ValueSpecification(ValueRequirement.fromFudge(msg));
  }

}
