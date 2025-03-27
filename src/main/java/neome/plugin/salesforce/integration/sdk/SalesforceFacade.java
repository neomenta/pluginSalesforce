package neome.plugin.salesforce.integration.sdk;

import neome.plugin.salesforce.integration.SalesForceAPIConfig;
import neome.plugin.salesforce.integration.sdk.repositories.AccountRepository;
import neome.plugin.salesforce.integration.sdk.repositories.EventRepository;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import neome.plugin.salesforce.integration.sdk.repositories.OpportunityRepository;
import neome.plugin.salesforce.integration.sdk.repositories.TaskRepository;
import neome.plugin.salesforce.integration.sdk.repositories.UserRepository;

public class SalesforceFacade
{

  public final SalesForceAPIConfig salesForceAPIConfig;

  public final AccountRepository accountRepository;

  public final OpportunityRepository opportunityRepository;

  public final MetaDataRepository metaDataRepository;

  public final UserRepository userRepository;

  public final TaskRepository taskRepository;

  public final EventRepository eventRepository;

  public final WSCInterface wscInterface;

  public SalesforceFacade(SalesForceAPIConfig salesForceAPIConfig)
  {
    this.wscInterface = getWSDLInterface(salesForceAPIConfig);
    this.salesForceAPIConfig = salesForceAPIConfig;
    this.metaDataRepository = new MetaDataRepository(wscInterface);
    this.accountRepository = new AccountRepository(wscInterface);
    this.opportunityRepository = new OpportunityRepository(wscInterface);
    this.userRepository = new UserRepository(wscInterface);
    this.taskRepository = new TaskRepository(wscInterface);
    this.eventRepository = new EventRepository(wscInterface);
  }

  private WSCInterface getWSDLInterface(SalesForceAPIConfig salesForceAPIConfig)
  {
    return new WSCInterface(salesForceAPIConfig);
  }
}
