package com.github.kreutzr.restapifilters.durationfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DurationServletFilterHandlerTest
{
  private void sleep( final long waitMs )
  {
    try {
      Thread.sleep( waitMs );
    }
    catch( final InterruptedException ex ) {
      // Ignore this
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  public void testThatSettingOuterDurationHeaderWorks()
  {
    final long maxHeaderLength = 16 * 1024; // 16 K
    try {
      final String outerUrl = "http://my-server:8080/my-app/my-endpoint";

      // --------------------------------------------------------------------------------------
      // Test with null
      // --------------------------------------------------------------------------------------
      // Given
      String originalSummary = null;

      // When
      DurationServletFilterHandler handler = new DurationServletFilterHandler( maxHeaderLength );
      sleep( 2000 );
      String responseSummary = handler.updateResponseDurationHeader( outerUrl, originalSummary);

//System.out.println( responseSummary );

      // Then
      assertThat( responseSummary ).contains( "begin" );
      assertThat( responseSummary ).contains( "end" );
      assertThat( responseSummary ).contains( "duration" );
      assertThat( responseSummary ).contains( "url" );
      assertThat( responseSummary ).contains( "trace" );

      // --------------------------------------------------------------------------------------
      // test with whitespaces
      // --------------------------------------------------------------------------------------
      // Given
      originalSummary = "   ";

      // When
      handler = new DurationServletFilterHandler( maxHeaderLength );
      sleep( 1000 );
      responseSummary = handler.updateResponseDurationHeader( outerUrl, originalSummary);

//System.out.println( responseSummary );

      // Then
      assertThat( responseSummary ).contains( "begin" );
      assertThat( responseSummary ).contains( "end" );
      assertThat( responseSummary ).contains( "duration" );
      assertThat( responseSummary ).contains( "url" );
      assertThat( responseSummary ).contains( "trace" );
    }
    catch( final Throwable ex ) {
      ex.printStackTrace();
      assertTrue( false, "Unreachable" );
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  public void testThatSettingInnerDurationHeaderWorks()
  {
    final long maxHeaderLength = 16 * 1024; // 16 K
    try {
      final String outerUrl     = "http://my-server:8080/my-app/my-endpoint";
      final String innerUrl     = "http://my-server:8080/my-app/my-inner-endpoint";
      final String innermostUrl = "http://my-server:8080/my-app/my-innermost-endpoint";

      // --------------------------------------------------------------------------------------
      // Test with outer summary
      // --------------------------------------------------------------------------------------
      // Given
      final String originalSummary = null;

      // When
      final DurationServletFilterHandler handler = new DurationServletFilterHandler( maxHeaderLength );
      sleep( 2000 );
      final DurationServletFilterHandler innerHandler = new DurationServletFilterHandler( maxHeaderLength );
      sleep( 1000 );
      final DurationServletFilterHandler innermostHandler = new DurationServletFilterHandler( maxHeaderLength );
      sleep( 500 );
      final String innermostResponseSummary = innermostHandler.updateResponseDurationHeader( innermostUrl, originalSummary );
      final String innerResponseSummary     = innerHandler.updateResponseDurationHeader( innerUrl, innermostResponseSummary );
      final String responseSummary          = handler.updateResponseDurationHeader( outerUrl, innerResponseSummary);

//System.out.println( responseSummary );

      // Then
      assertThat( responseSummary ).contains( "begin" );
      assertThat( responseSummary ).contains( "end" );
      assertThat( responseSummary ).contains( "duration" );
      assertThat( responseSummary ).contains( "url" );
      assertThat( responseSummary ).contains( "trace" );
      assertThat( responseSummary ).contains( outerUrl );
      assertThat( responseSummary ).contains( innerUrl );
    }
    catch( final Throwable ex ) {
      ex.printStackTrace();
      assertTrue( false, "Unreachable" );
    }
  }
}