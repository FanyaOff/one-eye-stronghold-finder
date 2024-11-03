package net.hopper4et.oneeyestrongholdfinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.entity.EntityType;

public class OneEyeStrongholdFinderClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity.getType() == EntityType.EYE_OF_ENDER) new Thread(new MainThread(entity)).start();
		});
	}
}