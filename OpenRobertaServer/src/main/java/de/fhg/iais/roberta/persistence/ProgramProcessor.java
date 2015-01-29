package de.fhg.iais.roberta.persistence;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;

import de.fhg.iais.roberta.javaServer.resources.HttpSessionState;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.bo.User;
import de.fhg.iais.roberta.persistence.bo.UserProgram;
import de.fhg.iais.roberta.persistence.dao.ProgramDao;
import de.fhg.iais.roberta.persistence.dao.UserDao;
import de.fhg.iais.roberta.persistence.dao.UserProgramDao;
import de.fhg.iais.roberta.persistence.util.DbSession;
import de.fhg.iais.roberta.util.Util;

public class ProgramProcessor extends AbstractProcessor {
    public ProgramProcessor(DbSession dbSession, HttpSessionState httpSessionState) {
        super(dbSession, httpSessionState);
    }

    public Program getProgram(String programName, int ownerId) {
        if ( !Util.isValidJavaIdentifier(programName) ) {
            setError(Util.PROGRAM_ERROR_ID_INVALID, programName);
            return null;
        } else if ( this.httpSessionState.isUserLoggedIn() ) {
            UserDao userDao = new UserDao(this.dbSession);
            ProgramDao programDao = new ProgramDao(this.dbSession);
            User owner = userDao.get(ownerId);
            Program program = programDao.load(programName, owner);
            if ( program != null ) {
                setSuccess(Util.PROGRAM_GET_ONE_SUCCESS);
                return program;
            } else {
                setError(Util.PROGRAM_GET_ONE_ERROR_NOT_FOUND);
                return null;
            }
        } else {
            setError(Util.PROGRAM_GET_ONE_ERROR_NOT_LOGGED_IN);
            return null;
        }
    }
    
    public JSONArray getProgramInfo(int ownerId) {
        UserDao userDao = new UserDao(this.dbSession);
        ProgramDao programDao = new ProgramDao(this.dbSession);
        UserProgramDao userProgramDao = new UserProgramDao(this.dbSession);

        User owner = userDao.get(ownerId);

        //First we obtain all programs owned by the user
        List<Program> programs = programDao.loadAll(owner);
        JSONArray programInfos = new JSONArray();

        for ( Program program : programs ) {

            JSONArray programInfo = new JSONArray();
            programInfos.put(programInfo);
            programInfo.put(program.getName());
            programInfo.put(program.getOwner().getAccount());
            programInfo.put(program.getNumberOfBlocks());
            programInfo.put(program.getCreated().toString());
            programInfo.put(program.getLastChanged().toString());

            //If shared find with whom and under which rights
            List<UserProgram> userProgramList = userProgramDao.loadUserProgramByProgram(program);
            JSONArray sharedUsersAndRights = new JSONArray();
            for ( UserProgram userProgram : userProgramList ) {
                JSONArray programSharedInfo = new JSONArray();
                sharedUsersAndRights.put(programSharedInfo);
                programSharedInfo.put(userProgram.getUser().getAccount());
                programSharedInfo.put(userProgram.getRelation().toString());
            }
            programInfo.put(sharedUsersAndRights);

        }

        //Now we find all the programs which are not owned by the user but have been shared to him
        List<UserProgram> userProgramList2 = userProgramDao.loadUserProgramByUser(owner);
        for ( UserProgram userProgram : userProgramList2 ) {
            JSONArray programInfo2 = new JSONArray();
            programInfos.put(programInfo2);
            programInfo2.put(userProgram.getProgram().getName());
            programInfo2.put(userProgram.getProgram().getOwner().getAccount());
            programInfo2.put(userProgram.getProgram().getNumberOfBlocks());
            programInfo2.put(userProgram.getProgram().getCreated().toString());
            programInfo2.put(userProgram.getProgram().getLastChanged().toString());
        }

        setSuccess("found " + programInfos.length() + " program(s)");
        return programInfos;
    }
    
    public void updateProgram(String programName, int ownerId, String programText) {
        if ( !Util.isValidJavaIdentifier(programName) ) {
            setError(Util.PROGRAM_ERROR_ID_INVALID, programName);
            return;
        }
        this.httpSessionState.setProgramNameAndProgramText(programName, programText);
        if ( this.httpSessionState.isUserLoggedIn() ) {
            UserDao userDao = new UserDao(this.dbSession);
            ProgramDao programDao = new ProgramDao(this.dbSession);
            User owner = userDao.get(ownerId);
            Program program = programDao.persistProgramText(programName, owner, programText);
            if ( program == null ) {
                setError(Util.PROGRAM_SAVE_ERROR_NOT_SAVED_TO_DB);
                return;
            }
        }
        setSuccess(Util.PROGRAM_SAVE_SUCCESS);
    }


    public void deleteByName(String programName, int ownerId) {
        UserDao userDao = new UserDao(this.dbSession);
        ProgramDao programDao = new ProgramDao(this.dbSession);
        User owner = userDao.get(ownerId);
        int rowCount = programDao.deleteByName(programName, owner);
        if ( rowCount > 0 ) {
            setSuccess(Util.PROGRAM_DELETE_SUCCESS);
        } else {
            setError(Util.PROGRAM_DELETE_ERROR);
        }
    }
}
