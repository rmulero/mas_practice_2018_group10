/**
 * IMAS base code for the practical work.
 * Copyright (C) 2016 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.ontology;

/**
 * Type of metal.
 * It provides a way of representing this type of metal.
 */
public enum WasteType {
    MUNICIPAL {
        @Override
        public String getShortString() {
            return "M";
        }
    },
    INDUSTRIAL {
        @Override
        public String getShortString() {
            return "I";
        }
    };

    /**
     * Gets a letter representation of this type of waste
     * @return String a single letter representing the type of garbage.
     */
    public abstract String getShortString();

    /**
     * Gets the waste type according to its short string.
     * @param type of waste in short string format.
     * @return The type of of waste
     */
    public static WasteType fromShortString(String type) {
        switch (type) {
			case "I": return INDUSTRIAL;
			case "M": return MUNICIPAL;
            default:
                throw new IllegalArgumentException("Waste type '" + type + "' is not supported.");
        }
    }
}
