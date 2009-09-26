package com.opengamma.plot;

public interface Renderable {
  public <T> T accept(RenderVisitor<T> visitor);
}
