package neome.plugin.salesforce.api;

import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.CreateEvent;
import neome.plugin.salesforce.form.CreatedRecordId;
import neome.plugin.salesforce.integration.sdk.objects.SObjectEvent;
import neome.plugin.salesforce.integration.sdk.repositories.EventRepository;
import reactor.core.publisher.Mono;

public class ApiCreateEvent implements IPluginApi<ApiCtx, CreateEvent, CreatedRecordId>
{
  @Override
  public void execute(ApiCtx ctx, CreateEvent input, IPluginApiAcceptor<CreatedRecordId> output)
  {
    EventRepository eventRepository = ctx.getSalesforceFacade().eventRepository;
    Mono<CreatedRecordId> apiDoneMono = ctx.getSalesforceUsername()
      .flatMap(salesforceUsername -> {
        SObject sObject = new SObject();
        sObject.setType(SObjectEvent.OBJECT_TYPE);
        sObject.setSObjectField(SObjectEvent.FIELD_SUBJECT, input.subject);
        sObject.setSObjectField(SObjectEvent.FIELD_LOCATION, input.location);
        sObject.setSObjectField(SObjectEvent.FIELD_START_DATE_TIME, input.startDate);
        sObject.setSObjectField(SObjectEvent.FIELD_END_DATE_TIME, input.endDate);
        sObject.setSObjectField(SObjectEvent.FIELD_DESCRIPTION, input.description);
        sObject.setSObjectField(SObjectEvent.FIELD_IS_ALL_DAY_EVENT, input.isAllDayEvent);
        sObject.setSObjectField(SObjectEvent.FIELD_WHAT_ID, input.relatedTo != null
          ? input.relatedTo.optionId
          : null);
        sObject.setSObjectField(SObjectEvent.FIELD_OWNER_ID, input.assignedTo != null
          ? input.assignedTo.optionId
          : null);
        return eventRepository.createEvent(salesforceUsername, sObject);
      }).map(id -> {
        CreatedRecordId createdRecordId = new CreatedRecordId();
        createdRecordId.id = id;
        return createdRecordId;
      });

    ctx.setOutput(output, apiDoneMono);
  }
}
