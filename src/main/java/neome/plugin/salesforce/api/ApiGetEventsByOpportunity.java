package neome.plugin.salesforce.api;

import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.EventList;
import neome.plugin.salesforce.form.RecordIdInputForm;
import neome.plugin.salesforce.integration.sdk.objects.SFRefField;
import neome.plugin.salesforce.integration.sdk.objects.SObjectEvent;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.repositories.EventRepository;
import neome.plugin.salesforce.integration.utils.FormUtils;
import reactor.core.publisher.Mono;

public class ApiGetEventsByOpportunity implements IPluginApi<ApiCtx, RecordIdInputForm, EventList>
{
  @Override
  public void execute(ApiCtx ctx, RecordIdInputForm input, IPluginApiAcceptor<EventList> output)
  {
    Mono<EventList> eventListMono =
      ctx.getSalesforceUsername().flatMap(salesforceUsername -> {
        EventRepository eventRepository = ctx.getSalesforceFacade().eventRepository;
        return eventRepository
          .getByOpportunityId(salesforceUsername, input.id)
          .flatMapIterable(sObjects -> sObjects)
          .collectList()
          .map(eventSObjectList -> {
            EventList eventList = new EventList();
            eventList.event = FormUtils.getGridRowList(eventSObjectList.toArray(new SObject[0]), eventSObject -> {
              EventList.Event resultEvent = new EventList.Event();
              resultEvent.id = eventSObject.getId();
              resultEvent.subject = SObjectPlus.getStringValue(eventSObject, SObjectEvent.FIELD_SUBJECT);
              resultEvent.description = SObjectPlus.getStringValue(eventSObject, SObjectEvent.FIELD_DESCRIPTION);
              resultEvent.startDate = SObjectPlus.getDateFieldValue(eventSObject, SObjectEvent.FIELD_START_DATE_TIME);
              resultEvent.endDate = SObjectPlus.getDateFieldValue(eventSObject, SObjectEvent.FIELD_END_DATE_TIME);
              resultEvent.location = SObjectPlus.getStringValue(eventSObject, SObjectEvent.FIELD_LOCATION);
              resultEvent.isAllDayEvent =
                SObjectPlus.getFieldValueSwitch(eventSObject, SObjectEvent.FIELD_IS_ALL_DAY_EVENT);
              resultEvent.assignedTo =
                SObjectPlus.getRefFieldFieldValueOption(eventSObject, SFRefField.owner, SObjectEvent.FIELD_OWNER_ID);
              resultEvent.relatedTo =
                SObjectPlus.getRefFieldFieldValueOption(eventSObject, SFRefField.what, SObjectEvent.FIELD_WHAT_ID);
              return resultEvent;
            });
            return eventList;
          });
      });
    ctx.setOutput(output, eventListMono);
  }
}
