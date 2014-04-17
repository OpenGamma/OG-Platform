/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Querying interface to a blacklist resource to test whether items are matched by the blacklisting rules on that resource. Using a query instance is likely to be faster than fetching the rules from a
 * blacklist and testing each one as it may be able to construct indexes.
 * <p>
 * Many of the functions have two forms; one taking the function identifier and function parameters and one taking the composite {@link ParameterizedFunction}. This is to reflect different use cases
 * (calculation nodes will use the components, graph builders will use the composite). An implementation will typically write one in terms of the other.
 */
public interface FunctionBlacklistQuery {

  /**
   * Tests if the function blacklist is empty and will never match anything. This is to allow code to bypass a large number of checks if each will return false.
   * 
   * @return true if the blacklist will match no items, false otherwise
   */
  boolean isEmpty();

  /**
   * Tests if a function is blacklisted in its entirety. The underlying blacklist may relate to the function parameters and/or the compiled function definition.
   * 
   * @param functionIdentifier the function instance to test
   * @param functionParameters the function parameters to test
   * @return true if the function is blacklisted, false otherwise
   */
  boolean isBlacklisted(String functionIdentifier, FunctionParameters functionParameters);

  /**
   * Tests if a function is blacklisted in its entirety. The underlying blacklist may relate to the function parameters and/or the compiled function definition.
   * 
   * @param function the parameterized function instance to test
   * @return true if the function is blacklisted, false otherwise
   */
  boolean isBlacklisted(DependencyNodeFunction function);

  /**
   * Tests if a computation target is blacklisted in its entirety. The underlying blacklist may relate to a target type and/or the actual target identifier.
   * 
   * @param target the target specification to test
   * @return true if the target is blacklisted, false otherwise
   */
  boolean isBlacklisted(ComputationTargetSpecification target);

  /**
   * Tests if a function should not be applied to a target. The underlying blacklist may relate to the individual function, target or the combination of both.
   * 
   * @param functionIdentifier the function instance to test
   * @param functionParameters the function parameters to test
   * @param target the target specification to test
   * @return true if the combination is blacklisted, false otherwise
   */
  boolean isBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target);

  /**
   * Tests if a function should not be applied to a target. The underlying blacklist may relate to the individual function, target or the combination of both.
   * 
   * @param function the parameterized function instance to test
   * @param target the target specification to test
   * @return true if the combination is blacklisted, false otherwise
   */
  boolean isBlacklisted(DependencyNodeFunction function, ComputationTargetSpecification target);

  /**
   * Tests if a function should not be used against a target with the given inputs to produce the requested outputs. The underlying blacklist may relate to the individual function, target, inputs,
   * outputs or combinations of all. If used at a calculation node, the implementation may have access to the value cache and base a decision on the {@link ComputedValue}s corresponding to the inputs.
   * 
   * @param functionIdentifier the function instance to test
   * @param functionParameters the function parameters to test
   * @param target the target specification to test
   * @param inputs the input value specifications to test
   * @param outputs the output value specifications to test
   * @return true if the combination is blacklisted, false otherwise
   */
  boolean isBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, ValueSpecification[] inputs, ValueSpecification[] outputs);

  /**
   * Tests if a function should not be used against a target with the given inputs to produce the requested outputs. The underlying blacklist may relate to the individual function, target, inputs,
   * outputs or combinations of all. If used at a calculation node, the implementation may have access to the value cache and base a decision on the {@link ComputedValue}s corresponding to the inputs.
   * 
   * @param functionIdentifier the function instance to test
   * @param functionParameters the function parameters to test
   * @param target the target specification to test
   * @param inputs the input value specifications to test
   * @param outputs the output value specifications to test
   * @return true if the combination is blacklisted, false otherwise
   */
  boolean isBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, Collection<ValueSpecification> inputs,
      Collection<ValueSpecification> outputs);

  /**
   * Tests if a function should not be used against a target with the given inputs to produce the requested outputs. The underlying blacklist may relate to the individual function, target, inputs,
   * outputs or combinations of all. If used at a calculation node, the implementation may have access to the value cache and base a decision on the {@link ComputedValue}s corresponding to the inputs.
   * 
   * @param function the parameterized function instance to test
   * @param target the target specification to test
   * @param inputs the input value specifications to test
   * @param outputs the output value specifications to test
   * @return true if the combination is blacklisted, false otherwise
   */
  boolean isBlacklisted(DependencyNodeFunction function, ComputationTargetSpecification target, ValueSpecification[] inputs, ValueSpecification[] outputs);

  /**
   * Tests if a function should not be used against a target with the given inputs to produce the requested outputs. The underlying blacklist may relate to the individual function, target, inputs,
   * outputs or combinations of all. If used at a calculation node, the implementation may have access to the value cache and base a decision on the {@link ComputedValue}s corresponding to the inputs.
   * 
   * @param function the parameterized function instance to test
   * @param target the target specification to test
   * @param inputs the input value specifications to test
   * @param outputs the output value specifications to test
   * @return true if the combination is blacklisted, false otherwise
   */
  boolean isBlacklisted(DependencyNodeFunction function, ComputationTargetSpecification target, Collection<ValueSpecification> inputs, Collection<ValueSpecification> outputs);

  /**
   * Tests if a dependency graph node should not be used. The underlying blacklist may relate to the individual function, target, inputs, outputs or combinations of all. If used at a calculation node,
   * the implementation may have access to the value cache and base a decision on the {@link ComputedValue}s corresponding to the inputs.
   * 
   * @param node the dependency graph node to test
   * @return true if the node is blacklisted, false otherwise
   */
  boolean isBlacklisted(DependencyNode node);

  /**
   * Tests if a job item should not be executed. The underlying blacklist may relate to the individual function, target, inputs, outputs or combinations of all. If used at a calculation node, the
   * implementation may have access to the value cache and base a decision on the {@link ComputedValue}s corresponding to the inputs.
   * 
   * @param jobItem the job item to test
   * @return true if the job item is blacklisted, false otherwise
   */
  boolean isBlacklisted(CalculationJobItem jobItem);

}
