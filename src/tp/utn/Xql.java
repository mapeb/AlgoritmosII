package tp.utn;

import java.util.ArrayList;

public class Xql
{
	protected static ArrayList<String> variablesXqlWhere=new ArrayList<String>();
	protected static ArrayList<String> variablesXqlUpdate = new ArrayList<String>();
	
	
	public String getClaseDe(String anotacionSQL)// ej: persona.id_persona
	{
		if(anotacionSQL.contains("."))
		{
			String[] division=anotacionSQL.split("\\.");
			return division[0];
		}
		return "";
	}
	public static String stringMayuscula(String palabra)
	{
		return (palabra.substring(0,1).toUpperCase()+palabra.substring(1));
	}
	public static String stringMinuscula(String palabra)
	{
		return (palabra.substring(0,1).toLowerCase()+palabra.substring(1));

	}
	public boolean esMasDeUnaClase(String atributo)
	{
		 String[] division = atributo.split("\\.");
		 return division.length > 2;
	}
	public String getSubClaseYAtributo(String claseCompuesta)
	{
		String subClaseYAtributo="";
	String[] clases = claseCompuesta.split("\\.");
	for(int i=1;i<clases.length;i++)
	{
		subClaseYAtributo+=clases[i]+".";
	}
	if(subClaseYAtributo.endsWith("."))
	subClaseYAtributo = subClaseYAtributo.substring(0,subClaseYAtributo.length()-1);
	return subClaseYAtributo;
	}
	public void setVariablesXqlWhere(String xql)
	{
		if(variablesXqlWhere.size()!=0)
			variablesXqlWhere.clear();    // NO ME QUEDO OTRA YA QUE ES VARIABLE DE CLASE PORQUE 
		String[] palabras=xql.split(" "); // TERMINAMOS DIVIENDO LAS COSAS EN SUBCLASES
		for(String palabra:palabras)
		{
			if(palabra.length()!=0 && palabra.substring(0,1).equals("$")) variablesXqlWhere.add(palabra.substring(1));
		}
	}
	public void setVariablesXqlUpdate(String setDeUpdate)
	{
		if(variablesXqlUpdate.size()!=0) 
			variablesXqlUpdate.clear();    // NO ME QUEDO OTRA YA QUE ES VARIABLE DE CLASE PORQUE 
		String[] palabras=setDeUpdate.split(", "); // TERMINAMOS DIVIENDO LAS COSAS EN SUBCLASES
		
		for(String palabra:palabras)
		{
			if(palabra.substring(0,1).equals("$")) 
			variablesXqlUpdate.add(palabra.split(" ")[0].substring(1));
		}
	}
	public String getAtributoSinNombreClase(String anotacionSQL)
	{
		if(anotacionSQL.contains("."))
		{

			String[] division=anotacionSQL.split("\\.");
			return division[1];
		}
		return anotacionSQL;
	}
}
