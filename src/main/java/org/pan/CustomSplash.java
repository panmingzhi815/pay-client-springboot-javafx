package org.pan;

import de.felixroske.jfxsupport.SplashScreen;

/**
 * @author panmingzhi
 */
public class CustomSplash extends SplashScreen {
    @Override
    public boolean visible() {
        return super.visible();
    }

    @Override
    public String getImagePath() {
        return "/image/banner.jpg";
    }
}
