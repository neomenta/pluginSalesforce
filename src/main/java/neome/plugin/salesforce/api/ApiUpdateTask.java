package neome.plugin.salesforce.api;

import com.neome.plugin.base.ApiDone;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.CreateTask;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import neome.plugin.salesforce.integration.sdk.objects.SObjectTask;
import neome.plugin.salesforce.integration.sdk.repositories.TaskRepository;
import reactor.core.publisher.Mono;

public class ApiUpdateTask implements IPluginApi<ApiCtx, CreateTask, ApiDone>
{
  @Override
  public void execute(ApiCtx ctx, CreateTask input, IPluginApiAcceptor<ApiDone> output) throws PluginApiException
  {

    String taskId = input.id;
    SalesforceFacade salesforceFacade = ctx.pluginCtx.getSalesforceFacade();
    TaskRepository taskRepository = salesforceFacade.taskRepository;
    Mono<ApiDone> apiDoneMono = ctx.getSalesforceUsername()
      .flatMap(username -> {
        SObject taskSObject = new SObject();
        taskSObject.setType(SObjectTask.OBJECT_TYPE);
        taskSObject.setId(taskId);
        taskSObject.setSObjectField(SObjectTask.FIELD_SUBJECT, input.subject);
        taskSObject.setSObjectField(SObjectTask.FIELD_DUE_DATE, input.dueDate);
        taskSObject.setSObjectField(SObjectTask.FIELD_STATUS,
          input.status != null
            ? input.status.optionId
            : null);
        taskSObject.setSObjectField(SObjectTask.FIELD_PRIORITY,
          input.priority != null
            ? input.priority.optionId
            : null);
        taskSObject.setSObjectField(SObjectTask.FIELD_OWNER_ID,
          input.assignedTo != null
            ? input.assignedTo.optionId
            : null);
        taskSObject.setSObjectField(SObjectTask.FIELD_WHAT,
          input.relatedTo != null
            ? input.relatedTo.optionId
            : null);
        taskSObject.setSObjectField(SObjectTask.FIELD_DESCRIPTION, input.description);
        return taskRepository.update(username, taskId, taskSObject)
          .map(createdRecordId -> ApiDone.instance);
      });
    ctx.setOutput(output, apiDoneMono);
  }
}
