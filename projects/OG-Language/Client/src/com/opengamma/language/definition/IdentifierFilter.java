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
   * Converts the identifier to a valid form. Returning {@code null} will prevent a definition
   * from being exposed to a client, although it is better to do this by controlling the
   * provider instances.
   * 
   * @param name the identifier
   * @return the validated identifier, or {@code null} if it cannot be converted
   */
  String convertName(String name);

  /**
   * Converts an alias to a valid form given the converted identifier.
   * 
   * @param name the primary identifier, as validated by {@link #convertName}
   * @param alias the alias
   * @return the validated alias, or {@code null} if it cannot be converted
   */
  String convertAlias(String name, String alias);

}
