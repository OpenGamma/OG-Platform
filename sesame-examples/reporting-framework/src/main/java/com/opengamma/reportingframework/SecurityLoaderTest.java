package com.opengamma.reportingframework;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.money.Currency;

public class SecurityLoaderTest {
	
	private static final String URL = "http://devsvr-lx-3:8080/jax";
	private static final Logger s_logger = LoggerFactory.getLogger(SecurityLoaderTest.class);

	/*public static void main(String[] args) {
		
		String fileName = "C:\\Users\\Ankit\\Desktop\\securitydata.csv";
		
		SecurityLoaderTest securityLoader = new SecurityLoaderTest();
		
		List<InterestRateSwapSecurity> securities = securityLoader.readSecurities(fileName);
		//securityLoader.saveSecurities(securities);
		System.out.println("Exiting...");
		System.exit(0);
		
	} */
	
	public List<ManageableSecurity> execute(String fileName) {
		
		List<ManageableSecurity> securities = readSecurities(fileName);
		//securityLoader.saveSecurities(securities);
		if(securities != null) {
			try {
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd \"C:\\Program Files\\MongoDB\\bin\" && mongoimport -d test -host 10.5.2.132 -c FullSecurityDataCsv --type csv --file \\\\WINDOWS8\\RadarTemp\\securitydata.csv --upsertFields External_ID --headerline");
				//"cmd.exe", "/c", "cd \"C:\\Program Files\\MongoDB\\bin\" && mongoimport -d test -host devsvr-lx-7 -c FullSecurityDataCsv --type csv --file \\\\WINDOWS8\\RadarTemp\\out\\securitydata.csv --headerline");
				Process p = builder.start();
				p.waitFor();
			} catch(Exception e) {
				e.printStackTrace();
			}
			moveFile(fileName);
		}
		return securities;
	}
	
	public void saveSecurities(List<InterestRateSwapSecurity> securities) {
		System.out.println("Connecting to Engine to save securities...");
		ToolContext context = ToolContextUtils.getToolContext(URL, ToolContext.class);
		System.out.println("Connected...");
		
		SecurityMaster securityMaster = context.getSecurityMaster();
		for(ManageableSecurity manageableSecurity : securities) {
			SecurityDocument secDoc = new SecurityDocument(manageableSecurity);
			secDoc = securityMaster.add(secDoc);
			System.out.println("Object id: " + secDoc.getObjectId());
		}
	}
	
	public List<ManageableSecurity> readSecurities(String fileName) {
		List<ManageableSecurity> securities = new ArrayList();
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				//s_logger.error("File: " + fileName + " not found, exiting...");
				//System.exit(1);
				return null;
			}
			CSVReader reader = new CSVReader(new FileReader(fileName));
			
			String[] headingLine = reader.readNext();
			//for(String header : headingLine) {
			//	System.out.println("Header: " + header);
			//}

			// iterate over all securities in the file
			while(true) {
				String[] line = reader.readNext();
				if(line == null) {
					System.out.println("Reached end of file...");
					break;
				}
				List<InterestRateSwapLeg> legs = new ArrayList<>();
			    FixedInterestRateSwapLeg fixedLeg = new FixedInterestRateSwapLeg();
			    FloatingInterestRateSwapLeg floatingLeg = new FloatingInterestRateSwapLeg();
			    
			    double rate = 0.0;
			    double notional = 0.0;
			    Currency currency = null;
			    LocalDate effectiveDate = null;
			    LocalDate maturityDate = null;
			    //Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
			    String referenceRateSource=null;
			    String referenceRateTicker=null;
			    ExternalId externalID = null;
			    
				//String[] line = reader.readNext();
				for(int index=0; index<line.length; index++) {
					String securityAttribute = line[index];
					String header = headingLine[index];
				    
					//System.out.println("Security Attribute: " + securityAttribute);
					switch (header) {
						case "Fixed_Rate":		
							rate = Double.valueOf(securityAttribute);
							fixedLeg.setRate(new Rate(rate));
							//System.out.println("Rate: " + securityAttribute);
							break;
						case "Notional": 		
							notional = Double.valueOf(securityAttribute);
							break;
						case "Currency": 		
							currency = Currency.of(securityAttribute);
							//System.out.println("Currency: " + currency);
							break;
						case "Effective_Date":	
							effectiveDate = LocalDate.parse(securityAttribute);
							break;
						case "Maturity_Date":	
							maturityDate = LocalDate.parse(securityAttribute);
							break;
						case "Fixed_Payment_Date_Frequency":	
							fixedLeg.setPaymentDateFrequency(PeriodFrequency.of(Period.ofMonths(new Integer(securityAttribute))));
							//System.out.println("Payment Date Frequency: " + fixedLeg.getPaymentDateFrequency());
							break;
						case "Fixed_Accrual_Period_Frequency":  
							fixedLeg.setAccrualPeriodFrequency(PeriodFrequency.of(Period.ofMonths(new Integer(securityAttribute))));
							//System.out.println("Accrual Period Frequency: " + fixedLeg.getPaymentDateFrequency());
							break;
						case "Fixed_Payment_Date_Calendar":		
							fixedLeg.setPaymentDateCalendars(getCurrencyCalendar(securityAttribute));
							break;
						case "Fixed_Accrual_Period_Calendar":	
							fixedLeg.setAccrualPeriodCalendars(getCurrencyCalendar(securityAttribute));
							break;
						case "Fixed_Day_Count":					
							fixedLeg.setDayCountConvention(matchDayCountConvention(securityAttribute));
							break;
						case "Fixed_Payment_Date_Business_Day_Convention":	
							fixedLeg.setPaymentDateBusinessDayConvention(matchBusinessDayConvention(securityAttribute));
							//System.out.println("Fixed payment business day convention: " + matchBusinessDayConvention(securityAttribute));
							break;
						case "Fixed_Accrual_Period_Business_Day_Convention":
							fixedLeg.setAccrualPeriodBusinessDayConvention(matchBusinessDayConvention(securityAttribute));
							break;
						case "Fixed_Maturity_Date_Business_Day_Convention":	
							fixedLeg.setMaturityDateBusinessDayConvention(matchBusinessDayConvention(securityAttribute));
							break;
						case "Fixed_Leg":		
							if(securityAttribute.equals("PAY"))
								fixedLeg.setPayReceiveType(PayReceiveType.PAY);
							else 
								fixedLeg.setPayReceiveType(PayReceiveType.RECEIVE);
							break;
						case "Floating_Leg":	
							if(securityAttribute.equals("PAY"))
								floatingLeg.setPayReceiveType(PayReceiveType.PAY);
							else 
								floatingLeg.setPayReceiveType(PayReceiveType.RECEIVE);
							break;
						case "Floating_Payment_Date_Frequency":	
							floatingLeg.setPaymentDateFrequency(PeriodFrequency.of(Period.ofMonths(new Integer(securityAttribute))));
							//System.out.println("Floating payment Date Frequency: " + fixedLeg.getPaymentDateFrequency());
							break;
						case "Floating_Accrual_Period_Frequency":
							floatingLeg.setAccrualPeriodFrequency(PeriodFrequency.of(Period.ofMonths(new Integer(securityAttribute))));
							//System.out.println("Floating accrual Period Frequency: " + fixedLeg.getPaymentDateFrequency());
							break;
						case "Floating_Payment_Date_Calendar":	
							floatingLeg.setPaymentDateCalendars(getCurrencyCalendar(securityAttribute)); 
							break;
						case "Floating_Accrual_Period_Calendar":
							floatingLeg.setAccrualPeriodCalendars(getCurrencyCalendar(securityAttribute));
							break;
						case "Floating_Day_Count":	
							floatingLeg.setDayCountConvention(matchDayCountConvention(securityAttribute)); //TODO currently hardcoded to USNY, make it dynamic
							break;
						case "Floating_Payment_Date_Business_Day_Convention":	
							floatingLeg.setPaymentDateBusinessDayConvention(matchBusinessDayConvention(securityAttribute));
							break;
						case "Floating_Accrual_Period_Business_Day_Convention":	
							floatingLeg.setAccrualPeriodBusinessDayConvention(matchBusinessDayConvention(securityAttribute));
							break;
						case "Floating_Maturity_Date_Business_Day_Convention":	
							floatingLeg.setMaturityDateBusinessDayConvention(matchBusinessDayConvention(securityAttribute));
							break;	
						case "Reset_Period_Frequency":	
							floatingLeg.setResetPeriodFrequency(PeriodFrequency.of(Period.ofMonths(new Integer(securityAttribute))));
							break;
						case "Reset_Period_Business_Day_Convention":	
							floatingLeg.setResetPeriodBusinessDayConvention(matchBusinessDayConvention(securityAttribute));
							break;
						case "Reset_Period_Calendar":	
							floatingLeg.setResetPeriodCalendars(getCurrencyCalendar(securityAttribute));
							break;
						case "Fixing_Date_Business_Day":	
							floatingLeg.setFixingDateBusinessDayConvention(matchBusinessDayConvention(securityAttribute));
							break;
						case "Fixing_Date_Calenders":	
							floatingLeg.setFixingDateCalendars(getCurrencyCalendar(securityAttribute));
							break;
						case "Fixing_Date_Offset": 	
							floatingLeg.setFixingDateOffset(new Integer(securityAttribute));
							break;
						case "Floating_Rate_Type":	
							floatingLeg.setFloatingRateType(FloatingRateType.IBOR);	//TODO currently hardcoded to IBOR, make it dynamic
							break;
						case "Floating_Reference_Rate_Source":	
							referenceRateSource = securityAttribute;
							break;
						case "Floating_Reference_Rate_Ticker":	
							referenceRateTicker = securityAttribute;
							break;
						case "External_ID":
							externalID = ExternalId.of("RADAR", securityAttribute);
							break;
					}
				}
				InterestRateSwapNotional swapNotional = new InterestRateSwapNotional(currency, notional);
				floatingLeg.setNotional(swapNotional);
				fixedLeg.setNotional(swapNotional);
				floatingLeg.setFloatingReferenceRateId(ExternalId.of(referenceRateSource, referenceRateTicker));
				legs.add(floatingLeg);
				legs.add(fixedLeg);
				//System.out.println("Line: " + line);
				InterestRateSwapSecurity irs = new InterestRateSwapSecurity(
				        //ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
						ExternalIdBundle.of(externalID),
				        "Fixed " + rate + " vs Libor 3m",
				        effectiveDate, maturityDate, legs);
				System.out.println("Security: " + irs);
				securities.add(irs);
			}
			reader.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
			//System.exit(1);
			return null;
		}
		return securities;
	}
	
	public BusinessDayConvention matchBusinessDayConvention(String businessDayConvention) {
		if(businessDayConvention.equals("MODIFIED_FOLLOWING"))
			return BusinessDayConventions.MODIFIED_FOLLOWING;
		else if(businessDayConvention.equals("MODIFIED_PRECEDING"))
			return BusinessDayConventions.MODIFIED_PRECEDING;
		else if(businessDayConvention.equals("FOLLOWING"))
			return BusinessDayConventions.FOLLOWING;
		else if(businessDayConvention.equals("PRECEDING"))
			return BusinessDayConventions.PRECEDING;
		else 
			return BusinessDayConventions.NONE;
	}
	
	public DayCount matchDayCountConvention(String dayCountConvention) {
		if(dayCountConvention.equals("ACT_360"))
			return DayCounts.ACT_360;
		else if(dayCountConvention.equals("ACT_365"))
			return DayCounts.ACT_365;
		else if(dayCountConvention.equals("30_360"))
			return DayCounts.THIRTY_360;
		else 
			return null;
	}
	
	protected Set<ExternalId> getCurrencyCalendar(String ccy) {
	    switch (ccy) {
	      case "USD":
	    	  return Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
	      case "GBP":
	    	  return Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "GBLO"));
	      case "EUR":
	    	  return Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "EUTA"));
	      case "AUD":
	    	  return Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "AUSY"));
	      case "JPY":
	    	  return Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "JPTO"));
	      default:
	    	  throw new OpenGammaRuntimeException("Unexpected ccy for calendar " + ccy);
	    }
	}
	
	public void moveFile(String fileName) {
		try {
			String currentDateTime = LocalDateTime.now().toString().replace(':', '-');
			File srcFile = new File(fileName);
			File destDir = new File("\\\\WINDOWS8\\RadarTemp\\out\\securitydata" + currentDateTime + ".csv");
					//"C:/Users/Ankit/Desktop/Out/securitydata" + currentDateTime + ".csv");
			Path src = srcFile.toPath();
			Path dest = new File(destDir, srcFile.getName()).toPath();
			Files.move(srcFile, destDir);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}