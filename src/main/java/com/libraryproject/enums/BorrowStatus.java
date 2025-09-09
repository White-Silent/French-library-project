package com.libraryproject.enums;

public enum BorrowStatus {
    BORROWED ("BORROWED"),
    RETURNED ("RETURNED"),
    LATE ("LATE");

    private final String dbValue;

    BorrowStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue(){
        return dbValue;
    }

    public static BorrowStatus fromDbValue(String dbValue) {
        for (BorrowStatus status : BorrowStatus.values()) {
            if (status.getDbValue().equalsIgnoreCase(dbValue)){
                return status;
            }
        }
        throw  new IllegalArgumentException("Invalid Borrow Status: " + dbValue);
    }
}
