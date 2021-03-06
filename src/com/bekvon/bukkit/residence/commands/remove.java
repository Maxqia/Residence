package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

public class remove implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2300)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {

	ClaimedResidence res = null;
	String senderName = sender.getName();
	if (args.length == 2) {
	    res = plugin.getResidenceManager().getByName(args[1]);
	} else if (sender instanceof Player && args.length == 1) {
	    res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
	}

	if (res == null) {
	    plugin.msg(sender, lm.Invalid_Residence);
	    return true;
	}

	if (res.isSubzone()  && !resadmin && !plugin.hasPermission(sender, "residence.delete.subzone", lm.Subzone_CantDelete)) {
	    return true;
	}

	if (res.isSubzone() &&
	    !resadmin &&
	    plugin.getConfigManager().isPreventSubZoneRemoval() &&
	    !res.getParent().isOwner(sender) &&
	    !res.getPermissions().playerHas(sender.getName(), Flags.admin, FlagCombo.OnlyTrue) &&
	    plugin.hasPermission(sender, "residence.delete.subzone", lm.Subzone_CantDeleteNotOwnerOfParent)) {
	    return true;
	}

	if (!res.isSubzone() &&
	    !resadmin &&
	    !res.isOwner(sender) &&
	    plugin.hasPermission(sender, "residence.delete", lm.Residence_CantDeleteResidence)) {
	    return true;
	}

	if (!res.isSubzone() && !resadmin && !plugin.hasPermission(sender, "residence.delete", lm.Residence_CantDeleteResidence)) {
	    return true;
	}

	if (plugin.deleteConfirm.containsKey(senderName))
	    plugin.deleteConfirm.remove(senderName);

	String resname = res.getName();

	if (!plugin.deleteConfirm.containsKey(senderName) || !resname.equalsIgnoreCase(plugin.deleteConfirm.get(senderName))) {
	    String cmd = "res";
	    if (resadmin)
		cmd = "resadmin";
	    if (sender instanceof Player) {
		String raw = "";
		if (res.isSubzone()) {
		    raw = plugin.getResidenceManager().convertToRaw(null, plugin.msg(lm.Subzone_DeleteConfirm, res.getResidenceName()),
			"Click to confirm", cmd + " confirm");
		} else {
		    raw = plugin.getResidenceManager().convertToRaw(null, plugin.msg(lm.Residence_DeleteConfirm, res.getResidenceName()),
			"Click to confirm", cmd + " confirm");
		}
		if (plugin.msg(lm.Subzone_DeleteConfirm, res.getResidenceName()).length() > 0)
		    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + raw);
	    } else {
		if (res.isSubzone())
		    plugin.msg(sender, lm.Subzone_DeleteConfirm, res.getResidenceName());
		else
		    plugin.msg(sender, lm.Residence_DeleteConfirm, res.getResidenceName());
	    }
	    plugin.deleteConfirm.put(senderName, resname);
	} else {
	    plugin.getResidenceManager().removeResidence(sender, resname, resadmin);
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Remove residences.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res remove <residence name>"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));
    }
}
