/*
 * Copyright (c) 2021. Increff
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.increff.commons.gelf;

/**
 * The log level of a {@link GelfRequest}.
 */
public enum GelfLevel {
	EMERGENCY(0), ALERT(1), CRITICAL(2), ERROR(3), WARNING(4), NOTICE(5), INFO(6), DEBUG(7);

	private final int numericLevel;

	GelfLevel(final int numericLevel) {
		this.numericLevel = numericLevel;
	}

	/**
	 * Get the numerical level of a {@link GelfRequest}.
	 *
	 * @return the numerical level
	 */
	public int getNumericLevel() {
		return numericLevel;
	}

	public static GelfLevel fromNumericLevel(final int level) {
		for (final GelfLevel gelfMessageLevel : GelfLevel.values()) {
			if (level == gelfMessageLevel.getNumericLevel()) {
				return gelfMessageLevel;
			}
		}

		throw new IllegalArgumentException("Unknown GELF message level: " + level);
	}

	public String toString() {
		return name() + "(" + numericLevel + ")";
	}
}
