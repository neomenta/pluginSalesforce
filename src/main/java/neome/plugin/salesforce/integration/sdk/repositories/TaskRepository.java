package neome.plugin.salesforce.integration.sdk.repositories;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.integration.sdk.WSCInterface;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SObjectTask;
import neome.plugin.salesforce.integration.sdk.query.SFQueryBuilder;
import neome.plugin.salesforce.integration.sdk.query.SFWhere;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TaskRepository
{
  private final WSCInterface wscInterface;

  public TaskRepository(WSCInterface wscInterface)
  {
    this.wscInterface = wscInterface;
  }

  public Mono<String> create(String username, SObject sObject)
  {
    return wscInterface.createRecord(username, sObject);
  }

  public Mono<String> update(String username, String taskId, SObject sObject)
  {
    return wscInterface.updateRecord(username, taskId, sObject);
  }

  public Mono<Optional<SObject>> getById(String username, String taskId)
  {
    Set<String> refFieldIdSet = new HashSet<>();
    refFieldIdSet.add(SObjectTask.FIELD_OWNER_NAME);
    refFieldIdSet.add(SObjectTask.FIELD_WHO_NAME);
    refFieldIdSet.add(SObjectTask.FIELD_WHAT_NAME);
    return wscInterface.getRecordById(username, SFObjectType.task, refFieldIdSet, taskId);
  }

  public Flux<List<SObject>> getByOpportunityId(String username, String opportunityId)
  {
    SFQueryBuilder queryBuilder = new SFQueryBuilder()
      .addColumn(SObjectTask.FIELD_ID)
      .addColumn(SObjectTask.FIELD_SUBJECT)
      .addColumn(SObjectTask.FIELD_DESCRIPTION)
      .addColumn(SObjectTask.FIELD_DUE_DATE)
      .addColumn(SObjectTask.FIELD_SUBJECT)
      .addColumn(SObjectTask.FIELD_STATUS)
      .addColumn(SObjectTask.FIELD_PRIORITY)
      .addColumn(SObjectTask.FIELD_OWNER_ID)
      .addColumn(SObjectTask.FIELD_OWNER_NAME)
      .addColumn(SObjectTask.FIELD_WHAT)
      .addColumn(SObjectTask.FIELD_WHAT_NAME)
      .addColumn(SObjectTask.FIELD_WHO)
      .addColumn(SObjectTask.FIELD_WHO_NAME)
      .from(SObjectTask.OBJECT_TYPE)
      .where(List.of(new SFWhere(SObjectTask.FIELD_WHAT, opportunityId)), true);
    queryBuilder.orderBy(SObjectTask.FIELD_CREATED_DATE);
    queryBuilder.limit(WSCInterface.LIMIT);

    String query = queryBuilder.build();
    return wscInterface.getRecords(username, query);
  }
}