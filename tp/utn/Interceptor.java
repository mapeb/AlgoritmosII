package tp.utn;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import tp.utn.Reflection;
import tp.utn.ann.Column;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Interceptor
{

	public static void intercept(Class clazz)
	{
		Enhancer enhancer=new Enhancer();
		enhancer.setSuperclass(clazz);

		enhancer.setCallback(new MethodInterceptor()
		{
			@Override
			public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
			{
				if(Reflection.isGetter(method))
				{
					Field field=o.getClass().getDeclaredField(method.getName().substring(3));
					if(field.getAnnotation(Column.class).fetchType()==1) return null;		
				}
				return null;
			}
		});
	}
	
	public MethodProxy proxy(){
		return null;
	}

}