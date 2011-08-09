/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class NaiveDistributionSpecificationResolver 
  extends AbstractResolver<LiveDataSpecification, DistributionSpecification> 
  implements DistributionSpecificationResolver {

  @Override
  public DistributionSpecification resolve(
      LiveDataSpecification spec) throws IllegalArgumentException {
    return new DistributionSpecification(
        spec.getIdentifiers().getExternalIds().iterator().next(), 
        StandardRules.getNoNormalization(),
        spec.getIdentifiers().toString());
  }

}
