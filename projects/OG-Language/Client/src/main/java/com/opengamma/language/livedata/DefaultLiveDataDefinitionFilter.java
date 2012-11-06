/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.definition.DefaultDefinitionFilter;

/**
 * A default filter for live data definitions that will leave the definition unchanged. 
 */
public class DefaultLiveDataDefinitionFilter extends DefaultDefinitionFilter<Definition, MetaLiveData> implements
    LiveDataDefinitionFilter {

}
