package com.konecta.trello.builders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.trello4j.TrelloObjectFactoryImpl;
import org.trello4j.TrelloURL;
import org.trello4j.model.Card.Label;

import com.google.gson.reflect.TypeToken;
import com.konecta.trello.CustomTrelloURL;
import com.konecta.trello.http.HttpCall;

@Component
public class LabelBuilder {
	
	@Value("${trello.api.key}")
	private String apiKey;
	
	@Value("${trello.api.secret}")
	private String apiSecret;
	
	private TrelloObjectFactoryImpl trelloObjFactory = new TrelloObjectFactoryImpl();
	
	protected HttpCall call = new HttpCall();
	
	public Label getLabel(String labelId){
		final String url = TrelloURL
				.create(apiKey, CustomTrelloURL.LABEL_URL, labelId )
				.token(apiSecret)
				.build();

		return trelloObjFactory.createObject(new TypeToken<Label>() {
		}, call.doGet(url));
	}
	
}
