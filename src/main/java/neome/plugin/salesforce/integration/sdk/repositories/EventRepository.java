package neome.plugin.salesforce.integration.sdk.repositories;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.integration.sdk.WSCInterface;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SObjectEvent;
import neome.plugin.salesforce.integration.sdk.objects.SObjectTask;
import neome.plugin.salesforce.integration.sdk.query.SFQueryBuilder;
import neome.plugin.salesforce.integration.sdk.query.SFWhere;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EventRepository
{
  private final WSCInterface wscInterface;

  public EventRepository(WSCInterface wscInterface)
  {
    this.wscInterface = wscInterface;
  }

  public Mono<String> createEvent(String username, SObject sObject)
  {
    return wscInterface.createRecord(username, sObject);
  }

  public Mono<String> update(String username, String eventId, SObject sObject)
  {
    return wscInterface.updateRecord(username, eventId, sObject);
  }

  public Mono<Optional<SObject>> getById(String username, String eventId)
  {
    Set<String> refFieldIdSet = new HashSet<>();
    refFieldIdSet.add(SObjectTask.FIELD_OWNER_NAME);
    refFieldIdSet.add(SObjectTask.FIELD_WHO_NAME);
    refFieldIdSet.add(SObjectTask.FIELD_WHAT_NAME);
    return wscInterface.getRecordById(username, SFObjectType.event, refFieldIdSet, eventId);
  }

  public Flux<List<SObject>> getByOpportunityId(String username, String opportunityId)
  {
    SFQueryBuilder queryBuilder = new SFQueryBuilder()
      .addColumn(SObjectEvent.FIELD_ID)
      .addColumn(SObjectEvent.FIELD_SUBJECT)
      .addColumn(SObjectEvent.FIELD_START_DATE_TIME)
      .addColumn(SObjectEvent.FIELD_END_DATE_TIME)
      .addColumn(SObjectEvent.FIELD_DESCRIPTION)
      .addColumn(SObjectEvent.FIELD_LOCATION)
      .addColumn(SObjectEvent.FIELD_IS_ALL_DAY_EVENT)
      .addColumn(SObjectEvent.FIELD_WHAT_ID)
      .addColumn(SObjectEvent.FIELD_WHAT_NAME)
      .addColumn(SObjectEvent.FIELD_OWNER_ID)
      .addColumn(SObjectEvent.FIELD_OWNER_NAME)
      .from(SObjectEvent.OBJECT_TYPE)
      .where(List.of(new SFWhere(SObjectEvent.FIELD_WHAT_ID, opportunityId)), true);
    queryBuilder.orderBy(SObjectEvent.FIELD_CREATED_DATE);
    queryBuilder.limit(WSCInterface.LIMIT);
    return wscInterface.getRecords(username, queryBuilder.build());
  }
}