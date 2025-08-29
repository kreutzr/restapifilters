package com.github.kreutzr.restapifilters.durationfilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.kreutzr.restapifilters.tools.JsonHelper;

/**
 * Since ServletRequest and ServletResponse can not easily be mocked, the filter logic was moved to this testable handler class.
 * Only reading and setting the duration header value remains within the servlet filter itself.
 */
public class DurationServletFilterHandler
{
  private static Duration lastFinalisationDuration_ = Duration.ZERO;

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Instant begin_;
  private final long    maxHeaderLength_;

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Constructor
   * @param maxLength The maximum header length in bytes. If the header length exceeds this length the trace list is reduced.
   */
  public DurationServletFilterHandler( final long maxLength )
  {
    begin_           = Instant.now();
    maxHeaderLength_ = maxLength;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Updates the duration header value (DurationInfoSummary as JSON) of the current response.
   * @param requestUrl The URL of the current request. Must not be null.
   * @param requestSummaryJson The duration header value of the current request. May be null.
   * @param responseSummaryJson The duration header value of the current response. May be null.
   * @return The updated duration header value. Never null.
   * @throws IOException
   */
  public String updateResponseDurationHeader( final String requestUrl, final String requestSummaryJson, final String responseSummaryJson )
  throws IOException
  {
    final Instant end = Instant.now();

    DurationInfoSummary summary = null;
    if( responseSummaryJson != null ) {
      // (A) Response summaries are the most recent
      summary = JsonHelper.provideObjectMapper().readValue( responseSummaryJson, DurationInfoSummary.class );
    }
    else if( requestSummaryJson != null ) {
      // (B) Request summaries are better than nothing
      summary = JsonHelper.provideObjectMapper().readValue( requestSummaryJson, DurationInfoSummary.class );
    }
    else {
      // (C) Start with empty summary.
      summary = new DurationInfoSummary();
    }

    // ================================================================================================================
    //
    //   /          /                        // We start with no request duration headers.
    // -----> [a] -----> [b]
    //         |          |
    //         |     b    |  (C)             // No response duration header is returned (due to missing sub requests).
    //         | <---------                  //  Therefore initialize with local trace.
    //         |
    //         |                             // IMPORTANT: Received response duration header MUST be passed
    //         |                             //   as request header for sibling requests!
    //         |    b          b
    //         | ------> [c] -----> [d]
    //                               |
    //  abcd       bcd         bd    |  (B)  // No response duration header is returned (due to missing sub requests).
    // <------   <------     <--------       //  Therefore use the request duration header to append a trace.
    //
    //   (A)                                 // Response duration header is extended with local trace.
    //
    // ================================================================================================================

    // Create and append a trace entry if this request is an inner request.
    final DurationInfo traceEntry = new DurationInfo();
    traceEntry.url      = requestUrl;
    traceEntry.begin    = begin_;
    traceEntry.end      = end;
    traceEntry.duration = Duration.between( traceEntry.begin, traceEntry.end );

    if( summary.trace == null ) {
      summary.trace = new ArrayList< DurationInfo >();
    }
    insertByBegin( summary.trace, traceEntry );

    // Finalize the "outermost" request by setting end and duration.
    summary.begin    = begin_;
    summary.end      = end;
    summary.duration = Duration.between( summary.begin, summary.end );

    final Instant finalizationStart = Instant.now();
    traceEntry.filterDuration = Duration
      .between( end, finalizationStart )
      .plus( lastFinalisationDuration_ ) // Since we can not add values after serialization, we use the former finalization duration as expected remaining execution time.
      ;

    // Set updated duration header to response.
    String summaryString = JsonHelper.provideObjectMapper().writeValueAsString( summary );
    if( summaryString.length() > maxHeaderLength_ ) {
      // Remove latest (innermost) trace information.
      summary.trace.remove( summary.trace.size() - 1 );
      summary.traceRemovalCount = summary.traceRemovalCount == null
        ? 1
        : summary.traceRemovalCount + 1;
      summaryString = JsonHelper.provideObjectMapper().writeValueAsString( summary );
    }

    // Assure alphabetical ordered attributes (which unfortunately only works on JsonNodes NOT on class attributes).
    final JsonNode root   = JsonHelper.provideObjectMapper().readTree( summaryString );
    final String   result = JsonHelper.provideObjectMapper().writeValueAsString( root );

    // Calculate finalization duration overhead for next request.
    // NOTE: We assume that the finalization overhead is more or less constant over all requests.
    final Instant finalizationEnd = Instant.now();
    lastFinalisationDuration_ = Duration.between( finalizationStart, finalizationEnd );

    return result;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  private void insertByBegin( final List< DurationInfo > trace,  DurationInfo traceEntry )
  {
    int i=0;
    while( i < trace.size() ) {
      if( trace.get( i ).begin.isAfter( traceEntry.begin ) ) {
        trace.add( i, traceEntry );
        return;
      }
      i++;
    }
    trace.add( traceEntry );
  }
}
