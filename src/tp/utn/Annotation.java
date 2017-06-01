package tp.utn;

import java.lang.reflect.Field;

import tp.utn.ann.Column;
import tp.utn.ann.Table;

public class Annotation {
	
	
	public static String getTableName(Class<?> clase)
	{
		return ((Table)clase.getAnnotation(Table.class)).name();
	}
	
	public static String getAnnotationFieldName(Field campo)
	{
		return (campo.getAnnotation(Column.class)).name();
	}

}
