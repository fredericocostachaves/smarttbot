package br.com.analiserobos.smarttbot;

import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.opencsv.CSVReader;

import br.com.analiserobos.smarttbot.model.DiaOperacao;

@SpringBootApplication
public class SmarttbotApplication {
	private static final Logger logger = Logger.getLogger(SmarttbotApplication.class.getName());  

	public static void main(String[] args) {
		SpringApplication.run(SmarttbotApplication.class, args);
		try (CSVReader reader = new CSVReader(new FileReader("C:\\Users\\fcost\\git\\smarttbot\\src\\main\\resources\\resultados\\Johannes_Brahms.csv"))) {			
			String[] nextLine;
			boolean primeiraLinha = true;
			List<DiaOperacao> diasOperacao = new ArrayList<>();
			logger.info("Iniciando Análise");
			//CSV: 6821290961";"17/09/2018 / 10:08:32";"WINV18";"V";"2";"75.575,00";"executada";"entrada";"2";"75.580,00";"-";"-";"-
			while ((nextLine = reader.readNext()) != null) {
				for (String token : nextLine) {
					if (primeiraLinha) {
						primeiraLinha = false;
					} else {
						DiaOperacao diaOperacao = null;
						String[] linha = token.split(";");
						String linhaSTR = linha[1].substring(7,11) + '-' + linha[1].substring(4,6) + '-' + linha[1].substring(1, 3);						
						LocalDate diaLinha = LocalDate.parse(linhaSTR);						
						Optional<DiaOperacao> diaOptional = diasOperacao.stream().filter(o -> o.getData().equals(diaLinha)).findFirst();
						if (diaOptional.isEmpty()) {
							diaOperacao = new DiaOperacao();
							diaOperacao.setData(diaLinha);
							diasOperacao.add(diaOperacao);
						} else {
							diaOperacao = diaOptional.get();
						}						
						if (!linha[12].equals("\"-")) {
							String resultadoSTR = linha[12].replace(".", "");
							resultadoSTR = resultadoSTR.replace(",", ".").replace("\"", ""); 
							Double resultadoLinha = Double.parseDouble(resultadoSTR);
							diaOperacao.setResultado(diaOperacao.getResultado()+resultadoLinha);
						}
					}
				}				
			}
			int diasGainsConsecutivos = 0;
			int diasLossConsecutivos = 0;
			Double mediaGainsConsecutivos = 0.0;
			Double mediaLossConsecutivos = 0.0;
			DiaOperacao diaAnterior = new DiaOperacao();
			Collections.reverse(diasOperacao);
			for(DiaOperacao dia: diasOperacao) {				
				logger.info(dia.toString());
				if (dia.getResultado() > 0) {
					if (diaAnterior.getResultado() < 0) {
						mediaGainsConsecutivos = (mediaGainsConsecutivos + diasGainsConsecutivos)/2;
						diasGainsConsecutivos = 0;
					}
					diasGainsConsecutivos++;
				} else {
					if (diaAnterior.getResultado() > 0) {
						mediaLossConsecutivos = (mediaLossConsecutivos + diasLossConsecutivos) /2;
						diasLossConsecutivos = 0;
					}
					diasLossConsecutivos++;
				}
				diaAnterior = dia;
			}
			logger.info("Média de gains consecutivos: "+mediaGainsConsecutivos);
			logger.info("Média de loss consecutivos: "+mediaLossConsecutivos);			
			logger.info("Fim Análise");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}