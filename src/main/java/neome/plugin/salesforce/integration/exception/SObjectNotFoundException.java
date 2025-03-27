package neome.plugin.salesforce.integration.exception;

import com.neome.plugin.base.PluginApiException;

public class SObjectNotFoundException extends PluginApiException
{
  public SObjectNotFoundException(String message, String... messageParams)
  {
    super(message, messageParams);
  }

  public SObjectNotFoundException(Throwable throwable, String message, String... messageParams)
  {
    super(throwable, message, messageParams);
  }
}
