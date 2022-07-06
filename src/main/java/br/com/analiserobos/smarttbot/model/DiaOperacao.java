package br.com.analiserobos.smarttbot.model;

import java.time.LocalDate;

/**
 * 
 * @author fcost
 *
 */
public class DiaOperacao {
	private LocalDate data;
	private Double resultado = 0.0;

	public LocalDate getData() {
		return data;
	}

	public void setData(LocalDate data) {
		this.data = data;
	}

	public Double getResultado() {
		return resultado;
	}

	public void setResultado(Double resultado) {
		this.resultado = resultado;
	}

	@Override
	public String toString() {
		return "DiaOperacao [data=" + data + ", resultado=" + resultado + "]";
	}
	
}
