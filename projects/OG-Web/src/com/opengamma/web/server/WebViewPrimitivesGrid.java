/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.EnumSet;
import java.util.Map;

import org.cometd.Client;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * Represents a primitives grid
 */
public class WebViewPrimitivesGrid extends WebViewGrid {

  protected WebViewPrimitivesGrid(CompiledViewDefinition compiledViewDefinition, ResultConverterCache resultConverterCache, Client local, Client remote) {
    super("primitives", compiledViewDefinition, null, EnumSet.of(ComputationTargetType.PRIMITIVE),
        resultConverterCache, local, remote, "");
  }

  @Override
  protected void addRowDetails(UniqueIdentifier target, long rowId, Map<String, Object> details) {
    // TODO: resolve the target and use a more sensible name
    details.put("name", target.toString());
  }
  
}
