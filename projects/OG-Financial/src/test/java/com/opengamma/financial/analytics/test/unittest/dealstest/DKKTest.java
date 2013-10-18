package com.opengamma.financial.analytics.test.unittest.dealstest;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.financial.analytics.test.IRCurveParser;
import com.opengamma.financial.analytics.test.IRSwapSecurity;
import com.opengamma.financial.analytics.test.IRSwapTradeParser;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for DKK deals
 */
@Test(groups = TestGroup.UNIT)
public class DKKTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DKKTest.class);
  private static final String CURRENCY = "DKK";

  private static final String ON_NAME = "DKK_CIBOR_6M_ERS";
  private static final String THREE_MONTH_NAME = "DKK_CIBOR_6M_ERS";
  private static final String SIX_MONTH_NAME = "DKK_CIBOR_6M_ERS";
  final static String discountingCurvename = "Discounting";
  final static String forward3MCurveName = "Forward 3M";
  final static String forward6MCurveName = "Forward 6M";

  final static Currency ccy = Currency.DKK;

  private static final String PAY_CURRENCY = "LEG1_CCY";

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  public void test() throws Exception {

    // Build the clean list of swap
    IRSwapTradeParser tradeParser = new IRSwapTradeParser();
    Resource resource = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Trades14Oct.csv");
    List<IRSwapSecurity> trades = tradeParser.parseCSVFile(resource.getURL());
    List<IRSwapSecurity> tradesClean = Lists.newArrayList();
    for (IRSwapSecurity irSwapSecurity : trades) {

      String currency = irSwapSecurity.getRawInput().getString(PAY_CURRENCY);
      if (currency.equals(CURRENCY)) {
        tradesClean.add(irSwapSecurity);
      }

    }

    // Build the curve bundle
    final HashMap<String, Currency> ccyMap = new HashMap<>();
    ccyMap.put(discountingCurvename, ccy);
    ccyMap.put(forward3MCurveName, ccy);
    ccyMap.put(forward6MCurveName, ccy);
    final FXMatrix fx = new FXMatrix(ccy);
    final YieldCurveBundle curvesClean = new YieldCurveBundle(fx, ccyMap);

    IRCurveParser curveParser = new IRCurveParser();
    Resource resourceCurve = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Base_Curves_20131014_Clean.csv");
    List<InterpolatedDoublesCurve> curves = curveParser.parseCSVFile(resourceCurve.getURL());

    for (InterpolatedDoublesCurve interpolatedDoublesCurve : curves) {

      String name = interpolatedDoublesCurve.getName();
      if (name.equals(ON_NAME)) {
        curvesClean.setCurve(discountingCurvename, DiscountCurve.from(interpolatedDoublesCurve));
      }
      if (name.equals(THREE_MONTH_NAME)) {
        curvesClean.setCurve(forward3MCurveName, DiscountCurve.from(interpolatedDoublesCurve));
      }
      if (name.equals(SIX_MONTH_NAME)) {
        curvesClean.setCurve(forward6MCurveName, DiscountCurve.from(interpolatedDoublesCurve));
      }
    }

    // Convert the swap security into a swap definition 
    //TODO
    s_logger.warn("Got {} trades", trades.size());
  }

}
