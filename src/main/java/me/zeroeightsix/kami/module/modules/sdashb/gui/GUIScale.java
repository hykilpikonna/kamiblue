package me.zeroeightsix.kami.module.modules.sdashb.gui;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

@Module.Info(name = "GUI Scale", category = Module.Category.GUI, description = "Configures size of GUI")
public class GUIScale extends Module {
    private Setting<Integer> scaleGlobal = this.register(Settings.integerBuilder("Scale").withMinimum(1).withValue(2).withMaximum(3).build());

    public void onUpdate() {
        if (mc.player == null) return;
        mc.gameSettings.guiScale = scaleGlobal.getValue();
//        Command.sendWarningMessage(Wrapper.getMinecraft().gameSettings.guiScale + "");
//        Command.sendWarningMessage(scaleGlobal.getValue() + "");
//        Command.sendWarningMessage();
    }

    public void onDisable() {
        this.enable();
    }
}