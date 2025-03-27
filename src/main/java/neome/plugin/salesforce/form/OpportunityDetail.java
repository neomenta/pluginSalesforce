package neome.plugin.salesforce.form;

import java.util.Date;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.plugin.base.IApiDone;

public class OpportunityDetail implements IApiDone
{

  public String id;

  public String opportunityName;

  public FieldValueOptionId accountName;

  public Date closeDate;

  public FieldValueOptionId type;

  public FieldValueOptionId leadSource;

  public FieldValueOptionId stage;

  public Double probability;

  public String orderNumber;

  public String currentGenerator;

  public String trackingNumber;

  public FieldValueOptionId deliveryInstallationStatus;

  public String description;

  public Double amount;

  public String mainCompetitors;
}
