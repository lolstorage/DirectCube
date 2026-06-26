package direct.cube;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import org.lwjgl.system.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DirectCube implements ClientModInitializer {
	public static final String MOD_ID = "directcube";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("DirectCube: Hijacking OpenGL and initializing Pure D3D9 Backend...");

		try {
			// 1. Create a temporary folder for your D3D9 native shim
			File tempDir = new File(System.getProperty("java.io.tmpdir"), "directcube_natives");
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
			
			File nativeDll = new File(tempDir, "directcube_core.dll");

			// 2. Extract the compiled C++ DLL from your mod resources
			try (InputStream is = DirectCube.class.getResourceAsStream("/assets/directcube/natives/directcube_core.dll")) {
				if (is == null) {
					throw new RuntimeException("Could not find directcube_core.dll inside jar resources!");
				}
				Files.copy(is, nativeDll.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			// 3. Force LWJGL to route all OpenGL 3.3 Core calls into your D3D9 shim
			Configuration.OPENGL_LIBRARY_NAME.set(nativeDll.getAbsolutePath());
			
			LOGGER.info("DirectCube: Successfully bound LWJGL library path to: " + nativeDll.getAbsolutePath());

		} catch (Exception e) {
			LOGGER.error("DirectCube: CRITICAL - Failed to hijack OpenGL library context!", e);
		}
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}