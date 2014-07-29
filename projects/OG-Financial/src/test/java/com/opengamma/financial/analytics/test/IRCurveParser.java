package com.opengamma.financial.analytics.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import au.com.bytecode.opencsv.CSVParser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LogNaturalCubicMonotonicityPreservingInterpolator1D;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.FloatingIndex;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.csv.CSVDocumentReader;
import com.opengamma.util.money.Currency;

public class IRCurveParser {
  
private static final Logger s_logger = LoggerFactory.getLogger(IRSwapTradeParser.class);
  
 
  private static final String CURVE_NAME="Curve Name"; 
  private static final String THREE_MONTHS = "3M";
  private static final String SIX_MONTHS = "6M";
  private static final String NINE_MONTHS = "9M";
  private static final String ONE_YEAR = "1Y";
  private static final String FIFTEEN_MONTHS = "15M";
  private static final String HEIGHTEEN_MONTHS = "18M";
  private static final String TWENTY_ONE_MONTHS = "21M";
  private static final String TWO_YEARS = "2Y";
  private static final String THREE_YEARS = "3Y";
  private static final String FOUR_YEARS = "4Y";
  private static final String FIVE_YEARS= "5Y";
  private static final String SIX_YEARS= "6Y";
  private static final String SEVEN_YEARS= "7Y";
  private static final String HEIGHT_YEARS= "8Y";
  private static final String NINE_YEARS= "9Y";
  private static final String TEN_YEARS= "10Y";
  private static final String TWELVE_YEARS= "12Y";
  private static final String FIFTEEN_YEARS= "15Y";
  private static final String TWENTY_YEARS= "20Y";
  private static final String TWENTY_FIVE_YEARS= "25Y";
  private static final String THIRTY_YEARS= "30Y";
  private static final String FOuRTY_YEARS= "40Y";
  private static final String FIFTY_YEARS= "50Y";
  private static final String[] DATES =new String[]{ THREE_MONTHS, SIX_MONTHS,NINE_MONTHS, ONE_YEAR, FIFTEEN_MONTHS, HEIGHTEEN_MONTHS, TWENTY_ONE_MONTHS,
    TWO_YEARS, THREE_YEARS, FOUR_YEARS, FIVE_YEARS, SIX_YEARS, SEVEN_YEARS, HEIGHT_YEARS, NINE_YEARS, TEN_YEARS, TWELVE_YEARS, FIFTEEN_YEARS, TWENTY_YEARS, TWENTY_FIVE_YEARS, THIRTY_YEARS, FOuRTY_YEARS,FIFTY_YEARS};
  public static final LogNaturalCubicMonotonicityPreservingInterpolator1D LOG_NATURAL_CUBIC_MONOTONE_INSTANCE = new LogNaturalCubicMonotonicityPreservingInterpolator1D();
  private static final double[] TIMES={0.249144422, 0.501026694,0.750171116, 0.999315537, 1.25119781, 1.500342231, 1.749486653, 2.001368925, 3.000684463, 4, 4.999315537, 6.001368925, 7.000684463, 8, 8.999315537, 10.00136893
, 12, 15.00068446,20, 24.99931554, 30.00136893, 40, 50.00136893};

  
  public List<InterpolatedDoublesCurve> parseCSVFile(URL fileUrl) {
    ArgumentChecker.notNull(fileUrl, "fileUrl");
    
    final List<InterpolatedDoublesCurve> curves = Lists.newArrayList();
    CSVDocumentReader csvDocumentReader = new CSVDocumentReader(fileUrl, CSVParser.DEFAULT_SEPARATOR, 
        CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, new FudgeContext());
    
    List<FudgeMsg> rowsWithError = Lists.newArrayList();
    for (FudgeMsg row : csvDocumentReader) {
      try {
        curves.add( createCurve(row));
      } catch (Exception ex) {
        ex.printStackTrace();
        rowsWithError.add(row);
      }
    }
    
    s_logger.warn("Total unprocessed rows: {}", rowsWithError.size());
    for (FudgeMsg fudgeMsg : rowsWithError) {
      s_logger.warn("{}", fudgeMsg);
    }
    return curves;
  }

  
  private InterpolatedDoublesCurve createCurve(FudgeMsg row) {
   double[] dicountFactors=new double [DATES.length];
  for (int loop=0; loop<DATES.length; loop++) {
    dicountFactors[loop]=row.getDouble(DATES[loop]);
  }
  final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(TIMES,dicountFactors, 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE, Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.LINEAR), true, row.getString(CURVE_NAME));
    return curve;
  }

}
