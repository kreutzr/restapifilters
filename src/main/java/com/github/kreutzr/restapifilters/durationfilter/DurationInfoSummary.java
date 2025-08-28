package com.github.kreutzr.restapifilters.durationfilter;

import java.util.List;

public class DurationInfoSummary extends DurationInfo
{
  public List< DurationInfo > trace;             // An optional list of requests that were executed by inner (micro) service communication.
  public Long                 traceRemovalCount; // Indicates how many trace list entries were removed to avoid exceed the maximum header size.
}
