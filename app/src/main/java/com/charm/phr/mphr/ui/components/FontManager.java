package com.charm.phr.mphr.ui.components;

/*
 * This class handle the font required by the application. Views can request the appropriate typeface based on the
 * font type. Custom fonts can be associated through FontFactory implementations
 */
public class FontManager
{
    public enum Font
    {
        REGULAR("Regular"),BOLD("Bold"),MEDIUM("Medium"),ITALIC("Italic"),BOLD_ITALIC("BoldItalic"),SBOLD("SBold"),THIN("Thin"),LIGHT("Light"); //No I18N
        private String name;
        private Font(String fontName)
        {
            this.name = fontName;
        }

        public static Font getFontType(String name)
        {
            Font[] vals = values();
            for(Font ft : vals)
            {
                if(ft.name().equalsIgnoreCase(name))
                {
                    return ft;
                }
            }
            return Font.REGULAR;
        }
    }

 /*   private static HashMap<Font, Typeface> fonts = new HashMap<Font, Typeface>();
    private static FontFactory factory = null;

    private FontManager()
    {}

    public static void initialize(Application appContext)
    {
        fonts.clear();
        factory = new RobotoFontFactory(appContext);
    }

    public static void initialize(FontFactory fontfactory)
    {
        fonts.clear();
        factory = fontfactory;
    }

    public static Typeface getTypeface(Font type)
    {
        Typeface typeface = fonts.get(type);
        if(typeface == null)
        {
            typeface = factory.prepareTypeFace(type);
            fonts.put(type,typeface);
        }
        return typeface;
    }

    public static String getCurrentFactoryName()
    {
        if(factory != null)
        {
            return factory.getName();
        }
        return null;
    }*/

}

