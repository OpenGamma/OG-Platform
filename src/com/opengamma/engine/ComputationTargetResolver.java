/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

/**
 * 
 *
 * @author kirk
 */
public interface ComputationTargetResolver {
  
  ComputationTarget resolve(ComputationTargetSpecification targetSpecification);

}
