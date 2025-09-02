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
   * @param responseSummaryJson The duration header value of the current response. Must not be null.
   * @return The updated duration header value. Never null.
   * @throws IOException
   */
  public String updateResponseDurationHeader( final String requestUrl, final String responseSummaryJson )
  throws IOException
  {
    final Instant end = Instant.now();

    DurationInfoSummary summary = null;
    if( responseSummaryJson == null || responseSummaryJson.isBlank() ) {
      // Start with empty request duration summary if duration header is not set.
      summary = new DurationInfoSummary();
    }
    else {
      summary = JsonHelper.provideObjectMapper().readValue( responseSummaryJson, DurationInfoSummary.class );
    }

    // Create and append a trace entry if this request is an inner request.
    final DurationInfo traceEntry = new DurationInfo();
    traceEntry.url      = requestUrl;
    traceEntry.begin    = begin_;
    traceEntry.end      = end;
    traceEntry.duration = Duration.between( traceEntry.begin, traceEntry.end );

    if( summary.trace == null ) {
      summary.trace = new ArrayList< DurationInfo >();
    }
    if( summary.traceRemovalCount == null ) {
      insertByBegin( summary.trace, traceEntry );
    }
    else {
      // We do not add any more entries to avoid disturbing the order (e.g. after an entry with a long url was removed but a later entry with a shorter url is added).
      summary.traceRemovalCount = summary.traceRemovalCount + 1;
    }

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
    if( summary.traceRemovalCount == null && summaryString.length() > maxHeaderLength_ ) {
      // Remove latest (innermost) trace information.
      summary.trace.remove( summary.trace.size() - 1 );
      summary.traceRemovalCount = 1;
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
