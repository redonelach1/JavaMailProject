package com.emm.model;

public class CompteUtilisateur {
	private String identifiant;
	private BoiteMail boiteMail;

	public CompteUtilisateur(String identifiant) {
		this.identifiant = identifiant;
		this.boiteMail = new BoiteMail();
	}

	public String getIdentifiant() {
        return identifiant;
    }

    public BoiteMail getBoiteMail() {
        return boiteMail;
    }
}
