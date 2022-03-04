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

package com.winterhavenmc.deathcompass.util;

public enum BukkitTime {

	MILLISECONDS(1L),
	TICKS(50L),
	SECONDS(1000L),
	MINUTES(60000L),
	HOURS(3600000L),
	DAYS(86400000L),
	WEEKS(604800000L),
	MONTHS(2629800000L),
	YEARS(31557600000L);

	private final long millis;


	BukkitTime(final long millis) {
		this.millis = millis;
	}

	public final long toMillis(final long duration) {
		return convert(duration, MILLISECONDS);
	}

	public final long toTicks(final long duration) {
		return convert(duration, TICKS);
	}

	public final long toSeconds(final long duration) {
		return convert(duration, SECONDS);
	}

	public final long toMinutes(final long duration) {
		return convert(duration, MINUTES);
	}

	public final long toHours(final long duration) {
		return convert(duration, HOURS);
	}

	public final long toDays(final long duration) {
		return convert(duration, DAYS);
	}

	public final long toWeeks(final long duration) {
		return convert(duration, WEEKS);
	}

	public final long toMonths(final long duration) {
		return convert(duration, MONTHS);
	}

	public final long toYears(final long duration) {
		return convert(duration, YEARS);
	}

	public final long convert(long duration, BukkitTime unit) {
		if (duration < Long.MIN_VALUE / this.millis) {
			throw new IllegalArgumentException("duration of " + duration + " " + this + " would cause an underflow in conversion to " + unit + ".");
		}
		if (duration > Long.MAX_VALUE / this.millis) {
			throw new IllegalArgumentException("duration of " + duration + " " + this + " would cause an overflow in conversion to " + unit + ".");
		}
		return duration * this.millis / unit.millis;
	}

	/**
	 * Get the number of milliseconds for each time unit.
	 *
	 * @return the number of milliseconds equal to each time unit
	 */
	public final long getMillis() {
		return this.millis;
	}

}
