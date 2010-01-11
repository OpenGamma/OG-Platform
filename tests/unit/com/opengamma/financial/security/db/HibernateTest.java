package com.opengamma.financial.security.db;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.id.DomainSpecificIdentifier;

public class HibernateTest {
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateTest.class);

}
