
package tp.utn;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import tp.utn.ann.Column;
import tp.utn.ann.Id;
import tp.utn.ann.Table;
import tp.utn.main.SingletonConexion;
import tp.utn.main.ValorDiccionario;

public class Query
{
	List<String> select;
	String from;
	ArrayList<String> anotacionesVariables=new ArrayList<String>();

	public Query(String from)
	{
		super();
		select=new ArrayList<String>();
		this.from=from;

	}

	public Query()
	{
		super();
	}

	public void generarQuery(Field[] campos, Class dtoClass)
	{
		for(Field campo:campos)
		{
			if(campo.getAnnotation(Column.class)!=null&&isPrimitiveClass(campo)) this.addAttr(dtoClass,campo.getAnnotation(Column.class).name());
			else
			{
				if(campo.getType().getAnnotation(Table.class)!=null&&campo.getAnnotation(Column.class).fetchType()==2)
				{
					// this.addJoin(campo, campo.getType());
					this.addJoin(dtoClass,campo);
					generarQuery(campo.getType().getDeclaredFields(),campo.getType());
				}
			}
		}
	}

	public String sacarPesos(String frase)
	{
		StringBuffer cadena=new StringBuffer();
		String[] palabras=frase.split("\\$");
		for(String palabra:palabras)
		{
			cadena=cadena.append(palabra);
		}
		return cadena.toString();
	}

	public String generarString(String xql)
	{
		String xqlLimpio=sacarPesos(xql);
		String q="SELECT ";
		for(String attr:this.getSelect())
			q+=attr+",";
		q=q.substring(0,q.length()-1);
		q+=" FROM "+from+" "+xqlLimpio;
		return q;
	}

	private static boolean isPrimitiveClass(Field field)
	{
		Class type=field.getType();
		if((type==int.class)||(type==Integer.class)||(type==String.class)||(type==char.class)||(type==double.class)||(type==long.class)||(type==short.class)||(type==boolean.class)) return true;
		return false;
	}

	public void addAttr(Class claseContenedora, String atrr)
	{
		String newAtrr=nombreTabla(claseContenedora)+"."+atrr;
		this.getSelect().add(newAtrr);
	}

	public void addJoin(Class claseRaiz, Field campo)
	{
		// Table tabla2 = (Table) clase2.getAnnotation(Table.class);
		from+=" JOIN "+nombreTabla(campo.getType())+joinDeTablas(claseRaiz,campo);
	}

	private String joinDeTablas(Class campoSolicitante, Field campoSolicitado)
	{
		String comparacion=" ON ( "+nombreTabla(campoSolicitante);
		comparacion+="."+campoSolicitado.getAnnotation(Column.class).name()+" = ";
		comparacion+=nombreTabla(campoSolicitado.getType())+".";

		for(Field field:campoSolicitado.getType().getDeclaredFields())
		{
			if(field.getAnnotation(Id.class)!=null) return comparacion+field.getAnnotation(Column.class).name()+" )";
		}
		return ""; // Hola si la funcion no tiene id debo explotar, ya se .
	}

	private String nombreTabla(Class clase)
	{
		return ((Table)clase.getAnnotation(Table.class)).name();
	}

	public List<String> getSelect()
	{
		return select;
	}

	public void setSelect(List<String> select)
	{
		this.select=select;
	}

	public void setFrom(String n)
	{
		this.from=n;
	}

	public ArrayList<Method> obtenerSettersOGetters(Method metodos[], String prefijo)
	{
		ArrayList<Method> settersOGetters=new ArrayList<Method>();
		for(Method metodo:metodos)
		{
			if(esUnSetterOGetter(metodo,prefijo)) settersOGetters.add(metodo);
		}
		return settersOGetters;
	}

	public String stringMinuscula(String palabra)
	{
		return (palabra.substring(0,1).toLowerCase()+palabra.substring(1));

	}

	public String stringMayuscula(String palabra)
	{
		return (palabra.substring(0,1).toUpperCase()+palabra.substring(1));
	}

	public Method buscarSetterHijoAPadre(Object objetoHijo, Object objetoPadre)
	{
		Method metodosPadre[]=objetoPadre.getClass().getDeclaredMethods();
		ArrayList<Method> settersPadre=obtenerSettersOGetters(metodosPadre,"set");
		for(Method setter:settersPadre)
		{
			if(setter.getName().substring(3).equals(objetoHijo.getClass().getSimpleName())) return setter;
		}
		return null;
	}

	public boolean esUnSetterOGetter(Method metodo, String prefijo)
	{
		String nombre=metodo.getName();
		return nombre.substring(0,3).equals(prefijo);
	}

	public <T> void settearSobreObjeto(ResultSet rs, Field campo, String nombreEnTabla, Method setter, Object objeto, List<T> listaObjetos)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,SQLException
	{
		Class type=campo.getType();
		try
		{
			if(type==int.class||type==Integer.class) setter.invoke(objeto,rs.getInt(nombreEnTabla));

			else
			{
				if(!isPrimitiveClass(campo))
				{
					Constructor<?> constructor=obtenerConstructor(campo.getType());
					Object objetoCampo=null;

					objetoCampo=constructor.newInstance();
					settearValoresAObjeto(campo.getType(),objetoCampo,rs,listaObjetos);

					Method settearObjeto=buscarSetterHijoAPadre(objetoCampo,objeto);
					settearObjeto.invoke(objeto,objetoCampo);
					listaObjetos.add((T)objetoCampo);

				}
				else
				{
					Method[] metodosStatement=rs.getClass().getDeclaredMethods();
					for(Method metodo:metodosStatement)
					{
						String tipoClase;
						String tipoClaseMin;
						int valorInt;
						if(esUnSetterOGetter(metodo,"get"))
						{
							tipoClase=metodo.getName().substring(3);
							tipoClaseMin=stringMinuscula(tipoClase);
							if(type.getSimpleName().equals(tipoClase)||type.getSimpleName().equals(tipoClaseMin))
							{
								Class parametros[]=metodo.getParameterTypes();
								int cantidadParametros=0;
								for(Class parametro:parametros)
									cantidadParametros++;
								if(cantidadParametros==1&&parametros[0].getSimpleName().equals(type.getSimpleName())) setter.invoke(objeto,metodo.invoke(rs,nombreEnTabla));
							}

						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	public String nombreAtributoEnTabla(Class dtoClass, Field campo)
	{
		return nombreTabla(dtoClass)+"."+campo.getAnnotation(Column.class).name();
	}

	public <T> Constructor<?> obtenerConstructor(Class dtoClass) throws NoSuchMethodException,SecurityException,ClassNotFoundException
	{
		Class<T> clazz=(Class<T>)Class.forName(dtoClass.getName());
		return clazz.getConstructor();
	}

	public String obtenerAtributoDelSetterOGetter(Method metodo)
	{
		return metodo.getName().substring(3);

	}

	public String getTabla(String anotacionSQL)// ej: persona.id_persona
	{
		String retorno=null;
		if(anotacionSQL.contains("."))
		{

			String[] division=anotacionSQL.split("\\.");
			retorno=division[0];
		}
		return retorno;
	}

	public String sacarTabla(String anotacionSQL)
	{
		String atributo=null;
		if(anotacionSQL.contains("."))
		{

			String[] division=anotacionSQL.split("\\.");
			atributo=division[1];
		}
		return atributo;
	}

	public <T> ArrayList<String> obtenerVariablesDesdeAnotaciones(Class<T> dtoClass)
	{
		ArrayList<String> variables=new ArrayList<String>();
		Field[] campos=dtoClass.getDeclaredFields();
		ArrayList<String> anotacionesDeOtraClase=new ArrayList<String>();
		for(String anotacion:anotacionesVariables)
		{
			if(getTabla(anotacion).equals(dtoClass.getAnnotation(Table.class).name()))
			{

				anotacion=sacarTabla(anotacion);
				for(Field campo:campos)
				{
					if(campo.getAnnotation(Column.class).name().equals(anotacion))
					{

						variables.add(campo.getName());
						break;
					}
				}
			}
			else
			{
				int i=0;
				for(Field campito : campos)
				{
					if(!isPrimitiveClass(campito) && getTabla(anotacion).equals(campito.getType().getAnnotation(Table.class).name()))
					{
						anotacion = sacarTabla(anotacion);
						Field[] camposSegundaClase = campito.getType().getDeclaredFields();
						for(Field campoSegunda : camposSegundaClase)
						{
							if(campoSegunda.getAnnotation(Column.class).name().equals(anotacion))
							{
								variables.add(campoSegunda.getName());
								i=1;
								break;
							}
						}
					}
					if(i==1) break;
				}
			}

		}

		return variables;

	}

	public <T> void settearValoresAObjeto(Class dtoClass, Object objeto, ResultSet rs, List<T> listaObjetos)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,SQLException
	{
		Field[] campos=dtoClass.getDeclaredFields();
		Method metodos[]=dtoClass.getDeclaredMethods();
		ArrayList<Method> setters=obtenerSettersOGetters(metodos,"set");

		for(Method setter:setters)
		{
			String atributoSetter=obtenerAtributoDelSetterOGetter(setter);
			atributoSetter=stringMinuscula(atributoSetter);
			for(Field campo:campos)
			{
				if(campo.getName().equals(atributoSetter)&&campo.getAnnotation(Column.class)!=null)
				{
					String nombreEnTabla=nombreAtributoEnTabla(dtoClass,campo);
					// if(type==String.class)
					// setter.invoke(objeto,rs.getString(nombreEnTabla));
					settearSobreObjeto(rs,campo,nombreEnTabla,setter,objeto,listaObjetos);
				}
			}

		}
	}

	public <T> String obtenerTipoCampoDeOtraClase(Class<T> dtoClass, String campo) throws NoSuchFieldException,SecurityException
	{
		Field[] camposClasePrincipal=dtoClass.getDeclaredFields();
		for(Field campoClaseP:camposClasePrincipal)
		{
			if(!isPrimitiveClass(campoClaseP))
			{
				if(campoClaseP.getType().getDeclaredField(campo)!=null)
				{
					Field campoSec=campoClaseP.getType().getDeclaredField(campo);
					return campoSec.getType().getSimpleName();
				}
			}
		}
		return null;

	}

	public <T> void agregarCondicion(String xql, PreparedStatement pstm, Object[] args, Class dtoClass)
			throws NoSuchFieldException,SecurityException,IllegalAccessException,IllegalArgumentException,InvocationTargetException
	{
		try
		{
			String nuevaPalabra=null;
			String[] palabras=xql.split(" ");
			for(String palabra:palabras)
			{
				if(palabra.substring(0,1).equals("$"))
				{
					palabra=palabra.substring(1);
					/*
					 * if(palabra.contains(".")) {
					 * 
					 * String[] division=palabra.split("\\.");
					 * nuevaPalabra=division[1]; }
					 */
					anotacionesVariables.add(palabra);
				}
			}
			ArrayList<String> variables=obtenerVariablesDesdeAnotaciones(dtoClass);
			Method[] metodos=pstm.getClass().getDeclaredMethods();
			ArrayList<Method> setters=obtenerSettersOGetters(metodos,"set");
			int i=0;
			boolean esVariableDeLaClase=true;
			String tipo=null;
			for(String variable:variables)
			{
				try
				{
					dtoClass.getDeclaredField(variable);

				}
				catch(NoSuchFieldException ex)
				{
					tipo=obtenerTipoCampoDeOtraClase(dtoClass,variable);
					esVariableDeLaClase=false;
				}
				if(esVariableDeLaClase)
				{
					Field campo=dtoClass.getDeclaredField(variable);
					tipo=campo.getType().getSimpleName();
				}
				for(Method setter:setters)
				{
					if(obtenerAtributoDelSetterOGetter(setter).equals(tipo)||obtenerAtributoDelSetterOGetter(setter).equals(stringMayuscula(tipo))
							||(obtenerAtributoDelSetterOGetter(setter).equals("Int")&&tipo.equals("Integer")))
					{
						setter.invoke(pstm,i+1,args[i]);
						i++;
						break;
					}
				}

			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

	}

	public <T> List<T> obtenerObjetosDeBD(Class dtoClass, String query, Object[] args, String xql)
	{

		Connection con=null;
		PreparedStatement pstm=null;
		ResultSet rs=null;
		try
		{
			con=SingletonConexion.getConnection();
			String sql=query;
			pstm=con.prepareStatement(sql);
			agregarCondicion(xql,pstm,args,dtoClass);
			// pstm.setString(1,(String)args[0]);
			rs=pstm.executeQuery();

			Constructor<?> constructor=obtenerConstructor(dtoClass);
			Object objeto=null;

			List<T> listaObjetos=new ArrayList<T>();
			while(rs.next())
			{
				objeto=constructor.newInstance();
				settearValoresAObjeto(dtoClass,objeto,rs,listaObjetos);
				/*
				 * Method
				 * getterId=objeto.getClass().getMethod("getIdPersona",null);
				 * System.out.println(getterId.invoke(objeto,null)); Method
				 * getterNombre=objeto.getClass().getMethod("getNombre",null);
				 * System.out.println(getterNombre.invoke(objeto,null));
				 */
				listaObjetos.add((T)objeto);
			}
			return listaObjetos;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		finally
		{
			try
			{
				if(rs!=null) rs.close();
				if(pstm!=null) pstm.close();

			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}

		}
	}

}
