/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

/**
 * A filter for converting identifiers from the (probably) Java-esque meta descriptions present here to
 * a form compliant with a bound language's syntax rules.
 */
public interface IdentifierFilter {

  /**
   * Converts the entity identifier to a valid form. Returning null will prevent a definition
   * from being exposed to a client, although it is better to do this by controlling the
   * provider instances.
   * 
   * @param name the identifier
   * @return the validated identifier, null if it cannot be converted
   */
  String convertName(String name);

  /**
   * Converts an alias to a valid form given the converted identifier.
   * 
   * @param name the primary identifier, as validated by {@link #convertName}
   * @param alias the alias
   * @return the validated alias, null if it cannot be converted
   */
  String convertAlias(String name, String alias);

  /**
   * Converts a parameter identifier to a valid form. Returning null will prevent a definition
   * from being exposed to a client. If a parameter identifier cannot be converted, it is usually best
   * to autogenerate something that is unique (for the function) and syntactically valid.
   * 
   * @param entityName identifier of the containing entity, as validated by {@link #convertName}
   * @param parameterName the parameter
   * @return the validated parameter, null if the entity should be invalid
   */
  String convertParameter(String entityName, String parameterName);

}
