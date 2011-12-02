/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import static com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper.getWithException;
import static com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper.normaliseHeaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Example code to load a very simple swap portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.  There are no checks for the securities or portfolios already existing, so if you run it 
 * more than once you will get multiple copies portfolios and securities with the same names.  It is designed to run against the HSQLDB example
 * database.  It should be possible to run this class with no extra parameters.
 */
public class DemoSwapPortfolioLoader {
  /**
   * Logger.
   */
  private static Logger s_logger = LoggerFactory.getLogger(DemoSwapPortfolioLoader.class);
  
  /**
   * The name of the portfolio.
   */
  public static final String PORTFOLIO_NAME = "Demo Swap Portfolio";
  
  /**
   * The scheme used for an identifier which is added to each swap created from the CSV file
   */
  private static final String ID_SCHEME = "SWAP_LOADER";

  // Fields expected in CSV file (case insensitive).
  // Using headers since there are so many columns that indices get messy.
  private static final String TRADE_DATE = "trade date";
  private static final String EFFECTIVE_DATE = "effective date";
  private static final String TERMINATION_DATE = "termination date";
  private static final String PAY_FIXED = "pay fixed";

  private static final String FIXED_LEG_CURRENCY = "fixed currency";
  private static final String FIXED_LEG_NOTIONAL = "fixed notional (mm)";
  private static final String FIXED_LEG_DAYCOUNT = "fixed daycount";
  private static final String FIXED_LEG_BUS_DAY_CONVENTION = "fixed business day convention";
  private static final String FIXED_LEG_FREQUENCY = "fixed frequency";
  private static final String FIXED_LEG_REGION = "fixed region";
  private static final String FIXED_LEG_RATE = "fixed rate";

  private static final String FLOATING_LEG_CURRENCY = "floating currency";
  private static final String FLOATING_LEG_NOTIONAL = "floating notional (mm)";
  private static final String FLOATING_LEG_DAYCOUNT = "floating daycount";
  private static final String FLOATING_LEG_BUS_DAY_CONVENTION = "floating business day convention";
  private static final String FLOATING_LEG_FREQUENCY = "floating frequency";
  private static final String FLOATING_LEG_REGION = "floating region";
  private static final String FLOATING_LEG_RATE = "initial floating rate";
  private static final String FLOATING_LEG_REFERENCE = "floating reference";
  
  /** Standard date-time formatter for the input */
  public static final DateTimeFormatter CSV_DATE_FORMATTER;
  /** Standard date-time formatter for the output */
  public static final DateTimeFormatter OUTPUT_DATE_FORMATTER;
  /** Standard rate formatter */
  public static final DecimalFormat RATE_FORMATTER = new DecimalFormat("0.###%");
  /** Standard notional formatter */
  public static final DecimalFormat NOTIONAL_FORMATTER = new DecimalFormat("0,000");
  
  static {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern("dd/MM/yyyy");
    CSV_DATE_FORMATTER = builder.toFormatter();
    builder = new DateTimeFormatterBuilder();
    builder.appendPattern("yyyy-MM-dd");
    OUTPUT_DATE_FORMATTER = builder.toFormatter();
  }

  /**
   * The context.
   */
  private LoaderContext _loaderContext;

  //-------------------------------------------------------------------------
  /**
   * Sets the loader context.
   * <p>
   * This initializes this bean, typically from Spring.
   * 
   * @param loaderContext  the context, not null
   */
  public void setLoaderContext(final LoaderContext loaderContext) {
    _loaderContext = loaderContext;
  }
  
  /**
   * Gets the loader context.
   * <p>
   * This lets us access the masters that should have been initialized via Spring.
   * @return the loader context
   */
  public LoaderContext getLoaderContext() {
    return _loaderContext;
  }

  /**
   * Loads the test portfolio into the position master.
   * @return
   */
  public void createExamplePortfolio() {
    InputStream inputStream = DemoSwapPortfolioLoader.class.getResourceAsStream("demo-swap-portfolio.csv");  
    if (inputStream != null) {
      Collection<SwapSecurity> swaps = parseSwaps(inputStream);
      if (swaps.size() == 0) {
        throw new OpenGammaRuntimeException("No (valid) swaps were found in the specified file.");
      }
      persistToPortfolio(swaps, PORTFOLIO_NAME);
    }
  }

  private void persistToPortfolio(Collection<SwapSecurity> swaps, String portfolioName) {
    PortfolioMaster portfolioMaster = _loaderContext.getPortfolioMaster();
    PositionMaster positionMaster = _loaderContext.getPositionMaster();
    SecurityMaster securityMaster = _loaderContext.getSecurityMaster();
    
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    
    for (SwapSecurity swap : swaps) {
      SecurityDocument swapToAddDoc = new SecurityDocument();
      swapToAddDoc.setSecurity(swap);
      securityMaster.add(swapToAddDoc);
      ManageablePosition swapPosition = new ManageablePosition(BigDecimal.ONE, swap.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(swapPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

  private Collection<SwapSecurity> parseSwaps(InputStream inputStream) {
    Collection<SwapSecurity> swaps = new ArrayList<SwapSecurity>();
    try {
      CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
      
      String[] headers = csvReader.readNext();
      normaliseHeaders(headers);
      
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        Map<String, String> swapDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            // Run out of headers for this line
            break;
          }
          swapDetails.put(headers[i], line[i]);
        }
        try {
          SwapSecurity swap = parseSwap(swapDetails);
          swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          swaps.add(swap);
        } catch (Exception e) {
          s_logger.warn("Skipped row " + rowIndex + " because of an error", e);
        }
        rowIndex++;
      }
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("File '" + inputStream + "' could not be found");
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("An error occurred while reading file '" + inputStream + "'");
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(swaps.size()).append(" swaps:\n");
    for (SwapSecurity swap : swaps) {
      sb.append("\t").append(swap.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    
    return swaps;
  }

  private SwapSecurity parseSwap(Map<String, String> swapDetails) {
    // REVIEW jonathan 2010-08-03 --
    // Admittedly this looks a bit messy, but it's going to be less error-prone than relying on column indices.
    
    DayCount fixedDayCount = DayCountFactory.INSTANCE.getDayCount(getWithException(swapDetails, FIXED_LEG_DAYCOUNT));
    Frequency fixedFrequency = SimpleFrequencyFactory.INSTANCE.getFrequency(getWithException(swapDetails, FIXED_LEG_FREQUENCY));
    ExternalId fixedRegionIdentifier = RegionUtils.countryRegionId(Country.of(getWithException(swapDetails, FIXED_LEG_REGION)));
    BusinessDayConvention fixedBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(getWithException(swapDetails, FIXED_LEG_BUS_DAY_CONVENTION));
    Currency fixedCurrency = Currency.of(getWithException(swapDetails, FIXED_LEG_CURRENCY));
    double fixedNotionalAmount = Double.parseDouble(getWithException(swapDetails, FIXED_LEG_NOTIONAL));
    Notional fixedNotional = new InterestRateNotional(fixedCurrency, fixedNotionalAmount);
    double fixedRate = Double.parseDouble(getWithException(swapDetails, FIXED_LEG_RATE));
    FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(fixedDayCount, fixedFrequency, fixedRegionIdentifier, fixedBusinessDayConvention, fixedNotional, false, fixedRate);
    
    DayCount floatingDayCount = DayCountFactory.INSTANCE.getDayCount(getWithException(swapDetails, FLOATING_LEG_DAYCOUNT));
    Frequency floatingFrequency = SimpleFrequencyFactory.INSTANCE.getFrequency(getWithException(swapDetails, FLOATING_LEG_FREQUENCY));
    ExternalId floatingRegionIdentifier = RegionUtils.countryRegionId(Country.of(getWithException(swapDetails, FLOATING_LEG_REGION)));
    BusinessDayConvention floatingBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(getWithException(swapDetails, FLOATING_LEG_BUS_DAY_CONVENTION));
    Currency floatingCurrency = Currency.of(getWithException(swapDetails, FLOATING_LEG_CURRENCY));
    double floatingNotionalAmount = Double.parseDouble(getWithException(swapDetails, FLOATING_LEG_NOTIONAL));
    Notional floatingNotional = new InterestRateNotional(floatingCurrency, floatingNotionalAmount);
    // TODO: not sure that this actually does anything, or what identifier we're looking for - just invented something for now
    String floatingReferenceRate = getWithException(swapDetails, FLOATING_LEG_REFERENCE);
    ExternalId floatingReferenceRateIdentifier = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, floatingReferenceRate);
    double floatingInitialRate = Double.parseDouble(getWithException(swapDetails, FLOATING_LEG_RATE));
    FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(floatingDayCount, floatingFrequency,
        floatingRegionIdentifier, floatingBusinessDayConvention, floatingNotional, false, floatingReferenceRateIdentifier, FloatingRateType.IBOR);
    floatingLeg.setInitialFloatingRate(floatingInitialRate);
    
    LocalDateTime tradeDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, TRADE_DATE), CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    LocalDateTime effectiveDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, EFFECTIVE_DATE), CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    LocalDateTime terminationDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, TERMINATION_DATE), CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    
    String fixedLegDescription = RATE_FORMATTER.format(fixedRate);
    String floatingLegDescription = floatingReferenceRate;
    
    boolean isPayFixed = Boolean.parseBoolean(getWithException(swapDetails, PAY_FIXED));
    SwapLeg payLeg;
    String payLegDescription;
    SwapLeg receiveLeg;
    String receiveLegDescription;
    if (isPayFixed) {
      payLeg = fixedLeg;
      payLegDescription = fixedLegDescription;
      receiveLeg = floatingLeg;
      receiveLegDescription = floatingLegDescription;
    } else {
      payLeg = floatingLeg;
      payLegDescription = floatingLegDescription;
      receiveLeg = fixedLeg;
      receiveLegDescription = fixedLegDescription;
    }
    
    SwapSecurity swap = new SwapSecurity(tradeDate.atZone(TimeZone.UTC), effectiveDate.atZone(TimeZone.UTC),
        terminationDate.atZone(TimeZone.UTC), "Cpty Name", payLeg, receiveLeg);
    
    // Assume notional / currencies are the same for both legs - the name is really just to give us something to display anyway
    swap.setName("IR Swap " + NOTIONAL_FORMATTER.format(fixedNotionalAmount) + " " + fixedCurrency + " " +
        terminationDate.toString(OUTPUT_DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    
    return swap;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Sets up and loads the database.
   * <p>
   * This loader requires a Spring configuration file that defines the security,
   * position and portfolio masters, together with an instance of this bean
   * under the name "demoSwapPortfolioLoader".
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    try {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset(); 
      configurator.doConfigure("src/com/opengamma/examples/server/logback.xml");
      
      // Set the run mode to EXAMPLE so we use the HSQLDB example database.
      PlatformConfigUtils.configureSystemProperties(RunMode.EXAMPLE);
      System.out.println("Starting connections");
      AbstractApplicationContext appContext = new ClassPathXmlApplicationContext("demoPortfolioLoader.xml");
      appContext.start();
      
      try {
        DemoSwapPortfolioLoader loader = appContext.getBean("demoSwapPortfolioLoader", DemoSwapPortfolioLoader.class);
        System.out.println("Loading data");
        loader.createExamplePortfolio();
      } finally {
        appContext.close();
      }
      System.out.println("Finished");
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }


}
