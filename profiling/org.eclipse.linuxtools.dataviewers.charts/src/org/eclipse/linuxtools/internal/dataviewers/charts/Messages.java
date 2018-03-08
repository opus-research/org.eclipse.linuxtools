package org.eclipse.linuxtools.internal.dataviewers.charts;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.dataviewers.charts.messages"; //$NON-NLS-1$

    /** The section name of the viewer's dialog settings where the chart dialog save its state */
    public static final String TAG_SECTION_CHARTS_STATE = "charts_section"; //$NON-NLS-1$
    /**
     * The key used by the column buttons to save their state. For example the button i will use the key
     * <code>TAG_COLUMN_BUTTON_+i</code>
     */
    public static final String TAG_COLUMN_BUTTON_ = "COLUMN_BUTTON_"; //$NON-NLS-1$
    /** The key used by the bar graph button to save its state */
    public static final String TAG_BAR_GRAPH_BUTTON = "BAR_GRAPH_BUTTON"; //$NON-NLS-1$
    /** The key used by the vertical bars button to save its state */
    public static final String TAG_VERTICAL_BARS_BUTTON = "VERTICAL_BARS_BUTTON"; //$NON-NLS-1$

    /** The default value of the column buttons */
    public static final boolean DEFAULT_COLUMN_BUTTON = true;
    /** The default value of the bar graph button */
    public static final boolean DEFAULT_BAR_GRAPH_BUTTON = true;
    /** The default value of the vertical bars button */
    public static final boolean DEFAULT_VERTICAL_BARS_BUTTON = false;

    /** The section name of the "save chart as image" action dialog settings */
    public static final String TAG_SECTION_CHARTS_SAVEACTION_STATE = "charts_saveasimg_section"; //$NON-NLS-1$
    /** The key used by the file dialog to save its file name */
    public static final String TAG_IMG_FILE_NAME = "IMG_FILE_NAME"; //$NON-NLS-1$
    /** The key used by the file dialog to save its filter path */
    public static final String TAG_IMG_FILTER_PATH = "IMG_FILTER_PATH"; //$NON-NLS-1$

    /** The default value of the file dialog file name */
    public static final String DEFAULT_IMG_FILE_NAME = "."; //$NON-NLS-1$
    /** The default value of the file dialog filter path */
    public static final String DEFAULT_IMG_FILTER_PATH = "."; //$NON-NLS-1$

    /** Image extension for jpg format */
    public static final String EXT_JPG = "*.jpg"; //$NON-NLS-1$
    /** Image extension for jpeg format */
    public static final String EXT_JPEG = "*.jpeg"; //$NON-NLS-1$
    /** Image extension for png format */
    public static final String EXT_PNG = "*.png"; //$NON-NLS-1$
    /** Image extension for gif format */
    public static final String EXT_GIF = "*.gif"; //$NON-NLS-1$
    /** The file extensions provided by the "save chart as image" file dialog */
    public static final String[] saveAsImageExt = { EXT_PNG, EXT_GIF, EXT_JPG, EXT_JPEG, "*.*" }; //$NON-NLS-1$
    /** The names associated to the files extensions provided by the "save chart as image" file dialog */
    public static final String[] saveAsImageExtNames;

    public static final String ChartConstants_SAVE_CHART_AS_TITLE;

    public static String ChartConstants_ALL_FILES;
    public static String ChartConstants_BAR_GRAPH;
    public static String ChartConstants_CHART_BUILDER;
    public static String ChartConstants_CONFIRM_OVERWRITE_MSG;
    public static String ChartConstants_CONFIRM_OVERWRITE_TITLE;
    public static String ChartConstants_CREATE_CHART;
    public static String ChartConstants_CREATE_NEW_CHART_FROM_SELECTION;
    public static String ChartConstants_DESELECT_ALL;
    public static String ChartConstants_ERROR_SAVING_CHART;
    public static String ChartConstants_NO_COLUMN_SELECTED;
    public static String ChartConstants_PIE_CHART;
    public static String ChartConstants_SAVE_CHART_AS;
    public static String ChartConstants_SAVE_CHART_DIALOG_TEXT;
    public static String ChartConstants_SELECT_ALL;
    public static String ChartConstants_SELECT_COLUMNS_TO_SHOW;
    public static String ChartConstants_SELECT_YOUR_CHART_TYPE;
    public static String ChartConstants_VERTICAL_BARS;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
        saveAsImageExtNames = new String[] { "PNG (*.png)", "GIF (*.gif)", "JPEG (*.jpg)", "JPEG (*.jpeg)", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                ChartConstants_ALL_FILES };
        ChartConstants_SAVE_CHART_AS_TITLE = ChartConstants_SAVE_CHART_AS + "..."; //$NON-NLS-1$
    }

    private Messages() {
    }
}
