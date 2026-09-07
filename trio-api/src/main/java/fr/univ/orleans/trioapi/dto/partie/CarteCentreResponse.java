package fr.univ.orleans.trioapi.dto.partie;

public class CarteCentreResponse {

    private final Long id;
    private final Integer valeur;
    private final boolean revelee;

    public CarteCentreResponse(Long id) {
        this(id, null, false);
    }

    public CarteCentreResponse(Long id, Integer valeur, boolean revelee) {
        this.id = id;
        this.valeur = valeur;
        this.revelee = revelee;
    }

    public Long getId() {
        return id;
    }

    public Integer getValeur() {
        return valeur;
    }

    public boolean isRevelee() {
        return revelee;
    }
}
