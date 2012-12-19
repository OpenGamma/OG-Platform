/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.JodaBeanUtils;
import org.joda.convert.StringConverter;
import org.json.JSONArray;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.LongShort;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.swap.FloatingRateType;

/**
 *
 */
public class BlotterLookupResource {

  static {
    // ensure the converters are loaded and registered
    JodaBeanConverters.getInstance();
  }

  @SuppressWarnings("unchecked")
  private String convertToJsonArray(Class<?> type, Iterator<?> it) {
    StringConverter<Object> converter = (StringConverter<Object>) JodaBeanUtils.stringConverter().findConverter(type);
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
  public String getFrequencies() {
    return convertToJsonArray(Frequency.class, SimpleFrequencyFactory.INSTANCE.enumerateAvailableFrequencies());
  }

  @GET
  @Path("exercisetypes")
  @Produces(MediaType.APPLICATION_JSON)
  public String getExerciseTypes() {
    ImmutableList<ExerciseType> exerciseTypes = ImmutableList.of(new AmericanExerciseType(),
                                                                 new AsianExerciseType(),
                                                                 new BermudanExerciseType(),
                                                                 new EuropeanExerciseType());
    return convertToJsonArray(ExerciseType.class, exerciseTypes.iterator());
  }

  @GET
  @Path("daycountconventions")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDayCountConventions() {
    return convertToJsonArray(DayCount.class, DayCountFactory.INSTANCE.enumerateAvailableDayCounts());
  }

  @GET
  @Path("businessdayconventions")
  @Produces(MediaType.APPLICATION_JSON)
  public String getBusinessDayConventions() {
    return convertToJsonArray(BusinessDayConvention.class,
                              BusinessDayConventionFactory.INSTANCE.enumerateAvailableBusinessDayConventions());
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
}
