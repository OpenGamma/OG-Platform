/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.user;

import com.opengamma.engine.view.AggregatingViewDefinitionRepository;
import com.opengamma.engine.view.ViewDefinitionRepository;

/**
 * Aggregates a user view definition repository with a default repository. Just a wrapper to aid registration.
 */
public class UserAggregatingViewDefinitionRepository extends AggregatingViewDefinitionRepository {
  
  public UserAggregatingViewDefinitionRepository(ViewDefinitionRepository defaultRepository, UserViewDefinitionRepository userRepository) {
    addRepository(userRepository);
    addRepository(defaultRepository);
  }
  
}
