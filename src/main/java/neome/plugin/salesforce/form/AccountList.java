package neome.plugin.salesforce.form;

import com.neome.api.meta.base.GridRowList;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.plugin.base.IApiDone;

public class AccountList implements IApiDone
{
  public GridRowList<Account> account;

  public static class Account
  {
    public String id;

    public String name;

    public String accountSite;

    public String accountNumber;

    public String phone;

    public FieldValueOptionId type;

    public FieldValueOptionId industry;

    public String billingState;

    public FieldValueOptionId owner;
  }
}
