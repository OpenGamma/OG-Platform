/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.graph;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.sesame.config.ConfigUtils;
import com.opengamma.sesame.config.FunctionConfig;
import com.opengamma.sesame.config.GraphConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.function.FunctionMetadata;
import com.opengamma.sesame.function.InvokableFunction;
import com.opengamma.sesame.function.Parameter;

/**
 * A lightweight representation of the dependency tree for a single function.
 * TODO joda bean? needs to be serializable along with all Node subclasses.
 * TODO flag to indicate whether it's valid. probably need to ask all the nodes
 */
public final class FunctionModel {

  private static final Set<Class<?>> INELIGIBLE_TYPES =
      Sets.<Class<?>>newHashSet(UniqueId.class, ExternalId.class, ExternalIdBundle.class);

  private final Node _root;
  private final FunctionMetadata _rootMetadata;

  public FunctionModel(Node root, FunctionMetadata rootMetadata) {
    _root = root;
    _rootMetadata = rootMetadata;
  }

  public Node getRootFunction() {
    return _root;
  }

  public FunctionMetadata getRootMetadata() {
    return _rootMetadata;
  }

  public static FunctionModel forFunction(FunctionMetadata function, GraphConfig config) {
    return new FunctionModel(createNode(function.getDeclaringType(), config), function);
  }

  public static FunctionModel forFunction(FunctionMetadata function, FunctionConfig config) {
    return new FunctionModel(createNode(function.getDeclaringType(), new GraphConfig(config)), function);
  }

  public static FunctionModel forFunction(FunctionMetadata function) {
    return new FunctionModel(createNode(function.getDeclaringType(), GraphConfig.EMPTY), function);
  }

  // TODO need to pass in config that merges default from system, view, (calc config), column, input type
  // it needs to know about the view def, (calc config), column because this class doesn't
  // it needs to merge arguments from everything going up the type hierarchy
  // when searching for args should we go up the type hierarchy at each level of config or go up the levels of config
  // for each type? do we only need to look for the implementation type and the function type?
  // constructor args only apply to the impl and method args apply to the fn method which is on all subtypes of
  // the fn interface. so only need to look at the superclasses that impl the fn interface
  // so in this case we don't need to go up the hierarchy because we're only dealing with constructor args
  // in the engine we'll need to go up the class hierarchy and check everything
  // for implementation class it just needs to go up the set of defaults looking for the first one that matches

  private static Node createNode(Class<?> type, GraphConfig config) {
    List<GraphBuildException> failures = Lists.newArrayList();
    Node node = createNode(type, config, Lists.<Parameter>newArrayList(), failures);
    if (!failures.isEmpty()) {
      GraphBuildException exception = new GraphBuildException("Failed to build graph");
      for (GraphBuildException failure : failures) {
        exception.addSuppressed(failure);
      }
      throw exception;
    }
    return node;
  }

  // TODO this is ugly, simplify / beautify
  @SuppressWarnings("unchecked")
  private static Node createNode(Class<?> type,
                                 GraphConfig config,
                                 List<Parameter> path,
                                 List<GraphBuildException> failureAccumulator) {
    if (!isEligibleForBuilding(type)) {
      return null;
    }
    Class<?> implType = config.getImplementationType(type);
    if (implType == null && !type.isInterface()) {
      implType = type;
    }
    if (implType == null) {
      NoImplementationException e = new NoImplementationException(path, "No implementation or provider available for " +
                                                                    type.getSimpleName());
      failureAccumulator.add(e);
      return new ExceptionNode(e);
    }
    Constructor<?> constructor = ConfigUtils.getConstructor(implType);
    List<Parameter> parameters = ConfigUtils.getParameters(constructor);
    List<Node> constructorArguments = Lists.newArrayListWithCapacity(parameters.size());
    for (Parameter parameter : parameters) {
      // this isn't terribly efficient but unlikely to be a problem. alternatively could use a stack but nasty and mutable
      List<Parameter> newPath = Lists.newArrayList(path);
      newPath.add(parameter);
      Node argNode;
      try {
        if (config.getObject(parameter.getType()) != null) {
          argNode = config.decorateNode(new ObjectNode(parameter.getType()));
        } else {
          Object argument = config.getConstructorArgument(implType, parameter);
          if (argument == null) {
            // TODO don't ever return null. if it's eligible for building it's a failure if it doesn't
            Node createdNode = createNode(parameter.getType(), config, newPath, failureAccumulator);
            if (createdNode != null) {
              argNode = createdNode;
            } else if (parameter.isNullable()) {
              argNode = config.decorateNode(new ArgumentNode(parameter.getType(), null));
            } else {
              throw new NoConstructorArgumentException(newPath, "No value available for non-nullable parameter " +
                                                           parameter.getFullName());
            }
          } else {
            argNode = config.decorateNode(new ArgumentNode(parameter.getType(), argument));
          }
        }
      } catch (GraphBuildException e) {
        failureAccumulator.add(e);
        argNode = new ExceptionNode(e);
      }
      constructorArguments.add(argNode);
    }
    Node node;
    if (type.isInterface()) {
      node = new InterfaceNode(type, implType, constructorArguments);
    } else {
      node = new ClassNode(implType, constructorArguments);
    }
    return config.decorateNode(node);
  }

  public InvokableFunction build(ComponentMap components) {
    Object receiver = _root.create(components);
    return _rootMetadata.getInvokableFunction(receiver);
  }

  /**
   * @param type A type
   * @return true If the system should try to build an instance
   */
  private static boolean isEligibleForBuilding(Class<?> type) {
    if (INELIGIBLE_TYPES.contains(type)) {
      return false;
    }
    if (type.isPrimitive()) {
      return false;
    }
    Package pkg = type.getPackage();
    String packageName = pkg.getName();
    if (packageName.startsWith("java")) {
      return false;
    }
    if (packageName.startsWith("org.threeten")) {
      return false;
    }
    return true;
  }

  // TODO pretty print method
}
