package tp.utn;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Reflection {

	public static boolean isPrimitiveClass(Field field)
	{
		Class<?> type=field.getType();
		if((type==int.class)||(type==Integer.class)||(type==String.class)||(type==char.class)||(type==double.class)||(type==long.class)||(type==short.class)||(type==boolean.class)) return true;
		return false;
	}
	
	public static ArrayList<Method> getGettersSetters(Method metodos[], String prefijo)
	{
		ArrayList<Method> settersOGetters=new ArrayList<Method>();
		for(Method metodo:metodos)
		{
			if(esUnSetterOGetter(metodo,prefijo)) settersOGetters.add(metodo);
		}
		return settersOGetters;
	}
	
	public static boolean esUnSetterOGetter(Method metodo, String prefijo)
	{
		return metodo.getName().substring(0,3).equals(prefijo);
	}
	
	public static <T> Constructor<?> getConstructor(Class<T> dtoClass) throws NoSuchMethodException,SecurityException,ClassNotFoundException
	{
		return ((Class<T>)Class.forName(dtoClass.getName())).getConstructor();
	}

	public static String getAtributoDelSetterOGetter(Method metodo)
	{
		return metodo.getName().substring(3);

	}
}
