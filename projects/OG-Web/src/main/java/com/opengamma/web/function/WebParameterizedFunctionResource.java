/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.function;

import java.net.URI;
import java.util.SortedMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.base.Preconditions;

/**
 * RESTful resource for a parameterized function.
 */
@Path("/functions/parameterziedfunction/{functionName}")
public class WebParameterizedFunctionResource extends AbstractWebFunctionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebParameterizedFunctionResource(final AbstractWebFunctionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "parameterizedfunction.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    
    WebFunctionQueryDelegate queryDelegate = new WebFunctionQueryDelegate(data().getFunctionSource());
    SortedMap<String, WebFunctionTypeDetails> allFunctions = queryDelegate.queryAll();
    String functionName = data().getUriFunctionName();
    WebFunctionTypeDetails typeDetails = allFunctions.get(data().getUriFunctionName());
    
    Preconditions.checkNotNull(typeDetails, "Couldn't find %s", functionName);
    
    FlexiBean out = super.createRootData();
    out.put("searchResult", typeDetails);
    return out;
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param functionName  the override function id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebFunctionData data, final String functionName) {
    return data.getUriInfo().getBaseUriBuilder().path(WebParameterizedFunctionResource.class).build(functionName);
  }

}
