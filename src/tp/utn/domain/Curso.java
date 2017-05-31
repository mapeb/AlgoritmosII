package tp.utn.domain;

import tp.utn.ann.Column;
import tp.utn.ann.Id;
import tp.utn.ann.Table;

@Table(name="x_curso")
public class Curso
{
	@Id(strategy=Id.IDENTITY)
	@Column(name="id_curso")
	private Integer idCurso;
	
	@Column(name="id_dia_semana")
	private DiaSemana diaSemana;
	
	@Column(name="id_turno")
	private Turno turno;
	
	@Column(name="codigo")
	private String codigo;
	
	@Column(name="id_docente")
	private Docente docente;
	
	@Column(name="id_materia")
	private Materia materia;
	
	@Column(name="id_carrera")
	private Carrera carrera;

	public Integer getIdCurso()
	{
		return idCurso;
	}
	public DiaSemana getDiaSemana()
	{
		return diaSemana;
	}

	public void setDiaSemana(DiaSemana diaSemana)
	{
		this.diaSemana = diaSemana;
	}

	public Turno getTurno()
	{
		return turno;
	}

	public void setTurno(Turno turno)
	{
		this.turno = turno;
	}

	public Docente getDocente()
	{
		return docente;
	}

	public void setDocente(Docente docente)
	{
		this.docente = docente;
	}

	public Materia getMateria()
	{
		return materia;
	}

	public void setMateria(Materia materia)
	{
		this.materia = materia;
	}

	public Carrera getCarrera()
	{
		return carrera;
	}

	public void setCarrera(Carrera carrera)
	{
		this.carrera = carrera;
	}

	public String getCodigo()
	{
		return codigo;
	}

	public void setCodigo(String codigo)
	{
		this.codigo = codigo;
	}
	
	

}
