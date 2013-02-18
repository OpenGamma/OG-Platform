package com.opengamma.integration.tool.portfolio.xml.v1_0;

import java.io.IOException;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import com.beust.jcommander.internal.Sets;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class GenerateSchema {

  @Test
  public void generate() throws JAXBException, IOException {

    JAXBContext ctx = JAXBContext.newInstance(PortfolioDocumentV1_0.class);

    DOMResult result = extractSchemaResult(ctx);

    Document document = (Document) result.getNode();

    OutputFormat format = new OutputFormat(document);
    format.setIndenting(true);
    XMLSerializer serializer = new XMLSerializer(System.out, format);
    serializer.serialize(document);



  }

  private DOMResult extractSchemaResult(JAXBContext ctx) throws IOException {

    final Set<DOMResult> resultWrapper = Sets.newHashSet();

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
