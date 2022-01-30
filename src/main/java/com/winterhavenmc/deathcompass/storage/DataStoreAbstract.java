/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathcompass.storage;


/**
 * Abstract class declares methods that need to be implemented for plugin data stores.
 */
abstract class DataStoreAbstract {

	// datastore initialized state
	private boolean initialized;

	// datastore type
	DataStoreType type;


	/**
	 * Check if datastore is initialize
	 *
	 * @return {@code true} if datastore is initialized, {@code false} if datastore is not initialized
	 */
	public boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set initialized state of datastore
	 *
	 * @param initialized boolean for initialized state of datastore
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}


	/**
	 * Get datastore type
	 *
	 * @return DataStoreType - the datastore type for this datastore
	 */
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Override toString method to return the datastore type name
	 *
	 * @return the name of this datastore instance
	 */
	@Override
	public String toString() {
		return this.type.toString();
	}

}
