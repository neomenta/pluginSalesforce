package neome.plugin.salesforce.integration.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.neome.api.meta.base.GridRowList;
import com.neome.api.meta.base.Types;
import com.neome.api.meta.base.dto.FieldValueOptionId;
import com.neome.api.meta.base.dto.StudioDtoOption;
import com.neome.api.meta.base.dto.StudioMapOfOption;
import com.neome.plugin.base.forms.SysFormMapOfOptions;

public class FormUtils
{
  public static <T, INPUT> GridRowList<T> getGridRowList(INPUT[] inputArray, Function<INPUT, T> fnConvert)
  {
    GridRowList<T> gridRowList = new GridRowList<>();
    List<Types.RowId> keys = new LinkedList<>();
    Map<Types.RowId, T> rowMap = new HashMap<>();
    Arrays.stream(inputArray).forEach(input -> {
      Types.RowId rowId = Types.RowId.nextId(Types.RowId.class);
      T converted = fnConvert.apply(input);
      keys.add(rowId);
      rowMap.put(rowId, converted);
    });
    gridRowList.keys = keys.toArray(new Types.RowId[0]);
    gridRowList.map = rowMap;
    return gridRowList;
  }

  public static <T> SysFormMapOfOptions getSysFormMapOfOptions(List<T> pickValueList,
    Function<T, StudioDtoOption> fnConvert)
  {
    SysFormMapOfOptions sysFormMapOfOptions = new SysFormMapOfOptions();
    StudioMapOfOption options = new StudioMapOfOption();
    Map<String, StudioDtoOption> map = new HashMap<>();
    List<String> keys = new LinkedList<>();
    for(T pickListValue : pickValueList)
    {
      StudioDtoOption studioDtoOption = fnConvert.apply(pickListValue);
      keys.add(studioDtoOption.metaId);
      map.put(studioDtoOption.metaId, studioDtoOption);
    }
    options.keys = keys.toArray(new String[0]);
    options.map = map;
    sysFormMapOfOptions.options = options;
    return sysFormMapOfOptions;
  }

  public static String parseOptionId(FieldValueOptionId fieldValueOptionId)
  {
    return fieldValueOptionId != null
      ? fieldValueOptionId.optionId
      : null;
  }
}
