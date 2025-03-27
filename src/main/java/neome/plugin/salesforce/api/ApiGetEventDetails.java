package neome.plugin.salesforce.api;

import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.EventDetail;
import neome.plugin.salesforce.form.RecordIdInputForm;
import neome.plugin.salesforce.integration.exception.SObjectNotFoundException;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import neome.plugin.salesforce.integration.sdk.objects.SFRefField;
import neome.plugin.salesforce.integration.sdk.objects.SObjectEvent;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.repositories.EventRepository;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class ApiGetEventDetails implements IPluginApi<ApiCtx, RecordIdInputForm, EventDetail>
{
  @Override
  public void execute(ApiCtx ctx, RecordIdInputForm input, IPluginApiAcceptor<EventDetail> output)
    throws PluginApiException
  {
    String eventId = input.id;
    Mono<EventDetail> getEventDetail = ctx.getSalesforceUsername()
      .flatMap(username -> getEventDetails(ctx, username, eventId));
    ctx.setOutput(output, getEventDetail);
  }

  public static @NotNull Mono<EventDetail> getEventDetails(ApiCtx ctx, String username, String taskId)
  {
    SalesforceFacade salesforceFacade = ctx.pluginCtx.getSalesforceFacade();
    EventRepository eventRepository = salesforceFacade.eventRepository;
    return eventRepository.getById(username, taskId)
      .flatMap(optionalEventObj -> {
        if(optionalEventObj.isPresent())
        {
          SObject eventObj = optionalEventObj.get();
          EventDetail eventDetail = new EventDetail();
          eventDetail.id = SObjectPlus.getStringValue(eventObj, SObjectEvent.FIELD_ID);
          eventDetail.subject = SObjectPlus.getStringValue(eventObj, SObjectEvent.FIELD_SUBJECT);
          eventDetail.relatedTo =
            SObjectPlus.getRefFieldFieldValueOption(eventObj, SFRefField.what, SObjectEvent.FIELD_WHAT_ID);
          eventDetail.isAllDayEvent = SObjectPlus.getFieldValueSwitch(eventObj, SObjectEvent.FIELD_IS_ALL_DAY_EVENT);
          eventDetail.startDateTime = SObjectPlus.getDateFieldValue(eventObj, SObjectEvent.FIELD_START_DATE_TIME);
          eventDetail.endDateTime = SObjectPlus.getDateFieldValue(eventObj, SObjectEvent.FIELD_END_DATE_TIME);
          eventDetail.assignedTo =
            SObjectPlus.getRefFieldFieldValueOption(eventObj, SFRefField.owner, SObjectEvent.FIELD_OWNER_ID);
          eventDetail.location = SObjectPlus.getStringValue(eventObj, SObjectEvent.FIELD_LOCATION);
          eventDetail.description = SObjectPlus.getStringValue(eventObj, SObjectEvent.FIELD_DESCRIPTION);
          return Mono.just(eventDetail);
        }
        else
        {
          return Mono.error(new SObjectNotFoundException("Event not found"));
        }
      });
  }
}
