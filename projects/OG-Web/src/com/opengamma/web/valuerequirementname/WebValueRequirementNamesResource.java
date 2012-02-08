/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.valuerequirementname;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.web.AbstractWebResource;

/**
 * RESTful resource that returns the value requirement names for the Web GUI.
 */
@Path("/valuerequirementnames/metaData")
public class WebValueRequirementNamesResource extends AbstractWebResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(WebValueRequirementNamesResource.class);

  /**
   * The value requirement names.
   */
  private static final Set<String> s_valueRequirementNames;
  static {
    final List<String> list = new ArrayList<String>();
    for (Field field : ValueRequirementNames.class.getDeclaredFields()) {
      try {
        list.add((String) field.get(null));
      } catch (Exception e) {
        // Ignore
      }
    }
    Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
    s_valueRequirementNames = new LinkedHashSet<String>(list);
  }

  /**
   * Creates the resource.
   */
  public WebValueRequirementNamesResource() {
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    String result = null;
    try {
      result = new JSONStringer()
          .object()
          .key("types")
          .value(new JSONArray(s_valueRequirementNames))
          .endObject()
          .toString();
    } catch (JSONException ex) {
      s_logger.warn("error creating json document for valueRequirementNames");
    }
    return result;
  }

}
