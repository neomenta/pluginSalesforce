package neome.plugin.salesforce.api;

import java.util.List;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.OpportunityList;
import neome.plugin.salesforce.form.RecordIdInputForm;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SFRefField;
import neome.plugin.salesforce.integration.sdk.objects.SObjectOpportunity;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import neome.plugin.salesforce.integration.sdk.repositories.OpportunityRepository;
import neome.plugin.salesforce.integration.utils.FormUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class ApiGetOpportunityByAccount implements IPluginApi<ApiCtx, RecordIdInputForm, OpportunityList>
{
  @Override
  public void execute(ApiCtx ctx, RecordIdInputForm input, IPluginApiAcceptor<OpportunityList> output)
  {
    String accountId = input.id;
    MetaDataRepository metaDataRepository = ctx.getSalesforceFacade().metaDataRepository;
    Mono<OpportunityList> getOpportunities = ctx.getSalesforceUsername()
      .flatMap(salesforceUsername -> metaDataRepository
        .describe(salesforceUsername, SFObjectType.opportunity)
        .flatMap(sfDescribeDerived -> {
          OpportunityRepository opportunityRepository = ctx.getSalesforceFacade().opportunityRepository;
          return getOpportunitiesByAccountId(salesforceUsername, opportunityRepository, accountId)
            .map(opportunities -> {
              SObject[] opportunitiesSObjectArray = opportunities.toArray(new SObject[0]);
              OpportunityList opportunityList = new OpportunityList();
              opportunityList.opportunity = FormUtils.getGridRowList(opportunitiesSObjectArray,
                opportunity -> {
                  OpportunityList.Opportunity respOpportunity = new OpportunityList.Opportunity();
                  respOpportunity.id = opportunity.getId();
                  respOpportunity.amount = SObjectPlus.getDoubleValue(opportunity, SObjectOpportunity.FIELD_AMOUNT);
                  respOpportunity.name = SObjectPlus.getStringValue(opportunity, SObjectOpportunity.FIELD_NAME);
                  respOpportunity.description = SObjectPlus.getStringValue(opportunity, SObjectOpportunity.FIELD_NAME);
                  respOpportunity.closeDate =
                    SObjectPlus.getDateFieldValue(opportunity, SObjectOpportunity.FIELD_CLOSE_DATE);
                  respOpportunity.owner = SObjectPlus.getRefFieldFieldValueOption(opportunity,
                    SFRefField.owner,
                    SObjectOpportunity.FIELD_OWNER_ID);
                  respOpportunity.stage = SObjectPlus.getPickListFieldValue(sfDescribeDerived,
                    opportunity,
                    SObjectOpportunity.FIELD_STAGE_NAME);
                  return respOpportunity;
                });
              return opportunityList;
            });
        }));
    ctx.setOutput(output, getOpportunities);
  }

  private @NotNull Mono<List<SObject>> getOpportunitiesByAccountId(
    String salesforceUsername,
    OpportunityRepository opportunityRepository,
    String accountId)
  {
    return opportunityRepository
      .getByAccountId(salesforceUsername, accountId).
      flatMapIterable(items -> items).collectList();
  }
}


