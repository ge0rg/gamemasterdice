package de.duenndns.gmdice;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Random;

public class GameMasterDice extends ListActivity
		implements OnClickListener, OnLongClickListener
{
	private static String TAG = "GameMasterDice";

	// map button IDs to dice
	int button_ids[] = { R.id.die0, R.id.die1, R.id.die2, R.id.die3 };
	Button buttons[];
	TextView resultview;
	ArrayAdapter<String> resultlog;

	Random generator = new Random();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_gmdice);

		buttons = new Button[button_ids.length];
		for (int i = 0; i < button_ids.length; i++) {
			buttons[i] = (Button)findViewById(button_ids[i]);
			buttons[i].setOnClickListener(this);
			buttons[i].setOnLongClickListener(this);
		}
		resultview = (TextView)findViewById(R.id.rollresult);
		resultlog = new ArrayAdapter<String>(this, R.layout.view_log);
		setListAdapter(resultlog);
	}

	public void onClick(View view) {
		Button btn = (Button)view;
		String diceVal = btn.getText().toString();
		DiceSet ds = new DiceSet(diceVal);
		String roll = ds.roll(generator);

		Log.d(TAG, "clicked on " + diceVal + ": " + ds);
		Log.d(TAG, "rolled: " + roll);
		resultview.setText(roll);

		String rolllog = ds.toString() + ": " + roll;
		resultlog.add(rolllog);
	}

	public boolean onLongClick(View view) {
		final Button btn = (Button)view;
		Log.d(TAG, "onLongClicked " + btn);
		String diceVal = btn.getText().toString();
		configureDice(new DiceSet(diceVal), new OnDiceChange() {
			public void onDiceChange(DiceSet ds) {
				btn.setText(ds.toString());
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

	void configureDice(DiceSet defaults, final OnDiceChange onOk) {
		android.view.LayoutInflater inflater = (android.view.LayoutInflater)getSystemService(
			      LAYOUT_INFLATER_SERVICE);
		View group = inflater.inflate(R.layout.dg_configure, null, false);
		final Spinner sp_c = setupSpinner(group, R.id.spin_count, SPIN_COUNT, defaults.count);
		final Spinner sp_s = setupSpinner(group, R.id.spin_sides, SPIN_SIDES, defaults.sides);
		final Spinner sp_m = setupSpinner(group, R.id.spin_modifier, SPIN_MODIFIER, defaults.modifier);

		new AlertDialog.Builder(this)
			.setTitle("Configure Die")
			.setView(group)
			.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "ok clicked");
						DiceSet ds = new DiceSet((Integer)sp_c.getSelectedItem(),
							(Integer)sp_s.getSelectedItem(),
							(Integer)sp_m.getSelectedItem());
						onOk.onDiceChange(ds);
					}
				})
			.setNegativeButton(android.R.string.cancel, null)

			.create().show();
	}

}

abstract class OnDiceChange {
	abstract public void onDiceChange(DiceSet ds);
}
