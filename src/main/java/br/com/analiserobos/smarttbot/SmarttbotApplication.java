package br.com.analiserobos.smarttbot;

import java.io.FileReader;
import java.io.IOException;
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
		String nomeArquivo = "Ouro Preto";
		logger.info("Iniciando Análise");
		try (CSVReader reader = new CSVReader(new FileReader("C:\\Users\\fcost\\git\\smarttbot\\src\\main\\resources\\resultados\\"+nomeArquivo+".csv"))) {									
			Integer diasGainsConsecutivos = 0;
			Integer diasLossConsecutivos = 0;
			List<Integer> mediaGainsConsecutivos = new ArrayList<>();
			List<Integer> mediaLossConsecutivos = new ArrayList<>();
			DiaOperacao diaAnterior = new DiaOperacao();
			List<DiaOperacao> diasOperacao = processaOperacoes(reader);
			for(DiaOperacao dia: diasOperacao) {
				String diaSTR = dia.toString();
				if (dia.getResultado() > 0) {
					if (diaAnterior.getResultado() < 0) {
						mediaGainsConsecutivos.add(diasGainsConsecutivos);
						diasGainsConsecutivos = 0;
					}
					diasGainsConsecutivos++;
				} else {
					if (diaAnterior.getResultado() > 0) {
						mediaLossConsecutivos.add(diasLossConsecutivos);
						diasLossConsecutivos = 0;
					}
					diasLossConsecutivos++;
				}
				diaAnterior = dia;
				logger.info(diaSTR);
			}
			Double mediaGains = mediaGainsConsecutivos.stream().mapToInt(val -> val).average().orElse(0.0);
			Double mediaLoss = mediaLossConsecutivos.stream().mapToInt(val -> val).average().orElse(0.0);
			String mediaGainsSTR = "Média de gains consecutivos: "+mediaGains;
			String mediaLossSTR = "Média de loss consecutivos: "+mediaLoss;
			logger.info(nomeArquivo);
			logger.info(mediaLossSTR);
			logger.info(mediaGainsSTR);							
			if (bomPontoMedio(diasOperacao, mediaLoss.intValue(), true) || bomPontoMedio(diasOperacao, mediaGains.intValue(), false)) {
				logger.info("BOM PRA RODAR BASEADO EM MÉDIA SIMPLES");
			} else {
				logger.info("RUIM PRA RODAR BASEADO EM MÉDIA SIMPLES");
			}
			if (recordeLoss(diasLossConsecutivos, mediaLossConsecutivos)) {
				logger.info("ATENÇÃO! RECORDE DE LOSS CONSECUTIVO EM ANDAMENTO");
			}
			logger.info("Fim Análise");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private static boolean recordeLoss(Integer diasLossConsecutivos, List<Integer> mediaLossConsecutivos) {
		return mediaLossConsecutivos.contains(diasLossConsecutivos);
	}

	private static List<DiaOperacao> processaOperacoes(CSVReader reader) throws NumberFormatException, IOException {
		List<DiaOperacao> diasOperacao = processaLinhas(reader);		
		Collections.reverse(diasOperacao);
		return diasOperacao;
	}
	
	private static boolean bomPontoMedio(List<DiaOperacao> diasOperacao, Integer quantidadeConsecutivas, boolean buscandoLoss) {
		Collections.reverse(diasOperacao);
		int qtdeLossConsecutivo = 0;
		int qtdeGainConsecutivo = 0;
		for (int i=0; i<= quantidadeConsecutivas; i++) {
			boolean loss = buscandoLoss && diasOperacao.get(i).getResultado() < 0;
			boolean gain = !buscandoLoss && diasOperacao.get(i).getResultado() > 0;
			if (loss) {
				qtdeLossConsecutivo++;
			} else {
				if (gain) {
					qtdeGainConsecutivo++;
				}
			}
		}
		return qtdeLossConsecutivo > quantidadeConsecutivas || qtdeGainConsecutivo > quantidadeConsecutivas;	
	}	
	
	private static List<DiaOperacao> processaLinhas(CSVReader reader) throws NumberFormatException, IOException {
		//CSV: 6821290961";"17/09/2018 / 10:08:32";"WINV18";"V";"2";"75.575,00";"executada";"entrada";"2";"75.580,00";"-";"-";"-
		String[] nextLine;
		boolean primeiraLinha = true;
		List<DiaOperacao> diasOperacao = new ArrayList<>();
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
		return diasOperacao;
	}
}
