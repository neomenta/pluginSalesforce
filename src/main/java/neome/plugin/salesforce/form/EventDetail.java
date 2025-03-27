package neome.plugin.salesforce.form;

import java.util.Date;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.api.meta.base.dto.FieldValueSwitch;
import com.neome.plugin.base.IApiDone;

public class EventDetail implements IApiDone
{
  public String id;

  public String subject;

  public String description;

  public Date startDateTime;

  public Date endDateTime;

  public FieldValueSwitch isAllDayEvent;

  public String location;

  public FieldValueOptionId assignedTo;

  public FieldValueOptionId relatedTo;
}
