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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class GameMasterDice extends ListActivity
		implements OnClickListener, OnLongClickListener
{
	private static String TAG = "GameMasterDice";

	// map button IDs to dice
	int button_ids[] = { R.id.die0, R.id.die1, R.id.die2, R.id.die3 };
	Button buttons[];
	Button button_more;
	TextView resultview;
	ArrayAdapter<String> resultlog;
	SharedPreferences prefs;

	DiceSet button_cfg[] = {
		new DiceSet(DiceSet.DSA),
		new DiceSet(1, 20, 0),
		new DiceSet(1, 6, 0),
		new DiceSet(1, 6, 4)
	};
	DiceCache dicecache = new DiceCache(10);
	Random generator = new Random();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_gmdice);
		setTitle(R.string.app_name_long);


		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		buttons = new Button[button_ids.length];
		for (int i = 0; i < button_ids.length; i++) {
			buttons[i] = (Button)findViewById(button_ids[i]);
			buttons[i].setOnClickListener(this);
			buttons[i].setOnLongClickListener(this);
		}
		button_more = (Button)findViewById(R.id.more);
		button_more.setOnClickListener(this);
		resultview = (TextView)findViewById(R.id.rollresult);
		resultlog = new ArrayAdapter<String>(this, R.layout.view_log);
		setListAdapter(resultlog);


		if (savedInstanceState != null) {
			getListView().onRestoreInstanceState(savedInstanceState.getParcelable("resultlog"));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadDicePrefs();
		for (int i = 0; i < button_ids.length; i++)
			buttons[i].setText(button_cfg[i].toString());
	}

	@Override
	protected void onPause() {
		super.onPause();
		storeDicePrefs();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("resultlog", getListView().onSaveInstanceState());
		super.onSaveInstanceState(savedInstanceState);
	}

	void loadDicePrefs() {
		String btn_str = prefs.getString("buttons", null);
		if (btn_str == null)
			return;
		String[] btn_dice = btn_str.split("\\|");
		for (int i = 0; i < btn_dice.length; i++) {
			Log.d(TAG, "load: " + btn_dice[i]);
			button_cfg[i] = new DiceSet(btn_dice[i]);
		}
		dicecache.loadFromString(prefs.getString("cache", null));
	}

	void storeDicePrefs() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < button_cfg.length; i++) {
			sb.append(button_cfg[i]);
			if (i < button_cfg.length - 1)
			sb.append("|");
		}
		prefs.edit().putString("buttons", sb.toString())
			.putString("cache", dicecache.toString())
			.commit();
	}

	public void onClick(View view) {
		Button btn = (Button)view;
		if (btn == button_more) {
			selectDice(new DiceSet(), true, new OnDiceChange() {
				public void onDiceChange(DiceSet ds) {
					roll(ds);
				 }});
		} else {
			String diceVal = btn.getText().toString();
			DiceSet ds = new DiceSet(diceVal);
			roll(ds);
		}
	}
	
	public void roll(DiceSet ds) {
		String roll = ds.roll(generator);
		dicecache.add(ds);

		resultview.setText(roll);

		String rolllog = ds.toString() + ": " + roll;
		Log.d(TAG, "rolled: " + rolllog);
		resultlog.add(rolllog);
	}

	public boolean onLongClick(View view) {
		final Button btn = (Button)view;
		Log.d(TAG, "onLongClicked " + btn);
		String diceVal = btn.getText().toString();
		selectDice(new DiceSet(diceVal), false, new OnDiceChange() {
			public void onDiceChange(DiceSet ds) {
				btn.setText(ds.toString());
				// store button config
				for (int i = 0; i < buttons.length; i++) {
					if (btn == buttons[i])
						button_cfg[i] = ds;
				}
			}});
		return true;
	}

	static final Integer[] SPIN_COUNT = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	static final Integer[] SPIN_SIDES = { 2, 3, 4, 6, 8, 10, 12, 20, 100 };
	static final Integer[] SPIN_MODIFIER = { -10, -9, -8, -7, -6, -5, -4, -3, -2, -1,
						0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	Spinner setupSpinner(View group, int r_id, Integer[] values, int defVal) {
		Spinner sp = (Spinner)group.findViewById(r_id);
		ArrayAdapter adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, values);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(adapter);
		for (int i = 0; i < values.length; i++) {
			if (values[i] == defVal) {
				sp.setSelection(i);
			}
		}
		return sp;
	}

	// create a DiceSet by setting count, sides, modifier
	void configureDice(DiceSet defaults, final OnDiceChange onOk) {
		android.view.LayoutInflater inflater = (android.view.LayoutInflater)getSystemService(
			      LAYOUT_INFLATER_SERVICE);
		View group = inflater.inflate(R.layout.dg_configure, null, false);
		final Spinner sp_c = setupSpinner(group, R.id.spin_count, SPIN_COUNT, defaults.count);
		final Spinner sp_s = setupSpinner(group, R.id.spin_sides, SPIN_SIDES, defaults.sides);
		final Spinner sp_m = setupSpinner(group, R.id.spin_modifier, SPIN_MODIFIER, defaults.modifier);

		new AlertDialog.Builder(this)
			.setTitle(R.string.ds_config)
			.setView(group)
			.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						DiceSet ds = new DiceSet((Integer)sp_c.getSelectedItem(),
							(Integer)sp_s.getSelectedItem(),
							(Integer)sp_m.getSelectedItem());
						onOk.onDiceChange(ds);
					}
				})
			.setNegativeButton(android.R.string.cancel, null)

			.create().show();
	}

	// choose a DiceSet from the last-used list
	void selectDice(final DiceSet defaults, boolean hideBtns, final OnDiceChange onOk) {
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_dropdown_item);
		dicecache.populate(adapter, hideBtns ? java.util.Arrays.asList(button_cfg)
							: new ArrayList<DiceSet>());
		adapter.add(getString(R.string.ds_custom));
		new AlertDialog.Builder(this)
			.setTitle(R.string.ds_choose)
			.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String ds = adapter.getItem(which).toString();
						Log.d(TAG, "item clicked: " + which + " - " + ds);
						if (which == adapter.getCount() - 1)
							configureDice(defaults, onOk);
						else
							onOk.onDiceChange(new DiceSet(ds));
					}
				})
			.setNegativeButton(android.R.string.cancel, null)
			.create().show();
	}
}

abstract class OnDiceChange {
	abstract public void onDiceChange(DiceSet ds);
}
