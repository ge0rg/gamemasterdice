// GameMaster Dice
// Copyright (C) 2014 David Pflug
// Copyright (C) 2011-2014 Georg Lukas
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

import android.content.Context; // needed for translation strings

import java.util.Random;

public class FUDGEDiceSet extends DiceSet {

	public FUDGEDiceSet(int c, int m) {
		count = c;
		sides = DiceSet.FUDGE; // The purist in me wants 6, but there's no good reason to.
		modifier = m;
	}
	public FUDGEDiceSet() {
		this(4, 0);
	}

	public String roll(Context ctx, Random gen) {
		StringBuilder sb = new StringBuilder();
		int total;
		total = 0;
		for (int i = 0; i < count; i++) {
			int roll1 = gen.nextInt(3) + 1;
			if (roll1 == 1) {
				sb.append("-");
				total--;
			} else if (roll1 == 3) {
				sb.append("+");
				total++;
			} else {
				sb.append("0");
			}
			sb.append(" ");
		}
		if (modifier != 0) {
			sb.append(String.format("%+d ", modifier));
			total += modifier;
		}
		sb.append("= ");
		sb.append(String.format("%+d", total));
		return sb.toString();
	}

	public String toString() {
		if (modifier == 0)
			return String.format("%ddF", count);
		else
			return String.format("%ddF%+d", count, modifier);
	}

	public int hashCode() {
		return 10040300;
	}
}
