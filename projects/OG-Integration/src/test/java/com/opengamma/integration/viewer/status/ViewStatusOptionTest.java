/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link ViewStatusOption}
 */
@Test(groups = TestGroup.UNIT)
public class ViewStatusOptionTest {
  
  private static final Options s_options = ViewStatusOption.createOptions();
  private static final CommandLineParser s_parser = new PosixParser();

  public void defaultUser() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME"};
    
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args));
    assertNotNull(statusOption);
    assertNotNull(statusOption.getUser());
    assertEquals(UserPrincipal.getLocalUser(), statusOption.getUser());
  }
  
  public void userOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "-u", "test/127.0.0.1"};
    ViewStatusOption statusOption = ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args));
    assertNotNull(statusOption);
    assertNotNull(statusOption.getUser());
    assertEquals("test", statusOption.getUser().getUserName());
    assertEquals("127.0.0.1", statusOption.getUser().getIpAddress());
  }
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void invalidUserOption() throws Exception {
    String[] args = {"-n", "PORTFOLIO_NAME", "-u", "test~127.0.0.1"};
    ViewStatusOption.getViewStatusReporterOption(s_parser.parse(s_options, args));
  }
}
