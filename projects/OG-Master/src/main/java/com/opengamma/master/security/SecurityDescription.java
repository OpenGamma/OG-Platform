/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying the security/index type of a financial instrument intended to be stored in a {@link SecurityMaster}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface SecurityDescription {
  /**
   * Security or index type.
   */
  String type();
  
  /**
   * A short description of the security type/index to be used in GUIs.
   */
  String description();

}
