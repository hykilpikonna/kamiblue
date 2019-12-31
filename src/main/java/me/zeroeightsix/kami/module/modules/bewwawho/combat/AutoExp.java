package me.zeroeightsix.kami.module.modules.bewwawho.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Created 17 October 2019 by hub
 * Updated 21 November 2019 by hub
 * Updated 1 January 2020 by d1gress/Qther
 */
@Module.Info(name = "AutoExp", category = Module.Category.COMBAT, description = "Auto Switch to XP and throw fast")
public class AutoExp extends Module {

    private Setting<Boolean> autoThrow = register(Settings.b("Auto Throw", true));
    private Setting<Boolean> autoSwitch = register(Settings.b("Auto Switch", true));
    private Setting<Boolean> autoDisable = register(Settings.booleanBuilder("Auto Disable").withValue(true).withVisibility(o -> autoSwitch.getValue()).build());
    private Setting<Boolean> checkRepairable = register(Settings.b("Check Repairable", true));
    private Setting<Integer> delay = register(Settings.integerBuilder("Delay (Ticks)").withMinimum(1).withValue(1));

    private int initHotbarSlot = -1;
    private int usableDelay;
    private int oldDelay = delay.getValue();

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event ->
    {
        if (mc.player != null && (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE)) {
            mc.rightClickDelayTimer = 0;
        }
    });

    @Override
    protected void onEnable() {

        if (mc.player == null) {
            return;
        }

        if (autoSwitch.getValue()) {
            initHotbarSlot = mc.player.inventory.currentItem;
        }

    }

    @Override
    protected void onDisable() {

        if (mc.player == null) {
            return;
        }

        if (autoSwitch.getValue()) {
            if (initHotbarSlot != -1 && initHotbarSlot != mc.player.inventory.currentItem) {
                mc.player.inventory.currentItem = initHotbarSlot;
            }
        }

    }

    private boolean hasMending(ItemStack stack) {
        if (stack.getEnchantmentTagList() != null && (stack.getEnchantmentTagList().toString().contains("lvl:1s,id:70s") || stack.getEnchantmentTagList().toString().contains("id:70s,lvl:1s"))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isDamaged(ItemStack stack) {
        return (stack != ItemStack.EMPTY && stack.getItemDamage() > 0);
    }

    @Override
    public void onUpdate() {
        if (delay.getValue() != oldDelay) {
            usableDelay = delay.getValue();
        }

        oldDelay = delay.getValue();

        if (usableDelay > 0) {
            usableDelay--;
            return;
        } else {
            usableDelay = delay.getValue();
        }

        if (mc.player == null) {
            return;
        }

        if (checkRepairable.getValue() && !((hasMending(mc.player.inventory.armorInventory.get(0)) && isDamaged(mc.player.inventory.armorInventory.get(0)))
                || (hasMending(mc.player.inventory.armorInventory.get(1)) && isDamaged(mc.player.inventory.armorInventory.get(1)))
                || (hasMending(mc.player.inventory.armorInventory.get(2)) && isDamaged(mc.player.inventory.armorInventory.get(2)))
                || (hasMending(mc.player.inventory.armorInventory.get(3)) && isDamaged(mc.player.inventory.armorInventory.get(3)))
                || (hasMending(mc.player.getHeldItemOffhand()) && isDamaged(mc.player.getHeldItemOffhand())))) {
            return;
        }

        if (autoSwitch.getValue() && (mc.player.getHeldItemMainhand().getItem() != Items.EXPERIENCE_BOTTLE)) {
            int xpSlot = findXpPots();
            if (xpSlot == -1) {
                if (autoDisable.getValue()) {
                    Command.sendWarningMessage("[AutoExp] No XP in hotbar, disabling");
                    this.disable();
                }
                return;
            }
            mc.player.inventory.currentItem = xpSlot;
        }

        if (autoThrow.getValue() && mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            mc.rightClickMouse();
        }

    }

    private int findXpPots() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                slot = i;
                break;
            }
        }
        return slot;
    }

}
