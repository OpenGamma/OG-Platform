/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

/**
 * A filter allowing different or augmented definitions to be substituted for a specific language
 * binding.
 * 
 * @param <WireDefinition> the logical definition to be sent to a client (Fudge serializable), possibly a subclass
 * @param <MetaDefinition> possibly a subclass of {@code WireDefinition} providing additional information to a language specific filter
 */
public interface DefinitionFilter<WireDefinition extends Definition, MetaDefinition extends WireDefinition> {

  /**
   * Return the definition to be published for the given definition. The returned object may be
   * the same, or a substituted one. Returning null will suppress publication of the
   * definition, although it is better to do that through the definition providers.
   * 
   * @param definition raw definition given by a provider
   * @return the definition to be published, null to suppress
   */
  WireDefinition createDefinition(MetaDefinition definition);

}
