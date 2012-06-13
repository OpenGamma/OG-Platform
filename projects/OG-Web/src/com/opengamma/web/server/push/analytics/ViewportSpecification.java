/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

/**
 * TODO should there be subclasses for portfolio, depgraph, primitives? and associated visitors
 */
public class ViewportSpecification {

  /*private final List<Integer> _rows;
  private final List<Integer> _columns;*/

  public static ViewportSpecification empty() {
    return new ViewportSpecification();
  }
}
