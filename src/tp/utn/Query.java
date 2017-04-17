package tp.utn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Id;
import tp.utn.ann.Table;

public class Query {
	List<String> select;
	String from;
	
	public Query(String from) {
		super();
		select = new ArrayList<String>();
		this.from = from;
	}
	
	public void generarQuery(Field[] campos, Class dtoClass) {
		for (Field campo : campos) {
			if (campo.getAnnotation(Column.class) != null && isPrimiteClass(campo))
				this.addAttr(campo.getAnnotation(Column.class).name());
			else {
				Class clase = campo.getType();
				if (campo.getType().getAnnotation(Table.class) != null) {
					this.addJoin(dtoClass, campo.getType());
					generarQuery(campo.getType().getDeclaredFields(), campo.getType());
				}
			}
		}
	}
	
	public String generarString(String xql) {
		String q = "Select ";
		for(String attr:this.getSelect())
			q+=attr + ",";
		q=q.substring(0,q.length()-1);
		q+=" FROM "+ from + xql;
		return q;
	}

	
	private static boolean isPrimiteClass(Field field) {
		Class type = field.getType();
		if ((type == int.class) || (type == Integer.class) || (type == String.class) || (type == char.class)
				|| (type == double.class) || (type == long.class) || (type == short.class) || (type == boolean.class))
			return true;
		return false;
	}

	public void addAttr(String atrr){
		this.getSelect().add(atrr);
	}
	public void addJoin(Class table,Class table2){ 
		Table tabla2 = (Table) table2.getAnnotation(Table.class);
		from+=" JOIN " + tabla2.name() + " ON (" + identificador(table) + "=" + identificador(table2) + ")";
	}
	
	private String identificador(Class table) {
		for (Field field: table.getDeclaredFields()) {
			if(field.getAnnotation(Id.class) != null)
				return field.getAnnotation(Column.class).name();
		}
		return ""; // Hola si la funcion no tiene id debo explotar, ya se .
	}
	
	public List<String> getSelect() {
		return select;
	}

	public void setSelect(List<String> select) {
		this.select = select;
	}
	
	public void setFrom(String n) {
		this.from = n;
	}	

}
