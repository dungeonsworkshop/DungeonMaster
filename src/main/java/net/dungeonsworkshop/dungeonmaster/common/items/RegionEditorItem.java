package net.dungeonsworkshop.dungeonmaster.common.items;

import net.dungeonsworkshop.dungeonmaster.common.entity.TileBlockTE;
import net.dungeonsworkshop.dungeonmaster.common.init.DungeonItems;
import net.dungeonsworkshop.dungeonmaster.util.Vec2i;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class RegionEditorItem extends Item {

    public final static String REGION_SETTING = "regionSetting";
    public final static String LINK_POS = "linkedTileManager";
    private final static int[] UNLINKED = new int[]{0, -1, 0};

    public RegionEditorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (!nbt.contains(REGION_SETTING)) {
            nbt.putInt(REGION_SETTING, 0);
        }
        if (!nbt.contains(LINK_POS)) {
            nbt.putIntArray(LINK_POS, UNLINKED);
        }
        if (isSelected && entityIn instanceof PlayerEntity) {
            if (nbt.getIntArray(LINK_POS)[1] == -1) {
                ((PlayerEntity) entityIn).sendStatusMessage(new StringTextComponent(TextFormatting.DARK_RED + "Region Editor is Unlinked"), true);
            }
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            ItemStack regionEditor = new ItemStack(DungeonItems.REGION_EDITOR.get());
            CompoundNBT nbt = regionEditor.getOrCreateTag();
            nbt.putInt(REGION_SETTING, 0);
            nbt.putIntArray(LINK_POS, UNLINKED);
            items.add(regionEditor);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (nbt.contains(REGION_SETTING)) {
            tooltip.add(new StringTextComponent("Region Setting: " + (nbt.getInt(REGION_SETTING) + 1)));
            tooltip.add(new StringTextComponent("Linked Tile Manager: " + Arrays.toString(nbt.getIntArray(LINK_POS))));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        if (itemStack.getItem() instanceof RegionEditorItem) {
            CompoundNBT nbt = itemStack.getOrCreateTag();
            BlockPos playerLookBlock = ((BlockRayTraceResult) playerIn.pick(20.0D, 0.0f, false)).getPos();
            if (!worldIn.isRemote()) {
                if (playerIn.isSneaking()) {
                    if (worldIn.getTileEntity(playerLookBlock) instanceof TileBlockTE) {
                        int[] newLinkPos = linkToBlock(itemStack, playerLookBlock);
                        playerIn.sendStatusMessage(new StringTextComponent("Linked to: " + Arrays.toString(newLinkPos)), true);
                    } else {
                        playerIn.sendStatusMessage(new StringTextComponent("Region Setting: " + (cycleRegionSetting(itemStack) + 1)), true);
                    }
                } else {
                    if (nbt.contains(LINK_POS) && nbt.contains(REGION_SETTING)) {
                        BlockPos linkPos = new BlockPos(nbt.getIntArray(LINK_POS)[0], nbt.getIntArray(LINK_POS)[1], nbt.getIntArray(LINK_POS)[2]);
                        TileEntity tileEntity = worldIn.getTileEntity(linkPos);
                        if (tileEntity instanceof TileBlockTE) {
                            ((TileBlockTE) tileEntity).getLoadedTile().setRegionPlaneDataAtPos(new Vec2i(playerLookBlock.getX() - linkPos.getX(), playerLookBlock.getZ() - linkPos.getZ()), nbt.getInt(REGION_SETTING));
                            ((TileBlockTE) tileEntity).sync();
                        }
                    }
                }
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return super.onItemUse(context);
    }

    public static int cycleRegionSetting(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof RegionEditorItem)) return -1;
        CompoundNBT nbt = itemStack.getOrCreateTag();
        if (nbt.contains(REGION_SETTING)) {
            int setting = nbt.getInt(REGION_SETTING);
            if (setting + 1 > 4) {
                setting = 0;
            } else {
                setting++;
            }
            nbt.putInt(REGION_SETTING, setting);
            return setting;
        }
        return 0;
    }

    public static int[] linkToBlock(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof RegionEditorItem)) return UNLINKED;
        CompoundNBT nbt = itemStack.getOrCreateTag();
        if (nbt.contains(LINK_POS)) {
            int[] newLinkPos = new int[]{pos.getX(), pos.getY(), pos.getZ()};
            nbt.putIntArray(LINK_POS, newLinkPos);
            return newLinkPos;
        }
        return UNLINKED;
    }

    /*

     */

}
