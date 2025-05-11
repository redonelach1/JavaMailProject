package com.emm.model;

import java.util.List;

public record MailingList (
	String nom,
	List<String> adresses
	) {}
	
	

