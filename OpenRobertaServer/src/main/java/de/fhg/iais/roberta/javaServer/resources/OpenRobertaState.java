package de.fhg.iais.roberta.javaServer.resources;

public class OpenRobertaState {

    private int userId;

    private OpenRobertaState() {
        this.userId = 1;
    }

    public static OpenRobertaState init() {
        return new OpenRobertaState();
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
