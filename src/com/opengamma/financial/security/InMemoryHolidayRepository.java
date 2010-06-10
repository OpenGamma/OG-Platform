/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.time.calendar.LocalDate;

import au.com.bytecode.opencsv.CSVReader;

/**
 * In-memory implementation of HolidayRepository that is populated from CSV files.
 */
public class InMemoryHolidayRepository implements HolidayRepository {

  private InMemoryRegionRepository _regionRepo;

  public InMemoryHolidayRepository(InMemoryRegionRepository regionRepo, File currencies, File financialCenters, File exchangeSettlement, File exchangeTrading) throws IOException {
    _regionRepo = regionRepo;
    parseCurrencyFile(currencies);
    parseFinancialCentersFile(financialCenters);
    parseExchangeSettlementFile(exchangeSettlement);
    parseExchangeTradingFile(exchangeTrading);
  }
  
  private void parseCurrencyFile(File currencyFile) throws IOException {
    String[] columnNames = new String[] {"CenterID", "ISOCurrencyCode", "RelatedFinancialCentre", "EventYear"
    CSVReader reader = new CSVReader(new FileReader(currencyFile));
    String[] row;
    while ((row = reader.readNext()) != null) {
      
    }
  }
  
  private void parseFinancialCentersFile(File financialCentersFile) {
    
  }
  
  private void parseExchangeSettlementFile(File exchangeSettlementFile) {
    
  }
  
  private void parseExchangeTradingFile(File exchangeTradingFile) {
    
  }
  
  @Override
  public boolean isHoliday(LocalDate versionDate, Region region, LocalDate holidayDate) {
    // TODO Auto-generated method stub
    return false;
  }
}
