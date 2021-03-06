package com.meteogrup.infrastructure;

import com.amazonaws.util.json.Jackson;
import com.meteogroup.check.CheckResult;
import com.meteogroup.check.PerformanceCheckExecutor;
import com.meteogroup.check.concreete.LatencyCheckResult;
import com.meteogroup.http.HttpRequest;
import com.meteogroup.http.HttpResponse;
import com.meteogroup.infrastructure.ApiRequestHandler;
import org.apache.http.HttpStatus;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApiRequestHandlerTest {

  @Mock
  private PerformanceCheckExecutor performanceCheckExecutor;

  @InjectMocks
  private ApiRequestHandler apiRequestHandler;

  @BeforeMethod
  public void setUp() throws IOException, InstantiationException, IllegalAccessException {
    apiRequestHandler = new ApiRequestHandler();
    MockitoAnnotations.initMocks(this);
    Mockito.when(performanceCheckExecutor.execute(Mockito.any(), Mockito.any(), Mockito.anyInt())).thenReturn(getSuccessfulCheckResult());
  }

  private List<Optional<CheckResult>> getSuccessfulCheckResult() {
    return Collections.singletonList(Optional.of(new LatencyCheckResult(4291L, true)));
  }

  @Test
  public void test_handleRequest() throws IOException {
    final HttpRequest httpRequest = new HttpRequest();

    Map<String, String> params = createCorrectParams();

    httpRequest.setQueryStringParameters(params);
    final HttpResponse httpResponse = apiRequestHandler.handleRequest(httpRequest, null);

    final LatencyCheckResult actual = Jackson.fromJsonString(httpResponse.getBody(), LatencyCheckResult.class);
    final String expectedResponse = Files.readFile(getClass().getClassLoader().getResourceAsStream("response.json"));
    final LatencyCheckResult expected = Jackson.fromJsonString(expectedResponse, LatencyCheckResult.class);

    Assert.assertEquals(httpResponse.getStatusCode(), HttpStatus.SC_OK);
    Assert.assertEquals(actual, expected);
  }

  private Map<String, String> createCorrectParams() {
    return new HashMap<String, String>() {{
        put("checksToExecute", "LatencyCheck");
        put("repetitions", "1");
        put("reduceResults", "false");
        put("payload", "https://elevation.weather.mg?locatedAt=10.041,22.409");
      }};
  }
}
