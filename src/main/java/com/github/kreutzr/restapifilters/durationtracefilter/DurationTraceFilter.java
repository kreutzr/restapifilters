package com.github.kreutzr.restapifilters.durationtracefilter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * This filter allows to measure the request execution duration and to trace all inner requests as long as the passed custom duration header is passed over to all internal requests.
 */
public class DurationTraceFilter extends GenericFilter
{
  private static final long   serialVersionUID               = -4599612059756903195L;
  private static final String CONFIGURATION_PARAMETER_PREFIX = "durationtracefilter";

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  private String  headerName_;
  private boolean active_;
  private long    maxHeaderLength_;

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void init( final FilterConfig filterConfig )
  throws ServletException
  {
    super.init( filterConfig );

    headerName_ = filterConfig.getInitParameter( CONFIGURATION_PARAMETER_PREFIX + ".header-name" );
    if (headerName_ == null || headerName_.isEmpty())
    {
      headerName_ = "x-duration";
    }

    final String activeString = filterConfig.getInitParameter( CONFIGURATION_PARAMETER_PREFIX + ".active" );
    active_ = activeString == null || activeString.trim().equalsIgnoreCase( "true" );

    final String maxHeaderLengthString = filterConfig.getInitParameter( CONFIGURATION_PARAMETER_PREFIX + ".max-length" );
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

    // Allow adding or adjusting of headers after the response was finished (and perhaps committed) by the service (controller).
    final HttpServletResponseWrapper httpResponse = new HttpServletResponseWrapper( (HttpServletResponse)response );

    // Perform response header calculation if active.
    final DurationTraceFilterHandler handler = new DurationTraceFilterHandler( maxHeaderLength_ );
    try {
      // Proceed with filter chain and let request be executed
      chain.doFilter( request, httpResponse );
    }
    finally {
      final HttpServletRequest  httpRequest  = (HttpServletRequest)  request;

      httpResponse.setHeader( headerName_, handler.updateResponseDurationHeader(
        httpRequest.getRequestURL().toString(),
        httpRequest.getHeader( headerName_ ),
        httpResponse.getHeader( headerName_ ),
        httpResponse.getStatus()
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
