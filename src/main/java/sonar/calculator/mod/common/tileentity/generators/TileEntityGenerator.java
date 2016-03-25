package sonar.calculator.mod.common.tileentity.generators;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.calculator.mod.CalculatorConfig;
import sonar.calculator.mod.client.gui.generators.GuiExtractor;
import sonar.calculator.mod.common.containers.ContainerExtractor;
import sonar.calculator.mod.common.recipes.machines.GlowstoneExtractorRecipes;
import sonar.calculator.mod.common.recipes.machines.RedstoneExtractorRecipes;
import sonar.calculator.mod.common.recipes.machines.StarchExtractorRecipes;
import sonar.calculator.mod.common.tileentity.TileEntityFlux;
import sonar.core.common.tileentity.TileEntityEnergyInventory;
import sonar.core.inventory.SonarTileInventory;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncEnergyStorage;
import sonar.core.network.sync.SyncTagType;
import sonar.core.utils.IGuiTile;
import sonar.core.utils.helpers.FontHelper;
import sonar.core.utils.helpers.NBTHelper.SyncType;
import sonar.core.utils.helpers.SonarHelper;
import cofh.api.energy.IEnergyReceiver;

public abstract class TileEntityGenerator extends TileEntityEnergyInventory implements ISidedInventory,IGuiTile  {

	protected TileEntity[] handlers = new TileEntity[6];

	public SyncTagType.INT itemLevel = new SyncTagType.INT(0);
	public SyncTagType.INT burnTime = new SyncTagType.INT(1);
	public SyncTagType.INT maxBurnTime = new SyncTagType.INT(2);
	//public int itemLevel, burnTime;
	public int levelMax = 5000;
	public int requiredLevel = 400;
	public int energyMultiplier;

	private static final int[] slotsTop = new int[] { 0 };
	private static final int[] slotsBottom = new int[] { 2, 1 };
	private static final int[] slotsSides = new int[] { 1 };

	public TileEntityGenerator() {
		super.storage = new SyncEnergyStorage(1000000, 800);
		super.inv = new SonarTileInventory(this, 2);
		super.energyMode = EnergyMode.SEND;
		super.maxTransfer = 2000;
	}

	@Override
	public void update() {
		super.update();
		if (!this.worldObj.isRemote) {
			processItemLevel();
			generateEnergy();
			int maxTransfer = Math.min(this.maxTransfer, this.storage.getEnergyStored());
			this.storage.extractEnergy(maxTransfer - this.pushEnergy(maxTransfer, false), false);
		}
		this.markDirty();
	}

	public void generateEnergy() {
		ItemStack stack = this.getStackInSlot(0);
		if (!(stack == null)) {
			if (burnTime.getObject() == 0 && TileEntityFurnace.isItemFuel(stack)) {
				if (!(this.storage.getEnergyStored() == this.storage.getMaxEnergyStored()) && this.itemLevel.getObject() >= requiredLevel) {
					this.maxBurnTime.setObject(TileEntityFurnace.getItemBurnTime(stack));
					burnTime.increaseBy(1);;
					this.slots()[0].stackSize--;

					if (this.slots()[0].stackSize <= 0) {
						this.slots()[0] = null;
					}
				}
			}

		}
		if (burnTime.getObject() > 0 && !(burnTime.getObject() == maxBurnTime.getObject())) {
			this.storage.receiveEnergy(energyMultiplier, false);
			burnTime.increaseBy(1);;
		}
		if (maxBurnTime.getObject() != 0 && burnTime.getObject() == maxBurnTime.getObject()) {
			this.storage.receiveEnergy(energyMultiplier, false);
			burnTime.setObject(0);
			this.removeItem(requiredLevel);
		}

	}

	public void processItemLevel() {
		ItemStack stack = this.slots()[1];
		if (stack == null || !(getItemValue(stack) > 0)) {
			return;
		}
		if (!(itemLevel.getObject() + getItemValue(stack) > levelMax)) {
			addItem(getItemValue(stack));
			this.slots()[1].stackSize--;
			if (this.slots()[1].stackSize <= 0) {
				this.slots()[1] = null;
			}
		}

	}

	public int pushEnergy(int recieve, boolean simulate) {
		for (int i = 0; i < 6; i++) {
			if (this.handlers[i] != null) {
				if (handlers[i] instanceof IEnergyReceiver) {
					recieve -= ((IEnergyReceiver) this.handlers[i]).receiveEnergy(EnumFacing.VALUES[(i ^ 0x1)], recieve, simulate);
				}
			}
		}
		return recieve;
	}

	public void updateAdjacentHandlers() {
		for (int i = 0; i < 6; i++) {
			TileEntity te = SonarHelper.getAdjacentTileEntity(this, EnumFacing.getFront(i));
			if (!(te instanceof TileEntityFlux)) {
				if (SonarHelper.isEnergyHandlerFromSide(te, EnumFacing.VALUES[(i ^ 0x1)])) {
					this.handlers[i] = te;
				} else
					this.handlers[i] = null;
			}
		}
	}

	public void onLoaded() {
		super.onLoaded();
		this.updateAdjacentHandlers();
	}

	public abstract int getItemValue(ItemStack stack);

	public void addSyncParts(List<ISyncPart> parts) {
		super.addSyncParts(parts);
		parts.addAll(Lists.newArrayList(itemLevel, burnTime, maxBurnTime));
	}
	
	public void addItem(int add) {
		itemLevel.increaseBy(add);
	}

	public void removeItem(int remove) {
		itemLevel.increaseBy(-remove);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (slot == 0) {
			if (TileEntityFurnace.isItemFuel(stack)) {
				return true;
			}
		}
		if (slot == 1) {
			if (getItemValue(stack) > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return side == EnumFacing.DOWN ? slotsSides : (side == EnumFacing.UP ? slotsTop : slotsSides);
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, EnumFacing direction) {
		return this.isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, EnumFacing direction) {
		return direction != EnumFacing.DOWN || slot != 1 || stack != null && stack.getItem() == Items.bucket;
	}

	public static class StarchExtractor extends TileEntityGenerator {
		public StarchExtractor() {
			super.energyMultiplier = CalculatorConfig.getInteger("Starch Extractor");
		}

		@SideOnly(Side.CLIENT)
		public List<String> getWailaInfo(List<String> currenttip) {
			currenttip.add(FontHelper.translate("generator.starch") + ": " + this.itemLevel.getObject() * 100 / 5000 + "%");
			return currenttip;
		}

		@Override
		public int getItemValue(ItemStack stack) {
			return (Integer) StarchExtractorRecipes.instance().getOutput(stack);
		}


		@Override
		public Object getGuiScreen(EntityPlayer player) {
			return new GuiExtractor.Starch(player.inventory, this);
		}
	}

	public static class RedstoneExtractor extends TileEntityGenerator {
		public RedstoneExtractor() {
			super.energyMultiplier = CalculatorConfig.getInteger("Redstone Extractor");
		}

		public int getItemValue(ItemStack stack) {
			return (Integer) RedstoneExtractorRecipes.instance().getOutput(stack);
		}

		@SideOnly(Side.CLIENT)
		public List<String> getWailaInfo(List<String> currenttip) {
			currenttip.add(FontHelper.translate("generator.redstone") + ": " + this.itemLevel.getObject() * 100 / 5000 + "%");
			return currenttip;
		}

		@Override
		public Object getGuiScreen(EntityPlayer player) {
			return new GuiExtractor.Redstone(player.inventory, this);
		}
	}

	public static class GlowstoneExtractor extends TileEntityGenerator {
		public GlowstoneExtractor() {
			super.energyMultiplier = CalculatorConfig.getInteger("Glowstone Extractor");
		}

		public int getItemValue(ItemStack stack) {
			return (Integer) GlowstoneExtractorRecipes.instance().getOutput(stack);
		}

		@SideOnly(Side.CLIENT)
		public List<String> getWailaInfo(List<String> currenttip) {
			currenttip.add(FontHelper.translate("generator.glowstone") + ": " + this.itemLevel.getObject() * 100 / 5000 + "%");
			return currenttip;
		}

		@Override
		public Object getGuiScreen(EntityPlayer player) {
			return new GuiExtractor.Glowstone(player.inventory, this);
		}
	}

	@Override
	public Object getGuiContainer(EntityPlayer player) {
		return new ContainerExtractor(player.inventory, this);
	}
}
