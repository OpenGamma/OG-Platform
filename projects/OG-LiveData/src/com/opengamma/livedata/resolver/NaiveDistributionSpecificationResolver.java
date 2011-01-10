/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.DistributionSpecification;

/**
 * A naive implementation of {@code DistributionSpecificationResolver}.
 * <p>
 * This class is only useful in tests.
 */
public class NaiveDistributionSpecificationResolver implements DistributionSpecificationResolver {

  @Override
  public DistributionSpecification getDistributionSpecification(
      LiveDataSpecification spec) throws IllegalArgumentException {
    return new DistributionSpecification(
        spec.getIdentifiers().getIdentifiers().iterator().next(), 
        StandardRules.getNoNormalization(),
        spec.getIdentifiers().toString());
  }

}
