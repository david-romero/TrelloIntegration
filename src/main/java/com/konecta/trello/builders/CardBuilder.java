package com.konecta.trello.builders;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.trello4j.TrelloObjectFactoryImpl;
import org.trello4j.TrelloURL;
import org.trello4j.model.Card;

import com.google.gson.reflect.TypeToken;
import com.konecta.trello.CustomTrelloURL;
import com.konecta.trello.http.HttpCall;

@Component
public class CardBuilder {

	@Value("${trello.api.key}")
	private String apiKey;
	
	@Value("${trello.api.secret}")
	private String apiSecret;
	
	private TrelloObjectFactoryImpl trelloObjFactory = new TrelloObjectFactoryImpl();
	
	protected HttpCall call = new HttpCall();
	
	public Card addLabel(Card card,String labelId){
		final String url = TrelloURL
				.create(apiKey, CustomTrelloURL.CARD_URL_ADD_LABEL, card.getId() )
				.token(apiSecret)
				.build();
		Map<String,String> nuevaEtiqueta = new HashMap<String, String>(1);
		nuevaEtiqueta.put("value", labelId);
		return trelloObjFactory.createObject(new TypeToken<Card>() {
		}, call.doPost(url, nuevaEtiqueta));
	}

	public Card addMember(Card card, String member) {
		final String url = TrelloURL
				.create(apiKey, CustomTrelloURL.CARD_URL_ADD_MEMBER, card.getId() )
				.token(apiSecret)
				.build();
		Map<String,String> nuevoMember = new HashMap<String, String>(1);
		nuevoMember.put("value", member);
		return trelloObjFactory.createObject(new TypeToken<Card>() {
		}, call.doPost(url, nuevoMember));
	}

	public Card addComment(Card card, String comment) {
		final String url = TrelloURL
				.create(apiKey, CustomTrelloURL.CARD_URL_ADD_COMMENT, card.getId() )
				.token(apiSecret)
				.build();
		Map<String,String> nuevoComment = new HashMap<String, String>(1);
		nuevoComment.put("text", comment);
		return trelloObjFactory.createObject(new TypeToken<Card>() {
		}, call.doPost(url, nuevoComment));
	}

	public Card update(Card card) {
		final String url = TrelloURL
				.create(apiKey, CustomTrelloURL.CARD_URL, card.getId() )
				.token(apiSecret)
				.build();
		Map<String,String> nuevosDatos = new HashMap<String, String>(1);
		nuevosDatos.put("name", card.getName());
		nuevosDatos.put("desc", card.getDesc());
		return trelloObjFactory.createObject(new TypeToken<Card>() {
		}, call.doPut(url, nuevosDatos));
	}
	
}
