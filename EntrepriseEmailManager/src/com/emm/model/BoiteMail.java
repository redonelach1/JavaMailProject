package com.emm.model;

import java.util.ArrayList;
import java.util.List;

public class BoiteMail {
	private List<Email> messagesRecus = new ArrayList<>();
	private List<Email> messagesEnvoyes = new ArrayList<>();
	public List<Email> getMessagesRecus() {
		return new ArrayList<>(messagesRecus);
	}
	public List<Email> getMessagesEnvoyes() {
		return new ArrayList<>(messagesEnvoyes);
	}
	
	public void addMessagesEnvoyes(Email email) {
		messagesEnvoyes.add(email);
	}

	public void addMessagesRecu(Email email) {
		messagesRecus.add(email);
	}

	
}
