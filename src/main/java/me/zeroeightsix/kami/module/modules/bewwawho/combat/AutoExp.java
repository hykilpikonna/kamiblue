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
 * Updated 15 January 2020 by d1gress/Qther
 * Updated by S-B99 on 15/01/20
 */
@Module.Info(name = "AutoExp", category = Module.Category.COMBAT, description = "Auto Switch to XP and throw fast")
public class AutoExp extends Module {

    private Setting<Boolean> autoThrow = register(Settings.b("Auto Throw", true));
    private Setting<Boolean> autoSwitch = register(Settings.b("Auto Switch", true));
    private Setting<Boolean> autoDisable = register(Settings.booleanBuilder("Auto Disable").withValue(true).withVisibility(o -> autoSwitch.getValue()).build());
    private Setting<Boolean> mendOffhand = register(Settings.b("Mend Offhand", false));
    private Setting<Boolean> checkRepairable = register(Settings.b("Auto Repair", true));
    private Setting<Integer> threshold = register(Settings.integerBuilder("Repair %").withMinimum(1).withMaximum(100).withValue(70));
    private Setting<Integer> delay = register(Settings.integerBuilder("Delay (Ticks)").withMinimum(1).withValue(1));

    private int initHotbarSlot = -1;
    private int usableDelay;
    private int oldDelay = delay.getValue();

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (mc.player != null && (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE)) {
            mc.rightClickDelayTimer = 0;
        }
    });

    @Override
    protected void onEnable() {
        if (mc.player == null) return;

        if (autoSwitch.getValue()) {
            initHotbarSlot = mc.player.inventory.currentItem;
        }

    }

    @Override
    protected void onDisable() {
        if (mc.player == null) return;

        if (autoSwitch.getValue()) {
            if (initHotbarSlot != -1 && initHotbarSlot != mc.player.inventory.currentItem) {
                mc.player.inventory.currentItem = initHotbarSlot;
            }
        }

    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (slotNotEmpty(1)) Command.sendChatMessage("not empty");
        if (delay.getValue() != oldDelay) usableDelay = delay.getValue();

        oldDelay = delay.getValue();

        if (usableDelay > 0) {
            usableDelay--;
            return;
        } else {
            usableDelay = delay.getValue();
        }

        if (checkRepairable.getValue()
                && ((slotNotEmpty(0) && canMendArmour(0) && shouldMendArmour(0))
                || (slotNotEmpty(1) && canMendArmour(1) && shouldMendArmour(1))
                || (slotNotEmpty(2) && canMendArmour(2) && shouldMendArmour(2))
                || (slotNotEmpty(3) && canMendArmour(3) && shouldMendArmour(3))
                || (offhandNotEmpty() && canMendOffhand() && shouldMendOffhand()))) {
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

    /*
/give @p diamond_chestplate 1 200 {ench:[{lvl:1s,id:70s}]}
    */

    /* Should do mending and can mend */
    private boolean shouldMendArmour(int i) { // (100 * damage / max damage) >= (100 - 70)
        return (
                (100 * mc.player.inventory.armorInventory.get(i).getItemDamage())
                / mc.player.inventory.armorInventory.get(i).getMaxDamage())
                < threshold.getValue();
    }
    private boolean shouldMendOffhand() {
        return (mendOffhand.getValue() &&
                ((100 * mc.player.getHeldItemOffhand().getItemDamage() / mc.player.getHeldItemOffhand().getMaxDamage())
                < threshold.getValue()));
    }

    /* Has mending enchantment */
    private boolean canMendArmour(int i) {
        return (mc.player.inventory.armorInventory.get(i).getEnchantmentTagList().toString().contains("lvl:1s,id:70s") || mc.player.inventory.armorInventory.get(i).getEnchantmentTagList().toString().contains("id:70s,lvl:1s"));
    }
    private boolean canMendOffhand() {
        return mendOffhand.getValue() && (mc.player.getHeldItemOffhand().getEnchantmentTagList().toString().contains("lvl:1s,id:70s") || mc.player.getHeldItemOffhand().getEnchantmentTagList().toString().contains("id:70s,lvl:1s"));
    }

    /* Are any of the slots empty */
    private boolean slotNotEmpty(int i) {
        return mc.player.inventory.armorInventory.get(i) != ItemStack.EMPTY;
    }
    private boolean offhandNotEmpty() {
        return mendOffhand.getValue() && mc.player.getHeldItemOffhand() != ItemStack.EMPTY;
    }

    /* Does the player have experience bottles */
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
