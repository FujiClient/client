package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.utils.Sound;
import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.injection.interfaces.IMixinShaderGroup;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventShader;
import me.eldodebug.soar.management.event.impl.EventUpdateDisplay;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;

public class MotionBlurMod extends Mod {

    private long lastCheck = 0L;
    
	private ResourceLocation motion_blur = new ResourceLocation("minecraft:shaders/post/motion_blur.json");
	private ResourceLocation motion_blur_gaussian = new ResourceLocation("minecraft:shaders/post/motion_blur_gaussian.json");
	private ResourceLocation motion_blur_box = new ResourceLocation("minecraft:shaders/post/motion_blur_box.json");
	private ShaderGroup group;
	private float groupBlur;
	private boolean loaded;
	private int prevWidth, prevHeight;
	
	private ComboSetting typeSetting = new ComboSetting(TranslateText.TYPE, this, TranslateText.SHADER, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.ACCUM),
			new Option(TranslateText.SHADER),
			new Option("GAUSSIAN"),
			new Option("BOX")
	)));

	private NumberSetting amountSetting = new NumberSetting(TranslateText.AMOUNT, this, 0.5, 0.1, 0.9, false);
	
	public MotionBlurMod() {
		super(TranslateText.MOTION_BLUR, TranslateText.MOTION_BLUR_DESCRIPTION, ModCategory.RENDER);
	}
	
	@Override
	public void setup() {
		loaded = false;
	}

	@EventTarget
	public void onShader(EventShader event) {
		
		ScaledResolution sr = new ScaledResolution(mc);

		String type = typeSetting.getOption().getTranslate();
		ResourceLocation shaderRes = null;
		if(type.equals(TranslateText.SHADER)) {
			shaderRes = motion_blur;
		} else if(type.equalsIgnoreCase("GAUSSIAN")) {
			shaderRes = motion_blur_gaussian;
		} else if(type.equalsIgnoreCase("BOX")) {
			shaderRes = motion_blur_box;
		}

		if(shaderRes != null) {
			if(group == null || prevWidth != sr.getScaledWidth() || prevHeight != sr.getScaledHeight() || !group.getShaderGroupName().equals(shaderRes.toString())) {
				prevWidth = sr.getScaledWidth();
				prevHeight = sr.getScaledHeight();
				groupBlur = amountSetting.getValueFloat();
				try {
					group = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shaderRes);
					group.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			if(groupBlur != amountSetting.getValueFloat() || !loaded) {
				loaded = true;
				((IMixinShaderGroup) group).getListShaders().forEach((shader) -> {
					ShaderUniform factor = shader.getShaderManager().getShaderUniform("BlurFactor");
					if(factor != null) {
						factor.set(amountSetting.getValueFloat());
					}
				});
				groupBlur = amountSetting.getValueFloat();
			}
			event.getGroups().add(group);
		}
	}
	
	@EventTarget
	public void onUpdateDisplay(EventUpdateDisplay event) {
		
		if(typeSetting.getOption().getTranslate().equals(TranslateText.ACCUM)) {
			
			if(group != null) {
				group = null;
				loaded = false;
			}
			
			if(mc.thePlayer != null) {
				
		        GL11.glAccum(259, amountSetting.getValueFloat());
		        GL11.glAccum(256, 1.0f - amountSetting.getValueFloat());
		        GL11.glAccum(258, 1.0f);
		        
		        if (lastCheck + 1000L < System.currentTimeMillis()) {
		        	lastCheck = System.currentTimeMillis();
		        	
		            int error = GL11.glGetError();
		            
		            if (error == 1282) {
		            	this.setToggled(false);
						try{
							Sound.play("soar/audio/error.wav", false);
						} catch (Exception ignored) {}
		            }
		        }
			}
		}
	}
	
	@Override
	public void onEnable() {
		group = null;
		super.onEnable();
	}
}
