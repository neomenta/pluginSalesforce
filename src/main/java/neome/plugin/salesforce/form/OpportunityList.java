package neome.plugin.salesforce.form;

import java.util.Date;
import com.neome.api.meta.base.GridRowList;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.plugin.base.IApiDone;

public class OpportunityList implements IApiDone
{
  public GridRowList<Opportunity> opportunity;

  public static class Opportunity
  {
    public String id;

    public String name;

    public Double amount;

    public Date closeDate;

    public FieldValueOptionId owner;

    public FieldValueOptionId stage;

    public String description;
  }
}
