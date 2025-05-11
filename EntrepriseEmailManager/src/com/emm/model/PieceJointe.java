package com.emm.model;

import java.util.Arrays;

public class PieceJointe {
	private String nomFichier ;
	private String typeMIME ;
	private byte[] contenu ;
	
	public PieceJointe() {
		
	}

	public PieceJointe(String nomFichier, String typeMIME, byte[] contenu) {
		this.nomFichier = nomFichier;
		this.typeMIME = typeMIME;
		this.contenu = contenu;
	}

	public String getNomFichier() {
		return nomFichier;
	}

	public void setNomFichier(String nomFichier) {
		this.nomFichier = nomFichier;
	}

	public String getTypeMIME() {
		return typeMIME;
	}

	public void setTypeMIME(String typeMIME) {
		this.typeMIME = typeMIME;
	}

	public byte[] getContenu() {
		return contenu;
	}

	public void setContenu(byte[] contenu) {
		this.contenu = contenu;
	}

	public String toString() {
		return "PieceJointe [nomFichier=" + nomFichier + ", typeMIME=" + typeMIME + ", contenu="
				+ Arrays.toString(contenu) + "]";
	}
	
	
	
	
}
