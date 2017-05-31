package tp.utn.domain;

import java.sql.Date;
import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Gui;
import tp.utn.ann.Id;
import tp.utn.ann.Relation;
import tp.utn.ann.Table;

@Table(name="x_docente")
public class Docente
{

	
	@Column(name="id_docente")
	private Integer idDocente;

	@Column(name="nombre")
	private String nombre;
	
	@Column(name="legajo")
	private Integer legajo;

	public Integer getIdDocente()
	{
		return idDocente;
	}

	public void setIdDocente(Integer idDocente)
	{
		this.idDocente = idDocente;
	}

	public String getNombre()
	{
		return nombre;
	}

	public void setNombre(String nombre)
	{
		this.nombre = nombre;
	}

	public Integer getLegajo()
	{
		return legajo;
	}

	public void setLegajo(Integer legajo)
	{
		this.legajo = legajo;
	}
	
	
}
