/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.csv;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CSVDocumentReaderTest {
  
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  private List<FudgeMsg> _expectedRows;
  CSVDocumentReader _csvDocReader;
  
  @BeforeMethod
  public void setUp() {
    
    _expectedRows = Lists.newArrayList();
    
    MutableFudgeMsg row = s_fudgeContext.newMessage();
    row.add("Name", "Kirk");
    row.add("JobTitle", "CEO");
    _expectedRows.add(row);
    
    row = s_fudgeContext.newMessage();
    row.add("Name", "Jim");
    row.add("JobTitle", "CTO");
    _expectedRows.add(row);
    
    row = s_fudgeContext.newMessage();
    row.add("Name", "Elaine");
    row.add("JobTitle", "CQO");
    _expectedRows.add(row);
    
    row = s_fudgeContext.newMessage();
    row.add("Name", "Andrew");
    row.add("JobTitle", "Engineer");
    _expectedRows.add(row);
    
    row = s_fudgeContext.newMessage();
    row.add("Name", "Alan");
    _expectedRows.add(row);
    
    _csvDocReader = new CSVDocumentReader(CSVDocumentReaderTest.class.getResource("test_csv_document_read.csv"));
    
  }
  
  public void read() {
    List<FudgeMsg> actualRows = Lists.newArrayList();
    for (FudgeMsg row : _csvDocReader) {
      actualRows.add(row);
    }
    assertEquals(_expectedRows, actualRows);        
  }
  
  public void multipleReads() {
    read();
    read();
  }

}
