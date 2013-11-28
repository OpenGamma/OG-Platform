/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.LongShort;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;

/**
 *
 */
public class BlotterLookupResource {

  static {
    // ensure the converters are loaded and registered
    JodaBeanConverters.getInstance();
  }

  private final StringConvert _stringConvert;

  /* package */ BlotterLookupResource(StringConvert stringConvert) {
    ArgumentChecker.notNull(stringConvert, "stringConvert");
    _stringConvert = stringConvert;
  }

  @SuppressWarnings("unchecked")
  private String convertToJsonArray(Class<?> type, Iterator<?> it) {
    StringConverter<Object> converter = (StringConverter<Object>) _stringConvert.findConverter(type);
    List<String> results = Lists.newArrayList();
    while (it.hasNext()) {
      Object item = it.next();
      results.add(converter.convertToString(item));
    }
    return new JSONArray(results).toString();
  }

  @GET
  @Path("frequencies")
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressWarnings("deprecation")
  public String getFrequencies() {
    // deprecated method has an ordering applied
    return convertToJsonArray(Frequency.class, SimpleFrequencyFactory.INSTANCE.enumerateAvailableFrequencies());
  }

  @GET
  @Path("exercisetypes")
  @Produces(MediaType.APPLICATION_JSON)
  public String getExerciseTypes() {
    ImmutableList<ExerciseType> exerciseTypes = ImmutableList.<ExerciseType>of(/*new AmericanExerciseType(),
                                                                 new AsianExerciseType(),
                                                                 new BermudanExerciseType(),*/
                                                                 new EuropeanExerciseType());
    return convertToJsonArray(ExerciseType.class, exerciseTypes.iterator());
  }

  @GET
  @Path("daycountconventions")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDayCountConventions() {
    return convertToJsonArray(DayCount.class, DayCountFactory.INSTANCE.instanceMap().values().iterator());
  }

  @GET
  @Path("businessdayconventions")
  @Produces(MediaType.APPLICATION_JSON)
  public String getBusinessDayConventions() {
    return convertToJsonArray(BusinessDayConvention.class,
                              BusinessDayConventionFactory.INSTANCE.instanceMap().values().iterator());
  }

  @GET
  @Path("barriertypes")
  @Produces(MediaType.APPLICATION_JSON)
  public String getBarrierTypes() {
    return convertToJsonArray(BarrierType.class, Arrays.asList(BarrierType.values()).iterator());
  }

  @GET
  @Path("barrierdirections")
  @Produces(MediaType.APPLICATION_JSON)
  public String getBarrierDirections() {
    return convertToJsonArray(BarrierDirection.class, Arrays.asList(BarrierDirection.values()).iterator());
  }

  @GET
  @Path("debtseniority")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDebtSeniority() {
    return convertToJsonArray(DebtSeniority.class, Arrays.asList(DebtSeniority.values()).iterator());
  }

  @GET
  @Path("restructuringclause")
  @Produces(MediaType.APPLICATION_JSON)
  public String getRestructuringClause() {
    return convertToJsonArray(RestructuringClause.class, Arrays.asList(RestructuringClause.values()).iterator());
  }

  @GET
  @Path("stubtype")
  @Produces(MediaType.APPLICATION_JSON)
  public String getStubType() {
    return convertToJsonArray(StubType.class, Arrays.asList(StubType.values()).iterator());
  }

  @GET
  @Path("samplingfrequencies")
  @Produces(MediaType.APPLICATION_JSON)
  public String getSamplingFrequencies() {
    return convertToJsonArray(SamplingFrequency.class, Arrays.asList(SamplingFrequency.values()).iterator());
  }

  @GET
  @Path("floatingratetypes")
  @Produces(MediaType.APPLICATION_JSON)
  public String getFloatingRateTypes() {
    return convertToJsonArray(FloatingRateType.class, Arrays.asList(FloatingRateType.values()).iterator());
  }

  @GET
  @Path("longshort")
  @Produces(MediaType.APPLICATION_JSON)
  public String getLongShort() {
    return convertToJsonArray(LongShort.class, Arrays.asList(LongShort.values()).iterator());
  }

  @GET
  @Path("monitoringtype")
  @Produces(MediaType.APPLICATION_JSON)
  public String getMonitoringType() {
    return convertToJsonArray(MonitoringType.class, Arrays.asList(MonitoringType.values()).iterator());
  }
  
  @GET
  @Path("interpolationmethods")
  @Produces(MediaType.APPLICATION_JSON)
  public String getInterpolationMethods() {
    return convertToJsonArray(InterpolationMethod.class, Arrays.asList(InterpolationMethod.values()).iterator());
  }

  @GET
  @Path("idschemes")
  @Produces(MediaType.APPLICATION_JSON)
  public String getIdSchemes() {
    Map<String, ExternalScheme> schemes = Maps.newHashMap();
    schemes.put("ISIN", ExternalSchemes.ISIN);
    schemes.put("CUSIP", ExternalSchemes.CUSIP);
    schemes.put("SEDOL1", ExternalSchemes.SEDOL1);
    schemes.put("Bloomberg BUID", ExternalSchemes.BLOOMBERG_BUID);
    schemes.put("Bloomberg BUID (weak)", ExternalSchemes.BLOOMBERG_BUID_WEAK);
    schemes.put("Bloomberg Ticker", ExternalSchemes.BLOOMBERG_TICKER);
    schemes.put("Bloomberg Ticker (weak)", ExternalSchemes.BLOOMBERG_TICKER_WEAK);
    schemes.put("Bloomberg Ticker/Coupon/Maturity", ExternalSchemes.BLOOMBERG_TCM);
    schemes.put("Reuters RIC", ExternalSchemes.RIC);
    schemes.put("ActiveFeed Ticker", ExternalSchemes.ACTIVFEED_TICKER);
    schemes.put("Tullett Prebon SURF", ExternalSchemes.SURF);
    schemes.put("ICAP", ExternalSchemes.ICAP);
    schemes.put("GMI", ExternalSchemes.GMI);
    schemes.put("Markit RED Code", ExternalSchemes.MARKIT_RED_CODE);
    return new JSONObject(schemes).toString();
  }

  @GET
  @Path("regions")
  @Produces(MediaType.APPLICATION_JSON)
  public String getRegions() {
    List<Country> countryList = Lists.newArrayList(Country.getAvailableCountries());
    Collections.sort(countryList);
    return convertToJsonArray(Country.class, countryList.iterator());
  }
}
