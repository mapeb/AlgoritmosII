package tp.utn.main;

import java.sql.DriverManager;
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

import tp.utn.Utn;
import tp.utn.ann.Column;
import tp.utn.ann.Table;
import tp.utn.demo.domain.*;

public class Main
{


	public static <T> void main(String[] args) throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{

		 String query = Utn._query(Persona.class,"where nombre = ?");
		 System.out.println(query);
		 
		List<T> lista = (List<T>) Utn.query(null,Persona.class,"where persona.nombre = ?","Juani");
		
		/*System.out.println(lista.size());
		Method metodo = lista.get(0).getClass().getMethod("getNombre",null);
		Object objeto = lista.get(0);
		System.out.println(metodo.invoke(objeto,null));*/

	}

}
