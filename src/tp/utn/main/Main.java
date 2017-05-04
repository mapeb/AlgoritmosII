package tp.utn.main;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import tp.utn.Utn;
import tp.utn.demo.domain.*;

public class Main {
	
	public static void main(String[] args){
		
		String query = Utn._query(Persona.class, "where nombre = ?");
		
		System.out.println(query);
		
	
	
}

}
