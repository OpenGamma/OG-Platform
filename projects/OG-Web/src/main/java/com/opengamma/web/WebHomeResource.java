/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.ImmutableList;
import com.opengamma.web.config.WebConfigData;
import com.opengamma.web.config.WebConfigUris;
import com.opengamma.web.config.WebConfigsResource;
import com.opengamma.web.convention.WebConventionData;
import com.opengamma.web.convention.WebConventionUris;
import com.opengamma.web.convention.WebConventionsResource;
import com.opengamma.web.exchange.WebExchangeData;
import com.opengamma.web.exchange.WebExchangeUris;
import com.opengamma.web.exchange.WebExchangesResource;
import com.opengamma.web.function.WebFunctionData;
import com.opengamma.web.function.WebFunctionUris;
import com.opengamma.web.function.WebFunctionsResource;
import com.opengamma.web.historicaltimeseries.WebAllHistoricalTimeSeriesResource;
import com.opengamma.web.historicaltimeseries.WebHistoricalTimeSeriesData;
import com.opengamma.web.historicaltimeseries.WebHistoricalTimeSeriesUris;
import com.opengamma.web.holiday.WebHolidayData;
import com.opengamma.web.holiday.WebHolidayUris;
import com.opengamma.web.holiday.WebHolidaysResource;
import com.opengamma.web.legalentity.WebLegalEntitiesResource;
import com.opengamma.web.legalentity.WebLegalEntityData;
import com.opengamma.web.legalentity.WebLegalEntityUris;
import com.opengamma.web.namedsnapshot.WebNamedSnapshotData;
import com.opengamma.web.namedsnapshot.WebNamedSnapshotUris;
import com.opengamma.web.namedsnapshot.WebNamedSnapshotsResource;
import com.opengamma.web.portfolio.WebPortfoliosData;
import com.opengamma.web.portfolio.WebPortfoliosResource;
import com.opengamma.web.portfolio.WebPortfoliosUris;
import com.opengamma.web.position.WebPositionsData;
import com.opengamma.web.position.WebPositionsResource;
import com.opengamma.web.position.WebPositionsUris;
import com.opengamma.web.region.WebRegionData;
import com.opengamma.web.region.WebRegionUris;
import com.opengamma.web.region.WebRegionsResource;
import com.opengamma.web.security.WebSecuritiesData;
import com.opengamma.web.security.WebSecuritiesResource;
import com.opengamma.web.security.WebSecuritiesUris;
import com.opengamma.web.user.WebRoleData;
import com.opengamma.web.user.WebRoleUris;
import com.opengamma.web.user.WebRolesResource;
import com.opengamma.web.user.WebUserData;
import com.opengamma.web.user.WebUserUris;
import com.opengamma.web.user.WebUsersResource;

/**
 * RESTful resource for the home page.
 */
@Path("/")
public class WebHomeResource extends AbstractSingletonWebResource {

  private static final ImmutableList<ResourceConfig> RESOURCE_CONFIGS;
  private static final List<ResourceConfig> s_resourceConfigs = new CopyOnWriteArrayList<>();
  static {
    ImmutableList.Builder<ResourceConfig> builder = ImmutableList.builder();
    builder.add(new ResourceConfig(WebConfigsResource.class, WebConfigData.class, WebConfigUris.class, "configUris"));
    builder.add(new ResourceConfig(WebConventionsResource.class, WebConventionData.class, WebConventionUris.class, "conventionUris"));
    builder.add(new ResourceConfig(WebExchangesResource.class, WebExchangeData.class, WebExchangeUris.class, "exchangeUris"));
    builder.add(new ResourceConfig(WebFunctionsResource.class, WebFunctionData.class, WebFunctionUris.class, "functionUris"));
    builder.add(new ResourceConfig(WebAllHistoricalTimeSeriesResource.class, WebHistoricalTimeSeriesData.class, WebHistoricalTimeSeriesUris.class, "timeseriesUris"));
    builder.add(new ResourceConfig(WebHolidaysResource.class, WebHolidayData.class, WebHolidayUris.class, "holidayUris"));
    builder.add(new ResourceConfig(WebLegalEntitiesResource.class, WebLegalEntityData.class, WebLegalEntityUris.class, "legalEntityUris"));
    builder.add(new ResourceConfig(WebNamedSnapshotsResource.class, WebNamedSnapshotData.class, WebNamedSnapshotUris.class, "namedSnapshotUris"));
    builder.add(new ResourceConfig(WebPortfoliosResource.class, WebPortfoliosData.class, WebPortfoliosUris.class, "portfolioUris"));
    builder.add(new ResourceConfig(WebPositionsResource.class, WebPositionsData.class, WebPositionsUris.class, "positionUris"));
    builder.add(new ResourceConfig(WebRegionsResource.class, WebRegionData.class, WebRegionUris.class, "regionUris"));
    builder.add(new ResourceConfig(WebRolesResource.class, WebRoleData.class, WebRoleUris.class, "roleUris"));
    builder.add(new ResourceConfig(WebSecuritiesResource.class, WebSecuritiesData.class, WebSecuritiesUris.class, "securityUris"));
    builder.add(new ResourceConfig(WebUsersResource.class, WebUserData.class, WebUserUris.class, "userUris"));
    RESOURCE_CONFIGS = builder.build();
  }

  private final Set<Class<?>> _publishedTypes;

  /**
   * Registers a new home page link.
   * <p>
   * This method is not intended for general use and may disappear without warning in the future.
   * 
   * @param resourceType  the resource type
   * @param dataType  the type of the web data class
   * @param urisType  the type of the web uri class
   * @param name  the name exposed to Freemarker
   */
  public static void registerHomePageLink(
      Class<?> resourceType, Class<? extends Bean> dataType, Class<?> urisType, String name) {
    
    ResourceConfig config = new ResourceConfig(resourceType, dataType, urisType, name);
    s_resourceConfigs.add(config);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the resource.
   * 
   * @param publishedTypes  the set of published types
   */
  public WebHomeResource(Set<Class<?>> publishedTypes) {
    _publishedTypes = publishedTypes;
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(@Context ServletContext servletContext, @Context UriInfo uriInfo) {
    FlexiBean out = createRootData(uriInfo);
    return getFreemarker(servletContext).build("home.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @param uriInfo  the URI information, not null
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData(UriInfo uriInfo) {
    FlexiBean out = super.createRootData(uriInfo);
    out.put("uris", new WebHomeUris(uriInfo));
    
    for (ResourceConfig config : RESOURCE_CONFIGS) {
      if (_publishedTypes.contains(config._resourceType)) {
        Object uriObj = createUriObj(config, uriInfo);
        out.put(config._name, uriObj);
      }
    }
    for (ResourceConfig config : s_resourceConfigs) {
      Object uriObj = createUriObj(config, uriInfo);
      out.put(config._name, uriObj);
    }
    return out;
  }

  private Object createUriObj(ResourceConfig resourceConfig, UriInfo uriInfo) {
    try {
      Bean dataInstance = resourceConfig._dataClazz.newInstance();
      dataInstance.property("uriInfo").set(uriInfo);
      return resourceConfig._uris.getConstructor(resourceConfig._dataClazz).newInstance(dataInstance);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create uri for resource " + resourceConfig._name);
    }
  }

  private static class ResourceConfig {
    private final Class<?> _resourceType;
    private final Class<? extends Bean> _dataClazz;
    private final Class<?> _uris;
    private final String _name;
    
    public ResourceConfig(Class<?> resourceType, Class<? extends Bean> dataClazz, Class<?> uris, String name) {
      _resourceType = resourceType;
      _dataClazz = dataClazz;
      _uris = uris;
      _name = name;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this page.
   * @param uriInfo  the uriInfo, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(WebHomeResource.class).build();
  }

}
