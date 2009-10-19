package com.opengamma.engine.viewer;

public interface Renderable {
  public <T> T accept(RenderVisitor<T> visitor);
}
