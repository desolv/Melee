package gg.desolve.melee.inventory.rank.modify;

import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import gg.desolve.mithril.Mithril;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ModifyRankInventory implements InventoryProvider {

    public static final SmartInventory INVENTORY = SmartInventory.builder()
            .id("modifyRankInventory")
            .provider(new ModifyRankInventory())
            .size(3, 9)
            .title("Rank Modify")
            .manager(Mithril.getInstance().getInventoryManager())
            .build();


    @Override
    public void init(Player player, InventoryContents contents) {
        ItemStack glassStack = XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
        ItemMeta glassMeta = glassStack.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassStack.setItemMeta(glassMeta);
        contents.fillRow(0, ClickableItem.empty(glassStack));



    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
