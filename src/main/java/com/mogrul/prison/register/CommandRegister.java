package com.mogrul.prison.register;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.command.CellCommand;
import com.mogrul.prison.command.ContrabandCommand;
import com.mogrul.prison.command.PrisonCommand;
import com.mogrul.prison.command.SentenceCommand;

public class CommandRegister {
    private final MogrulPrison plugin;

    public CommandRegister(MogrulPrison plugin) {
        this.plugin = plugin;

        register();
    }
    private void register() {
        new CellCommand(plugin);
        new PrisonCommand(plugin);
        new ContrabandCommand(plugin);
        new SentenceCommand(plugin);
    }
}
