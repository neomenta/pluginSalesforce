package neome.plugin.salesforce.integration.sdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import neome.plugin.salesforce.integration.JWTGenerator;
import neome.plugin.salesforce.integration.SalesForceAPIConfig;

public class SFConnectionManager
{
  private final SalesForceAPIConfig salesForceAPIConfig;

  private static final int MAX_RETRIES = 3;

  private static final long RETRY_DELAY_MS = 2000; // 2 seconds delay

  private final Cache<String, PartnerConnection> connectionCache;

  public SFConnectionManager(SalesForceAPIConfig salesForceAPIConfig)
  {
    this.salesForceAPIConfig = salesForceAPIConfig;
    this.connectionCache = CacheBuilder.newBuilder()
      .expireAfterWrite(30, TimeUnit.MINUTES)
      .maximumSize(1000)
      .build();
  }

  // Create a new Salesforce connection for a specific user
  private PartnerConnection createConnectionForUser(String username) throws ConnectionException
  {
    ConnectorConfig config = new ConnectorConfig();
    config.setUsername(username);
    config.setServiceEndpoint("%s/services/Soap/u/v63.0/".formatted(salesForceAPIConfig.configForm.domain));
    String accessToken = getAccessToken(username);
    config.setSessionId(accessToken); // Use JWT token
    return new PartnerConnection(config);
  }

  private String getJWT(String username)
  {
    return JWTGenerator.INSTANCE.generate(salesForceAPIConfig.entId,
      salesForceAPIConfig.configForm.privateKey.value.mediaIdDocument,
      salesForceAPIConfig.configForm.clientId,
      username);
  }

  private String getAccessToken(String username)
  {
    String jwt = getJWT(username);
    String url = "https://login.salesforce.com/services/oauth2/token";
    String formData =
      "assertion=" + encodeValue(jwt) + "&grant_type=" + encodeValue(GrantType.JWT.getValue());
    HttpClient httpclient = HttpClient.newBuilder().build();
    HttpRequest authenticationReq = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(formData))
      .build();
    try
    {
      HttpResponse<String> response = httpclient.send(authenticationReq, HttpResponse.BodyHandlers.ofString());
      String responseBody = response.body();
      JsonObject asJsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
      return asJsonObject.get("access_token").getAsString();
    }
    catch(IOException | InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  private static String encodeValue(String value)
  {
    return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  @FunctionalInterface
  public interface CheckedFunction<T, R>
  {
    R apply(T t) throws Exception;  // Allows throwing checked exceptions
  }

  public <T> T executeWithRetry(String userId, CheckedFunction<PartnerConnection, T> apiCall) throws RuntimeException
  {
    int attempt = 0;
    while(attempt < MAX_RETRIES)
    {
      try
      {
        PartnerConnection connection = getOrCreateConnection(userId);
        return apiCall.apply(connection);
      }
      catch(ConnectionException e)
      {
        if(isInvalidSessionError(e))
        {
          System.out.println("Session expired. Re-authenticating for user " + userId);
          try
          {
            refreshConnection(userId);  // Handle re-authentication failure gracefully
          }
          catch(ConnectionException authEx)
          {
            System.err.println("Failed to refresh connection: " + authEx.getMessage());
            throw new RuntimeException("Re-authentication failed", authEx);
          }
        }
        else
        {
          throw new RuntimeException("Salesforce API call failed: " + e.getMessage(), e);
        }
      }
      catch(Exception ex)
      {
        throw new RuntimeException("Unexpected error: " + ex.getMessage(), ex);
      }

      attempt++;
      try
      {
        Thread.sleep(RETRY_DELAY_MS * attempt);  // Exponential backoff
      }
      catch(InterruptedException ignored)
      {

      }
    }
    throw new RuntimeException("Failed after " + MAX_RETRIES + " retries.");
  }

  private PartnerConnection getOrCreateConnection(String username) throws ConnectionException
  {
    PartnerConnection connection = connectionCache.getIfPresent(username);
    if(connection == null)
    {
      connection = createNewConnection(username);
      connectionCache.put(username, connection);
    }
    return connection;
  }

  private void refreshConnection(String username) throws ConnectionException
  {
    connectionCache.invalidate(username);
    PartnerConnection newConnection = createNewConnection(username);
    connectionCache.put(username, newConnection);
  }

  private PartnerConnection createNewConnection(String username) throws ConnectionException
  {
    System.out.println("Creating new connection for user: " + username);
    return createConnectionForUser(username);
  }

  private boolean isInvalidSessionError(ConnectionException ex)
  {
    return ex.getMessage() != null && ex.getMessage().contains("INVALID_SESSION_ID");
  }
}
