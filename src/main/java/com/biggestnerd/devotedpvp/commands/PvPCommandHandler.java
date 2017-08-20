package com.biggestnerd.devotedpvp.commands;

import com.biggestnerd.devotedpvp.commands.commands.AcceptCommand;
import com.biggestnerd.devotedpvp.commands.commands.ClearCommand;
import com.biggestnerd.devotedpvp.commands.commands.DuelCommand;
import com.biggestnerd.devotedpvp.commands.commands.EloCommand;
import com.biggestnerd.devotedpvp.commands.commands.ForfeitCommand;
import com.biggestnerd.devotedpvp.commands.commands.InvCommand;
import vg.civcraft.mc.civmodcore.command.CommandHandler;

public class PvPCommandHandler extends CommandHandler {

	@Override
	public void registerCommands() {
		addCommands(new DuelCommand());
		addCommands(new EloCommand());
		addCommands(new AcceptCommand());
		addCommands(new ForfeitCommand());
		addCommands(new InvCommand());
		addCommands(new ClearCommand());
	}
}