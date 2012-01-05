/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.grid;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.conversion.ResultConverterCache;

import java.util.EnumSet;
import java.util.Map;

/**
 * Represents a primitives grid
 * TODO temporary name just to distinguish it from the similarly named class in the parent package
*/
/* package */ class PushWebViewPrimitivesGrid extends PushRequirementBasedWebViewGrid {

  public PushWebViewPrimitivesGrid(ViewClient viewClient,
                                   CompiledViewDefinition compiledViewDefinition,
                                   ResultConverterCache resultConverterCache) {
    super("primitives", viewClient, compiledViewDefinition, null, EnumSet.of(ComputationTargetType.PRIMITIVE), resultConverterCache, "");
  }

  @Override
  protected void addRowDetails(UniqueId target, int rowId, Map<String, Object> details) {
    // TODO: resolve the target and use a more sensible name
    details.put("name", target.toString());
  }
  
  //-------------------------------------------------------------------------

  @Override
  protected void supplementCsvColumnHeaders(String[] headers) {
    headers[0] = "Target";
  }

  @Override
  protected void supplementCsvRowData(int rowId, ComputationTargetSpecification target, String[] row) {
    row[0] = target.toString();
  }

  @Override
  protected int getAdditionalCsvColumnCount() {
    return 1;
  }

  @Override
  protected int getCsvDataColumnOffset() {
    return 1;
  }
  
}
