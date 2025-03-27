package neome.plugin.salesforce.api;

import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.OpportunityDetail;
import neome.plugin.salesforce.form.RecordIdInputForm;
import neome.plugin.salesforce.integration.exception.SObjectNotFoundException;
import neome.plugin.salesforce.integration.sdk.SalesforceFacade;
import neome.plugin.salesforce.integration.sdk.objects.SFObjectType;
import neome.plugin.salesforce.integration.sdk.objects.SFRefField;
import neome.plugin.salesforce.integration.sdk.objects.SObjectOpportunity;
import neome.plugin.salesforce.integration.sdk.objects.SObjectPlus;
import neome.plugin.salesforce.integration.sdk.repositories.MetaDataRepository;
import neome.plugin.salesforce.integration.sdk.repositories.OpportunityRepository;
import neome.plugin.salesforce.integration.utils.SFDateUtil;
import reactor.core.publisher.Mono;

public class ApiGetOpportunityDetails implements IPluginApi<ApiCtx, RecordIdInputForm, OpportunityDetail>
{
  @Override
  public void execute(ApiCtx ctx, RecordIdInputForm input, IPluginApiAcceptor<OpportunityDetail> output)
    throws PluginApiException
  {
    String id = input.id;
    Mono<OpportunityDetail> getOpportunityDetails =
      ctx.getSalesforceUsername()
        .flatMap(username -> getOpportunityDetail(ctx, username, id));
    ctx.setOutput(output, getOpportunityDetails);
  }

  public static Mono<OpportunityDetail> getOpportunityDetail(ApiCtx apiCtx, String username, String id)
  {
    SalesforceFacade salesforceFacade = apiCtx.getSalesforceFacade();
    OpportunityRepository opportunityRepository = salesforceFacade.opportunityRepository;
    MetaDataRepository metaDataRepository = salesforceFacade.metaDataRepository;
    return metaDataRepository.describe(username, SFObjectType.opportunity)
      .flatMap(sObjectMetaData ->
        opportunityRepository
          .getById(username, id)
          .flatMap(optionalSObject -> {
            if(optionalSObject.isPresent())
            {
              SObject sObject = optionalSObject.get();
              OpportunityDetail opportunityDetail = new OpportunityDetail();
              opportunityDetail.id = id;
              opportunityDetail.opportunityName =
                SObjectPlus.getStringValue(sObject, SObjectOpportunity.FIELD_NAME);
              opportunityDetail.type =
                SObjectPlus.getPickListFieldValue(sObjectMetaData, sObject, SObjectOpportunity.FIELD_TYPE);
              opportunityDetail.accountName =
                SObjectPlus.getRefFieldFieldValueOption(sObject,
                  SFRefField.account,
                  SObjectOpportunity.FIELD_ACCOUNT_ID);
              opportunityDetail.leadSource =
                SObjectPlus.getPickListFieldValue(sObjectMetaData, sObject, SObjectOpportunity.FIELD_LEAD_SOURCE);
              opportunityDetail.amount = SObjectPlus.getDoubleValue(sObject, SObjectOpportunity.FIELD_AMOUNT);
              String closeDateString = SObjectPlus.getStringValue(sObject, SObjectOpportunity.FIELD_CLOSE_DATE);
              opportunityDetail.closeDate = SFDateUtil.parseDate(closeDateString);
              opportunityDetail.stage =
                SObjectPlus.getPickListFieldValue(sObjectMetaData, sObject, SObjectOpportunity.FIELD_STAGE_NAME);
              opportunityDetail.currentGenerator =
                SObjectPlus.getStringValue(sObject, SObjectOpportunity.FIELD_CURRENT_GENERATOR);
              opportunityDetail.deliveryInstallationStatus =
                SObjectPlus.getPickListFieldValue(sObjectMetaData,
                  sObject,
                  SObjectOpportunity.FIELD_DELIVERY_INSTALLATION_STATUS);
              opportunityDetail.mainCompetitors =
                SObjectPlus.getStringValue(sObject, SObjectOpportunity.FIELD_MAIN_COMPETITOR);
              opportunityDetail.trackingNumber =
                SObjectPlus.getStringValue(sObject, SObjectOpportunity.FIELD_TRACKING_NUMBER);
              opportunityDetail.orderNumber =
                SObjectPlus.getStringValue(sObject, SObjectOpportunity.FIELD_ORDER_NUMBER);
              opportunityDetail.probability =
                SObjectPlus.getDoubleValue(sObject, SObjectOpportunity.FIELD_PROBABILITY);
              opportunityDetail.description =
                SObjectPlus.getStringValue(sObject, SObjectOpportunity.FIELD_DESCRIPTION);
              return Mono.just(opportunityDetail);
            }
            return Mono.error(new SObjectNotFoundException("Opportunity not found"));
          }));
  }
}


