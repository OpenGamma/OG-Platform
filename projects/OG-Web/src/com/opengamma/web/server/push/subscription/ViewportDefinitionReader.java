package com.opengamma.web.server.push.subscription;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 *
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class ViewportDefinitionReader implements MessageBodyReader<ViewportDefinition> {

  /**
   *
   * @param type
   * @param genericType
   * @param annotations
   * @param mediaType
   * @return
   */
  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(ViewportDefinition.class);
  }

  /**
   * @param type {@link ViewportDefinition} class
   * @param genericType Not used
   * @param annotations Not used
   * @param mediaType Not used
   * @param httpHeaders Not used
   * @param entityStream Stream of JSON
   * @return {@link ViewportDefinition} decoded from the JSON
   * @see ViewportDefinition#fromJSON(String)
   */
  @Override
  public ViewportDefinition readFrom(Class<ViewportDefinition> type,
                                     Type genericType,
                                     Annotation[] annotations,
                                     MediaType mediaType,
                                     MultivaluedMap<String, String> httpHeaders,
                                     InputStream entityStream) throws IOException, WebApplicationException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(entityStream));
    String line;
    StringBuilder builder = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    return ViewportDefinition.fromJSON(builder.toString());
  }
}
