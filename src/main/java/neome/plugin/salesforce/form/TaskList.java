package neome.plugin.salesforce.form;

import java.util.Date;
import com.neome.api.meta.base.GridRowList;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.plugin.base.IApiDone;

public class TaskList implements IApiDone
{
  public GridRowList<Task> task;

  public static class Task
  {
    public String id;

    public String subject;

    public String description;

    public Date dueDate;

    public FieldValueOptionId status;

    public FieldValueOptionId priority;

    public FieldValueOptionId relatedTo;

    public FieldValueOptionId assignedTo;
  }
}
