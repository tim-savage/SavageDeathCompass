package com.winterhaven_mc.deathcompass;

public enum ConfigSetting {

	LANGUAGE("en-US"),
	ENABLED_WORLDS("[]"),
	DISABLED_WORLDS("[disabled_world1, disabled_world2]"),
	DESTROY_ON_DROP("true"),
	PREVENT_STORAGE("true"),
	TARGET_DELAY("20"),
	SOUND_EFFECTS("true"),
	;

	private final String value;

	ConfigSetting(String value) {
		this.value = value;
	}

	public String getKey() {
		return this.name().toLowerCase().replace('_', '-');
	}

	public String getValue() {
		return this.value;
	}

}
