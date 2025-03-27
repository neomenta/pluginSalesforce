package neome.plugin.salesforce.api;

import java.util.Optional;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.RecordIdInputForm;
import neome.plugin.salesforce.form.TaskDetail;
import neome.plugin.salesforce.integration.exception.SObjectNotFoundException;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import neome.plugin.salesforce.integration.sdk.objects.SFDescribeDerived;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SFRefField;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.objects.SObjectTask;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import neome.plugin.salesforce.integration.sdk.repositories.TaskRepository;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class ApiGetTaskDetails implements IPluginApi<ApiCtx, RecordIdInputForm, TaskDetail>
{
  @Override
  public void execute(ApiCtx ctx, RecordIdInputForm input, IPluginApiAcceptor<TaskDetail> output)
    throws PluginApiException
  {
    String taskId = input.id;
    Mono<TaskDetail> getTaskDetailMono = ctx.getSalesforceUsername()
      .flatMap(username -> getTaskDetails(ctx, username, taskId));
    ctx.setOutput(output, getTaskDetailMono);
  }

  public static @NotNull Mono<TaskDetail> getTaskDetails(ApiCtx ctx,
    String username,
    String taskId)
  {
    SalesforceFacade salesforceFacade = ctx.pluginCtx.getSalesforceFacade();
    TaskRepository taskRepository = salesforceFacade.taskRepository;
    MetaDataRepository metaDataRepository = salesforceFacade.metaDataRepository;
    return Mono
      .zip(metaDataRepository.describe(username, SFObjectType.task), taskRepository.getById(username, taskId))
      .flatMap(objects -> {
        SFDescribeDerived taskMetaData = objects.getT1();
        Optional<SObject> taskSObject = objects.getT2();
        if(taskSObject.isPresent())
        {
          SObject taskObj = taskSObject.get();
          TaskDetail taskDetail = new TaskDetail();
          taskDetail.id = SObjectPlus.getStringValue(taskObj, SObjectTask.FIELD_ID);
          taskDetail.subject = SObjectPlus.getStringValue(taskObj, SObjectTask.FIELD_SUBJECT);
          taskDetail.dueDate = SObjectPlus.getDateFieldValue(taskObj, SObjectTask.FIELD_DUE_DATE);
          taskDetail.status = SObjectPlus.getPickListFieldValue(taskMetaData, taskObj, SObjectTask.FIELD_STATUS);
          taskDetail.priority = SObjectPlus.getPickListFieldValue(taskMetaData, taskObj, SObjectTask.FIELD_PRIORITY);
          taskDetail.relatedTo =
            SObjectPlus.getRefFieldFieldValueOption(taskObj, SFRefField.what, SObjectTask.FIELD_WHAT);
          taskDetail.assignedTo =
            SObjectPlus.getRefFieldFieldValueOption(taskObj, SFRefField.owner, SObjectTask.FIELD_OWNER_ID);
          taskDetail.description = SObjectPlus.getStringValue(taskObj, SObjectTask.FIELD_DESCRIPTION);
          return Mono.just(taskDetail);
        }
        else
        {
          return Mono.error(new SObjectNotFoundException("Task not found"));
        }
      });
  }
}
