/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.time.calendar.ZonedDateTime;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.LongShort;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.web.FreemarkerOutputter;

/**
 * TODO move some of this into subresources?
 * TODO date and time zone handling
 */
@Path("blotter")
public class BlotterResource {

  // TODO this should be configurable, should be able to add from client projects
  // TODO where should these live? is this the right place?
  /**
   * All the securities and related types supported by the blotter.
   */
  /* package */ static final Set<MetaBean> s_metaBeans = Sets.<MetaBean>newHashSet(
      FXForwardSecurity.meta(),
      SwapSecurity.meta(),
      SwaptionSecurity.meta(),
      FixedInterestRateLeg.meta(),
      FloatingInterestRateLeg.meta(),
      FloatingSpreadIRLeg.meta(),
      FloatingGearingIRLeg.meta(),
      InterestRateNotional.meta(),
      CapFloorCMSSpreadSecurity.meta(),
      NonDeliverableFXOptionSecurity.meta(),
      FXOptionSecurity.meta(),
      FRASecurity.meta(),
      CapFloorSecurity.meta(),
      EquityVarianceSwapSecurity.meta(),
      FXBarrierOptionSecurity.meta(),
      NonDeliverableFXOptionSecurity.meta(),
      ManageableTrade.meta());

  private static final Map<Class<?>, Class<?>> s_underlyingSecurityTypes = ImmutableMap.<Class<?>, Class<?>>of(
      IRFutureOptionSecurity.class, InterestRateFutureSecurity.class,
      SwaptionSecurity.class, SwapSecurity.class);

  private static final Map<Class<?>, String> s_endpoints = Maps.newHashMap();

  // TODO this is an ugly but it's only temporay - fix or remove when the HTML bean info isn't needed
  static {
    s_endpoints.put(Frequency.class, "frequencies");
    s_endpoints.put(ExerciseType.class, "exercisetypes");
    s_endpoints.put(DayCount.class, "daycountconventions");
    s_endpoints.put(BusinessDayConvention.class, "businessdayconventions");
    s_endpoints.put(BarrierType.class, "barriertypes");
    s_endpoints.put(BarrierDirection.class, "barrierdirections");
    s_endpoints.put(SamplingFrequency.class, "samplingfrequencies");
    s_endpoints.put(FloatingRateType.class, "floatingratetypes");
    s_endpoints.put(LongShort.class, "longshort");
    s_endpoints.put(MonitoringType.class, "monitoringtype");
  }

  private static final List<String> s_otherTypeNames = Lists.newArrayList();
  private static final List<String> s_securityTypeNames = Lists.newArrayList();
  private static final Map<String, MetaBean> s_metaBeansByTypeName = Maps.newHashMap();

  static {
    JodaBeanConverters.getInstance();
    for (MetaBean metaBean : s_metaBeans) {
      Class<? extends Bean> beanType = metaBean.beanType();
      String typeName = beanType.getSimpleName();
      s_metaBeansByTypeName.put(typeName, metaBean);
      if (isSecurity(beanType)) {
        s_securityTypeNames.add(typeName);
      } else {
        s_otherTypeNames.add(typeName);
      }
    }
    Collections.sort(s_otherTypeNames);
    Collections.sort(s_securityTypeNames);
  }

  private final SecurityMaster _securityMaster;
  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;

  private FreemarkerOutputter _freemarker;

  public BlotterResource(SecurityMaster securityMaster, PortfolioMaster portfolioMaster, PositionMaster positionMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _securityMaster = securityMaster;
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
  }

  /* package */
  static boolean isSecurity(Class<?> type) {
    if (type == null) {
      return false;
    } else if (ManageableSecurity.class.equals(type)) {
      return true;
    } else {
      return isSecurity(type.getSuperclass());
    }
  }

  @Context
  public void setServletContext(final ServletContext servletContext) {
    _freemarker = new FreemarkerOutputter(servletContext);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("types")
  public String getTypes() {
    Map<Object, Object> data = map("title", "Types",
                                   "securityTypeNames", s_securityTypeNames,
                                   "otherTypeNames", s_otherTypeNames);
    return _freemarker.build("blotter/bean-types.ftl", data);
  }

  @SuppressWarnings("unchecked")
  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("types/{typeName}")
  public String getStructure(@PathParam("typeName") String typeName) {
    MetaBean metaBean = s_metaBeansByTypeName.get(typeName);
    if (metaBean == null) {
      throw new DataNotFoundException("Unknown type name " + typeName);
    }
    Map<String, Object> beanData;
    if (typeName.equals("ManageableTrade")) {
      beanData = manageableTradeStructure();
    } else {
      BeanStructureBuilder structureBuilder = new BeanStructureBuilder(s_metaBeans,
                                                                       s_underlyingSecurityTypes,
                                                                       s_endpoints);
      BeanVisitorDecorator propertyNameFilter = new PropertyNameFilter("externalIdBundle");
      BeanTraverser traverser = new BeanTraverser(propertyNameFilter);
      beanData = (Map<String, Object>) traverser.traverse(metaBean, structureBuilder);
    }
    return _freemarker.build("blotter/bean-structure.ftl", beanData);
  }

  // TODO clean this up. move into helper?
  // TODO separate into different methods for OTC and fungible securities
  // TODO move this logic to trade builder? can it populate a BeanDataSink to build the JSON?
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("trades/{tradeId}")
  public String getJSON(@PathParam("tradeId") String tradeIdStr) {
    // TODO this is a bit of a palaver, surely there's something that already does this?
    ObjectId tradeId = ObjectId.parse(tradeIdStr);
    PositionSearchRequest positionSearch = new PositionSearchRequest();
    positionSearch.addTradeObjectId(tradeId);
    PositionSearchResult positions = _positionMaster.search(positionSearch);
    ManageablePosition position = positions.getSinglePosition();
    // TODO this should never fail but there's a bug in position searching ATM
    ManageableTrade trade = position.getTrade(tradeId);
    if (trade == null) {
      throw new IllegalArgumentException("No trade with ID " + tradeId + " found on position " + position.getUniqueId() +
                                             ", this is a known bug (http://jira.opengamma.com/browse/PLAT-2946)");
    }
    // TODO there's no need to look up the security for fungible security types
    ManageableSecurityLink securityLink = position.getSecurityLink();
    SecurityDocument securityDocument;
    if (securityLink.getObjectId() != null) {
      securityDocument = _securityMaster.get(securityLink.getObjectId(), VersionCorrection.LATEST);
    } else {
      SecuritySearchRequest searchRequest = new SecuritySearchRequest(securityLink.getExternalId());
      SecuritySearchResult searchResult = _securityMaster.search(searchRequest);
      securityDocument = searchResult.getFirstDocument();
      if (securityDocument == null) {
        throw new IllegalStateException("No security found with external IDs " + securityLink.getExternalId());
      }
    }
    ManageableSecurity security = securityDocument.getSecurity();
    MetaBean securityMetaBean = s_metaBeansByTypeName.get(security.getClass().getSimpleName());
    if (securityMetaBean == null) {
      throw new DataNotFoundException("No MetaBean is registered for security type " + security.getClass().getName());
    }
    // TODO filter out underlyingId for securities with OTC underlying
    // TODO trade data - need different structure depending on whether the security is OTC
    // OTCs don't need quantity or security ID, trades in fungible securities need both
    BeanVisitor<JSONObject> securityVisitor = new BuildingBeanVisitor<JSONObject>(security, new JsonDataSink());
    PropertyFilter securityPropertyFilter = new PropertyFilter(ManageableSecurity.meta().securityType());
    JSONObject securityJson = (JSONObject) new BeanTraverser(securityPropertyFilter).traverse(securityMetaBean, securityVisitor);
    BeanVisitor<JSONObject> tradeVisitor = new BuildingBeanVisitor<JSONObject>(trade, new JsonDataSink());
    // TODO special handling of counterparty, send value as string not external ID
    // TODO don't filter out quantity for fungible securities
    // TODO include external security ID for fungible securities
    ManageableTrade.Meta tradeMetaBean = ManageableTrade.meta();
    // TODO factor this out, it's repeating logic from the structure building visitor and trade builder
    PropertyFilter tradePropertyFilter = new PropertyFilter(tradeMetaBean.securityLink(), // TODO how will fungible securities work?
                                                            tradeMetaBean.quantity(),
                                                            tradeMetaBean.deal(),
                                                            tradeMetaBean.parentPositionId(),
                                                            tradeMetaBean.providerId(),
                                                            tradeMetaBean.securityLink());
    JSONObject tradeJson = (JSONObject) new BeanTraverser(tradePropertyFilter).traverse(tradeMetaBean, tradeVisitor);
    JSONObject root = new JSONObject();
    try {
      // TODO only include security for OTCs
      root.put("security", securityJson);
      root.put("trade", tradeJson);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("", e);
    }
    // TODO underlying for securities with OTC underlying securities
    return root.toString();
  }

  @POST
  @Path("trades")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public String createOtcTrade(@FormParam("trade") String tradeJsonStr) {
    // TODO no need to create a new builder every time?
    return createOtcTrade(tradeJsonStr, new NewOtcTradeBuilder(_securityMaster, _positionMaster, s_metaBeans));
    // TODO don't return JSON, just set the created header with the URL
  }

  @PUT
  @Path("trades/{tradeIdStr}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  // TODO the config endpoint uses form params for the JSON. why? better to use a MessageBodyWriter?
  public String updateOtcTrade(@FormParam("trade") String tradeJsonStr,
                               @PathParam("tradeIdStr") String tradeIdStr) {
    UniqueId tradeId = UniqueId.parse(tradeIdStr);
    OtcTradeBuilder tradeBuilder = new ExistingOtcTradeBuilder(tradeId, _securityMaster, _positionMaster, s_metaBeans);
    return createOtcTrade(tradeJsonStr, tradeBuilder);
  }

  private String createOtcTrade(String jsonStr, OtcTradeBuilder tradeBuilder) {
    try {
      JSONObject json = new JSONObject(jsonStr);
      JSONObject tradeJson = json.getJSONObject("trade");
      JSONObject securityJson = json.getJSONObject("security");
      JSONObject underlyingJson = json.optJSONObject("underlying");
      BeanDataSource tradeData = new JsonBeanDataSource(tradeJson);
      BeanDataSource securityData = new JsonBeanDataSource(securityJson);
      BeanDataSource underlyingData;
      if (underlyingJson != null) {
        underlyingData = new JsonBeanDataSource(underlyingJson);
      } else {
        underlyingData = null;
      }
      UniqueId tradeId = tradeBuilder.buildAndSaveTrade(tradeData, securityData, underlyingData);
      return new JSONObject(ImmutableMap.of("tradeId", tradeId)).toString();
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to parse JSON", e);
    }
  }

  private static Map<Object, Object> map(Object... values) {
    final Map<Object, Object> result = Maps.newHashMap();
    for (int i = 0; i < values.length / 2; i++) {
      result.put(values[i * 2], values[(i * 2) + 1]);
    }
    result.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return result;
  }

  @Path("lookup")
  public BlotterLookupResource getLookupResource() {
    return new BlotterLookupResource();
  }

  // TODO create fungible trade - identifier and quantity

  // TODO different versions for OTC / non OTC
  // the horror... make this go away
  private static Map<String, Object> manageableTradeStructure() {
    Map<String, Object> structure = Maps.newHashMap();
    List<Map<String, Object>> properties = Lists.newArrayList();
    properties.add(property("uniqueId", true, true, typeInfo("string", "UniqueId")));
    // don't need quantity for OTCs, always 1
    //properties.add(property("quantity", true, false, typeInfo("number", "")));
    properties.add(property("counterparty", false, false, typeInfo("string", "")));
    properties.add(property("tradeDate", true, false, typeInfo("string", "LocalDate")));
    properties.add(property("tradeTime", true, false, typeInfo("string", "OffsetTime")));
    // TODO which premium fields are relevant for OTCs?
    properties.add(property("premium", true, false, typeInfo("number", "")));
    properties.add(property("premiumCurrency", true, false, typeInfo("string", "Currency")));
    properties.add(property("premiumDate", true, false, typeInfo("string", "LocalDate")));
    properties.add(property("premiumTime", true, false, typeInfo("string", "OffsetTime")));
    properties.add(attributesProperty());
    structure.put("type", "ManageableTrade");
    structure.put("properties", properties);
    structure.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return structure;
  }

  private static Map<String, Object> property(String name,
                                              boolean optional,
                                              boolean readOnly,
                                              Map<String, Object> typeInfo) {
    return ImmutableMap.<String, Object>of("name", name,
                                           "type", "single",
                                           "optional", optional,
                                           "readOnly", readOnly,
                                           "types", ImmutableList.of(typeInfo));
  }

  private static Map<String, Object> attributesProperty() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("name", "attributes");
    map.put("type", "map");
    map.put("optional", true); // can't be null but have a default value so client doesn't need to specify
    map.put("readOnly", false);
    map.put("types", ImmutableList.of(typeInfo("string", "")));
    map.put("valueTypes", ImmutableList.of(typeInfo("string", "")));
    return map;
  }

  private static Map<String, Object> typeInfo(String expectedType, String actualType) {
    return ImmutableMap.<String, Object>of("beanType", false, "expectedType", expectedType, "actualType", actualType);
  }
}
