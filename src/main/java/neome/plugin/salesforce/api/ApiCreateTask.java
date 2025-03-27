package neome.plugin.salesforce.api;

import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.util.AppLog;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.CreateTask;
import neome.plugin.salesforce.form.CreatedRecordId;
import neome.plugin.salesforce.integration.sdk.objects.SObjectTask;
import neome.plugin.salesforce.integration.sdk.repositories.TaskRepository;
import reactor.core.publisher.Mono;

public class ApiCreateTask implements IPluginApi<ApiCtx, CreateTask, CreatedRecordId>
{
  @Override
  public void execute(ApiCtx ctx, CreateTask input, IPluginApiAcceptor<CreatedRecordId> output)
  {
    TaskRepository taskRepository = ctx.getSalesforceFacade().taskRepository;
    Mono<CreatedRecordId> apiDoneMono = ctx.getSalesforceUsername()
      .flatMap(salesforceUsername -> {
        SObject sObject = new SObject();
        sObject.setType(SObjectTask.OBJECT_TYPE);
        sObject.setSObjectField(SObjectTask.FIELD_SUBJECT, input.subject);
        sObject.setSObjectField(SObjectTask.FIELD_STATUS,
          input.status != null
            ? input.status.optionId
            : null);
        sObject.setSObjectField(SObjectTask.FIELD_PRIORITY,
          input.priority != null
            ? input.priority.optionId
            : null);
        sObject.setSObjectField(SObjectTask.FIELD_DUE_DATE, input.dueDate);
        sObject.setSObjectField(SObjectTask.FIELD_DESCRIPTION, input.description);
        sObject.setSObjectField(SObjectTask.FIELD_WHAT,
          input.relatedTo != null
            ? input.relatedTo.optionId
            : null);
        sObject.setSObjectField(SObjectTask.FIELD_OWNER_ID,
          input.assignedTo != null
            ? input.assignedTo.optionId
            : null);
        return taskRepository.create(salesforceUsername, sObject);
      })
      .map(id -> {
        CreatedRecordId createdRecordId = new CreatedRecordId();
        createdRecordId.id = id;
        return createdRecordId;
      });
    ctx.setOutput(output, apiDoneMono);
  }
}
