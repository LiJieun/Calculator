package sonar.calculator.mod.common.tileentity.machines;

import net.minecraft.entity.player.EntityPlayer;
import sonar.calculator.mod.CalculatorConfig;
import sonar.calculator.mod.client.gui.machines.GuiPowerCube;
import sonar.calculator.mod.common.containers.ContainerPowerCube;
import sonar.core.common.tileentity.TileEntityEnergyInventory;
import sonar.core.inventory.SonarTileInventory;
import sonar.core.network.sync.SyncEnergyStorage;
import sonar.core.utils.IGuiTile;

public class TileEntityPowerCube extends TileEntityEnergyInventory implements IGuiTile {

	public TileEntityPowerCube() {
		super.storage = new SyncEnergyStorage(CalculatorConfig.getInteger("Standard Machine"), 200);
		super.inv = new SonarTileInventory(this, 2);
		super.energyMode = EnergyMode.RECIEVE;
		super.maxTransfer = 1;
	}

	@Override
	public void update() {
		super.update();
		charge(0);
		discharge(1);		
		this.markDirty();
	}

	@Override
	public Object getGuiContainer(EntityPlayer player) {
		return new ContainerPowerCube(player.inventory, this);
	}

	@Override
	public Object getGuiScreen(EntityPlayer player) {
		return new GuiPowerCube(player.inventory, this);
	}

}
