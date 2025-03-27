package neome.plugin.salesforce.form;

import java.util.Date;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.plugin.base.IApiInput;

public class CreateTask implements IApiInput
{

  public String id;

  public String subject;

  public Date dueDate;

  public FieldValueOptionId status;

  public FieldValueOptionId priority;

  public FieldValueOptionId assignedTo;

  public FieldValueOptionId relatedTo;

  public String description;
}
