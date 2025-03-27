package neome.plugin.salesforce.integration.sdk.repositories;

import java.util.List;
import com.neome.api.meta.base.dto.StudioDtoOption;
import com.neome.plugin.base.forms.SysFormMapOfOptions;
import com.sforce.soap.partner.GetUserInfoResult;
import neome.plugin.salesforce.integration.sdk.WSCInterface;
import neome.plugin.salesforce.integration.sdk.objects.SFDescribeDerived;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.utils.FormUtils;
import reactor.core.publisher.Mono;

public class MetaDataRepository
{
  private final WSCInterface wscInterface;

  public MetaDataRepository(WSCInterface wsdlInterface)
  {
    this.wscInterface = wsdlInterface;
  }

  public Mono<SFDescribeDerived> describe(String username, SFObjectType objectType)
  {
    return wscInterface.describe(username, objectType);
  }

  public Mono<GetUserInfoResult> getLoggedInUser(String username)
  {
    return wscInterface.getLoggedInUserInfo(username);
  }

  public Mono<SysFormMapOfOptions> getTypeList(String username, SFObjectType objectType, String fieldName)
  {
    return wscInterface.getPickList(username, objectType, fieldName)
      .map(picklistEntries -> FormUtils.getSysFormMapOfOptions(List.of(picklistEntries), (pickListValue -> {
        StudioDtoOption dtoOption = new StudioDtoOption();
        dtoOption.metaId = pickListValue.getValue();
        dtoOption.value = pickListValue.getLabel();
        return dtoOption;
      })));
  }
}