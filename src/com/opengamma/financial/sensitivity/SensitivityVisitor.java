package com.opengamma.financial.sensitivity;

public interface SensitivityVisitor<T> {

  public T visitValueDelta();

  public T visitValueGamma();

  public T visitValueVega();

  public T visitValueVanna();

  public T visitValueTheta();

  public T visitPV01();

  public T visitConvexity();
}
