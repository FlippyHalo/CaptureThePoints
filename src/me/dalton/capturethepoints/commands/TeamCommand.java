package me.dalton.capturethepoints.commands;

import java.util.ArrayList;
import java.util.List;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.PlayerData;
import me.dalton.capturethepoints.Team;
import org.bukkit.ChatColor;

public class TeamCommand extends CTPCommand
{

    public TeamCommand(CaptureThePoints instance)
    {
        super.ctp = instance;
        super.aliases.add("team");
        super.aliases.add("myteam");
        super.notOpCommand = true;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.play", "ctp.admin"};
        super.senderMustBePlayer = true;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp team";
    }

    @Override
    public void perform()
    {
        if (ctp.teams.size() <= 0)
        {
            player.sendMessage(ChatColor.RED + "There are no teams - has a game been started?");
            return;
        }

        if (!ctp.blockListener.isAlreadyInGame(player) || ctp.playerData.get(player) == null)
        {
            player.sendMessage(ChatColor.RED + "You must be playing a game to get who's on your team!");
            return;
        }

        PlayerData data = ctp.playerData.get(player);
        String teamcolour = data.color.trim();

        List<String> playernames = new ArrayList<String>();
        ChatColor cc = ChatColor.GREEN;
        for (Team aTeam : ctp.teams)
        {
            if (teamcolour.equalsIgnoreCase(aTeam.color))
            {
                playernames = aTeam.getTeamPlayerNames(ctp);
                cc = aTeam.chatcolor;
            }
        }
        player.sendMessage(cc + String.valueOf(playernames.size()) + " teammates: " + ChatColor.GREEN + playernames);
        return;
    }
}
