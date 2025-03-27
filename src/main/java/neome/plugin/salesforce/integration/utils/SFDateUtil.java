package neome.plugin.salesforce.integration.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SFDateUtil
{
  public static Date parseDate(String dateString)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try
    {
      return dateFormat.parse(dateString);
    }
    catch(ParseException e)
    {
      throw new RuntimeException(e);
    }
  }
}
