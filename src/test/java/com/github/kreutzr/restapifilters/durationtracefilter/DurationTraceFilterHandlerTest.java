package com.github.kreutzr.restapifilters.durationtracefilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.kreutzr.restapifilters.durationtracefilter.DurationTraceFilterHandler;

public class DurationTraceFilterHandlerTest
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
    final int httpstatus = 200;
    final long maxHeaderLength = 16 * 1024; // 16 K
    try {
      final String outerUrl = "http://my-server:8080/my-app/my-endpoint";

      // --------------------------------------------------------------------------------------
      // Test with null
      // --------------------------------------------------------------------------------------
      // Given
      final String originalSummary = null;

      // When
      final DurationTraceFilterHandler handler = new DurationTraceFilterHandler( maxHeaderLength );
      sleep( 2000 );
      final String responseSummary = handler.updateResponseDurationHeader( outerUrl, null, originalSummary, httpstatus );

//System.out.println( responseSummary );

      // Then
      assertThat( responseSummary ).contains( "begin" );
      assertThat( responseSummary ).contains( "end" );
      assertThat( responseSummary ).contains( "duration" );
      assertThat( responseSummary ).contains( "url" );
      assertThat( responseSummary ).contains( "trace" );
      assertThat( responseSummary ).contains( "httpstatus" );
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
    final int httpstatus = 200;
    final long maxHeaderLength = 16 * 1024; // 16 K
    try {
      final String urlA = "http://my-server:8080/my-app/my-A-endpoint";
      final String urlB = "http://my-server:8080/my-app/my-B-endpoint";
      final String urlC = "http://my-server:8080/my-app/my-C-endpoint";
      final String urlD = "http://my-server:8080/my-app/my-D-endpoint";

      // --------------------------------------------------------------------------------------
      // Test with outer summary
      // --------------------------------------------------------------------------------------
      // Given

      // When
      // =====================
      // ---> A ---> B
      //        ---> C ---> D
      // =====================
      final DurationTraceFilterHandler handlerA = new DurationTraceFilterHandler( maxHeaderLength );
      sleep( 500 );  // Service A execution time begin

      final DurationTraceFilterHandler handlerB = new DurationTraceFilterHandler( maxHeaderLength );
      sleep( 2000 ); // Service B execution time total
      final String responseSummaryB = handlerB.updateResponseDurationHeader( urlB, null,  null, httpstatus );

      final DurationTraceFilterHandler handlerC = new DurationTraceFilterHandler( maxHeaderLength );
      sleep( 500 );  // Service C execution time begin

      final DurationTraceFilterHandler handlerD = new DurationTraceFilterHandler( maxHeaderLength );
      sleep( 1000 ); // Service D execution time total
      final String responseSummaryD = handlerD.updateResponseDurationHeader( urlD, responseSummaryB, null, httpstatus );

      sleep( 500 ); // Service C execution time end
      final String responseSummaryC = handlerC.updateResponseDurationHeader( urlC, responseSummaryB, responseSummaryD, httpstatus );

      sleep( 500 );  // Service A execution time end
      final String responseSummaryA = handlerA.updateResponseDurationHeader( urlA, null, responseSummaryC, httpstatus );

//System.out.println( responseSummaryA );

      // Then
      assertThat( responseSummaryA ).contains( "begin" );
      assertThat( responseSummaryA ).contains( "end" );
      assertThat( responseSummaryA ).contains( "duration" );
      assertThat( responseSummaryA ).contains( "url" );
      assertThat( responseSummaryA ).contains( "trace" );
      assertThat( responseSummaryA ).contains( "httpstatus" );
    }
    catch( final Throwable ex ) {
      ex.printStackTrace();
      assertTrue( false, "Unreachable" );
    }
  }
}