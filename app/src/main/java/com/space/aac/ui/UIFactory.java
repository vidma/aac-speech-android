package com.space.aac.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import android.widget.TextView;

import com.space.aac.MainActivity;
import com.space.aac.R;
import com.space.aac.data.DBHelper;
import com.space.aac.data.Pictogram;
import com.space.aac.data.PictogramFactory;
import com.space.aac.lib.ImageUtils;
import com.space.aac.nlg.Pic2NLG;
import com.space.aac.nlg.Pic2NLG.ActionType;
import com.space.aac.data.Pictogram.SpcColor.SPC_ColorCode;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by space on 29/7/16.
 */
public class UIFactory {

    protected LayoutInflater inflater;
    protected Context appContext; // TODO: storing the context, but it's appContext so OK?
    protected PictogramFactory iconsFactory;
    protected DBHelper dbHelper;

    /* TODO: do not recreate the factory */
    public boolean nlg_state_subject_selected;

    // UI default options
    private int MAINPAGE_ITEMS_PER_ROW = 5, MAINPAGE_ITEMS_PER_COL = 5;
    private OnClickListener table_item_icon_onclick_listener;

    public static final String TAG_IMGS_CATEGORIES = "ImageButtonsCategoriesRight";
    public static final String TAG_IMGS_MAIN = "ImageButtonsMain";


    // button cache
    ArrayList<Pictogram> category_buttons;
    Pictogram[] clitic_pronouns_1,  simple_pronouns_1, clitic_pronouns_2, simple_pronouns_2;
    Pictogram[][] main_buttons;

    Map<Integer, Integer> categoryButtonResIdCache = new HashMap<Integer, Integer>();


    public UIFactory(LayoutInflater inflater, Context appContext, PictogramFactory iconsFactory,
                     OnClickListener table_item_icon_onclick_listener, DBHelper dbHelper) {
        this.inflater = inflater;
        this.appContext = appContext;
        this.iconsFactory = iconsFactory;
        this.table_item_icon_onclick_listener = table_item_icon_onclick_listener;
        this.dbHelper = dbHelper;

        init_button_cache();
    }

    private void init_button_cache(){
		/*
		 * TODO: that shall have a proper class -- PRONOUN
		 * http://french.about.com/od/grammar/a/pronouns_3.htm
		 */
		/* pronouns column 1 - singular */

		/* main buttons - home */
        clitic_pronouns_1 = new Pictogram[] { iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_MYSELF),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_YOU),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_HIM),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_HER),
                iconsFactory.get(PictogramFactory.ACT_LETS_DO_SMF), };

        simple_pronouns_1 = new Pictogram[] { iconsFactory.get(PictogramFactory.ACT_PRONOUN_I),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_YOU), iconsFactory.get(PictogramFactory.ACT_PRONOUN_HE),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_SHE), iconsFactory.get(PictogramFactory.ACT_LETS_DO_SMF), };

		/* pronouns column 2 - plural */
        clitic_pronouns_2 = new Pictogram[] { iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_US),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_YOU_PL),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_THEM),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_OBJ_THEM_F),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_THAT), };

        simple_pronouns_2 = new Pictogram[] { iconsFactory.get(PictogramFactory.ACT_PRONOUN_WE),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_YOU_PL),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_THEY_M),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_THEY_F),
                iconsFactory.get(PictogramFactory.ACT_PRONOUN_THAT), };

        main_buttons = new Pictogram[][] {
                //TODO: currently here it's only the text that changes, so there's no need to recreate the buttons, we could change the tag...
				/* col 1 */
                null,
				/* col2 */
                null,
				/* col 3 */
                { iconsFactory.get(PictogramFactory.ACT_TENSE_PAST), // TODO: new Pictogram(" ", ActionType.EMPTY),
                        iconsFactory.get(PictogramFactory.ACT_NEGATE),
                        iconsFactory.get(PictogramFactory.ACT_VERB_HAVE),
                        createCategoryButton(14, R.drawable.qualities),
                        createCategoryButton(1, R.drawable.adv_of_place),
                },
				/* col 4 */
                { createCategoryButton(19, R.drawable.verbes),
                        iconsFactory.get(PictogramFactory.ACT_VERB_TO_BE),
                        iconsFactory.get(PictogramFactory.ACT_VERB_CAN),
                        createCategoryButton(10, R.drawable.food),
                        createCategoryButton(6, R.drawable.common_expressions),
                },
				/* col 5 */
                { iconsFactory.get(PictogramFactory.ACT_TENSE_FUTURE),  // TODO: new Pictogram(" ", ActionType.EMPTY),
                        iconsFactory.get(PictogramFactory.ACT_VERB_WANT),
                        iconsFactory.get(PictogramFactory.ACT_QUESTION), createCategoryButton(8, R.drawable.objets),
                        iconsFactory.get(PictogramFactory.ACT_DOT),
                }, };

		/* category buttons (2nd subpage) */
        category_buttons = new ArrayList<Pictogram>(Arrays.asList(new Pictogram[]{
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
                new Pictogram("0", "non-classif.", SPC_ColorCode.MISC, Pic2NLG.ActionType.CATEGORY,
                        R.drawable.not_classified), new Pictogram(" ", ActionType.EMPTY),
                new Pictogram(" ", ActionType.EMPTY),
        }));
    }

    private Pictogram[][] get_main_buttons(){
        main_buttons[0] = 	nlg_state_subject_selected ? clitic_pronouns_1 : simple_pronouns_1;
        main_buttons[1] = 	nlg_state_subject_selected ? clitic_pronouns_2 : simple_pronouns_2;
        return main_buttons;
    }

    public View createImageButton(ViewGroup parentElm, final Pictogram currentButton) {
        int inflaterTemplate = getButtonInflatingTemplate(currentButton.type);
        View view = inflater.inflate(inflaterTemplate, parentElm, false);
        updateImageButton(parentElm, currentButton, view, inflaterTemplate);
        return view;
    }

    public View createImageButton(ViewGroup parentElm, final Pictogram currentButton, int inflaterTemplate) {
        View view = inflater.inflate(inflaterTemplate, parentElm, false);
        updateImageButton(parentElm, currentButton, view, inflaterTemplate);
        return view;
    }

    public View updateImageButton(ViewGroup parentElm, final Pictogram currentButton, View view) {
        int inflaterTemplate = getButtonInflatingTemplate(currentButton.type);
        updateImageButton(parentElm, currentButton, view, inflaterTemplate);
        return view;
    }

    public View updateImageButton(ViewGroup parentElm, final Pictogram currentButton, View view, int inflaterTemplate) {
        //Log.d("UIFact", view.toString());
        ImageButton img = (ImageButton) view.findViewById(R.id.icons_imgButton);
        // set the image to required resource or FileURI
        if (currentButton.imageResourceId != 0) {
            // Log.d("createButtons","adding image for " + currentButton.toString());
            // TODO: this might be very resource consuming now - figure out when no change needed?!!
            img.setImageResource(currentButton.imageResourceId);
        } else if (currentButton.imageFileURI != "") {
            Bitmap bMap = ImageUtils.getBitmapFromURI(currentButton.imageFileURI, appContext);
            img.setImageBitmap(bMap);
        }
        // only if SPC colors are not disabled
        if (currentButton.type != ActionType.CATEGORY && !MainActivity.getPrefHideSPCColor())
            img.setBackgroundColor(currentButton.getBgColor());

        img.setTag(R.id.TAG_PICTOGRAM, currentButton); // TODO: shall I set tag or img or view?
        return view;
    }


    public int getCategoryButtonDrawableId(int categoryId) {
        return categoryButtonResIdCache.get(categoryId);
    }

    /**
     * creates an Pictogram button for a Category
     *
     * @param categoryId
     * @param drawbableId
     * @return
     */
    private Pictogram createCategoryButton(int categoryId, int drawbableId) {
        // TODO: this might be more clean...
        categoryButtonResIdCache.put(categoryId, drawbableId);

        // TODO: refactor the nasty old constructor: e.g. SPC color code is not
        // and also the drawableId is not really needed anymore as we have
        // iconPath in database
        return new Pictogram(Integer.toString(categoryId), dbHelper.getCategoryTitleShort(categoryId),
                SPC_ColorCode.MISC, Pic2NLG.ActionType.CATEGORY, drawbableId);
    }

    public TableLayout createHomePictogramTable(ViewGroup parent, boolean update) {
        Pictogram[][] buttons = get_main_buttons();

        ArrayList<Pictogram> buttons_list = new ArrayList<Pictogram>();
        for (int row = 0; row < MAINPAGE_ITEMS_PER_ROW; row++) {
            // create Icons
            for (int col = 0; col < MAINPAGE_ITEMS_PER_COL; col++) {
                buttons_list.add(buttons[col][row]);
            }
        }

        TableLayout tl;
        if (!update){
            tl = (TableLayout) inflater.inflate(R.layout.tablelayout, parent, false);
            tl.setTag(TAG_IMGS_MAIN);
        } else {
            tl = (TableLayout) parent.findViewWithTag(TAG_IMGS_MAIN);
        }

        createIconsTable(buttons_list.iterator(), MAINPAGE_ITEMS_PER_ROW, MAINPAGE_ITEMS_PER_COL, tl, update);
        return tl;

    }

    /**
     * Creates a table layout to be shown on alternative main page (accessible
     * after a flip)
     *
     * @param parent
     * @return
     */
    public TableLayout createImageButtonsCategoriesRight(ViewGroup parent, boolean update) {
        TableLayout tl;
        if (!update){
            tl = (TableLayout) inflater.inflate(R.layout.tablelayout, parent, false);
            tl.setTag(TAG_IMGS_CATEGORIES);
        } else {
            tl = (TableLayout) parent.findViewWithTag(TAG_IMGS_CATEGORIES);
        }
        createIconsTable(category_buttons.iterator(), 4, MAINPAGE_ITEMS_PER_COL, tl, update);
        return tl;

    }

    private static int getButtonInflatingTemplate(ActionType type) {
        return (type == ActionType.CATEGORY) ? R.layout.icontable_category_imagebutton : R.layout.icontable_imagebutton;
    }

    private void createIconsTable(Iterator<Pictogram> buttons_it, int cols, int rows, TableLayout tl,  boolean update) {
        //final boolean update = false;
        if (!update)
            tl.removeAllViews();
        int row = 0;
        TableRow img_row, text_row;
        TextView text_v;
        View btn_v;

        while (buttons_it.hasNext()) {
            // create Icons
            row += 1;
            String rowtag_img = "img_row_" + row, rowtag_txt = "text_row_" + row;
            if (!update){
                // create the elements
                img_row = (TableRow) inflater.inflate(R.layout.icontable_tablerow, tl, false);
                text_row = (TableRow) inflater.inflate(R.layout.icontable_tablerow, tl, false);
                img_row.setTag(rowtag_img);
                text_row.setTag(rowtag_txt);
            } else {
                // reuse existing
                img_row = (TableRow) tl.findViewWithTag(rowtag_img);
                text_row = (TableRow) tl.findViewWithTag(rowtag_txt);
            }

            for (int col = 0; col < cols; col++) {
                final Pictogram currentButton = buttons_it.next();
                String cell_tag = "iconcell_" + row + "_" + col;

                if (!update){
                    text_v = (TextView) inflater.inflate(R.layout.icontable_imagetext, text_row, false);
                    btn_v = createImageButton(img_row, currentButton);
                } else {
                    text_v = (TextView) text_row.findViewWithTag(cell_tag);
                    btn_v = img_row.findViewWithTag(cell_tag);
                    // TODO: why is it sometimes null?
                    //Log.d("UIFACT", "tag:" + cell_tag +  " txt_v:"+ text_v + " btn_v:"+ btn_v + "parent(img_row):" + img_row + "  tl: "+ tl);
                    if (btn_v == null)	continue;
                    updateImageButton(img_row, currentButton, btn_v);
                }

                // a button can be contained within template
                ImageButton img = (ImageButton) btn_v.findViewById(R.id.icons_imgButton);
                // TODO: img.setTag("aaa"+i);
                text_v.setText(getText(currentButton.toString()));

                if (!update){
                    btn_v.setTag(cell_tag);
                    text_v.setTag(cell_tag);
                    btn_v.setTag(R.id.TAG_CELL_ID, cell_tag);
                    btn_v.setTag(R.id.TAG_PICTOGRAM, currentButton);

                    img.setOnClickListener(table_item_icon_onclick_listener);
                    img_row.addView(btn_v);
                    text_row.addView(text_v);
                }

                if (!buttons_it.hasNext())
                    break;
            }
            if (!update){
                tl.addView(img_row);
                tl.addView(text_row);
            }
        }
    }



    private CharSequence getText(String string) {
        // TODO Auto-generated method stub
        return MainActivity.getText(string);
    }
}
