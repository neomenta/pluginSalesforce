package neome.plugin.salesforce.integration.sdk.objects;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.PicklistEntry;

public class SFDescribeDerived
{
  private Map<String, Field> fieldMap;

  private final Map<String, Map<String, String>> pickListLabelMap;

  private final Map<String, PicklistEntry[]> pickListMap;

  public SFDescribeDerived(DescribeSObjectResult describeObj)
  {
    this.fieldMap = computeFieldMap(describeObj);
    this.pickListMap = computePickListMap(describeObj);
    this.pickListLabelMap = computePickValueMap(pickListMap);
  }

  public Map<String, Field> getFieldMap()
  {
    return Collections.unmodifiableMap(fieldMap);
  }

  private Map<String, PicklistEntry[]> computePickListMap(DescribeSObjectResult describeObj)
  {
    Map<String, PicklistEntry[]> pickListMap = new LinkedHashMap<>();
    for(Field field : describeObj.getFields())
    {
      if(field.getType().equals(FieldType.picklist))
      {
        pickListMap.put(field.getName(), field.getPicklistValues());
      }
    }
    return pickListMap;
  }

  private Map<String, Map<String, String>> computePickValueMap(Map<String, PicklistEntry[]> pickListMap)
  {
    Map<String, Map<String, String>> fieldMap = new LinkedHashMap<>();
    pickListMap.forEach((fieldName, pickList) -> {
      for(PicklistEntry picklistValue : pickList)
      {
        fieldMap
          .computeIfAbsent(fieldName, k -> new LinkedHashMap<>())
          .put(picklistValue.getValue(), picklistValue.getLabel());
      }
    });
    return fieldMap;
  }

  private Map<String, Field> computeFieldMap(DescribeSObjectResult describeObj)
  {
    Map<String, Field> fieldMap = new LinkedHashMap<>();
    for(Field field : describeObj.getFields())
    {
      fieldMap.put(field.getName(), field);
    }
    return fieldMap;
  }

  public String getLabelByPickListItemId(String fieldName, String pickListItemId)
  {
    Map<String, String> fieldPickListMap = pickListLabelMap.get(fieldName);
    if(fieldPickListMap == null)
    {
      return null;
    }
    return fieldPickListMap.get(pickListItemId);
  }
}
