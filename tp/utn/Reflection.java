package tp.utn;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Reflection
{

	public static boolean isPrimitiveClass(Field field)
	{
		Class<?> type=field.getType();
		if((type==int.class)||(type==Integer.class)||(type==String.class)||(type==char.class)||(type==double.class)||(type==long.class)||(type==short.class)||(type==boolean.class)) return true;
		return false;
	}

	public static String getIdField(Class dtoClass)
	{
		Field[] campos=dtoClass.getDeclaredFields();
		for(Field campo:campos)
		{

			String prefijo=campo.getName().substring(0,2);
			if(prefijo.equals("id")||prefijo.equals("ID")) return campo.getName();
		}
		return null;
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

	public static boolean isGetter(Method method) throws Exception
	{
		try{
		String subfijo = method.getName().substring(0,3);
		String name = method.getName().substring(3);
		Class clazz = method.getDeclaringClass();
		Field field = clazz.getDeclaredField(name);
		if(subfijo.equalsIgnoreCase("get"))
			return true;
		}catch(Exception ex){
			return false;
		}
		return false;
	}

}
