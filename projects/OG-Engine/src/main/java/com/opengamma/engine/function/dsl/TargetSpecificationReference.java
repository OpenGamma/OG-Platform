/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

/**
 * Target specification reference.
 */
public final class TargetSpecificationReference {

  private TargetSpecificationReference() {
  }

  public static TargetSpecificationReference originalTarget() {
    return new TargetSpecificationReference();
  }

}
