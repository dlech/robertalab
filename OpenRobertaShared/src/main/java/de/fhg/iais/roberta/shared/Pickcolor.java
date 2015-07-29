package de.fhg.iais.roberta.shared;

import java.util.Locale;

import de.fhg.iais.roberta.util.dbc.DbcException;

/**
 * All colors that are legal.
 */
public enum Pickcolor {

    RED( 0, "#B30006", "ROT", "#b30006" ), GREEN( 1, "#00642E", "GRÜN" ), BLUE( 2, "#0057A6" ), YELLOW( 3, "#F7D117" ), MAGENTA( 4, "#000001" ), ORANGE(
        5,
        "#000002" ), WHITE( 6, "#FFFFFF", "WEIß", "WEISS", "#ffffff" ), BLACK( 7, "#000000" ), PINK( 8, "#000003" ), GRAY( 9, "#000004" ), LIGHT_GRAY(
        10,
        "#000005" ), DARK_GRAY( 11, "#000006" ), CYAN( 12, "#000007" ), BROWN( 13, "#532115" ), NONE( -1, "#585858" );

    private final String[] values;
    private final int colorID;

    private Pickcolor(int colorID, String... values) {
        this.values = values;
        this.colorID = colorID;
    }

    public int getColorID() {
        return this.colorID;
    }

    public String getHex() {
        return this.values[0];
    }

    public static Pickcolor get(int id) {
        for ( Pickcolor sp : Pickcolor.values() ) {
            if ( sp.colorID == id ) {
                return sp;
            }
        }
        throw new DbcException("Invalid color: " + id);
    }

    /**
     * get {@link Pickcolor} from string parameter. It is possible for one color to have multiple string mappings.
     * Throws exception if the color cannot be found.
     *
     * @param name of the color
     * @return enum {@link Pickcolor}
     */
    public static Pickcolor get(String s) {
        if ( s == null || s.isEmpty() ) {
            throw new DbcException("Color missing");
        }
        String sUpper = s.trim().toUpperCase(Locale.GERMAN);
        for ( Pickcolor sp : Pickcolor.values() ) {
            if ( sp.toString().equals(sUpper) ) {
                return sp;
            }
            for ( String value : sp.values ) {
                if ( sUpper.equals(value) ) {
                    return sp;
                }
            }
        }
        throw new DbcException("Invalid color: " + s);
    }
}
