package edu.isi.karma.controller.history;

import edu.isi.karma.controller.command.ICommand;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Frank on 9/14/15.
 */
public class RedoCommandObject {
    private ICommand command;
    private Pair<ICommand,JSONArray> consolidatedCommand;

    RedoCommandObject(ICommand command, Pair<ICommand,JSONArray> consolidatedCommand) {
        this.command = command;
        this.consolidatedCommand = consolidatedCommand;
    }

    public ICommand getCommand() {
        return command;
    }

    public Pair<ICommand,JSONArray> getConsolidatedCommand() {
        return consolidatedCommand;
    }
}
