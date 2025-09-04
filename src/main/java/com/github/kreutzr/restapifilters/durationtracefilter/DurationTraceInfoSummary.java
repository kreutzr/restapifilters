package com.github.kreutzr.restapifilters.durationtracefilter;

import java.util.List;

public class DurationTraceInfoSummary extends DurationTraceInfo
{
  public List< DurationTraceInfo > trace;             // An optional list of requests that were executed by inner (micro) service communication.
  public Integer                   traceRemovalCount; // Indicates how many trace list entries were removed to avoid exceeding the maximum header size.
}
