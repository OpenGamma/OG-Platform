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
 * Annotation to signal that a method should be exposed through the external
 * language stack to appear, for example, as a function in Excel or R.
 * <p>
 * When the annotation is used on a method it means to expose that method. If
 * the method is not static, it can only be exposed if there is an accessible
 * no-arg constructor for the class.
 * <p>
 * When the annotation is used on a constructor it means to expose a function
 * that creates an instance of the object and returns it. Note that this will
 * typically rely on the object to be Fudge serializable to work correctly.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface ExternalFunction {

  // TODO: the package name should be com.opengamma.language.annotation to match OG-Language

  /**
   * The name of the function exposed to callers. If not specified here, a name
   * will be created from the method name (or class name if on a constructor).
   */
  String name() default "";

  /**
   * Alternative names for the function also exposed to callers in addition to
   * the primary name. If omitted, default aliases are created as:
   * <ul>
   *  <li>Class prefixed method name (for methods)
   *  <li>Class name (for constructors)
   *  <li>Package and class prefixed method name (for methods)
   *  <li>Package and class name (for constructors)
   * <ul>
   * With any of the above omitted if they match the published primary name.
   */
  String[] alias() default {
  // empty
  };

  /**
   * Category describing the function's behavior. Typically omit.
   */
  String category() default "";

  /**
   * A brief description of the function to show to the user. This might be as
   * a tooltip in an interactive environment such as Excel, or appear in
   * generated reference artifacts such as 'man' pages, PDF or HTML
   * documentation.
   * <p>
   * This should be written in complete sentences but with no trailing full
   * stop, properly capitalized. For example, "Calculates the foo to bar ratio".
   */
  String description() default "";

}
