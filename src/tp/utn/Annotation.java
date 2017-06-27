package tp.utn;

import java.lang.reflect.Field;
import java.util.ArrayList;

import tp.utn.ann.Column;
import tp.utn.ann.Table;

public class Annotation {
	
	
	public static String getTableName(Class<?> clase)
	{
		return ((Table)clase.getAnnotation(Table.class)).name();
	}
	public static String getTableNameFromClassName(String nombreClase, Class<?> clasePrincipal)
	{

		Field[] atributos = clasePrincipal.getDeclaredFields();
		for(Field atributo : atributos)
		{
			if(atributo.getName().equals(nombreClase))
			return (String)atributo.getType().getAnnotation(Table.class).name();
		}
		
		return null;
	}
	public static String getAnnotationFieldName(Field campo)
	{
		if(campo.getAnnotation(Column.class)!=null)
			return (campo.getAnnotation(Column.class)).name();
		return null;
	}

}
