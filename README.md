<img src="doc/img/restapifilters-logo_320x160.png" alt="RestApiFilters"/>

RestApiFilters provides servlet filters that may be used by web (micro) services to return diagnostic information


# DurationServletFilter
This filter provides the
- total execution duration and
- invocation traces through the inner service requests (ordered by their execution begin time)

in a configurable response header.

## Example
The header value is a JSON string structured as follows:
```
{
  "begin":    "<ISO-8601 time format (as UTC)>",      // e.g. "2025-08-28T20:21:15.000000000Z"
  "end":      "<ISO-8601 time format (as UTC)>",      // e.g. "2025-08-28T20:21:25.000000000Z"
  "duration": "<ISO-8601 duration (period)>",         // e.g. "PT10S"
  "trace": [                                          // A list that holds all executed requests ordered by their begin time.
    {
      "url":      "<URL of inner request>",           // e.g. "https://my-server:8080/my-app/my-endpoint"
      "begin":    "<ISO-8601 time format (as UTC)>",  // e.g. "2025-08-28T20:21:15.000000000Z"
      "end":      "<ISO-8601 time format (as UTC)>",  // e.g. "2025-08-28T20:21:25.000000000Z"
      "duration": "<ISO-8601 duration (period)>",     // e.g. "PT10S"
    },
    {
      "url":      "<URL of inner request>",           // e.g. "https://my-server:8080/my-app/my-inner-endpoint"
      "begin":    "<ISO-8601 time format (as UTC)>",  // e.g. "2025-08-28T20:21:17.000000000Z"
      "end":      "<ISO-8601 time format (as UTC)>",  // e.g. "2025-08-28T20:21:19.000000000Z"
      "duration": "<ISO-8601 duration (period)>",     // e.g. "PT2S"
    }
  ],
  "traceRemovalCount": <int>                          // Optional counter to indicate how many trace entries were removed to avoid exceed the maximum header length. Missing if no trace entry was removed.
}
```

## Configuration
 - The name of the custom header is configurable using the key "durationfilter.header-name". The default is "x-duration".
 - The filter may be deactivated by configuring "durationfilter.active" to any value but "true" (case insensitive). The filter is active by default.
 - The maximum header value length may be configured using the key "durationfilter.max-length" as bytes. The default is 16384 (= 16 * 1024 = 16 K).
 
**web.xml**
```
<filter>
  <filter-name>durationfilter</filter-name>
  <filter-class>com.github.kreutzr.restapifilters.durationfilter.DurationfilterFilter</filter-class>
  <init-param>
    <param-name>durationfilter.header-name</param-name>
    <param-value>x-duration</param-value>
  </init-param>
  <init-param>
    <param-name>durationfilter.active</param-name>
    <param-value>true</param-value>
  </init-param>
  <init-param>
    <param-name>durationfilter.max-length</param-name>
    <param-value>16384</param-value>
  </init-param>
</filter>

<filter-mapping>
  <filter-name>durationfilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```
 
