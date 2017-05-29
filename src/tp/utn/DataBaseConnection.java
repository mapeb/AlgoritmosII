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
import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Table;

public class DataBaseConnection {
	
	Connection connection;
	ArrayList<String> variablesDeXql=new ArrayList<String>();
	
	public DataBaseConnection(Connection connection) {
		super();
		this.connection = connection;
	}
	
	public Connection getConnection() {
		return connection;
	}



	public <T> List<T> getObjetosDeBD(Class<?> dtoClass, String query, Object[] args, String xql)
	{
		PreparedStatement pstm=null;
		ResultSet rs=null;
		try
		{
			pstm=this.getConnection().prepareStatement(query);
			agregarCondicion(xql,pstm,args,dtoClass);
			rs=pstm.executeQuery();

			List<T> listaObjetos=new ArrayList<T>();
			while(rs.next())
			{
				Object objeto=Reflection.getConstructor(dtoClass).newInstance();
				settearValoresAObjeto(dtoClass,objeto,rs,listaObjetos);
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

	public String stringMinuscula(String palabra)
	{
		return (palabra.substring(0,1).toLowerCase()+palabra.substring(1));

	}

	public String stringMayuscula(String palabra)
	{
		return (palabra.substring(0,1).toUpperCase()+palabra.substring(1));
	}

	public Method buscarSetterHijoAPadre(Object hijo, Object padre) // Confio en ti hermano.
	{
		Method metodosPadre[]=padre.getClass().getDeclaredMethods();
		ArrayList<Method> settersPadre=Reflection.getGettersSetters(metodosPadre,"set");
		for(Method setter:settersPadre)
		{
			if(setter.getName().substring(3).equals(hijo.getClass().getSimpleName())) return setter;
		}
		return null;
	}

	//SETEA LAS VARIABLES USANDO REFLECTION EN EL RESULT SET PARA TENER LOS GETTERS PRIMITIVOS
	//SI CAMPO A SETTEAR NO ES PRIMITIVO, HACE RECURSIVIDAD SOBRE EL SETTEO DE OBJETOS 
	//AL SER UNA CLASE Y UNA TABLA DIFERENTE Y LE SETTEA A LA CLASE PRINCIPAL 
	// LA CLASE SECUNDARIA EN SU ATRIBUTO
	public <T> void settearSobreObjeto(ResultSet rs, Field campo, String nombreEnTabla, Method setter, Object objeto, List<T> listaObjetos)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,SQLException
	{
		Class type=campo.getType();
		try
		{
			if(type==int.class||type==Integer.class) setter.invoke(objeto,rs.getInt(nombreEnTabla));

			else
			{
				if(!Reflection.isPrimitiveClass(campo))
				{
					Object objetoCampo=Reflection.getConstructor(campo.getType()).newInstance();
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
						if(Reflection.esUnSetterOGetter(metodo,"get"))
						{
							tipoClase=metodo.getName().substring(3);
							tipoClaseMin=stringMinuscula(tipoClase);
							if(type.getSimpleName().equals(tipoClase)||type.getSimpleName().equals(tipoClaseMin))
							{
								Class parametros[]=metodo.getParameterTypes();
								if(parametros.length==1&&parametros[0].getSimpleName().equals(type.getSimpleName())) setter.invoke(objeto,metodo.invoke(rs,nombreEnTabla));
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
		return Query.nombreTabla(dtoClass)+"."+campo.getAnnotation(Column.class).name();
	}

	public String getTabla(String anotacionSQL)// ej: persona.id_persona
	{
		if(anotacionSQL.contains("."))
		{
			String[] division=anotacionSQL.split("\\.");
			return division[0];
		}
		return "";
	}

	public String sacarTabla(String anotacionSQL)
	{
		if(anotacionSQL.contains("."))
		{

			String[] division=anotacionSQL.split("\\.");
			return division[1];
		}
		return "";
	}
	
	//OBTIENE LAS VARIABLES DE LA CLASE RELACIONADAS AL WHERE. EN EL CASO DE QUE LA VARIABLE
	//SEA DE OTRA TABLA, SE OBTIENE ESA SEGUNDA TABLA Y SE VERIFICA QUE ESTE EL CAMPO BUSCADO.
	public <T> ArrayList<String> obtenerVariablesDesdeAnotaciones(Class<T> dtoClass)
	{
		ArrayList<String> variables=new ArrayList<String>();
		Field[] campos=dtoClass.getDeclaredFields();
		ArrayList<String> anotacionesDeOtraClase=new ArrayList<String>();
		for(String anotacion:variablesDeXql)
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
					if(!Reflection.isPrimitiveClass(campito) && getTabla(anotacion).equals(campito.getType().getAnnotation(Table.class).name()))
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

	// BUSCA LOS SETTERS DE CADA CAMPO Y LOS INVOCA CON REFLECTION AL ENCONTRAR EL CAMPO 
	// PERTINENTE
	public <T> void settearValoresAObjeto(Class dtoClass, Object objeto, ResultSet rs, List<T> listaObjetos)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,SQLException
	{
		Field[] campos=dtoClass.getDeclaredFields();
		Method metodos[]=dtoClass.getDeclaredMethods();
		ArrayList<Method> setters=Reflection.getGettersSetters(metodos,"set");

		for(Method setter:setters)
		{
			String atributoSetter=Reflection.getAtributoDelSetterOGetter(setter);
			atributoSetter=stringMinuscula(atributoSetter);
			for(Field campo:campos)
			{
				if(campo.getName().equals(atributoSetter)&&campo.getAnnotation(Column.class)!=null)
				{
					String nombreEnTabla=nombreAtributoEnTabla(dtoClass,campo);
					settearSobreObjeto(rs,campo,nombreEnTabla,setter,objeto,listaObjetos);
				}
			}

		}
	}
	// BUSCA EN LOS DISTINTOS ATRIBUTOS DE LA CLASE AQUELLOS QUE NO SON PRIMITIVOS Y REPRESENTAN
	// UNA TABLA, PARA LUEGO BUSCAR EL CAMPO INVOLUCRADO EN EL WHERE EN ESA TABLA Y DEVOLVER EL TIPO DE DATO
	public <T> String obtenerTipoCampoDeOtraClase(Class<T> dtoClass, String campo) throws NoSuchFieldException,SecurityException
	{
		Field[] camposClasePrincipal=dtoClass.getDeclaredFields();
		for(Field campoClaseP:camposClasePrincipal)
		{
			if(!Reflection.isPrimitiveClass(campoClaseP))
			{
				if(campoClaseP.getType().getDeclaredField(campo)!=null)
				{
					Field campoSec=campoClaseP.getType().getDeclaredField(campo); // 2 veces getType (?)
					return campoSec.getType().getSimpleName();
				}
			}
		}
		return null;

	}

	public void obtenerVariablesDeXql(String xql)
	{
		String[] palabras=xql.split(" ");
		for(String palabra:palabras)
		{
			if(palabra.substring(0,1).equals("$"))
			{
				palabra=palabra.substring(1);
				variablesDeXql.add(palabra);
			}
		}
	}
	public void settearVariablesALaQuery(ArrayList<String> variablesDelWhere, Class dtoClass, PreparedStatement pstm, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, SecurityException
	{
		
		Method[] metodos=pstm.getClass().getDeclaredMethods();
		ArrayList<Method> setters=Reflection.getGettersSetters(metodos,"set");
		
		String tipo=null;
		int i=0;
		boolean esVariableDeLaClase=true;
		
		for(String variable:variablesDelWhere)
		{
			try
			{
				//VERIFICA SI LA VARIABLE DEL WHERE ES DE LA CLASE O DE UNA CLASE QUE TIENE
				//COMO ATRIBUTO. SI NO ES DE ESTA CLASE, CATCHEA EXCEPCION.
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
				String attr = Reflection.getAtributoDelSetterOGetter(setter);
				if(attr.equals(tipo)||attr.equals(stringMayuscula(tipo))||(attr.equals("Int")&&tipo.equals("Integer")))
				{
					// SETEA SEGUN EL SETTER PERTINENTE DEL PSTM
					setter.invoke(pstm,i+1,args[i]);
					i++;
					break;
				}
			}

		}
	}
	//OBTIENE LAS VARIABLES IMPLICADAS EN EL WHERE Y LAS SETEA EN LA QUERY A TRAVES DEL PSTM
	public <T> void agregarCondicion(String xql, PreparedStatement pstm, Object[] args, Class dtoClass)
			throws NoSuchFieldException,SecurityException,IllegalAccessException,IllegalArgumentException,InvocationTargetException
	{
		try
		{
			obtenerVariablesDeXql(xql);
			settearVariablesALaQuery(obtenerVariablesDesdeAnotaciones(dtoClass), dtoClass, pstm, args);
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

	}
}
