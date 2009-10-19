package com.opengamma.financial.render;

public interface Renderable {
  public <T> T accept(RenderVisitor<T> visitor);
}
