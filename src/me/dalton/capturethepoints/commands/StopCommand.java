package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;

public class StopCommand extends CTPCommand {
   
    public StopCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("stop");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.stop", "ctp.admin"};
        super.senderMustBePlayer = false;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp stop";
    }

    @Override
    public void perform() {
        ctp.getServer().broadcastMessage("[CTP] The Capture The Points game has ended.");
        ctp.blockListener.endGame(true);
    }
}