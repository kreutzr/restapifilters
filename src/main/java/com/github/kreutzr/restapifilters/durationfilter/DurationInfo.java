package com.github.kreutzr.restapifilters.durationfilter;

import java.time.Duration;
import java.time.Instant;

public class DurationInfo
{
  public String   url;            // Optional for RequestDurationSummary, mandatory for RequestDuration within outer trace list.
  public Instant  begin;          // The time (UTC) the request arrived.
  public Instant  end;            // The time (UTC) the response left.
  public Duration duration;       // The difference between end and begin.
  public Duration filterDuration; // The time spent on the filter internal logic.
}
