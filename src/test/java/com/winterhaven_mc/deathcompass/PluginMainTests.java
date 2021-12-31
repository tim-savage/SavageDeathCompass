package com.winterhaven_mc.deathcompass;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PluginMainTests {

	private ServerMock server;
	private PluginMain plugin;

	@BeforeAll
	public void setUp() {
		// Start the mock server
		server = MockBukkit.mock();

		// start the mock plugin
		plugin = MockBukkit.load(PluginMain.class);

	}

	@AfterAll
	public void tearDown() {
		// Stop the mock server
		MockBukkit.unmock();
	}

	@Nested
	@DisplayName("Test mock objects.")
	class MockingTests {

		@Test
		@DisplayName("server is not null.")
		void ServerNotNull() {
			Assertions.assertNotNull(server, "server is null.");
		}

		@Test
		@DisplayName("plugin not null.")
		void PluginNotNull() {
			Assertions.assertNotNull(plugin, "plugin is null.");
		}

		@Test
		@DisplayName("plugin enabled.")
		void PluginEnabled() {
			Assertions.assertTrue(plugin.isEnabled(), "plugin not enabled.");
		}

		@Test
		@DisplayName("plugin data folder not null.")
		void PluginDataFolderNotNull() {
			Assertions.assertNotNull(plugin.getDataFolder(),"data folder is null.");
		}
	}

	@Nested
	@DisplayName("Test plugin main objects.")
	class PluginMainObjectTests {

		@Test
		@DisplayName("language handler not null.")
		void LanguageHandlerNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder, "language handler is null.");
		}

		@Test
		@DisplayName("world manager not null.")
		void WorldManagerNotNull() {
			Assertions.assertNotNull(plugin.worldManager, "world manager is null.");
		}

		@Test
		@DisplayName("sound config not null.")
		void SoundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig,"sound config is null.");
		}

	}


	@Nested
	@DisplayName("Test plugin config.")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ConfigTests {

		Set<String> enumConfigKeyStrings = new HashSet<>();

		public ConfigTests() {
			for (ConfigSetting configSetting : ConfigSetting.values()) {
				this.enumConfigKeyStrings.add(configSetting.getKey());
			}
		}

		@Test
		@DisplayName("config not null.")
		void ConfigNotNull() {
			Assertions.assertNotNull(plugin.getConfig(),
					"plugin config is null.");
		}

		@Test
		@DisplayName("test configured language.")
		void GetLanguage() {
			Assertions.assertEquals("en-US", plugin.getConfig().getString("language"),
					"language does not equal 'en-US'");
		}

		@SuppressWarnings("unused")
		Set<String> ConfigFileKeys() {
			return plugin.getConfig().getKeys(false);
		}

		@ParameterizedTest
		@DisplayName("file config key is contained in ConfigSetting enum.")
		@MethodSource("ConfigFileKeys")
		void ConfigFileKeyNotNull(String key) {
			Assertions.assertNotNull(key);
			Assertions.assertTrue(enumConfigKeyStrings.contains(key),
					"file config key is not contained in ConfigSetting enum.");
		}

		@ParameterizedTest
		@EnumSource(ConfigSetting.class)
		@DisplayName("ConfigSetting enum matches config file key/value pairs.")
		void ConfigFileKeysContainsEnumKey(ConfigSetting configSetting) {
			Assertions.assertEquals(configSetting.getValue(), plugin.getConfig().getString(configSetting.getKey()),
					"ConfigSetting enum key '" + configSetting.getKey() + "' does not match config file key/value pair.");
		}
	}

}
