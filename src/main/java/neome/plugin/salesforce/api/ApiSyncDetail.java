package neome.plugin.salesforce.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.neome.api.meta.base.Types;
import com.neome.plugin.base.ApiDone;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.WebhookExecutor;
import neome.plugin.salesforce.form.SyncDetailInputForm;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import reactor.core.publisher.Mono;

public class ApiSyncDetail implements IPluginApi<ApiCtx, SyncDetailInputForm, ApiDone>
{
  @Override
  public void execute(ApiCtx ctx, SyncDetailInputForm input, IPluginApiAcceptor<ApiDone> output)
  {
    String id = input.id;
    String objectTypeString = input.objectType;
    SFObjectType sfObjectType = SFObjectType.getByValue(objectTypeString);
    if(sfObjectType == null)
    {
      output.error(new PluginApiException("Invalid object type provided"));
      return;
    }
    String entId = ctx.entUserIdTriple.entId.getId();
    Types.MetaIdPlugin pluginId = ctx.pluginCtx.pluginId;
    Mono<ApiDone> syncDetails = ctx.getSalesforceUsername()
      .flatMap(username -> {
        String url =
          "http://localhost:11001/global/hook/%s/%s/%s?%s=%s&%s=%s&%s=%s".formatted(
            entId,
            pluginId.getId(),
            WebhookExecutor.WEBHOOK_TYPE_DETAIL,
            WebhookExecutor.QUERY_PARAM_OBJECT_TYPE,
            sfObjectType.getValue(),
            WebhookExecutor.QUERY_PARAM_USERNAME,
            username,
            WebhookExecutor.QUERY_PARAM_ID,
            id);
        return sendGetRequest(url);
      });
    ctx.setOutput(output, syncDetails);
  }

  public static Mono<ApiDone> sendGetRequest(String url)
  {
    return Mono.create(sink -> {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Accept", "application/json")
        .GET()
        .build();
      client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenAccept(response -> {
          if(response.statusCode() < 200 || response.statusCode() >= 300)
          {
            sink.error(new PluginApiException("Failed to execute sync detail"));
            return;
          }
          sink.success(ApiDone.instance);
        })
        .exceptionally(ex -> {
          sink.error(new PluginApiException("Failed to execute sync detail api %s".formatted(ex.getMessage())));
          return null;
        });
    });
  }
}
