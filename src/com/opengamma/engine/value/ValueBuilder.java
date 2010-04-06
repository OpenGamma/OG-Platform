/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

/**
 * Provides utility methods for creating instances of the objects in the
 * {@code com.opengamma.engine.value} package.
 *
 * @author kirk
 */
public final class ValueBuilder {
  private ValueBuilder() {
  }
  
  // REVIEW kirk 2010-03-31 -- Does this make sense to turn into a Proper Builder?
  
  public static ValueSpecification buildSpec(String requirementName, ComputationTargetSpecification targetSpec) {
    return new ValueSpecification(
        new ValueRequirement(requirementName, targetSpec));
  }
  
  public static ValueSpecification buildSpec(String requirementName, ComputationTargetType targetType, String domainName, String key) {
    return buildSpec(requirementName,
        new ComputationTargetSpecification(targetType,
            new DomainSpecificIdentifier(
                new IdentificationDomain(domainName),
                key)));
            
  }

}
