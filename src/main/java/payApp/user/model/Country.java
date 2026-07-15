package payApp.user.model;

public enum Country {

    BULGARIA("Bulgaria"),
    GERMANY("Germany"),
    FRANCE("France");

    private String displayName;

    Country(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
