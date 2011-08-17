/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WebPushTestUtils {

  private static final String URL_BASE = "http://localhost:8080";

  private WebPushTestUtils() {
  }

  /* package */ static URL url(String path) throws MalformedURLException {
    return new URL(URL_BASE + path);
  }

  /* package */ static String readFromPath(String path) throws IOException {
    return readFromPath(path, null);
  }

  /* package */ static String readFromPath(String path, String clientId) throws IOException {
    String fullPath;
    if (clientId != null) {
      fullPath = path + "?clientId=" + clientId;
    } else {
      fullPath = path;
    }
    BufferedReader reader = null;
    StringBuilder builder;
    try {
      char[] chars = new char[512];
      builder = new StringBuilder();
      reader = new BufferedReader(new InputStreamReader(url(fullPath).openStream()));
      int bytesRead;
      while ((bytesRead = reader.read(chars)) != -1) {
        builder.append(chars, 0, bytesRead);
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return builder.toString();
  }

  /**
   * Creates and starts a Jetty server using the {@code web-push/WEB-INF/web.xml} file and configured using Spring
   * @return The server and the Spring context
   * @param springXml The location of the Spring XML config file
   */
  public static Pair<Server, WebApplicationContext> createJettyServer(String springXml) throws Exception {
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(8080);
    WebAppContext context = new WebAppContext();
    context.setContextPath("/");
    context.setResourceBase("build/classes");
    context.setDescriptor("web-push/WEB-INF/web.xml");
    Map<String, String> params = new HashMap<String, String>();
    params.put("contextConfigLocation", springXml);
    context.setInitParams(params);
    context.addEventListener(new ContextLoaderListener());
    Server server = new Server();
    server.addConnector(connector);
    server.setHandler(context);
    server.start();
    WebApplicationContext springContext = (WebApplicationContext) context.getServletContext().getAttribute(
            WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    return new ObjectsPair<Server, WebApplicationContext>(server, springContext);
  }
}
