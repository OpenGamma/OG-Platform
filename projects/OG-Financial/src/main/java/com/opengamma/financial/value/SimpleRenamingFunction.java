/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.value;

import java.util.Collections;

import com.opengamma.engine.target.ComputationTargetType;

/** Take one name that is available as an output, 'satisfiedByTheValueName' and create an alias for it, 'askForThisName'.
 *  This means that one can ask for either ValueRequirementName and return the same result, thus allowing us to alias our name to a client's preference
 */
public class SimpleRenamingFunction extends ValueRenamingFunction {

  public SimpleRenamingFunction(String satisfiedByTheValueName, String askForThisName) {
    super(Collections.singleton(satisfiedByTheValueName), askForThisName, ComputationTargetType.SECURITY.or(ComputationTargetType.POSITION).or(ComputationTargetType.TRADE));
  }

}
