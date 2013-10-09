/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.LongShort;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.analytics.OtcSecurityVisitor;

/**
 * REST resource for the trade blotter and trade entry forms.
 */
@Path("blotter")
public class BlotterResource {

  /** Map of underlying security types keyed by the owning security type. */
  private static final Map<Class<?>, Class<?>> s_underlyingSecurityTypes = ImmutableMap.<Class<?>, Class<?>>of(
      IRFutureOptionSecurity.class, InterestRateFutureSecurity.class,
      SwaptionSecurity.class, SwapSecurity.class,
      CreditDefaultSwapOptionSecurity.class, AbstractCreditDefaultSwapSecurity.class);

  /** Map of paths to the endpoints for looking up values, keyed by the value class. */
  private static final Map<Class<?>, String> s_endpoints = Maps.newHashMap();
  /** OTC Security types that can be created by the trade entry froms. */
  private static final List<String> s_securityTypeNames = Lists.newArrayList();
  /** Types that can be created by the trade entry forms that aren't securities by are required by them (e.g. legs). */
  private static final List<String> s_otherTypeNames = Lists.newArrayList();
  /** Meta beans for types that can be created by the trade entry forms keyed by type name. */
  private static final Map<String, MetaBean> s_metaBeansByTypeName = Maps.newHashMap();

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
    s_endpoints.put(DebtSeniority.class, "debtseniority");
    s_endpoints.put(RestructuringClause.class, "restructuringclause");
    s_endpoints.put(StubType.class, "stubtype");
    s_endpoints.put(InterpolationMethod.class, "interpolationmethods");

    for (MetaBean metaBean : BlotterUtils.getMetaBeans()) {
      Class<? extends Bean> beanType = metaBean.beanType();
      String typeName = beanType.getSimpleName();
      s_metaBeansByTypeName.put(typeName, metaBean);
      if (isSecurity(beanType)) {
        s_securityTypeNames.add(typeName);
      } else {
        s_otherTypeNames.add(typeName);
      }
    }
    s_otherTypeNames.add(OtcTradeBuilder.TRADE_TYPE_NAME);
    s_otherTypeNames.add(FungibleTradeBuilder.TRADE_TYPE_NAME);
    Collections.sort(s_otherTypeNames);
    Collections.sort(s_securityTypeNames);
  }

  /** For loading and saving securities. */
  private final SecurityMaster _securityMaster;
  /** For loading and saving positions. */
  private final PositionMaster _positionMaster;
  /** For creating output from Freemarker templates. */
  private FreemarkerOutputter _freemarker;
  /** For building and saving new trades and associated securities. */
  private final OtcTradeBuilder _otcTradeBuilder;
  /** For building trades in fungible securities. */
  private final FungibleTradeBuilder _fungibleTradeBuilder;
  /** For removing positions. */
  private final PortfolioMaster _portfolioMaster;

  public BlotterResource(SecurityMaster securityMaster, PortfolioMaster portfolioMaster, PositionMaster positionMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _fungibleTradeBuilder = new FungibleTradeBuilder(_positionMaster,
                                                     _portfolioMaster,
                                                     _securityMaster,
                                                     BlotterUtils.getMetaBeans(),
                                                     BlotterUtils.getStringConvert());
    _otcTradeBuilder = new OtcTradeBuilder(_positionMaster,
                                           _portfolioMaster,
                                           _securityMaster,
                                           BlotterUtils.getMetaBeans(),
                                           BlotterUtils.getStringConvert());
  }

  /* package */ static boolean isSecurity(Class<?> type) {
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
    Map<String, Object> beanData;
    // TODO tell don't ask
    if (typeName.equals(OtcTradeBuilder.TRADE_TYPE_NAME)) {
      beanData = OtcTradeBuilder.tradeStructure();
    } else if (typeName.equals(FungibleTradeBuilder.TRADE_TYPE_NAME)) {
      beanData = FungibleTradeBuilder.tradeStructure();
    } else {
      MetaBean metaBean = s_metaBeansByTypeName.get(typeName);
      if (metaBean == null) {
        throw new DataNotFoundException("Unknown type name " + typeName);
      }
      BeanStructureBuilder structureBuilder = new BeanStructureBuilder(BlotterUtils.getMetaBeans(),
                                                                       s_underlyingSecurityTypes,
                                                                       s_endpoints,
                                                                       BlotterUtils.getStringConvert());
      BeanTraverser traverser = BlotterUtils.structureBuildingTraverser();
      beanData = (Map<String, Object>) traverser.traverse(metaBean, structureBuilder);
    }
    return _freemarker.build("blotter/bean-structure.ftl", beanData);
  }

  // for getting the security for fungible trades - the user can change the ID and see the trade details update

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("securities/{securityExternalId}")
  public String getSecurityJSON(@PathParam("securityExternalId") String securityExternalIdStr) {
    if (StringUtils.isEmpty(securityExternalIdStr)) {
      return new JSONObject().toString();
    }
    ExternalId securityExternalId = ExternalId.parse(securityExternalIdStr);
    SecuritySearchResult searchResult = _securityMaster.search(new SecuritySearchRequest(securityExternalId));
    if (searchResult.getSecurities().size() == 0) {
      throw new DataNotFoundException("No security found with ID " + securityExternalId);
    }
    ManageableSecurity security = searchResult.getFirstSecurity();
    BeanVisitor<JSONObject> securityVisitor =
        new BuildingBeanVisitor<>(security, new JsonDataSink(BlotterUtils.getJsonBuildingConverters()));
    BeanTraverser securityTraverser = BlotterUtils.securityJsonBuildingTraverser();
    MetaBean securityMetaBean = JodaBeanUtils.metaBean(security.getClass());
    JSONObject securityJson = (JSONObject) securityTraverser.traverse(securityMetaBean, securityVisitor);
    return securityJson.toString();
  }

  // for getting the trade and security data for an OTC trade/security/position
  // TODO move this logic to trade builder? can it populate a BeanDataSink to build the JSON?
  // TODO refactor this, it's ugly. can the security and trade logic be cleanly moved into classes for OTC & fungible?
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("trades/{tradeId}")
  public String getTradeJSON(@PathParam("tradeId") String tradeIdStr) {
    UniqueId tradeId = UniqueId.parse(tradeIdStr);
    if (!tradeId.isLatest()) {
      throw new IllegalArgumentException("The blotter can only be used to update the latest version of a trade");
    }
    ManageableTrade trade = _positionMaster.getTrade(tradeId);
    ManageableSecurityLink securityLink = trade.getSecurityLink();
    return buildTradeJSON(trade, securityLink);
  }

  // TODO does it matter if the trade is not the most recent?
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("trades/{tradeId}")
  public Response deleteTrade(@PathParam("tradeId") String tradeIdStr) {
    UniqueId tradeId = UniqueId.parse(tradeIdStr);
    ManageableTrade trade = _positionMaster.getTrade(tradeId);
    PositionDocument positionDoc = _positionMaster.get(trade.getParentPositionId());
    positionDoc.getPosition().removeTrade(trade);
    _positionMaster.update(positionDoc);
    return Response.ok().build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("positions/{positionId}")
  public String getPositionJSON(@PathParam("positionId") String positionIdStr) {
    UniqueId positionId = UniqueId.parse(positionIdStr);
    if (!positionId.isLatest()) {
      throw new IllegalArgumentException("The blotter can only be used to update the latest version of a trade");
    }
    ManageablePosition position = _positionMaster.get(positionId).getPosition();
    ManageableSecurityLink securityLink = position.getSecurityLink();
    ManageableTrade trade = new ManageableTrade();
    trade.setSecurityLink(securityLink);
    if (position.getTrades().size() == 0) {
      trade.setTradeDate(LocalDate.now());
      trade.setCounterpartyExternalId(ExternalId.of(AbstractTradeBuilder.CPTY_SCHEME, AbstractTradeBuilder.DEFAULT_COUNTERPARTY));
      trade.setQuantity(position.getQuantity());
    }
    return buildTradeJSON(trade, securityLink);
  }

  private String buildTradeJSON(ManageableTrade trade, ManageableSecurityLink securityLink) {
    ManageableSecurity security = findSecurity(securityLink);
    JSONObject root = new JSONObject();
    try {
      JsonDataSink tradeSink = new JsonDataSink(BlotterUtils.getJsonBuildingConverters());
      if (isOtc(security)) {
        _otcTradeBuilder.extractTradeData(trade, tradeSink);
        MetaBean securityMetaBean = s_metaBeansByTypeName.get(security.getClass().getSimpleName());
        if (securityMetaBean == null) {
          throw new DataNotFoundException("No MetaBean is registered for security type " + security.getClass().getName());
        }
        BeanVisitor<JSONObject> securityVisitor =
            new BuildingBeanVisitor<>(security, new JsonDataSink(BlotterUtils.getJsonBuildingConverters()));
        BeanTraverser securityTraverser = BlotterUtils.securityJsonBuildingTraverser();
        JSONObject securityJson = (JSONObject) securityTraverser.traverse(securityMetaBean, securityVisitor);
        if (security instanceof FinancialSecurity) {
          UnderlyingSecurityVisitor visitor = new UnderlyingSecurityVisitor(VersionCorrection.LATEST, _securityMaster);
          ManageableSecurity underlying = ((FinancialSecurity) security).accept(visitor);
          if (underlying != null) {
            BeanVisitor<JSONObject> underlyingVisitor =
                new BuildingBeanVisitor<>(underlying, new JsonDataSink(BlotterUtils.getJsonBuildingConverters()));
            MetaBean underlyingMetaBean = s_metaBeansByTypeName.get(underlying.getClass().getSimpleName());
            JSONObject underlyingJson = (JSONObject) securityTraverser.traverse(underlyingMetaBean, underlyingVisitor);
            root.put("underlying", underlyingJson);
          }
        }
        root.put("security", securityJson);
      } else {
        _fungibleTradeBuilder.extractTradeData(trade, tradeSink, BlotterUtils.getStringConvert());
      }
      JSONObject tradeJson = tradeSink.finish();
      root.put("trade", tradeJson);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to build JSON", e);
    }
    return root.toString();
  }

  private static boolean isOtc(ManageableSecurity security) {
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(new OtcSecurityVisitor());
    } else {
      return false;
    }
  }

  /**
   * Finds the security referred to by securityLink. This basically does the same thing as resolving the link but
   * it returns a {@link ManageableSecurity} instead of a {@link Security}.
   * @param securityLink Contains the security ID(s)
   * @return The security, not null
   * @throws DataNotFoundException If a matching security can't be found
   */
  private ManageableSecurity findSecurity(ManageableSecurityLink securityLink) {
    SecurityDocument securityDocument;
    if (securityLink.getObjectId() != null) {
      // TODO do we definitely want the LATEST?
      securityDocument = _securityMaster.get(securityLink.getObjectId(), VersionCorrection.LATEST);
    } else {
      SecuritySearchRequest searchRequest = new SecuritySearchRequest(securityLink.getExternalId());
      SecuritySearchResult searchResult = _securityMaster.search(searchRequest);
      securityDocument = searchResult.getFirstDocument();
      if (securityDocument == null) {
        throw new DataNotFoundException("No security found with external IDs " + securityLink.getExternalId());
      }
    }
    return securityDocument.getSecurity();
  }

  @POST
  @Path("trades")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createTrade(@Context UriInfo uriInfo, @FormParam("trade") String jsonStr) {
    // TODO need to make sure this can't be used for updating existing trades, only for creating new ones
    try {
      JSONObject json = new JSONObject(jsonStr);
      JSONObject tradeJson = json.getJSONObject("trade");
      UniqueId nodeId = UniqueId.parse(json.getString("nodeId"));
      String tradeTypeName = tradeJson.getString("type");
      // TODO tell don't ask - it is an option to ask each of the trade builders? but their interfaces are different
      // they would have to know how to extract the subsections of the JSON
      UniqueId tradeId;
      if (tradeTypeName.equals(OtcTradeBuilder.TRADE_TYPE_NAME)) {
        tradeId =  createOtcTrade(json, tradeJson, nodeId);
      } else if (tradeTypeName.equals(FungibleTradeBuilder.TRADE_TYPE_NAME)) {
        tradeId = _fungibleTradeBuilder.addTrade(new JsonBeanDataSource(tradeJson), nodeId);
      } else {
        throw new IllegalArgumentException("Unknown trade type " + tradeTypeName);
      }
      URI createdTradeUri = uriInfo.getAbsolutePathBuilder().path(tradeId.getObjectId().toString()).build();
      return Response.status(Response.Status.CREATED).header("Location", createdTradeUri).build();
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to parse JSON", e);
    }
  }

  /* TODO endpoint for updating positions that don't have any trades?
  create a dummy trade for the position amount?
  what about
    * positions with trades whose position and trade amounts are inconsistent
    * adding trade to positions with no trades but a position amount? create 2 trades?
    * editing a position but not by adding a trade? leave empty and update the amount?
  should editing a position always leave it with a consistent set of trades? even if it's empty?
  */

  @PUT
  @Path("positions/{positionIdStr}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public void updatePosition(@FormParam("trade") String jsonStr, @PathParam("positionIdStr") String positionIdStr) {
    try {
      UniqueId positionId = UniqueId.parse(positionIdStr);
      JSONObject json = new JSONObject(jsonStr);
      JSONObject tradeJson = json.getJSONObject("trade");
      String tradeTypeName = tradeJson.getString("type");
      // TODO tell don't ask - ask each of the existing trade builders until one of them can handle it?
      if (tradeTypeName.equals(OtcTradeBuilder.TRADE_TYPE_NAME)) {
        updateOtcPosition(positionId, json, tradeJson);
      } else if (tradeTypeName.equals(FungibleTradeBuilder.TRADE_TYPE_NAME)) {
        _fungibleTradeBuilder.updatePosition(new JsonBeanDataSource(tradeJson), positionId);
      } else {
        throw new IllegalArgumentException("Unknown trade type " + tradeTypeName);
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to parse JSON", e);
    }
  }

  @PUT
  @Path("trades/{tradeIdStr}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public void updateTrade(@FormParam("trade") String jsonStr, @PathParam("tradeIdStr") String tradeIdStr) {
    try {
      // TODO what should happen to this? the ID is also in the JSON. check they match?
      @SuppressWarnings("unused")
      UniqueId tradeId = UniqueId.parse(tradeIdStr);
      JSONObject json = new JSONObject(jsonStr);
      JSONObject tradeJson = json.getJSONObject("trade");
      String tradeTypeName = tradeJson.getString("type");
      // TODO tell don't ask - ask each of the existing trade builders until one of them can handle it?
      if (tradeTypeName.equals(OtcTradeBuilder.TRADE_TYPE_NAME)) {
        createOtcTrade(json, tradeJson, null);
      } else if (tradeTypeName.equals(FungibleTradeBuilder.TRADE_TYPE_NAME)) {
        _fungibleTradeBuilder.updateTrade(new JsonBeanDataSource(tradeJson));
      } else {
        throw new IllegalArgumentException("Unknown trade type " + tradeTypeName);
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to parse JSON", e);
    }
  }

  // TODO this could move inside the builder
  private UniqueId createOtcTrade(JSONObject json, JSONObject tradeJson, UniqueId nodeId) {
    try {
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
      if (nodeId == null) {
        return _otcTradeBuilder.updateTrade(tradeData, securityData, underlyingData);
      } else {
        return _otcTradeBuilder.addTrade(tradeData, securityData, underlyingData, nodeId);
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to parse JSON", e);
    }
  }

  private UniqueId updateOtcPosition(UniqueId positionId, JSONObject json, JSONObject tradeJson) {
    try {
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
      return _otcTradeBuilder.updatePosition(positionId, tradeData, securityData, underlyingData);
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
    return new BlotterLookupResource(BlotterUtils.getStringConvert());
  }
}
