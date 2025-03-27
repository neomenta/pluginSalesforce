package neome.plugin.salesforce.api;

import com.neome.plugin.base.ApiDone;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.CreateEvent;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import neome.plugin.salesforce.integration.sdk.objects.SObjectEvent;
import neome.plugin.salesforce.integration.sdk.repositories.EventRepository;
import reactor.core.publisher.Mono;

public class ApiUpdateEvent implements IPluginApi<ApiCtx, CreateEvent, ApiDone>
{
  @Override
  public void execute(ApiCtx ctx, CreateEvent input, IPluginApiAcceptor<ApiDone> output) throws PluginApiException
  {

    String eventId = input.id;
    SalesforceFacade salesforceFacade = ctx.pluginCtx.getSalesforceFacade();
    EventRepository eventRepository = salesforceFacade.eventRepository;
    Mono<ApiDone> apiDoneMono = ctx.getSalesforceUsername()
      .flatMap(username -> {
        SObject eventSObject = new SObject();
        eventSObject.setType(SObjectEvent.OBJECT_TYPE);
        eventSObject.setId(eventId);
        eventSObject.setSObjectField(SObjectEvent.FIELD_SUBJECT, input.subject);
        eventSObject.setSObjectField(SObjectEvent.FIELD_OWNER_ID,
          input.assignedTo != null
            ? input.assignedTo.optionId
            : null);
        eventSObject.setSObjectField(SObjectEvent.FIELD_IS_ALL_DAY_EVENT,
          input.isAllDayEvent != null
            ? input.isAllDayEvent.value
            : Boolean.FALSE);
        eventSObject.setSObjectField(SObjectEvent.FIELD_START_DATE_TIME, input.startDate);
        eventSObject.setSObjectField(SObjectEvent.FIELD_END_DATE_TIME, input.endDate);
        eventSObject.setSObjectField(SObjectEvent.FIELD_DESCRIPTION, input.description);
        eventSObject.setSObjectField(SObjectEvent.FIELD_LOCATION, input.location);
        eventSObject.setSObjectField(SObjectEvent.FIELD_WHAT_ID,
          input.relatedTo != null
            ? input.relatedTo.optionId
            : null);
        return eventRepository.update(username, eventId, eventSObject)
          .map(createdRecordId -> ApiDone.instance);
      });
    ctx.setOutput(output, apiDoneMono);
  }
}
