package com.opengamma.financial.analytics.test;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.DayPeriodPreCalculatedDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
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
  
  @Test
  public void testInterpolation() {
    double[] x = { 0.249144422, 0.501026694,0.750171116, 0.999315537, 1.25119781, 1.500342231, 1.749486653};
    double[] y = { 0.999297948, 0.998546826, 0.997720761, 0.996770227, 0.995642429, 0.994330655, 0.992795137 };

    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        "LogNaturalCubicWithMonotonicity",
        "FlatExtrapolator",
        "LinearExtrapolator");
   
    DoublesCurve doublesCurve = InterpolatedDoublesCurve.from(x, y, interpolator);

    double value0=doublesCurve.getYValue(0.0);
    double value=doublesCurve.getYValue(.31);
    
    double[] r = new double[y.length];
    for (int i = 0; i < r.length; i++) {
            r[i]=-Math.log(y[i])/x[i];      
    }
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(x,y, 
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE, Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.LINEAR), true, "curve");
    double value2=curve.getYValue(.31);
    
    final InterpolatedDoublesCurve curve2 = new InterpolatedDoublesCurve(x,r, 
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.CLAMPED_CUBIC_MONOTONE, Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.LINEAR), true, "curve");
    
    double value3=Math.exp(-curve2.getYValue(.31)*.31);
    
    final InterpolatedDoublesCurve curveForYieldCurve = new InterpolatedDoublesCurve(x,y, 
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE, Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.LINEAR), true, "curve");
    final YieldAndDiscountCurve yieldcurve= DiscountCurve.from(curveForYieldCurve);
    double value4=yieldcurve.getDiscountFactor(.31);
    
    final DayPeriodPreCalculatedDiscountCurve discountCurve= new DayPeriodPreCalculatedDiscountCurve("",curveForYieldCurve);
    double value5=discountCurve.getDiscountFactor(.31);
  }
  
}
