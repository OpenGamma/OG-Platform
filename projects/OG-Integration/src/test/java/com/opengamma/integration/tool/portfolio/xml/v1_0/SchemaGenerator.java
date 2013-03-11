/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;

import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.PortfolioDocumentV1_0;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Utility class used for generating a schema from a base portfolio document. This can
 * be used as a starting point for schema creation. The schema will be written to
 * the standard output stream.
 */
public class SchemaGenerator {

  public static void main(String[] args) throws JAXBException, IOException {

    JAXBContext ctx = JAXBContext.newInstance(PortfolioDocumentV1_0.class);

    DOMResult result = extractSchemaResult(ctx);

    Document document = (Document) result.getNode();

    OutputFormat format = new OutputFormat(document);
    format.setIndenting(true);
    XMLSerializer serializer = new XMLSerializer(System.out, format);
    serializer.serialize(document);
  }

  private static DOMResult extractSchemaResult(JAXBContext ctx) throws IOException {

    final Set<DOMResult> resultWrapper = new HashSet<>();

    ctx.generateSchema(new SchemaOutputResolver() {
      @Override
      public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        DOMResult result = new DOMResult();
        result.setSystemId(suggestedFileName);
        resultWrapper.add(result);
        return result;
      }
    });

    return resultWrapper.iterator().next();
  }

}
