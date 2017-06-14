
package tp.utn;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Iterator;
import java.util.List;

// import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import tp.utn.ann.Column;
import tp.utn.ann.Id;
import tp.utn.ann.Table;
//import tp.utn.main.SingletonConexion;


public class Query extends Xql
{
	List<String> select;
	String from;
	private static String condicionOrdenada;


	public Query(String from)
	{
		super();
		select=new ArrayList<String>();
		this.from=from;

	}

	public void generarQuery(Field[] campos, Class<?> dtoClass)
	{
		for(Field campo:campos)
		{
			if(campo.getAnnotation(Column.class)!=null&&Reflection.isPrimitiveClass(campo))
				this.addAttr(dtoClass,campo.getAnnotation(Column.class).name());
			else
			{
				if(campo.getType().getAnnotation(Table.class)!=null)
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

public String cambiarAtributoPorNombreEnTabla(Field campo, Class dtoClass, String atributo)
{
	
	String[] modificacion = condicionOrdenada.split(" ");
	String nombreEnTabla = Annotation.getAnnotationFieldName(campo);
	String reemplazo = "$"+stringMinuscula(dtoClass.getSimpleName())+"."+nombreEnTabla;
	String aModificar = null;
	for(String modif : modificacion)
	{
		if(modif.equals("$"+atributo))
		{
		 aModificar = modif;
		break;
		}
	}
	return condicionOrdenada = condicionOrdenada.replace(aModificar,reemplazo);
}
	public String modificarAtributosClaseAFilasTabla(String xql, Class dtoClass)
	{
		setVariablesXql(xql);
		condicionOrdenada = xql;
		ArrayList<String> variables=new ArrayList<String>();
		String modificacion=xql;
		for(String atributo : variablesXql)
		{
			
			if(stringMayuscula(getClaseDe(atributo)).equals(dtoClass.getSimpleName()))
			{
				for(Field campo:dtoClass.getDeclaredFields())
				{
					if(campo.getName().equals(getAtributoSinNombreClase(atributo))
							&& !(Annotation.getAnnotationFieldName(campo).equals(getAtributoSinNombreClase(atributo))))
					{
						modificacion = cambiarAtributoPorNombreEnTabla(campo,dtoClass, atributo);
					}
				}
			}
			else
			{
				int i=0;
				for(Field campito:dtoClass.getDeclaredFields())
				{
					
					if((!Reflection.isPrimitiveClass(campito))&&stringMayuscula(getClaseDe(atributo)).equals(campito.getType().getSimpleName()))
					{
						
						for(Field campoSegunda:campito.getType().getDeclaredFields())
						{
							if(campoSegunda.getName().equals(getAtributoSinNombreClase(atributo))
									&& !(Annotation.getAnnotationFieldName(campoSegunda).equals(getAtributoSinNombreClase(atributo))))
							{
								modificacion = cambiarAtributoPorNombreEnTabla(campoSegunda,dtoClass, atributo);
								i=1;
								break;
							}
						}
					}
					if(i==1) break;
				}
			}
		}
		return modificacion;
	}
	public String getAtributosRealesDeTabla(String xql, Class dtoClass)
	{
		String xqlConFilasDeTabla = modificarAtributosClaseAFilasTabla(xql, dtoClass);
		return sacarPesos(xqlConFilasDeTabla);
		
	}
	public String generarString(String xql, Class dtoClass)
	{
		String xqlFinal="";
		if(!xql.equals(""))
		xqlFinal =getAtributosRealesDeTabla(xql, dtoClass);
		String q="SELECT ";
		for(String attr:this.getSelect())
			q+=attr+",";
		q=q.substring(0,q.length()-1);
		q+=" FROM "+from+" "+xqlFinal;
		return q;
	}

	public void addAttr(Class<?> claseContenedora, String atrr)
	{
		String newAtrr=Annotation.getTableName(claseContenedora)+"."+atrr;
		this.getSelect().add(newAtrr);
	}

	public void addJoin(Class<?> claseRaiz, Field campo)
	{
		// Table tabla2 = (Table) clase2.getAnnotation(Table.class);
		from+=" JOIN "+Annotation.getTableName(campo.getType())+joinDeTablas(claseRaiz,campo);
	}

	private String joinDeTablas(Class<?> campoSolicitante, Field campoSolicitado)
	{
		String comparacion=" ON ( "+Annotation.getTableName(campoSolicitante);
		comparacion+="."+campoSolicitado.getAnnotation(Column.class).name()+" = ";
		comparacion+=Annotation.getTableName(campoSolicitado.getType())+".";

		for(Field field:campoSolicitado.getType().getDeclaredFields())
		{
			if(field.getAnnotation(Id.class)!=null) return comparacion+field.getAnnotation(Column.class).name()+" )";
		}
		return ""; // Hola si la funcion no tiene id debo explotar, ya se .
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


}
