package gg.desolve.melee.common;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Material {

    @Getter
    private static final Map<String, XMaterial> wools = new HashMap<>();

    @Getter
    private static final Map<String, XMaterial> aestheticWools = new HashMap<>();

    static {
        wools.put("red", XMaterial.RED_WOOL);
        wools.put("dark_red", XMaterial.RED_WOOL);
        wools.put("dark_green", XMaterial.GREEN_WOOL);
        wools.put("green", XMaterial.LIME_WOOL);
        wools.put("light_green", XMaterial.LIME_WOOL);
        wools.put("blue", XMaterial.BLUE_WOOL);
        wools.put("dark_blue", XMaterial.BLUE_WOOL);
        wools.put("yellow", XMaterial.YELLOW_WOOL);
        wools.put("white", XMaterial.WHITE_WOOL);
        wools.put("black", XMaterial.BLACK_WOOL);
        wools.put("gray", XMaterial.GRAY_WOOL);
        wools.put("light_gray", XMaterial.LIGHT_GRAY_WOOL);
        wools.put("pink", XMaterial.PINK_WOOL);
        wools.put("dark_purple", XMaterial.PURPLE_WOOL);
        wools.put("cyan", XMaterial.CYAN_WOOL);
        wools.put("aqua", XMaterial.CYAN_WOOL);
        wools.put("dark_aqua", XMaterial.CYAN_WOOL);
        wools.put("orange", XMaterial.ORANGE_WOOL);
        wools.put("gold", XMaterial.ORANGE_WOOL);
        wools.put("brown", XMaterial.BROWN_WOOL);
        wools.put("magenta", XMaterial.MAGENTA_WOOL);
        wools.put("purple", XMaterial.MAGENTA_WOOL);
        wools.put("light_purple", XMaterial.MAGENTA_WOOL);
        wools.put("light_blue", XMaterial.LIGHT_BLUE_WOOL);
        aestheticWools.put("red", XMaterial.RED_WOOL);
        aestheticWools.put("light_green", XMaterial.LIME_WOOL);
        aestheticWools.put("yellow", XMaterial.YELLOW_WOOL);
        aestheticWools.put("white", XMaterial.WHITE_WOOL);
        aestheticWools.put("pink", XMaterial.PINK_WOOL);
        aestheticWools.put("aqua", XMaterial.CYAN_WOOL);
        aestheticWools.put("dark_aqua", XMaterial.CYAN_WOOL);
        aestheticWools.put("gold", XMaterial.ORANGE_WOOL);
        aestheticWools.put("light_purple", XMaterial.MAGENTA_WOOL);
    }

    public static XMaterial getMiniMessageWool(String mmColor) {
        return wools.getOrDefault(
                mmColor.toLowerCase()
                        .replace("<", "")
                        .replace(">", "")
                , XMaterial.WHITE_WOOL);
    }

    public static ItemStack getTypeSkull(String skull) {
        if (skull.length() > 36)
            return XSkull.createItem()
                    .profile(Profileable.detect(skull))
                    .fallback(Profileable.username("Steve"))
                    .apply();

        return XSkull.createItem()
                .profile(Profileable.of(UUID.fromString(skull)))
                .fallback(Profileable.username("Steve"))
                .apply();
    }

}
