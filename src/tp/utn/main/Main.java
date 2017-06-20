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

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import tp.utn.Utn;
import tp.utn.ann.Column;
import tp.utn.ann.Table;
import tp.utn.demo.domain.*;

public class Main
{

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException
	{
			Connection con = SingletonConexion.getConnection();
			List<Persona> lista = (List<Persona>) Utn.query(con,Persona.class,"","");
			
			for(Persona per : lista)
			{
				System.out.println(per.direccion);
				//Puse que el fetchType de Nombre sea 1 (Lazy) asi que nunca lo va a agarrar
				System.out.println("Nombre : " + per.getNombre());
				System.out.println("Direccion : " + per.getDireccion());
				System.out.print("Ocupacion : " + per.getOcupacion().getDescripcion());
				System.out.println(" Descripcion : " + per.getOcupacion().getTipoOcupacion().getDescripcion());
			
			}
			Persona personaFound = Utn.find(con,Persona.class,1);
			
			System.out.println("La persona es " + personaFound.getNombre());

	} 

}
