/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.render;

/**
 * Visitor pattern accept interface for an item that can be rendered.
 */
public interface Renderable {

  /**
   * Accepts the visitor and performs some processing.
   * @param <T>  the visitor type
   * @param visitor  the visitor, not null
   * @return the item
   */
  <T> T accept(RenderVisitor<T> visitor);

}
