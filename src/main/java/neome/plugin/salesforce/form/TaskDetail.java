package neome.plugin.salesforce.form;

import java.util.Date;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.plugin.base.IApiDone;

public class TaskDetail implements IApiDone
{
  public String id;

  public String subject;

  public Date dueDate;

  public FieldValueOptionId status;

  public FieldValueOptionId priority;

  public FieldValueOptionId relatedTo;

  public FieldValueOptionId assignedTo;

  public String description;
}
