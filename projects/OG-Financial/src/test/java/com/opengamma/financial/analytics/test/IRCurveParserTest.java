package com.opengamma.financial.analytics.test;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class IRCurveParserTest {
 private static final Logger s_logger = LoggerFactory.getLogger(IRSwapTradeParserTest.class);
  
  public void test() throws Exception {
    IRCurveParser curveParser = new IRCurveParser();
    Resource resource = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Base_Curves_20131014_Clean.csv"); 
    List<InterpolatedDoublesCurve> curves = curveParser.parseCSVFile(resource.getURL());
    for (InterpolatedDoublesCurve interpolatedDoublesCurve : curves) {
      
      
    }
    s_logger.info("Got {} trades", curves.size());
  }
}
