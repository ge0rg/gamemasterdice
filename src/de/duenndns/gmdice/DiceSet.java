// GameMaster Dice
// Copyright (C) 2011 Georg Lukas
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

package de.duenndns.gmdice;

import java.util.Random;

/** DiceSet is the implementation of a set of dice
 *
 * a dice set is specified by three parameters:
 *
 * @param count number of dice
 * @param sides number of sides each die has
 * @param modifier value to add/subtract to the result
 */
public class DiceSet {
	public static String DSA = "DSA";
	public int count;
	public int sides;
	public int modifier;
	boolean dsa;

	public DiceSet(int c, int s, int m) {
		count = c;
		sides = s;
		modifier = m;
	}

	public DiceSet() {
		this(1, 6, 0);
	}

	public DiceSet(String set) {
		if (set.equals(DSA)) {
			dsa = true;
			count = 3;
			sides = 20;
			modifier = 0;
		} else {
			String[] parts = set.split("[d+-]");
			count = Integer.parseInt(parts[0]);
			sides = Integer.parseInt(parts[1]);
			if (parts.length > 2) {
				modifier = Integer.parseInt(parts[2]);
				if (set.indexOf('-') >= 0)
					modifier = -modifier;
			}
		}
	}

	public String toString() {
		if (dsa)
			return DSA;
		else if (modifier == 0)
			return String.format("%dd%d", count, sides);
		else
			return String.format("%dd%d%+d", count, sides, modifier);
	}

	public String rollDSA(Random gen) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			int roll1 = gen.nextInt(sides) + 1;
			sb.append(roll1);
			if (i < count-1)
				sb.append(" · ");
		}
		return sb.toString();
	}

	public String roll(Random gen) {
		if (dsa)
			return rollDSA(gen);

		int result = 0;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			int roll1 = gen.nextInt(sides) + 1;
			sb.append(roll1);
			result += roll1;
			if (i < count-1)
				sb.append("+");
		}
		if (modifier != 0) {
			sb.append(String.format("%+d", modifier));
			result += modifier;
		}
		if (count > 1 || modifier != 0) {
			sb.append(" = ");
			sb.append(result);
		}
		return sb.toString();
	}
}

