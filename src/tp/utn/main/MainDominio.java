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
import tp.utn.domain.Alumno;
import tp.utn.domain.Curso;

public class MainDominio
{
	public static <T> void main(String[] args) throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		
		
		
		 String query = Utn._query(Alumno.class,"where $id_alumno = ?");
		 System.out.println(query);
		 
		List<T> lista = (List<T>) Utn.query(SingletonConexion.getConnection(),Alumno.class,"where $x_alumno.id_alumno = ?", 1);
		
		System.out.println(lista.size());
		int i = 0;
		
		int cantidad = lista.size();
		while(i<cantidad)
		{
			if(lista.get(i).getClass().getSimpleName().equals("Alumno"))
			{
		Method metodo = lista.get(i).getClass().getMethod("getNombre",null);
		Object objeto = lista.get(i);
		System.out.println(metodo.invoke(objeto,null));
		
			}
			i++;
		}
		}
}
