/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.VersionCorrection;

/**
 * REST interface that produces a JSON list of view definition names for populating the web client.
 */
@Path("viewdefinitions")
public class ViewDefinitionEntriesResource {

  private final ConfigSource _configSource;

  public ViewDefinitionEntriesResource(ConfigSource configSource) {
    _configSource = configSource;
  }

  /**
   * @return {@code [{id: viewDefId1, name: viewDefName1}, {id: viewDefId2, name: viewDefName2}, ...]}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getViewDefinitionEntriesJson() {
    Collection<ConfigItem<ViewDefinition>> viewDefs = _configSource.getAll(ViewDefinition.class, VersionCorrection.LATEST);
    List<Map<String, Object>> viewDefList = new ArrayList<Map<String, Object>>(viewDefs.size());
    for (ConfigItem<ViewDefinition> viewDef : viewDefs) {
      viewDefList.add(ImmutableMap.<String, Object>of("id", viewDef.getObjectId(), "name", viewDef.getName()));
    }
    return new JSONArray(viewDefList).toString();
  }
}
