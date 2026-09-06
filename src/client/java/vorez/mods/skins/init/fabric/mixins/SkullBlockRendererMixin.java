package vorez.mods.skins.init.fabric.mixins;

import vorez.mods.skins.init.fabric.FabricOfflineSkinsReloaded;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private static RenderType offlineSkinsResolveSkullRenderType(
            RenderType original,
            SkullBlock.Type type,
            ResolvableProfile resolvableProfile
    ) {
        if (type != SkullBlock.Types.PLAYER || resolvableProfile == null) {
            return original;
        }

        ResourceLocation loc = FabricOfflineSkinsReloaded.getLocationSkin(
                resolvableProfile.gameProfile()
        );

        if (loc != null) {
            return RenderType.entityTranslucent(loc);
        }

        return original;
    }
}
