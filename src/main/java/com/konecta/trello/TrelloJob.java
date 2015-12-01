/**
 * 
 */
package com.konecta.trello;



import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.trello4j.Trello;
import org.trello4j.TrelloImpl;
import org.trello4j.model.Board;
import org.trello4j.model.Card;
import org.trello4j.model.Card.Label;
import org.trello4j.model.Organization;

import com.konecta.trello.builders.CardBuilder;
import com.konecta.trello.builders.LabelBuilder;


/**
 * @author David
 *
 */
public class TrelloJob implements Tasklet {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TrelloJob.class);

	@Value("${trello.api.key}")
	private String apiKey;
	
	@Value("${trello.api.secret}")
	private String apiSecret;
	
	@Value("${trello.api.organization}")
	private String organizationId;
	
	@Value("${trello.api.member}")
	private String member;
	
	@Value("${trello.api.board}")
	private String board;
	
	@Value("${trello.api.list}")
	private String list;
	
	@Value("${trello.api.labels.incidencia}")
	private String labelIncidencia;
	
	@Value("${trello.api.labels.proyecto}")
	private String labelProyecto;
	
	@Autowired
	private LabelBuilder labelBuilder;
	
	@Autowired
	private CardBuilder cardBuilder;
	
	//NOTE: To Get appiSecret
	// https://trello.com/1/authorize?key=[APIKET]&name=TrelloKonecta&expiration=never&response_type=token&scope=read,write

	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1)
			throws Exception {
		LOGGER.debug("run()");
		Trello trelloApi = new TrelloImpl(apiKey, apiSecret);
		Organization org = trelloApi.getOrganization(organizationId);
		List<Organization> org2 = trelloApi.getOrganizationsByMember(member);
		Board b = trelloApi.getBoard(board);
		
		Label etiquetaIncidencia = labelBuilder.getLabel(labelIncidencia);
		Label etiquetaProyecto = labelBuilder.getLabel(labelProyecto);
		
		org.trello4j.model.List toDo = trelloApi.getList(list);
		List<Card> cardsToDo = trelloApi.getCardsByList(list);
		Stream<Card> tarjetasSinProcesar = cardsToDo.parallelStream().filter(card->{
			return card.getLabels().size() == 0 ;
		});
		tarjetasSinProcesar.parallel().forEach(card ->{
			cardBuilder.addLabel(card, labelProyecto);
			cardBuilder.addMember(card, member);
			String title = card.getName();
			int idRedmine = Integer.valueOf(title.substring(title.indexOf("#")+1,title.indexOf("]")).trim());
			Stream<Card> mismaPeticion = obtenerTarjetas(cardsToDo.stream(),idRedmine);
			if ( mismaPeticion.count() <= 1 ){
				//Es nueva
				if ( title.contains("INCIDENCIA") ){
					cardBuilder.addLabel(card, labelIncidencia);
				}
				title = title.substring(title.indexOf("]")+1);
				title = "#".concat(Integer.valueOf(idRedmine).toString()).concat(" ").concat(title);
				card.setName(title);
				String descripcion = card.getDesc();
				int indexPrimerosGuiones = descripcion.indexOf("-");
				int indexBorrarDesde = descripcion.indexOf("* Autor:");
				String temporalParaBorrar = descripcion.substring(indexBorrarDesde);
				int indexBorrarHasta = temporalParaBorrar.indexOf("----------------------------------------");
				temporalParaBorrar = temporalParaBorrar.substring(0,indexBorrarHasta);
				descripcion = descripcion.replace(temporalParaBorrar, "");
				card.setDesc(descripcion);
				cardBuilder.update(card);
			}else{
				//Hay mas de una. Tengo que encontrar la mas vieja y en ella aglutinar todo.
				final Comparator<Card> comp = (p1, p2) -> Double.compare( p1.getPos(), p2.getPos());
				Card firstCard = mismaPeticion.min(comp).get();
				mismaPeticion = mismaPeticion.filter(cardExistente->{ return !cardExistente.getId().equals(firstCard.getId()); } );
				mismaPeticion.parallel().forEach(cardAEliminar->{
					String descripcion = cardAEliminar.getDesc();
					cardBuilder.addComment(card, descripcion);
				});
			}
		});
		return RepeatStatus.FINISHED;
	}
	
	public Stream<Card> obtenerTarjetas(Stream<Card> tarjetas,Integer redmineId){
		return tarjetas.filter(card->{
			return card.getName().contains("#".concat(redmineId.toString()));
		});
	}


}
