package neome.plugin.salesforce.form;

import java.util.Date;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.plugin.base.IApiInput;

public class CreateOpportunity implements IApiInput
{

  public String id;

  public String opportunityName;

  public FieldValueOptionId accountName;

  public FieldValueOptionId type;

  public FieldValueOptionId leadSource;

  public Double amount;

  public Date closeDate;

  public FieldValueOptionId stage;

  public Double probability;

  public String orderNumber;

  public String currentGenerator;

  public String trackingNumber;

  public String mainCompetitors;

  public FieldValueOptionId deliveryInstallationStatus;

  public String description;
}
