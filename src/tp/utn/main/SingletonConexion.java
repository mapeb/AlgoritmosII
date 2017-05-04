package tp.utn.main;

import java.sql.DriverManager;
import java.sql.Connection;
import java.util.ResourceBundle;

public class SingletonConexion
{
	private static Connection con = null;
	
	public static Connection getConnection()
	{
		try
		{
			if(con==null)
			{
				Runtime.getRuntime().addShutdownHook(new ShutDownJuano());
				ResourceBundle rb = ResourceBundle.getBundle("jdbc");
				String driver = rb.getString("driver");
				String url = rb.getString("url");
				String pwd = rb.getString("pwd");
				String usr = rb.getString("usr");
				
				Class.forName(driver);
				con = DriverManager.getConnection(url,usr,pwd);
			}
			
			return con;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException("Error al crear conexion, loco",ex);
		}
	}
	
	static class ShutDownJuano extends Thread
	{
		public void run()
		{
			try
			{
				Connection con = SingletonConexion.getConnection();
				con.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		}
		
	}
}
