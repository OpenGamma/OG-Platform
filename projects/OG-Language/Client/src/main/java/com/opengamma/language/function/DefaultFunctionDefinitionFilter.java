/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import com.opengamma.language.definition.DefaultDefinitionFilter;

/**
 * A default filter for function definitions that will leave the definition unchanged. 
 */
public class DefaultFunctionDefinitionFilter extends DefaultDefinitionFilter<Definition, MetaFunction> implements
    FunctionDefinitionFilter {

}
