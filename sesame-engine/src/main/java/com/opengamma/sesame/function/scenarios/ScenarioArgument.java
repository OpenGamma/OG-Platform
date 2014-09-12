/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios;

/**
 *
 * TODO explain why hashCode() and equals() are important for scenario args
 *
 * TODO why is that useful? why not key args by their own type and have decorators specify the arg type they want?
 * useful for:
 *   ScenarioArguments.getDecoratorTypes() - for creating view config for scenarios - self-describing scenarios
 *   FilteredScenarioArguments.forFunctions() - arg pruning for better caching
 * could still key by arg, not decorator type
 * used to work without this because scenario args were a map of decorator class to arg instance
 * if arg type were used for lookup instead of fn instance it would allow any old fn to look up args
 * that might break caching because the logic behind argument pruning wouldn't be safe
 */
public interface ScenarioArgument<T extends ScenarioFunction<?>> {

  /**
   * @return  the function type that uses this type of arguments
   */
  Class<T> getFunctionType();
}
