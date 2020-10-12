package sonar.fluxnetworks.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.misc.FluxConfigurationType;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.api.network.INetworkConnector;
import sonar.fluxnetworks.api.text.FluxTranslate;
import sonar.fluxnetworks.api.text.StyleUtils;
import sonar.fluxnetworks.client.FluxClientCache;
import sonar.fluxnetworks.common.misc.ContainerConnector;
import sonar.fluxnetworks.common.misc.FluxUtils;
import sonar.fluxnetworks.common.tileentity.TileFluxDevice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemFluxConfigurator extends Item {

    public ItemFluxConfigurator(Properties props) {
        super(props);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        if (context.getWorld().isRemote) {
            return ActionResultType.SUCCESS;
        }
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        TileEntity tile = context.getWorld().getTileEntity(context.getPos());
        if (tile instanceof TileFluxDevice) {
            TileFluxDevice flux = (TileFluxDevice) tile;
            if (!flux.canPlayerAccess(context.getPlayer())) {
                player.sendStatusMessage(StyleUtils.getErrorStyle(FluxTranslate.ACCESS_DENIED_KEY), true);
                return ActionResultType.FAIL;
            }
            ItemStack stack = player.getHeldItem(context.getHand());
            if (player.isSneaking()) {
                stack.setTagInfo(FluxUtils.CONFIGS_TAG, FluxUtils.copyConfiguration(flux, new CompoundNBT()));
                player.sendMessage(new StringTextComponent("Copied Configuration"), UUID.randomUUID());
            } else {
                CompoundNBT configs = stack.getOrCreateChildTag(FluxUtils.CONFIGS_TAG);
                if (!configs.isEmpty()) {
                    FluxUtils.pasteConfiguration(flux, configs);
                    player.sendMessage(new StringTextComponent("Pasted Configuration"), UUID.randomUUID());
                }
            }
            return ActionResultType.SUCCESS;
        }
        NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(player.getHeldItem(context.getHand())), buf -> buf.writeBoolean(false));
        return ActionResultType.SUCCESS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull PlayerEntity player, @Nonnull Hand hand) {
        if (!world.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(player.getHeldItem(hand)), buf -> buf.writeBoolean(false));
        }
        return ActionResult.resultSuccess(player.getHeldItem(hand));
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        CompoundNBT tag = stack.getChildTag(FluxUtils.CONFIGS_TAG);
        if (tag != null) {
            tooltip.add(new StringTextComponent(FluxTranslate.NETWORK_FULL_NAME.t() + ": " + TextFormatting.WHITE + FluxClientCache.getNetworkName(
                    tag.getInt(FluxConfigurationType.NETWORK.getNBTName()))));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public static class ContainerProvider implements INamedContainerProvider, INetworkConnector {

        public ItemStack stack;
        public IFluxNetwork network;

        public ContainerProvider(ItemStack stack) {
            this.stack = stack;
            CompoundNBT tag = stack.getChildTag(FluxUtils.CONFIGS_TAG);
            int networkID = tag != null ? tag.getInt(FluxConfigurationType.NETWORK.getNBTName()) : -1;
            network = FluxNetworks.PROXY.getNetwork(networkID);
        }

        @Override
        public int getNetworkID() {
            return network.getNetworkID();
        }

        @Override
        public IFluxNetwork getNetwork() {
            return network;
        }

        @Override
        public void onContainerOpened(PlayerEntity player) {
        }

        @Override
        public void onContainerClosed(PlayerEntity player) {
        }

        @Nonnull
        @Override
        public ITextComponent getDisplayName() {
            return stack.getDisplayName();
        }

        @Nullable
        @Override
        public Container createMenu(int windowID, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity player) {
            return new ContainerConnector<>(windowID, playerInventory, this);
        }
    }
}