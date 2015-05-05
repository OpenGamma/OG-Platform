package com.opengamma.sesame.web.curves;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.marketdata.scenarios.SingleScenarioDefinition;
import com.opengamma.util.money.Currency;

public class CurveBundleResourceTest {

  private CurveBundleResource _resource;

  @BeforeClass
  public void setup() {

    MarketDataEnvironmentFactory factory = mock(MarketDataEnvironmentFactory.class);
    _resource = new CurveBundleResource(factory);
    MulticurveId multicurveId = MulticurveId.of("bundle-name");
    SingleValueRequirement requirement = SingleValueRequirement.of(multicurveId);
    Set<MarketDataRequirement> requirements = ImmutableSet.<MarketDataRequirement>of(requirement);

    MarketDataEnvironment env = mock(MarketDataEnvironment.class);

    when(factory.build(any(MarketDataEnvironment.class),
                       eq(requirements),
                       any(SingleScenarioDefinition.class),
                       any(MarketDataSpecification.class),
                       any(ZonedDateTime.class))).thenReturn(env);

    FXMatrix matrix = new FXMatrix();
    Map<Currency, YieldAndDiscountCurve> discounting = new LinkedHashMap<>();
    NodalDoublesCurve nodalDoublesCurve = NodalDoublesCurve.from(new double[]{0.1, 0.2, 0.3}, new double[]{0.4, 0.5, 0.6});
    discounting.put(Currency.USD, new YieldCurve("A", nodalDoublesCurve));
    discounting.put(Currency.EUR, new DiscountCurve("B", nodalDoublesCurve));
    Map<IborIndex, YieldAndDiscountCurve> ibor = new LinkedHashMap<>();
    ibor.put(new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCounts.ACT_360,
                           BusinessDayConventions.FOLLOWING, false, "C-Index"),
             new YieldCurve("C", nodalDoublesCurve));
    ibor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), 1, DayCounts.ACT_360,
                           BusinessDayConventions.FOLLOWING, false, "D-Index"),
             new YieldCurve("D", nodalDoublesCurve));
    Map<IndexON, YieldAndDiscountCurve> overnight = new LinkedHashMap<>();
    overnight.put(new IndexON("NAME1", Currency.USD, DayCounts.ACT_360, 1), new YieldCurve("E", nodalDoublesCurve));
    overnight.put(new IndexON("NAME2", Currency.EUR, DayCounts.ACT_360, 0), new YieldCurve("F", nodalDoublesCurve));
    MulticurveProviderDiscount provider = new MulticurveProviderDiscount(discounting, ibor, overnight, matrix);

    MulticurveBundle bundle = new MulticurveBundle(provider, new CurveBuildingBlockBundle());
    ImmutableMap<SingleValueRequirement, MulticurveBundle> data = ImmutableMap.of(requirement, bundle);
    when(env.getData()).thenReturn(ImmutableMap.<SingleValueRequirement, Object>copyOf(data));

  }

  @Test
  public void test() {
    String calculate = _resource.calculate("bundle-name", "live:bloomberg");

    JsonParser parser = new JsonParser();
    JsonObject o = (JsonObject)parser.parse(calculate);
    JsonElement discounting = o.get("discounting");

    JsonElement usd = discounting.getAsJsonObject().get("USD");
    assertThat(usd.getAsJsonObject().get("name").getAsString(), is("A"));
    testCurveX(usd.getAsJsonObject().get("x").getAsJsonArray());
    testCurveY(usd.getAsJsonObject().get("y").getAsJsonArray());

    JsonElement eur = discounting.getAsJsonObject().get("EUR");
    assertThat(eur.getAsJsonObject().get("name").getAsString(), is("B"));
    testCurveX(eur.getAsJsonObject().get("x").getAsJsonArray());
    testCurveY(eur.getAsJsonObject().get("y").getAsJsonArray());

    JsonElement forwardIbor = o.get("forwardIbor");

    JsonElement c = forwardIbor.getAsJsonObject().get("C-Index");
    assertThat(c.getAsJsonObject().get("name").getAsString(), is("C"));
    testCurveX(c.getAsJsonObject().get("x").getAsJsonArray());
    testCurveY(c.getAsJsonObject().get("y").getAsJsonArray());

    JsonElement d = forwardIbor.getAsJsonObject().get("D-Index");
    assertThat(d.getAsJsonObject().get("name").getAsString(), is("D"));
    testCurveX(d.getAsJsonObject().get("x").getAsJsonArray());
    testCurveY(d.getAsJsonObject().get("y").getAsJsonArray());

    JsonElement forwardOn = o.get("forwardOn");

    JsonElement name1 = forwardOn.getAsJsonObject().get("NAME1-USD-Actual/360");
    assertThat(name1.getAsJsonObject().get("name").getAsString(), is("E"));
    testCurveX(name1.getAsJsonObject().get("x").getAsJsonArray());
    testCurveY(name1.getAsJsonObject().get("y").getAsJsonArray());

    JsonElement name2 = forwardOn.getAsJsonObject().get("NAME2-EUR-Actual/360");
    assertThat(name2.getAsJsonObject().get("name").getAsString(), is("F"));
    testCurveX(name2.getAsJsonObject().get("x").getAsJsonArray());
    testCurveY(name2.getAsJsonObject().get("y").getAsJsonArray());

  }

  private void testCurveX(JsonArray array) {
    assertThat(array.get(0).getAsDouble(), is(0.1));
    assertThat(array.get(1).getAsDouble(), is(0.2));
    assertThat(array.get(2).getAsDouble(), is(0.3));
  }

  private void testCurveY(JsonArray array) {
    assertThat(array.get(0).getAsDouble(), is(0.4));
    assertThat(array.get(1).getAsDouble(), is(0.5));
    assertThat(array.get(2).getAsDouble(), is(0.6));
  }

}