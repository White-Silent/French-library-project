public enum Role {
    //La String SQL "ADMIN" et l'objet Java est Role.DIRECTOR
    READER("READER"),
    ADMIN("ADMIN");

    //Chaîne de caractère -> permet de séparer le nom de l'énum en Java
    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue(){
        return dbValue;
    }

    public static Role fromDbValue(String dbValue) {
        for (Role role : Role.values()) {
            if (role.getDbValue().equalsIgnoreCase(dbValue)){
                return role;
            }
        }
        throw  new IllegalArgumentException("Invalid Role Value: " + dbValue);
    }
}
