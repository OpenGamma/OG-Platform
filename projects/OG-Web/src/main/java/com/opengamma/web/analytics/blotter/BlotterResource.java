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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.web.FreemarkerOutputter;

/**
 *
 */
@Path("blotter")
public class BlotterResource {

  private static final Set<MetaBean> s_metaBeans = Sets.<MetaBean>newHashSet(
      FXForwardSecurity.meta(),
      SwapSecurity.meta(),
      SwaptionSecurity.meta(),
      FixedInterestRateLeg.meta(),
      FloatingInterestRateLeg.meta(),
      FloatingSpreadIRLeg.meta(),
      FloatingGearingIRLeg.meta(),
      InterestRateNotional.meta(),
      AmericanExerciseType.meta(),
      AsianExerciseType.meta(),
      EuropeanExerciseType.meta(),
      BermudanExerciseType.meta(),
      CapFloorCMSSpreadSecurity.meta(),
      NonDeliverableFXOptionSecurity.meta(),
      ForwardSwapSecurity.meta(),
      FXOptionSecurity.meta(),
      FRASecurity.meta(),
      CapFloorSecurity.meta(),
      EquityBarrierOptionSecurity.meta(),
      EquityVarianceSwapSecurity.meta(),
      FXBarrierOptionSecurity.meta(),
      NonDeliverableFXOptionSecurity.meta()
  );
  private final SecurityBuilder _securityBuilder = new SecurityBuilder(s_metaBeans);

  private static final Map<Class<?>, Class<?>> s_underlyingSecurityTypes = ImmutableMap.<Class<?>, Class<?>>of(
      IRFutureOptionSecurity.class, InterestRateFutureSecurity.class,
      SwaptionSecurity.class, SwapSecurity.class);

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

  private FreemarkerOutputter _freemarker;

  public BlotterResource(SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
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

  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("types/{typeName}")
  public String getStructure(@PathParam("typeName") String typeName) {
    MetaBean metaBean = s_metaBeansByTypeName.get(typeName);
    if (metaBean == null) {
      throw new DataNotFoundException("Unknown type name " + typeName);
    }
    BeanStructureBuilder structureBuilder = new BeanStructureBuilder(s_metaBeans, s_underlyingSecurityTypes);
    // filter out underlying ID property for security types whose underlying is another OTC security
    PropertyFilter<Map<String, Object>> filter = new PropertyFilter<Map<String, Object>>(SwaptionSecurity.meta().underlyingId());
    Map<String,Object> beanData = new BeanTraverser(filter).traverse(metaBean, structureBuilder);
    return _freemarker.build("blotter/bean-structure.ftl", beanData);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("securities/{securityId}")
  public String getJSON(@PathParam("securityId") String securityIdStr) {
    UniqueId securityId = UniqueId.parse(securityIdStr);
    SecurityDocument document = _securityMaster.get(securityId);
    ManageableSecurity security = document.getSecurity();
    MetaBean metaBean = s_metaBeansByTypeName.get(security.getClass().getSimpleName());
    if (metaBean == null) {
      throw new DataNotFoundException("No MetaBean is registered for security type " + security.getClass().getName());
    }
    BeanVisitor<JSONObject> writingVisitor = new BuildingBeanVisitor<JSONObject>(security, new JsonDataSink());
    // TODO filter out underlyingId for securities with OTC underlying
    JSONObject json = new BeanTraverser().traverse(metaBean, writingVisitor);
    JSONObject root = new JSONObject();
    try {
      root.put("security", json);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("", e);
    }
    // TODO underlying for securities with OTC underlying securities
    return root.toString();
  }

  // TODO this is only here for my benefit during development - delete
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("securities/{securityId}")
  public String getText(@PathParam("securityId") String securityIdStr) {
    return getJSON(securityIdStr);
  }

  @POST
  @Path("securities")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  // TODO the config endpoint uses form params for the JSON. why? better to use a MessageBodyWriter?
  public String postJSON(@FormParam("security") String securityJsonStr) {
    JSONObject securityJson;
    try {
      JSONObject json = new JSONObject(securityJsonStr);
      securityJson = json.getJSONObject("security");
      // TODO this needs to happen for swaptions and possibly some others
      //underlyingJson = json.getJSONObject("underlying");
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to parse security JSON", e);
    }
    // TODO this doesn't cover swaptions (where the underlying is an OTC security)
    ManageableSecurity security = _securityBuilder.buildSecurity(new JsonBeanDataSource(securityJson));
    UniqueId securityId;
    if (security.getUniqueId() == null) {
      SecurityDocument addedDocument = _securityMaster.add(new SecurityDocument(security));
      securityId = addedDocument.getUniqueId();
    } else {
      SecurityDocument updatedDocument = _securityMaster.update(new SecurityDocument(security));
      securityId = updatedDocument.getUniqueId();
    }
    return new JSONObject(ImmutableMap.of("securityId", securityId)).toString();
  }

  private static Map<Object, Object> map(Object... values) {
    final Map<Object, Object> result = Maps.newHashMap();
    for (int i = 0; i < values.length / 2; i++) {
      result.put(values[i * 2], values[(i * 2) + 1]);
    }
    result.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return result;
  }

  // TODO create OTC trade - create new security, position of one, contains one trade

  // TODO create fungible trade - identifier and quantity

  // TODO get OTC trade (security)

  // TODO what about bond futures? is the BondFutureDeliverable part of the definition from bbg?
}
