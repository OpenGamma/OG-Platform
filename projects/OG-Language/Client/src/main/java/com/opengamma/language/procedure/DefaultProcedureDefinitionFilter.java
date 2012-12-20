/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import com.opengamma.language.definition.DefaultDefinitionFilter;

/**
 * A default filter for procedure definitions that will leave the definition unchanged. 
 */
public class DefaultProcedureDefinitionFilter extends DefaultDefinitionFilter<Definition, MetaProcedure> implements
    ProcedureDefinitionFilter {

}
