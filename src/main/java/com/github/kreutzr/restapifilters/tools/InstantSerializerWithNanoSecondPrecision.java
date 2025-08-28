package com.github.kreutzr.restapifilters.tools;

import java.time.format.DateTimeFormatterBuilder;

import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;

public class InstantSerializerWithNanoSecondPrecision extends InstantSerializer
{
  private static final long serialVersionUID = 8321858271965055893L;

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public InstantSerializerWithNanoSecondPrecision() {
    super(
      InstantSerializer.INSTANCE, // base
      false, // useTimestamp
      new DateTimeFormatterBuilder().appendInstant( 9 ).toFormatter(), // formatter
      Shape.STRING // shape
    );
  }
}