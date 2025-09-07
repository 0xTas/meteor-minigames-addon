package dev.minigames.addon.util;


import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.concurrent.ThreadLocalRandom;

public class AddonUtil {
    private static final ReferenceList<ItemStack> CATEGORY_ICONS = ReferenceList.of(
        Items.ALLIUM.getDefaultStack(), Items.ALLAY_SPAWN_EGG.getDefaultStack(),
        Items.COOKIE.getDefaultStack(), Items.CREEPER_HEAD.getDefaultStack()
    );

    public static ItemStack getCategoryIcon() {
        return CATEGORY_ICONS.get(ThreadLocalRandom.current().nextInt(CATEGORY_ICONS.size()));
    }
}
