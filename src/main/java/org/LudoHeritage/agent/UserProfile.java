package org.LudoHeritage.agent;

public class UserProfile {
    private String niveau;
    private String typeJeuPrefere;
    private String regionPreferee;
    private String langue;

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getTypeJeuPrefere() {
        return typeJeuPrefere;
    }

    public void setTypeJeuPrefere(String typeJeuPrefere) {
        this.typeJeuPrefere = typeJeuPrefere;
    }

    public String getRegionPreferee() {
        return regionPreferee;
    }

    public void setRegionPreferee(String regionPreferee) {
        this.regionPreferee = regionPreferee;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }
}
