package neome.plugin.salesforce.integration.sdk;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.integration.SalesForceAPIConfig;
import neome.plugin.salesforce.integration.sdk.objects.SFDescribeDerived;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.query.SFQueryBuilder;
import neome.plugin.salesforce.integration.sdk.query.SFWhere;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

public class WSCInterface
{
  private final Cache<SFObjectType, SFDescribeDerived> sObjDescribeDerivedCache;

  public static final int LIMIT = 2000;

  private final SFConnectionManager connManager;

  public WSCInterface(SalesForceAPIConfig salesForceAPIConfig)
  {
    this.connManager = new SFConnectionManager(salesForceAPIConfig);
    this.sObjDescribeDerivedCache = CacheBuilder.newBuilder()
      .expireAfterWrite(30, TimeUnit.MINUTES)
      .maximumSize(1000)
      .build();
  }

  public Mono<SFDescribeDerived> describe(String username, SFObjectType objectType)
  {
    return Mono.create(monoSink -> {
      try
      {
        SFDescribeDerived describeSObjectResult = sObjDescribeDerivedCache.get(objectType,
          () -> {
            DescribeSObjectResult describeObj = connManager.executeWithRetry(username,
              connection -> connection.describeSObject(objectType.getValue()));
            return new SFDescribeDerived(describeObj);
          });
        monoSink.success(describeSObjectResult);
      }
      catch(Exception e)
      {
        monoSink.error(e);
      }
    });
  }

  public Mono<Optional<SObject>> getRecordById(String username,
    SFObjectType objectType,
    Set<String> referenceFieldSet,
    String id)
  {
    return describe(username, objectType)
      .flatMap(describeSObjectResult -> {
        Field[] fields = describeSObjectResult.getFieldMap().values().toArray(new Field[0]);
        Set<String> fieldSet = Arrays.stream(fields)
          .map(Field::getName)
          .collect(Collectors.toSet());
        SFQueryBuilder queryBuilder = new SFQueryBuilder();
        for(String fieldName : fieldSet)
        {
          queryBuilder.addColumn(fieldName);
        }
        if(referenceFieldSet != null)
        {
          for(String referenceFieldName : referenceFieldSet)
          {
            queryBuilder.addColumn(referenceFieldName);
          }
        }
        queryBuilder
          .from(objectType.getValue())
          .where(List.of(new SFWhere("Id", id)), true);
        return getRecords(username, queryBuilder.build())
          .flatMapIterable(sObjects -> sObjects)
          .collectList()
          .map(listOfSObject -> {
            if(!listOfSObject.isEmpty())
            {
              SObject value = listOfSObject.get(0);
              return Optional.of(value);
            }
            return Optional.empty();
          });
      });
  }

  public Mono<GetUserInfoResult> getLoggedInUserInfo(String username)
  {
    return Mono.create(sink -> {
      try
      {
        GetUserInfoResult getUserInfoResult = connManager.executeWithRetry(username, PartnerConnection::getUserInfo);
        sink.success(getUserInfoResult);
      }
      catch(Exception e)
      {
        sink.error(e);
      }
    });
  }

  public Flux<List<SObject>> getRecords(String username, String query)
  {
    return Flux.create(sink -> {
      QueryResult queryResult = connManager.executeWithRetry(username, connection -> connection.query(query));
      sink.next(List.of(queryResult.getRecords()));
      while(!queryResult.isDone())
      {
        QueryResult finalQueryResult = queryResult;
        queryResult = connManager.executeWithRetry(username,
          connection -> connection.queryMore(finalQueryResult.getQueryLocator()));
        sink.next(List.of(queryResult.getRecords()));
      }
      sink.complete();
    });
  }

  public Mono<String> createRecord(String username, SObject sObject)
  {
    return Mono.create(monoSink -> {
      SaveResult[] saveResults =
        connManager.executeWithRetry(username, connection -> connection.create(new SObject[] { sObject }));
      processSaveResults(monoSink, saveResults);
    });
  }

  public Mono<String> deleteRecords(String username, String id)
  {
    return deleteRecords(username, new String[] { id });
  }

  public Mono<String> deleteRecords(String username, String[] ids)
  {
    return Mono.create(monoSink -> {
      DeleteResult[] deleteResults = connManager.executeWithRetry(username, connection -> connection.delete(ids));
      processDeleteResults(monoSink, deleteResults);
    });
  }

  public Mono<String> updateRecord(String username, String id, SObject updatedObj)
  {
    return Mono.create(monoSink -> {
      updatedObj.setId(id);
      SaveResult[] saveResults =
        connManager.executeWithRetry(username, connection -> connection.update(new SObject[] { updatedObj }));
      processSaveResults(monoSink, saveResults);
    });
  }

  private void processDeleteResults(MonoSink<String> sink, DeleteResult[] deleteResults)
  {
    if(deleteResults[0].isSuccess())
    {
      System.out.println("Id: " + deleteResults[0].getId());
      sink.success(deleteResults[0].getId());
    }
    else
    {
      //sink.error(new RuntimeException(deleteResults[0].getErrors()[0].getMessage()));
      sink.success("");
    }
  }

  private void processSaveResults(MonoSink<String> sink, SaveResult[] saveResults)
  {
    if(saveResults != null)
    {
      if(saveResults.length > 0)
      {
        if(saveResults[0].isSuccess())
        {
          System.out.println("ID: " + saveResults[0].getId());
          sink.success(saveResults[0].getId());
        }
        else
        {
          sink.error(new RuntimeException(saveResults[0].getErrors()[0].getMessage()));
        }
      }
    }
    sink.error(new RuntimeException("Failed to save record"));
  }

  public Mono<PicklistEntry[]> getPickList(String username, SFObjectType objectType, String fieldName)
  {
    return Mono.create(sink -> {

      PicklistEntry[] picklistEntries = connManager.executeWithRetry(username, connection -> {
        DescribeSObjectResult describeSObjectResult = connection.describeSObject(objectType.getValue());
        for(Field field : describeSObjectResult.getFields())
        {
          if(field.getName().equals(fieldName))
          {
            return field.getPicklistValues();
          }
        }
        return new PicklistEntry[0];
      });
      sink.success(picklistEntries);
    });
  }
}
