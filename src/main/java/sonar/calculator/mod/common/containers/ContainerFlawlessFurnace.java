package sonar.calculator.mod.common.containers;

import ic2.api.item.IElectricItem;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.calculator.mod.common.item.calculators.CalculatorItem;
import sonar.calculator.mod.common.tileentity.TileEntityAbstractProcess;
import sonar.calculator.mod.common.tileentity.machines.TileEntityFlawlessFurnace;
import sonar.core.inventory.ContainerSync;
import sonar.core.utils.DischargeValues;
import sonar.core.utils.SlotBlockedInventory;
import sonar.core.utils.SonarAPI;
import cofh.api.energy.IEnergyContainerItem;

public class ContainerFlawlessFurnace extends ContainerSync {
	private TileEntityFlawlessFurnace entity;

	private static final int INV_START = 27, INV_END = INV_START + 26, HOTBAR_START = INV_END + 1, HOTBAR_END = HOTBAR_START + 8;

	public ContainerFlawlessFurnace(InventoryPlayer inventory, TileEntityFlawlessFurnace entity) {
		super(entity);
		this.entity = entity;

		int slotID = 0;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				addSlotToContainer(new Slot(entity, slotID, 14 + j * 56, 27 + i * 40));
				slotID++;
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				addSlotToContainer(new Slot(entity, slotID, 38 + j * 56, 19 + i * 40));
				slotID++;
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				addSlotToContainer(new Slot(entity, slotID, 38 + j * 56, 39 + i * 40));
				slotID++;
			}
		}
		addSlotToContainer(new Slot(entity, 27, 28, 136));
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 160 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 218));
		}
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotID);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotID < INV_START) {
				if (!this.mergeItemStack(itemstack1, INV_START, HOTBAR_END + 1, true)) {
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else {

				if (slotID >= INV_START) {
					if (entity.getOutput(true, itemstack1)[0] != null) {
						if (!this.mergeItemStack(itemstack1, 0, INV_START - 18 + 1, false)) {
							return null;
						}
					}
				} else if (slotID >= INV_START && slotID < HOTBAR_START) {
					if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END + 1, false)) {
						return null;
					}
				} else if (slotID >= HOTBAR_START && slotID < HOTBAR_END + 1) {
					if (!this.mergeItemStack(itemstack1, INV_START, INV_END + 1, false)) {
						return null;
					}
				}
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return entity.isUseableByPlayer(player);
	}

}
