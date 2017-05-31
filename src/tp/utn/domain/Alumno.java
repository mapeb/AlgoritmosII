package tp.utn.domain;

import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Gui;
import tp.utn.ann.Id;
import tp.utn.ann.Relation;
import tp.utn.ann.Table;

@Table(name="x_alumno")
public class Alumno
{

	@Id(strategy=Id.IDENTITY)
	@Column(name="id_alumno")
	private Integer idAlumno;
	
	@Column(name="nombre")
	private String nombre;
	
	@Column(name="legajo")
	private int legajo;
	

	public void setIdAlumno(Integer unId)
	{
		this.idAlumno = unId;
	}
	public void setNombre(String unNombre)
	{
		this.nombre = unNombre;
	}
	public void setLegajo(int unLegajo)
	{
		this.legajo = unLegajo;
	}
	public Integer getIdAlumno()
	{
		return idAlumno;
	}
	public String getNombre()
	{
		return nombre;
	}
	public int getLegajo()
	{
		return legajo;
	}
	
	
}
