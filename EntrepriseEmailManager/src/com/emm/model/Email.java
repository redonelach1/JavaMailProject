package com.emm.model;

import java.time.LocalDateTime;
import java.util.List;

public record Email(
		String expediteur,
		List<String> destinataires,
		String sujet,
		LocalDateTime date,
		String contenu
		
		) {}