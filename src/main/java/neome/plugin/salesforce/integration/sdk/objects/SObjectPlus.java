package neome.plugin.salesforce.integration.sdk.objects;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.api.meta.base.dto.FieldValueSwitch;
import com.sforce.soap.partner.sobject.SObject;
import org.jetbrains.annotations.Nullable;

public class SObjectPlus
{
  public static String getPickListLabel(SFDescribeDerived describeSObj,
    String fieldName,
    String pickListItemId)
  {
    return describeSObj.getLabelByPickListItemId(fieldName, pickListItemId);
  }

  public static FieldValueOptionId getPickListFieldValue(
    SFDescribeDerived describeSObject,
    SObject sObject,
    String fieldName)
  {
    String pickListValue = (String) sObject.getField(fieldName);
    if(pickListValue != null)
    {
      String pickListLabel = SObjectPlus.getPickListLabel(describeSObject, fieldName, pickListValue);
      FieldValueOptionId fieldValueOptionId = new FieldValueOptionId();
      fieldValueOptionId.optionId = pickListValue;
      fieldValueOptionId.value = pickListLabel;
      return fieldValueOptionId;
    }
    return null;
  }

  private static Date parseDate(String dateString)
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

  public static Double getDoubleValue(SObject sObject, String fieldName)
  {
    Object field = getFieldValue(sObject, fieldName);
    if(field == null)
    {
      return null;
    }
    String fieldValue = (String) field;
    return Double.parseDouble(fieldValue);
  }

  public static Boolean getBoolean(SObject sObject, String fieldName)
  {
    Object field = getFieldValue(sObject, fieldName);
    if(field == null)
    {
      return null;
    }
    String fieldValue = (String) field;
    return Boolean.parseBoolean(fieldValue);
  }

  public static FieldValueSwitch getFieldValueSwitch(SObject sObject, String fieldName)
  {
    Boolean aBooleanValue = getBoolean(sObject, fieldName);
    if(aBooleanValue == null)
    {
      return null;
    }
    FieldValueSwitch fieldValueSwitch = new FieldValueSwitch();
    fieldValueSwitch.value = aBooleanValue;
    return fieldValueSwitch;
  }

  public static Date getDateFieldValue(SObject sObject, String fieldName)
  {
    Object field = getFieldValue(sObject, fieldName);
    if(field == null)
    {
      return null;
    }
    String fieldValue = (String) field;
    return parseDate(fieldValue);
  }

  public static String getStringValue(SObject sObject, String fieldName)
  {
    Object field = getFieldValue(sObject, fieldName);
    if(field == null)
    {
      return null;
    }
    return (String) field;
  }

  private static @Nullable Object getFieldValue(SObject sObject, String fieldName)
  {
    if(sObject == null)
    {
      return null;
    }
    Object field = sObject.getField(fieldName);
    if(field == null)
    {
      return null;
    }
    return field;
  }

  public static FieldValueOptionId getRefFieldFieldValueOption(SObject sObject, SFRefField objectType, String idField)
  {
    String id = (String) sObject.getField(idField);
    SObject subSObject = (SObject) sObject.getField(objectType.getValue());
    if(subSObject != null)
    {
      String name = (String) subSObject.getField("Name");
      FieldValueOptionId fieldValueOptionId = new FieldValueOptionId();
      fieldValueOptionId.optionId = id;
      fieldValueOptionId.value = name;
      return fieldValueOptionId;
    }
    return null;
  }
}
