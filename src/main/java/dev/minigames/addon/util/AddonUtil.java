package dev.minigames.addon.util;


import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import java.util.concurrent.ThreadLocalRandom;
import it.unimi.dsi.fastutil.objects.ReferenceList;

public class AddonUtil {
    private static final ReferenceList<ItemStack> CATEGORY_ICONS = ReferenceList.of(
        Items.ALLIUM.getDefaultStack(),
        Items.COOKIE.getDefaultStack(),
        Items.SNOWBALL.getDefaultStack(),
        Items.ENDER_PEARL.getDefaultStack(),
        Items.CREEPER_HEAD.getDefaultStack(),
        Items.GLOWSTONE_DUST.getDefaultStack(),
        Items.ALLAY_SPAWN_EGG.getDefaultStack()
    );

    private static final ItemStack[] discIcons = {
        Items.MUSIC_DISC_5.getDefaultStack(),
        Items.MUSIC_DISC_11.getDefaultStack(),
        Items.MUSIC_DISC_13.getDefaultStack(),
        Items.MUSIC_DISC_CAT.getDefaultStack(),
        Items.MUSIC_DISC_FAR.getDefaultStack(),
        Items.MUSIC_DISC_MALL.getDefaultStack(),
        Items.MUSIC_DISC_STAL.getDefaultStack(),
        Items.MUSIC_DISC_WARD.getDefaultStack(),
        Items.MUSIC_DISC_WAIT.getDefaultStack(),
        Items.MUSIC_DISC_CHIRP.getDefaultStack(),
        Items.MUSIC_DISC_STRAD.getDefaultStack(),
        Items.MUSIC_DISC_RELIC.getDefaultStack(),
        Items.MUSIC_DISC_BLOCKS.getDefaultStack(),
        Items.MUSIC_DISC_MELLOHI.getDefaultStack(),
        Items.MUSIC_DISC_PIGSTEP.getDefaultStack(),
        Items.MUSIC_DISC_CREATOR.getDefaultStack(),
        Items.MUSIC_DISC_PRECIPICE.getDefaultStack(),
        Items.MUSIC_DISC_OTHERSIDE.getDefaultStack(),
        Items.MUSIC_DISC_CREATOR_MUSIC_BOX.getDefaultStack(),
    };

    public static ItemStack getCategoryIcon() {
        final ItemStack[] icons = new ItemStack[] {
            discIcons[ThreadLocalRandom.current().nextInt(discIcons.length)],
            CATEGORY_ICONS.get(ThreadLocalRandom.current().nextInt(CATEGORY_ICONS.size()))
        };

        return icons[ThreadLocalRandom.current().nextInt(icons.length)];
    }

}
