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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import static com.winterhavenmc.deathcompass.util.BukkitTime.*;


class BukkitTimeTest {

	// these are the smallest/largest numbers that will not cause an overflow for any time unit
	private final static long MIN = Long.MIN_VALUE / YEARS.getMillis();
	private final static long MAX = Long.MAX_VALUE / YEARS.getMillis();

	// return an array of values that will be used as durations for tests
	static long[] durationProvider() {
		return new long[] {MIN, -86400001L, -3600001L, -60001L, -1001L, -51L, -1L, 0L, 1L, 51L, 1001L, 60001L, 3600001L, 86400001L, MAX};
	}


	@ParameterizedTest
	@DisplayName("Overflow tests")
	// exclude MILLISECONDS because it cannot overflow with any value of long
	@EnumSource(mode = EXCLUDE, names = "MILLISECONDS" )
	void overflowTests(final BukkitTime timeUnit) {

		// calculate values that will cause overflow for time unit
		long minOverflowValue = Long.MIN_VALUE / timeUnit.getMillis() - 1;
		long maxOverflowValue = Long.MAX_VALUE / timeUnit.getMillis() + 1;

		// negative overflow tests
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toMillis(minOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toTicks(minOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toSeconds(minOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toMinutes(minOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toHours(minOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toDays(minOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toWeeks(minOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toMonths(minOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toYears(minOverflowValue));

		// test convert method for negative overflow
		for (BukkitTime innerTimeUnit : BukkitTime.values()) {
			assertThrows(IllegalArgumentException.class, () -> timeUnit.convert(minOverflowValue, innerTimeUnit));
		}

		// positive overflow tests
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toMillis(maxOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toTicks(maxOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toSeconds(maxOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toMinutes(maxOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toHours(maxOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toDays(maxOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toWeeks(maxOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toMonths(maxOverflowValue));
		assertThrows(IllegalArgumentException.class, () -> timeUnit.toYears(maxOverflowValue));

		// test convert method for positive overflow
		for (BukkitTime innerTimeUnit : BukkitTime.values()) {
			assertThrows(IllegalArgumentException.class, () -> timeUnit.convert(maxOverflowValue, innerTimeUnit));
		}
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toMillis(final long duration) {
		assertEquals(duration, MILLISECONDS.toMillis(duration), "MILLISECONDS to millis failed with duration " + duration + ".");
		assertEquals(duration * 50L, TICKS.toMillis(duration), "TICKS to millis failed with duration " + duration + ".");
		assertEquals(duration * 1000L, SECONDS.toMillis(duration), "SECONDS to millis failed with duration " + duration + ".");
		assertEquals(duration * 60000L, MINUTES.toMillis(duration), "MINUTES to millis failed with duration " + duration + ".");
		assertEquals(duration * 3600000L, HOURS.toMillis(duration), "HOURS to millis failed with duration " + duration + ".");
		assertEquals(duration * 86400000L, DAYS.toMillis(duration), "DAYS to millis failed with duration " + duration + ".");
		assertEquals(duration * 604800000L, WEEKS.toMillis(duration), "WEEKS to millis failed with duration " + duration + ".");
		assertEquals(duration * 2629800000L, MONTHS.toMillis(duration), "MONTHS to millis failed with duration " + duration + ".");
		assertEquals(duration * 31557600000L, YEARS.toMillis(duration), "YEARS to millis failed with duration " + duration + ".");
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toTicks(final long duration) {
		assertEquals(duration / 50L, MILLISECONDS.toTicks(duration), "MILLISECONDS to ticks failed with duration " + duration + ".");
		assertEquals(duration, TICKS.toTicks(duration), "TICKS to ticks failed with duration " + duration + ".");
		assertEquals(duration * 20L, SECONDS.toTicks(duration), "SECONDS to ticks failed with duration " + duration + ".");
		assertEquals(duration * 1200L, MINUTES.toTicks(duration), "MINUTES to ticks failed with duration " + duration + ".");
		assertEquals(duration * 72000L, HOURS.toTicks(duration), "HOURS to ticks failed with duration " + duration + ".");
		assertEquals(duration * 1728000L, DAYS.toTicks(duration), "DAYS to ticks failed with duration " + duration + ".");
		assertEquals(duration * 12096000L, WEEKS.toTicks(duration), "WEEKS to ticks failed with duration " + duration + ".");
		assertEquals(duration * 52596000L, MONTHS.toTicks(duration), "MONTHS to ticks failed with duration " + duration + ".");
		assertEquals(duration * 631152000L, YEARS.toTicks(duration), "YEARS to ticks failed with duration " + duration + ".");
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toSeconds(final long duration) {
		assertEquals(duration / 1000L, MILLISECONDS.toSeconds(duration), "MILLISECONDS to seconds failed with duration " + duration + ".");
		assertEquals(duration / 20L, TICKS.toSeconds(duration), "TICKS to seconds failed with duration " + duration + ".");
		assertEquals(duration, SECONDS.toSeconds(duration), "SECONDS to seconds failed with duration " + duration + ".");
		assertEquals(duration * 60L, MINUTES.toSeconds(duration), "MINUTES to seconds failed with duration " + duration + ".");
		assertEquals(duration * 3600L, HOURS.toSeconds(duration), "HOURS to seconds failed with duration " + duration + ".");
		assertEquals(duration * 86400L, DAYS.toSeconds(duration), "DAYS to seconds failed with duration " + duration + ".");
		assertEquals(duration * 604800L, WEEKS.toSeconds(duration), "WEEKS to seconds failed with duration " + duration + ".");
		assertEquals(duration * 2629800L, MONTHS.toSeconds(duration), "MONTHS to seconds failed with duration " + duration + ".");
		assertEquals(duration * 31557600L, YEARS.toSeconds(duration), "YEARS to seconds failed with duration " + duration + ".");
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toMinutes(final long duration) {
		assertEquals(duration / 60000L, MILLISECONDS.toMinutes(duration), "MILLISECONDS to minutes failed with duration " + duration + ".");
		assertEquals(duration / 1200L, TICKS.toMinutes(duration), "TICKS to minutes failed with duration " + duration + ".");
		assertEquals(duration / 60L, SECONDS.toMinutes(duration), "SECONDS to minutes failed with duration " + duration + ".");
		assertEquals(duration, MINUTES.toMinutes(duration), "MINUTES to minutes failed with duration " + duration + ".");
		assertEquals(duration * 60L, HOURS.toMinutes(duration), "HOURS to minutes failed with duration " + duration + ".");
		assertEquals(duration * 1440L, DAYS.toMinutes(duration), "DAYS to minutes failed with duration " + duration + ".");
		assertEquals(duration * 10080L, WEEKS.toMinutes(duration), "DAYS to minutes failed with duration " + duration + ".");
		assertEquals(duration * 43830L, MONTHS.toMinutes(duration), "MONTHS to minutes failed with duration " + duration + ".");
		assertEquals(duration * 525960L, YEARS.toMinutes(duration), "YEARS to minutes failed with duration " + duration + ".");
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toHours(final long duration) {
		assertEquals(duration / 3600000L, MILLISECONDS.toHours(duration), "MILLISECONDS to hours failed with duration " + duration + ".");
		assertEquals(duration / 72000L, TICKS.toHours(duration), "TICKS to hours failed with duration " + duration + ".");
		assertEquals(duration / 3600L, SECONDS.toHours(duration), "SECONDS to hours failed with duration " + duration + ".");
		assertEquals(duration / 60L, MINUTES.toHours(duration), "MINUTES to hours failed with duration " + duration + ".");
		assertEquals(duration, HOURS.toHours(duration), "HOURS to hours failed with duration " + duration + ".");
		assertEquals(duration * 24L, DAYS.toHours(duration), "DAYS to hours failed with duration " + duration + ".");
		assertEquals(duration * 168L, WEEKS.toHours(duration), "WEEKS to hours failed with duration " + duration + ".");
		assertEquals(duration * 43830L / 60L, MONTHS.toHours(duration), "MONTHS to hours failed with duration " + duration + ".");
		assertEquals(duration * 8766L, YEARS.toHours(duration), "YEARS to hours failed with duration " + duration + ".");
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toDays(final long duration) {
		assertEquals(duration / 86400000L, MILLISECONDS.toDays(duration), "MILLISECONDS to days failed with duration " + duration + ".");
		assertEquals(duration / 1728000L, TICKS.toDays(duration), "TICKS to days failed with duration " + duration + ".");
		assertEquals(duration / 86400L, SECONDS.toDays(duration), "SECONDS to days failed with duration " + duration + ".");
		assertEquals(duration / 1440L, MINUTES.toDays(duration), "MINUTES to days failed with duration " + duration + ".");
		assertEquals(duration / 24L, HOURS.toDays(duration), "HOURS to days failed with duration " + duration + ".");
		assertEquals(duration, DAYS.toDays(duration), "DAYS to days failed with duration " + duration + ".");
		assertEquals(duration * 7L, WEEKS.toDays(duration), "WEEKS to days failed with duration " + duration + ".");
		assertEquals(duration * 43830L / 1440L, MONTHS.toDays(duration), "MONTHS to days failed with duration " + duration + ".");
		assertEquals(duration * 8766L / 24L, YEARS.toDays(duration), "YEARS to days failed with duration " + duration + ".");
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toWeeks(final long duration) {
		assertEquals(duration / 604800000L, MILLISECONDS.toWeeks(duration), "MILLISECONDS to weeks failed with duration " + duration + ".");
		assertEquals(duration / 12096000L, TICKS.toWeeks(duration), "TICKS to weeks failed with duration " + duration + ".");
		assertEquals(duration / 604800L, SECONDS.toWeeks(duration), "SECONDS to weeks failed with duration " + duration + ".");
		assertEquals(duration / 10080L, MINUTES.toWeeks(duration), "MINUTES to weeks failed with duration " + duration + ".");
		assertEquals(duration / 168L, HOURS.toWeeks(duration), "HOURS to weeks failed with duration " + duration + ".");
		assertEquals(duration / 7L, DAYS.toWeeks(duration), "DAYS to weeks failed with duration " + duration + ".");
		assertEquals(duration, WEEKS.toWeeks(duration), "WEEKS to weeks failed with duration " + duration + ".");
		assertEquals(duration * 43830L / 10080L, MONTHS.toWeeks(duration), "MONTHS to weeks failed with duration " + duration + ".");
		assertEquals(duration * 8766L / 168L, YEARS.toWeeks(duration), "YEARS to weeks failed with duration " + duration + ".");
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toMonths(final long duration) {
		assertEquals(duration / 2629800000L, MILLISECONDS.toMonths(duration), "MILLISECONDS to months failed with duration " + duration + ".");
		assertEquals(duration / 52596000L, TICKS.toMonths(duration), "TICKS to months failed with duration " + duration + ".");
		assertEquals(duration / 2629800L, SECONDS.toMonths(duration), "SECONDS to months failed with duration " + duration + ".");
		assertEquals(duration / 43830L, MINUTES.toMonths(duration), "MINUTES to months failed with duration " + duration + ".");
		assertEquals(duration * 60L / 43830L, HOURS.toMonths(duration), "HOURS to months failed with duration " + duration + ".");
		assertEquals(duration * 1440L / 43830L, DAYS.toMonths(duration), "DAYS to months failed with duration " + duration + ".");
		assertEquals(duration * 10080L / 43830L, WEEKS.toMonths(duration), "WEEKS to months failed with duration " + duration + ".");
		assertEquals(duration, MONTHS.toMonths(duration), "MONTHS to months failed with duration " + duration + ".");
		assertEquals(duration * 12L, YEARS.toMonths(duration), "YEARS to months failed with duration " + duration + ".");
	}


	@ParameterizedTest
	@MethodSource("durationProvider")
	void toYears(final long duration) {
		assertEquals(duration / 31557600000L, MILLISECONDS.toYears(duration), "MILLISECONDS to years failed with duration " + duration + ".");
		assertEquals(duration / 631152000L, TICKS.toYears(duration), "TICKS to years failed with duration " + duration + ".");
		assertEquals(duration / 31557600L, SECONDS.toYears(duration), "SECONDS to years failed with duration " + duration + ".");
		assertEquals(duration / 525960L, MINUTES.toYears(duration), "MINUTES to years failed with duration " + duration + ".");
		assertEquals(duration / 8766L, HOURS.toYears(duration), "HOURS to years failed with duration " + duration + ".");
		assertEquals(duration * 24L / 8766L, DAYS.toYears(duration), "DAYS to years failed with duration " + duration + ".");
		assertEquals(duration * 168L / 8766L, WEEKS.toYears(duration), "WEEKS to years failed with duration " + duration + ".");
		assertEquals(duration / 12L, MONTHS.toYears(duration), "MONTHS to years failed with duration " + duration + ".");
		assertEquals(duration, YEARS.toYears(duration), "YEARS to years failed with duration " + duration + ".");
	}

	@ParameterizedTest
	@EnumSource(BukkitTime.class)
	@DisplayName("convert method tests")
	void convert(final BukkitTime timeUnit) {
		for (Long duration : durationProvider()) {
			assertEquals(duration * timeUnit.getMillis(), timeUnit.convert(duration, MILLISECONDS), "convert " + timeUnit + " to milliseconds failed.");
			assertEquals(duration * timeUnit.getMillis() / 50L, timeUnit.convert(duration, TICKS), "convert " + timeUnit + " to ticks failed.");
			assertEquals(duration * timeUnit.getMillis() / 1000L, timeUnit.convert(duration, SECONDS), "convert " + timeUnit + " to seconds failed.");
			assertEquals(duration * timeUnit.getMillis() / 60000L, timeUnit.convert(duration, MINUTES), "convert " + timeUnit + " to minutes failed.");
			assertEquals(duration * timeUnit.getMillis() / 3600000L, timeUnit.convert(duration, HOURS), "convert " + timeUnit + " to hours failed.");
			assertEquals(duration * timeUnit.getMillis() / 86400000L, timeUnit.convert(duration, DAYS), "convert " + timeUnit + " to days failed.");
			assertEquals(duration * timeUnit.getMillis() / 604800000L, timeUnit.convert(duration, WEEKS), "convert " + timeUnit + " to days failed.");
			assertEquals(duration * timeUnit.getMillis() / 2629800000L, timeUnit.convert(duration, MONTHS), "convert " + timeUnit + " to days failed.");
			assertEquals(duration * timeUnit.getMillis() / 31557600000L, timeUnit.convert(duration, YEARS), "convert " + timeUnit + " to days failed.");
		}
	}


	@Test
	void values() {
		BukkitTime[] bukkitTimes = { MILLISECONDS, TICKS, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS };
		Assertions.assertArrayEquals(BukkitTime.values(), bukkitTimes, "BukkitTime values did not match.");

		// check that millis field holds correct value for each time unit
		assertEquals(1L, MILLISECONDS.getMillis());
		assertEquals(50L, TICKS.getMillis());
		assertEquals(1000L, SECONDS.getMillis());
		assertEquals(1000L * 60L, MINUTES.getMillis());
		assertEquals(1000L * 60L * 60L, HOURS.getMillis());
		assertEquals(1000L * 60L * 60L * 24L, DAYS.getMillis());
		assertEquals(1000L * 60L * 60L * 24L * 7L, WEEKS.getMillis());
		assertEquals(1000L * 60L * 43830L, MONTHS.getMillis());
		assertEquals(1000L * 60L * 60L * 8766L, YEARS.getMillis());
	}


	@Test
	void valueOf() {
		// test all valid member names
		assertEquals(MILLISECONDS, BukkitTime.valueOf("MILLISECONDS"));
		assertEquals(TICKS, BukkitTime.valueOf("TICKS"));
		assertEquals(SECONDS, BukkitTime.valueOf("SECONDS"));
		assertEquals(MINUTES, BukkitTime.valueOf("MINUTES"));
		assertEquals(HOURS, BukkitTime.valueOf("HOURS"));
		assertEquals(DAYS, BukkitTime.valueOf("DAYS"));
		assertEquals(WEEKS, BukkitTime.valueOf("WEEKS"));
		assertEquals(YEARS, BukkitTime.valueOf("YEARS"));

		// test invalid member name
		Exception exception = Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> BukkitTime.valueOf("invalid"));

		String expectedMessage = "No enum constant";
		String actualMessage = exception.getMessage();

		Assertions.assertTrue(actualMessage.startsWith(expectedMessage));
	}

}
