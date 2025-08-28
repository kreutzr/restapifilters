package com.github.kreutzr.restapifilters.durationfilter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This servlet filter allows to measure the request execution duration and to trace all inner requests as long as the passed custom duration header is passed over to all internal requests.
 * <p/>
 * <b>Example</b>
 * <br/>
 * The header value is JSON structured as follows:
 * <pre>
 * {
 *   "begin":    "&lt;ISO-8601 time format (as UTC)&gt;",      // e.g. "2025-08-28T20:21:15.000000000Z"
 *   "end":      "&lt;ISO-8601 time format (as UTC)&gt;",      // e.g. "2025-08-28T20:21:25.000000000Z"
 *   "duration": "&lt;ISO-8601 duration (period)&gt;",         // e.g. "PT10S"
 *   "trace": [                                          // A list that holds all executed requests ordered by their begin time.
 *     {
 *       "url":      "&lt;URL of inner request&gt;",           // e.g. "https://my-server:8080/my-app/my-endpoint"
 *       "begin":    "&lt;ISO-8601 time format (as UTC)&gt;",  // e.g. "2025-08-28T20:21:15.000000000Z"
 *       "end":      "&lt;ISO-8601 time format (as UTC)&gt;",  // e.g. "2025-08-28T20:21:25.000000000Z"
 *       "duration": "&lt;ISO-8601 duration (period)&gt;",     // e.g. "PT10S"
 *     },
 *     {
 *       "url":      "&lt;URL of inner request&gt;",           // e.g. "https://my-server:8080/my-app/my-inner-endpoint"
 *       "begin":    "&lt;ISO-8601 time format (as UTC)&gt;",  // e.g. "2025-08-28T20:21:17.000000000Z"
 *       "end":      "&lt;ISO-8601 time format (as UTC)&gt;",  // e.g. "2025-08-28T20:21:19.000000000Z"
 *       "duration": "&lt;ISO-8601 duration (period)&gt;",     // e.g. "PT2S"
 *     },
 *   ]
 * }
 * </pre>
 * <p/>
 * <b>Configuration</b>
 * <br/>
 * <ul>
 * <li>The name of the custom header is configurable using the key "durationfilter.header-name". The default is "x-duration".</li>
 * <li>The filter may be deactivated by configuring "durationfilter.active" to any value but "true" (case insensitive). The filter is active by default.</li>
 * <li>The maximum header value length may be configured using the key "durationfilter.max-length" as bytes. The default is 16384 (= 16 * 1024 = 16 K).</li>
 * </ul>
 * */
public class DurationServletFilter implements Filter
{
  private String  headerName_;
  private boolean active_;
  private long    maxHeaderLength_;

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void init( final FilterConfig filterConfig )
  throws ServletException
  {
    headerName_ = filterConfig.getInitParameter( "durationfilter.header-name" );
    if (headerName_ == null || headerName_.isEmpty())
    {
      headerName_ = "x-duration";
    }

    final String activeString = filterConfig.getInitParameter( "durationfilter.active" );
    active_ = activeString == null || activeString.trim().equalsIgnoreCase( "true" );

    final String maxHeaderLengthString = filterConfig.getInitParameter( "durationfilter.max-length" );
    if (maxHeaderLengthString == null || maxHeaderLengthString.isEmpty())
    {
      maxHeaderLength_ = 16 * 1024; // 16 K
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void doFilter( final ServletRequest request, final ServletResponse response, final FilterChain chain )
  throws IOException, ServletException
  {
    // Skip response header calculation if not active
    if( !active_ ) {
      chain.doFilter( request, response );
      return;
    }

    // Perform response header calculation if active.
    final DurationServletFilterHandler handler = new DurationServletFilterHandler( maxHeaderLength_ );
    try {
      // Proceed with filter chain and let request be executed
      chain.doFilter( request, response );
    }
    finally {
      final HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
      final HttpServletResponse httpResponse = (HttpServletResponse) response;

      httpResponse.setHeader( headerName_, handler.updateResponseDurationHeader(
        httpRequest.getRequestURL().toString(),
        httpResponse.getHeader( headerName_ )
      ) );
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void destroy()
  {
    // Do nothing
  }
}
