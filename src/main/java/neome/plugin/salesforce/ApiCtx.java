package neome.plugin.salesforce;

import java.util.Map;
import java.util.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neome.api.meta.base.Types;
import com.neome.api.studio.base.dto.DtoEntUserSettingValue;
import com.neome.api.studio.studioMain.RpcStudioMain;
import com.neome.api.studio.studioMain.msg.MsgEntUserGet;
import com.neome.api.studio.studioMain.sig.SigEntUser;
import com.neome.plugin.base.AgentApiCtx;
import com.neome.plugin.base.IApiDone;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.neome.util.AppLog;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import reactor.core.publisher.Mono;

public class ApiCtx extends AgentApiCtx
{
  private final AppLog LOG = new AppLog(getClass());

  public final PluginCtx pluginCtx;

  public ApiCtx(PluginCtx pluginCtx)
  {
    super(pluginCtx);
    this.pluginCtx = pluginCtx;
  }

  private Mono<SigEntUser> getUser(Types.EntId entId, Types.EntUserId entUserId)
  {
    return Mono.create(monoSink -> {
      MsgEntUserGet msgEntUserGet = new MsgEntUserGet();
      msgEntUserGet.entId = entId;
      msgEntUserGet.entUserId = entUserId;
      RpcStudioMain.entUserGet(msgEntUserGet, envSignal -> {
        if(envSignal.error == null)
        {
          SigEntUser sig = envSignal.sig;
          monoSink.success(sig);
        }
        else
        {
          monoSink.error(new PluginApiException(envSignal.error.errorMessage, envSignal.error.errorParams));
        }
      });
    });
  }

  @SuppressWarnings("unchecked")
  public Mono<String> getSalesforceUsername()
  {
    String usernameVariableName = pluginCtx.getSalesforceFacade().salesForceAPIConfig.usernameVariableName;
    return getUser(entUserIdTriple.entId, entUserIdTriple.entUserId).flatMap(sigEntUser -> {
      Map<Types.MetaIdVar, DtoEntUserSettingValue> userSettingValueMap = sigEntUser.user.userSettingValueMap;
      if(userSettingValueMap != null)
      {
        Optional<DtoEntUserSettingValue> salesforceUserSetting =
          userSettingValueMap.values().stream().filter(dtoVarUserSetting -> {
            assert dtoVarUserSetting.name != null;
            return dtoVarUserSetting.name.name.equals(usernameVariableName);
          }).findFirst();
        if(salesforceUserSetting.isPresent())
        {
          DtoEntUserSettingValue dtoEntUserSettingValue = salesforceUserSetting.get();
          JsonElement jsonElement = dtoEntUserSettingValue.value;
          if(jsonElement != null)
          {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            if(asJsonObject != null)
            {
              JsonElement salesforceUsernameJsonElement = asJsonObject.get("value");
              if(salesforceUsernameJsonElement != null)
              {
                String salesforceUsername = salesforceUsernameJsonElement.getAsString();
                if(!salesforceUsername.isEmpty())
                {
                  return Mono.just(salesforceUsername);
                }
              }
            }
          }
          return Mono.error(new PluginApiException("Please set your %s", usernameVariableName));
        }
      }
      return Mono.error(new PluginApiException("User setting variable with name '%s' required", usernameVariableName));
    });
  }

  public <T extends IApiDone> void setOutput(IPluginApiAcceptor<T> outputAcceptor, Mono<T> mono)
  {
    mono.subscribe(outputAcceptor::success, throwable -> {
      if(throwable instanceof PluginApiException)
      {
        outputAcceptor.error((PluginApiException) throwable);
      }
      else
      {
        LOG.debug("Throwable is not plugin exception type");
        outputAcceptor.error(new PluginApiException(throwable, throwable.getMessage()));
      }
    });
  }

  public SalesforceFacade getSalesforceFacade()
  {
    return pluginCtx.getSalesforceFacade();
  }
}
