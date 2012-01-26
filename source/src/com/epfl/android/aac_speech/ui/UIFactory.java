package com.epfl.android.aac_speech.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.epfl.android.aac_speech.MainActivity;
import com.epfl.android.aac_speech.R;
import com.epfl.android.aac_speech.data.DBHelper;
import com.epfl.android.aac_speech.data.Pictogram;
import com.epfl.android.aac_speech.data.Pictogram.SpcColor.SPC_ColorCode;
import com.epfl.android.aac_speech.data.PictogramFactory;
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
	protected PictogramFactory iconsFactory;
	protected DBHelper dbHelper;

	/* TODO: do not recreate the factory */
	public boolean nlg_state_subject_selected;

	// UI default options
	private int MAINPAGE_ITEMS_PER_ROW = 5, MAINPAGE_ITEMS_PER_COL = 5;
	private OnClickListener table_item_icon_onclick_listener;

	public UIFactory(LayoutInflater inflater, Context appContext,
			PictogramFactory iconsFactory,
			OnClickListener table_item_icon_onclick_listener, DBHelper dbHelper) {
		this.inflater = inflater;
		this.appContext = appContext;
		this.iconsFactory = iconsFactory;
		this.table_item_icon_onclick_listener = table_item_icon_onclick_listener;
		this.dbHelper = dbHelper;
	}

	public View createImageButton(ViewGroup parentElm,
			final Pictogram currentButton, int inflaterTemplate) {
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

	/**
	 * creates an Pictogram button for a Category
	 * 
	 * @param categoryId
	 * @param drawbableId
	 * @return
	 */
	private Pictogram createCategoryButton(int categoryId, int drawbableId) {

		// TODO: refactor the nasty old constructor: e.g. SPC color code is not
		// needed here..
		return new Pictogram(Integer.toString(categoryId),
				dbHelper.getCategoryTitleShort(categoryId), SPC_ColorCode.MISC,
				Pic2NLG.ActionType.CATEGORY, drawbableId);
	}

	public void createHomePictogramTable(TableLayout tl) {

		/*
		 * TODO: that shall have a proper class -- PRONOUN
		 * http://french.about.com/od/grammar/a/pronouns_3.htm
		 */
		/* pronouns column 1 */
		Pictogram[] clitic_pronouns_1 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_MYSELF),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_YOU),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_HIM),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_HER),

				iconsFactory.get(iconsFactory.ACT_LETS_DO_SMF), };

		Pictogram[] simple_pronouns_1 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_I),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_YOU),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_HE),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_SHE),
				iconsFactory.get(iconsFactory.ACT_LETS_DO_SMF), };

		/* pronouns column 2 */
		Pictogram[] clitic_pronouns_2 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_US),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_YOU_PL),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_THEM),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_THEM_F),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_THAT), };

		Pictogram[] simple_pronouns_2 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_WE),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_YOU_PL),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_THEY_M),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_THEY_F),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_THAT), };

		Pictogram[][] buttons = {
				/* col 1 */
				nlg_state_subject_selected ? clitic_pronouns_1
						: simple_pronouns_1,
				/* col2 */
				nlg_state_subject_selected ? clitic_pronouns_2
						: simple_pronouns_2,

				/* col 3 */
				{

						iconsFactory.get(iconsFactory.ACT_TENSE_PAST),
						iconsFactory.get(iconsFactory.ACT_NEGATE),
						iconsFactory.get(iconsFactory.ACT_VERB_HAVE),

						new Pictogram("14", dbHelper.getCategoryTitleShort(14),
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY,
								R.drawable.qualities),
						new Pictogram("1", dbHelper.getCategoryTitleShort(1),
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY,
								R.drawable.adv_of_place), },
				/* col 4 */
				{
						new Pictogram("19", dbHelper.getCategoryTitleShort(19),
								SPC_ColorCode.ACTION,
								Pic2NLG.ActionType.CATEGORY, R.drawable.verbes),
						iconsFactory.get(iconsFactory.ACT_VERB_TO_BE),
						iconsFactory.get(iconsFactory.ACT_VERB_CAN),

						new Pictogram("10", dbHelper.getCategoryTitleShort(10),
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY, R.drawable.food),

						new Pictogram("6", dbHelper.getCategoryTitleShort(6),
								SPC_ColorCode.SOCIAL,
								Pic2NLG.ActionType.CATEGORY,
								R.drawable.common_expressions), },

				/* col 5 */
				{

						iconsFactory.get(iconsFactory.ACT_TENSE_FUTURE),
						iconsFactory.get(iconsFactory.ACT_VERB_WANT),
						iconsFactory.get(iconsFactory.ACT_QUESTION),

						new Pictogram("8", dbHelper.getCategoryTitleShort(8),
								SPC_ColorCode.COMMON_NAME,
								Pic2NLG.ActionType.CATEGORY, R.drawable.objets),

						iconsFactory.get(iconsFactory.ACT_DOT),

				}, };

		for (int row = 0; row < MAINPAGE_ITEMS_PER_ROW; row++) {
			// create Icons

			TableRow img_row = (TableRow) inflater.inflate(
					R.layout.icontable_tablerow, tl, false);

			for (int col = 0; col < MAINPAGE_ITEMS_PER_COL; col++) {
				final Pictogram currentButton = buttons[col][row];

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
				Pictogram currentButton = buttons[col][row];

				TextView text = (TextView) inflater.inflate(
						R.layout.icontable_imagetext, text_row, false);

				text.setText(getText(currentButton.toString()));

				text_row.addView(text);
			}
			tl.addView(text_row);

		}
	}

	public void createHomePictogramTable_old(TableLayout tl) {

		/*
		 * TODO: that shall have a proper class -- PRONOUN
		 * http://french.about.com/od/grammar/a/pronouns_3.htm
		 */
		/* pronouns column 1 */
		Pictogram[] clitic_pronouns_1 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_MYSELF),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_YOU),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_HIM),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_HER),

				iconsFactory.get(iconsFactory.ACT_PRONOUN_THAT), };

		Pictogram[] simple_pronouns_1 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_I),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_YOU),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_HE),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_SHE),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_THAT), };

		/* pronouns column 2 */

		Pictogram[] clitic_pronouns_2 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_US),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_YOU_PL),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_THEM),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_OBJ_THEM_F),

				iconsFactory.get(iconsFactory.ACT_TENSE_PAST), };

		Pictogram[] simple_pronouns_2 = {
				iconsFactory.get(iconsFactory.ACT_PRONOUN_WE),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_YOU_PL),

				iconsFactory.get(iconsFactory.ACT_PRONOUN_THEY_M),
				iconsFactory.get(iconsFactory.ACT_PRONOUN_THEY_F),

				iconsFactory.get(iconsFactory.ACT_TENSE_PAST), };

		Pictogram[][] buttons = {
				nlg_state_subject_selected ? clitic_pronouns_1
						: simple_pronouns_1,
				nlg_state_subject_selected ? clitic_pronouns_2
						: simple_pronouns_2,

				{

				iconsFactory.get(iconsFactory.ACT_LETS_DO_SMF),
						iconsFactory.get(iconsFactory.ACT_NEGATE),
						iconsFactory.get(iconsFactory.ACT_VERB_WANT),
						iconsFactory.get(iconsFactory.ACT_VERB_CAN),
						iconsFactory.get(iconsFactory.ACT_TENSE_FUTURE) },
				{
						new Pictogram("19", "verbes", SPC_ColorCode.ACTION,
								Pic2NLG.ActionType.CATEGORY, R.drawable.verbes),
						new Pictogram("8", "objets", SPC_ColorCode.COMMON_NAME,
								Pic2NLG.ActionType.CATEGORY, R.drawable.objets),
						new Pictogram("10", "aliments",
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY, R.drawable.food),

						iconsFactory.get(iconsFactory.ACT_VERB_TO_BE),
						iconsFactory.get(iconsFactory.ACT_DOT), },

				{
						new Pictogram("1", "adv.place",
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY,
								R.drawable.adv_of_place),
						new Pictogram("14", "qualités",
								SPC_ColorCode.DESCRIPTIVE,
								Pic2NLG.ActionType.CATEGORY,
								R.drawable.qualities),

						new Pictogram("6", "expr.comun", SPC_ColorCode.SOCIAL,
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
				final Pictogram currentButton = buttons[col][row];

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
				Pictogram currentButton = buttons[col][row];

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
		Pictogram but[] = {
				new Pictogram("16", "émotion", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.emotions),

				// TODO:
				new Pictogram("3", "body", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY,
						R.drawable.body_health_hygiene),

				new Pictogram("4", "vetements", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.clothing),

				new Pictogram("12", "peuple", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.people),

				new Pictogram("11", "jeux-sports", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.games_sports),

				new Pictogram("13", "lieux", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.places),

				new Pictogram("2", "animaux-plantes",
						SPC_ColorCode.DESCRIPTIVE, Pic2NLG.ActionType.CATEGORY,
						R.drawable.plants_animas),

				new Pictogram("18", "transport", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.transport),

				new Pictogram("9", "équipement", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY,
						R.drawable.equipment_furniture),

				new Pictogram("17", "(le)temps", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.time_weather),

				new Pictogram("5", "forme couleur etc",
						SPC_ColorCode.DESCRIPTIVE, Pic2NLG.ActionType.CATEGORY,
						R.drawable.color),

				new Pictogram("7", "vacances etc", SPC_ColorCode.DESCRIPTIVE,
						Pic2NLG.ActionType.CATEGORY, R.drawable.holiday),

				new Pictogram("15", "particles", SPC_ColorCode.MISC,
						Pic2NLG.ActionType.CATEGORY, R.drawable.empty),

				new Pictogram("0", "non-classif.", SPC_ColorCode.MISC,
						Pic2NLG.ActionType.CATEGORY, R.drawable.question),

		};
		ArrayList<Pictogram> buttons = new ArrayList<Pictogram>(
				Arrays.asList(but));

		TableLayout tl1 = (TableLayout) inflater.inflate(R.layout.tablelayout,
				parent, false);
		createIconsTable(buttons.iterator(), MAINPAGE_ITEMS_PER_COL,
				MAINPAGE_ITEMS_PER_COL, tl1);
		return tl1;

	}

	private void createIconsTable(Iterator<Pictogram> buttons_it, int cols,
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

				final Pictogram currentButton = buttons_it.next();

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
