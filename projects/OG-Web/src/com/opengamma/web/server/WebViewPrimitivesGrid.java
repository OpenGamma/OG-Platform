/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cometd.Client;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * 
 */
public class WebViewPrimitivesGrid extends WebViewGrid {

  protected WebViewPrimitivesGrid(ViewDefinition viewDefinition, ResultConverterCache resultConverterCache, Client local, Client remote) {
    super("primitives", getTargets(viewDefinition), viewDefinition,
        EnumSet.of(ComputationTargetType.PRIMITIVE), resultConverterCache, local, remote, "");
  }
  
  private static List<UniqueIdentifier> getTargets(ViewDefinition viewDefinition) {
    Set<UniqueIdentifier> targets = new HashSet<UniqueIdentifier>();
    for (ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (specificRequirement.getTargetSpecification().getType() != ComputationTargetType.PRIMITIVE) {
          continue;
        }
        targets.add(specificRequirement.getTargetSpecification().getUniqueId());
      }
    }
    // Want some kind of consistent order
    List<UniqueIdentifier> targetList = new ArrayList<UniqueIdentifier>(targets);
    Collections.sort(targetList);
    return targetList;
  }

  @Override
  protected void addRowDetails(UniqueIdentifier target, long rowId, Map<String, Object> details) {
    // TODO: resolve the target and use a more sensible name
    details.put("name", target.toString());
  }
  
}
