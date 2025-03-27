package neome.plugin.salesforce.api;

import java.util.Date;
import com.neome.plugin.base.IPluginApi;
import com.neome.plugin.base.IPluginApiAcceptor;
import com.neome.plugin.base.PluginApiException;
import com.sforce.soap.partner.sobject.SObject;
import neome.plugin.salesforce.ApiCtx;
import neome.plugin.salesforce.form.CreateOpportunity;
import neome.plugin.salesforce.form.CreatedRecordId;
import neome.plugin.salesforce.integration.sdk.objects.SObjectOpportunity;
import neome.plugin.salesforce.integration.sdk.repositories.OpportunityRepository;
import neome.plugin.salesforce.integration.utils.FormUtils;
import reactor.core.publisher.Mono;

public class ApiCreateOpportunity implements IPluginApi<ApiCtx, CreateOpportunity, CreatedRecordId>
{
  @Override
  public void execute(ApiCtx ctx, CreateOpportunity input,
    IPluginApiAcceptor<CreatedRecordId> output) throws PluginApiException
  {
    OpportunityRepository opportunityRepository = ctx.getSalesforceFacade().opportunityRepository;
    Mono<CreatedRecordId> createOpportunity = ctx.getSalesforceUsername()
      .flatMap(salesforceUsername -> opportunityRepository.create(salesforceUsername, createSObject(input)));
    ctx.setOutput(output, createOpportunity);
  }

  private SObject createSObject(CreateOpportunity createOpportunity)
  {
    String opportunityName = createOpportunity.opportunityName;
    String accountId = FormUtils.parseOptionId(createOpportunity.accountName);
    String type = FormUtils.parseOptionId(createOpportunity.type);
    String leadSource = FormUtils.parseOptionId(createOpportunity.leadSource);
    Double amount = createOpportunity.amount;
    Date closeDate = createOpportunity.closeDate;
    String stage = FormUtils.parseOptionId(createOpportunity.stage);
    Double probability = createOpportunity.probability;
    String orderNumber = createOpportunity.orderNumber;
    String currentGenerator = createOpportunity.currentGenerator;
    String trackingNumber = createOpportunity.trackingNumber;
    String mainCompetitor = createOpportunity.mainCompetitors;
    String deliveryInstallationStatus = FormUtils.parseOptionId(createOpportunity.deliveryInstallationStatus);
    String desc = createOpportunity.description;

    SObject sObject = new SObject();
    sObject.setType(SObjectOpportunity.OBJECT_TYPE);
    sObject.setSObjectField(SObjectOpportunity.FIELD_NAME, opportunityName);
    sObject.setSObjectField(SObjectOpportunity.FIELD_ACCOUNT_ID, accountId);
    sObject.setSObjectField(SObjectOpportunity.FIELD_TYPE, type);
    sObject.setSObjectField(SObjectOpportunity.FIELD_LEAD_SOURCE, leadSource);
    sObject.setSObjectField(SObjectOpportunity.FIELD_AMOUNT,
      amount != null
        ? amount
        : 0D);
    sObject.setSObjectField(SObjectOpportunity.FIELD_CLOSE_DATE, closeDate);
    sObject.setSObjectField(SObjectOpportunity.FIELD_STAGE_NAME, stage);
    sObject.setSObjectField(SObjectOpportunity.FIELD_PROBABILITY,
      probability != null
        ? probability
        : 0D);
    sObject.setSObjectField(SObjectOpportunity.FIELD_ORDER_NUMBER, orderNumber);
    sObject.setSObjectField(SObjectOpportunity.FIELD_CURRENT_GENERATOR, currentGenerator);
    sObject.setSObjectField(SObjectOpportunity.FIELD_TRACKING_NUMBER, trackingNumber);
    sObject.setSObjectField(SObjectOpportunity.FIELD_MAIN_COMPETITOR, mainCompetitor);
    sObject.setSObjectField(SObjectOpportunity.FIELD_DELIVERY_INSTALLATION_STATUS, deliveryInstallationStatus);
    sObject.setSObjectField(SObjectOpportunity.FIELD_DESCRIPTION, desc);
    return sObject;
  }
}
