package neome.plugin.salesforce.integration.sdk.repositories;

import java.util.List;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.integration.sdk.WSCInterface;
import neome.plugin.salesforce.integration.sdk.objects.SObjectUser;
import neome.plugin.salesforce.integration.sdk.query.SFQueryBuilder;
import neome.plugin.salesforce.integration.sdk.query.SFWhere;
import reactor.core.publisher.Flux;

public class UserRepository
{
  private final static String STANDARD_USER_TYPE = "Standard";

  private final WSCInterface wsdlInterface;

  public UserRepository(WSCInterface wscInterface)
  {
    this.wsdlInterface = wscInterface;
  }

  public Flux<List<SObject>> getStandardUsers(String username)
  {
    SFQueryBuilder queryBuilder = new SFQueryBuilder()
      .addColumn(SObjectUser.FIELD_ID)
      .addColumn(SObjectUser.FIELD_NAME)
      .addColumn(SObjectUser.FIELD_USER_TYPE)
      .from(SObjectUser.OBJECT_TYPE)
      .where(List.of(new SFWhere(SObjectUser.FIELD_USER_TYPE, STANDARD_USER_TYPE)), true);
    queryBuilder.orderBy(SObjectUser.FIELD_CREATED_DATE);
    queryBuilder.limit(WSCInterface.LIMIT);
    String query = queryBuilder.build();
    return wsdlInterface.getRecords(username, query);
  }
}