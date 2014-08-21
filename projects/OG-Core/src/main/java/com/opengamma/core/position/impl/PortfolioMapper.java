/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opengamma.core.position.PortfolioNode;

/**
 * Contains utilities to transform the nodes and positions in a portfolio using a {@code PortfolioMapperFunction}.
 */
public final class PortfolioMapper {

  private PortfolioMapper() {
  }

  //-------------------------------------------------------------------------
  public static <T> List<T> map(PortfolioNode node, PortfolioMapperFunction<T> fn) {
    MappingCallback<T, List<T>> callback = new MappingCallback<T, List<T>>(fn, new ArrayList<T>());
    return getValues(node, callback);
  }

  public static <T> List<T> flatMap(PortfolioNode node, PortfolioMapperFunction<List<T>> fn) {
    MappingCallback<List<T>, List<List<T>>> callback =
        new MappingCallback<List<T>, List<List<T>>>(fn, new ArrayList<List<T>>());
    List<List<T>> values = getValues(node, callback);
    return Lists.newArrayList(Iterables.concat(values));
  }

  public static <T> Set<T> mapToSet(PortfolioNode node, PortfolioMapperFunction<T> fn) {
    MappingCallback<T, Set<T>> callback = new MappingCallback<T, Set<T>>(fn, new HashSet<T>());
    return getValues(node, callback);
  }
  
  //-------------------------------------------------------------------------
  private static <T, V extends Collection<T>> V getValues(PortfolioNode node, MappingCallback<T, V> callback) {
    PortfolioNodeTraverser traverser = PortfolioNodeTraverser.depthFirst(callback);
    traverser.traverse(node);
    return callback.getValues();
  }
  
}
