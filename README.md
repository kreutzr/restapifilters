<img src="doc/img/restapifilters-logo_320x160.png" alt="RestApiFilters"/>

RestApiFilters provides filters that may be used by web (micro) services to return diagnostic information.

**Manuals** are currently provided in [English](doc/manual_en.adoc) and [German](doc/manual_de.adoc).


# DurationTraceFilter
This filter provides the
- **total execution duration** and
- invocation traces through the inner service requests (ordered by their execution begin time) including the **duration** and **http status**

in a configurable response header.

## Example
The header value is a JSON string structured as follows:
```
{
  "begin":    "<ISO-8601 time format (as UTC)>",        // e.g. "2025-08-28T20:21:15.000000000Z"
  "end":      "<ISO-8601 time format (as UTC)>",        // e.g. "2025-08-28T20:21:25.000000000Z"
  "duration": "<ISO-8601 duration (period)>",           // e.g. "PT10S"
  "trace": [                                            // A list that holds all executed requests ordered by their begin time.
    {
      "begin":      "<ISO-8601 time format (as UTC)>",  // e.g. "2025-08-28T20:21:15.000000000Z"
      "end":        "<ISO-8601 time format (as UTC)>",  // e.g. "2025-08-28T20:21:25.000000000Z"
      "duration":   "<ISO-8601 duration (period)>",     // e.g. "PT10S"
      "url":        "<URL of inner request>",           // e.g. "https://my-server:8080/my-app/my-endpoint"
      "httpstatus": <http status of inner response>     // e.g. 200, 404, ...
    },
    {
      "begin":      "<ISO-8601 time format (as UTC)>",  // e.g. "2025-08-28T20:21:17.000000000Z"
      "end":        "<ISO-8601 time format (as UTC)>",  // e.g. "2025-08-28T20:21:19.000000000Z"
      "duration":   "<ISO-8601 duration (period)>",     // e.g. "PT2S"
      "url":        "<URL of inner request>",           // e.g. "https://my-server:8080/my-app/my-inner-endpoint"
      "httpstatus": <http status of inner response>     // e.g. 200, 404, ...
    }
  ],
  "traceRemovalCount": <int>                            // Optional counter to indicate how many trace entries were removed to avoid exceeding the maximum header length. Missing if no trace entry was removed.
}
```

