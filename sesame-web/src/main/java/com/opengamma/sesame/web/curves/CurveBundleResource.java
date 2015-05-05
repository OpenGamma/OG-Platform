/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.web.curves;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecificationParser;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.marketdata.scenarios.SingleScenarioDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * REST endpoint to obtain a curve bundle
 */
@Path("curvebundle/{bundle}/{spec}")
public class CurveBundleResource  {

  /** Builds market data in response to specified requirements */
  private final MarketDataEnvironmentFactory _environmentFactory;

  private static final Logger s_logger = LoggerFactory.getLogger(CurveBundleResource.class);

  /**
   *  @param environmentFactory builds market data in response to specified requirements
   */
  public CurveBundleResource(MarketDataEnvironmentFactory environmentFactory) {
    _environmentFactory = ArgumentChecker.notNull(environmentFactory, "environmentFactory");
  }

  /**
   * Rest endpoint to obtain curve bundle data
   * @param bundle the name of the bundle
   * @param spec the market data specification in the format {type}:{name/id}
   * @return a json representation of the bundle, split into discounting, forward ON and forward Ibor Curves
   */
  @GET @Produces("application/json")
  public String calculate(@PathParam("bundle") String bundle,
                          @PathParam("spec") String spec) {

    MarketDataSpecification marketDataSpec = MarketDataSpecificationParser.parse(spec);
    ZonedDateTime valuationTime = ZonedDateTime.now();
    MarketDataEnvironment suppliedData = MarketDataEnvironmentBuilder.empty();
    MulticurveId multicurveId = MulticurveId.of(bundle);
    SingleValueRequirement requirement = SingleValueRequirement.of(multicurveId);
    Set<MarketDataRequirement> requirements = ImmutableSet.<MarketDataRequirement>of(requirement);
    SingleScenarioDefinition perturbations = SingleScenarioDefinition.base();
    MarketDataEnvironment marketData = _environmentFactory.build(suppliedData,
                                                                 requirements,
                                                                 perturbations,
                                                                 marketDataSpec,
                                                                 valuationTime);
    MulticurveBundle curveBundle = (MulticurveBundle) marketData.getData().get(requirement);
    MulticurveProviderDiscount multicurveProvider = curveBundle.getMulticurveProvider();

    Map<Currency, YieldAndDiscountCurve> discountingCurves = multicurveProvider.getDiscountingCurves();
    Map<IndexON, YieldAndDiscountCurve> forwardONCurves = multicurveProvider.getForwardONCurves();
    Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = multicurveProvider.getForwardIborCurves();

    Map<String, Map<String, Object>> discounting = new HashMap<>();
    Map<String, Map<String, Object>> forwardON = new HashMap<>();
    Map<String, Map<String, Object>> forwardIbor = new HashMap<>();

    for (Map.Entry<Currency, YieldAndDiscountCurve> entry : discountingCurves.entrySet()) {
      discounting.put(entry.getKey().getCode(), getCurveData(entry.getValue()));
    }

    for (Map.Entry<IndexON, YieldAndDiscountCurve> entry : forwardONCurves.entrySet()) {
      forwardON.put(entry.getKey().toString(), getCurveData(entry.getValue()));
    }

    for (Map.Entry<IborIndex, YieldAndDiscountCurve> entry : forwardIborCurves.entrySet()) {
      forwardIbor.put(entry.getKey().toString(), getCurveData(entry.getValue()));
    }

    Map<String, Map> curves = new HashMap<>();
    curves.put("discounting", discounting);
    curves.put("forwardOn", forwardON);
    curves.put("forwardIbor", forwardIbor);

    Gson gson = new Gson();
    return gson.toJson(curves);
  }

  private Map<String, Object> getCurveData(YieldAndDiscountCurve curve) {
    Double[] tenors;
    Double[] rates;
    Map<String, Object> data = new HashMap<>();

    if (curve instanceof YieldCurve) {
      tenors = ((YieldCurve) curve).getCurve().getXData();
      rates = ((YieldCurve) curve).getCurve().getYData();
    } else if (curve instanceof DiscountCurve) {
      tenors = ((DiscountCurve) curve).getCurve().getXData();
      rates = ((DiscountCurve) curve).getCurve().getYData();
    } else {
      tenors = new Double[0];
      rates = new Double[0];
      s_logger.error(curve.getName() + " is in instance of " + curve.getClass() + " CurveBundleResource only " +
                         "supports YieldCurve and DiscountCurve instances of YieldAndDiscountCurve");
    }

    data.put("name", curve.getName());
    data.put("x", ArrayUtils.toPrimitive(tenors));
    data.put("y", ArrayUtils.toPrimitive(rates));

    return data;
  }

}
