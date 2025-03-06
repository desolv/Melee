package gg.desolve.melee.inventory.rank.metadata;

import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import gg.desolve.melee.Melee;
import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.rank.Rank;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Converter;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.Stream;

public class MetadataModifyInventory implements InventoryProvider {

    private final Profile profile;
    private final Rank rank;

    public MetadataModifyInventory(Profile profile, Rank rank) {
        this.profile = profile;
        this.rank = Melee.getInstance().getRankManager().retrieve(rank.getName());
    }

    public static SmartInventory getInventory(Profile profile, Rank rank) {
        return SmartInventory.builder()
                .id("rankModifyInventory")
                .provider(new MetadataModifyInventory(profile, rank))
                .size(3, 9)
                .title(Message.translate("Modifying " + rank.getNameColored()))
                .manager(Mithril.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemStack glassStack = XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
        ItemMeta glassMeta = glassStack.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassStack.setItemMeta(glassMeta);
        contents.fillColumn(1, ClickableItem.empty(glassStack));
        contents.fillRow(0, ClickableItem.empty(glassStack));

        ItemStack nameStack = XMaterial.NAME_TAG.parseItem();
        ItemMeta nameMeta = nameStack.getItemMeta();
        nameMeta.setDisplayName(Message.translate("<green>Name"));
        nameMeta.setLore(Stream.of(
                "<gray>Self explanatory its the rank name",
                "<gray>but its used for identification",
                "<gray>- " + rank.getNameColored(),
                "<gray>",
                "<yellow>Click to modify display name"
        ).map(Message::translate).toList());
        nameStack.setItemMeta(nameMeta);

        // TODO: Make sure to update globally
        contents.set(1, 2, ClickableItem.empty(nameStack));

        ItemStack displayStack = XMaterial.WRITABLE_BOOK.parseItem();
        ItemMeta displayMeta = displayStack.getItemMeta();
        displayMeta.setDisplayName(Message.translate("<green>Display Name"));
        displayMeta.setLore(Stream.of(
                "<gray>The display name is what players",
                "<gray>will see first when viewing",
                "<gray>- " + rank.getDisplayColored(),
                "<gray>",
                "<yellow>Click to modify display name"
        ).map(Message::translate).toList());
        displayStack.setItemMeta(displayMeta);
        contents.set(1, 3, ClickableItem.of(displayStack, r -> profile.setRankProcess(rank, "display")));

        ItemStack priorityStack = XMaterial.GOLD_INGOT.parseItem();
        ItemMeta priorityMeta = priorityStack.getItemMeta();
        priorityMeta.setDisplayName(Message.translate("<green>Priority"));
        priorityMeta.setLore(Stream.of(
                "<gray>Ranks with a higher priority override",
                "<gray>lowers, used to manage hierarchy",
                "<gray>- <aqua>" + rank.getPriority(),
                "<gray>",
                "<yellow>Click to modify priority"
        ).map(Message::translate).toList());
        priorityStack.setItemMeta(priorityMeta);
        contents.set(1, 4, ClickableItem.of(priorityStack, r -> profile.setRankProcess(rank, "priority")));

        ItemStack prefixStack = XMaterial.OAK_SIGN.parseItem();
        ItemMeta prefixMeta = prefixStack.getItemMeta();
        prefixMeta.setDisplayName(Message.translate("<green>Prefix"));
        prefixMeta.setLore(Stream.of(
                "<gray>Appears before a player name",
                "<gray>in chat helping distinguish ranks",
                "<gray>- " + rank.getPrefix() + "You",
                "<gray>",
                "<yellow>Click to modify prefix"
        ).map(Message::translate).toList());
        prefixStack.setItemMeta(prefixMeta);
        contents.set(1, 5, ClickableItem.of(prefixStack, r -> profile.setRankProcess(rank, "prefix")));

        ItemStack colorStack = XMaterial.RED_DYE.parseItem();
        ItemMeta colorMeta = colorStack.getItemMeta();
        colorMeta.setDisplayName(Message.translate("<green>Color"));
        colorMeta.setLore(Stream.of(
                "<gray>Color defines how this rank",
                "<gray>appears in chat and menus",
                "<gray>- " + rank.getColor() + "this",
                "<gray>",
                "<yellow>Click to modify color"
        ).map(Message::translate).toList());
        colorStack.setItemMeta(colorMeta);
        contents.set(1, 6, ClickableItem.of(colorStack, r -> profile.setRankProcess(rank, "color")));

        ItemStack primaryStack = XMaterial.NETHER_STAR.parseItem();
        ItemMeta primaryMeta = primaryStack.getItemMeta();
        primaryMeta.setDisplayName(Message.translate("<green>Primary"));
        primaryMeta.setLore(Stream.of(
                "<gray>When a player has no other rank",
                "<gray>this will be assigned automatically",
                "<gray>- " + (rank.isPrimary() ? "<green>true" : "<red>false"),
                "<gray>",
                "<red>Primary cannot be modified"
        ).map(Message::translate).toList());
        primaryStack.setItemMeta(primaryMeta);
        contents.set(1, 7, ClickableItem.empty(primaryStack));

        ItemStack grantableStack = XMaterial.BLAZE_POWDER.parseItem();
        ItemMeta grantableMeta = grantableStack.getItemMeta();
        grantableMeta.setDisplayName(Message.translate("<green>Grantable"));
        grantableMeta.setLore(Stream.of(
                "<gray>Determines if this rank can be",
                "<gray>granted to players by staff",
                "<gray>- " + (rank.isPrimary() ? "<red>false" : (rank.isGrantable() ? "<green>true" : "<red>false")),
                "<gray>",
                rank.isPrimary() ? "<red>Unable to modify since is primary" : "<yellow>Click to modify grantable"
        ).map(Message::translate).toList());
        grantableStack.setItemMeta(grantableMeta);
        contents.set(1, 8, ClickableItem.of(grantableStack, r -> profile.setRankProcess(rank, "grantable")));

        ItemStack visibleStack = XMaterial.PRISMARINE_SHARD.parseItem();
        ItemMeta visibleMeta = visibleStack.getItemMeta();
        visibleMeta.setDisplayName(Message.translate("<green>Visible"));
        visibleMeta.setLore(Stream.of(
                "<gray>Hides ranks from players, priority",
                "<gray>will be stripped if not visible",
                "<gray>- " + (rank.isPrimary() ? "<green>true" : (rank.isVisible() ? "<green>true" : "<red>false")),
                "<gray>",
                rank.isPrimary() ? "<red>Unable to modify since is primary" : "<yellow>Click to modify visible"
        ).map(Message::translate).toList());
        visibleStack.setItemMeta(visibleMeta);
        contents.set(2, 2, ClickableItem.of(visibleStack, r -> profile.setRankProcess(rank, "visible")));

        ItemStack timestampStack = XMaterial.FIRE_CHARGE.parseItem();
        ItemMeta timestampMeta = timestampStack.getItemMeta();
        timestampMeta.setDisplayName(Message.translate("<green>Timestamp"));
        timestampMeta.setLore(Stream.of(
                "<gray>Informs last time the rank",
                "<gray>was updated locally",
                "<gray>- <aqua>" + Converter.time(System.currentTimeMillis() - rank.getTimestamp()) + " ago",
                "<gray>"
        ).map(Message::translate).toList());
        timestampStack.setItemMeta(timestampMeta);
        contents.set(2, 3, ClickableItem.empty(timestampStack));

        ItemStack permissionsStack = XMaterial.ANVIL.parseItem();
        ItemMeta permissionsMeta = permissionsStack.getItemMeta();
        permissionsMeta.setDisplayName(Message.translate("<green>Permissions"));
        permissionsMeta.setLore(Stream.of(
                "<gray>Permissions define what actions",
                "<gray>a player with this rank can do",
                "<gray>- <aqua>" + (rank.getPermissions().isEmpty() ? "&cNone" : rank.getPermissions().size()),
                "<gray>",
                "<yellow>Click to modify permissions"
        ).map(Message::translate).toList());
        permissionsStack.setItemMeta(permissionsMeta);
        contents.set(1, 0, ClickableItem.of(permissionsStack, r -> MetadataModifyPermissionsInventory.getInventory(profile, rank).open(player)));

        ItemStack inheritsStack = XMaterial.COMPARATOR.parseItem();
        ItemMeta inheritsMeta = inheritsStack.getItemMeta();
        inheritsMeta.setDisplayName(Message.translate("<green>Inherits"));
        inheritsMeta.setLore(Stream.of(
                "<gray>Ranks that this rank will inherit",
                "<gray>permissions and attributes from",
                "<gray>- <aqua>" + (rank.getInherits().isEmpty() ? "&cNone" : rank.getInherits().size()),
                "<gray>",
                "<yellow>Click to modify inherits"
        ).map(Message::translate).toList());
        inheritsStack.setItemMeta(inheritsMeta);
        contents.set(2, 0, ClickableItem.of(inheritsStack, r -> MetadataModifyInheritsInventory.getInventory(profile, rank).open(player)));
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
