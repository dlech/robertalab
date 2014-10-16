package de.fhg.iais.roberta.javaServer.resources;

import java.util.HashSet;
import java.util.Set;

import de.fhg.iais.roberta.dbc.Assert;

public class OpenRobertaSessionState {
    private static final Set<String> allTokensUsed = new HashSet<String>();

    private int userId = -1;
    private String token = "1Q2W3E4R";
    private String programName;
    private String program;
    private String configurationName;
    private String configuration;

    private OpenRobertaSessionState() {
    }

    public static OpenRobertaSessionState init() {
        return new OpenRobertaSessionState();
    }

    public int getUserId() {
        return this.userId;
    }

    public boolean isUserLoggedIn() {
        return this.userId >= 1;
    }

    public void rememberLogin(int userId) {
        Assert.isTrue(userId >= 1);
        // token is not cleared. This would annoy the user.
        this.userId = userId;
        this.programName = null;
        this.program = null;
        this.configurationName = null;
        this.configuration = null;
    }

    public void rememberLogout() {
        this.userId = -1;
        // token is not cleared. This would annoy the user.
        this.programName = null;
        this.program = null;
        this.configurationName = null;
        this.configuration = null;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        Assert.notNull(token);
        synchronized ( OpenRobertaSessionState.class ) {
            Assert.isTrue(!allTokensUsed.contains(token), "token already used. New token required.");
            allTokensUsed.add(token);
        }
        this.token = token;
    }

    public String getProgramName() {
        return this.programName;
    }

    public String getProgram() {
        return this.program;
    }

    public void setProgramNameAndProgramText(String programName, String program) {
        this.programName = programName;
        this.program = program;
    }

    public String getConfigurationName() {
        return this.configurationName;
    }

    public String getConfiguration() {
        return this.configuration;
    }

    public void setConfigurationNameAndConfiguration(String configurationName, String configuration) {
        this.configurationName = configurationName;
        this.configuration = configuration;
    }
}