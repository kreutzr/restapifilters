package com.github.kreutzr.restapifilters.tools;

import java.time.Duration;
import java.time.Instant;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer;

/**
 * Provides an ObjectMapper for JSON with nodes sorted in alphabetical order.
 * <br/>
 * <b>NOTE:</b> ObjectMapper is thread safe
 */
public class JsonHelper
{
  private static class SortingNodeFactory extends JsonNodeFactory
  {
    private static final long serialVersionUID = 5562900389805445467L;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ObjectNode objectNode() {
      return new ObjectNode( this, new TreeMap< String, JsonNode >() );
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private static final ObjectMapper MAPPER = createObjectMapper();

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @return An (re-used) ObjectMapper for JSON with nodes sorted in alphabetical order. Never null. (Note: ObjectMapper is thread safe)
   */
  public static ObjectMapper provideObjectMapper()
  {
    return MAPPER;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @return An new created ObjectMapper for JSON with nodes sorted in alphabetical order. Never null.
   * <p/>
   * <b>NOTE:</b> An ObjectMapper is an expensive object. You most probably want to use provideObjectMapper().
   */
  public static ObjectMapper createObjectMapper()
  {
    final JavaTimeModule module = new JavaTimeModule();
    module
      .addSerializer( Duration.class, DurationSerializer.INSTANCE )
      .addSerializer( Instant.class,  new InstantSerializerWithNanoSecondPrecision() )
    ;

    return new ObjectMapper()
      .setNodeFactory( new SortingNodeFactory() )    // Nodes in alphabetical order (required for comparison in Validator)
      .setSerializationInclusion( Include.NON_NULL ) // Ignore null entries
      .registerModule( module )
      .disable( SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS )
      .disable( SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS )
      ;
  }
}
