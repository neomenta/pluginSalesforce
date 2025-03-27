package neome.plugin.salesforce.integration.sdk.query;

public class SFWhere
{
  private final String columnName;

  private final String columnValue;

  public SFWhere(String columnName, String columnValue)
  {
    this.columnName = columnName;
    this.columnValue = columnValue;
  }

  public String getColumnName()
  {
    return columnName;
  }

  public String getColumnValue()
  {
    return columnValue;
  }
}
