package neome.plugin.salesforce.api;

import java.util.List;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.RecordIdInputForm;
import neome.plugin.salesforce.form.TaskList;
import neome.plugin.salesforce.integration.sdk.objects.SFDescribeDerived;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SFRefField;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.objects.SObjectTask;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import neome.plugin.salesforce.integration.sdk.repositories.TaskRepository;
import neome.plugin.salesforce.integration.utils.FormUtils;
import reactor.core.publisher.Mono;

public class ApiGetTasksByOpportunity implements IPluginApi<ApiCtx, RecordIdInputForm, TaskList>
{
  @Override
  public void execute(ApiCtx ctx, RecordIdInputForm input, IPluginApiAcceptor<TaskList> output)
  {
    TaskRepository taskRepository = ctx.getSalesforceFacade().taskRepository;
    MetaDataRepository metaDataRepository = ctx.getSalesforceFacade().metaDataRepository;
    Mono<TaskList> taskListMono = ctx.getSalesforceUsername()
      .flatMap(salesforceUsername -> {
        return metaDataRepository.describe(salesforceUsername, SFObjectType.task)
          .flatMap(taskDescribeObj -> getTaskList(input, salesforceUsername, taskDescribeObj, taskRepository));
      });
    ctx.setOutput(output, taskListMono);
  }

  private Mono<TaskList> getTaskList(RecordIdInputForm input,
    String salesforceUsername,
    SFDescribeDerived taskDescribeObj,
    TaskRepository taskRepository)
  {
    Mono<List<SObject>> listMono = taskRepository
      .getByOpportunityId(salesforceUsername, input.id)
      .flatMapIterable(f -> f)
      .collectList();
    return listMono.map(sObjectList -> {
      TaskList taskList = new TaskList();
      taskList.task = FormUtils.getGridRowList(sObjectList.toArray(new SObject[0]), sObj -> {
        TaskList.Task resultTask = new TaskList.Task();
        resultTask.id = sObj.getId();
        resultTask.subject = SObjectPlus.getStringValue(sObj, SObjectTask.FIELD_SUBJECT);
        resultTask.dueDate = SObjectPlus.getDateFieldValue(sObj, SObjectTask.FIELD_DUE_DATE);
        resultTask.status = SObjectPlus.getPickListFieldValue(taskDescribeObj, sObj, SObjectTask.FIELD_STATUS);
        resultTask.relatedTo =
          SObjectPlus.getRefFieldFieldValueOption(sObj, SFRefField.what, SObjectTask.FIELD_WHAT);
        resultTask.priority = SObjectPlus.getPickListFieldValue(taskDescribeObj, sObj, SObjectTask.FIELD_PRIORITY);
        resultTask.description = SObjectPlus.getStringValue(sObj, SObjectTask.FIELD_DESCRIPTION);
        SObjectPlus.getRefFieldFieldValueOption(sObj, SFRefField.who, SObjectTask.FIELD_WHO);
        resultTask.assignedTo =
          SObjectPlus.getRefFieldFieldValueOption(sObj, SFRefField.owner, SObjectTask.FIELD_OWNER_ID);
        return resultTask;
      });
      return taskList;
    });
  }
}
