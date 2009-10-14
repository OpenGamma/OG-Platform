package com.opengamma.engine.viewer;

public interface VisitableValueDefinition {
  public <T> T accept(ValueDefinitionVisitor<T> visitor);
}
