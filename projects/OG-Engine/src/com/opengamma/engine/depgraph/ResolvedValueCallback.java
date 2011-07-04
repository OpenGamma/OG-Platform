/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Callback interface to receive the results of a resolution.
 */
/* package */interface ResolvedValueCallback {

  void resolved(ValueRequirement valueRequirement, ResolvedValue resolvedValue, ResolutionPump pump);

  void failed(ValueRequirement value);

}
