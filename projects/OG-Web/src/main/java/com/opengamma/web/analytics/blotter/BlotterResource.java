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
import java.util.Set;

import javax.servlet.ServletContext;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.WordUtils;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.LongShort;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.analytics.OtcSecurityVisitor;

/**
 * REST resource for the trade blotter and trade entry forms.
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
      CapFloorCMSSpreadSecurity.meta(),
      NonDeliverableFXOptionSecurity.meta(),
      FXOptionSecurity.meta(),
      FRASecurity.meta(),
      CapFloorSecurity.meta(),
      EquityVarianceSwapSecurity.meta(),
      FXBarrierOptionSecurity.meta(),
      FixedInterestRateLeg.meta(),
      FloatingInterestRateLeg.meta(),
      FloatingSpreadIRLeg.meta(),
      FloatingGearingIRLeg.meta(),
      InterestRateNotional.meta());

  /** Map of underlying security types keyed by the owning security type. */
  private static final Map<Class<?>, Class<?>> s_underlyingSecurityTypes = ImmutableMap.<Class<?>, Class<?>>of(
      IRFutureOptionSecurity.class, InterestRateFutureSecurity.class,
      SwaptionSecurity.class, SwapSecurity.class);

  // TODO define all property filters and converters in one place, they're shotgunned all over this package ATM
  /* package */ static final Map<MetaProperty<?>, Converter<?, ?>> s_regionConverters;
  /** Map of paths to the endpoints for looking up values, keyed by the value class. */
  private static final Map<Class<?>, String> s_endpoints = Maps.newHashMap();
  /** OTC Security types that can be created by the trade entry froms. */
  private static final List<String> s_securityTypeNames = Lists.newArrayList();
  /** Types that can be created by the trade entry forms that aren't securities by are required by them (e.g. legs). */
  private static final List<String> s_otherTypeNames = Lists.newArrayList();
  /** Meta beans for types that can be created by the trade entry forms keyed by type name. */
  private static final Map<String, MetaBean> s_metaBeansByTypeName = Maps.newHashMap();
  /** For converting between strings values used by the UI and real objects. */
  private static final StringConvert s_stringConvert;
  /** For converting property values when creating JSON object from trades and securities. */
  // TODO define all property filters and converters in one place, they're shotgunned all over this package ATM
/* package */ static final Converters JSON_BUILDING_CONVERTERS;

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

    // TODO exactly the same properties are needed for the reverse conversion in OtcTradeBuilder. refactor
    // TODO define all property filters and converters in one place, they're shotgunned all over this package ATM
    RegionIdToStringConverter regionIdToStringConverter = new RegionIdToStringConverter();
    s_regionConverters =
        ImmutableMap.<MetaProperty<?>, Converter<?, ?>>of(
            CashSecurity.meta().regionId(), regionIdToStringConverter,
            CreditDefaultSwapSecurity.meta().regionId(), regionIdToStringConverter,
            EquityVarianceSwapSecurity.meta().regionId(), regionIdToStringConverter,
            FRASecurity.meta().regionId(), regionIdToStringConverter,
            SwapLeg.meta().regionId(), regionIdToStringConverter);

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
    // TODO use constants for these
    s_otherTypeNames.add(OtcTradeBuilder.TRADE_TYPE_NAME);
    s_otherTypeNames.add(FungibleTradeBuilder.TRADE_TYPE_NAME);
    Collections.sort(s_otherTypeNames);
    Collections.sort(s_securityTypeNames);

    s_stringConvert = new StringConvert();
    s_stringConvert.register(Frequency.class, new JodaBeanConverters.FrequencyConverter());
    s_stringConvert.register(Currency.class, new JodaBeanConverters.CurrencyConverter());
    s_stringConvert.register(DayCount.class, new JodaBeanConverters.DayCountConverter());
    s_stringConvert.register(ExternalId.class, new JodaBeanConverters.ExternalIdConverter());
    s_stringConvert.register(ExternalIdBundle.class, new JodaBeanConverters.ExternalIdBundleConverter());
    s_stringConvert.register(CurrencyPair.class, new JodaBeanConverters.CurrencyPairConverter());
    s_stringConvert.register(ObjectId.class, new JodaBeanConverters.ObjectIdConverter());
    s_stringConvert.register(UniqueId.class, new JodaBeanConverters.UniqueIdConverter());
    s_stringConvert.register(Expiry.class, new ExpiryConverter());
    s_stringConvert.register(ExerciseType.class, new JodaBeanConverters.ExerciseTypeConverter());
    s_stringConvert.register(BusinessDayConvention.class, new JodaBeanConverters.BusinessDayConventionConverter());
    s_stringConvert.register(YieldConvention.class, new JodaBeanConverters.YieldConventionConverter());
    s_stringConvert.register(MonitoringType.class, new EnumConverter<MonitoringType>());
    s_stringConvert.register(BarrierType.class, new EnumConverter<BarrierType>());
    s_stringConvert.register(BarrierDirection.class, new EnumConverter<BarrierDirection>());
    s_stringConvert.register(SamplingFrequency.class, new EnumConverter<SamplingFrequency>());
    s_stringConvert.register(LongShort.class, new EnumConverter<LongShort>());
    s_stringConvert.register(OptionType.class, new EnumConverter<OptionType>());
    s_stringConvert.register(GICSCode.class, new GICSCodeConverter());
    s_stringConvert.register(ZonedDateTime.class, new ZonedDateTimeConverter());
    s_stringConvert.register(OffsetTime.class, new OffsetTimeConverter());
    s_stringConvert.register(Country.class, new CountryConverter());

    JSON_BUILDING_CONVERTERS = new Converters(s_regionConverters, s_stringConvert);
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
  // TODO define all property filters and converters in one place, they're shotgunned all over this package ATM
  private static final PropertyFilter s_fxRegionFilter =
      new PropertyFilter(FXForwardSecurity.meta().regionId(), NonDeliverableFXForwardSecurity.meta().regionId());

  public BlotterResource(SecurityMaster securityMaster, PortfolioMaster portfolioMaster, PositionMaster positionMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    _fungibleTradeBuilder = new FungibleTradeBuilder(_positionMaster,
                                                     portfolioMaster,
                                                     _securityMaster,
                                                     s_metaBeans,
                                                     s_stringConvert);
    _otcTradeBuilder = new OtcTradeBuilder(positionMaster,
                                           portfolioMaster,
                                           securityMaster,
                                           s_metaBeans, s_stringConvert);
  }

  /* package */ static StringConvert getStringConvert() {
    return s_stringConvert;
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
      BeanStructureBuilder structureBuilder = new BeanStructureBuilder(s_metaBeans,
                                                                       s_underlyingSecurityTypes,
                                                                       s_endpoints,
                                                                       s_stringConvert);
      BeanVisitorDecorator propertyNameFilter = new PropertyNameFilter("externalIdBundle", "securityType");
      PropertyFilter swaptionUnderlyingFilter = new PropertyFilter(SwaptionSecurity.meta().underlyingId());
      BeanTraverser traverser = new BeanTraverser(propertyNameFilter, swaptionUnderlyingFilter, s_fxRegionFilter);
      beanData = (Map<String, Object>) traverser.traverse(metaBean, structureBuilder);
    }
    return _freemarker.build("blotter/bean-structure.ftl", beanData);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("securities/{securityExternalId}")
  public String getSecurityJSON(@PathParam("securityExternalId") String securityExternalIdStr) {
    ExternalId securityExternalId = ExternalId.parse(securityExternalIdStr);
    SecuritySearchResult searchResult = _securityMaster.search(new SecuritySearchRequest(securityExternalId));
    if (searchResult.getSecurities().size() == 0) {
      throw new DataNotFoundException("No security found with ID " + securityExternalId);
    }
    ManageableSecurity security = searchResult.getFirstSecurity();
    BeanVisitor<JSONObject> securityVisitor = new BuildingBeanVisitor<>(security, new JsonDataSink(
        JSON_BUILDING_CONVERTERS));
    PropertyFilter securityPropertyFilter = new PropertyFilter(ManageableSecurity.meta().securityType());
    BeanTraverser securityTraverser = new BeanTraverser(securityPropertyFilter);
    MetaBean securityMetaBean = JodaBeanUtils.metaBean(security.getClass());
    JSONObject securityJson = (JSONObject) securityTraverser.traverse(securityMetaBean, securityVisitor);
    return securityJson.toString();
  }

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
    ManageableSecurity security = findSecurity(trade.getSecurityLink());
    JSONObject root = new JSONObject();
    try {
      JsonDataSink tradeSink = new JsonDataSink(JSON_BUILDING_CONVERTERS);
      if (isOtc(security)) {
        _otcTradeBuilder.extractTradeData(trade, tradeSink);
        MetaBean securityMetaBean = s_metaBeansByTypeName.get(security.getClass().getSimpleName());
        if (securityMetaBean == null) {
          throw new DataNotFoundException("No MetaBean is registered for security type " + security.getClass().getName());
        }
        BeanVisitor<JSONObject> securityVisitor = new BuildingBeanVisitor<>(security, new JsonDataSink(JSON_BUILDING_CONVERTERS));
        PropertyFilter securityTypeFilter = new PropertyFilter(ManageableSecurity.meta().securityType());
        BeanTraverser securityTraverser = new BeanTraverser(securityTypeFilter, s_fxRegionFilter);
        JSONObject securityJson = (JSONObject) securityTraverser.traverse(securityMetaBean, securityVisitor);
        if (security instanceof FinancialSecurity) {
          UnderlyingSecurityVisitor visitor = new UnderlyingSecurityVisitor(VersionCorrection.LATEST, _securityMaster);
          ManageableSecurity underlying = ((FinancialSecurity) security).accept(visitor);
          if (underlying != null) {
            BeanVisitor<JSONObject> underlyingVisitor =
                new BuildingBeanVisitor<>(underlying, new JsonDataSink(JSON_BUILDING_CONVERTERS));
            MetaBean underlyingMetaBean = s_metaBeansByTypeName.get(underlying.getClass().getSimpleName());
            JSONObject underlyingJson = (JSONObject) securityTraverser.traverse(underlyingMetaBean, underlyingVisitor);
            root.put("underlying", underlyingJson);
          }
        }
        root.put("security", securityJson);
      } else {
        FungibleTradeBuilder.extractTradeData(trade, tradeSink, s_stringConvert);
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
  @Path("trades/{tradeIdStr}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  // TODO the config endpoint uses form params for the JSON. why? better to use a MessageBodyWriter?
  public void updateTrade(@FormParam("trade") String jsonStr, @PathParam("tradeIdStr") String tradeIdStr) {
    try {
      // TODO what should happen to this? the ID is also in the JSON. check they match?
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
    return new BlotterLookupResource(s_stringConvert);
  }

  /**
   * For converting between enum instances and strings. The enum value names are made more readable by downcasing
   * and capitalizing them and replacing underscores with spaces.
   * @param <T> Type of the enum
   */
  private static class EnumConverter<T extends Enum> implements StringConverter<T> {

    @Override
    public T convertFromString(Class<? extends T> type, String str) {
      // IntelliJ says this cast is redundant but javac disagrees
      //noinspection RedundantCast
      return (T) Enum.valueOf(type, str.toUpperCase().replace(' ', '_'));
    }

    @Override
    public String convertToString(T e) {
      return WordUtils.capitalize(e.name().toLowerCase().replace('_', ' '));
    }
  }

  /**
   * For converting between strings and {@link GICSCode}.
   */
  private static class GICSCodeConverter implements StringConverter<GICSCode> {

    @Override
    public GICSCode convertFromString(Class<? extends GICSCode> cls, String code) {
      return GICSCode.of(code);
    }

    @Override
    public String convertToString(GICSCode code) {
      return code.getCode();
    }
  }

  /**
   * Converts {@link ZonedDateTime} to a local date string (e.g. 2012-12-21) and creates a {@link ZonedDateTime} from
   * a local date string with a time of 11:00 and a zone of UTC.
   */
  private static class ZonedDateTimeConverter implements StringConverter<ZonedDateTime> {

    @Override
    public ZonedDateTime convertFromString(Class<? extends ZonedDateTime> cls, String localDateString) {
      LocalDate localDate = LocalDate.parse(localDateString);
      return localDate.atTime(11, 0).atZone(ZoneOffset.UTC);
    }

    @Override
    public String convertToString(ZonedDateTime dateTime) {
      return dateTime.getDate().toString();
    }
  }

  /**
   * Converts an {@link OffsetTime} to a time string (e.g. 11:35) and discards the offset. Creates
   * an {@link OffsetTime} instance by parsing a local date string and using UTC as the offset.
   */
  private static class OffsetTimeConverter implements StringConverter<OffsetTime> {

    @Override
    public OffsetTime convertFromString(Class<? extends OffsetTime> cls, String timeString) {
      return OffsetTime.of(LocalTime.parse(timeString), ZoneOffset.UTC);
    }

    @Override
    public String convertToString(OffsetTime time) {
      return time.getTime().toString();
    }
  }

  /**
   * Converts between an {@link Expiry} and a local date string (e.g. 2011-03-08).
   */
  private static class ExpiryConverter implements StringConverter<Expiry> {

    @Override
    public Expiry convertFromString(Class<? extends Expiry> cls, String localDateString) {
      LocalDate localDate = LocalDate.parse(localDateString);
      return new Expiry(localDate.atTime(11, 0).atZone(ZoneOffset.UTC));
    }

    @Override
    public String convertToString(Expiry expiry) {
      return expiry.getExpiry().getDate().toString();
    }
  }

  /**
   * Converts between an {@link Expiry} and a local date string (e.g. 2011-03-08).
   */
  private static class CountryConverter implements StringConverter<Country> {

    @Override
    public Country convertFromString(Class<? extends Country> cls, String countryCode) {
      return Country.of(countryCode);
    }

    @Override
    public String convertToString(Country country) {
      return country.getCode();
    }
  }
}
