/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to document a parameter on a method (or constructor) that is
 * exposed to the external language stack by the {@link ExternalFunction}
 * annotation. Use of this annotation is optional, available to provide names,
 * additional type information or descriptions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface ExternalFunctionParam {

  /**
   * The name of the parameter. If not specified, a name will be inferred from
   * the parameter order, for example, first param = a, second = b, etc.
   */
  String name() default "";

  /**
   * Whether to allow null to be passed.
   */
  boolean allowNull() default true;

  /**
   * The logical type of the parameter. If omitted, the actual type will be
   * used. This is only necessary in the case of parameterized types or if the
   * application conventions requires a stricter sub-class than the method
   * signature indicates.
   */
  String type() default "";

  /**
   * A brief description of the parameter to show to the user. This might be as
   * a tooltip in an interactive environment such as Excel, or appear in
   * generated reference artifacts such as 'man' pages, PDF or HTML
   * documentation.
   * <p>
   * This should be written in complete sentences but with no trailing full
   * stop, properly capitalized. For example, "The number of foos".
   */
  String description() default "";

}
