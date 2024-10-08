package fzzyhmstrs.emi_loot.client;

import fzzyhmstrs.emi_loot.util.LText;
import fzzyhmstrs.emi_loot.util.TextKey;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class ClientBlockLootTable extends AbstractTextKeyParsingClientLootTable<ClientBlockLootTable> {

    public static ClientBlockLootTable INSTANCE = new ClientBlockLootTable();
    private static final Identifier EMPTY = Identifier.of("blocks/empty");
    public final Identifier id;
    public final Identifier blockId;

    public ClientBlockLootTable() {
        super();
        this.id = EMPTY;
        this.blockId = Identifier.of("air");
    }

    public ClientBlockLootTable(Identifier id, Map<List<TextKey>, ClientRawPool> map) {
        super(map);
        this.id = id;
        String ns = id.getNamespace();
        String pth = id.getPath();
        int lastSlashIndex = pth.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            blockId = Identifier.of(ns, pth);
        } else {
            blockId = Identifier.of(ns, pth.substring(Math.min(lastSlashIndex + 1, pth.length())));
        }
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean isEmpty() {
        return Objects.equals(id, EMPTY);
    }

    @Override
    List<Pair<Integer, Text>> getSpecialTextKeyList(World world, Block block) {
        String tool = "";
        if (block.getRegistryEntry().isIn(BlockTags.PICKAXE_MINEABLE)) {
            tool = "pickaxe";
        } else if (block.getRegistryEntry().isIn(BlockTags.AXE_MINEABLE)) {
            tool = "axe";
        } else if (block.getRegistryEntry().isIn(BlockTags.SHOVEL_MINEABLE)) {
            tool = "shovel";
        } else if (block.getRegistryEntry().isIn(BlockTags.HOE_MINEABLE)) {
            tool = "hoe";
        }
        List<Pair<Integer, Text>> toolNeededList = new LinkedList<>();
        if (!Objects.equals(tool, "")) {
            String type;
            if (block.getRegistryEntry().isIn(BlockTags.NEEDS_STONE_TOOL)) {
                type = "stone";
            } else if (block.getRegistryEntry().isIn(BlockTags.NEEDS_IRON_TOOL)) {
                type = "iron";
            } else if (block.getRegistryEntry().isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
                type = "diamond";
            } else {
                type = "wood";
            }
            String keyString = "emi_loot." + tool + "." + type;
            int keyIndex = TextKey.getIndex(keyString);
            if (keyIndex != -1) {
                toolNeededList.add(new Pair<>(keyIndex, LText.translatable(keyString)));
            }
        }
        return toolNeededList;
    }

    @Override
    Pair<Identifier, Identifier> getBufId(PacketByteBuf buf) {
        return new Pair<>(getIdFromBuf(buf), EMPTY);
    }

    @Override
    ClientBlockLootTable simpleTableToReturn(Pair<Identifier, Identifier> ids, PacketByteBuf buf) {
        ClientRawPool simplePool = new ClientRawPool(new HashMap<>());
        Object2FloatMap<ItemStack> simpleMap = new Object2FloatOpenHashMap<>();
        ItemStack simpleStack = new ItemStack(Registries.ITEM.getEntry(buf.readRegistryKey(RegistryKeys.ITEM)).map(RegistryEntry.Reference::value).orElse(Items.AIR));
        simpleMap.put(simpleStack, 100F);
        simplePool.map().put(new ArrayList<>(), simpleMap);
        Map<List<TextKey>, ClientRawPool> itemMap = new HashMap<>();
        itemMap.put(new ArrayList<>(), simplePool);
        return new ClientBlockLootTable(ids.getLeft(), itemMap);
    }

    @Override
    ClientBlockLootTable emptyTableToReturn() {
        return new ClientBlockLootTable();
    }

    @Override
    ClientBlockLootTable filledTableToReturn(Pair<Identifier, Identifier> ids, Map<List<TextKey>, ClientRawPool> itemMap) {
        return new ClientBlockLootTable(ids.getLeft(), itemMap);
    }
}