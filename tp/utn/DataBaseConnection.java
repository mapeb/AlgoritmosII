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

public class DataBaseConnection extends Xql
{

	Connection connection;


	public DataBaseConnection(Connection connection)
	{
		super();
		this.connection=connection;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public <T> List<T> getObjetosDeBD(Class<?> dtoClass, String query, Object[] args, String xql)
	{
		PreparedStatement pstm=null;
		ResultSet rs=null;
		Interceptor.intercept(dtoClass);

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
			if(listaObjetos.size()==0)
				return null;
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




	public Method buscarSetterHijoAPadre(Object hijo, Object padre) // Confio en
																	// ti
																	// hermano.
	{
		Method metodosPadre[]=padre.getClass().getDeclaredMethods();
		ArrayList<Method> settersPadre=Reflection.getGettersSetters(metodosPadre,"set");
		for(Method setter:settersPadre)
		{
			if(setter.getName().substring(3).equals(hijo.getClass().getSimpleName())) return setter;
		}
		return null;
	}

	// SETEA LAS VARIABLES USANDO REFLECTION EN EL RESULT SET PARA TENER LOS
	// GETTERS PRIMITIVOS
	// SI CAMPO A SETTEAR NO ES PRIMITIVO, HACE RECURSIVIDAD SOBRE EL SETTEO DE
	// OBJETOS
	// AL SER UNA CLASE Y UNA TABLA DIFERENTE Y LE SETTEA A LA CLASE PRINCIPAL
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
					// listaObjetos.add((T)objetoCampo);

				}
				else
				{
					Method[] metodosStatement=rs.getClass().getDeclaredMethods();
					for(Method metodo:metodosStatement)
					{
						String tipoClase;
						String tipoClaseMin;
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
		return Annotation.getTableName(dtoClass)+"."+Annotation.getAnnotationFieldName(campo);
	}



	

	// OBTIENE LAS VARIABLES DE LA CLASE RELACIONADAS AL WHERE. EN EL CASO DE
	// QUE LA VARIABLE
	// SEA DE OTRA TABLA, SE OBTIENE ESA SEGUNDA TABLA Y SE VERIFICA QUE ESTE EL
	// CAMPO BUSCADO.
	public <T> ArrayList<String> getVariablesDelWhere(Class<T> dtoClass)
	{
		ArrayList<String> variables=new ArrayList<String>();
		for(String atributo:variablesXql)
		{
			if(stringMayuscula(getClaseDe(atributo)).equals(dtoClass.getSimpleName()))
			{
				atributo=getAtributoSinNombreClase(atributo);
				for(Field campo:dtoClass.getDeclaredFields())
				{
					if(campo.getName().equals(atributo))
					{
						variables.add(campo.getName());
						break;
					}
				}
			}
			else
			{
				int i=0;
				for(Field campito:dtoClass.getDeclaredFields())
				{
					if(!Reflection.isPrimitiveClass(campito)&&stringMayuscula(getClaseDe(atributo)).equals(campito.getType().getSimpleName()))
					{
						atributo=getAtributoSinNombreClase(atributo);
						for(Field campoSegunda:campito.getType().getDeclaredFields())
						{
							if(campoSegunda.getName().equals(atributo))
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

	// BUSCA LOS SETTERS DE CADA CAMPO Y LOS INVOCA CON REFLECTION AL ENCONTRAR
	// EL CAMPO
	// PERTINENTE
	public <T> void settearValoresAObjeto(Class dtoClass, Object objeto, ResultSet rs, List<T> listaObjetos)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,SQLException
	{
		ArrayList<Method> setters=Reflection.getGettersSetters(dtoClass.getDeclaredMethods(),"set");
		for(Method setter:setters)
		{
			String atributoSetter=Reflection.getAtributoDelSetterOGetter(setter);
			atributoSetter=stringMinuscula(atributoSetter);
			for(Field campo:dtoClass.getDeclaredFields())
			{
				if(campo.getName().equals(atributoSetter)&&Annotation.getAnnotationFieldName(campo)!=null)
				{
					String nombreEnTabla=nombreAtributoEnTabla(dtoClass,campo);
					settearSobreObjeto(rs,campo,nombreEnTabla,setter,objeto,listaObjetos);
				}
			}

		}
	}

	// BUSCA EN LOS DISTINTOS ATRIBUTOS DE LA CLASE AQUELLOS QUE NO SON
	// PRIMITIVOS Y REPRESENTAN
	// UNA TABLA, PARA LUEGO BUSCAR EL CAMPO INVOLUCRADO EN EL WHERE EN ESA
	// TABLA Y DEVOLVER EL TIPO DE DATO
	public <T> String obtenerTipoCampoDeOtraClase(Class<T> dtoClass, String campo) throws NoSuchFieldException,SecurityException
	{
		Field[] camposClasePrincipal=dtoClass.getDeclaredFields();
		for(Field campoClaseP:camposClasePrincipal)
		{
			if(!Reflection.isPrimitiveClass(campoClaseP))
			{
				Field campoSec=campoClaseP.getType().getDeclaredField(campo); 
				if(campoSec!=null)
					return campoSec.getType().getSimpleName();
			}
		}
		return null;

	}

	public void settearVariablesALaQuery(ArrayList<String> variablesDelWhere, Class dtoClass, PreparedStatement pstm, Object[] args)
			throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,NoSuchFieldException,SecurityException
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
				// VERIFICA SI LA VARIABLE DEL WHERE ES DE LA CLASE O DE UNA
				// CLASE QUE TIENE
				// COMO ATRIBUTO. SI NO ES DE ESTA CLASE, CATCHEA EXCEPCION.
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
				String attr=Reflection.getAtributoDelSetterOGetter(setter);
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

	// OBTIENE LAS VARIABLES IMPLICADAS EN EL WHERE Y LAS SETEA EN LA QUERY A
	// TRAVES DEL PSTM
	public <T> void agregarCondicion(String xql, PreparedStatement pstm, Object[] args, Class dtoClass)
			throws NoSuchFieldException,SecurityException,IllegalAccessException,IllegalArgumentException,InvocationTargetException
	{
		try
		{
			//setVariablesXql(xql);
			settearVariablesALaQuery(getVariablesDelWhere(dtoClass),dtoClass,pstm,args);

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

	}
}
