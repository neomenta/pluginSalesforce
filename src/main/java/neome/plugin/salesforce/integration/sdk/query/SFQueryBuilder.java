package neome.plugin.salesforce.integration.sdk.query;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.neome.util.plus.StringPlus;

public class SFQueryBuilder
{

  private final Set<String> columns = new LinkedHashSet<>();

  private String from;

  private int recordLimit;

  private String orderBy;

  private final List<String> whereList = new ArrayList<>();

  public SFQueryBuilder limit(int limit)
  {
    this.recordLimit = limit;
    return this;
  }

  public SFQueryBuilder addColumn(String column)
  {
    columns.add(column);
    return this;
  }

  public SFQueryBuilder where(List<SFWhere> whereList, boolean and)
  {
    if(whereList.isEmpty())
    {
      return this;
    }
    if(whereList.size() == 1)
    {
      SFWhere sfWhere = whereList.get(0);
      String whereString = "%s='%s'".formatted(sfWhere.getColumnName(), sfWhere.getColumnValue());
      this.whereList.add(whereString);
    }
    else
    {
      String whereCondition = buildWhereCondition(whereList, and);
      if(!StringPlus.isNullOrEmpty(whereCondition))
      {
        this.whereList.add(whereCondition);
      }
    }
    return this;
  }

  private String buildWhereCondition(List<SFWhere> whereList, boolean and)
  {
    if(whereList.isEmpty())
    {
      return "";
    }
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("(");
    for(int whereIndex = 0; whereIndex < whereList.size(); whereIndex++)
    {
      SFWhere sfWhere = whereList.get(whereIndex);
      if(whereIndex == whereList.size() - 1)
      {
        String whereString = "%s='%s'".formatted(sfWhere.getColumnName(), sfWhere.getColumnValue());
        stringBuilder.append(whereString);
        stringBuilder.append(")");
      }
      else
      {
        String whereString = "%s='%s'".formatted(sfWhere.getColumnName(), sfWhere.getColumnValue());
        stringBuilder.append(whereString);
        if(and)
        {
          stringBuilder.append(" AND ");
        }
        else
        {
          stringBuilder.append(" OR ");
        }
      }
    }
    return stringBuilder.toString();
  }

  public SFQueryBuilder from(String from)
  {
    this.from = from;
    return this;
  }

  public String build()
  {
    validate();
    String commaSeparatedColumn = String.join(",", columns);
    StringBuilder sb = new StringBuilder("SELECT ");
    sb.append(commaSeparatedColumn);
    sb.append(" FROM ").append(from);

    if(!whereList.isEmpty())
    {
      StringBuilder whereClause = new StringBuilder();
      for(int whereIndex = 0; whereIndex < whereList.size(); whereIndex++)
      {
        String whereString = whereList.get(whereIndex);
        if(whereIndex == 0)
        {
          whereClause.append(" WHERE ");
        }
        whereClause.append(whereString);
      }
      sb.append(whereClause);
    }
    if(orderBy != null)
    {
      sb.append(orderBy);
    }
    if(recordLimit > 0)
    {
      sb.append(" LIMIT ")
        .append(recordLimit);
    }
    return sb.toString();
  }

  private void validate()
  {
    boolean isFromValid = from != null && !from.trim().isEmpty();
    boolean isColumnsExist = !columns.isEmpty();
    boolean isValid = isFromValid && isColumnsExist;
    if(!isValid)
    {
      throw new RuntimeException("Please provide valid values");
    }
  }

  public void orderBy(String createdDate)
  {
    orderBy = " ORDER BY %s".formatted(createdDate);
  }
}


