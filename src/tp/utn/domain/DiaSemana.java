package tp.utn.domain;

import java.sql.Date;
import java.util.List;

import tp.utn.ann.Column;
import tp.utn.ann.Gui;
import tp.utn.ann.Id;
import tp.utn.ann.Relation;
import tp.utn.ann.Table;

@Table(name="x_dia_semana")
public class DiaSemana
{

	@Id(strategy=Id.IDENTITY)
	@Column(name="id_dia_semana")
	private Integer idDiaSemana;
	
	@Column(name="flg_laborable")
	private Integer flgLaborable;
	
	@Column(name="descripcion")
	private String descripcion;

	public Integer getIdDiaSemana()
	{
		return idDiaSemana;
	}

	public void setIdDiaSemana(Integer idDiaSemana)
	{
		this.idDiaSemana = idDiaSemana;
	}

	public Integer getFlgLaborable()
	{
		return flgLaborable;
	}

	public void setFlgLaborable(Integer flgLaborable)
	{
		this.flgLaborable = flgLaborable;
	}

	public String getDescripcion()
	{
		return descripcion;
	}

	public void setDescripcion(String descripcion)
	{
		this.descripcion = descripcion;
	}
	

}
