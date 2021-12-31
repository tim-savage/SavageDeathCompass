package com.winterhaven_mc.deathcompass;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeathCompassTests {

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

    @Test
    @DisplayName("mock server not null.")
    void MockServerNotNull() {
        Assertions.assertNotNull(server);
    }

    @Test
    @DisplayName("mock plugin not null.")
    void MockPluginNotNull() {
        Assertions.assertNotNull(plugin);
    }



    @Nested
    @DisplayName("Test config objects.")
    class ConfigTests {

        @Test
        @DisplayName("plugin config not null.")
        void ConfigNotNull() {
            Assertions.assertNotNull(plugin.getConfig());
        }
    }

    @Test
    @DisplayName("language manager not null.")
    void LanguageManagerNotNull() {
        Assertions.assertNotNull(plugin.messageBuilder);
    }

    @Test
    @DisplayName("sound config not null.")
    void SoundConfigNotNull() {
        Assertions.assertNotNull(plugin.soundConfig);
    }

    @Test
    @DisplayName("world manager not null.")
    void WorldManagerNotNull() {
        Assertions.assertNotNull(plugin.worldManager);
    }

}

