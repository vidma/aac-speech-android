package com.epfl.android.aac_speech.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.epfl.android.aac_speech.MainActivity;
import com.epfl.android.aac_speech.R;
import com.epfl.android.aac_speech.data.PicWordAction;
import com.epfl.android.aac_speech.data.PicWordAction.SpcColor.SPC_ColorCode;
import com.epfl.android.aac_speech.data.PicWordActionFactory;
import com.epfl.android.aac_speech.lib.ImageUtils;
import com.epfl.android.aac_speech.nlg.Pic2NLG;
import com.epfl.android.aac_speech.nlg.Pic2NLG.ActionType;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class UIFactory {

	protected LayoutInflater inflater;
	protected Context appContext;
	protected PicWordActionFactory iconsFactory;

	/* TODO: do not recreate the factory */
	public boolean nlg_state_subject_selected;

	// UI default options
	private int MAINPAGE_ITEMS_PER_ROW = 5, MAINPAGE_ITEMS_PER_COL = 5;
	private OnClickListener table_item_icon_onclick_listener;

	public UIFactory(LayoutInflater inflater, Context appContext,
			PicWordActionFactory iconsFactory,
			OnClickListener table_item_icon_onclick_listener) {
		this.inflater = inflater;
		this.appContext = appContext;
		this.iconsFactory = iconsFactory;
		this.table_item_icon_onclick_listener = table_item_icon_onclick_listener;
	}

	public View createImageButton(ViewGroup parentElm,
			final PicWordAction currentButton, int inflaterTemplate) {
		View view = inflater.inflate(inflaterTemplate, parentElm, false);

		ImageButton img = (ImageButton) view.findViewById(R.id.icons_imgButton);

		/* load the image either from resource or FileURI */
		if (currentButton.imageResourceId != 0) {
			// Log.d("createButtons","adding image for " +
			// currentButton.toString());
			// TODO: this is very resource consuming now
			img.setImageResource(currentButton.imageResourceId);
		} else if (currentButton.imageFileURI != "") {
			Bitmap bMap = ImageUtils.getBitmapFromURI(
					currentButton.imageFileURI, appContext);
			img.setImageBitmap(bMap);
		}

		/* only if SPC colors are not disabled */
		if (currentButton.type != ActionType.CATEGORY
				&& !MainActivity.getPrefHideSPCColor())
			img.setBackgroundColor(currentButton.getBgColor());

		img.setTag(currentButton);

		return view;
	}

	public void createHomePictogramTable(TableLayout tl) {

		/*
		 * TODO: that shall have a proper class -- PRONOUN
		 * http://french.about.com/od/grammar/a/pronouns_3.htm
		 */
		PicWordAction[] stuff1 = {
				new PicWordAction("me", ActionType.CLITIC_PRONOUN,
						R.drawable.je),
				new PicWordAction("te", ActionType.CLITIC_PRONOUN,
						R.drawable.tu),
				new PicWordAction("lui", ActionType.CLITIC_PRONOUN,
						R.drawable.il),
				new PicWordAction("lui", ActionType.CLITIC_PRONOUN,
						R.drawable.elle),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_THAT), };

		PicWordAction[] stuff2 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_I),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_YOU),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_HE),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_SHE),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_THAT), };

		PicWordAction[][] buttons = {
				nlg_state_subject_selected ? stuff1 : stuff2,
				{
					/* TODO: then here I shall change the tyoe to CLITIC_PRONOUN too */
						iconsFactory.get(iconsFactory.ACT_PRONOUN_WE),
						iconsFactory.get(iconsFactory.ACT_PRONOUN_YOU_PL),
						(nlg_state_subject_selected ? (new PicWordAction("eux",
								ActionType.CLITIC_PRONOUN, R.drawable.ils))
								: iconsFactory
										.get(iconsFactory.ACT_PRONOUN_THEY_M)),

						(nlg_state_subject_selected ? (new PicWordAction("eux",
								ActionType.CLITIC_PRONOUN, R.drawable.ils))
								: iconsFactory
										.get(iconsFactory.ACT_PRONOUN_THEY_F)),
						/*
						 * present is by default anyway
						 * iconsFactory.get(iconsFactory.ACT_TENSE_PRESENT)
						 */
						iconsFactory.get(iconsFactory.ACT_TENSE_PAST), },

				{

				iconsFactory.get(iconsFactory.ACT_LETS_DO_SMF),
						iconsFactory.get(iconsFactory.ACT_NEGATE),
						iconsFactory.get(iconsFactory.ACT_VERB_WANT),
						iconsFactory.get(iconsFactory.ACT_VERB_CAN),
						iconsFactory.get(iconsFactory.ACT_TENSE_FUTURE) },
				{
						new PicWordAction("19", "verbes", SPC_ColorCode.ACTION,
								Pic2NLG.ActionType.CATEGORY, R.drawable.verbes),
						new PicWordAction("8", "objets",
								SPC_ColorCode.COMMON_NAME,
								Pic2NLG.ActionType.CATEGORY, R.drawable.objets),
						new PicWordAction("10", "aliments",
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY, R.drawable.food),

						iconsFactory.get(iconsFactory.ACT_VERB_TO_BE),
						iconsFactory.get(iconsFactory.ACT_DOT), },

				{
						new PicWordAction("1", "adv.place",
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY,
								R.drawable.adv_of_place),
						new PicWordAction("14", "qualités",
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY,
								R.drawable.qualities),

						new PicWordAction("6", "expr.comun",
								SPC_ColorCode.SOCIAL,
								Pic2NLG.ActionType.CATEGORY,
								R.drawable.common_expressions),

						iconsFactory.get(iconsFactory.ACT_VERB_HAVE),
						iconsFactory.get(iconsFactory.ACT_QUESTION),

				}, };

		for (int row = 0; row < MAINPAGE_ITEMS_PER_ROW; row++) {
			// create Icons

			TableRow img_row = (TableRow) inflater.inflate(
					R.layout.icontable_tablerow, tl, false);

			for (int col = 0; col < MAINPAGE_ITEMS_PER_COL; col++) {
				final PicWordAction currentButton = buttons[col][row];

				View view = createImageButton(
						img_row,
						currentButton,
						(currentButton.type == ActionType.CATEGORY) ? R.layout.icontable_category_imagebutton
								: R.layout.icontable_imagebutton);

				ImageButton img = (ImageButton) view
						.findViewById(R.id.icons_imgButton);

				// TODO: instead of final use, tag!
				img.setOnClickListener(this.table_item_icon_onclick_listener);
				/*
				 * img.setOnLongClickListener(new OnLongClickListener() {
				 * 
				 * @Override public boolean onLongClick(View v) { // TODO
				 * Auto-generated method stub Vibrator vibrator = (Vibrator)
				 * getSystemService(Context.VIBRATOR_SERVICE);
				 * vibrator.vibrate(1000); return true; } });
				 */
				img_row.addView(view);
			}
			tl.addView(img_row);

			// Add the display_text below each icon

			TableRow text_row = (TableRow) inflater.inflate(
					R.layout.icontable_tablerow, tl, false);
			for (int col = 0; col < MAINPAGE_ITEMS_PER_COL; col++) {
				PicWordAction currentButton = buttons[col][row];

				TextView text = (TextView) inflater.inflate(
						R.layout.icontable_imagetext, text_row, false);

				text.setText(getText(currentButton.toString()));

				text_row.addView(text);
			}
			tl.addView(text_row);

		}
	}

	/**
	 * Creates a table layout to be shown on alternative main page (accessible
	 * after a flip)
	 * 
	 * @param parent
	 * @return
	 */
	public TableLayout createImageButtonsCategoriesRight(ViewGroup parent) {
		PicWordAction but[] = {
				new PicWordAction("16", "émotion", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.emotions),

				// TODO:
				new PicWordAction("3", "body", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY,
						R.drawable.body_health_hygiene),

				new PicWordAction("4", "vetements", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.clothing),

				new PicWordAction("12", "peuple", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.people),

				new PicWordAction("11", "jeux-sports",
						SPC_ColorCode.DESCRIPTIVE, Pic2NLG.ActionType.CATEGORY,
						R.drawable.games_sports),

				new PicWordAction("13", "lieux", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.places),

				new PicWordAction("2", "animaux-plantes",
						SPC_ColorCode.DESCRIPTIVE, Pic2NLG.ActionType.CATEGORY,
						R.drawable.plants_animas),

				new PicWordAction("18", "transport", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.transport),

				new PicWordAction("9", "équipement", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY,
						R.drawable.equipment_furniture),

				new PicWordAction("17", "(le)temps", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.time_weather),

				new PicWordAction("5", "forme couleur etc",
						SPC_ColorCode.DESCRIPTIVE, Pic2NLG.ActionType.CATEGORY,
						R.drawable.color),

				new PicWordAction("7", "vacances etc",
						SPC_ColorCode.DESCRIPTIVE, Pic2NLG.ActionType.CATEGORY,
						R.drawable.holiday),

				new PicWordAction("15", "particles", SPC_ColorCode.MISC,
						Pic2NLG.ActionType.CATEGORY, R.drawable.empty),

				new PicWordAction("0", "non-classif.", SPC_ColorCode.MISC,
						Pic2NLG.ActionType.CATEGORY, R.drawable.question),

		};
		ArrayList<PicWordAction> buttons = new ArrayList<PicWordAction>(
				Arrays.asList(but));

		TableLayout tl1 = (TableLayout) inflater.inflate(R.layout.tablelayout,
				parent, false);
		createIconsTable(buttons.iterator(), MAINPAGE_ITEMS_PER_COL,
				MAINPAGE_ITEMS_PER_COL, tl1);
		return tl1;

	}

	private void createIconsTable(Iterator<PicWordAction> buttons_it, int cols,
			int rows, TableLayout tl) {

		tl.removeAllViews();
		while (buttons_it.hasNext()) {
			// create Icons
			if (!buttons_it.hasNext())
				break;

			TableRow img_row = (TableRow) inflater.inflate(
					R.layout.icontable_tablerow, tl, false);
			TableRow text_row = (TableRow) inflater.inflate(
					R.layout.icontable_tablerow, tl, false);

			for (int col = 0; col < cols; col++) {
				if (!buttons_it.hasNext())
					break;

				final PicWordAction currentButton = buttons_it.next();

				// View view = createImageButton(img_row, currentButton,
				// R.layout.icons_imagebutton);
				View view = createImageButton(
						img_row,
						currentButton,
						(currentButton.type == ActionType.CATEGORY) ? R.layout.icontable_category_imagebutton
								: R.layout.icontable_imagebutton);

				ImageButton img = (ImageButton) view
						.findViewById(R.id.icons_imgButton);

				// TODO: img.setTag("aaa"+i);
				img.setOnClickListener(table_item_icon_onclick_listener);

				/*
				 * img.setOnLongClickListener(new OnLongClickListener() {
				 * 
				 * @Override public boolean onLongClick(View v) { // TODO
				 * Auto-generated method stub Vibrator vibrator = (Vibrator)
				 * getSystemService(Context.VIBRATOR_SERVICE);
				 * vibrator.vibrate(1000); return true; } });
				 */
				img_row.addView(view);

				// Add the display_text below each icon
				TextView text = (TextView) inflater.inflate(
						R.layout.icontable_imagetext, text_row, false);

				text.setText(getText(currentButton.toString()));

				text_row.addView(text);
			}
			tl.addView(img_row);
			tl.addView(text_row);
		}
	}

	private CharSequence getText(String string) {
		// TODO Auto-generated method stub
		return MainActivity.getText(string);
	}
}
