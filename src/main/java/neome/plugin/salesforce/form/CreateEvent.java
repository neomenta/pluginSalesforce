package neome.plugin.salesforce.form;

import java.util.Date;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.api.meta.base.dto.FieldValueSwitch;
import com.neome.plugin.base.IApiInput;

public class CreateEvent implements IApiInput
{

  public String id;

  public String subject;

  public String description;

  public Date startDate;

  public Date endDate;

  public String location;

  public FieldValueSwitch isAllDayEvent;

  public FieldValueOptionId assignedTo;

  public FieldValueOptionId relatedTo;
}
