/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.function;

import static com.google.common.base.Predicates.not;
import static org.springframework.util.StringUtils.isEmpty;

import java.net.URI;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all functions.
 * <p>
 * The functions resource represents the whole of a function master.
 */
@Path("/functions")
public class WebFunctionsResource extends AbstractWebFunctionResource {

  private static String s_parameterizedFunctionUri = "parameterziedfunction";
  
  /**
   * Creates the resource.
   * @param functionConfigurationSource  the function master, not null
   */
  public WebFunctionsResource(final FunctionConfigurationSource functionConfigurationSource) {
    super(functionConfigurationSource);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("name") String name,
      @QueryParam("parameterized") String parameterized,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    //TODO sort: FunctionSearchSortOrder so = buildSortOrder(sort, FunctionSearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, name, parameterized, uriInfo);
    return getFreemarker().build(HTML_DIR + "functions.ftl", out);
  }

  private FlexiBean createSearchResultData(PagingRequest pr, String name, String parameterized, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    WebFunctionSearchRequest searchRequest = new WebFunctionSearchRequest();
    searchRequest.setName(name);
    searchRequest.setParameterized(parameterized);
    
    Predicate<WebFunctionTypeDetails> predicate = buildPredicate(name, parameterized);
    
    out.put("searchRequest", searchRequest); 
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      WebFunctionQueryDelegate queryDelegate = new WebFunctionQueryDelegate(data().getFunctionSource());
      SortedMap<String, WebFunctionTypeDetails> allFunctions = queryDelegate.query(predicate);
      
      int total = allFunctions.size();
      int limit = Ints.min(pr.getLastItem(), total);
      List<WebFunctionTypeDetails> functions = Lists.newArrayList(allFunctions.values()).subList(pr.getFirstItem(), limit);
      WebFunctionSearchResult result = new WebFunctionSearchResult();
      result.setFunctions(functions);
      out.put("searchResult", result);
      out.put("paging", new WebPaging(Paging.of(pr, total), uriInfo));
    }
    return out;
  }

  private Predicate<WebFunctionTypeDetails> buildPredicate(String name, String parameterized) {
    List<Predicate<WebFunctionTypeDetails>> predicates = Lists.newLinkedList();
    if (!isEmpty(parameterized)) {
      if ("Y".equals(parameterized)) {
        predicates.add(new IsParameterized());
      } else if ("N".equals(parameterized)) {
        predicates.add(not(new IsParameterized()));
      }
    }
    
    if (!isEmpty(name)) {
      predicates.add(new NameContains(name));
    }
    
    Predicate<WebFunctionTypeDetails> predicate = Predicates.and(predicates);
    return predicate;
  }

  @Path("parameterziedfunction/{functionName}")
  public WebParameterizedFunctionResource getParameterizedFunction(@PathParam("functionName") String functionName) {
    data().setUriFunctionName(functionName);
    return new WebParameterizedFunctionResource(this);
  }
  
  
  //-------------------------------------------------------------------------
  /**
   * Builds a URI for functions.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebFunctionData data) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebFunctionsResource.class);
    return builder.build();
  }
  
  
  /**
   * Builds a URI for parameterized functions.
   * @param data context data
   * @param functionId the function name
   * @return the uri
   */
  public static URI parameterziedFunctionUri(WebFunctionData data, String functionId) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebFunctionsResource.class);
    builder.path(s_parameterizedFunctionUri).path(functionId).build();
    return builder.build();
  }
  
  

  
  private static class IsParameterized implements Predicate<WebFunctionTypeDetails> {

    @Override
    public boolean apply(WebFunctionTypeDetails input) {
      return input.isParameterized();
    }
    
  }
  
  private static class NameContains implements Predicate<WebFunctionTypeDetails> {

    private final Pattern _pattern;
    
    public NameContains(String pattern) {
      super();
      _pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean apply(WebFunctionTypeDetails input) {
      Matcher matcher = _pattern.matcher(input.getSimpleName());
      return matcher.find();
    }
    
  }


}
