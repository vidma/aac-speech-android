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
import android.graphics.Color;
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
		// and also the drawableId is not really needed anymore as we have
		// iconPath in database
		return new Pictogram(Integer.toString(categoryId),
				dbHelper.getCategoryTitleShort(categoryId), SPC_ColorCode.MISC,
				Pic2NLG.ActionType.CATEGORY, drawbableId);
	}

	public void createHomePictogramTable(TableLayout tl) {

		/*
		 * TODO: that shall have a proper class -- PRONOUN
		 * http://french.about.com/od/grammar/a/pronouns_3.htm
		 */
		/* pronouns column 1 - singular */
		Pictogram[] clitic_pronouns_1 = {
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_MYSELF),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_YOU),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_HIM),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_HER),

				iconsFactory.get(PictogramFactory.ACT_LETS_DO_SMF), };

		Pictogram[] simple_pronouns_1 = {
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_I),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_YOU),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_HE),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_SHE),
				iconsFactory.get(PictogramFactory.ACT_LETS_DO_SMF), };

		/* pronouns column 2 - plural */
		Pictogram[] clitic_pronouns_2 = {
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_US),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_YOU_PL),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_THEM),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_THEM_F),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_THAT), };

		Pictogram[] simple_pronouns_2 = {
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_WE),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_YOU_PL),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_THEY_M),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_THEY_F),
				iconsFactory.get(PictogramFactory.ACT_PRONOUN_THAT), };

		Pictogram[][] buttons = {
				/* col 1 */
				nlg_state_subject_selected ? clitic_pronouns_1
						: simple_pronouns_1,
				/* col2 */
				nlg_state_subject_selected ? clitic_pronouns_2
						: simple_pronouns_2,

				/* col 3 */
				{ iconsFactory.get(PictogramFactory.ACT_TENSE_PAST),
						iconsFactory.get(PictogramFactory.ACT_NEGATE),
						iconsFactory.get(PictogramFactory.ACT_VERB_HAVE),

						createCategoryButton(14, R.drawable.qualities),
						createCategoryButton(1, R.drawable.adv_of_place), },
				/* col 4 */
				{ createCategoryButton(19, R.drawable.verbes),
						iconsFactory.get(PictogramFactory.ACT_VERB_TO_BE),
						iconsFactory.get(PictogramFactory.ACT_VERB_CAN),
						createCategoryButton(10, R.drawable.food),
						createCategoryButton(6, R.drawable.common_expressions), },

				/* col 5 */
				{ iconsFactory.get(PictogramFactory.ACT_TENSE_FUTURE),
						iconsFactory.get(PictogramFactory.ACT_VERB_WANT),
						iconsFactory.get(PictogramFactory.ACT_QUESTION),
						createCategoryButton(8, R.drawable.objets),
						iconsFactory.get(PictogramFactory.ACT_DOT),

				}, };

		for (int row = 0; row < MAINPAGE_ITEMS_PER_ROW; row++) {
			// create Icons

			TableRow img_row = (TableRow) inflater.inflate(
					R.layout.icontable_tablerow, tl, false);

			for (int col = 0; col < MAINPAGE_ITEMS_PER_COL; col++) {
				final Pictogram currentButton = buttons[col][row];

				View view = createImageButton(img_row, currentButton,
						getButtonInflatingTemplate(currentButton.type));

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
				createCategoryButton(16, R.drawable.emotions),
				createCategoryButton(3, R.drawable.body_health_hygiene),
				createCategoryButton(4, R.drawable.clothing),
				createCategoryButton(12, R.drawable.people),
				createCategoryButton(11, R.drawable.games_sports),
				createCategoryButton(13, R.drawable.places),
				createCategoryButton(2, R.drawable.plants_animas),
				createCategoryButton(18, R.drawable.transport),
				createCategoryButton(9, R.drawable.equipment_furniture),
				createCategoryButton(17, R.drawable.time_weather),
				createCategoryButton(5, R.drawable.color),
				createCategoryButton(7, R.drawable.holiday),
				createCategoryButton(15, R.drawable.empty),

				new Pictogram("0", "non-classif.", SPC_ColorCode.MISC,
						Pic2NLG.ActionType.CATEGORY, R.drawable.not_classified),
				null, null

		};
		ArrayList<Pictogram> buttons = new ArrayList<Pictogram>(
				Arrays.asList(but));

		TableLayout tl1 = (TableLayout) inflater.inflate(R.layout.tablelayout,
				parent, false);
		createIconsTable(buttons.iterator(), 4, MAINPAGE_ITEMS_PER_COL, tl1);
		return tl1;

	}

	private static int getButtonInflatingTemplate(ActionType type) {
		return (type == ActionType.CATEGORY) ? R.layout.icontable_category_imagebutton
				: R.layout.icontable_imagebutton;
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

				if (currentButton == null) {
					/* empty slots */

					View v = inflater.inflate(
							getButtonInflatingTemplate(ActionType.TENSE_PAST),
							img_row, false);
					// img button maybe be contained in some container (e.g.
					// linear layout) depending on template
					ImageButton img_button_view = (ImageButton) v
							.findViewById(R.id.icons_imgButton);
					img_button_view.setBackgroundColor(Color.TRANSPARENT);
					img_row.addView(v);

					// Add the display_text below each icon
					TextView text = (TextView) inflater.inflate(
							R.layout.icontable_imagetext, text_row, false);
					text.setText("  ");
					text_row.addView(text);
				} else {

					View v = createImageButton(img_row, currentButton,
							getButtonInflatingTemplate(currentButton.type));

					ImageButton img = (ImageButton) v
							.findViewById(R.id.icons_imgButton);

					// TODO: img.setTag("aaa"+i);
					img.setOnClickListener(table_item_icon_onclick_listener);
					img_row.addView(v);

					// Add the display_text below each icon
					TextView text = (TextView) inflater.inflate(
							R.layout.icontable_imagetext, text_row, false);
					text.setText(getText(currentButton.toString()));

					text_row.addView(text);
				}
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
