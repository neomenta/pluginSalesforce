package neome.plugin.salesforce;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.neome.plugin.base.ApiDone;
import com.neome.plugin.base.IApiDone;
import com.neome.plugin.base.IPluginWebhookAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.neome.plugin.request.PluginWebhookHttpReq;
import com.neome.util.tuple.Tuple3;
import neome.plugin.salesforce.api.ApiGetAccountDetails;
import neome.plugin.salesforce.api.ApiGetEventDetails;
import neome.plugin.salesforce.api.ApiGetOpportunityDetails;
import neome.plugin.salesforce.api.ApiGetTaskDetails;
import neome.plugin.salesforce.form.SalesforceRecordNotFound;
import neome.plugin.salesforce.integration.exception.SObjectNotFoundException;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import org.jetbrains.annotations.Nullable;

public class WebhookExecutor
{

  public static final String WEBHOOK_TYPE_DETAIL = "detail";

  public static final String QUERY_PARAM_OBJECT_TYPE = "objectType";

  public static final String QUERY_PARAM_ID = "id";

  public static final String QUERY_PARAM_USERNAME = "username";

  public void execute(ApiCtx ctx, PluginWebhookHttpReq webhookReq, IPluginWebhookAcceptor output)
  {
    try
    {
      IApiDone outputForm = getOutputForm(ctx, webhookReq);
      output.success(outputForm);
    }
    catch(Throwable e)
    {
      output.error(new PluginApiException(e, "Failed to execute webhook"));
    }
  }

  public static List<String> getPathSegments(String url)
  {
    try
    {
      URI uri = new URI(url);
      return List.of(uri.getPath().split("/"));
    }
    catch(Exception e)
    {
      throw new RuntimeException("Invalid URL: " + e.getMessage());
    }
  }

  public static Map<String, String> getQueryParams(String url)
  {
    try
    {
      URI uri = new URI(url);
      String query = uri.getQuery();
      if(query == null || query.isEmpty())
      {
        return Map.of();
      }
      return Stream.of(query.split("&"))
        .map(param -> param.split("="))
        .collect(Collectors.toMap(
          keyValue -> URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
          keyValue -> keyValue.length > 1
            ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
            : ""
        ));
    }
    catch(Exception e)
    {
      throw new RuntimeException("Invalid URL: " + e.getMessage());
    }
  }

  private Tuple3<SFObjectType, String, String> getWebhookType(PluginWebhookHttpReq webhookReq)
  {
    String requestUrl = webhookReq.requestUrl;
    List<String> pathSegments = getPathSegments(requestUrl);
    Map<String, String> queryParams = getQueryParams(requestUrl);
    if(!pathSegments.contains(WEBHOOK_TYPE_DETAIL))
    {
      return null;
    }
    String objectTypeValue = queryParams.get(QUERY_PARAM_OBJECT_TYPE);
    SFObjectType objectType = SFObjectType.getByValue(objectTypeValue);
    if(objectType == null)
    {
      return null;
    }
    String id = queryParams.get(QUERY_PARAM_ID);
    if(id == null)
    {
      return null;
    }
    String username = queryParams.get(QUERY_PARAM_USERNAME);
    if(username == null)
    {
      return null;
    }
    return Tuple3.of(objectType, id, username);
  }

  private IApiDone getOutputForm(ApiCtx apiCtx, PluginWebhookHttpReq webhookReq) throws Throwable
  {
    Tuple3<SFObjectType, String, String> webhookInfo = getWebhookType(webhookReq);
    if(webhookInfo == null)
    {
      return ApiDone.instance;
    }
    SFObjectType objectType = webhookInfo.getV1();
    return getObjectDetail(apiCtx, webhookInfo, objectType);
  }

  private @Nullable IApiDone getObjectDetail(ApiCtx apiCtx,
    Tuple3<SFObjectType, String, String> webhookInfo,
    SFObjectType objectType) throws Throwable
  {
    String objectId = webhookInfo.getV2();
    String username = webhookInfo.getV3();
    return executeWithErrorHandling(() -> switch(objectType)
    {
      case account -> ApiGetAccountDetails.getAccountDetails(apiCtx, username, objectId).block();
      case opportunity -> ApiGetOpportunityDetails.getOpportunityDetail(apiCtx, username, objectId).block();
      case event -> ApiGetEventDetails.getEventDetails(apiCtx, username, objectId).block();
      case task -> ApiGetTaskDetails.getTaskDetails(apiCtx, username, objectId).block();
    }, webhookInfo);
  }

  @FunctionalInterface
  public interface ThrowingSupplier<T, E extends Exception>
  {
    T get() throws E;
  }

  private IApiDone executeWithErrorHandling(ThrowingSupplier<IApiDone, Exception> supplier,
    Tuple3<SFObjectType, String, String> webhookInfo) throws Throwable
  {
    try
    {
      return supplier.get();
    }
    catch(Exception e)
    {
      if(e.getCause() instanceof SObjectNotFoundException)
      {
        SalesforceRecordNotFound salesforceRecordNotFound = new SalesforceRecordNotFound();
        salesforceRecordNotFound.objectType = webhookInfo.getV1().getValue();
        salesforceRecordNotFound.objectId = webhookInfo.getV2();
        return salesforceRecordNotFound;
      }
      else
      {
        throw e.getCause();
      }
    }
  }
}
